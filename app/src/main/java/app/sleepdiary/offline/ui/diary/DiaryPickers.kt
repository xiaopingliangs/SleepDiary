package app.sleepdiary.offline.ui.diary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.EditText
import android.text.Editable
import android.text.TextWatcher
import android.text.InputType
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun TimePickerDialog(field:DiaryField,initial:LocalDateTime,advancedInitially:Boolean=false,onDismiss:()->Unit,onConfirm:(LocalDateTime)->Unit,onPosition:(LocalDateTime)->LocalDateTime={it}) {
    var valueText by rememberSaveable(field.name,initial.toString()) {mutableStateOf(initial.toString())}
    val candidate=LocalDateTime.parse(valueText)
    var advanced by rememberSaveable {mutableStateOf(advancedInitially)}
    Dialog(onDismissRequest=onDismiss) {
        Surface(shape=MaterialTheme.shapes.large) {
            Column(Modifier.fillMaxWidth().heightIn(max=(LocalConfiguration.current.screenHeightDp*0.9).dp).verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Text(field.label,style=MaterialTheme.typography.titleMedium)
                Text(candidate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),Modifier.testTag("picker_candidate"))
                if(advanced) AndroidView(factory={context->android.widget.DatePicker(context,null,0,app.sleepdiary.offline.R.style.SleepDiaryDatePicker).apply {
                    calendarViewShown=false;spinnersShown=true;descendantFocusability=android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
                    init(candidate.year,candidate.monthValue-1,candidate.dayOfMonth){_,year,month,day->valueText=LocalDate.of(year,month+1,day).atTime(LocalDateTime.parse(valueText).toLocalTime()).toString()}
                }},modifier=Modifier.fillMaxWidth().testTag("picker_date").semantics{contentDescription="调整日期"})
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    NumberWheel("小时",(0..23).toList(),candidate.hour,Modifier.weight(1f),"hour"){valueText=candidate.withHour(it).toString()}
                    val minutes=if(advanced)(0..59).toList()else (listOf(0,15,30,45)+initial.minute).distinct().sorted()
                    NumberWheel("分钟",minutes,candidate.minute,Modifier.weight(1f),"minute"){valueText=candidate.withMinute(it).toString()}
                }
                if(!advanced)OutlinedButton(onClick={advanced=true},modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).testTag("adjust_datetime")){Text("调整日期和时间")}
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End) {
                    TextButton(onClick=onDismiss,modifier=Modifier.heightIn(min=48.dp).testTag("picker_cancel")){Text("取消")}
                    Button(onClick={onConfirm(candidate)},modifier=Modifier.heightIn(min=48.dp).testTag("picker_confirm")){Text("确认")}
                }
            }
        }
    }
}
@Composable
private fun NumberWheel(label:String,values:List<Int>,selected:Int,modifier:Modifier,tag:String,onSelect:(Int)->Unit) {
    val latest by rememberUpdatedState(onSelect)
    val latestValues by rememberUpdatedState(values)
    val wheelTextColor=MaterialTheme.colorScheme.onSurface.toArgb()
    val index=values.indexOf(selected).coerceAtLeast(0)
    Column(modifier) {
        Text(label)
        AndroidView(factory={context->android.widget.NumberPicker(context).apply {
            descendantFocusability=android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
            minValue=0;maxValue=values.lastIndex;displayedValues=values.map{it.toString().padStart(2,'0')}.toTypedArray();value=index;wrapSelectorWheel=true
            setOnValueChangedListener{_,_,next->latest(latestValues[next])}
        }},update={view->view.displayedValues=null;view.maxValue=values.lastIndex;view.displayedValues=values.map{it.toString().padStart(2,'0')}.toTypedArray();view.value=index;view.contentDescription=label+"选择";if(android.os.Build.VERSION.SDK_INT>=29)view.textColor=wheelTextColor;for(i in 0 until view.childCount)(view.getChildAt(i) as? android.widget.EditText)?.setTextColor(wheelTextColor)},
            modifier=Modifier.fillMaxWidth().height(168.dp).testTag(tag+"_wheel").semantics {
                contentDescription=label+"选择";stateDescription=selected.toString()
                progressBarRangeInfo=ProgressBarRangeInfo(index.toFloat(),0f..values.lastIndex.toFloat(),values.size-2)
                setProgress {next->latest(values[next.toInt().coerceIn(0,values.lastIndex)]);true}
            })
    }
}

@Composable
fun NumberPickerDialog(field:DiaryField,initial:Int?,onDismiss:()->Unit,onConfirm:(Int)->Unit) {
    val range=if(field==DiaryField.COUNT) 6..99 else 0..1440
    var text by rememberSaveable { mutableStateOf(initial?.takeIf { it in range }?.toString() ?: "") }
    val number=text.toIntOrNull()?.takeIf { it in range }
    AlertDialog(onDismissRequest=onDismiss,title={Text(field.label)},text={Column {
        FormTextInput(text,{text=it},field.unit+"（"+range.first+"–"+range.last+"）","number_input",keyboardType=KeyboardType.Number)
        if(text.isNotEmpty()&&number==null) Text("请选择范围内的明确整数")
    }},confirmButton={TextButton(onClick={onConfirm(checkNotNull(number))},enabled=number!=null,modifier=Modifier.heightIn(min=48.dp).testTag("number_confirm")){Text("确认")}},
        dismissButton={TextButton(onClick=onDismiss,modifier=Modifier.heightIn(min=48.dp)){Text("取消")}})
}


/** Native text editing inside Compose; candidate state remains owned by the dialog/form. */
@Composable
fun FormTextInput(value:String,onChange:(String)->Unit,label:String,tag:String,enabled:Boolean=true,multiline:Boolean=false,keyboardType:KeyboardType=KeyboardType.Text) {
    val latestChange by rememberUpdatedState(onChange)
    val holder=remember {arrayOfNulls<EditText>(1)}
    var inputFocused by remember {mutableStateOf(false)}
    val paddingPx=with(LocalDensity.current) {12.dp.roundToPx()}
    val textColor=MaterialTheme.colorScheme.onSurface.toArgb()
    val textSize=with(LocalDensity.current) {MaterialTheme.typography.bodyLarge.fontSize.toPx()}
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(6.dp)) {
        Text(label,style=MaterialTheme.typography.bodyMedium)
        AndroidView(factory={context ->
            EditText(context).apply {
                holder[0]=this
                inputType=if(keyboardType==KeyboardType.Number) InputType.TYPE_CLASS_NUMBER else InputType.TYPE_CLASS_TEXT or (if(multiline) InputType.TYPE_TEXT_FLAG_MULTI_LINE else 0)
                setSingleLine(!multiline)
                background=null
                setPadding(paddingPx,paddingPx,paddingPx,paddingPx)
                onFocusChangeListener=android.view.View.OnFocusChangeListener {_,hasFocus -> inputFocused=hasFocus}
                addTextChangedListener(object:TextWatcher {
                    override fun beforeTextChanged(s:CharSequence?,start:Int,count:Int,after:Int) {}
                    override fun onTextChanged(s:CharSequence?,start:Int,before:Int,count:Int) {latestChange(s?.toString() ?: "")}
                    override fun afterTextChanged(s:Editable?) {}
                })
            }
        },update={editor ->
            editor.isEnabled=enabled
            editor.contentDescription=label
            editor.setTextColor(textColor)
            editor.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,textSize)
            if(editor.text.toString()!=value) {editor.setText(value);editor.setSelection(editor.text.length)}
        },modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).border(1.dp,MaterialTheme.colorScheme.outline,MaterialTheme.shapes.medium)
            .testTag(tag).semantics {
                contentDescription=label
                editableText=AnnotatedString(value)
                focused=inputFocused
                requestFocus {holder[0]?.requestFocus() ?: false}
                setText {text -> if(enabled){holder[0]?.setText(text.text);true}else false}
                insertTextAtCursor {text ->
                    val editor=holder[0]
                    if(!enabled || editor==null) false else {
                        val start=editor.selectionStart.coerceAtLeast(0)
                        val end=editor.selectionEnd.coerceAtLeast(0)
                        editor.text.replace(minOf(start,end),maxOf(start,end),text.text)
                        true
                    }
                }
                if(!enabled) disabled()
            })
    }
}
