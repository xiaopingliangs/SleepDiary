package app.sleepdiary.offline.data.repository
import androidx.room.withTransaction
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.entity.LocalExecutionState
import java.util.UUID

class LocalExecutionRepository(private val db: SleepDiaryDatabase) {
    suspend fun current(): LocalExecutionState = db.withTransaction {
        val dao=db.localExecutionDao()
        val value=dao.state() ?: LocalExecutionState(restoreGeneration=UUID.randomUUID().toString()).also { dao.insert(it) }
        validate(value);check(dao.state()==value);value
    }
    /** Must be nested inside complete restore's transaction. */
    internal suspend fun invalidateForRestore(): LocalExecutionState = db.withTransaction {
        val previous=current()
        val next=LocalExecutionState(restoreGeneration=UUID.randomUUID().toString())
        require(next.restoreGeneration!=previous.restoreGeneration)
        check(db.localExecutionDao().update(next)==1)
        check(db.localExecutionDao().state()==next)
        next
    }
    private fun validate(value: LocalExecutionState) {
        require(value.singletonId==0 && value.localSchemaVersion=="LOCAL_EXECUTION_1")
        require(value.activationStatus=="REQUIRES_REVIEW")
        require(UUID.fromString(value.restoreGeneration).toString()==value.restoreGeneration)
    }
}
