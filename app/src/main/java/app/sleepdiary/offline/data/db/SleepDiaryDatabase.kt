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
 entities = [SleepDiaryEntry::class, SafetyAssessment::class, SafetyAnswer::class, WeeklySummarySnapshot::class, AppState::class, WorkspaceRecord::class, InputSnapshot::class],
 version = 4, exportSchema = true
)
@TypeConverters(DatabaseConverters::class)
abstract class SleepDiaryDatabase : RoomDatabase() {
 abstract fun diaryDao(): DiaryDao
 abstract fun safetyDao(): SafetyDao
 abstract fun stateAndSummaryDao(): StateAndSummaryDao
 abstract fun workspaceDao(): WorkspaceDao
 abstract fun inputSnapshotDao(): InputSnapshotDao
 companion object {
  val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
   override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
    db.execSQL("ALTER TABLE app_state ADD COLUMN hasCompletedOnboarding INTEGER NOT NULL DEFAULT 0")
    db.execSQL("UPDATE app_state SET backupFormatVersion = '" + Versions.LEGACY_BACKUP_1_1 + "'")
   }
  }
  val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
   override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
    db.execSQL("CREATE TABLE IF NOT EXISTS workspace_record (id TEXT NOT NULL, kind TEXT NOT NULL, title TEXT NOT NULL, body TEXT NOT NULL, createdAtUtc TEXT NOT NULL, updatedAtUtc TEXT NOT NULL, revision INTEGER NOT NULL, workspaceSchemaVersion TEXT NOT NULL, PRIMARY KEY(id))")
    db.execSQL("UPDATE app_state SET backupFormatVersion = '" + Versions.LEGACY_BACKUP_2_0 + "'")
   }
  }
  val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
   override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
    db.execSQL("CREATE TABLE IF NOT EXISTS input_snapshot (id TEXT NOT NULL, capturedAtUtc TEXT NOT NULL, fromDate TEXT NOT NULL, throughDate TEXT NOT NULL, captureVersion TEXT NOT NULL, payloadBase64 TEXT NOT NULL, contentHash TEXT NOT NULL, PRIMARY KEY(id))")
    db.execSQL("UPDATE app_state SET backupFormatVersion = '" + Versions.BACKUP_FORMAT + "'")
   }
  }
  fun open(context: Context): SleepDiaryDatabase =
   Room.databaseBuilder(context.applicationContext, SleepDiaryDatabase::class.java, "sleepdiary.db").addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build()
 }
}
