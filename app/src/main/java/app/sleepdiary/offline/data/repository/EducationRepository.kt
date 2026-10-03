package app.sleepdiary.offline.data.repository

import androidx.room.withTransaction
import app.sleepdiary.offline.data.backup.EducationExport
import app.sleepdiary.offline.data.backup.EducationExportCodec
import app.sleepdiary.offline.data.backup.EducationExportRecord
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.entity.*
import app.sleepdiary.offline.domain.education.EducationContentIndex
import app.sleepdiary.offline.domain.education.EducationVersionDescriptor
import app.sleepdiary.offline.domain.model.Versions
import kotlinx.coroutines.CancellationException
import java.time.Clock
import java.time.Instant

data class EducationChoicePreview(
    val contentVersionId: String,
    val promptContentId: String,
    val choiceContentId: String,
    val optionId: String
)

data class EducationUserDataSnapshot(
    val contentVersions: List<EducationVersionDescriptor>,
    val progress: List<EducationProgress>,
    val committedChoices: List<EducationCommittedChoice>,
    val reviewStates: List<EducationReviewState>,
    val records: List<EducationRecord>,
    val positions: List<EducationPosition>
)

class EducationRepository(
    private val db: SleepDiaryDatabase,
    private val index: EducationContentIndex,
    private val clock: Clock = Clock.systemUTC(),
    private val checkpoint: (String) -> Unit = {}
) {
    private fun now() = Instant.now(clock).toString()
    private fun stamp(value: String) = Instant.parse(value)
    private fun base(version: String) {
        index.descriptor(version)
    }
    private fun recordKind(kind: String) {
        require(kind in setOf("NOTE", "REFLECTION", "QUESTION_FOR_CLINICIAN", "PERSONAL_REMINDER"))
    }
    private fun workspaceKind(kind: String) = when (kind) {
        "NOTE" -> "LEARNING_NOTE"
        "REFLECTION" -> "REFLECTION"
        "QUESTION_FOR_CLINICIAN" -> "CLINICIAN_NOTE"
        "PERSONAL_REMINDER" -> "CONTEXT_NOTE"
        else -> error("Unsupported Education record kind")
    }

    suspend fun snapshot(): EducationUserDataSnapshot = db.withTransaction {
        val dao = db.educationDao()
        val progress = dao.progress()
        val choices = dao.choices()
        val review = dao.reviewStates()
        val records = dao.records()
        val positions = dao.positions()
        val ids = (progress.map { it.contentVersionId } + choices.map { it.contentVersionId } +
            review.map { it.contentVersionId } + records.map { it.contentVersionId } +
            positions.map { it.contentVersionId }).toSet()
        EducationUserDataSnapshot(index.versions(ids), progress, choices, review, records, positions)
    }

    suspend fun exportProjection(): EducationExport = db.withTransaction {
        val dao = db.educationDao()
        val progress = dao.progress(); val choices = dao.choices(); val review = dao.reviewStates(); val records = dao.records()
        val ids = (progress.map { it.contentVersionId } + choices.map { it.contentVersionId } +
            review.map { it.contentVersionId } + records.map { it.contentVersionId }).toSet()
        val joined = records.map { association ->
            EducationExportRecord(association, checkNotNull(db.workspaceDao().byId(association.workspaceRecordId)))
        }
        EducationExport(Versions.EDUCATION_DATA_SCHEMA, index.versions(ids), progress, choices, review, joined)
    }

    suspend fun exportJson(): String = EducationExportCodec.json(exportProjection())
    suspend fun exportText(): String = EducationExportCodec.text(exportProjection())

    /** Explicit user intent only; opening or viewing content must never call this method. */
    suspend fun markInProgress(version: String, contentId: String): EducationProgress = db.withTransaction {
        base(version); index.node(version, contentId)
        val dao = db.educationDao()
        val old = dao.progress(version, contentId)
        val value = EducationProgress(version, contentId, "IN_PROGRESS", now(), null)
        check(old?.state != "COMPLETED") { "Reopen completed content explicitly before marking in progress" }
        dao.putProgress(value)
        check(dao.progress(version, contentId) == value)
        value
    }

    /** Call only from a clearly labeled explicit learning milestone action. */
    suspend fun completeContent(version: String, contentId: String): EducationProgress = db.withTransaction {
        base(version); index.node(version, contentId)
        val dao = db.educationDao(); val current = now()
        val old = dao.progress(version, contentId)
        val value = EducationProgress(version, contentId, "COMPLETED", old?.updatedAtUtc?.let {
            if (stamp(it) > stamp(current)) it else current
        } ?: current, current)
        dao.putProgress(value)
        check(dao.progress(version, contentId) == value)
        value
    }

    suspend fun reopen(version: String, contentId: String): EducationProgress = db.withTransaction {
        base(version); index.node(version, contentId)
        val dao = db.educationDao(); require(dao.progress(version, contentId)?.state == "COMPLETED")
        val value = EducationProgress(version, contentId, "IN_PROGRESS", now(), null)
        dao.putProgress(value); check(dao.progress(version, contentId) == value); value
    }

    suspend fun clearProgress() = db.withTransaction { db.educationDao().clearProgress() }

    /** Preview is an immutable value only; this method performs no database operation. */
    fun previewChoice(version: String, promptId: String, choiceId: String, optionId: String): EducationChoicePreview {
        base(version); index.validateChoice(version, promptId, choiceId, optionId)
        return EducationChoicePreview(version, promptId, choiceId, optionId)
    }

    /** Explicit confirmation boundary. A Preview object alone is never persisted. */
    suspend fun commitChoice(preview: EducationChoicePreview): EducationCommittedChoice = db.withTransaction {
        index.validateChoice(preview.contentVersionId, preview.promptContentId, preview.choiceContentId, preview.optionId)
        val dao = db.educationDao(); val current = now(); val old = dao.choice(preview.contentVersionId, preview.promptContentId)
        val value = EducationCommittedChoice(preview.contentVersionId, preview.promptContentId,
            preview.choiceContentId, preview.optionId, old?.committedAtUtc ?: current, current)
        dao.putChoice(value); check(dao.choice(value.contentVersionId, value.promptContentId) == value); value
    }

    suspend fun clearChoice(version: String, promptId: String) = db.withTransaction {
        base(version); index.node(version, promptId); db.educationDao().clearChoice(version, promptId)
    }

    suspend fun saveForReview(version: String, contentId: String): EducationReviewState = setReview(version, contentId, "SAVED_FOR_REVIEW")
    suspend fun markReviewed(version: String, contentId: String): EducationReviewState = setReview(version, contentId, "REVIEWED")

    private suspend fun setReview(version: String, contentId: String, state: String): EducationReviewState = db.withTransaction {
        base(version); index.node(version, contentId)
        val dao = db.educationDao(); val old = dao.reviewState(version, contentId); val current = now()
        val value = EducationReviewState(version, contentId, state, old?.createdAtUtc ?: current, current,
            if (state == "REVIEWED") current else null)
        dao.putReviewState(value); check(dao.reviewState(version, contentId) == value); value
    }

    suspend fun removeReviewState(version: String, contentId: String) = db.withTransaction {
        base(version); index.node(version, contentId); db.educationDao().clearReviewState(version, contentId)
    }

    suspend fun updatePosition(version: String, courseId: String, sceneId: String): EducationPosition = db.withTransaction {
        base(version); index.validatePosition(version, courseId, sceneId)
        val value = EducationPosition(version, courseId, sceneId, now())
        val dao = db.educationDao(); dao.putPosition(value); check(dao.position(version, courseId) == value); value
    }

    suspend fun clearPosition(version: String, courseId: String) = db.withTransaction {
        base(version); db.educationDao().clearPosition(version, courseId)
    }

    /** Saves the user text and its one-to-one Education association atomically. */
    suspend fun saveAuthoredRecord(workspace: WorkspaceRecord, education: EducationRecord): EducationRecord {
        require(education.workspaceRecordId == workspace.id)
        recordKind(education.recordKind)
        require(workspace.kind == workspaceKind(education.recordKind))
        base(education.contentVersionId); index.node(education.contentVersionId, education.contentId)
        education.promptId?.let { prompt ->
            val promptNode = index.node(education.contentVersionId, prompt)
            require(promptNode.kind == "NOTE_PROMPT" || promptNode.kind == "REFLECTION")
        }
        WorkspaceValidation.record(workspace)
        require(education.educationDataSchemaVersion == Versions.EDUCATION_DATA_SCHEMA)
        stamp(education.createdAtUtc)
        return try {
            db.withTransaction {
                val wdao = db.workspaceDao(); val edao = db.educationDao()
                val existingAssociation = edao.record(workspace.id)
                val oldWorkspace = wdao.byId(workspace.id)
                if (oldWorkspace == null) wdao.insert(workspace)
                else {
                    require(existingAssociation != null) { "Existing general WorkspaceRecord cannot be implicitly claimed" }
                    check(wdao.update(workspace) == 1)
                }
                checkpoint("afterWorkspaceWrite")
                if (existingAssociation == null) edao.insertRecord(education)
                else edao.putRecord(education.copy(createdAtUtc = existingAssociation.createdAtUtc))
                checkpoint("afterEducationLink")
                val persistedLink = checkNotNull(edao.record(workspace.id))
                check(wdao.byId(workspace.id) == workspace && persistedLink.workspaceRecordId == workspace.id)
                persistedLink
            }
        } catch (c: CancellationException) { throw c }
    }

    /** Unlinks Education while preserving the general WorkspaceRecord body. */
    suspend fun unlinkAuthoredRecord(workspaceRecordId: String): Boolean = db.withTransaction {
        db.educationDao().unlinkRecord(workspaceRecordId) == 1
    }

    /** Explicitly associates a selected existing WorkspaceRecord; no association is inferred. */
    suspend fun associateExistingRecord(workspaceRecordId: String, education: EducationRecord): EducationRecord = db.withTransaction {
        require(education.workspaceRecordId == workspaceRecordId)
        recordKind(education.recordKind); base(education.contentVersionId)
        index.node(education.contentVersionId, education.contentId)
        education.promptId?.let { prompt -> require(index.node(education.contentVersionId, prompt).kind in setOf("NOTE_PROMPT", "REFLECTION")) }
        require(education.educationDataSchemaVersion == Versions.EDUCATION_DATA_SCHEMA); stamp(education.createdAtUtc)
        val workspace = checkNotNull(db.workspaceDao().byId(workspaceRecordId))
        WorkspaceValidation.record(workspace)
        require(workspace.kind == workspaceKind(education.recordKind))
        val dao = db.educationDao(); require(dao.record(workspaceRecordId) == null)
        dao.insertRecord(education); checkpoint("afterEducationLink")
        check(dao.record(workspaceRecordId) == education)
        education
    }

    /** Explicitly deletes both the link and the user-owned WorkspaceRecord body. */
    suspend fun deleteAuthoredRecord(workspaceRecordId: String): Boolean = db.withTransaction {
        val body = db.workspaceDao().byId(workspaceRecordId) ?: return@withTransaction false
        db.educationDao().unlinkRecord(workspaceRecordId)
        check(db.workspaceDao().deleteById(workspaceRecordId) == 1)
        check(db.educationDao().record(workspaceRecordId) == null && db.workspaceDao().byId(body.id) == null)
        true
    }
}
