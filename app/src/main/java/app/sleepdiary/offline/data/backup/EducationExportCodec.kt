package app.sleepdiary.offline.data.backup

import app.sleepdiary.offline.data.entity.*
import app.sleepdiary.offline.domain.education.EducationVersionDescriptor
import app.sleepdiary.offline.domain.model.Versions
import org.json.JSONArray

data class EducationExportRecord(val education: EducationRecord, val workspace: WorkspaceRecord)

data class EducationExport(
    val educationDataSchemaVersion: String,
    val contentVersions: List<EducationVersionDescriptor>,
    val progress: List<EducationProgress>,
    val committedChoices: List<EducationCommittedChoice>,
    val reviewStates: List<EducationReviewState>,
    val records: List<EducationExportRecord>
)

object EducationExportCodec {
    fun json(data: EducationExport): String = obj(
        "exportFormatVersion" to Versions.EDUCATION_EXPORT,
        "educationDataSchemaVersion" to data.educationDataSchemaVersion,
        "educationContentVersions" to JSONArray(data.contentVersions.sortedBy { it.contentVersionId }.map(BackupCodec::educationVersionJson)),
        "progress" to JSONArray(data.progress.sortedWith(compareBy({ it.contentVersionId }, { it.contentId })).map(BackupCodec::educationProgressJson)),
        "committedChoices" to JSONArray(data.committedChoices.sortedWith(compareBy({ it.contentVersionId }, { it.promptContentId })).map(BackupCodec::educationChoiceJson)),
        "reviewStates" to JSONArray(data.reviewStates.sortedWith(compareBy({ it.contentVersionId }, { it.contentId })).map(BackupCodec::educationReviewJson)),
        "records" to JSONArray(data.records.sortedWith(compareBy({ it.education.contentVersionId }, { it.education.contentId }, { it.education.workspaceRecordId })).map {
            obj("workspaceRecordId" to it.education.workspaceRecordId,
                "contentVersionId" to it.education.contentVersionId,
                "contentId" to it.education.contentId,
                "promptId" to it.education.promptId,
                "recordKind" to it.education.recordKind,
                "createdAtUtc" to it.education.createdAtUtc,
                "educationDataSchemaVersion" to it.education.educationDataSchemaVersion,
                "title" to it.workspace.title,
                "body" to it.workspace.body,
                "workspaceRevision" to it.workspace.revision,
                "workspaceUpdatedAtUtc" to it.workspace.updatedAtUtc)
        })
    ).toString(2)

    fun text(data: EducationExport): String = buildString {
        fun line(value: String = "") { append(value).append('\n') }
        line("SleepDiary Education export")
        line("教育数据版本：${data.educationDataSchemaVersion}")
        line("内容版本：${data.contentVersions.joinToString { it.contentVersionId }}")
        line()
        line("学习进度")
        data.progress.sortedWith(compareBy({ it.contentVersionId }, { it.contentId })).forEach {
            line("${it.contentVersionId} / ${it.contentId}: ${it.state} (${it.updatedAtUtc})")
        }
        line()
        line("已确认的学习选择")
        data.committedChoices.sortedWith(compareBy({ it.contentVersionId }, { it.promptContentId })).forEach {
            line("${it.contentVersionId} / ${it.promptContentId}: ${it.optionId} [${it.choiceContentId}]")
        }
        line()
        line("稍后复习")
        data.reviewStates.sortedWith(compareBy({ it.contentVersionId }, { it.contentId })).forEach {
            line("${it.contentVersionId} / ${it.contentId}: ${it.reviewState}")
        }
        line()
        line("个人记录")
        data.records.sortedWith(compareBy({ it.education.contentVersionId }, { it.education.contentId }, { it.education.workspaceRecordId })).forEach {
            line("${it.education.recordKind} — ${it.workspace.title}")
            line(it.workspace.body.replace("\r\n", "\n").replace('\r', '\n'))
            line()
        }
    }
}
