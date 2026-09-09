package app.sleepdiary.offline.data.dao
import androidx.room.*
import app.sleepdiary.offline.data.entity.SleepDiaryEntry

@Dao
interface DiaryDao {
 @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(entry: SleepDiaryEntry)
 @Update(onConflict = OnConflictStrategy.ABORT) suspend fun update(entry: SleepDiaryEntry): Int
 @Query("SELECT * FROM sleep_diary_entry ORDER BY sleepDate, id") suspend fun all(): List<SleepDiaryEntry>
 @Query("SELECT * FROM sleep_diary_entry WHERE id = :id") suspend fun byId(id: String): SleepDiaryEntry?
}
