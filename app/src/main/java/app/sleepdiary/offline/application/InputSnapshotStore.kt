package app.sleepdiary.offline.application

import androidx.room.withTransaction
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.entity.InputSnapshot
import kotlinx.coroutines.CancellationException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.util.Base64
import java.util.UUID

internal sealed interface SnapshotSaveResult {
    data class Saved(val snapshot: InputSnapshot): SnapshotSaveResult
    data object InvalidRequest: SnapshotSaveResult
    data object CommandConflict: SnapshotSaveResult
    data class Failed(val commandId: String, val mayHaveCommitted: Boolean): SnapshotSaveResult
}

internal class InputSnapshotStore(private val db: SleepDiaryDatabase, private val clock: Clock = Clock.systemUTC(),
    private val checkpoint: (String) -> Unit = {}) {
    suspend fun capture(commandId: String, from: LocalDate, through: LocalDate): SnapshotSaveResult {
        if(runCatching { require(UUID.fromString(commandId).toString()==commandId); require(from<=through) }.isFailure)
            return SnapshotSaveResult.InvalidRequest
        var committed=false
        try {
            val result=db.withTransaction {
                val dao=db.inputSnapshotDao()
                val previous=dao.byId(commandId)
                if(previous!=null) {
                    InputSnapshotValidation.validate(previous)
                    return@withTransaction if(previous.fromDate==from.toString() && previous.throughDate==through.toString())
                        SnapshotSaveResult.Saved(previous) else SnapshotSaveResult.CommandConflict
                }
                val capture=ProtocolInputCapture(db,clock).capture(from,through)
                val snapshot=InputSnapshot(commandId,capture.capturedAtUtc,from.toString(),through.toString(),capture.captureVersion,
                    Base64.getEncoder().encodeToString(capture.canonicalBytes()),capture.contentHash)
                InputSnapshotValidation.validate(snapshot)
                dao.insert(snapshot);checkpoint("afterWrite")
                check(dao.byId(commandId)==snapshot)
                SnapshotSaveResult.Saved(snapshot)
            }
            if(result is SnapshotSaveResult.Saved) {
                committed=true;checkpoint("afterCommit")
                check(db.inputSnapshotDao().byId(commandId)==result.snapshot)
            }
            return result
        } catch(c:CancellationException) { throw c }
        catch(_:Exception) { return SnapshotSaveResult.Failed(commandId,committed) }
    }
}

internal object InputSnapshotValidation {
    fun validate(value: InputSnapshot) {
        require(UUID.fromString(value.id).toString()==value.id)
        Instant.parse(value.capturedAtUtc)
        val from=LocalDate.parse(value.fromDate);val through=LocalDate.parse(value.throughDate)
        require(from<=through && from.toString()==value.fromDate && through.toString()==value.throughDate)
        require(value.captureVersion==RawInputEncoding.VERSION)
        require(value.contentHash.matches(Regex("[0-9a-f]{64}")))
        val bytes=Base64.getDecoder().decode(value.payloadBase64)
        require(Base64.getEncoder().encodeToString(bytes)==value.payloadBase64)
        require(CapturedProtocolInput(value.id,value.capturedAtUtc,bytes).contentHash==value.contentHash)
        RawInputDecoder.verify(bytes,from,through)
    }
}
