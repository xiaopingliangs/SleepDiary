package app.sleepdiary.offline.data.db
import android.content.Context
import androidx.room.*
import app.sleepdiary.offline.data.entity.*
import app.sleepdiary.offline.data.dao.*
import app.sleepdiary.offline.domain.model.*

class DatabaseConverters {
 @TypeConverter fun gate(value: String): SafetyGate = SafetyGate.valueOf(value)
 @TypeConverter fun gate(value: SafetyGate): String = value.name
 @TypeConverter fun status(value: String): AssessmentStatus = AssessmentStatus.valueOf(value)
 @TypeConverter fun status(value: AssessmentStatus): String = value.name
}
@Database(
 entities = [SleepDiaryEntry::class, SafetyAssessment::class, SafetyAnswer::class, WeeklySummarySnapshot::class, AppState::class],
 version = 1, exportSchema = true
)
@TypeConverters(DatabaseConverters::class)
abstract class SleepDiaryDatabase : RoomDatabase() {
 abstract fun diaryDao(): DiaryDao
 abstract fun safetyDao(): SafetyDao
 abstract fun stateAndSummaryDao(): StateAndSummaryDao
 companion object {
  fun open(context: Context): SleepDiaryDatabase =
   Room.databaseBuilder(context.applicationContext, SleepDiaryDatabase::class.java, "sleepdiary.db").build()
 }
}
