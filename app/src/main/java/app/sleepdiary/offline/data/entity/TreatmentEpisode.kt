package app.sleepdiary.offline.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.sleepdiary.offline.domain.model.Versions

@Entity(tableName = "treatment_episode")
data class TreatmentEpisode(
    @PrimaryKey val id: String,
    val mode: String,
    val state: String,
    val pausedFromState: String? = null,
    val adultScopeConfirmed: Boolean,
    val protocolVersion: String = Versions.CBTI_PROTOCOL,
    val metricsVersion: String = "NONE",
    val treatmentSafetyRuleVersion: String = "NONE",
    val exceptionRuleVersion: String = "NONE",
    val currentPlanId: String? = null,
    val startedAtUtc: String,
    val createdAtUtc: String,
    val updatedAtUtc: String,
    val completedAtUtc: String? = null,
    val revision: Int = 1,
    val treatmentDataSchemaVersion: String = Versions.TREATMENT_DATA_SCHEMA,
)
