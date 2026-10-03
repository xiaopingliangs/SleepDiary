package app.sleepdiary.offline.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import app.sleepdiary.offline.domain.model.Versions

@Entity(tableName = "education_progress", primaryKeys = ["contentVersionId", "contentId"])
data class EducationProgress(
    val contentVersionId: String,
    val contentId: String,
    val state: String,
    val updatedAtUtc: String,
    val completedAtUtc: String? = null,
    val educationDataSchemaVersion: String = Versions.EDUCATION_DATA_SCHEMA
)

@Entity(tableName = "education_committed_choice", primaryKeys = ["contentVersionId", "promptContentId"])
data class EducationCommittedChoice(
    val contentVersionId: String,
    val promptContentId: String,
    val choiceContentId: String,
    val optionId: String,
    val committedAtUtc: String,
    val updatedAtUtc: String,
    val educationDataSchemaVersion: String = Versions.EDUCATION_DATA_SCHEMA
)

@Entity(tableName = "education_review_state", primaryKeys = ["contentVersionId", "contentId"])
data class EducationReviewState(
    val contentVersionId: String,
    val contentId: String,
    val reviewState: String,
    val createdAtUtc: String,
    val updatedAtUtc: String,
    val reviewedAtUtc: String? = null,
    val educationDataSchemaVersion: String = Versions.EDUCATION_DATA_SCHEMA
)

@Entity(
    tableName = "education_record",
    foreignKeys = [ForeignKey(
        entity = WorkspaceRecord::class,
        parentColumns = ["id"],
        childColumns = ["workspaceRecordId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["contentVersionId", "contentId"])]
)
data class EducationRecord(
    @PrimaryKey val workspaceRecordId: String,
    val contentVersionId: String,
    val contentId: String,
    val promptId: String? = null,
    val recordKind: String,
    val createdAtUtc: String,
    val educationDataSchemaVersion: String = Versions.EDUCATION_DATA_SCHEMA
)

@Entity(tableName = "education_position", primaryKeys = ["contentVersionId", "courseId"])
data class EducationPosition(
    val contentVersionId: String,
    val courseId: String,
    val sceneContentId: String,
    val updatedAtUtc: String,
    val educationDataSchemaVersion: String = Versions.EDUCATION_DATA_SCHEMA
)
