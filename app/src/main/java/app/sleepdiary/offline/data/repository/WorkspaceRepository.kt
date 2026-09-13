package app.sleepdiary.offline.data.repository

import androidx.room.withTransaction
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.entity.WorkspaceRecord
import app.sleepdiary.offline.domain.model.Versions
import kotlinx.coroutines.CancellationException
import java.time.Clock
import java.time.Instant
import java.util.UUID

enum class WorkspaceKind(val label: String) {
    LEARNING_NOTE("学习笔记"), REFLECTION("想法记录"), CONTEXT_NOTE("现实情境记录"), CLINICIAN_NOTE("就诊沟通记录")
}
data class WorkspaceDraft(val id: String, val kind: String, val title: String, val body: String, val baseRevision: Int = 0, val baseFingerprint: String? = null)
enum class WorkspaceRejection { INVALID_INPUT, STALE_EDIT, CLOCK_MOVED_BACKWARD }
sealed interface WorkspaceSaveResult {
    data class Saved(val record: WorkspaceRecord) : WorkspaceSaveResult
    data class Rejected(val reason: WorkspaceRejection) : WorkspaceSaveResult
    data class Failed(val id: String, val mayHaveCommitted: Boolean) : WorkspaceSaveResult
}
object WorkspaceValidation {
    fun fingerprint(r: WorkspaceRecord): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        listOf(r.id, r.kind, r.title, r.body, r.createdAtUtc, r.updatedAtUtc, r.revision.toString(), r.workspaceSchemaVersion).forEach {
            val bytes = it.toByteArray(Charsets.UTF_8)
            digest.update(java.nio.ByteBuffer.allocate(4).putInt(bytes.size).array()); digest.update(bytes)
        }
        return digest.digest().joinToString("") { (it.toInt() and 255).toString(16).padStart(2, '0') }
    }
    fun content(id: String, kind: String, title: String, body: String) {
        require(UUID.fromString(id).toString() == id)
        require(WorkspaceKind.entries.any { it.name == kind })
        require(title.codePointCount(0, title.length) <= 200)
        require(body.isNotBlank() && body.codePointCount(0, body.length) <= 20000)
    }
    fun record(r: WorkspaceRecord) {
        content(r.id, r.kind, r.title, r.body)
        require(r.workspaceSchemaVersion == Versions.WORKSPACE_SCHEMA && r.revision >= 1)
        require(Instant.parse(r.createdAtUtc) <= Instant.parse(r.updatedAtUtc))
    }
}

class WorkspaceRepository(private val db: SleepDiaryDatabase, private val clock: Clock = Clock.systemUTC(),
    private val checkpoint: (String) -> Unit = {}) {
    suspend fun all(): List<WorkspaceRecord> = db.workspaceDao().all().onEach(WorkspaceValidation::record)
        .sortedByDescending { Instant.parse(it.updatedAtUtc) }

    suspend fun save(draft: WorkspaceDraft): WorkspaceSaveResult {
        if (runCatching {
                WorkspaceValidation.content(draft.id, draft.kind, draft.title, draft.body); require(draft.baseRevision >= 0)
                require(if(draft.baseRevision == 0) draft.baseFingerprint == null else draft.baseFingerprint?.matches(Regex("[0-9a-f]{64}")) == true)
            }.isFailure)
            return WorkspaceSaveResult.Rejected(WorkspaceRejection.INVALID_INPUT)
        var committed = false
        try {
            val result = db.withTransaction {
                val dao = db.workspaceDao()
                val old = dao.byId(draft.id)
                old?.let(WorkspaceValidation::record)
                val sameContent = old?.let { it.kind == draft.kind && it.title == draft.title && it.body == draft.body } == true
                // Recover a committed save whose acknowledgement was lost without duplicating it.
                if (old != null && sameContent && (old.revision == draft.baseRevision ||
                        old.revision.toLong() == draft.baseRevision.toLong() + 1)) return@withTransaction WorkspaceSaveResult.Saved(old)
                if ((old == null && draft.baseRevision != 0) || (old != null &&
                        (old.revision != draft.baseRevision || draft.baseFingerprint != WorkspaceValidation.fingerprint(old))))
                    return@withTransaction WorkspaceSaveResult.Rejected(WorkspaceRejection.STALE_EDIT)
                val now = Instant.now(clock)
                if (old != null && now < Instant.parse(old.updatedAtUtc))
                    return@withTransaction WorkspaceSaveResult.Rejected(WorkspaceRejection.CLOCK_MOVED_BACKWARD)
                val record = WorkspaceRecord(draft.id, draft.kind, draft.title, draft.body,
                    old?.createdAtUtc ?: now.toString(), now.toString(), Math.addExact(old?.revision ?: 0, 1))
                WorkspaceValidation.record(record)
                if (old == null) dao.insert(record) else check(dao.update(record) == 1)
                checkpoint("afterWrite")
                check(dao.byId(record.id) == record) { "Workspace readback mismatch" }
                WorkspaceSaveResult.Saved(record)
            }
            if (result is WorkspaceSaveResult.Saved) {
                committed = true
                checkpoint("afterCommit")
                check(db.workspaceDao().byId(result.record.id) == result.record)
            }
            return result
        } catch (c: CancellationException) { throw c }
        catch (_: Exception) { return WorkspaceSaveResult.Failed(draft.id, committed) }
    }
}
