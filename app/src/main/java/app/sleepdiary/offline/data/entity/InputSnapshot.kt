package app.sleepdiary.offline.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Immutable historical raw inputs; no treatment authorization. id is also the capture command ID. */
@Entity(tableName = "input_snapshot")
data class InputSnapshot(
    @PrimaryKey val id: String,
    val capturedAtUtc: String,
    val fromDate: String,
    val throughDate: String,
    val captureVersion: String,
    val payloadBase64: String,
    val contentHash: String
)
