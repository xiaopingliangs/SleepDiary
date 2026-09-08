package app.sleepdiary.offline.data.repository
import androidx.room.withTransaction
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.entity.SleepDiaryEntry

/** Persistence boundary only. Form validation and editing workflows belong to later milestones. */
class DiaryRepository(private val database: SleepDiaryDatabase) {
 suspend fun insertAndVerify(entry: SleepDiaryEntry): SleepDiaryEntry {
  database.withTransaction {
   database.diaryDao().insert(entry)
   check(database.diaryDao().byId(entry.id) == entry) { "Diary read-back mismatch" }
  }
  return checkNotNull(database.diaryDao().byId(entry.id)).also {
   check(it == entry) { "Committed diary read-back mismatch" }
  }
 }
}
