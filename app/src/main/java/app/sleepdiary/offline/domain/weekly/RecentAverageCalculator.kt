package app.sleepdiary.offline.domain.weekly

import app.sleepdiary.offline.data.entity.SleepDiaryEntry
import app.sleepdiary.offline.domain.model.LocalTimeMath
import app.sleepdiary.offline.domain.validation.DiaryValidator
import java.time.LocalDate
import java.time.LocalDateTime

object RecentAverageCalculator {
    fun calculate(today: LocalDate, days: Int, entries: List<SleepDiaryEntry>): WeeklySummaryValues {
        require(days == 7 || days == 14)
        val start = today.minusDays(days.toLong() - 1)
        val selected = entries
            .filter { LocalDate.parse(it.sleepDate) in start..today }
            .sortedWith(compareBy({ it.sleepDate }, { it.id }))
        require(selected.size <= days && selected.map { it.sleepDate }.distinct().size == selected.size)
        require(selected.map { it.id }.distinct().size == selected.size)
        require(selected.all { DiaryValidator.validate(it).canSave }) { "Invalid source entry" }

        fun mean(value: (SleepDiaryEntry) -> Double): Double? =
            if (selected.isEmpty()) null else selected.map(value).average()
        fun minutes(startAt: String, endAt: String) =
            LocalTimeMath.minutesBetween(LocalDateTime.parse(startAt), LocalDateTime.parse(endAt))

        return WeeklySummaryValues(
            validEntryCount = selected.size,
            avgSolMin = mean { it.sleepOnsetLatencyMin.toDouble() },
            avgAwakeningCount = mean { it.awakeningCount.toDouble() },
            avgWasoMin = mean { it.wakeAfterSleepOnsetMin.toDouble() },
            avgTerminalWakefulnessMin = mean { minutes(it.finalAwakeningAtLocal, it.outOfBedAtLocal) },
            avgTibMin = mean { minutes(it.bedEnteredAtLocal, it.outOfBedAtLocal) },
            avgSleepQuality = mean { it.sleepQuality.toDouble() },
            sourceEntryIds = selected.map { it.id }
        )
    }
}
