package app.sleepdiary.offline.ui.publicnav

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import app.sleepdiary.offline.data.entity.SleepDiaryEntry
import app.sleepdiary.offline.data.entity.WeeklySummarySnapshot
import app.sleepdiary.offline.domain.model.AssessmentStatus
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

enum class PublicDestination(val route: String, val label: String, val symbol: String) {
    TODAY("today", "今天", "今"),
    SLEEP("sleep", "睡眠", "眠"),
    EDUCATION("education", "学习", "学"),
    ME("me", "我的", "我")
}

@Composable
fun PublicBottomNavigation(currentRoute: String, onSelect: (PublicDestination) -> Unit) {
    NavigationBar {
        PublicDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { onSelect(destination) },
                icon = { Text(destination.symbol, style = MaterialTheme.typography.titleMedium) },
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
    onEdit: () -> Unit,
    onEducation: () -> Unit,
    onSleep: () -> Unit
) {
    val reference = state.form.referenceDate
    var days by rememberSaveable { mutableIntStateOf(7) }
    val averages = remember(reference, days, state.entries) {
        RecentAverageCalculator.calculate(reference, days, state.entries)
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("今天", style = MaterialTheme.typography.headlineMedium)
        Text(if (state.today == null) "今天还没有睡眠记录。记录主观估计即可，不必精确。" else "今天的睡眠记录已保存。你可以查看或修改。")
        Action(if (state.today == null) "记录今天的睡眠" else "查看或修改今天记录", if (state.today == null) onCreate else onEdit)
        HorizontalDivider()
        Text("最近平均数据", style = MaterialTheme.typography.titleMedium)
        Text("只汇总已有记录，不评价好坏，也不生成建议。")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = days == 7, onClick = { days = 7 }, label = { Text("最近 7 天") }, modifier = Modifier.heightIn(min = 48.dp))
            FilterChip(selected = days == 14, onClick = { days = 14 }, label = { Text("最近 14 天") }, modifier = Modifier.heightIn(min = 48.dp))
        }
        Text("有记录的天数：${averages.validEntryCount} / $days")
        val labels = listOf("平均估计入睡耗时（分钟）", "平均夜醒次数", "平均夜间清醒总时长（分钟）", "平均最后醒来到离床时长（分钟）", "平均在睡觉位置总时长 TIB", "平均主观睡眠质量（1 很差 — 5 很好）")
        val values = listOf(averages.avgSolMin, averages.avgAwakeningCount, averages.avgWasoMin, averages.avgTerminalWakefulnessMin, averages.avgTibMin, averages.avgSleepQuality)
        labels.zip(values).forEachIndexed { index, (label, value) ->
            HorizontalDivider()
            val display = value?.let { if (index == 4) AverageDurationText.fromMinutes(it) else String.format(Locale.ROOT, "%.1f", it) } ?: "无记录"
            Text("$label：$display")
        }
        HorizontalDivider()
        OutlinedButton(onClick = onEducation, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("阅读睡眠与 CBT-I 知识") }
        OutlinedButton(onClick = onSleep, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("查看睡眠历史与总结") }
    }
}

@Composable
fun PublicSleepScreen(
    summaries: List<WeeklySummarySnapshot>,
    onHistory: () -> Unit,
    onCsvExport: () -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("睡眠", style = MaterialTheme.typography.headlineMedium)
        Text("查看原始日记和一段时间内的中性统计。数据来自你的主观估计。")
        Action("日记历史", onHistory)
        Action("导出睡眠日记 CSV", onCsvExport)
        HorizontalDivider()
        Text("周总结", style = MaterialTheme.typography.titleLarge)
        if (summaries.isEmpty()) Text("还没有已生成的周总结。")
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
    onSafety: () -> Unit,
    onRepeatSafety: () -> Unit,
    onResumeSafety: () -> Unit,
    hasDraftSafety: Boolean
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("我的", style = MaterialTheme.typography.headlineMedium)
        Text("个人记录", style = MaterialTheme.typography.titleMedium)
        Action("个人记录", onWorkspace)
        Text("导出与恢复", style = MaterialTheme.typography.titleMedium)
        Action("导出完整备份 JSON", onBackupExport)
        Action("导入完整备份", onBackupImport)
        Action("导出睡眠日记 CSV", onCsvExport)
        Action("导出学习数据 JSON", onEducationJson)
        Action("导出学习数据 TXT", onEducationText)
        Action("就诊摘要", onClinicianSummary)
        Text("隐私与说明", style = MaterialTheme.typography.titleMedium)
        Action("隐私说明", onPrivacy)
        Action("方法与依据", onMethods)
        Action("术语表", onTerms)
        Action("版本信息", onVersions)
        Text("历史安全问询", style = MaterialTheme.typography.titleMedium)
        Action("查看当前安全问询", onSafety)
        Action("重新填写安全问询", onRepeatSafety)
        if (hasDraftSafety) Action("继续未完成的安全问询", onResumeSafety)
    }
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
            entry.comment?.takeIf { it.isNotBlank() }?.let { Text("特殊情况\n$it") }
            Action("修改这条记录", { onEdit(entry) })
        }
    }
}
