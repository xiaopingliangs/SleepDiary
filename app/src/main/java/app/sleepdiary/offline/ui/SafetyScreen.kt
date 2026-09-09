package app.sleepdiary.offline.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.sleepdiary.offline.ui.diary.*
import app.sleepdiary.offline.data.entity.SafetyAnswer
import app.sleepdiary.offline.domain.safety.S09Codec
import org.json.JSONObject
import org.json.JSONArray
import java.util.UUID

val S10_CHOICES=listOf("RLS" to "不宁腿综合征（Restless legs syndrome, RLS）","NARCOLEPSY" to "发作性睡病（Narcolepsy）","RBD" to "REM 睡眠行为障碍（REM sleep behavior disorder, RBD）","SLEEPWALKING" to "梦游/睡行症（Sleepwalking）","SLEEP_TERRORS" to "夜惊（Sleep terrors）","CIRCADIAN_RHYTHM_DISORDER" to "昼夜节律睡眠-觉醒障碍（Circadian rhythm sleep-wake disorder）","OTHER" to "其他")
@Composable fun SafetyScreen(model:DiaryViewModel,state:DiaryUiState) {
 val original=state.safety
 val id by rememberSaveable {mutableStateOf(original?.assessment?.id?:UUID.randomUUID().toString())}
 var index by rememberSaveable {mutableStateOf(SAFETY_QUESTIONS.indexOfFirst{q->original?.answers?.none{it.questionId==q.id}?:true}.coerceAtLeast(0))}
 var sub by rememberSaveable {mutableStateOf(false)}
 var diagnoses by rememberSaveable {mutableStateOf(false)}
 var selected by rememberSaveable {mutableStateOf(arrayListOf<String>())}
 var other by rememberSaveable {mutableStateOf("")}
 var explain by rememberSaveable {mutableStateOf(false)}
 val q=SAFETY_QUESTIONS[index]
 fun submit(code:String,detail:String?=null) {
  val old=(state.safety?.takeIf{it.assessment.id==id}?.answers?:emptyList()).filterNot{it.questionId==q.id||q.id=="S07"&&it.questionId=="S07b"}
  val new=if(sub)listOf(SafetyAnswer(id,"S07","YES",detailJson=null),SafetyAnswer(id,"S07b",code,detailJson=null))else listOf(SafetyAnswer(id,q.id,code,detailJson=detail))
  model.saveSafety(id,old+new,index==14){if(index<14)index++;sub=false;diagnoses=false;selected=arrayListOf();other="";explain=false}
 }
 SimplePage("安全问询 · ${index+1} / 15") {
  Text(if(sub)"最近是否仍有发作，或医生认为控制不稳定？" else q.text,style=MaterialTheme.typography.titleMedium)
  TextButton(onClick={explain=!explain},modifier=Modifier.heightIn(min=48.dp)){Text("为什么问这个？")}
  if(explain)Text(q.help)
  state.message?.let{Text(it,color=MaterialTheme.colorScheme.error)}
  when {
   q.id=="S09" -> {
    val options=listOf("LOUD_SNORING" to "经常大声打鼾","WITNESSED_APNEA" to "睡眠中被别人发现暂停呼吸","GASPING_AWAKENING" to "经常憋醒或喘醒","DIAGNOSED_OSA" to "已被医生诊断阻塞性睡眠呼吸暂停（OSA）","NONE" to "以上都没有","UNSURE" to "不确定")
    options.forEach{(code,label)->OutlinedButton(onClick={selected=if(code in listOf("NONE","UNSURE"))arrayListOf(code)else ArrayList(selected.filterNot{it in listOf("NONE","UNSURE")}.let{if(code in it)it-code else it+code})},enabled=!state.busy,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)){Text((if(code in selected)"✓ "else "")+label)}}
    Action("确认",{val a=S09Codec.fromSelection(id,selected);submit(a.answerCode,a.detailJson)},selected.isNotEmpty()&&!state.busy)
   }
   diagnoses -> {
    Text("仅选择曾被医生诊断的名称；可以不选具体名称。")
    S10_CHOICES.forEach{(code,label)->OutlinedButton(onClick={selected=ArrayList(if(code in selected)selected-code else selected+code)},enabled=!state.busy,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)){Text((if(code in selected)"✓ "else "")+label)}}
    if("OTHER" in selected)FormTextInput(other,{other=it},"其他名称（可选）","s10_other")
    Action("确认",{submit("YES",JSONObject().put("diagnoses",JSONArray(selected)).put("otherText",other).toString())},!state.busy)
   }
   else -> {
    val options=when {
     sub->listOf("STABLE" to "目前控制稳定，近期无发作，或医生认为控制稳定","UNSTABLE" to "近期仍有发作，或医生认为控制不稳定","UNSURE" to "不确定")
     q.id=="S01"->listOf("YES" to "是","NO" to "否")
     q.id=="S08"->listOf("NONE" to "没有","OCCASIONAL" to "偶尔","FREQUENT" to "经常","DANGER_OCCURRED" to "曾因此出现危险")
     q.id=="S11"->listOf("SAFE" to "安全","POSSIBLY_UNSAFE" to "可能不安全","UNSURE" to "不确定")
     else->listOf("NO" to "否","YES" to "是","UNSURE" to "不确定")+if(q.id in listOf("S13","S14"))listOf("NOT_APPLICABLE" to "不适用")else emptyList()
    }
    options.forEach{(code,label)->Action(label,{if(q.id=="S07"&&code=="YES"&&!sub)sub=true else if(q.id=="S10"&&code=="YES")diagnoses=true else submit(code)},!state.busy)}
   }
  }
 }
}
