package app.sleepdiary.offline.data.repository

import androidx.room.withTransaction
import app.sleepdiary.offline.data.dao.DiaryDao
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.entity.*
import app.sleepdiary.offline.domain.validation.DiaryValidator
import app.sleepdiary.offline.domain.weekly.PeriodCalculator
import kotlinx.coroutines.CancellationException
import java.time.Clock
import java.time.LocalDate

sealed interface SaveResult {
    data class Success(val entry: SleepDiaryEntry, val pendingSummary: WeeklySummarySnapshot?) : SaveResult
    data class ValidationWarning(val input: SleepDiaryEntry) : SaveResult
    data class Failure(val reason: SaveFailure, val input: SleepDiaryEntry, val fields: List<String> = emptyList()) : SaveResult
}
enum class SaveFailure { INVALID_INPUT, BEFORE_TRACKING_START, WRITE_FAILED, READ_BACK_FAILED }

/** DAO is injectable for storage failure tests; production defaults to this database's DAO. */
class DiaryRepository(
    private val database: SleepDiaryDatabase,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val diaryDao: DiaryDao = database.diaryDao()
) {
    private class ReadBackFailure : RuntimeException()
    private class BeforeStart : RuntimeException()

    suspend fun save(input: SleepDiaryEntry, confirmEstimateWarning: Boolean = false): SaveResult {
        val validation = DiaryValidator.validate(input)
        if (!validation.canSave) return SaveResult.Failure(SaveFailure.INVALID_INPUT, input, validation.errors)
        if (validation.consistencyWarning && !confirmEstimateWarning) return SaveResult.ValidationWarning(input)
        return try {
            val committed = database.withTransaction {
                val original = diaryDao.byId(input.id)
                val stateDao = database.stateAndSummaryDao()
                val state = stateDao.state() ?: AppState()
                val frozen = stateDao.summaries().isNotEmpty()
                val date = LocalDate.parse(input.sleepDate)
                if (frozen && date < LocalDate.parse(checkNotNull(state.trackingStartDate))) throw BeforeStart()
                val now = clock.instant().toString()
                val expected = input.copy(createdAtUtc = original?.createdAtUtc ?: now, updatedAtUtc = now,
                    consistencyWarning = validation.consistencyWarning)
                if (original == null) diaryDao.insert(expected) else check(diaryDao.update(expected) == 1)
                verify(expected)
                val start = if (frozen) checkNotNull(state.trackingStartDate)
                    else checkNotNull(diaryDao.all().minOfOrNull { it.sleepDate })
                val updatedState = state.copy(trackingStartDate = start)
                stateDao.putState(updatedState)
                check(stateDao.state() == updatedState) { "AppState read-back mismatch" }
                val summaries = WeeklySummaryRepository(database, clock)
                val tracking = LocalDate.parse(start)
                val target = PeriodCalculator.containing(tracking, date)
                if (original != null && frozen) {
                    val source = PeriodCalculator.containing(tracking, LocalDate.parse(original.sleepDate))
                    summaries.revise(source)
                    if (target.index != source.index && target.end <= LocalDate.now(clock)) summaries.revise(target)
                }
                val pending = summaries.pendingAfterSave(tracking, date, LocalDate.now(clock))
                verify(expected)
                SaveResult.Success(expected, pending)
            }
            // Success is exposed only after Room commits and the persisted entry is read again.
            verify(committed.entry)
            committed
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: BeforeStart) {
            SaveResult.Failure(SaveFailure.BEFORE_TRACKING_START, input)
        } catch (_: ReadBackFailure) {
            SaveResult.Failure(SaveFailure.READ_BACK_FAILED, input)
        } catch (_: Exception) {
            SaveResult.Failure(SaveFailure.WRITE_FAILED, input)
        }
    }

    private suspend fun verify(expected: SleepDiaryEntry) {
        val actual = try { diaryDao.byId(expected.id) } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) { throw ReadBackFailure() }
        // Data-class equality compares every persisted field, including all raw business fields.
        if (actual != expected) throw ReadBackFailure()
    }
}
