package app.sleepdiary.offline.application

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.sleepdiary.offline.data.backup.BackupRepository
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.repository.LocalExecutionRepository
import app.sleepdiary.offline.domain.treatment.TreatmentMode
import app.sleepdiary.offline.domain.treatment.TreatmentPlan
import app.sleepdiary.offline.domain.treatment.TreatmentPlanOrigin
import app.sleepdiary.offline.domain.treatment.TreatmentPlanStatus
import app.sleepdiary.offline.domain.treatment.TreatmentState
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.UUID

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34])
class TreatmentStateControllerTest {
    private val context:Context get()=ApplicationProvider.getApplicationContext()
    private lateinit var db:SleepDiaryDatabase
    private val clock=Clock.fixed(Instant.parse("2026-09-22T00:00:00Z"),ZoneOffset.UTC)
    @Before fun setup(){db=Room.inMemoryDatabaseBuilder(context,SleepDiaryDatabase::class.java).build()}
    @After fun close(){db.close()}
    private fun id()=UUID.randomUUID().toString()
    private suspend fun start(commandId:String=id(),controller:TreatmentStateController=TreatmentStateController(db,clock)) =
        controller.startBaseline(commandId,TreatmentMode.SELF_GUIDED,true) as TreatmentCommandResult.Applied

    @Test fun minorOrUnconfirmedAdultCannotCreateEpisode()=runBlocking {
        val result=TreatmentStateController(db,clock).startBaseline(id(),TreatmentMode.SELF_GUIDED,false)
        assertEquals(TreatmentCommandResult.Rejected(TreatmentRejection.NOT_ADULT_SCOPE),result)
        assertTrue(db.treatmentDao().episodes().isEmpty());assertTrue(db.treatmentDao().audits().isEmpty())
    }
    @Test fun startPersistsEpisodeAndAuditWithLocalGeneration()=runBlocking {
        val result=start()
        assertEquals(TreatmentState.BASELINE_COLLECTING.name,result.episode.state)
        assertEquals("NONE",result.episode.protocolVersion);assertTrue(result.episode.adultScopeConfirmed)
        assertEquals(result.episode,db.treatmentDao().episode(result.episode.id))
        assertEquals(1,result.audit.toRevision);assertEquals(null,result.audit.fromState)
        assertEquals(LocalExecutionRepository(db).current().restoreGeneration,result.audit.restoreGenerationAtCommand)
    }
    @Test fun commandRetryIsIdempotentAndDifferentPayloadConflicts()=runBlocking {
        val command=id();start(command)
        assertTrue(TreatmentStateController(db,clock).startBaseline(command,TreatmentMode.SELF_GUIDED,true) is TreatmentCommandResult.AlreadyApplied)
        assertEquals(TreatmentCommandResult.Rejected(TreatmentRejection.COMMAND_CONFLICT),
            TreatmentStateController(db,clock).startBaseline(command,TreatmentMode.CLINICIAN_ASSISTED,true))
        assertEquals(1,db.treatmentDao().episodes().size);assertEquals(1,db.treatmentDao().audits().size)
    }
    @Test fun secondUnfinishedEpisodeIsRejected()=runBlocking {
        start();val result=TreatmentStateController(db,clock).startBaseline(id(),TreatmentMode.CLINICIAN_ASSISTED,true)
        assertEquals(TreatmentCommandResult.Rejected(TreatmentRejection.ACTIVE_EPISODE_EXISTS),result)
    }
    @Test fun baselineReviewPauseResumeAndReturnAreExplicitAndAudited()=runBlocking {
        val episode=start().episode;val c=TreatmentStateController(db,clock)
        val review=c.transition(id(),episode.id,1,TreatmentTransition.REQUEST_BASELINE_REVIEW) as TreatmentCommandResult.Applied
        val paused=c.transition(id(),episode.id,2,TreatmentTransition.PAUSE) as TreatmentCommandResult.Applied
        assertEquals(TreatmentState.USER_PAUSED.name,paused.episode.state);assertEquals(TreatmentState.BASELINE_REVIEW.name,paused.episode.pausedFromState)
        val resumed=c.transition(id(),episode.id,3,TreatmentTransition.RESUME) as TreatmentCommandResult.Applied
        assertEquals(TreatmentState.BASELINE_REVIEW.name,resumed.episode.state);assertNull(resumed.episode.pausedFromState)
        val collecting=c.transition(id(),episode.id,4,TreatmentTransition.RETURN_TO_BASELINE_COLLECTION) as TreatmentCommandResult.Applied
        assertEquals(TreatmentState.BASELINE_COLLECTING.name,collecting.episode.state)
        assertEquals((1..5).toList(),db.treatmentDao().auditsForEpisode(episode.id).map{it.toRevision})
        assertEquals(review.audit.toState,paused.audit.fromState)
    }
    @Test fun staleInvalidAndMedicalTransitionsDoNotMutate()=runBlocking {
        val episode=start().episode;val c=TreatmentStateController(db,clock);val before=db.treatmentDao().audits()
        assertEquals(TreatmentCommandResult.Rejected(TreatmentRejection.STALE_REVISION),c.transition(id(),episode.id,9,TreatmentTransition.PAUSE))
        assertEquals(TreatmentCommandResult.Rejected(TreatmentRejection.INVALID_TRANSITION),c.transition(id(),episode.id,1,TreatmentTransition.RESUME))
        assertEquals(TreatmentCommandResult.Rejected(TreatmentRejection.MEDICAL_RULE_BLOCKED),c.transition(id(),episode.id,1,TreatmentTransition.CONFIRM_PLAN))
        assertEquals(before,db.treatmentDao().audits());assertEquals(episode,db.treatmentDao().episode(episode.id))
    }
    @Test fun transactionFailureAndLostAcknowledgementAreDistinct()=runBlocking {
        val firstId=id();val failed=TreatmentStateController(db,clock){if(it=="afterTreatmentWrite")error("injected")}
            .startBaseline(firstId,TreatmentMode.SELF_GUIDED,true)
        assertEquals(TreatmentCommandResult.Failed(firstId,false),failed);assertNull(db.treatmentDao().episode(firstId))
        val secondId=id();val uncertain=TreatmentStateController(db,clock){if(it=="afterTreatmentCommit")error("injected")}
            .startBaseline(secondId,TreatmentMode.SELF_GUIDED,true)
        assertEquals(TreatmentCommandResult.Failed(secondId,true),uncertain);assertNotNull(db.treatmentDao().episode(secondId))
        assertTrue(TreatmentStateController(db,clock).startBaseline(secondId,TreatmentMode.SELF_GUIDED,true) is TreatmentCommandResult.AlreadyApplied)
    }
    @Test fun backupThreeRoundTripAndLegacyReplacementAreAtomic()=runBlocking {
        start();val repo=BackupRepository(db,context);val before=repo.snapshot();val text=repo.export()
        assertEquals("BACKUP_3.0",JSONObject(text).getString("exportFormatVersion"));assertEquals(1,repo.preview(text).treatmentEpisodes.size)
        val c=TreatmentStateController(db,clock);val episode=before.treatmentEpisodes.single()
        c.transition(id(),episode.id,1,TreatmentTransition.REQUEST_BASELINE_REVIEW)
        repo.restore(text);assertEquals(before,repo.snapshot())
        repo.restore(File("../samples/backup-v2.1.example.json").readText())
        assertTrue(repo.snapshot().treatmentEpisodes.isEmpty());assertTrue(repo.snapshot().treatmentAuditEvents.isEmpty())
    }
    @Test fun malformedTreatmentBackupNeverChangesDatabase()=runBlocking {
        start();val repo=BackupRepository(db,context);val before=repo.snapshot();val bad=JSONObject(repo.export())
        bad.getJSONArray("treatmentAuditEvents").getJSONObject(0).put("toRevision",7)
        assertTrue(runCatching{repo.restore(bad.toString())}.isFailure);assertEquals(before,repo.snapshot())
    }
    @Test fun failureAtTreatmentRestoreCheckpointsRollsBackEverything()=runBlocking {
        start();val repo=BackupRepository(db,context);val before=repo.snapshot()
        val legacy=File("../samples/backup-v2.1.example.json").readText()
        for(point in listOf("afterTreatmentDelete","afterTreatment")){
            var reached=false
            val failing=BackupRepository(db,context){if(it==point){reached=true;error("injected")}}
            assertTrue(runCatching{failing.restore(legacy)}.isFailure);assertTrue(reached);assertEquals(before,repo.snapshot())
        }
    }
    private fun plan(protocol:String="TEST_PROTOCOL")=TreatmentPlan(
        id(),id(),1,protocol,"TEST_RULES",LocalDate.parse("2026-09-22"),null,
        LocalTime.parse("07:00"),1,LocalTime.parse("06:59"),"TEST_SC",
        TreatmentMode.SELF_GUIDED,TreatmentPlanOrigin.USER_CONFIRMED,id(),id(),id(),
        "synthetic domain test",Instant.parse("2026-09-22T00:00:00Z"),null,TreatmentPlanStatus.CANDIDATE)
    @Test fun immutablePlanSkeletonRetainsEveryTraceReference(){val value=plan();assertEquals(3,listOf(value.inputSnapshotId,value.metricSnapshotId,value.safetySnapshotId).distinct().size);assertEquals(1,value.revision)}
    @Test fun planSkeletonCannotTreatNoneAsReleasedProtocol(){assertTrue(runCatching{plan("NONE")}.isFailure)}
}
