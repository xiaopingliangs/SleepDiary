package app.sleepdiary.offline.share

import app.sleepdiary.offline.data.entity.SleepDiaryEntry
import app.sleepdiary.offline.domain.model.Versions
import app.sleepdiary.offline.domain.weekly.RecentAverageCalculator
import app.sleepdiary.offline.ui.AverageDurationText
import java.time.LocalDate
import java.util.Locale

/** A read-only projection of persisted raw rows, never a stored weekly snapshot. */
object RecentDiaryShare {
    fun lines(today: LocalDate, days: Int, entries: List<SleepDiaryEntry>): List<String> {
        val values = RecentAverageCalculator.calculate(today, days, entries)
        val start = today.minusDays(days.toLong() - 1)
        fun number(value: Double?) = value?.let { String.format(Locale.ROOT, "%.1f", it) } ?: "无记录"
        return buildList {
            add("最近 $days 天主观睡眠记录")
            add("$start — $today")
            add("有记录的天数：${values.validEntryCount} / $days")
            add("平均估计入睡耗时（分钟）：${number(values.avgSolMin)}")
            add("平均夜醒次数：${number(values.avgAwakeningCount)}")
            add("平均夜间清醒总时长（分钟）：${number(values.avgWasoMin)}")
            add("平均最后醒来到离床时长（分钟）：${number(values.avgTerminalWakefulnessMin)}")
            add("平均在睡觉位置总时长 TIB：${values.avgTibMin?.let(AverageDurationText::fromMinutes) ?: "无记录"}")
            add("TIB 不等于睡眠时长")
            add("平均主观睡眠质量（1 很差 — 5 很好）：${number(values.avgSleepQuality)}")
            add("按实际有效记录计算；缺失日期不计为 0。")
            val notes = entries.filter {
                LocalDate.parse(it.sleepDate) in start..today && !it.comment.isNullOrBlank()
            }.sortedBy { it.sleepDate }
            if (notes.isNotEmpty()) {
                add("特殊情况（按原始日记日期列出）")
                notes.forEach { add(it.sleepDate); add(checkNotNull(it.comment)) }
            }
            add("主观估计数据 · SleepDiary Public Edition")
            add("${Versions.DIARY_SCHEMA} · ${Versions.WEEKLY_RULE}")
        }
    }
}
