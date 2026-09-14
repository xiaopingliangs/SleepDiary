package app.sleepdiary.offline.data.dao
import androidx.room.*
import app.sleepdiary.offline.data.entity.LocalExecutionState

@Dao
interface LocalExecutionDao {
    @Query("SELECT * FROM local_execution_state WHERE singletonId = 0") suspend fun state(): LocalExecutionState?
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(value: LocalExecutionState)
    @Update suspend fun update(value: LocalExecutionState): Int
}
