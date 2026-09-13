package app.sleepdiary.offline.domain.protocol

/** Engineering-only intersection. No medical mapping, production caller, or execution authority. */
internal object ConstraintIntersection {
    fun evaluate(knownActions: Set<String>, layers: List<ConstraintLayer>): ConstraintResult {
        if (knownActions.isEmpty() || knownActions.any { !validId(it) })
            return ConstraintResult.Invalid("ACTION_CATALOG")
        if (layers.map { it.sourceId }.distinct().size != layers.size ||
            layers.any { !validId(it.sourceId) || it.reasonRefs.any { ref -> !validId(ref) } })
            return ConstraintResult.Invalid("SOURCE_ID")
        val required = setOf(ConstraintRole.PROTOCOL, ConstraintRole.SAFETY, ConstraintRole.EXCEPTION)
        if (required.any { role -> layers.count { it.role == role } != 1 })
            return ConstraintResult.Invalid("REQUIRED_LAYERS")
        if (layers.any { it.allowedActions?.any { action -> action !in knownActions } == true })
            return ConstraintResult.Invalid("UNKNOWN_ACTION")
        val trace = layers.sortedBy { it.sourceId }.map {
            ConstraintTrace(it.sourceId, it.role, it.allowedActions?.sorted(), it.reasonRefs.sorted())
        }
        // Missing/unsupported results are not an unrestricted allow-list.
        if (layers.any { it.allowedActions == null }) return ConstraintResult.Unresolved(trace)
        val allowed = layers.fold(knownActions.toSet()) { result, layer -> result intersect checkNotNull(layer.allowedActions) }
        return if (allowed.isEmpty()) ConstraintResult.NoCompatibleAction(trace)
        else ConstraintResult.Candidates(allowed.sorted(), trace)
    }

    private fun validId(value: String) = value.isNotBlank() && value.length <= 200
}

internal enum class ConstraintRole { PROTOCOL, SAFETY, EXCEPTION, CLINICIAN_SCOPE }
internal data class ConstraintLayer(
    val sourceId: String,
    val role: ConstraintRole,
    /** null = not evaluated/unsupported; an evaluated empty set allows no action. */
    val allowedActions: Set<String>?,
    val reasonRefs: Set<String>
)
internal data class ConstraintTrace(val sourceId: String, val role: ConstraintRole,
    val allowedActions: List<String>?, val reasonRefs: List<String>)
internal sealed interface ConstraintResult {
    data class Invalid(val reason: String): ConstraintResult
    data class Unresolved(val trace: List<ConstraintTrace>): ConstraintResult
    data class NoCompatibleAction(val trace: List<ConstraintTrace>): ConstraintResult
    /** Still requires released clinical rules and atomic commit checks; never a prescription. */
    data class Candidates(val actions: List<String>, val trace: List<ConstraintTrace>): ConstraintResult
}

/** Caller must capture this in the same database transaction as a future plan commit.
 * backupEpoch distinguishes restore-to-older-values from an unchanged live database.
 * Hash covers immutable input bytes; revision alone does not identify original diary contents.
 */
internal data class ProposalWatermark(
    val backupEpoch: String,
    val stateRevision: Long,
    val safetyRevision: Long,
    val inputHash: String,
    val pinnedBundleHash: String
)
internal enum class ProposalFreshness { INVALID, STALE, UNCHANGED_NOT_AUTHORIZED }
internal object ProposalFreshnessGuard {
    fun check(prepared: ProposalWatermark, current: ProposalWatermark): ProposalFreshness {
        if (!valid(prepared) || !valid(current)) return ProposalFreshness.INVALID
        return if (prepared == current) ProposalFreshness.UNCHANGED_NOT_AUTHORIZED else ProposalFreshness.STALE
    }
    private fun valid(value: ProposalWatermark) = value.backupEpoch.isNotBlank() &&
        value.backupEpoch.length <= 200 && value.stateRevision >= 0 && value.safetyRevision >= 0 &&
        value.inputHash.matches(Regex("[0-9a-f]{64}")) && value.pinnedBundleHash.matches(Regex("[0-9a-f]{64}"))
}

