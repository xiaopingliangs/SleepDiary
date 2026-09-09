package app.sleepdiary.offline.data.repository

import androidx.room.withTransaction
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.entity.*
import app.sleepdiary.offline.domain.weekly.*
import java.time.Clock
import java.time.LocalDate
import java.util.UUID

class WeeklySummaryRepository(private val database: SleepDiaryDatabase, private val clock: Clock = Clock.systemDefaultZone()) {
    /** Explicit generation for an ended period; presentation remains a later UI responsibility. */
    suspend fun generate(periodIndex: Int): WeeklySummarySnapshot = database.withTransaction {
        val start = LocalDate.parse(checkNotNull(database.stateAndSummaryDao().state()?.trackingStartDate))
        val period = PeriodCalculator.at(start, periodIndex)
        require(period.end <= LocalDate.now(clock)) { "Period has not ended" }
        current(periodIndex) ?: write(period, null)
    }

    internal suspend fun current(index: Int): WeeklySummarySnapshot? {
        val current = database.stateAndSummaryDao().summaries().filter { it.periodIndex == index && it.isCurrent }
        check(current.size <= 1) { "Multiple current revisions" }
        return current.singleOrNull()
    }

    internal suspend fun revise(period: DiaryPeriod) {
        current(period.index)?.let { write(period, it) }
    }

    /** Called inside the diary transaction, never marks a summary as displayed. */
    internal suspend fun pendingAfterSave(start: LocalDate, savedDate: LocalDate, today: LocalDate): WeeklySummarySnapshot? {
        val state = checkNotNull(database.stateAndSummaryDao().state())
        val savedPeriod = PeriodCalculator.containing(start, savedDate)
        val periods = database.diaryDao().all().map { PeriodCalculator.containing(start, LocalDate.parse(it.sleepDate)) }
            .distinctBy { it.index }.filter { it.index > state.lastDisplayedWeeklyPeriodIndex && it.end <= today }
        val candidate = if (savedDate == today && savedDate == savedPeriod.end && savedPeriod.index > state.lastDisplayedWeeklyPeriodIndex)
            savedPeriod else periods.minByOrNull { it.index }
        return candidate?.let { current(it.index) ?: write(it, null) }
    }

    private suspend fun write(period: DiaryPeriod, previous: WeeklySummarySnapshot?): WeeklySummarySnapshot {
        val values = WeeklySummaryCalculator.calculate(period, database.diaryDao().all())
        val snapshot = WeeklySummarySnapshot(
            id = UUID.randomUUID().toString(), periodIndex = period.index,
            periodStartDate = period.start.toString(), periodEndDate = period.end.toString(),
            validEntryCount = values.validEntryCount, avgSolMin = values.avgSolMin,
            avgAwakeningCount = values.avgAwakeningCount, avgWasoMin = values.avgWasoMin,
            avgTerminalWakefulnessMin = values.avgTerminalWakefulnessMin, avgTibMin = values.avgTibMin,
            avgSleepQuality = values.avgSleepQuality,
            sourceEntryIdsJson = values.sourceEntryIds.joinToString(prefix = "[", postfix = "]", separator = ",") { "\"" + it + "\"" },
            generatedAtUtc = clock.instant().toString(), revision = (previous?.revision ?: 0) + 1,
            isCurrent = true, supersedesSummaryId = previous?.id)
        val dao = database.stateAndSummaryDao()
        if (previous != null) check(dao.retireSummary(previous.id) == 1)
        dao.insertSummary(snapshot)
        check(dao.summaryById(snapshot.id) == snapshot) { "Summary read-back mismatch" }
        return snapshot
    }
}
