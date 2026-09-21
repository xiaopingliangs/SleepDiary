package app.sleepdiary.offline.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import app.sleepdiary.offline.domain.model.Versions

@Entity(
    tableName = "treatment_audit_event",
    foreignKeys = [ForeignKey(entity = TreatmentEpisode::class, parentColumns = ["id"], childColumns = ["episodeId"])],
    indices = [Index("episodeId"), Index(value = ["episodeId", "toRevision"], unique = true)],
)
data class TreatmentAuditEvent(
    @PrimaryKey val id: String,
    val episodeId: String,
    val commandType: String,
    val requestFingerprint: String,
    val fromState: String?,
    val toState: String,
    val fromRevision: Int,
    val toRevision: Int,
    val occurredAtUtc: String,
    val restoreGenerationAtCommand: String,
    val treatmentDataSchemaVersion: String = Versions.TREATMENT_DATA_SCHEMA,
)
