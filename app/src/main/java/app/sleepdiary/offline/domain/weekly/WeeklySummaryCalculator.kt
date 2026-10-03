package app.sleepdiary.offline.domain.weekly

import app.sleepdiary.offline.data.entity.SleepDiaryEntry
import app.sleepdiary.offline.domain.validation.DiaryValidator
import app.sleepdiary.offline.domain.model.LocalTimeMath
import java.time.LocalDate
import java.time.LocalDateTime

data class WeeklySummaryValues(
    val validEntryCount: Int,
    val avgSolMin: Double?, val avgAwakeningCount: Double?, val avgWasoMin: Double?,
    val avgTerminalWakefulnessMin: Double?, val avgTibMin: Double?, val avgSleepQuality: Double?,
    val sourceEntryIds: List<String>
)

object WeeklySummaryCalculator {
    fun calculate(period: DiaryPeriod, entries: List<SleepDiaryEntry>): WeeklySummaryValues {
        val selected = entries.filter { LocalDate.parse(it.sleepDate) in period.start..period.end }
            .filter { DiaryValidator.validate(it).canSave }
            .sortedWith(compareBy({ it.sleepDate }, { it.id }))
        require(selected.size <= 7 && selected.map { it.sleepDate }.distinct().size == selected.size)
        require(selected.map { it.id }.distinct().size == selected.size)
        fun mean(value: (SleepDiaryEntry) -> Double): Double? =
            if (selected.isEmpty()) null else selected.map(value).average()
        fun minutes(start: String, end: String) =
            LocalTimeMath.minutesBetween(LocalDateTime.parse(start), LocalDateTime.parse(end))
        return WeeklySummaryValues(selected.size,
            mean { it.sleepOnsetLatencyMin.toDouble() }, mean { it.awakeningCount.toDouble() },
            mean { it.wakeAfterSleepOnsetMin.toDouble() },
            mean { minutes(it.finalAwakeningAtLocal, it.outOfBedAtLocal) },
            mean { minutes(it.bedEnteredAtLocal, it.outOfBedAtLocal) }, mean { it.sleepQuality.toDouble() },
            selected.map { it.id })
    }
}
