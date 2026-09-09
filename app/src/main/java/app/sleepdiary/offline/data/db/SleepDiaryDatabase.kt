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
 version = 2, exportSchema = true
)
@TypeConverters(DatabaseConverters::class)
abstract class SleepDiaryDatabase : RoomDatabase() {
 abstract fun diaryDao(): DiaryDao
 abstract fun safetyDao(): SafetyDao
 abstract fun stateAndSummaryDao(): StateAndSummaryDao
 companion object {
  val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
   override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
    db.execSQL("ALTER TABLE app_state ADD COLUMN hasCompletedOnboarding INTEGER NOT NULL DEFAULT 0")
    db.execSQL("UPDATE app_state SET backupFormatVersion = '" + Versions.BACKUP_FORMAT + "'")
   }
  }
  fun open(context: Context): SleepDiaryDatabase =
   Room.databaseBuilder(context.applicationContext, SleepDiaryDatabase::class.java, "sleepdiary.db").addMigrations(MIGRATION_1_2).build()
 }
}
