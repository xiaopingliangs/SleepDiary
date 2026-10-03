package app.sleepdiary.offline.ui.diary

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import app.sleepdiary.offline.domain.validation.DiaryValidator
import java.time.*
import java.time.format.DateTimeFormatter

const val DIARY_HINT="按记忆大致估计即可。尤其不要为了记录，在夜间特意查看时间、手表、手环或睡眠设备。"
const val CONSISTENCY_TITLE="这些数字可能有一项选错了。"
const val CONSISTENCY_BODY="你填写的入睡耗时和夜间清醒时长，加起来已经超过了从“开始尝试入睡”到“最后醒来”的总时间。因为这些本来就是估计值，可以有误差。你可以返回检查，也可以确认按当前估计保存。"
val QUALITY_LABELS=listOf("很差","较差","一般","较好","很好")
val FIELD_HELP=listOf(
 "指你进入平时准备睡觉的位置，比如上床。它可能早于你真正开始尝试睡觉的时间。例如上床以后还看了一会儿手机，这两个时间就会不同。",
 "从躺到睡觉位置开始，到你真正开始尝试入睡，大约经过多久。选择估计值即可。",
 "从你真正开始尝试睡觉，到你觉得自己睡着，大概用了多久。按印象估计即可。",
 "指真正睡着以后，到最后一次醒来之前，大概醒过几次。",
 "把这些夜里醒来的清醒时间大概加起来。不需要准确计时。",
 "指这次醒来以后，没有再真正睡着的那一次。",
 "从最后一次醒来到离开床或平时睡觉的位置，大约经过多久。",
 "只按你自己的总体感觉选择，不需要根据手环或其他设备判断。")
val TIME_FIELDS=listOf(DiaryField.BED,DiaryField.FINAL)
val RELATIVE_FIELDS=listOf(DiaryField.TRY,DiaryField.OUT)
val TIME_IDS=mapOf("bedEnteredAtLocal" to DiaryField.BED,"trySleepAtLocal" to DiaryField.TRY,"finalAwakeningAtLocal" to DiaryField.FINAL,"outOfBedAtLocal" to DiaryField.OUT)
fun fieldValue(form:DiaryForm,field:DiaryField):String=when {
 field in TIME_FIELDS -> form.time(field)?.format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))?:"未填写"
 field in RELATIVE_FIELDS -> form.delay(field)?.let{if(field==DiaryField.TRY&&it==0)"几乎立刻" else "约 $it 分钟"}?:"未填写"
 else -> form.number(field)?.let{if(field==DiaryField.QUALITY)QUALITY_LABELS[it-1]else "$it ${field.unit}"}?:"未填写"
}

@Composable
fun DiaryScreen(state:DiaryUiState,onTime:(DiaryField,LocalDateTime)->Unit,onNumber:(DiaryField,Int)->Unit,onComment:(String)->Unit,
 onSave:(Boolean)->Unit,onDismissWarning:()->Unit,onSettings:()->Unit,onDelay:(DiaryField,Int)->Unit = { _, _ -> }) {
 var active by rememberSaveable {mutableStateOf<String?>(null)}
 var other by rememberSaveable {mutableStateOf<String?>(null)}
 var help by rememberSaveable {mutableStateOf<String?>(null)}
 var commentExpanded by rememberSaveable {mutableStateOf(false)}
 var advancedTime by rememberSaveable {mutableStateOf<String?>(null)}
 val list=rememberLazyListState()
 // Relations can be checked as soon as both values exist; placeholders remain UI-only.
 val f=state.form
 val conflicts=buildList {
  fun pair(a:LocalDateTime?,b:LocalDateTime?,x:DiaryField,y:DiaryField){if(a!=null&&b!=null&&a>b)add(Triple(x,y,"‘${y.label}’不能早于‘${x.label}’。"))}
  pair(f.bed,f.time(DiaryField.TRY),DiaryField.BED,DiaryField.TRY);pair(f.time(DiaryField.TRY),f.final,DiaryField.TRY,DiaryField.FINAL);pair(f.final,f.time(DiaryField.OUT),DiaryField.FINAL,DiaryField.OUT)
 }
 val firstError=state.missing?:conflicts.firstOrNull()?.first
 LaunchedEffect(state.message,firstError){if(state.message!=null)firstError?.let{list.animateScrollToItem(it.ordinal)}}
 Column(Modifier.fillMaxSize().testTag("diary_screen")) {
  Row(Modifier.fillMaxWidth().padding(horizontal=16.dp),horizontalArrangement=Arrangement.SpaceBetween){Text("睡眠日记",Modifier.weight(1f).padding(top=12.dp),style=MaterialTheme.typography.titleLarge);TextButton(onClick=onSettings,enabled=!state.busy,modifier=Modifier.heightIn(min=48.dp)){Text("设置")}}
  Row(Modifier.padding(horizontal=16.dp)){Text(DIARY_HINT,Modifier.weight(1f).padding(vertical=8.dp).testTag("diary_estimate_hint"),style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurface);TextButton(onClick={help=DIARY_HINT},modifier=Modifier.sizeIn(minWidth=48.dp,minHeight=48.dp).semantics{contentDescription="记录说明"}){Text("ⓘ")}}
  LazyColumn(state=list,modifier=Modifier.weight(1f).fillMaxWidth().testTag("diary_fields"),contentPadding=PaddingValues(horizontal=16.dp)) {
   items(DiaryField.entries,key={it.name}){field->
    val errors=conflicts.filter{it.first==field||it.second==field}
    val color=if(errors.isNotEmpty())MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    Column(Modifier.fillMaxWidth().testTag("field_${field.name}").semantics{if(errors.isNotEmpty())error(errors.first().third)}) {
     Row(Modifier.fillMaxWidth()) {
      Row(Modifier.weight(1f).heightIn(min=56.dp).clickable(enabled=!state.busy){active=field.name}.padding(vertical=12.dp).testTag(if(field in TIME_FIELDS)"time_${field.name}" else "input_${field.name}").semantics{contentDescription=field.label;stateDescription=fieldValue(f,field)},horizontalArrangement=Arrangement.SpaceBetween){
       Text(field.label,Modifier.weight(1f),color=color);Text(fieldValue(f,field),Modifier.padding(start=8.dp),color=color,style=MaterialTheme.typography.bodyMedium)
      }
      TextButton(onClick={help=FIELD_HELP[field.ordinal]},modifier=Modifier.sizeIn(minWidth=48.dp,minHeight=48.dp).semantics{contentDescription=field.label+"帮助"}){Text("?")}
     }
     errors.forEach{Text(it.third,color=color,style=MaterialTheme.typography.bodySmall)}
     if(state.missing==field)Text("请先完成这项记录",color=MaterialTheme.colorScheme.error)
     HorizontalDivider()
    }
   }
   item{TextButton(onClick={commentExpanded=!commentExpanded},enabled=!state.busy,modifier=Modifier.heightIn(min=48.dp)){Text("＋ 有什么特殊情况吗？（可选）")};if(commentExpanded){Text("如果昨晚有和平时明显不同的情况，可以简单记一句；没有就不用填写。",style=MaterialTheme.typography.bodySmall);CommentTextInput(f.comment?:"",onComment,"特殊情况备注（可选）","comment",enabled=!state.busy)}}
  }
  state.message?.let{Text(if(conflicts.isNotEmpty())conflicts.first().third else it,Modifier.padding(horizontal=16.dp).semantics{liveRegion=LiveRegionMode.Polite})}
  Button(onClick={onSave(false)},enabled=!state.busy,modifier=Modifier.fillMaxWidth().padding(16.dp).heightIn(min=48.dp).testTag("save_diary")){Text(if(state.busy)"正在保存…" else "保存记录")}
 }
 active?.let {name->val field=DiaryField.valueOf(name)
  if(field in TIME_FIELDS)TimePickerDialog(field,f.pickerPosition(field),onDismiss={active=null},onConfirm={onTime(field,it);active=null})
  else if(field in RELATIVE_FIELDS) AlertDialog(onDismissRequest={active=null},title={Text(field.label)},text={
   Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
    Text("按大概记忆选择即可。")
    LazyRow(Modifier.testTag("choices_${field.name}"),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
     val values=(0..120 step 5).toList()+(150..360 step 15).toList()+(420..1440 step 60).toList()
     items(values) { value ->
      val label=if(field==DiaryField.TRY&&value==0)"几乎立刻" else value.toString()
      OutlinedButton(onClick={onDelay(field,value);active=null},modifier=Modifier.sizeIn(minWidth=48.dp,minHeight=48.dp).testTag("choice_${field.name}_$value")){Text(label)}
     }
    }
    OutlinedButton(onClick={other=field.name;active=null},modifier=Modifier.heightIn(min=48.dp).testTag("other_${field.name}")){Text("其他")}
    TextButton(onClick={advancedTime=field.name;active=null},modifier=Modifier.heightIn(min=48.dp)){Text("调整具体日期和时间")}
   }
  },confirmButton={TextButton(onClick={active=null},modifier=Modifier.heightIn(min=48.dp)){Text("取消")}})
  else AlertDialog(onDismissRequest={active=null},title={Text(field.label)},text={
   Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)) {
    if(field==DiaryField.QUALITY)QUALITY_LABELS.forEachIndexed{i,label->Choice(field,i+1,label,f.number(field),onNumber={x,v->onNumber(x,v);active=null})}
    else {
     val values=when(field){DiaryField.SOL->listOf(5,10,15,20,30,45,60,90);DiaryField.COUNT->listOf(0,1,2,3,4,5);else->listOf(0,5,10,15,20,30,45,60,90,120)}
     LazyRow(Modifier.testTag("choices_${field.name}"),horizontalArrangement=Arrangement.spacedBy(8.dp)){items(values){v->Choice(field,v,v.toString(),f.number(field),onNumber={x,n->onNumber(x,n);active=null})}}
     OutlinedButton(onClick={other=field.name;active=null},modifier=Modifier.heightIn(min=48.dp).testTag("other_${field.name}")){Text(if(field==DiaryField.COUNT)"更多" else "其他")}
    }
   }
  },confirmButton={TextButton(onClick={active=null},modifier=Modifier.heightIn(min=48.dp)){Text("取消")}})
 }
  other?.let{val field=DiaryField.valueOf(it);NumberPickerDialog(field,if(field in RELATIVE_FIELDS)f.delay(field) else f.number(field),onDismiss={other=null},onConfirm={if(field in RELATIVE_FIELDS)onDelay(field,it) else onNumber(field,it);other=null})}
  advancedTime?.let { name -> val field=DiaryField.valueOf(name);TimePickerDialog(field,f.time(field)?:f.pickerPosition(field),advancedInitially=true,onDismiss={advancedTime=null},onConfirm={onTime(field,it);advancedTime=null}) }
 help?.let{AlertDialog(onDismissRequest={help=null},text={Text(it)},confirmButton={TextButton(onClick={help=null},modifier=Modifier.heightIn(min=48.dp)){Text("知道了")}})}
 if(state.warning)AlertDialog(onDismissRequest=onDismissWarning,title={Text(CONSISTENCY_TITLE)},text={Text(CONSISTENCY_BODY)},confirmButton={TextButton(onClick={onSave(true)},modifier=Modifier.heightIn(min=48.dp)){Text("仍按估计保存")}},dismissButton={TextButton(onClick=onDismissWarning,modifier=Modifier.heightIn(min=48.dp)){Text("返回检查")}})
}
@Composable private fun Choice(field:DiaryField,value:Int,label:String,current:Int?,onNumber:(DiaryField,Int)->Unit){OutlinedButton(onClick={onNumber(field,value)},modifier=Modifier.sizeIn(minWidth=48.dp,minHeight=48.dp).testTag("choice_${field.name}_$value").semantics{selected=current==value;contentDescription=field.label+" "+label+field.unit}){Text(label)}}
