package app.sleepdiary.offline.application

import androidx.room.withTransaction
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.dao.AssessmentWithAnswers
import app.sleepdiary.offline.data.entity.*
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.CharBuffer
import java.nio.charset.CodingErrorAction
import java.security.MessageDigest
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/** Internal engineering capture; no durable snapshot table, treatment eligibility, or export. */
internal class ProtocolInputCapture(private val db: SleepDiaryDatabase, private val clock: Clock = Clock.systemUTC()) {
    suspend fun capture(from: LocalDate, through: LocalDate): CapturedProtocolInput {
        require(from <= through)
        return db.withTransaction {
            val entries = db.diaryDao().all().filter { LocalDate.parse(it.sleepDate) in from..through }
            val safety = db.safetyDao().all()
            val state = db.stateAndSummaryDao().state()
            val payload = RawInputEncoding.encode(from, through, entries, safety, state)
            CapturedProtocolInput(UUID.randomUUID().toString(), Instant.now(clock).toString(), payload)
        }
    }
}

internal class CapturedProtocolInput(val id: String, val capturedAtUtc: String, bytes: ByteArray) {
    private val payload = bytes.copyOf()
    val captureVersion = RawInputEncoding.VERSION
    val contentHash: String = MessageDigest.getInstance("SHA-256").digest(payload)
        .joinToString("") { (it.toInt() and 255).toString(16).padStart(2, '0') }
    fun canonicalBytes(): ByteArray = payload.copyOf()
}

/** Fixed field order, big-endian integer/length values, UTF-8 REPORT, explicit null marker.
 * Scope and encoding version are hashed. Raw strings are never trimmed, parsed/reformatted,
 * or normalized. Assessment details remain raw JSON strings, not interpreted medical facts.
 */
internal object RawInputEncoding {
    const val VERSION = "RAW_INPUT_CAPTURE_1"
    fun encode(from: LocalDate, through: LocalDate, entries: List<SleepDiaryEntry>,
        safety: List<AssessmentWithAnswers>, state: AppState?): ByteArray {
        require(from <= through)
        require(entries.map { it.id }.distinct().size == entries.size)
        require(safety.map { it.assessment.id }.distinct().size == safety.size)
        require(entries.all { LocalDate.parse(it.sleepDate) in from..through })
        val out = ByteArrayOutputStream()
        DataOutputStream(out).use { w ->
            fun text(value: String?) {
                if(value == null) { w.writeByte(0); return }
                w.writeByte(1)
                val encoded = Charsets.UTF_8.newEncoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).encode(CharBuffer.wrap(value))
                val bytes = ByteArray(encoded.remaining()); encoded.get(bytes)
                w.writeInt(bytes.size); w.write(bytes)
            }
            text(VERSION); text(from.toString()); text(through.toString())
            w.writeInt(entries.size)
            entries.sortedBy { it.id }.forEach { e ->
                text(e.id); text(e.sleepDate); text(e.bedEnteredAtLocal); text(e.trySleepAtLocal)
                w.writeInt(e.sleepOnsetLatencyMin); w.writeInt(e.awakeningCount); w.writeInt(e.wakeAfterSleepOnsetMin)
                text(e.finalAwakeningAtLocal); text(e.outOfBedAtLocal); w.writeInt(e.sleepQuality)
                text(e.comment); text(e.timeZoneIdAtEntry); text(e.createdAtUtc); text(e.updatedAtUtc)
                text(e.diarySchemaVersion); w.writeBoolean(e.consistencyWarning)
            }
            w.writeInt(safety.size)
            safety.sortedBy { it.assessment.id }.forEach { pair ->
                val a = pair.assessment
                require(pair.answers.all { it.assessmentId == a.id })
                require(pair.answers.map { it.questionId }.distinct().size == pair.answers.size)
                text(a.id); text(a.answeredAtUtc); text(a.safetySchemaVersion); text(a.derivedGate.name)
                text(a.safetyRuleVersion); text(a.status.name); w.writeBoolean(a.isCurrent)
                w.writeInt(pair.answers.size)
                pair.answers.sortedBy { it.questionId }.forEach {
                    text(it.assessmentId); text(it.questionId); text(it.answerCode); text(it.safetySchemaVersion); text(it.detailJson)
                }
            }
            w.writeBoolean(state != null)
            state?.let {
                w.writeInt(it.singletonId); text(it.backupFormatVersion); text(it.trackingStartDate)
                w.writeBoolean(it.hasSeenMinimalInteractionExplanation); w.writeBoolean(it.hasSeenSharePrivacyNotice)
                w.writeInt(it.lastDisplayedWeeklyPeriodIndex); text(it.currentSafetyAssessmentId); w.writeBoolean(it.hasCompletedOnboarding)
            }
        }
        return out.toByteArray()
    }
}
