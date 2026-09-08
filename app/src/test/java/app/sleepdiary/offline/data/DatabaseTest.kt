
package app.sleepdiary.offline.data

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.entity.*
import app.sleepdiary.offline.data.repository.DiaryRepository
import app.sleepdiary.offline.domain.model.*
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class DatabaseTest {
 private lateinit var db: SleepDiaryDatabase
 private val context: Context get() = ApplicationProvider.getApplicationContext()
 @Before fun setUp() { db = Room.inMemoryDatabaseBuilder(context, SleepDiaryDatabase::class.java).build() }
 @After fun tearDown() { db.close() }

 private fun entry() = SleepDiaryEntry(
  id = UUID.randomUUID().toString(), sleepDate = "2026-09-09",
  bedEnteredAtLocal = "2026-09-08T23:00:00", trySleepAtLocal = "2026-09-08T23:30:00",
  sleepOnsetLatencyMin = 20, awakeningCount = 2, wakeAfterSleepOnsetMin = 30,
  finalAwakeningAtLocal = "2026-09-09T06:00:00", outOfBedAtLocal = "2026-09-09T07:00:00",
  sleepQuality = 3, comment = "主观估计，备注", timeZoneIdAtEntry = "Asia/Shanghai",
  createdAtUtc = "2026-09-09T00:10:00Z", updatedAtUtc = "2026-09-09T00:10:00Z",
  consistencyWarning = false
 )
 @Test fun databaseCreatesAllFiveTables() {
  val names = mutableSetOf<String>()
  db.openHelper.writableDatabase.query("SELECT name FROM sqlite_master WHERE type='table'").use {
   while (it.moveToNext()) names.add(it.getString(0))
  }
  assertTrue(names.containsAll(listOf("sleep_diary_entry", "safety_assessment", "safety_answer", "weekly_summary_snapshot", "app_state")))
 }
 @Test fun diaryWritesAndReadsAllRawFieldsByUuid() = runBlocking {
  val original = entry()
  assertEquals(original, DiaryRepository(db).insertAndVerify(original))
  assertEquals(original, db.diaryDao().byId(original.id))
 }
 @Test fun safetyDraftAndAnswersReadAsRelation() = runBlocking {
  val assessment = SafetyAssessment(UUID.randomUUID().toString(), "2026-09-09T00:00:00Z",
   derivedGate = SafetyGate.DELAY_AUTOMATED_TREATMENT, status = AssessmentStatus.DRAFT, isCurrent = true)
  val answer = SafetyAnswer(assessment.id, "S03", "YES", detailJson = null)
  db.withTransaction {
   db.safetyDao().insertAssessment(assessment)
   db.safetyDao().insertAnswers(listOf(answer))
  }
  val stored = checkNotNull(db.safetyDao().byId(assessment.id))
  assertEquals(assessment, stored.assessment)
  assertEquals(listOf(answer), stored.answers)
 }
 @Test fun weeklySnapshotWritesWithoutCalculatingAnything() = runBlocking {
  val original = entry(); db.diaryDao().insert(original)
  val snapshot = WeeklySummarySnapshot(
   id = UUID.randomUUID().toString(), periodIndex = 1, periodStartDate = "2026-09-03",
   periodEndDate = "2026-09-09", validEntryCount = 1,
   avgSolMin = 20.0, avgAwakeningCount = 2.0, avgWasoMin = 30.0,
   avgTerminalWakefulnessMin = 60.0, avgTibMin = 480.0, avgSleepQuality = 3.0,
   sourceEntryIdsJson = "[\"" + original.id + "\"]",
   generatedAtUtc = "2026-09-09T00:10:00Z", revision = 1, isCurrent = true, supersedesSummaryId = null)
  db.stateAndSummaryDao().insertSummary(snapshot)
  assertEquals(snapshot, db.stateAndSummaryDao().summaryById(snapshot.id))
 }
 @Test fun appStatePersistsAllBackupFields() = runBlocking {
  val state = AppState(trackingStartDate = "2026-09-09", hasSeenMinimalInteractionExplanation = true,
   hasSeenSharePrivacyNotice = true, lastDisplayedWeeklyPeriodIndex = 2)
  db.stateAndSummaryDao().putState(state)
  assertEquals(state, db.stateAndSummaryDao().state())
 }
 @Test fun duplicateUuidFailsWithoutOverwritingRawData() = runBlocking {
  val original = entry(); db.diaryDao().insert(original)
  var failed = false
  try { db.diaryDao().insert(original.copy(comment = "must not overwrite")) } catch (_: Exception) { failed = true }
  assertTrue(failed)
  assertEquals(original, db.diaryDao().byId(original.id))
 }
 @Test fun foreignKeyFailureRollsBackWholeTransaction() = runBlocking {
  val original = entry()
  var failed = false
  try {
   db.withTransaction {
    db.diaryDao().insert(original)
    db.safetyDao().insertAnswers(listOf(SafetyAnswer(UUID.randomUUID().toString(), "S03", "YES", detailJson = null)))
   }
  } catch (_: Exception) { failed = true }
  assertTrue(failed)
  assertNull(db.diaryDao().byId(original.id))
 }
 @Test fun savedDiarySurvivesDatabaseCloseAndReopen() = runBlocking {
  val name = "persistence-" + UUID.randomUUID() + ".db"
  val original = entry()
  val first = Room.databaseBuilder(context, SleepDiaryDatabase::class.java, name).build()
  try { DiaryRepository(first).insertAndVerify(original) } finally { first.close() }
  val reopened = Room.databaseBuilder(context, SleepDiaryDatabase::class.java, name).build()
  try { assertEquals(original, reopened.diaryDao().byId(original.id)) }
  finally { reopened.close(); context.deleteDatabase(name) }
 }
}
