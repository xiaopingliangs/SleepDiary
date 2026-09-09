package app.sleepdiary.offline.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.sleepdiary.offline.clockOn
import app.sleepdiary.offline.diary
import app.sleepdiary.offline.data.dao.DiaryDao
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.entity.*
import app.sleepdiary.offline.data.repository.*
import kotlinx.coroutines.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class) @Config(sdk=[28])
class ReliabilityTest {
    private lateinit var db:SleepDiaryDatabase
    @Before fun setup() { db=Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(),SleepDiaryDatabase::class.java).build() }
    @After fun close() { db.close() }
    private fun repo(date:String="2026-09-09",dao:DiaryDao=db.diaryDao())=DiaryRepository(db,clockOn(date),dao)
    private suspend fun saved(e:SleepDiaryEntry,date:String=e.sleepDate):SleepDiaryEntry {
        val result=repo(date).save(e)
        assertTrue(result.toString(),result is SaveResult.Success)
        return (result as SaveResult.Success).entry
    }
    @Test fun saveReadsEveryFieldAndSetsAutomaticTimestamps() = runBlocking {
        val input=diary();val stored=saved(input)
        assertEquals(input.copy(createdAtUtc=clockOn(input.sleepDate).instant().toString(),updatedAtUtc=clockOn(input.sleepDate).instant().toString()),stored)
        assertEquals(stored,db.diaryDao().byId(stored.id))
        assertEquals(input.sleepDate,db.stateAndSummaryDao().state()?.trackingStartDate)
    }
    @Test fun updatesPreserveUuidAndCreationTime() = runBlocking {
        val old=saved(diary());val changed=old.copy(comment="changed",createdAtUtc="invalid metadata")
        val new=saved(changed,"2026-09-10")
        assertEquals(old.id,new.id);assertEquals(old.createdAtUtc,new.createdAtUtc)
        assertEquals(clockOn("2026-09-10").instant().toString(),new.updatedAtUtc)
        assertEquals("changed",new.comment);assertEquals(1,db.diaryDao().all().size)
    }
    @Test fun warningRequiresExplicitConfirmationAndPreservesEstimates() = runBlocking {
        val e=diary().copy(sleepOnsetLatencyMin=400,consistencyWarning=true)
        assertEquals(SaveResult.ValidationWarning(e),repo().save(e));assertTrue(db.diaryDao().all().isEmpty())
        assertNull(db.stateAndSummaryDao().state())
        val r=repo().save(e,confirmEstimateWarning=true) as SaveResult.Success
        assertTrue(r.entry.consistencyWarning);assertEquals(400,r.entry.sleepOnsetLatencyMin);assertEquals(30,r.entry.wakeAfterSleepOnsetMin)
    }
    @Test fun correctedEstimateClearsOnlyDerivedWarning() = runBlocking {
        val e=diary().copy(sleepOnsetLatencyMin=400)
        val first=(repo().save(e,true) as SaveResult.Success).entry
        val second=saved(first.copy(sleepOnsetLatencyMin=20))
        assertFalse(second.consistencyWarning)
    }
    @Test fun hardErrorNeverWritesEvenWithConfirmation() = runBlocking {
        val e=diary().copy(outOfBedAtLocal="2026-09-09T05:00:00")
        val r=repo().save(e,true) as SaveResult.Failure
        assertEquals(SaveFailure.INVALID_INPUT,r.reason);assertEquals(e,r.input);assertTrue(db.diaryDao().all().isEmpty())
    }
    @Test fun writeFailureReturnsInputForRetryAndNoSuccess() = runBlocking {
        val real=db.diaryDao()
        val failing=object:DiaryDao by real { override suspend fun insert(entry:SleepDiaryEntry) { error("injected write failure") } }
        val e=diary();val r=repo(dao=failing).save(e) as SaveResult.Failure
        assertEquals(SaveFailure.WRITE_FAILED,r.reason);assertEquals(e,r.input)
        assertTrue(real.all().isEmpty());assertNull(db.stateAndSummaryDao().state());assertNotNull(saved(e))
    }
    @Test fun missingReadBackRollsBackInsertedEntryAndState() = runBlocking {
        val real=db.diaryDao()
        val failing=object:DiaryDao by real { override suspend fun byId(id:String):SleepDiaryEntry? = null }
        assertEquals(SaveFailure.READ_BACK_FAILED,(repo(dao=failing).save(diary()) as SaveResult.Failure).reason)
        assertTrue(real.all().isEmpty());assertNull(db.stateAndSummaryDao().state())
    }
    @Test fun mismatchedReadBackRollsBackUpdate() = runBlocking {
        val old=saved(diary());val real=db.diaryDao();var reads=0
        val failing=object:DiaryDao by real {
            override suspend fun byId(id:String):SleepDiaryEntry? {
                val row=real.byId(id);reads++
                return if(reads==1) row else row?.copy(awakeningCount=98)
            }
        }
        assertEquals(SaveFailure.READ_BACK_FAILED,(repo(dao=failing).save(old.copy(comment="new")) as SaveResult.Failure).reason)
        assertEquals(old,real.byId(old.id))
    }
    @Test fun committedReadFailureStillCannotReturnSuccess() = runBlocking {
        val real=db.diaryDao();var reads=0
        val failing=object:DiaryDao by real {
            override suspend fun byId(id:String):SleepDiaryEntry? { reads++;return if(reads==4) null else real.byId(id) }
        }
        val e=diary()
        assertEquals(SaveFailure.READ_BACK_FAILED,(repo(dao=failing).save(e) as SaveResult.Failure).reason)
        assertNotNull(real.byId(e.id)) // Commit occurred; failure is explicitly retryable under the same UUID.
        saved(e);assertEquals(1,real.all().size)
    }
    @Test fun concurrentSaveDoesNotDuplicateUuidOrDate() = runBlocking {
        val e=diary();val repository=repo()
        val results=awaitAll(async(Dispatchers.Default){repository.save(e)},async(Dispatchers.Default){repository.save(e)})
        assertTrue(results.all { it is SaveResult.Success });assertEquals(1,db.diaryDao().all().size)
        assertTrue(repository.save(diary()) is SaveResult.Failure);assertEquals(1,db.diaryDao().all().size)
    }
    @Test fun earliestEditCanMoveStartBeforeFirstSnapshot() = runBlocking {
        val first=saved(diary());saved(diary("2026-09-10"))
        val moved=diary("2026-09-11",first.id)
        saved(moved,"2026-09-11")
        assertEquals("2026-09-10",db.stateAndSummaryDao().state()?.trackingStartDate)
        assertTrue(db.stateAndSummaryDao().summaries().isEmpty())
    }
    @Test fun firstSnapshotFreezesStartAndBeforeStartEditIsRejected() = runBlocking {
        val first=saved(diary());val snapshot=WeeklySummaryRepository(db,clockOn("2026-09-15")).generate(1)
        saved(diary("2026-09-10",first.id),"2026-09-16")
        assertEquals("2026-09-09",db.stateAndSummaryDao().state()?.trackingStartDate)
        val before=diary("2026-09-08",first.id)
        assertEquals(SaveFailure.BEFORE_TRACKING_START,(repo("2026-09-16").save(before) as SaveResult.Failure).reason)
        assertEquals("2026-09-10",db.diaryDao().byId(first.id)?.sleepDate)
        assertNotNull(db.stateAndSummaryDao().summaryById(snapshot.id))
    }
    @Test fun samePeriodEditRetainsPreviousRevision() = runBlocking {
        val e=saved(diary());val old=WeeklySummaryRepository(db,clockOn("2026-09-15")).generate(1)
        saved(e.copy(sleepOnsetLatencyMin=40),"2026-09-16")
        val rows=db.stateAndSummaryDao().summaries()
        assertEquals(2,rows.size);assertEquals(old.copy(isCurrent=false),rows[0]);assertEquals(2,rows[1].revision)
        assertTrue(rows[1].isCurrent);assertEquals(old.id,rows[1].supersedesSummaryId);assertEquals(40.0,rows[1].avgSolMin!!,0.0)
        assertEquals(old.sourceEntryIdsJson,rows[1].sourceEntryIdsJson)
    }
    @Test fun crossPeriodEditRevisesBothEndedExistingSummaries() = runBlocking {
        val a=saved(diary());saved(diary("2026-09-16"))
        val summaries=WeeklySummaryRepository(db,clockOn("2026-09-23"));summaries.generate(1);summaries.generate(2)
        saved(diary("2026-09-17",a.id),"2026-09-23")
        val rows=db.stateAndSummaryDao().summaries()
        assertEquals(4,rows.size)
        val current=rows.filter { it.isCurrent }.associateBy { it.periodIndex }
        assertEquals(2,current[1]?.revision);assertEquals(0,current[1]?.validEntryCount);assertNull(current[1]?.avgSolMin)
        assertEquals(2,current[2]?.revision);assertEquals(2,current[2]?.validEntryCount)
        assertTrue(current[2]!!.sourceEntryIdsJson.contains(a.id));assertEquals(a.id,db.diaryDao().byId(a.id)?.id)
    }
    @Test fun moveIntoUnfinishedPeriodDoesNotGenerateTarget() = runBlocking {
        val e=saved(diary());WeeklySummaryRepository(db,clockOn("2026-09-15")).generate(1)
        saved(diary("2026-09-16",e.id),"2026-09-16")
        val rows=db.stateAndSummaryDao().summaries()
        assertEquals(2,rows.size);assertTrue(rows.all { it.periodIndex==1 });assertEquals(0,rows.last().validEntryCount)
    }
    @Test fun revisionFailureRollsBackEntryStateAndBothRevisions() = runBlocking {
        val e=saved(diary());val old=WeeklySummaryRepository(db,clockOn("2026-09-15")).generate(1)
        val real=db.diaryDao();var reads=0
        val failing=object:DiaryDao by real {
            override suspend fun byId(id:String):SleepDiaryEntry? { reads++;return if(reads==3) null else real.byId(id) }
        }
        val before=db.stateAndSummaryDao().state()
        assertTrue(repo("2026-09-16",failing).save(e.copy(comment="rollback")) is SaveResult.Failure)
        assertEquals(e,real.byId(e.id));assertEquals(listOf(old),db.stateAndSummaryDao().summaries());assertEquals(before,db.stateAndSummaryDao().state())
    }
    @Test fun seventhDaySaveGeneratesCurrentPeriod() = runBlocking {
        saved(diary());val r=repo("2026-09-15").save(diary("2026-09-15")) as SaveResult.Success
        assertEquals(1,r.pendingSummary?.periodIndex);assertEquals(2,r.pendingSummary?.validEntryCount)
        assertEquals(0,db.stateAndSummaryDao().state()?.lastDisplayedWeeklyPeriodIndex)
    }
    @Test fun missingSeventhDayGeneratesEarlierPeriodOnNextSave() = runBlocking {
        val e=saved(diary());val r=repo("2026-09-16").save(diary("2026-09-16")) as SaveResult.Success
        assertEquals(1,r.pendingSummary?.periodIndex);assertEquals(1,r.pendingSummary?.validEntryCount)
        assertEquals("[\""+e.id+"\"]",r.pendingSummary?.sourceEntryIdsJson)
    }
    @Test fun emptyPeriodsAreSkippedWithoutRenumbering() = runBlocking {
        saved(diary());WeeklySummaryRepository(db,clockOn("2026-09-15")).generate(1)
        val state=checkNotNull(db.stateAndSummaryDao().state())
        db.stateAndSummaryDao().putState(state.copy(lastDisplayedWeeklyPeriodIndex=1))
        saved(diary("2026-09-30"),"2026-09-30") // Period 4, periods 2 and 3 empty.
        val r=repo("2026-10-07").save(diary("2026-10-07")) as SaveResult.Success
        assertEquals(4,r.pendingSummary?.periodIndex);assertEquals(1,r.pendingSummary?.validEntryCount)
        assertTrue(db.stateAndSummaryDao().summaries().none { it.periodIndex in 2..3 })
    }
    @Test fun unfinishedPeriodCannotBeGeneratedExplicitly() = runBlocking {
        saved(diary());assertTrue(runCatching { WeeklySummaryRepository(db,clockOn("2026-09-14")).generate(1) }.isFailure)
        assertTrue(db.stateAndSummaryDao().summaries().isEmpty())
    }

    @Test fun eachRawFieldMismatchIsDetected() = runBlocking {
        val e=saved(diary());val real=db.diaryDao()
        val mutations:List<(SleepDiaryEntry)->SleepDiaryEntry> = listOf(
            { it.copy(id="other") },{ it.copy(sleepDate="2026-09-10") },
            { it.copy(bedEnteredAtLocal="changed") },{ it.copy(trySleepAtLocal="changed") },
            { it.copy(sleepOnsetLatencyMin=21) },{ it.copy(awakeningCount=3) },{ it.copy(wakeAfterSleepOnsetMin=31) },
            { it.copy(finalAwakeningAtLocal="changed") },{ it.copy(outOfBedAtLocal="changed") },
            { it.copy(sleepQuality=4) },{ it.copy(comment="changed") },{ it.copy(timeZoneIdAtEntry="UTC") },
            { it.copy(createdAtUtc="changed") },{ it.copy(updatedAtUtc="changed") },
            { it.copy(diarySchemaVersion="changed") },{ it.copy(consistencyWarning=true) })
        mutations.forEach { mutate ->
            var reads=0
            val failing=object:DiaryDao by real {
                override suspend fun byId(id:String):SleepDiaryEntry? {
                    reads++;return real.byId(id)?.let { if(reads==1) it else mutate(it) }
                }
            }
            assertEquals(SaveFailure.READ_BACK_FAILED,(repo(dao=failing).save(e) as SaveResult.Failure).reason)
            assertEquals(e,real.byId(e.id))
        }
    }
    @Test fun crossPeriodFailureRollsBackBothRevisions() = runBlocking {
        val e=saved(diary());saved(diary("2026-09-16"))
        WeeklySummaryRepository(db,clockOn("2026-09-22")).generate(2)
        val before=db.stateAndSummaryDao().summaries();val real=db.diaryDao();var reads=0
        val failing=object:DiaryDao by real {
            override suspend fun byId(id:String):SleepDiaryEntry? { reads++;return if(reads==3) null else real.byId(id) }
        }
        assertTrue(repo("2026-09-23",failing).save(diary("2026-09-17",e.id)) is SaveResult.Failure)
        assertEquals(e,real.byId(e.id));assertEquals(before,db.stateAndSummaryDao().summaries())
    }
    @Test fun collisionWhileMovingDatePreservesOriginalAndSummaries() = runBlocking {
        val e=saved(diary());saved(diary("2026-09-10"))
        val before=WeeklySummaryRepository(db,clockOn("2026-09-15")).generate(1)
        assertTrue(repo("2026-09-16").save(diary("2026-09-10",e.id)) is SaveResult.Failure)
        assertEquals(e,db.diaryDao().byId(e.id));assertEquals(listOf(before),db.stateAndSummaryDao().summaries())
    }
    @Test fun appStateFlagsSurviveDiarySave() = runBlocking {
        val state=AppState(hasSeenMinimalInteractionExplanation=true,hasSeenSharePrivacyNotice=true)
        db.stateAndSummaryDao().putState(state);saved(diary())
        assertEquals(state.copy(trackingStartDate="2026-09-09"),db.stateAndSummaryDao().state())
    }
}
