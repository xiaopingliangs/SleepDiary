package app.sleepdiary.offline.ui

import android.content.Context
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
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.backup.BackupRepository
import app.sleepdiary.offline.domain.model.*
import app.sleepdiary.offline.share.DiaryShare
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.util.Locale

const val HISTORY_EMPTY_TEXT="这一天没有睡眠记录，不需要补写。睡眠日记关注的是一段时间内的大致模式，不要求精确到分钟，也不建议为了补记反复回想或查看设备数据。"
const val FIRST_SAVE_EXPLANATION="为了让记录尽量简单，也减少对单个夜晚数据的反复关注，日常记录完成后应用会结束本次使用。每完成一个 7 天记录周期，再集中查看一次整体记录。"
class AppServices(context:Context,db:SleepDiaryDatabase) {
 val backup=BackupRepository(db,context.applicationContext)
 val share=DiaryShare(context.applicationContext,db)
}
@Composable fun SleepDiaryApp(model:DiaryViewModel,onExit:()->Unit,services:AppServices?=null) {
 val state by model.state.collectAsState();val nav=rememberNavController();val context=LocalContext.current;val scope=rememberCoroutineScope()
 var message by remember{mutableStateOf<String?>(null)};var busy by remember{mutableStateOf(false)}
 var exportKind by rememberSaveable{mutableStateOf<String?>(null)}
 var pendingRestore by remember{mutableStateOf<String?>(null)};var restoreSummary by remember{mutableStateOf("")}
 fun operation(block:suspend ()->Unit){if(busy)return;busy=true;scope.launch{try{block()}catch(c:CancellationException){throw c}catch(_:Exception){message="操作未完成，数据未确认成功。请重试。"}finally{busy=false}}}
 val exportJson=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")){uri->if(uri!=null)operation{val data=withContext(Dispatchers.IO){checkNotNull(services).backup.export()};withContext(Dispatchers.IO){checkNotNull(context.contentResolver.openOutputStream(uri,"wt")).use{it.write(data.toByteArray(Charsets.UTF_8))}};message="完整备份已导出"}}
 val exportCsv=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")){uri->if(uri!=null)operation{withContext(Dispatchers.IO){val data=checkNotNull(services).backup.csv();checkNotNull(context.contentResolver.openOutputStream(uri,"wt")).use{it.write(data)}};message="CSV 已导出"}}
 val importJson=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null)operation{val text=withContext(Dispatchers.IO){checkNotNull(context.contentResolver.openInputStream(uri)).bufferedReader(Charsets.UTF_8).use{it.readText()}};val preview=withContext(Dispatchers.IO){checkNotNull(services).backup.preview(text)};restoreSummary="睡眠记录 ${preview.entries.size} 条\n安全问询 ${preview.assessments.size} 份\n周总结（含历史修订）${preview.summaries.size} 份\n首次说明已完成：${if(preview.state.hasCompletedOnboarding)"是" else "否"}";pendingRestore=text}}
 fun share(){val entry=state.today?:return;operation{val intent=withContext(Dispatchers.IO){checkNotNull(services).share.prepare(entry)};model.exitAfterSuccess {DiaryShare.handOff(intent,{context.startActivity(it)},onExit)}}}
 Surface(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),color=MaterialTheme.colorScheme.background){
  NavHost(navController=nav,startDestination="home") {
   composable("home") {when(state.page){
    HomePage.LOADING->SimplePage("睡眠日记"){Text(state.message?:"正在读取记录…");state.message?.let{Action("重试",model::start)}}
    HomePage.INTRO-> {IntroScreen(state.introPage,model::nextIntro,!state.busy);state.message?.let{Text(it)}}
    HomePage.SAFETY->SafetyScreen(model,state)
    HomePage.HELP->SimplePage("请优先联系现实世界的帮助"){Text(ACUTE_HELP);Action("退出应用",onExit);Action("继续记录睡眠",model::continueDiary)}
    HomePage.SAFETY_RESULT->SimplePage("安全问询"){
     val safety=state.safety
     Text(if(safety?.assessment?.status==AssessmentStatus.DRAFT)"安全问询尚未完成"else "安全问询已完成")
     Text(when(safety?.assessment?.derivedGate){SafetyGate.CLINICIAN_REVIEW->"你填写的部分情况，在未来如果启用完整 CBT-I 治疗功能前，建议先告诉医生并确认是否需要调整方案。当前睡眠日记可以正常使用。";SafetyGate.DELAY_AUTOMATED_TREATMENT->"你填写的部分情况意味着未来不应直接由无人监管的软件启动睡眠限制等治疗功能，应先由专业人员处理或确认。当前睡眠日记可以正常使用。";else->"当前问询没有发现需要特别标记的项目。当前版本仍然只是睡眠日记，并不代表医学诊断或确认你适合任何治疗。"})
     safety?.answers?.forEach{a->Text((SAFETY_QUESTIONS.firstOrNull{it.id==a.questionId}?.text?:"S07b 控制情况")+"\n"+safetyAnswerLabel(a.answerCode)+(a.detailJson?.let{"\n"+safetyDetailLabel(it)}?:""))}
     Action("继续记录睡眠",model::continueDiary)
    }
    HomePage.TODAY->SimplePage("今天的睡眠记录已经完成"){Text("为减少对单个夜晚数据的反复关注，通常不需要再次查看今天的记录。");Action("退出",onExit);OutlinedButton(onClick=model::editToday,modifier=Modifier.heightIn(min=48.dp)){Text("继续查看")};Action("查看历史记录",{nav.navigate("history")});Action("设置",{nav.navigate("settings")})}
    HomePage.FORM->DiaryScreen(state,model::answerTime,model::answerNumber,model::comment,model::save,model::dismissWarning,{nav.navigate("settings")})
    HomePage.SUCCESS->{BackHandler{model.exitAfterSuccess(onExit)};SimplePage(if(state.firstSuccess)"记录已保存"else"数据已保存成功"){if(state.firstSuccess)Text(FIRST_SAVE_EXPLANATION);state.message?.let{Text(it)};Action("退出",{model.exitAfterSuccess(onExit)},!state.busy&&!busy);Action("分享并退出",::share,!state.busy&&!busy)}}
    HomePage.WEEKLY->{BackHandler{model.exitAfterSuccess(onExit)};SimplePage("过去 7 天"){
     state.weekly?.let{v->Text("第 ${v.periodIndex} 周 · ${v.periodStartDate} — ${v.periodEndDate}");Text("有效记录 ${v.validEntryCount} / 7");val labels=listOf("平均估计入睡耗时（分钟）","平均夜醒次数","平均夜间清醒总时长（分钟）","平均最后醒来到离床时长（分钟）","平均在睡觉位置总时长 TIB（分钟，不等于睡眠时长）","平均主观睡眠质量（1很差 — 5很好）");val means=listOf(v.avgSolMin,v.avgAwakeningCount,v.avgWasoMin,v.avgTerminalWakefulnessMin,v.avgTibMin,v.avgSleepQuality);labels.zip(means).forEach{(label,value)->Text(label+"："+(value?.let{String.format(Locale.ROOT,"%.1f",it)}?:"无记录"))}}
     Text("这些数据来自每天的主观估计，用于观察一段时间的整体模式，不用于评价某一个夜晚。");state.message?.let{Text(it)};Action("退出",{model.exitAfterSuccess(onExit)},!state.busy)
    }} }}
   composable("settings"){SimplePage("设置"){
    Action("返回",{nav.popBackStack()});Text("数据")
    Action("导出完整备份 JSON",{exportKind="json"},!busy);Action("导出睡眠日记 CSV",{exportKind="csv"},!busy);Action("导入完整备份",{importJson.launch(arrayOf("application/json","text/plain"))},!busy);Action("历史记录",{nav.navigate("history")})
    Text("安全与说明");Action("查看当前安全问询",{model.viewSafety();nav.popBackStack("home",false)})
    Action("重新完成安全问询",{model.openSafety(true);nav.popBackStack("home",false)})
    if(state.safety?.assessment?.status==AssessmentStatus.DRAFT)Action("安全问询尚未完成 · 继续",{model.openSafety();nav.popBackStack("home",false)})
    Action("隐私说明",{nav.navigate("privacy")});Text("方法");Action("方法与依据",{nav.navigate("methods")});Action("术语表",{nav.navigate("terms")});Action("版本信息",{nav.navigate("versions")})
   }}
   composable("history") {var selected by rememberSaveable{mutableStateOf(LocalDate.now().toString())};val day=LocalDate.parse(selected);val start=day.minusDays((day.dayOfWeek.value%7).toLong())
    SimplePage("历史记录") {Action("返回",{nav.popBackStack()});Text(start.toString()+" — "+start.plusDays(6));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){TextButton(onClick={selected=day.minusWeeks(1).toString()},modifier=Modifier.heightIn(min=48.dp)){Text("上一周")};TextButton(onClick={selected=day.plusWeeks(1).toString()},modifier=Modifier.heightIn(min=48.dp)){Text("下一周")}}
     Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())){(0..6).forEach{i->val d=start.plusDays(i.toLong());TextButton(onClick={selected=d.toString()},modifier=Modifier.widthIn(min=48.dp).heightIn(min=64.dp),contentPadding=PaddingValues(0.dp)){Text(listOf("日","一","二","三","四","五","六")[i]+"\n"+d.dayOfMonth+(if(d==day)"•" else ""))}}}
     val entry=state.entries.firstOrNull{it.sleepDate==selected};Text(selected);if(entry==null)Text(HISTORY_EMPTY_TEXT)else{val form=DiaryForm.from(entry);DiaryField.entries.forEach{Text(it.label+"："+fieldValue(form,it))};Action("修改这条记录",{model.edit(entry);nav.popBackStack("home",false)})}
    }
   }
   composable("methods"){MethodsScreen({nav.popBackStack()},{nav.navigate("terms")}){url->runCatching{context.startActivity(officialLinkIntent(url))}.onFailure{message="无法打开系统浏览器"}}}
   composable("versions"){VersionsScreen{nav.popBackStack()}}
   composable("terms"){SimplePage("术语表"){Action("返回",{nav.popBackStack()});TERMS.forEach{(title,body)->Text(title);Text(body)}}}
   composable("privacy"){SimplePage("隐私说明"){Action("返回",{nav.popBackStack()});Text("数据仅保存在应用私有存储。无账号、广告、分析或后台联网。只有你主动导出或分享时，记录才离开应用。备份和 CSV 是明文，请保存到可信位置。官方来源链接交给系统浏览器打开。")}}
  }
 }
 if(busy)AlertDialog(onDismissRequest={},text={Text("正在处理，请稍候…")},confirmButton={})
 if(exportKind!=null)AlertDialog(onDismissRequest={exportKind=null},title={Text("导出明文文件")},text={Text("导出的文件包含你的睡眠记录，是明文文件。请只保存到你信任的位置。完整备份也包含安全问询。")},confirmButton={TextButton(onClick={val kind=exportKind;exportKind=null;val stamp=java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));if(kind=="json")exportJson.launch("SleepDiary_backup_$stamp.json")else exportCsv.launch("SleepDiary_entries_$stamp.csv")}){Text("选择保存位置")}},dismissButton={TextButton(onClick={exportKind=null}){Text("取消")}})
 if(pendingRestore!=null)AlertDialog(onDismissRequest={if(!busy)pendingRestore=null},title={Text("恢复完整备份")},text={Column{Text("恢复备份将替换本机现有 SleepDiary 数据。");Text(restoreSummary);Action("先导出当前数据",{exportKind="json"},!busy)}},confirmButton={TextButton(enabled=!busy,onClick={val text=checkNotNull(pendingRestore);operation{withContext(Dispatchers.IO){checkNotNull(services).backup.restore(text)};pendingRestore=null;model.afterRestore();nav.popBackStack("home",false);message="完整备份已恢复"}}){Text("替换并恢复")}},dismissButton={TextButton(enabled=!busy,onClick={pendingRestore=null}){Text("取消")}})
 message?.let{AlertDialog(onDismissRequest={message=null},text={Text(it)},confirmButton={TextButton(onClick={message=null}){Text("知道了")}})}
}
@Composable fun IntroScreen(page:Int,onNext:()->Unit,enabled:Boolean=true) {
 val titles=listOf("睡眠日记","数据留在本机","先回答一次安全问题")
 val bodies=listOf("一个离线的主观睡眠记录工具。当前版本只记录，不诊断，也不提供 CBT-I 治疗。","不需要账号，不联网。只有你主动导出或分享时，数据才会离开应用。","这些问题不是为了给你诊断，而是为了未来如果启用完整 CBT-I 时，知道哪些情况应先让医生确认。")
 SimplePage(titles[page]){Text(bodies[page]);Action(if(page==2)"开始"else"下一页",onNext,enabled)}
}
@Composable fun SimplePage(title:String,content:@Composable ColumnScope.()->Unit){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text(title,style=MaterialTheme.typography.headlineSmall);content()}}
@Composable fun Action(label:String,action:()->Unit,enabled:Boolean=true){Button(onClick=action,enabled=enabled,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)){Text(label)}}
fun safetyAnswerLabel(code:String)=mapOf("YES" to "是","NO" to "否","UNSURE" to "不确定","NOT_APPLICABLE" to "不适用","NONE" to "没有／以上都没有","FLAGS_PRESENT" to "已选择具体提示","STABLE" to "目前控制稳定","UNSTABLE" to "近期仍有发作或控制不稳定","OCCASIONAL" to "偶尔","FREQUENT" to "经常","DANGER_OCCURRED" to "曾因此出现危险","SAFE" to "安全","POSSIBLY_UNSAFE" to "可能不安全")[code]?:code
fun safetyDetailLabel(json:String):String {val o=org.json.JSONObject(json);val names=S10_CHOICES.toMap()+mapOf("LOUD_SNORING" to "经常大声打鼾","WITNESSED_APNEA" to "睡眠中被别人发现暂停呼吸","GASPING_AWAKENING" to "经常憋醒或喘醒","DIAGNOSED_OSA" to "医生已诊断 OSA");val a=o.optJSONArray("selectedCodes")?:o.optJSONArray("diagnoses")?:org.json.JSONArray();return (0 until a.length()).joinToString("、"){names[a.getString(it)]?:a.getString(it)}+o.optString("otherText","")}
