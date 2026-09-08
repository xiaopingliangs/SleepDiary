package app.sleepdiary.offline.data.dao
import androidx.room.*
import app.sleepdiary.offline.data.entity.SleepDiaryEntry

@Dao
interface DiaryDao {
 @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(entry: SleepDiaryEntry)
 @Query("SELECT * FROM sleep_diary_entry WHERE id = :id") suspend fun byId(id: String): SleepDiaryEntry?
}
