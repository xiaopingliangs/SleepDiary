package app.sleepdiary.offline.application

import androidx.room.withTransaction
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.entity.TreatmentAuditEvent
import app.sleepdiary.offline.data.entity.TreatmentEpisode
import app.sleepdiary.offline.data.repository.LocalExecutionRepository
import app.sleepdiary.offline.domain.model.Versions
import app.sleepdiary.offline.domain.treatment.TreatmentMode
import app.sleepdiary.offline.domain.treatment.TreatmentState
import kotlinx.coroutines.CancellationException
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.time.Clock
import java.time.Instant
import java.util.UUID

enum class TreatmentTransition {
    REQUEST_BASELINE_REVIEW, RETURN_TO_BASELINE_COLLECTION, PAUSE, RESUME,
    COMPLETE_SAFETY_REVIEW, CREATE_PLAN_CANDIDATE, CONFIRM_PLAN, BEGIN_WEEKLY_REVIEW,
    PROPOSE_PLAN_ADJUSTMENT, CONFIRM_PLAN_ADJUSTMENT, ENTER_MAINTENANCE, COMPLETE_EPISODE,
    BEGIN_RELAPSE_REVIEW
}

enum class TreatmentRejection {
    INVALID_REQUEST, NOT_ADULT_SCOPE, ACTIVE_EPISODE_EXISTS, EPISODE_NOT_FOUND,
    STALE_REVISION, INVALID_TRANSITION, MEDICAL_RULE_BLOCKED, CLOCK_MOVED_BACKWARD,
    COMMAND_CONFLICT
}

sealed interface TreatmentCommandResult {
    data class Applied(val episode: TreatmentEpisode, val audit: TreatmentAuditEvent) : TreatmentCommandResult
    data class AlreadyApplied(val audit: TreatmentAuditEvent) : TreatmentCommandResult
    data class Rejected(val reason: TreatmentRejection) : TreatmentCommandResult
    data class Failed(val commandId: String, val mayHaveCommitted: Boolean) : TreatmentCommandResult
}

object TreatmentRecordValidation {
    private val hashPattern = Regex("[0-9a-f]{64}")
    fun episode(value: TreatmentEpisode) {
        require(UUID.fromString(value.id).toString() == value.id)
        require(TreatmentMode.entries.any { it.name == value.mode })
        val state = TreatmentState.valueOf(value.state)
        require(state in setOf(TreatmentState.BASELINE_COLLECTING, TreatmentState.BASELINE_REVIEW, TreatmentState.USER_PAUSED))
        value.pausedFromState?.let { TreatmentState.valueOf(it) }
        require((state == TreatmentState.USER_PAUSED) == (value.pausedFromState != null))
        require(value.adultScopeConfirmed && value.revision >= 1)
        require(value.treatmentDataSchemaVersion == Versions.TREATMENT_DATA_SCHEMA)
        require(value.protocolVersion == "NONE" && value.metricsVersion == "NONE")
        require(value.treatmentSafetyRuleVersion == "NONE" && value.exceptionRuleVersion == "NONE")
        require(value.currentPlanId == null && value.completedAtUtc == null)
        val started = Instant.parse(value.startedAtUtc)
        val created = Instant.parse(value.createdAtUtc)
        val updated = Instant.parse(value.updatedAtUtc)
        require(started == created && created <= updated)
    }

    fun audit(value: TreatmentAuditEvent) {
        require(UUID.fromString(value.id).toString() == value.id)
        require(UUID.fromString(value.episodeId).toString() == value.episodeId)
        require(UUID.fromString(value.restoreGenerationAtCommand).toString() == value.restoreGenerationAtCommand)
        require(value.commandType.isNotBlank() && value.requestFingerprint.matches(hashPattern))
        value.fromState?.let { TreatmentState.valueOf(it) }
        TreatmentState.valueOf(value.toState)
        require(value.fromRevision >= 0 && value.toRevision == value.fromRevision + 1)
        Instant.parse(value.occurredAtUtc)
        require(value.treatmentDataSchemaVersion == Versions.TREATMENT_DATA_SCHEMA)
        require(value.commandType == "START_BASELINE" || TreatmentTransition.entries.any { it.name == value.commandType })
    }
}

class TreatmentStateController(
    private val db: SleepDiaryDatabase,
    private val clock: Clock = Clock.systemUTC(),
    private val checkpoint: (String) -> Unit = {},
) {
    suspend fun startBaseline(commandId: String, mode: TreatmentMode, adultScopeConfirmed: Boolean): TreatmentCommandResult {
        val fingerprint = fingerprint("START_BASELINE", commandId, mode.name, adultScopeConfirmed.toString())
        if (!validUuid(commandId)) return TreatmentCommandResult.Rejected(TreatmentRejection.INVALID_REQUEST)
        if (!adultScopeConfirmed) return TreatmentCommandResult.Rejected(TreatmentRejection.NOT_ADULT_SCOPE)
        return execute(commandId, fingerprint) {
            val dao = db.treatmentDao()
            previous(commandId, fingerprint)?.let { return@execute it }
            if (dao.unfinishedEpisodes().isNotEmpty()) return@execute TreatmentCommandResult.Rejected(TreatmentRejection.ACTIVE_EPISODE_EXISTS)
            val local = LocalExecutionRepository(db).current()
            val now = Instant.now(clock).toString()
            val episode = TreatmentEpisode(
                id = commandId, mode = mode.name, state = TreatmentState.BASELINE_COLLECTING.name,
                adultScopeConfirmed = true, startedAtUtc = now, createdAtUtc = now, updatedAtUtc = now,
            )
            val audit = TreatmentAuditEvent(commandId, commandId, "START_BASELINE", fingerprint, null,
                episode.state, 0, 1, now, local.restoreGeneration)
            TreatmentRecordValidation.episode(episode); TreatmentRecordValidation.audit(audit)
            dao.insertEpisode(episode); dao.insertAudit(audit); checkpoint("afterTreatmentWrite")
            check(dao.episode(commandId) == episode && dao.audit(commandId) == audit)
            TreatmentCommandResult.Applied(episode, audit)
        }
    }

    suspend fun transition(commandId: String, episodeId: String, expectedRevision: Int,
        transition: TreatmentTransition): TreatmentCommandResult {
        val fingerprint = fingerprint("TRANSITION", commandId, episodeId, expectedRevision.toString(), transition.name)
        if (!validUuid(commandId) || !validUuid(episodeId) || expectedRevision < 1)
            return TreatmentCommandResult.Rejected(TreatmentRejection.INVALID_REQUEST)
        if (transition !in setOf(TreatmentTransition.REQUEST_BASELINE_REVIEW,
                TreatmentTransition.RETURN_TO_BASELINE_COLLECTION, TreatmentTransition.PAUSE, TreatmentTransition.RESUME))
            return TreatmentCommandResult.Rejected(TreatmentRejection.MEDICAL_RULE_BLOCKED)
        return execute(commandId, fingerprint) {
            val dao = db.treatmentDao()
            previous(commandId, fingerprint)?.let { return@execute it }
            val old = dao.episode(episodeId) ?: return@execute TreatmentCommandResult.Rejected(TreatmentRejection.EPISODE_NOT_FOUND)
            TreatmentRecordValidation.episode(old)
            if (old.revision != expectedRevision) return@execute TreatmentCommandResult.Rejected(TreatmentRejection.STALE_REVISION)
            val from = TreatmentState.valueOf(old.state)
            val target = when (transition) {
                TreatmentTransition.REQUEST_BASELINE_REVIEW -> if (from == TreatmentState.BASELINE_COLLECTING) TreatmentState.BASELINE_REVIEW else null
                TreatmentTransition.RETURN_TO_BASELINE_COLLECTION -> if (from == TreatmentState.BASELINE_REVIEW) TreatmentState.BASELINE_COLLECTING else null
                TreatmentTransition.PAUSE -> if (from in setOf(TreatmentState.BASELINE_COLLECTING, TreatmentState.BASELINE_REVIEW)) TreatmentState.USER_PAUSED else null
                TreatmentTransition.RESUME -> if (from == TreatmentState.USER_PAUSED) old.pausedFromState?.let(TreatmentState::valueOf) else null
                else -> null
            } ?: return@execute TreatmentCommandResult.Rejected(TreatmentRejection.INVALID_TRANSITION)
            val now = Instant.now(clock)
            if (now < Instant.parse(old.updatedAtUtc)) return@execute TreatmentCommandResult.Rejected(TreatmentRejection.CLOCK_MOVED_BACKWARD)
            val nextRevision = Math.addExact(old.revision, 1)
            val pausedFrom = when (transition) {
                TreatmentTransition.PAUSE -> old.state
                TreatmentTransition.RESUME -> null
                else -> old.pausedFromState
            }
            check(dao.updateState(old.id, old.revision, nextRevision, target.name, pausedFrom, now.toString()) == 1)
            val next = checkNotNull(dao.episode(old.id))
            val local = LocalExecutionRepository(db).current()
            val audit = TreatmentAuditEvent(commandId, old.id, transition.name, fingerprint, old.state,
                target.name, old.revision, nextRevision, now.toString(), local.restoreGeneration)
            TreatmentRecordValidation.episode(next); TreatmentRecordValidation.audit(audit)
            dao.insertAudit(audit); checkpoint("afterTreatmentWrite")
            check(dao.episode(old.id) == next && dao.audit(commandId) == audit)
            TreatmentCommandResult.Applied(next, audit)
        }
    }

    private suspend fun previous(commandId: String, fingerprint: String): TreatmentCommandResult? {
        val old = db.treatmentDao().audit(commandId) ?: return null
        TreatmentRecordValidation.audit(old)
        return if (old.requestFingerprint == fingerprint) TreatmentCommandResult.AlreadyApplied(old)
        else TreatmentCommandResult.Rejected(TreatmentRejection.COMMAND_CONFLICT)
    }

    private suspend fun execute(commandId: String, fingerprint: String,
        block: suspend () -> TreatmentCommandResult): TreatmentCommandResult {
        var committed = false
        try {
            val result = db.withTransaction { block() }
            if (result is TreatmentCommandResult.Applied) {
                committed = true; checkpoint("afterTreatmentCommit")
                check(db.treatmentDao().audit(result.audit.id) == result.audit)
                check(db.treatmentDao().episode(result.episode.id) == result.episode)
            }
            return result
        } catch (c: CancellationException) { throw c }
        catch (_: Exception) { return TreatmentCommandResult.Failed(commandId, committed) }
    }

    private fun validUuid(value: String) = runCatching { UUID.fromString(value).toString() == value }.getOrDefault(false)

    private fun fingerprint(vararg values: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        values.forEach { value ->
            val bytes = value.toByteArray(Charsets.UTF_8)
            digest.update(ByteBuffer.allocate(4).putInt(bytes.size).array()); digest.update(bytes)
        }
        return digest.digest().joinToString("") { (it.toInt() and 255).toString(16).padStart(2, '0') }
    }
}
