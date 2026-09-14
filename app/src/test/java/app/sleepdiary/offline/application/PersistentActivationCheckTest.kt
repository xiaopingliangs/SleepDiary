package app.sleepdiary.offline.application

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.backup.*
import app.sleepdiary.offline.data.entity.*
import app.sleepdiary.offline.data.repository.*
import app.sleepdiary.offline.diary
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.time.LocalDate
import java.util.UUID

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34])
class PersistentActivationCheckTest {
    private val context:Context get()=ApplicationProvider.getApplicationContext()
    private lateinit var db:SleepDiaryDatabase
    private val backup get()=BackupRepository(db,context)
    private val local get()=LocalExecutionRepository(db)
    private val checker get()=PersistentActivationCheck(db)
    @Before fun setup() { db=Room.inMemoryDatabaseBuilder(context,SleepDiaryDatabase::class.java).build() }
    @After fun close() { db.close() }
    private suspend fun seed():ActivationToken {
        db.diaryDao().insert(diary())
        db.stateAndSummaryDao().putState(AppState(trackingStartDate="2026-09-09"))
        val saved=InputSnapshotStore(db).capture(UUID.randomUUID().toString(),LocalDate.parse("2026-09-01"),LocalDate.parse("2026-09-30")) as SnapshotSaveResult.Saved
        return checker.prepare(saved.snapshot.id)
    }
    @Test fun newLocalStateIsStableAndRequiresReview()=runBlocking {
        val first=local.current()
        assertEquals(first,local.current())
        assertEquals("REQUIRES_REVIEW",first.activationStatus)
        assertEquals(first.restoreGeneration,UUID.fromString(first.restoreGeneration).toString())
    }
    @Test fun exportAndPreviewDoNotCreateOrChangeLocalMetadata()=runBlocking {
        val text=backup.export();backup.preview(text)
        assertNull(db.localExecutionDao().state())
        val state=local.current();backup.export();backup.preview(text)
        assertEquals(state,db.localExecutionDao().state())
        assertFalse(text.contains("restoreGeneration"));assertFalse(text.contains("local_execution_state"))
    }
    @Test fun unchangedInputsNeverActivateTreatment()=runBlocking {
        val token=seed()
        assertEquals(ActivationBlock.REQUIRES_REVIEW,checker.inspect(token))
    }
    @Test fun restoreIdenticalDataInvalidatesOldToken()=runBlocking {
        val token=seed();val text=backup.export();val before=backup.snapshot()
        backup.restore(text)
        assertEquals(before,backup.snapshot())
        assertNotEquals(token.restoreGeneration,local.current().restoreGeneration)
        assertEquals(ActivationBlock.RESTORE_GENERATION_CHANGED,checker.inspect(token))
        assertEquals(ActivationBlock.REQUIRES_REVIEW,checker.inspect(checker.prepare(token.snapshotId)))
    }
    @Test fun everySupportedFormatRotatesGenerationWithoutAcceptingFileAuthority()=runBlocking {
        val generations=mutableSetOf(local.current().restoreGeneration)
        for(v in listOf("v1","v1.1","v2","v2.1")) {
            backup.restore(File("../samples/backup-$v.example.json").readText())
            assertTrue(generations.add(local.current().restoreGeneration))
            assertEquals("REQUIRES_REVIEW",local.current().activationStatus)
        }
    }
    @Test fun failedRestoreRollsBackBusinessDataAndLocalGeneration()=runBlocking {
        seed()
        val before=backup.snapshot();val state=local.current()
        for(point in listOf("afterDelete","afterEntries","afterWorkspace","afterSnapshots","afterState","afterLocalState")) {
            var reached=false
            val failing=BackupRepository(db,context){if(it==point){reached=true;error("injected")}}
            assertTrue(runCatching { failing.restore(File("../samples/backup-v2.1.example.json").readText()) }.isFailure)
            assertTrue(reached);assertEquals(before,backup.snapshot());assertEquals(state,local.current())
        }
    }
    @Test fun failedFirstRestoreDoesNotLeaveLazyMetadata()=runBlocking {
        val failing=BackupRepository(db,context){if(it=="afterLocalState")error("injected")}
        assertTrue(runCatching { failing.restore(File("../samples/backup-v2.1.example.json").readText()) }.isFailure)
        assertNull(db.localExecutionDao().state());assertTrue(db.diaryDao().all().isEmpty())
    }
    @Test fun corruptBackupDoesNotRotateGeneration()=runBlocking {
        seed();val before=backup.snapshot();val state=local.current()
        assertTrue(runCatching { backup.restore("{bad") }.isFailure)
        assertEquals(before,backup.snapshot());assertEquals(state,local.current())
    }
    @Test fun backupCannotInjectLocalExecutionAuthority()=runBlocking {
        seed();val before=backup.snapshot();val state=local.current()
        val injected=JSONObject(backup.export()).put("localExecutionState",JSONObject().put("activationStatus","ACTIVE").put("restoreGeneration",state.restoreGeneration))
        assertTrue(runCatching { backup.restore(injected.toString()) }.isFailure)
        assertEquals(before,backup.snapshot());assertEquals(state,local.current())
    }
    @Test fun corruptedLocalStatusFailsClosedWithoutRepairingIt()=runBlocking {
        val token=seed();val corrupt=local.current().copy(activationStatus="ACTIVE")
        db.localExecutionDao().update(corrupt)
        assertEquals(ActivationBlock.TECHNICAL_FAILURE,checker.inspect(token))
        assertEquals(corrupt,db.localExecutionDao().state())
    }
    @Test fun diaryEditInvalidatesPreparedInputs()=runBlocking {
        val token=seed();val e=db.diaryDao().all().single()
        db.diaryDao().update(e.copy(comment="new original text"))
        assertEquals(ActivationBlock.INPUT_CHANGED,checker.inspect(token))
    }
    @Test fun newSafetyAnswersInvalidatePreparedInputs()=runBlocking {
        val token=seed();val id=UUID.randomUUID().toString()
        SafetyRepository(db).save(id,listOf(SafetyAnswer(id,"S01","YES",detailJson=null)),false)
        assertEquals(ActivationBlock.INPUT_CHANGED,checker.inspect(token))
    }
    @Test fun missingSnapshotIsNotApproval()=runBlocking {
        val token=seed();db.inputSnapshotDao().deleteAllForRestore()
        assertEquals(ActivationBlock.SNAPSHOT_MISSING,checker.inspect(token))
    }
    @Test fun generationSurvivesDatabaseCloseAndReopen()=runBlocking {
        val name="local-execution-"+UUID.randomUUID()+".db"
        var disk=Room.databaseBuilder(context,SleepDiaryDatabase::class.java,name).build()
        try {
            val state=LocalExecutionRepository(disk).current()
            disk.close();disk=Room.databaseBuilder(context,SleepDiaryDatabase::class.java,name).build()
            assertEquals(state,LocalExecutionRepository(disk).current())
        } finally {disk.close();context.deleteDatabase(name)}
    }
}
