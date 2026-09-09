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
    // These are dialog-only candidate values. Dismissal never invokes onConfirm.
    var valueText by rememberSaveable(field.name,initial.toString()) { mutableStateOf(initial.toString()) }
    val candidate=LocalDateTime.parse(valueText)
    var advanced by rememberSaveable { mutableStateOf(advancedInitially) }
    var dateText by rememberSaveable { mutableStateOf(initial.toLocalDate().toString()) }
    var timeText by rememberSaveable { mutableStateOf(initial.format(DateTimeFormatter.ofPattern("HH:mm"))) }
    val precise=runCatching { LocalDateTime.of(LocalDate.parse(dateText),LocalTime.parse(timeText)) }.getOrNull()
    Dialog(onDismissRequest=onDismiss) {
        Surface(shape=MaterialTheme.shapes.large,color=MaterialTheme.colorScheme.surface) {
            Column(Modifier.fillMaxWidth().heightIn(max=(LocalConfiguration.current.screenHeightDp*0.85).dp).verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                Text(field.label,style=MaterialTheme.typography.titleMedium)
                Text(candidate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),Modifier.testTag("picker_candidate"),style=MaterialTheme.typography.titleLarge)
                if(!advanced) {
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                        NumberColumn("小时",(0..23).toList(),candidate.hour,Modifier.weight(1f),"hour") { valueText=onPosition(candidate.withHour(it)).toString() }
                        val minutes=(listOf(0,15,30,45)+candidate.minute).distinct().sorted()
                        NumberColumn("分钟",minutes,candidate.minute,Modifier.weight(1f),"minute") { valueText=onPosition(candidate.withMinute(it)).toString() }
                    }
                    TextButton(onClick={dateText=candidate.toLocalDate().toString();timeText=candidate.format(DateTimeFormatter.ofPattern("HH:mm"));advanced=true},modifier=Modifier.heightIn(min=48.dp)) { Text("日期不对？调整 / 精细输入") }
                } else {
                    FormTextInput(dateText,{dateText=it},"日期（YYYY-MM-DD）","picker_date")
                    FormTextInput(timeText,{timeText=it},"时间（24 小时制 HH:mm）","picker_time")
                    if(precise==null) Text("请填写有效日期和时间，例如 2026-09-09、08:15")
                }
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End) {
                    TextButton(onClick=onDismiss,modifier=Modifier.heightIn(min=48.dp).testTag("picker_cancel")) { Text("取消") }
                    Button(onClick={onConfirm(if(advanced) checkNotNull(precise) else candidate)},enabled=!advanced||precise!=null,
                        modifier=Modifier.heightIn(min=48.dp).testTag("picker_confirm")) { Text("确认") }
                }
            }
        }
    }
}

@Composable
private fun NumberColumn(label:String,values:List<Int>,selected:Int,modifier:Modifier,tag:String,onSelect:(Int)->Unit) {
    Column(modifier) {
        Text(label)
        val list=rememberLazyListState(initialFirstVisibleItemIndex=(values.indexOf(selected)-1).coerceAtLeast(0))
        LazyColumn(Modifier.height(168.dp).fillMaxWidth().semantics { contentDescription=label+"选择" },state=list) {
            items(values) { number ->
                OutlinedButton(onClick={onSelect(number)},modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).testTag(tag+"_"+number).semantics { this.selected=number==selected;contentDescription=number.toString()+label },
                    colors=ButtonDefaults.outlinedButtonColors(containerColor=if(number==selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface)) {
                    Text(number.toString().padStart(2,'0'))
                }
            }
        }
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
