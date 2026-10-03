package app.sleepdiary.offline.ui

import app.sleepdiary.offline.application.AppServices
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import app.sleepdiary.offline.ui.diary.*
import app.sleepdiary.offline.domain.model.*
import app.sleepdiary.offline.domain.weekly.RecentAverageCalculator
import app.sleepdiary.offline.share.DiaryShare
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.util.Locale

const val HISTORY_EMPTY_TEXT="这一天没有睡眠记录，不需要补写。睡眠日记关注的是一段时间内的大致模式，不要求精确到分钟，也不建议为了补记反复回想或查看设备数据。"
const val FIRST_SAVE_EXPLANATION="为了让记录尽量简单，也减少对单个夜晚数据的反复关注，日常记录完成后应用会结束本次使用。每完成一个 7 天记录周期，再集中查看一次整体记录。"
@Composable fun IntroScreen(page:Int,onNext:()->Unit,enabled:Boolean=true) {
 val titles=listOf("睡眠日记","数据留在本机","SleepDiary Public Edition")
 val bodies=listOf("记录每日主观估计，查看中性统计，按需阅读睡眠知识。当前版本不诊断，也不提供 CBT-I 治疗。","不需要账号，不联网。只有你主动导出或分享时，数据才会离开应用。","你可以使用今天、睡眠、学习和我的四个区域。教育内容用于了解一般概念，不是治疗建议；当前不启用 CBT-I 治疗。")
 SimplePage(titles[page]){Text(bodies[page]);Action(if(page==2)"开始使用"else"下一页",onNext,enabled)}
}
@Composable fun SimplePage(title:String,content:@Composable ColumnScope.()->Unit){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text(title,style=MaterialTheme.typography.headlineSmall);content()}}
@Composable fun Action(label:String,action:()->Unit,enabled:Boolean=true,modifier:Modifier=Modifier){Button(onClick=action,enabled=enabled,modifier=modifier.fillMaxWidth().heightIn(min=48.dp)){Text(label)}}

@Composable fun TodayScreen(state:DiaryUiState,onExit:()->Unit,onEdit:()->Unit,onHistory:()->Unit,onSettings:()->Unit) {
 var showAverages by rememberSaveable{mutableStateOf(false)}
 var selectedDays by rememberSaveable{mutableIntStateOf(7)}
 val today=state.today?.sleepDate?.let(LocalDate::parse)
 val averages=remember(state.entries,today,selectedDays){today?.let{RecentAverageCalculator.calculate(it,selectedDays,state.entries)}}
 SimplePage("今天的睡眠记录已经完成") {
  Text("为减少对单个夜晚数据的反复关注，通常不需要再次查看今天的记录。")
  OutlinedButton(onClick={showAverages=!showAverages},modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)){Text(if(showAverages)"收起最近平均数据" else "查看最近平均数据")}
  if(showAverages) {
   Text("最近平均数据",style=MaterialTheme.typography.titleMedium)
   Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
    FilterChip(selected=selectedDays==7,onClick={selectedDays=7},label={Text("最近 7 天")},modifier=Modifier.heightIn(min=48.dp))
    FilterChip(selected=selectedDays==14,onClick={selectedDays=14},label={Text("最近 14 天")},modifier=Modifier.heightIn(min=48.dp))
   }
   averages?.let { value ->
    Text("有记录的天数：${value.validEntryCount} / $selectedDays")
    val labels=listOf("平均估计入睡耗时（分钟）","平均夜醒次数","平均夜间清醒总时长（分钟）","平均最后醒来到离床时长（分钟）","平均在睡觉位置总时长 TIB（不等于睡眠时长）","平均主观睡眠质量（1很差 — 5很好）")
    val means=listOf(value.avgSolMin,value.avgAwakeningCount,value.avgWasoMin,value.avgTerminalWakefulnessMin,value.avgTibMin,value.avgSleepQuality)
    labels.zip(means).forEachIndexed{index,(label,mean)->val shown=mean?.let{if(index==4) AverageDurationText.fromMinutes(it) else String.format(Locale.ROOT,"%.1f",it)}?:"无记录";Text(label+"："+shown)}
   }
   Text("按今天向前计算；没有记录的日期不会按 0 计算。这只是已有主观记录的实时平均，不是睡眠评分或治疗指标。",style=MaterialTheme.typography.bodySmall)
  }
  Action("退出",onExit)
  OutlinedButton(onClick=onEdit,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)){Text("继续查看")}
  Action("查看历史记录",onHistory)
  Action("设置",onSettings)
 }
}
fun safetyAnswerLabel(code:String)=mapOf("YES" to "是","NO" to "否","UNSURE" to "不确定","NOT_APPLICABLE" to "不适用","NONE" to "没有／以上都没有","FLAGS_PRESENT" to "已选择具体提示","STABLE" to "目前控制稳定","UNSTABLE" to "近期仍有发作或控制不稳定","OCCASIONAL" to "偶尔","FREQUENT" to "经常","DANGER_OCCURRED" to "曾因此出现危险","SAFE" to "安全","POSSIBLY_UNSAFE" to "可能不安全")[code]?:code
fun safetyDetailLabel(json:String):String {val o=org.json.JSONObject(json);val names=S10_CHOICES.toMap()+mapOf("LOUD_SNORING" to "经常大声打鼾","WITNESSED_APNEA" to "睡眠中被别人发现暂停呼吸","GASPING_AWAKENING" to "经常憋醒或喘醒","DIAGNOSED_OSA" to "医生已诊断 OSA");val a=o.optJSONArray("selectedCodes")?:o.optJSONArray("diagnoses")?:org.json.JSONArray();return (0 until a.length()).joinToString("、"){names[a.getString(it)]?:a.getString(it)}+o.optString("otherText","")}
