package app.sleepdiary.offline.domain.treatment

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

enum class TreatmentMode { SELF_GUIDED, CLINICIAN_ASSISTED }

enum class TreatmentState {
    DIARY_ONLY, BASELINE_COLLECTING, BASELINE_REVIEW, SAFETY_REVIEW,
    READY_FOR_TREATMENT, PLAN_PENDING_CONFIRMATION, ACTIVE_TREATMENT,
    WEEKLY_REVIEW_DUE, PLAN_ADJUSTMENT_PENDING, ACTIVE_ADAPTED,
    CLINICIAN_REVIEW, USER_PAUSED, SAFETY_HOLD, MEDICAL_HOLD,
    MAINTENANCE, COMPLETED, RELAPSE_REVIEW
}

enum class TreatmentPlanOrigin { PROTOCOL, USER_CONFIRMED, USER_ENTERED_CLINICIAN_INSTRUCTION }
enum class TreatmentPlanStatus { CANDIDATE, ACTIVE, SUPERSEDED, EXPIRED, CANCELLED }

/** Immutable domain value. Persistence and activation are deliberately deferred until Phase 4. */
data class TreatmentPlan(
    val id: String,
    val episodeId: String,
    val revision: Int,
    val protocolVersion: String,
    val ruleSetVersion: String,
    val validFrom: LocalDate,
    val validUntil: LocalDate?,
    val fixedWakeTime: LocalTime,
    val tibMinutes: Int,
    val earliestBedtime: LocalTime,
    val stimulusControlRuleVersion: String,
    val mode: TreatmentMode,
    val origin: TreatmentPlanOrigin,
    val inputSnapshotId: String,
    val metricSnapshotId: String,
    val safetySnapshotId: String,
    val reason: String,
    val createdAt: Instant,
    val supersedesPlanId: String?,
    val status: TreatmentPlanStatus,
) {
    init {
        listOf(id, episodeId, inputSnapshotId, metricSnapshotId, safetySnapshotId).forEach {
            require(UUID.fromString(it).toString() == it)
        }
        supersedesPlanId?.let { require(UUID.fromString(it).toString() == it) }
        require(revision >= 1 && tibMinutes > 0 && validUntil?.let { it >= validFrom } != false)
        require(protocolVersion.isNotBlank() && protocolVersion != "NONE")
        require(ruleSetVersion.isNotBlank() && stimulusControlRuleVersion.isNotBlank() && reason.isNotBlank())
    }
}
