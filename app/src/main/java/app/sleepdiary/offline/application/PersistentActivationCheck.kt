package app.sleepdiary.offline.application
import androidx.room.withTransaction
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.repository.LocalExecutionRepository
import kotlinx.coroutines.CancellationException
import java.time.LocalDate

internal data class ActivationToken(val snapshotId: String, val restoreGeneration: String, val inputHash: String)
internal enum class ActivationBlock {
    REQUIRES_REVIEW, RESTORE_GENERATION_CHANGED, INPUT_CHANGED, SNAPSHOT_MISSING, TECHNICAL_FAILURE
}
/** Preflight only. No activated/success result exists without released clinical rules. */
internal class PersistentActivationCheck(private val db: SleepDiaryDatabase) {
    suspend fun prepare(snapshotId: String): ActivationToken = db.withTransaction {
        val snapshot=checkNotNull(db.inputSnapshotDao().byId(snapshotId))
        InputSnapshotValidation.validate(snapshot)
        val state=LocalExecutionRepository(db).current()
        ActivationToken(snapshot.id,state.restoreGeneration,snapshot.contentHash)
    }
    suspend fun inspect(token: ActivationToken): ActivationBlock {
        try {
            return db.withTransaction {
                val state=LocalExecutionRepository(db).current()
                if(state.restoreGeneration!=token.restoreGeneration) return@withTransaction ActivationBlock.RESTORE_GENERATION_CHANGED
                val snapshot=db.inputSnapshotDao().byId(token.snapshotId) ?: return@withTransaction ActivationBlock.SNAPSHOT_MISSING
                InputSnapshotValidation.validate(snapshot)
                if(snapshot.contentHash!=token.inputHash) return@withTransaction ActivationBlock.INPUT_CHANGED
                val current=ProtocolInputCapture(db).capture(LocalDate.parse(snapshot.fromDate),LocalDate.parse(snapshot.throughDate))
                if(current.contentHash!=token.inputHash) ActivationBlock.INPUT_CHANGED else ActivationBlock.REQUIRES_REVIEW
            }
        } catch(c:CancellationException) { throw c }
        catch(_:Exception) { return ActivationBlock.TECHNICAL_FAILURE }
    }
}
