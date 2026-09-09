package app.sleepdiary.offline.ui.diary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val DIARY_HINT="以下时间、次数和时长按记忆大致估计即可。无需为了填写日记查看时钟、手环或其他睡眠设备，也不需要追求分钟级准确。"
const val CONSISTENCY_TITLE="这些数字可能有一项选错了。"
const val CONSISTENCY_BODY="你填写的入睡耗时和夜间清醒时长，加起来已经超过了从“开始尝试入睡”到“最后醒来”的总时间。因为这些本来就是估计值，可以有误差。你可以返回检查，也可以确认按当前估计保存。"
val QUALITY_LABELS=listOf("很差","较差","一般","较好","很好")

@Composable
fun DiaryScreen(state:DiaryUiState,onTime:(DiaryField,LocalDateTime)->Unit,onNumber:(DiaryField,Int)->Unit,onComment:(String)->Unit,
    onSave:(Boolean)->Unit,onDismissWarning:()->Unit,onSettings:()->Unit) {
    var timeField by rememberSaveable { mutableStateOf<String?>(null) }
    var advanced by rememberSaveable { mutableStateOf(false) }
    var numberField by rememberSaveable { mutableStateOf<String?>(null) }
    var commentExpanded by rememberSaveable { mutableStateOf(!state.form.comment.isNullOrEmpty()) }
    val list=rememberLazyListState()
    LaunchedEffect(state.missing) { state.missing?.let { list.animateScrollToItem(it.ordinal) } }
    Column(Modifier.fillMaxSize().testTag("diary_screen")) {
        Row(Modifier.fillMaxWidth().padding(horizontal=16.dp),horizontalArrangement=Arrangement.SpaceBetween) {
            Text("睡眠日记",Modifier.weight(1f).padding(top=12.dp),style=MaterialTheme.typography.titleLarge)
            TextButton(onClick=onSettings,enabled=!state.busy,modifier=Modifier.heightIn(min=48.dp)) {Text("设置")}
        }
        Text(DIARY_HINT,Modifier.padding(horizontal=20.dp,vertical=8.dp),style=MaterialTheme.typography.bodyMedium)
        LazyColumn(state=list,modifier=Modifier.weight(1f).fillMaxWidth().testTag("diary_fields"),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            items(DiaryField.entries,key={it.name}) { field ->
                Card(Modifier.fillMaxWidth().testTag("field_"+field.name),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                        Text(field.label,style=MaterialTheme.typography.titleMedium)
                        if(field in listOf(DiaryField.BED,DiaryField.TRY,DiaryField.FINAL,DiaryField.OUT)) {
                            val current=state.form.time(field)
                            val value=current?.format(DateTimeFormatter.ofPattern("MM-dd HH:mm")) ?: "未填写"
                            OutlinedButton(onClick={advanced=false;timeField=field.name},enabled=!state.busy,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).testTag("time_"+field.name).semantics {contentDescription=field.label;stateDescription=value}) {Text(value)}
                            TextButton(onClick={advanced=true;timeField=field.name},enabled=!state.busy,modifier=Modifier.heightIn(min=48.dp).semantics {contentDescription=field.label+" 日期不对？调整"}) {Text("日期不对？调整")}
                            if(field==DiaryField.BED) Text("这可能早于你真正开始尝试睡觉的时间。",style=MaterialTheme.typography.bodySmall)
                            if(field==DiaryField.FINAL) Text("指之后没有再真正睡着的那次。",style=MaterialTheme.typography.bodySmall)
                        } else {
                            val current=state.form.number(field)
                            Text(if(current==null) "未填写" else if(field==DiaryField.QUALITY) QUALITY_LABELS[current-1] else current.toString()+" "+field.unit,
                                modifier=Modifier.semantics {stateDescription=if(current==null) "未填写" else "已填写"})
                            if(field==DiaryField.QUALITY) {
                                QUALITY_LABELS.forEachIndexed { index,label -> Choice(field,index+1,label,current,!state.busy,onNumber,Modifier.fillMaxWidth()) }
                            } else {
                                val choices=when(field){DiaryField.SOL -> listOf(5,10,15,20,30,45,60,90);DiaryField.COUNT -> listOf(0,1,2,3,4,5);else -> listOf(0,5,10,15,20,30,45,60,90,120)}
                                LazyRow(horizontalArrangement=Arrangement.spacedBy(8.dp),modifier=Modifier.fillMaxWidth().testTag("choices_"+field.name)) {
                                    items(choices) { value -> Choice(field,value,value.toString(),current,!state.busy,onNumber) }
                                    item { OutlinedButton(onClick={numberField=field.name},enabled=!state.busy,modifier=Modifier.heightIn(min=48.dp).testTag("other_"+field.name)) {Text(if(field==DiaryField.COUNT) "更多" else "其他")} }
                                }
                            }
                        }
                        if(state.missing==field) Text("请先完成这项记录",Modifier.semantics {liveRegion=LiveRegionMode.Polite})
                    }
                }
            }
            item {
                TextButton(onClick={commentExpanded=!commentExpanded},enabled=!state.busy,modifier=Modifier.heightIn(min=48.dp)){Text("＋ 昨晚有特殊情况（可选）")}
                if(commentExpanded) FormTextInput(state.form.comment ?: "",onComment,"特殊情况备注（可选）","comment",enabled=!state.busy,multiline=true)
            }
        }
        if(state.message!=null&&state.missing==null) Text(state.message,Modifier.padding(horizontal=20.dp).semantics {liveRegion=LiveRegionMode.Polite})
        Button(onClick={onSave(false)},shape=MaterialTheme.shapes.medium,enabled=!state.busy,modifier=Modifier.fillMaxWidth().padding(16.dp).heightIn(min=48.dp).testTag("save_diary")) {Text(if(state.busy) "正在保存…" else "保存记录")}
    }
    timeField?.let { name -> val field=DiaryField.valueOf(name)
        TimePickerDialog(field,state.form.positionAt(field,state.form.pickerPosition(field).toLocalTime()),advanced,onDismiss={timeField=null},onConfirm={onTime(field,it);timeField=null},onPosition={state.form.positionAt(field,it.toLocalTime())})
    }
    numberField?.let { name -> val field=DiaryField.valueOf(name)
        NumberPickerDialog(field,state.form.number(field),onDismiss={numberField=null},onConfirm={onNumber(field,it);numberField=null})
    }
    if(state.warning) AlertDialog(onDismissRequest=onDismissWarning,title={Text(CONSISTENCY_TITLE)},text={Text(CONSISTENCY_BODY)},
        confirmButton={TextButton(onClick={onSave(true)},modifier=Modifier.heightIn(min=48.dp)){Text("仍按估计保存")}},
        dismissButton={TextButton(onClick=onDismissWarning,modifier=Modifier.heightIn(min=48.dp)){Text("返回检查")}})
}

@Composable
private fun Choice(field:DiaryField,value:Int,label:String,current:Int?,enabled:Boolean,onNumber:(DiaryField,Int)->Unit,modifier:Modifier=Modifier) {
    OutlinedButton(onClick={onNumber(field,value)},enabled=enabled,
        modifier=modifier.sizeIn(minWidth=48.dp,minHeight=48.dp).testTag("choice_"+field.name+"_"+value).semantics {selected=current==value;contentDescription=field.label+" "+label+field.unit},
        colors=ButtonDefaults.outlinedButtonColors(containerColor=if(current==value) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface)) {Text(label)}
}
