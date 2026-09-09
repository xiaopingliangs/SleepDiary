package app.sleepdiary.offline.data.dao
import androidx.room.*
import app.sleepdiary.offline.data.entity.*

@Dao
interface StateAndSummaryDao {
 @Query("DELETE FROM app_state") suspend fun deleteState()
 @Query("DELETE FROM weekly_summary_snapshot WHERE id = :id") suspend fun deleteSummary(id: String)
 @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insertSummary(value: WeeklySummarySnapshot)
 @Query("SELECT * FROM weekly_summary_snapshot WHERE id = :id") suspend fun summaryById(id: String): WeeklySummarySnapshot?
 @Query("SELECT * FROM weekly_summary_snapshot ORDER BY periodIndex, revision") suspend fun summaries(): List<WeeklySummarySnapshot>
 @Query("UPDATE weekly_summary_snapshot SET isCurrent = 0 WHERE id = :id AND isCurrent = 1") suspend fun retireSummary(id: String): Int
 @Upsert suspend fun putState(value: AppState)
 @Query("SELECT * FROM app_state WHERE singletonId = 0") suspend fun state(): AppState?
}
