package app.sleepdiary.offline.data.dao
import androidx.room.*
import app.sleepdiary.offline.data.entity.*

@Dao
interface StateAndSummaryDao {
 @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insertSummary(value: WeeklySummarySnapshot)
 @Query("SELECT * FROM weekly_summary_snapshot WHERE id = :id") suspend fun summaryById(id: String): WeeklySummarySnapshot?
 @Upsert suspend fun putState(value: AppState)
 @Query("SELECT * FROM app_state WHERE singletonId = 0") suspend fun state(): AppState?
}
