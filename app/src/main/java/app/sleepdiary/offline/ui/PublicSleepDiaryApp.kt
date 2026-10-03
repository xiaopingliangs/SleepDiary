package app.sleepdiary.offline.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import app.sleepdiary.offline.application.AppServices
import app.sleepdiary.offline.data.entity.WeeklySummarySnapshot
import app.sleepdiary.offline.domain.model.*
import app.sleepdiary.offline.share.DiaryShare
import app.sleepdiary.offline.ui.diary.*
import app.sleepdiary.offline.ui.publicnav.*
import app.sleepdiary.offline.ui.education.PublicEducationExperience
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun SleepDiaryApp(model: DiaryViewModel, onExit: () -> Unit, services: AppServices? = null) {
    val state by model.state.collectAsState()
    val nav = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var exportKind by rememberSaveable { mutableStateOf<String?>(null) }
    var educationExportKind by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingRestore by remember { mutableStateOf<String?>(null) }
    var restoreSummary by remember { mutableStateOf("") }
    var editReturnRoute by rememberSaveable { mutableStateOf("today") }
    var weeklySummaries by remember { mutableStateOf(emptyList<WeeklySummarySnapshot>()) }

    fun operation(block: suspend () -> Unit) {
        if (busy) return
        busy = true
        scope.launch {
            try { block() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (error: app.sleepdiary.offline.domain.education.EducationCompatibilityException) { message = error.message ?: "教育内容版本不兼容。" }
            catch (_: Exception) { message = "操作未完成，数据未确认成功。请重试。" }
            finally { busy = false }
        }
    }

    val exportBackup = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) operation {
            val value = withContext(Dispatchers.IO) { checkNotNull(services).backup.export() }
            withContext(Dispatchers.IO) { checkNotNull(context.contentResolver.openOutputStream(uri, "wt")).use { it.write(value.toByteArray(Charsets.UTF_8)) } }
            message = "完整备份已导出"
        }
    }
    val exportCsv = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) operation {
            val value = withContext(Dispatchers.IO) { checkNotNull(services).backup.csv() }
            withContext(Dispatchers.IO) { checkNotNull(context.contentResolver.openOutputStream(uri, "wt")).use { it.write(value) } }
            message = "CSV 已导出"
        }
    }
    val exportEducationJson = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) operation {
            val value = withContext(Dispatchers.IO) { checkNotNull(services).education.exportJson() }
            withContext(Dispatchers.IO) { checkNotNull(context.contentResolver.openOutputStream(uri, "wt")).use { it.write(value.toByteArray(Charsets.UTF_8)) } }
            message = "学习数据 JSON 已导出"
        }
    }
    val exportEducationText = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        if (uri != null) operation {
            val value = withContext(Dispatchers.IO) { checkNotNull(services).education.exportText() }
            withContext(Dispatchers.IO) { checkNotNull(context.contentResolver.openOutputStream(uri, "wt")).use { it.write(value.toByteArray(Charsets.UTF_8)) } }
            message = "学习数据 TXT 已导出"
        }
    }
    val importBackup = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) operation {
            val text = withContext(Dispatchers.IO) { checkNotNull(context.contentResolver.openInputStream(uri)).bufferedReader(Charsets.UTF_8).use { it.readText() } }
            val preview = withContext(Dispatchers.IO) { checkNotNull(services).backup.preview(text) }
            val oldFormatNotice = if (preview.sourceBackupFormatVersion in setOf(Versions.LEGACY_BACKUP_FORMAT, Versions.LEGACY_BACKUP_1_1, Versions.LEGACY_BACKUP_2_0, Versions.LEGACY_BACKUP_2_1, Versions.LEGACY_BACKUP_3_0))
                "\n旧版备份不包含 Education 学习数据，恢复后相关集合为空。" else ""
            restoreSummary = "教育内容版本 ${preview.educationContentVersions.size} 个；进度 ${preview.educationProgress.size} 项；已确认选择 ${preview.educationCommittedChoices.size} 项；复习标记 ${preview.educationReviewStates.size} 项；教育记录 ${preview.educationRecords.size} 条；恢复位置 ${preview.educationPositions.size} 项$oldFormatNotice\n个人记录 ${preview.workspaceRecords.size} 条\n睡眠记录 ${preview.entries.size} 条\n安全问询 ${preview.assessments.size} 份\n周总结（含历史修订）${preview.summaries.size} 份\n历史输入快照 ${preview.inputSnapshots.size} 份\n休眠 Treatment 历史：${preview.treatmentEpisodes.size} 个 episode，${preview.treatmentAuditEvents.size} 条审计记录；不会启用 Treatment。\n首次说明已完成：${if (preview.state.hasCompletedOnboarding) "是" else "否"}"
            pendingRestore = text
        }
    }

    LaunchedEffect(services) { weeklySummaries = runCatching { services?.weeklySummaries?.currentSummaries().orEmpty() }.getOrDefault(emptyList()) }
    LaunchedEffect(state.safetyFromSettings) { if (state.safetyFromSettings) nav.navigate("me") { launchSingleTop = true } }
    fun navigateTop(destination: PublicDestination) {
        nav.navigate(destination.route) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    fun shareAndExit() {
        val entry = state.today ?: return
        operation {
            val intent = withContext(Dispatchers.IO) { checkNotNull(services).share.prepare(entry) }
            model.exitAfterSuccess { DiaryShare.handOff(intent, { context.startActivity(it) }, onExit) }
        }
    }

    val currentRoute = nav.currentBackStackEntryAsState().value?.destination?.route.orEmpty()
    val topRoutes = PublicDestination.entries.map { it.route }.toSet()
    val overlay = state.page in setOf(HomePage.LOADING, HomePage.INTRO, HomePage.SAFETY, HomePage.HELP, HomePage.SAFETY_RESULT, HomePage.FORM, HomePage.SUCCESS, HomePage.WEEKLY)

    Surface(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            NavHost(navController = nav, startDestination = PublicDestination.TODAY.route, modifier = Modifier.weight(1f)) {
                composable("today") {
                    PublicTodayScreen(state, model::beginNewDiary, model::editToday, { navigateTop(PublicDestination.EDUCATION) }, { navigateTop(PublicDestination.SLEEP) })
                }
                composable("sleep") {
                    PublicSleepScreen(state.entries, state.form.referenceDate.toString(), weeklySummaries, { nav.navigate("history") }, { exportKind = "csv" })
                }
                composable("education") {
                    PublicEducationExperience(services?.education, services?.workspace) { navigateTop(PublicDestination.TODAY) }
                }
                composable("me") {
                    PublicMeHome(
                        onWorkspace = { nav.navigate("workspace") },
                        onBackupExport = { exportKind = "json" },
                        onBackupImport = { importBackup.launch(arrayOf("application/json", "text/plain")) },
                        onCsvExport = { exportKind = "csv" },
                        onEducationJson = { educationExportKind = "json" },
                        onEducationText = { educationExportKind = "txt" },
                        onClinicianSummary = { nav.navigate("clinicianSummary") },
                        onPrivacy = { nav.navigate("privacy") },
                        onMethods = { nav.navigate("methods") },
                        onTerms = { nav.navigate("terms") },
                        onVersions = { nav.navigate("versions") },
                        onSafety = model::viewSafety,
                        onRepeatSafety = { model.openSafety(newAssessment = true, fromSettings = true) },
                        onResumeSafety = { model.openSafety(fromSettings = true) },
                        hasDraftSafety = state.safety?.assessment?.status == AssessmentStatus.DRAFT
                    )
                }
                composable("history") {
                    DiaryHistoryScreen(state.entries, state.form.referenceDate.toString(), { nav.popBackStack() }) { entry ->
                        editReturnRoute = "history"
                        model.edit(entry)
                    }
                }
                composable("workspace") { app.sleepdiary.offline.ui.workspace.WorkspaceScreen(services?.workspace, "返回我的") { nav.popBackStack() } }
                composable("clinicianSummary") { app.sleepdiary.offline.ui.clinician.ClinicianSummaryScreen(services?.clinicianSummary) { nav.popBackStack() } }
                composable("methods") {
                    MethodsScreen({ nav.popBackStack() }, { nav.navigate("terms") }) { url ->
                        runCatching { context.startActivity(officialLinkIntent(url)) }.onFailure { message = "无法打开系统浏览器" }
                    }
                }
                composable("versions") { VersionsScreen { nav.popBackStack() } }
                composable("terms") { SimplePage("术语表") { Action("← 返回", { nav.popBackStack() }); TERMS.forEach { (title, body) -> Text(title); Text(body) } } }
                composable("privacy") { SimplePage("隐私说明") { Action("← 返回", { nav.popBackStack() }); Text("数据仅保存在应用私有存储。无账号、广告、分析或后台联网。只有你主动导出或分享时，记录才离开应用。备份、CSV 和学习导出是明文，请保存到可信位置。") } }
            }
            if (!overlay && currentRoute in topRoutes) PublicBottomNavigation(currentRoute, ::navigateTop)
        }

        if (state.page == HomePage.LOADING) SimplePage("SleepDiary") { Text(state.message ?: "正在读取本机记录…"); Action("重试", model::start) }
        if (overlay && state.page != HomePage.LOADING) {
            when (state.page) {
                HomePage.INTRO -> { IntroScreen(state.introPage, model::nextIntro, !state.busy); state.message?.let { Text(it) } }
                HomePage.SAFETY -> {
                    BackHandler(enabled = state.safetyFromSettings) { if (!state.busy) model.returnFromSafety {} }
                    Column(Modifier.fillMaxSize()) {
                        if (state.safetyFromSettings) TextButton(onClick = { model.returnFromSafety {} }, enabled = !state.busy, modifier = Modifier.heightIn(min = 48.dp)) { Text("← 返回我的") }
                        Box(Modifier.weight(1f)) { SafetyScreen(model, state) }
                    }
                }
                HomePage.HELP -> SimplePage("请优先联系现实世界的帮助") {
                    Text(ACUTE_HELP); Action("退出应用", onExit)
                    Action("返回我的", { if (state.safetyFromSettings) model.returnFromSafety {} else model.continueDiary() })
                }
                HomePage.SAFETY_RESULT -> SimplePage("历史安全问询") {
                    Action("← 返回我的", { if (state.safetyFromSettings) model.returnFromSafety {} else model.continueDiary() })
                    val safety = state.safety
                    Text(if (safety?.assessment?.status == AssessmentStatus.DRAFT) "安全问询尚未完成" else "安全问询已完成")
                    Text(when (safety?.assessment?.derivedGate) {
                        SafetyGate.CLINICIAN_REVIEW -> "问询中记录的部分情况，在未来如考虑治疗时可以与医生讨论。此处不是诊断；当前版本不启用 CBT-I 治疗。"
                        SafetyGate.DELAY_AUTOMATED_TREATMENT -> "历史数据含有未来治疗需要先由专业人员处理或确认的标记。此处不是诊断；当前版本不启用 CBT-I 治疗。"
                        else -> "此处只回看历史问询，不代表医学诊断或确认适合任何治疗。"
                    })
                    safety?.answers?.forEach { answer -> Text((SAFETY_QUESTIONS.firstOrNull { it.id == answer.questionId }?.text ?: "S07b 控制情况") + "\n" + safetyAnswerLabel(answer.answerCode) + (answer.detailJson?.let { "\n" + safetyDetailLabel(it) } ?: "")) }
                }
                HomePage.FORM -> {
                    BackHandler(enabled = !state.busy) { model.cancelForm(); nav.navigate(editReturnRoute) { launchSingleTop = true } }
                    Column(Modifier.fillMaxSize()) {
                        TextButton(onClick = { model.cancelForm(); nav.navigate(editReturnRoute) { launchSingleTop = true } }, enabled = !state.busy, modifier = Modifier.heightIn(min = 48.dp)) { Text("← 返回") }
                        Box(Modifier.weight(1f)) { DiaryScreen(state, model::answerTime, model::answerNumber, model::comment, model::save, model::dismissWarning) { navigateTop(PublicDestination.ME) } }
                    }
                }
                HomePage.SUCCESS -> {
                    BackHandler { model.returnToToday { nav.navigate(editReturnRoute) { launchSingleTop = true } } }
                    SimplePage(if (state.firstSuccess) "记录已保存" else "睡眠记录已保存") {
                        if (state.firstSuccess) Text(FIRST_SAVE_EXPLANATION)
                        state.message?.let { Text(it) }
                        Action("返回", { model.returnToToday { nav.navigate(editReturnRoute) { launchSingleTop = true } } }, !state.busy && !busy)
                        Action("分享并退出", ::shareAndExit, !state.busy && !busy)
                    }
                }
                HomePage.WEEKLY -> {
                    BackHandler { model.returnToToday { navigateTop(PublicDestination.SLEEP) } }
                    SimplePage("过去 7 天") {
                        state.weekly?.let { summary ->
                            Text("第 ${summary.periodIndex} 周 · ${summary.periodStartDate} — ${summary.periodEndDate}")
                            Text("有效记录 ${summary.validEntryCount} / 7")
                            val labels = listOf("平均估计入睡耗时（分钟）", "平均夜醒次数", "平均夜间清醒总时长（分钟）", "平均最后醒来到离床时长（分钟）", "平均在睡觉位置总时长 TIB（不等于睡眠时长）", "平均主观睡眠质量（1很差 — 5很好）")
                            val means = listOf(summary.avgSolMin, summary.avgAwakeningCount, summary.avgWasoMin, summary.avgTerminalWakefulnessMin, summary.avgTibMin, summary.avgSleepQuality)
                            labels.zip(means).forEachIndexed { index, (label, value) ->
                                val shown = value?.let { if (index == 4) AverageDurationText.fromMinutes(it) else String.format(Locale.ROOT, "%.1f", it) } ?: "无记录"
                                Text("$label：$shown")
                            }
                        }
                        Text("这些数据来自每天的主观估计，用于观察一段时间的整体模式，不用于评价某一个夜晚。")
                        Action("返回睡眠", { model.returnToToday { navigateTop(PublicDestination.SLEEP) } }, !state.busy)
                    }
                }
                else -> Unit
            }
        }
    }

    if (busy) AlertDialog(onDismissRequest = {}, text = { Text("正在处理，请稍候…") }, confirmButton = {})
    if (exportKind != null) AlertDialog(onDismissRequest = { exportKind = null }, title = { Text("导出明文文件") },
        text = { Text("睡眠日记和完整备份均为明文文件。完整备份还包含安全问询及个人记录。请只保存到你信任的位置。") },
        confirmButton = { TextButton(onClick = { val kind = exportKind; exportKind = null; val stamp = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")); if (kind == "json") exportBackup.launch("SleepDiary_backup_$stamp.json") else exportCsv.launch("SleepDiary_entries_$stamp.csv") }) { Text("选择保存位置") } },
        dismissButton = { TextButton(onClick = { exportKind = null }) { Text("取消") } })
    if (educationExportKind != null) AlertDialog(onDismissRequest = { educationExportKind = null }, title = { Text("导出学习数据") },
        text = { Text("只导出你保存的学习数据和关联笔记，不包含课程正文。文件是明文，请保存到可信位置。") },
        confirmButton = { TextButton(onClick = { val kind = educationExportKind; educationExportKind = null; val stamp = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")); if (kind == "json") exportEducationJson.launch("SleepDiary_education_$stamp.json") else exportEducationText.launch("SleepDiary_education_$stamp.txt") }) { Text("选择保存位置") } },
        dismissButton = { TextButton(onClick = { educationExportKind = null }) { Text("取消") } })
    if (pendingRestore != null) AlertDialog(onDismissRequest = { if (!busy) pendingRestore = null }, title = { Text("恢复完整备份") },
        text = { Column { Text("恢复备份将替换本机现有 SleepDiary 数据，包含个人记录、学习状态和历史输入快照。请核对数量；休眠 Treatment 历史不会启用治疗。"); Text(restoreSummary); Action("先导出当前数据", { exportKind = "json" }, !busy) } },
        confirmButton = { TextButton(enabled = !busy, onClick = { val text = checkNotNull(pendingRestore); operation { withContext(Dispatchers.IO) { checkNotNull(services).backup.restore(text) }; pendingRestore = null; model.afterRestore(); nav.navigate("today") { popUpTo(nav.graph.findStartDestination().id) { inclusive = false }; launchSingleTop = true }; message = "完整备份已恢复" } }) { Text("替换并恢复") } },
        dismissButton = { TextButton(enabled = !busy, onClick = { pendingRestore = null }) { Text("取消") } })
    message?.let { AlertDialog(onDismissRequest = { message = null }, text = { Text(it) }, confirmButton = { TextButton(onClick = { message = null }) { Text("知道了") } }) }
}
