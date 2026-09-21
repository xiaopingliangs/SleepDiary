package app.sleepdiary.offline.application

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.backup.*
import app.sleepdiary.offline.data.entity.*
import app.sleepdiary.offline.data.repository.*
import app.sleepdiary.offline.diary
import app.sleepdiary.offline.clockOn
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.time.LocalDate
import java.util.Base64
import java.util.UUID

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34])
class InputSnapshotStoreTest {
    private val context:Context get()=ApplicationProvider.getApplicationContext()
    private lateinit var db:SleepDiaryDatabase
    private val from=LocalDate.parse("2026-09-01")
    private val through=LocalDate.parse("2026-09-30")
    private val backup get()=BackupRepository(db,context)
    @Before fun setup() { db=Room.inMemoryDatabaseBuilder(context,SleepDiaryDatabase::class.java).build() }
    @After fun close() { db.close() }
    private suspend fun capture(id:String=UUID.randomUUID().toString())=
        (InputSnapshotStore(db).capture(id,from,through) as SnapshotSaveResult.Saved).snapshot
    private suspend fun seedAll() {
        db.diaryDao().insert(diary())
        db.stateAndSummaryDao().putState(AppState(trackingStartDate="2026-09-09"))
        val id=UUID.randomUUID().toString()
        SafetyRepository(db).save(id,listOf(SafetyAnswer(id,"S01","YES",detailJson=null)),false)
        WeeklySummaryRepository(db,clockOn("2026-09-20")).generate(1)
        WorkspaceRepository(db).save(WorkspaceDraft(UUID.randomUUID().toString(),"REFLECTION","标题","原文"))
        capture()
    }
    @Test fun capturesRawInputAndReadsBackEveryField()=runBlocking {
        db.diaryDao().insert(diary().copy(comment=" 中文 English 123 "))
        val saved=capture()
        assertEquals(saved,db.inputSnapshotDao().byId(saved.id))
        InputSnapshotValidation.validate(saved)
        assertTrue(String(Base64.getDecoder().decode(saved.payloadBase64),Charsets.UTF_8).contains(" 中文 English 123 "))
    }
    @Test fun retryKeepsHistoricalInputAfterDiaryChanges()=runBlocking {
        val e=diary();db.diaryDao().insert(e)
        val first=capture()
        db.diaryDao().update(e.copy(comment="new"))
        assertEquals(first,capture(first.id));assertEquals(1,db.inputSnapshotDao().all().size)
        assertNotEquals(first.contentHash,capture().contentHash)
    }
    @Test fun sameCommandWithDifferentScopeIsRejected()=runBlocking {
        val first=capture()
        assertEquals(SnapshotSaveResult.CommandConflict,InputSnapshotStore(db).capture(first.id,from.plusDays(1),through))
        assertEquals(listOf(first),db.inputSnapshotDao().all())
    }
    @Test fun transactionFailureDoesNotLeaveSnapshot()=runBlocking {
        val id=UUID.randomUUID().toString()
        val result=InputSnapshotStore(db,checkpoint={if(it=="afterWrite")error("injected")}).capture(id,from,through)
        assertEquals(SnapshotSaveResult.Failed(id,false),result);assertNull(db.inputSnapshotDao().byId(id))
    }
    @Test fun lostAcknowledgementReturnsSameCommittedRecordOnRetry()=runBlocking {
        val id=UUID.randomUUID().toString()
        assertEquals(SnapshotSaveResult.Failed(id,true),
            InputSnapshotStore(db,checkpoint={if(it=="afterCommit")error("injected")}).capture(id,from,through))
        val original=db.inputSnapshotDao().byId(id)
        assertEquals(original,capture(id));assertEquals(1,db.inputSnapshotDao().all().size)
    }
    @Test fun diskReopenPreservesSnapshot()=runBlocking {
        val name="snapshot-"+UUID.randomUUID()+".db"
        var disk=Room.databaseBuilder(context,SleepDiaryDatabase::class.java,name).build()
        try {
            val result=InputSnapshotStore(disk).capture(UUID.randomUUID().toString(),from,through) as SnapshotSaveResult.Saved
            disk.close();disk=Room.databaseBuilder(context,SleepDiaryDatabase::class.java,name).build()
            assertEquals(result.snapshot,disk.inputSnapshotDao().byId(result.snapshot.id))
        } finally { disk.close();context.deleteDatabase(name) }
    }
    @Test fun completeBackupRoundTripIncludesSnapshotsAndAllSevenTables()=runBlocking {
        seedAll();val before=backup.snapshot();val text=backup.export()
        assertEquals("BACKUP_3.0",JSONObject(text).getString("exportFormatVersion"))
        capture();backup.restore(text);assertEquals(before,backup.snapshot())
    }
    @Test fun legacyRestoreClearsSnapshotsAndKeepsItsOwnData()=runBlocking {
        for(v in listOf("v1","v1.1","v2")) {
            capture()
            val text=File("../samples/backup-$v.example.json").readText()
            assertTrue(backup.preview(text).inputSnapshots.isEmpty())
            backup.restore(text);assertTrue(db.inputSnapshotDao().all().isEmpty())
            assertTrue(db.diaryDao().all().isNotEmpty())
        }
    }
    @Test fun failuresAtAllRestoreStagesRollbackSevenTables()=runBlocking {
        seedAll();val before=backup.snapshot()
        for(v in listOf("v1","v1.1","v2","v2.1")) for(point in listOf("afterDelete","afterEntries","afterWorkspace","afterSnapshots","afterState")) {
            var reached=false
            val repo=BackupRepository(db,context){if(it==point){reached=true;error("injected")}}
            assertTrue(runCatching { repo.restore(File("../samples/backup-$v.example.json").readText()) }.isFailure)
            assertTrue("Failure must occur at $point for $v, not in earlier validation",reached)
            assertEquals(before,backup.snapshot())
        }
    }
    @Test fun corruptHashEncodingVersionAndDuplicateIdsCannotChangeDatabase()=runBlocking {
        seedAll();val before=backup.snapshot();val original=backup.export()
        for((key,value) in listOf("contentHash" to "a".repeat(64),"payloadBase64" to "!","captureVersion" to "UNKNOWN",
            "id" to "bad","fromDate" to "2030-01-01","unknown" to "field")) {
            val bad=JSONObject(original);bad.getJSONArray("inputSnapshots").getJSONObject(0).put(key,value)
            assertTrue(runCatching { backup.restore(bad.toString()) }.isFailure);assertEquals(before,backup.snapshot())
        }
        val duplicate=JSONObject(original);val items=duplicate.getJSONArray("inputSnapshots");items.put(items.getJSONObject(0))
        assertTrue(runCatching { backup.restore(duplicate.toString()) }.isFailure);assertEquals(before,backup.snapshot())
    }
    @Test fun correctlyHashedButMalformedPayloadIsRejected()=runBlocking {
        val original=capture();val bytes=Base64.getDecoder().decode(original.payloadBase64)
        for(changed in listOf(byteArrayOf(1,2,3),bytes+byteArrayOf(0),bytes.copyOf(bytes.size-1))) {
            val hash=CapturedProtocolInput(original.id,original.capturedAtUtc,changed).contentHash
            val bad=original.copy(payloadBase64=Base64.getEncoder().encodeToString(changed),contentHash=hash)
            assertTrue(runCatching { InputSnapshotValidation.validate(bad) }.isFailure)
        }
    }
    @Test fun invalidCommandsDoNotWrite()=runBlocking {
        assertEquals(SnapshotSaveResult.InvalidRequest,InputSnapshotStore(db).capture("bad",from,through))
        assertEquals(SnapshotSaveResult.InvalidRequest,InputSnapshotStore(db).capture(UUID.randomUUID().toString(),through,from))
        assertTrue(db.inputSnapshotDao().all().isEmpty())
    }
    @Test fun publishedExampleContainsValidHistoricalSnapshot() {
        val sample=backup.preview(File("../samples/backup-v2.1.example.json").readText())
        assertEquals(1,sample.inputSnapshots.size)
        InputSnapshotValidation.validate(sample.inputSnapshots.single())
    }
}
