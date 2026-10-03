package app.sleepdiary.offline.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.sleepdiary.offline.clockOn
import app.sleepdiary.offline.diary
import app.sleepdiary.offline.data.backup.*
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.entity.*
import app.sleepdiary.offline.data.repository.*
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.UUID

@RunWith(RobolectricTestRunner::class) @Config(sdk = [34])
class WorkspaceDataTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private lateinit var db: SleepDiaryDatabase
    private val repository get() = WorkspaceRepository(db, clockOn("2026-09-13"))
    private val backup get() = BackupRepository(db, context)
    private val body = " 中文 English 123,\"原文\"\r\n第二行 🙂 "
    @Before fun setup() { db = Room.inMemoryDatabaseBuilder(context, SleepDiaryDatabase::class.java).build() }
    @After fun close() { db.close() }
    private fun draft(text: String = body) = WorkspaceDraft(UUID.randomUUID().toString(), WorkspaceKind.REFLECTION.name, "标题", text)
    private fun editing(record: WorkspaceRecord, text: String) = WorkspaceDraft(record.id, record.kind, record.title, text,
        record.revision, WorkspaceValidation.fingerprint(record))
    private suspend fun saved(input: WorkspaceDraft = draft()) = (repository.save(input) as WorkspaceSaveResult.Saved).record
    private suspend fun seedAll() {
        db.diaryDao().insert(diary()); db.stateAndSummaryDao().putState(AppState(trackingStartDate = "2026-09-09", hasCompletedOnboarding = true))
        val id = UUID.randomUUID().toString()
        SafetyRepository(db).save(id, listOf(SafetyAnswer(id, "S01", "YES", detailJson = null)), false)
        WeeklySummaryRepository(db, clockOn("2026-09-20")).generate(1)
        saved()
    }
    @Test fun saveReadsEveryFieldBackWithoutChangingText() = runBlocking {
        val record = saved()
        assertEquals(record, db.workspaceDao().byId(record.id)); assertEquals(body, record.body)
        assertEquals(1, record.revision)
    }
    @Test fun editRetainsIdentityAndCreationTime() = runBlocking {
        val original = saved()
        val result = WorkspaceRepository(db, clockOn("2026-09-14")).save(editing(original, "new")) as WorkspaceSaveResult.Saved
        assertEquals(original.id, result.record.id); assertEquals(original.createdAtUtc, result.record.createdAtUtc)
        assertEquals(2, result.record.revision); assertNotEquals(original.updatedAtUtc, result.record.updatedAtUtc)
        assertEquals("new", db.workspaceDao().byId(original.id)!!.body)
    }
    @Test fun failedTransactionLeavesNoRow() = runBlocking {
        val input = draft()
        val result = WorkspaceRepository(db, clockOn("2026-09-13")) { if(it == "afterWrite") error("injected") }.save(input)
        assertEquals(WorkspaceSaveResult.Failed(input.id, false), result); assertNull(db.workspaceDao().byId(input.id))
    }
    @Test fun lostAcknowledgementRetriesWithoutDuplicateOrRevisionIncrease() = runBlocking {
        val input = draft()
        val result = WorkspaceRepository(db, clockOn("2026-09-13")) { if(it == "afterCommit") error("injected") }.save(input)
        assertEquals(WorkspaceSaveResult.Failed(input.id, true), result)
        val recovered = saved(input)
        assertEquals(1, recovered.revision); assertEquals(1, db.workspaceDao().all().size)
    }
    @Test fun staleEditCannotOverwriteLaterContent() = runBlocking {
        val original = saved(); saved(editing(original, "first edit"))
        assertEquals(WorkspaceSaveResult.Rejected(WorkspaceRejection.STALE_EDIT), repository.save(editing(original, "stale edit")))
        assertEquals("first edit", db.workspaceDao().byId(original.id)!!.body)
    }
    @Test fun restoredSameRevisionDifferentContentRejectsOldDraft() = runBlocking {
        val original = saved(); val oldDraft = editing(original, "unsaved")
        val json = JSONObject(backup.export())
        json.getJSONArray("workspaceRecords").getJSONObject(0).put("body", "restored content")
        backup.restore(json.toString())
        assertEquals(WorkspaceSaveResult.Rejected(WorkspaceRejection.STALE_EDIT), repository.save(oldDraft))
        assertEquals("restored content", db.workspaceDao().byId(original.id)!!.body)
    }
    @Test fun clockRollbackCannotRewriteLaterRecord() = runBlocking {
        val original = saved()
        assertEquals(WorkspaceSaveResult.Rejected(WorkspaceRejection.CLOCK_MOVED_BACKWARD),
            WorkspaceRepository(db, clockOn("2026-09-12")).save(editing(original, "new")))
    }
    @Test fun invalidAndOversizedInputIsRejectedWithoutTruncation() = runBlocking {
        for(input in listOf(draft(" "), draft().copy(kind = "DIAGNOSIS"), draft().copy(id = "bad"),
            draft().copy(title = "a".repeat(201)), draft("🙂".repeat(20001)))) {
            assertEquals(WorkspaceSaveResult.Rejected(WorkspaceRejection.INVALID_INPUT), repository.save(input))
        }
        assertTrue(db.workspaceDao().all().isEmpty())
        assertTrue(repository.save(draft("🙂".repeat(20000))) is WorkspaceSaveResult.Saved)
    }
    @Test fun backupTwoRoundTripPreservesAllSixTables() = runBlocking {
        seedAll(); val before = backup.snapshot(); val text = backup.export()
        assertEquals("BACKUP_3.1", JSONObject(text).getString("exportFormatVersion"))
        assertEquals(before, backup.preview(text)); saved(draft("later")); backup.restore(text)
        assertEquals(before, backup.snapshot())
    }
    @Test fun legacyFormatsRestoreWorkspaceToEmptyAndKeepDiary() = runBlocking {
        for(v in listOf("v1", "v1.1")) {
            saved(); val text = File("../samples/backup-$v.example.json").readText()
            assertTrue(backup.preview(text).workspaceRecords.isEmpty())
            backup.restore(text)
            assertTrue(db.workspaceDao().all().isEmpty()); assertTrue(db.diaryDao().all().isNotEmpty())
        }
    }
    @Test fun replaceFailureAtEveryCheckpointRollsBackAllSixTables() = runBlocking {
        seedAll(); val before = backup.snapshot()
        for(v in listOf("v1", "v1.1", "v2")) for(point in listOf("afterDelete", "afterEntries", "afterWorkspace", "afterState")) {
            val failing = BackupRepository(db, context) { if(it == point) error("injected") }
            assertTrue(runCatching { failing.restore(File("../samples/backup-$v.example.json").readText()) }.isFailure)
            assertEquals(before, backup.snapshot())
        }
    }
    @Test fun malformedNewDataNeverMutatesExistingDatabase() = runBlocking {
        seedAll(); val before = backup.snapshot(); val text = backup.export()
        val changes: List<(JSONObject) -> Unit> = listOf(
            { it.put("kind", "UNKNOWN") }, { it.put("body", " ") }, { it.put("title", "x".repeat(201)) },
            { it.put("workspaceSchemaVersion", "WORKSPACE_99") }, { it.put("revision", 0) },
            { it.put("revision", 2147483648L) }, { it.put("unexpected", true) }, { it.put("createdAtUtc", "invalid") })
        changes.forEach { change ->
            val bad = JSONObject(text); change(bad.getJSONArray("workspaceRecords").getJSONObject(0))
            assertTrue(runCatching { backup.restore(bad.toString()) }.isFailure); assertEquals(before, backup.snapshot())
        }
    }
    @Test fun duplicateIdsAndMissingCollectionAreRejected() = runBlocking {
        saved(); val text = backup.export(); val before = backup.snapshot()
        val duplicate = JSONObject(text); val items = duplicate.getJSONArray("workspaceRecords"); items.put(items.getJSONObject(0))
        val missing = JSONObject(text).apply { remove("workspaceRecords") }
        listOf(duplicate, missing).forEach { assertTrue(runCatching { backup.restore(it.toString()) }.isFailure); assertEquals(before, backup.snapshot()) }
    }
    @Test fun currentExampleValidatesAndCsvDoesNotIncludeWorkspaceText() = runBlocking {
        assertEquals(1, backup.preview(File("../samples/backup-v2.example.json").readText()).workspaceRecords.size)
        saved(draft("workspace-only-private"))
        assertFalse(backup.csv().toString(Charsets.UTF_8).contains("workspace-only-private"))
    }
}
