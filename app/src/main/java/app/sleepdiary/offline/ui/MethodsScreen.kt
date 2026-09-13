package app.sleepdiary.offline.ui
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import app.sleepdiary.offline.domain.model.Versions

data class EvidenceSource(val title:String,val attribution:String,val usage:String,val url:String)
val EVIDENCE_SOURCES=listOf(
 EvidenceSource("The consensus sleep diary: standardizing prospective sleep self-monitoring","Carney CE et al. · Sleep · 2012 · DOI 10.5665/sleep.1642","共识睡眠日记：每日主观记录的核心构念；本项目独立中文表述，不是官方授权中文版。","https://pubmed.ncbi.nlm.nih.gov/22294820/"),
 EvidenceSource("Sleep Diary Instructions - Core / Consensus Sleep Diary-Core","Colleen Carney 作者实验室公开表格 · 年份未在当前依据文件中注明","共识睡眠日记核心说明：主观估计、无需盯钟、漏记留空及可选备注。","https://drcolleencarney.com/wp-content/uploads/2016/01/Final-CSD-Core-and-instructions.pdf"),
 EvidenceSource("Psychometric Properties of the Consensus Sleep Diary in Those With Insomnia Disorder","Maich KHG, Lachowski AM, Carney CE · 2018（online 2016）· DOI 10.1080/15402002.2016.1173556","共识睡眠日记心理测量学研究；不据此自动启用治疗算法。","https://pubmed.ncbi.nlm.nih.gov/27231885/"),
 EvidenceSource("Measuring Sleep Efficiency: What Should the Denominator Be?","Reed DL, Sacco WP · 2016","睡眠效率定义存在方法差异；原始数据保留、规则独立版本化。","https://pmc.ncbi.nlm.nih.gov/articles/PMC4751425/"),
 EvidenceSource("VA/DoD Clinical Practice Guideline for the Management of Chronic Insomnia Disorder and Obstructive Sleep Apnea","VA/DoD · 2025","慢性失眠与阻塞性睡眠呼吸暂停指南：首次安全问询主要来源；仅为未来治疗前确认，不作诊断。","https://www.healthquality.va.gov/guidelines/CD/insomnia/"),
 EvidenceSource("失眠症数字疗法的中国专家共识（2024版）\nChinese expert consensus on digital therapeutics for insomnia (2024 edition)（项目译名）","中华医学杂志 · 104卷9期 · 2024","中国本地数字评估场景交叉核查；V0.1 不执行其中治疗规则。","https://www.sinomed.ac.cn/article.do?ui=2024308355"),
 EvidenceSource("国家卫生健康委关于应用“12356”全国统一心理援助热线电话号码的通知\nNotice on the nationwide 12356 psychological assistance hotline（项目译名）","中华人民共和国国家卫生健康委员会 · 2024","中国大陆现实世界安全求助提示。","https://www.nhc.gov.cn/yzygj/c100068/202412/49a1a65386cd4be582d4702fd0926ee8.shtml")
)
fun officialLinkIntent(url:String):Intent {require(EVIDENCE_SOURCES.any{it.url==url});return Intent(Intent.ACTION_VIEW,Uri.parse(url))}
val TERMS=listOf(
 "CBT-I · Cognitive Behavioral Therapy for Insomnia" to "失眠认知行为治疗。本版本未启用治疗协议。",
 "CSD · Consensus Sleep Diary" to "共识睡眠日记。每日主观记录的测量来源。",
 "SOL · Sleep Onset Latency" to "入睡潜伏期／估计入睡耗时。从开始尝试入睡到觉得睡着的估计时间。",
 "WASO · Wake After Sleep Onset" to "入睡后夜间清醒时间。真正睡着后到最后醒来前，各次清醒时间的估计合计。",
 "TIB · Time in Bed" to "在睡觉位置的总时长。从进入到真正离开，不等于睡眠时长。",
 "Terminal Wakefulness" to "最后一次醒来到真正离开睡觉位置的时长。",
 "OSA · Obstructive Sleep Apnea" to "阻塞性睡眠呼吸暂停。安全问询涉及的疾病名称；本工具不诊断。",
 "RLS · Restless Legs Syndrome" to "不宁腿综合征。安全问询只记录医生曾明确诊断的名称。",
 "RBD · REM Sleep Behavior Disorder" to "REM 睡眠行为障碍。安全问询只记录医生曾明确诊断的名称。",
 "Narcolepsy" to "发作性睡病。问询中的既往诊断名称，不提供自测诊断。",
 "Sleepwalking / Sleep terrors" to "梦游／睡行症、夜惊。仅用于识别问询中的诊断名称。",
 "Circadian rhythm sleep-wake disorder" to "昼夜节律睡眠-觉醒障碍。仅用于识别已诊断的名称。",
 "Bipolar disorder / Manic episode / Hypomanic episode" to "双相障碍／躁狂发作／轻躁狂发作。只询问医生明确诊断史，不根据此表自行判断。",
 "Epilepsy" to "癫痫。问询中的既往疾病名称，不作诊断。",
 "PTSD · Post-traumatic stress disorder" to "创伤后应激障碍。问询仅了解是否正在接受相关暴露治疗。"
)
@Composable fun MethodsScreen(onBack:()->Unit,onTerms:()->Unit,onLink:(String)->Unit) {
 SimplePage("方法与依据") {
  Action("返回",onBack)
  Action("术语表",onTerms)
  Text("本版本如何计算")
  Text("TIB = 真正离开睡觉位置时间 − 进入睡觉位置时间。\nTerminal Wakefulness = 真正离开睡觉位置时间 − 最后一次醒来时间。\n平均值只使用有记录的日期，缺失日期不按 0 计算。\nV0.1 不计算治疗用 TST / SE。当前 V0.1 不用于治疗计算或展示。")
  Text("产品设计选择")
  Text("‘记录后退出’‘只每7天集中反馈’‘单夜不评分’属于本项目 UX 设计，不是声称某份指南逐字要求。")
  EVIDENCE_SOURCES.forEach{source->Text(source.title);Text(source.attribution);Text(source.usage);Action("查看官方来源",{onLink(source.url)})}
 }
}

@Composable fun VersionsScreen(onBack:()->Unit) {
 SimplePage("版本信息") {
  Text("Workspace schema: ${Versions.WORKSPACE_SCHEMA}")
  Action("返回",onBack)
  Text("App: ${Versions.APP}\nSPEC: ${Versions.SPEC}\nDiary schema: ${Versions.DIARY_SCHEMA}\nSafety schema: ${Versions.SAFETY_SCHEMA}\nSafety rule: ${Versions.SAFETY_RULE}\nWeekly rule: ${Versions.WEEKLY_RULE}\nBackup format: ${Versions.BACKUP_FORMAT}\nCBT-I Protocol: ${Versions.CBTI_PROTOCOL}\n医学内容最后复核日期：2026-09-08")
 }
}
