package app.sleepdiary.offline.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.sleepdiary.offline.data.backup.BackupRepository
import app.sleepdiary.offline.data.backup.BackupSchemaValidator
import app.sleepdiary.offline.data.backup.EducationExportCodec
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.education.EducationRegistryLoader
import app.sleepdiary.offline.data.entity.*
import app.sleepdiary.offline.data.repository.EducationRepository
import app.sleepdiary.offline.domain.education.EducationCompatibilityException
import app.sleepdiary.offline.domain.education.EducationCompatibilityProblem
import app.sleepdiary.offline.domain.model.*
import app.sleepdiary.offline.diary
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class EducationPersistenceTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private lateinit var db: SleepDiaryDatabase
    private lateinit var index: app.sleepdiary.offline.domain.education.EducationContentIndex
    private lateinit var repository: EducationRepository
    private lateinit var manifest: JSONObject

    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(context, SleepDiaryDatabase::class.java).build()
        index = EducationRegistryLoader.fromAssets(context.assets)
        repository = EducationRepository(db, index, Clock.fixed(Instant.parse("2026-10-03T04:00:00Z"), ZoneOffset.UTC))
        manifest = context.assets.open("education/public_education_manifest_1_1.json").bufferedReader().use { JSONObject(it.readText()) }
    }

    @After fun close() = db.close()

    private fun nodes() = (0 until manifest.getJSONArray("contentNodes").length()).map { manifest.getJSONArray("contentNodes").getJSONObject(it) }
    private fun node(kind: String) = nodes().first { it.getString("kind") == kind }
    private fun choice(): Triple<String, String, String> {
        val n = node("CHOICE")
        return Triple(n.getString("parentContentId"), n.getString("contentId"), n.getJSONObject("payload").getString("choiceId"))
    }
    private fun courseFor(sceneId: String): String {
        val nodes = nodes().associateBy { it.getString("contentId") }
        var parent = nodes.getValue(sceneId).optString("parentContentId").takeIf { it.isNotEmpty() }
        while (parent != null) {
            val n = nodes.getValue(parent)
            if (n.getString("kind") == "COURSE") return n.getString("contentId")
            parent = n.optString("parentContentId").takeIf { it.isNotEmpty() }
        }
        error("Scene has no course")
    }

    @Test fun previewIsTransientAndOnlyExplicitCommitPersistsOneChoice() = runBlocking {
        val (prompt, choice, option) = choice()
        val before = repository.snapshot()
        val preview = repository.previewChoice(manifest.getString("contentVersionId"), prompt, choice, option)
        assertEquals(before, repository.snapshot())
        val committed = repository.commitChoice(preview)
        assertEquals(choice, committed.choiceContentId)
        assertEquals(1, db.educationDao().choices().size)
        repository.commitChoice(preview)
        assertEquals(1, db.educationDao().choices().size)
        assertEquals(committed.committedAtUtc, db.educationDao().choices().single().committedAtUtc)
    }

    @Test fun progressRequiresExplicitMilestoneAndSupportsReopen() = runBlocking {
        val content = node("SCENE").getString("contentId")
        assertTrue(db.educationDao().progress().isEmpty())
        val inProgress = repository.markInProgress(manifest.getString("contentVersionId"), content)
        assertEquals("IN_PROGRESS", inProgress.state)
        val completed = repository.completeContent(inProgress.contentVersionId, content)
        assertEquals("COMPLETED", completed.state)
        assertNotNull(completed.completedAtUtc)
        assertEquals("IN_PROGRESS", repository.reopen(completed.contentVersionId, content).state)
        assertNull(db.educationDao().progress().single().completedAtUtc)
    }

    @Test fun reviewStateIsUserControlledAndPositionIsNotProgress() = runBlocking {
        val scene = node("SCENE").getString("contentId")
        val version = manifest.getString("contentVersionId")
        repository.saveForReview(version, scene)
        assertEquals("SAVED_FOR_REVIEW", db.educationDao().reviewStates().single().reviewState)
        repository.markReviewed(version, scene)
        assertEquals("REVIEWED", db.educationDao().reviewStates().single().reviewState)
        repository.updatePosition(version, courseFor(scene), scene)
        assertEquals(scene, db.educationDao().positions().single().sceneContentId)
        assertTrue(db.educationDao().progress().isEmpty())
        repository.removeReviewState(version, scene)
        assertTrue(db.educationDao().reviewStates().isEmpty())
    }

    @Test fun educationRecordAndWorkspaceTextAreOneToOneAndTransactional() = runBlocking {
        val version = manifest.getString("contentVersionId")
        val prompt = node("NOTE_PROMPT")
        val sceneId = prompt.getString("parentContentId")
        val id = UUID.randomUUID().toString()
        val now = "2026-10-03T04:00:00Z"
        val workspace = WorkspaceRecord(id, "LEARNING_NOTE", "My learning note", "User-authored text", now, now)
        val link = EducationRecord(id, version, sceneId, prompt.getString("contentId"), "NOTE", now)
        assertEquals(link, repository.saveAuthoredRecord(workspace, link))
        assertEquals(workspace, db.workspaceDao().byId(id))
        assertEquals(link, db.educationDao().record(id))
        assertTrue(runCatching { repository.saveAuthoredRecord(workspace.copy(id=UUID.randomUUID().toString()), link.copy(workspaceRecordId=UUID.randomUUID().toString())) }.isFailure)
        assertTrue(repository.unlinkAuthoredRecord(id))
        assertNull(db.educationDao().record(id))
        assertEquals("User-authored text", db.workspaceDao().byId(id)!!.body)
        val linkedAgain = link.copy(createdAtUtc=now)
        assertEquals(linkedAgain, repository.associateExistingRecord(id, linkedAgain))
        assertTrue(repository.deleteAuthoredRecord(id))
        assertNull(db.workspaceDao().byId(id))
        assertNull(db.educationDao().record(id))
    }

    @Test fun legacyWorkspaceRecordsRemainUnassociatedUntilExplicitAssociation() = runBlocking {
        val now = "2026-10-03T04:00:00Z"
        val value = WorkspaceRecord(UUID.randomUUID().toString(), "LEARNING_NOTE", "General", "Existing note", now, now)
        db.workspaceDao().insert(value)
        assertTrue(db.educationDao().records().isEmpty())
        val prompt = node("NOTE_PROMPT")
        val link = EducationRecord(value.id, manifest.getString("contentVersionId"), prompt.getString("parentContentId"), prompt.getString("contentId"), "NOTE", now)
        assertEquals(link, repository.associateExistingRecord(value.id, link))
        assertEquals(1, db.educationDao().records().size)
        repository.unlinkAuthoredRecord(value.id)
        assertEquals(value, db.workspaceDao().byId(value.id))
        assertTrue(db.educationDao().records().isEmpty())
    }

    @Test fun workspaceAndEducationAssociationWriteRollBackTogether() = runBlocking {
        val prompt=node("NOTE_PROMPT");val id=UUID.randomUUID().toString();val now="2026-10-03T04:00:00Z"
        val workspace=WorkspaceRecord(id,"LEARNING_NOTE","Rollback","text",now,now)
        val link=EducationRecord(id,manifest.getString("contentVersionId"),prompt.getString("parentContentId"),prompt.getString("contentId"),"NOTE",now)
        val failing=EducationRepository(db,index,Clock.fixed(Instant.parse(now),ZoneOffset.UTC)){if(it=="afterWorkspaceWrite")error("injected")}
        assertTrue(runCatching{failing.saveAuthoredRecord(workspace,link)}.isFailure)
        assertNull(db.workspaceDao().byId(id));assertNull(db.educationDao().record(id))
    }

    @Test fun backup31RoundTripAndLegacyFormatsRestoreEducationEmpty() = runBlocking {
        val version = manifest.getString("contentVersionId");val scene=node("SCENE").getString("contentId")
        repository.markInProgress(version, scene)
        val (prompt, choice, option) = choice();repository.commitChoice(repository.previewChoice(version,prompt,choice,option))
        repository.saveForReview(version, scene);repository.updatePosition(version, courseFor(scene), scene)
        val id=UUID.randomUUID().toString();val now="2026-10-03T04:00:00Z";val p=node("NOTE_PROMPT")
        repository.saveAuthoredRecord(WorkspaceRecord(id,"LEARNING_NOTE","Private note","private text",now,now),EducationRecord(id,version,p.getString("parentContentId"),p.getString("contentId"),"NOTE",now))
        db.stateAndSummaryDao().putState(AppState(trackingStartDate="2026-10-03"));db.diaryDao().insert(diary("2026-10-03"))
        val backup=BackupRepository(db,context);val text=backup.export();assertEquals("BACKUP_3.1",JSONObject(text).getString("exportFormatVersion"))
        assertEquals("BACKUP_3.1",backup.preview(java.io.File("../samples/backup-v3.1.example.json").readText()).sourceBackupFormatVersion)
        assertEquals(1,backup.preview(text).educationProgress.size);assertEquals(1,backup.preview(text).educationRecords.size)
        val expected=backup.snapshot();db.educationDao().clearProgress();backup.restore(text);assertEquals(expected,backup.snapshot())
        for (versionName in listOf("v1","v1.1","v2","v2.1","v3")) {
            val legacy=java.io.File("../samples/backup-$versionName.example.json").readText()
            val decoded=backup.preview(legacy)
            assertTrue(versionName,decoded.educationProgress.isEmpty()&&decoded.educationCommittedChoices.isEmpty()&&decoded.educationReviewStates.isEmpty()&&decoded.educationRecords.isEmpty()&&decoded.educationPositions.isEmpty())
        }
    }

    @Test fun unknownOrMismatchedContentDescriptorRejectedBeforeRestoreMutation() = runBlocking {
        db.diaryDao().insert(diary("2026-10-03"));db.stateAndSummaryDao().putState(AppState(trackingStartDate="2026-10-03"))
        val backup=BackupRepository(db,context);val valid=JSONObject(backup.export());val before=backup.snapshot()
        val unknown=JSONObject(valid.toString());unknown.getJSONArray("educationContentVersions").put(JSONObject().put("contentVersionId","FUTURE_EDUCATION_9").put("manifestHashSha256","0".repeat(64)))
        val unknownError=runCatching{backup.restore(unknown.toString())}.exceptionOrNull() as EducationCompatibilityException
        assertEquals(EducationCompatibilityProblem.APP_UPDATE_REQUIRED,unknownError.problem);assertEquals(before,backup.snapshot())
        val mismatched=JSONObject(valid.toString());mismatched.getJSONArray("educationContentVersions").put(JSONObject().put("contentVersionId",manifest.getString("contentVersionId")).put("manifestHashSha256","0".repeat(64)))
        val mismatchError=runCatching{backup.restore(mismatched.toString())}.exceptionOrNull() as EducationCompatibilityException
        assertEquals(EducationCompatibilityProblem.CONTENT_VERSION_INCOMPATIBLE,mismatchError.problem);assertEquals(before,backup.snapshot())
    }

    @Test fun danglingWorkspaceAssociationRejectedBeforeRestoreMutation() = runBlocking {
        val version=manifest.getString("contentVersionId");val now="2026-10-03T04:00:00Z";val p=node("NOTE_PROMPT");val id=UUID.randomUUID().toString()
        repository.saveAuthoredRecord(WorkspaceRecord(id,"LEARNING_NOTE","Private","text",now,now),EducationRecord(id,version,p.getString("parentContentId"),p.getString("contentId"),"NOTE",now))
        val backup=BackupRepository(db,context);val value=JSONObject(backup.export());val before=backup.snapshot()
        value.getJSONArray("educationRecords").getJSONObject(0).put("workspaceRecordId",UUID.randomUUID().toString())
        assertTrue(runCatching{backup.restore(value.toString())}.isFailure)
        assertEquals(before,backup.snapshot())
    }

    @Test fun restoreFailureRollsBackAllEducationRowsAndWorkspaceLinks() = runBlocking {
        val version=manifest.getString("contentVersionId");val scene=node("SCENE").getString("contentId");repository.markInProgress(version,scene)
        val backup=BackupRepository(db,context);val replacement=backup.export();val before=backup.snapshot()
        val failure=BackupRepository(db,context){if(it=="afterEducation")error("injected restore failure")}
        assertTrue(runCatching{failure.restore(replacement)}.isFailure)
        assertEquals(before,backup.snapshot())
    }

    @Test fun educationExportIsDeterministicAndOmitsPositionAndOtherDomains() = runBlocking {
        val version=manifest.getString("contentVersionId");val scene=node("SCENE").getString("contentId");repository.markInProgress(version,scene)
        repository.updatePosition(version,courseFor(scene),scene)
        val id=UUID.randomUUID().toString();val now="2026-10-03T04:00:00Z";val p=node("NOTE_PROMPT")
        repository.saveAuthoredRecord(WorkspaceRecord(id,"LEARNING_NOTE","User title","Only user text",now,now),EducationRecord(id,version,p.getString("parentContentId"),p.getString("contentId"),"NOTE",now))
        val json=repository.exportJson();assertEquals("EDUCATION_EXPORT_1.0",JSONObject(json).getString("exportFormatVersion"));assertTrue(json.contains("Only user text"));assertFalse(json.contains("educationPositions"));assertFalse(json.contains("diaryEntries"));assertFalse(json.contains("treatmentEpisodes"));assertFalse(json.contains("Safety"))
        val schema=context.assets.open("education-export-v1.schema.json").bufferedReader().use{JSONObject(it.readText())}
        BackupSchemaValidator.validate(JSONObject(json),schema)
        val text=repository.exportText();assertTrue(text.contains("Only user text"));assertFalse(text.contains("position"));assertEquals(text,repository.exportText())
    }

    @Test fun educationWritesLeaveDiarySafetyAndTreatmentRowsUnchanged() = runBlocking {
        val id=UUID.randomUUID().toString();val now="2026-10-03T04:00:00Z"
        db.diaryDao().insert(diary("2026-10-03"))
        val assessment=SafetyAssessment(id,now,Versions.SAFETY_SCHEMA,SafetyGate.NO_MAJOR_FLAG,Versions.SAFETY_RULE,AssessmentStatus.DRAFT,true)
        db.safetyDao().insertAssessment(assessment);db.safetyDao().insertAnswers(listOf(SafetyAnswer(id,"S03","NO",Versions.SAFETY_SCHEMA,null)))
        val episodeId=UUID.randomUUID().toString();db.treatmentDao().insertEpisode(TreatmentEpisode(episodeId,"NON_PRESCRIPTIVE","BASELINE_COLLECTING",adultScopeConfirmed=false,startedAtUtc=now,createdAtUtc=now,updatedAtUtc=now))
        val beforeDiary=db.diaryDao().all();val beforeSafety=db.safetyDao().all();val beforeTreatment=db.treatmentDao().episodes();val beforeAudit=db.treatmentDao().audits()
        val version=manifest.getString("contentVersionId");val scene=node("SCENE").getString("contentId");repository.completeContent(version,scene);repository.updatePosition(version,courseFor(scene),scene);repository.saveForReview(version,scene)
        val (prompt,choice,option)=choice();repository.commitChoice(repository.previewChoice(version,prompt,choice,option))
        val notePrompt=node("NOTE_PROMPT");val noteId=UUID.randomUUID().toString();repository.saveAuthoredRecord(WorkspaceRecord(noteId,"LEARNING_NOTE","Education note","User text",now,now),EducationRecord(noteId,version,notePrompt.getString("parentContentId"),notePrompt.getString("contentId"),"NOTE",now))
        assertEquals(beforeDiary,db.diaryDao().all());assertEquals(beforeSafety,db.safetyDao().all());assertEquals(beforeTreatment,db.treatmentDao().episodes());assertEquals(beforeAudit,db.treatmentDao().audits())
    }

    @Test fun exportJsonDoesNotMirrorPreviewState() {
        val (prompt, choice, option)=choice();val preview=repository.previewChoice(manifest.getString("contentVersionId"),prompt,choice,option)
        assertNotNull(preview);assertTrue(EducationExportCodec.json(runBlocking{repository.exportProjection()}).contains("EDUCATION_EXPORT_1.0"))
        assertTrue(runBlocking{repository.snapshot()}.committedChoices.isEmpty())
    }
}
