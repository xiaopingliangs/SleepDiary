package app.sleepdiary.offline.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.sleepdiary.offline.domain.model.Versions

/** User-authored text, never a diagnosis, verified directive or executable treatment plan. */
@Entity(tableName = "workspace_record")
data class WorkspaceRecord(
    @PrimaryKey val id: String,
    val kind: String,
    val title: String,
    val body: String,
    val createdAtUtc: String,
    val updatedAtUtc: String,
    val revision: Int = 1,
    val workspaceSchemaVersion: String = Versions.WORKSPACE_SCHEMA
)
