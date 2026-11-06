package app.sleepdiary.offline.ui.publicnav

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import app.sleepdiary.offline.data.entity.SleepDiaryEntry
import app.sleepdiary.offline.data.entity.WeeklySummarySnapshot
import app.sleepdiary.offline.domain.model.AssessmentStatus
import app.sleepdiary.offline.domain.education.ContextualEducationSuggestion
import app.sleepdiary.offline.domain.education.ContextualEducationMatcher
import app.sleepdiary.offline.domain.weekly.RecentAverageCalculator
import app.sleepdiary.offline.ui.Action
import app.sleepdiary.offline.ui.AverageDurationText
import app.sleepdiary.offline.ui.HISTORY_EMPTY_TEXT
import app.sleepdiary.offline.ui.diary.DiaryField
import app.sleepdiary.offline.ui.diary.DiaryForm
import app.sleepdiary.offline.ui.diary.DiaryUiState
import app.sleepdiary.offline.ui.diary.fieldValue
import java.time.LocalDate
import java.util.Locale

enum class PublicDestination(val route: String, val label: String) {
    TODAY("today", "今日"),
    SLEEP("sleep", "历史"),
    EDUCATION("education", "了解"),
    ME("me", "我的")
}

@Composable
fun PublicBottomNavigation(currentRoute: String, onSelect: (PublicDestination) -> Unit) {
    NavigationBar {
        PublicDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { onSelect(destination) },
                icon = { Icon(when (destination) {
                    PublicDestination.TODAY -> Icons.Filled.Home
                    PublicDestination.SLEEP -> Icons.Filled.List
                    PublicDestination.EDUCATION -> Icons.Filled.Info
                    PublicDestination.ME -> Icons.Filled.AccountBox
                }, contentDescription = null) },
                label = { Text(destination.label) },
                modifier = Modifier.testTag("public_tab_${destination.route}")
            )
        }
    }
}

@Composable
fun PublicTodayScreen(
    state: DiaryUiState,
    onCreate: () -> Unit,
    suggestion: ContextualEducationSuggestion? = null,
    onContextWhy: () -> Unit = {},
    onLearnSuggestion: (ContextualEducationSuggestion) -> Unit = {},
    onShare: (SleepDiaryEntry) -> Unit = {},
    shareEnabled: Boolean = true,
    onView: (SleepDiaryEntry) -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("今日", style = MaterialTheme.typography.headlineMedium)
        if (state.today == null) {
            Text("按记忆大概填写即可，不需要为了几分钟的精确度去查手表或反复校准。")
            Action("记录昨晚睡眠", onCreate)
        } else {
            Text("昨晚的睡眠记录已保存")
            Action("查看记录", { state.today?.let(onView) })
            Action("分享记录", { state.today?.let(onShare) }, shareEnabled)
        }
        suggestion?.let { value ->
            Card(Modifier.fillMaxWidth().testTag("today_contextual_education")) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("也许可以想一想", style = MaterialTheme.typography.titleSmall)
                    Text(value.question, style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onContextWhy) { Text("为什么会看到这个？") }
                        TextButton(onClick = { onLearnSuggestion(value) }) { Text("了解一下") }
                    }
                }
            }
        }
    }
}

@Composable
fun PublicSleepScreen(
    entries: List<SleepDiaryEntry>,
    referenceDate: String,
    summaries: List<WeeklySummarySnapshot>,
    onHistory: () -> Unit,
    onShare: (Int) -> Unit = {},
    shareEnabled: Boolean = true
) {
    var days by rememberSaveable { mutableIntStateOf(7) }
    val averages = remember(referenceDate, days, entries) { RecentAverageCalculator.calculate(LocalDate.parse(referenceDate), days, entries) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("历史", style = MaterialTheme.typography.headlineMedium)
        Text("查看原始日记和一段时间内的中性统计。数据来自你的主观估计。")
        if (entries.isEmpty()) Text("还没有睡眠日记记录。完成一条记录后，历史会显示在这里。")
        Action("日记历史", onHistory)
        HorizontalDivider()
        Text("最近平均数据", style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = days == 7, onClick = { days = 7 }, label = { Text("最近 7 天") }, modifier = Modifier.heightIn(min = 48.dp))
            FilterChip(selected = days == 14, onClick = { days = 14 }, label = { Text("最近 14 天") }, modifier = Modifier.heightIn(min = 48.dp))
        }
        Text("有记录的天数：${averages.validEntryCount} / $days")
        Text("${LocalDate.parse(referenceDate).minusDays(days.toLong() - 1)} — $referenceDate")
        Action("分享最近 $days 天", { onShare(days) }, shareEnabled)
        val averageLabels = listOf("平均估计入睡耗时（分钟）", "平均夜醒次数", "平均夜间清醒总时长（分钟）", "平均最后醒来到离床时长（分钟）", "平均在睡觉位置总时长 TIB", "平均主观睡眠质量（1 很差 — 5 很好）")
        val averageValues = listOf(averages.avgSolMin, averages.avgAwakeningCount, averages.avgWasoMin, averages.avgTerminalWakefulnessMin, averages.avgTibMin, averages.avgSleepQuality)
        averageLabels.zip(averageValues).forEachIndexed { index, (label, value) ->
            HorizontalDivider()
            val shown = value?.let { if (index == 4) AverageDurationText.fromMinutes(it) else String.format(Locale.ROOT, "%.1f", it) } ?: "无记录"
            Text("$label：$shown")
        }
        HorizontalDivider()
        Text("周总结", style = MaterialTheme.typography.titleLarge)
        if (summaries.isEmpty()) Text("还没有可查看的周总结；有记录后会在这里显示。")
        summaries.forEach { summary -> WeeklySummaryCard(summary) }
    }
}

@Composable
private fun WeeklySummaryCard(value: WeeklySummarySnapshot) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("第 ${value.periodIndex} 周 · ${value.periodStartDate} — ${value.periodEndDate}", style = MaterialTheme.typography.titleMedium)
        Text("有效记录 ${value.validEntryCount} 天")
        val values = listOf(
            "平均估计入睡耗时" to value.avgSolMin?.let { String.format(Locale.ROOT, "%.1f 分钟", it) },
            "平均夜醒次数" to value.avgAwakeningCount?.let { String.format(Locale.ROOT, "%.1f 次", it) },
            "平均夜间清醒总时长" to value.avgWasoMin?.let { String.format(Locale.ROOT, "%.1f 分钟", it) },
            "平均最后醒来到离床" to value.avgTerminalWakefulnessMin?.let { String.format(Locale.ROOT, "%.1f 分钟", it) },
            "平均在睡觉位置总时长 TIB" to value.avgTibMin?.let(AverageDurationText::fromMinutes),
            "平均主观睡眠质量" to value.avgSleepQuality?.let { String.format(Locale.ROOT, "%.1f / 5", it) }
        )
        values.forEach { (label, display) -> Text("$label：${display ?: "无记录"}") }
        HorizontalDivider()
    }
}

@Composable
fun PublicEducationHome(onMap: () -> Unit, onContinue: () -> Unit, onReview: () -> Unit, onNotes: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("学习", style = MaterialTheme.typography.headlineMedium)
        Text("按自己的节奏了解睡眠知识与 CBT-I 概念。学习内容不是诊断或治疗，不会根据你的日记给建议。")
        Action("知识地图", onMap)
        Action("继续上次学习", onContinue)
        Action("稍后复习", onReview)
        Action("学习记录与笔记", onNotes)
        Text("教育内容不解锁或启动 CBT-I 治疗。", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun PublicMeHome(
    onWorkspace: () -> Unit,
    onBackupExport: () -> Unit,
    onBackupImport: () -> Unit,
    onCsvExport: () -> Unit,
    onEducationJson: () -> Unit,
    onEducationText: () -> Unit,
    onClinicianSummary: () -> Unit,
    onPrivacy: () -> Unit,
    onMethods: () -> Unit,
    onTerms: () -> Unit,
    onVersions: () -> Unit,
    onUsageGuide: () -> Unit = {},
    recordBasedMatchingEnabled: Boolean = true,
    onRecordBasedMatchingChanged: (Boolean) -> Unit = {}
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("我的", style = MaterialTheme.typography.headlineMedium)
        Text("了解内容偏好", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("按本机日记显示相关内容")
                Text("仅在本机匹配一条中性教育内容，不作诊断或治疗建议。", style = MaterialTheme.typography.bodySmall)
            }
            Switch(checked = recordBasedMatchingEnabled, onCheckedChange = onRecordBasedMatchingChanged, modifier = Modifier.testTag("record_based_matching_toggle"))
        }
        HorizontalDivider()
        Text("个人记录", style = MaterialTheme.typography.titleMedium)
        Action("个人记录", onWorkspace, modifier = Modifier.testTag("public_me_workspace"))
        Text("导出与恢复", style = MaterialTheme.typography.titleMedium)
        Action("完整备份", onBackupExport)
        Text("用于换手机或恢复全部数据")
        Action("导入完整备份", onBackupImport)
        Action("睡眠记录表", onCsvExport)
        Text("适合用 Excel / WPS 查看 · CSV")
        Action("可阅读的学习记录", onEducationText)
        Text("普通文本，方便直接查看或保存 · TXT")
        Action("学习数据导出", onEducationJson)
        Text("保存互动记录、选择和个人记录 · JSON")
        Action("就诊摘要", onClinicianSummary)
        Text("隐私与说明", style = MaterialTheme.typography.titleMedium)
        Action("隐私说明", onPrivacy)
        Action("使用说明", onUsageGuide)
        Action("方法与依据", onMethods)
        Action("术语表", onTerms)
        Action("版本信息", onVersions)
    }
}

@Composable
fun ContextualEducationExplanation(suggestion: ContextualEducationSuggestion, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("为什么会看到这个？") },
        text = {
            Text("这是根据本机保存的 ${suggestion.sourceSleepDate} 睡眠日记中“估计睡眠时长”的结果，与预先准备的教育内容对应显示的。匹配只在本机完成，不会上传。这不是诊断，也不是治疗建议。")
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("知道了") } }
    )
}

@Composable
fun DiaryHistoryScreen(
    entries: List<SleepDiaryEntry>,
    initialDate: String,
    onBack: () -> Unit,
    onEdit: (SleepDiaryEntry) -> Unit
) {
    var selected by rememberSaveable { mutableStateOf(initialDate) }
    val day = LocalDate.parse(selected)
    val start = day.minusDays((day.dayOfWeek.value % 7).toLong())
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("日记历史", style = MaterialTheme.typography.headlineSmall)
        TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) { Text("← 返回睡眠") }
        Text(start.toString() + " — " + start.plusDays(6))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = { selected = day.minusWeeks(1).toString() }, modifier = Modifier.heightIn(min = 48.dp)) { Text("上一周") }
            TextButton(onClick = { selected = day.plusWeeks(1).toString() }, modifier = Modifier.heightIn(min = 48.dp)) { Text("下一周") }
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
            (0..6).forEach { index ->
                val date = start.plusDays(index.toLong())
                TextButton(onClick = { selected = date.toString() }, modifier = Modifier.widthIn(min = 48.dp).heightIn(min = 64.dp)) {
                    Text(listOf("日", "一", "二", "三", "四", "五", "六")[index] + "\n" + date.dayOfMonth + if (date == day) "•" else "")
                }
            }
        }
        val entry = entries.firstOrNull { it.sleepDate == selected }
        Text(selected)
        if (entry == null) Text(HISTORY_EMPTY_TEXT) else {
            val form = DiaryForm.from(entry)
            DiaryField.entries.forEach { Text(it.label + "：" + fieldValue(form, it)) }
            entry.comment?.takeIf { it.isNotBlank() }?.let { Text("特殊情况"); Text(it) }
            Action("查看详情", { onEdit(entry) })
        }
    }
}

@Composable
fun DiaryDetailScreen(entry: SleepDiaryEntry, onBack: () -> Unit, onCorrect: () -> Unit) {
    var confirmCorrection by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("睡眠记录", style = MaterialTheme.typography.headlineSmall)
        TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) { Text("← 返回") }
        Text(entry.sleepDate, style = MaterialTheme.typography.titleMedium)
        val form = DiaryForm.from(entry)
        DiaryField.entries.forEach { Text(it.label + "：" + fieldValue(form, it)) }
        ContextualEducationMatcher.estimatedSleepDurationMinutes(entry)?.let { minutes ->
            Text("估计睡眠时长：${AverageDurationText.fromMinutes(minutes.toDouble())}")
        }
        entry.comment?.takeIf(String::isNotBlank)?.let { Text("特殊情况"); Text(it) }
        TextButton(onClick = { confirmCorrection = true }, modifier = Modifier.heightIn(min = 48.dp)) { Text("更正明显错误") }
    }
    if (confirmCorrection) AlertDialog(
        onDismissRequest = { confirmCorrection = false },
        text = { Text("睡眠日记记录的是大致情况，不需要因为几分钟的差异反复修改。只有明显填错或漏记重要情况时才需要更正。") },
        confirmButton = { TextButton(onClick = { confirmCorrection = false; onCorrect() }) { Text("继续更正") } },
        dismissButton = { TextButton(onClick = { confirmCorrection = false }) { Text("取消") } }
    )
}
