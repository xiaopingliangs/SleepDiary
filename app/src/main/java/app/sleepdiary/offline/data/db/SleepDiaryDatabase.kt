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
 entities = [SleepDiaryEntry::class, SafetyAssessment::class, SafetyAnswer::class, WeeklySummarySnapshot::class, AppState::class, WorkspaceRecord::class, InputSnapshot::class, LocalExecutionState::class, TreatmentEpisode::class, TreatmentAuditEvent::class],
 version = 6, exportSchema = true
)
@TypeConverters(DatabaseConverters::class)
abstract class SleepDiaryDatabase : RoomDatabase() {
 abstract fun diaryDao(): DiaryDao
 abstract fun safetyDao(): SafetyDao
 abstract fun stateAndSummaryDao(): StateAndSummaryDao
 abstract fun workspaceDao(): WorkspaceDao
 abstract fun inputSnapshotDao(): InputSnapshotDao
 abstract fun localExecutionDao(): LocalExecutionDao
 abstract fun treatmentDao(): TreatmentDao
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
    db.execSQL("UPDATE app_state SET backupFormatVersion = '" + Versions.LEGACY_BACKUP_2_1 + "'")
   }
  }
  val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
   override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
    db.execSQL("CREATE TABLE IF NOT EXISTS treatment_episode (id TEXT NOT NULL, mode TEXT NOT NULL, state TEXT NOT NULL, pausedFromState TEXT, adultScopeConfirmed INTEGER NOT NULL, protocolVersion TEXT NOT NULL, metricsVersion TEXT NOT NULL, treatmentSafetyRuleVersion TEXT NOT NULL, exceptionRuleVersion TEXT NOT NULL, currentPlanId TEXT, startedAtUtc TEXT NOT NULL, createdAtUtc TEXT NOT NULL, updatedAtUtc TEXT NOT NULL, completedAtUtc TEXT, revision INTEGER NOT NULL, treatmentDataSchemaVersion TEXT NOT NULL, PRIMARY KEY(id))")
    db.execSQL("CREATE TABLE IF NOT EXISTS treatment_audit_event (id TEXT NOT NULL, episodeId TEXT NOT NULL, commandType TEXT NOT NULL, requestFingerprint TEXT NOT NULL, fromState TEXT, toState TEXT NOT NULL, fromRevision INTEGER NOT NULL, toRevision INTEGER NOT NULL, occurredAtUtc TEXT NOT NULL, restoreGenerationAtCommand TEXT NOT NULL, treatmentDataSchemaVersion TEXT NOT NULL, PRIMARY KEY(id), FOREIGN KEY(episodeId) REFERENCES treatment_episode(id) ON UPDATE NO ACTION ON DELETE NO ACTION)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_treatment_audit_event_episodeId ON treatment_audit_event (episodeId)")
    db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_treatment_audit_event_episodeId_toRevision ON treatment_audit_event (episodeId, toRevision)")
    db.execSQL("UPDATE app_state SET backupFormatVersion = '" + Versions.BACKUP_FORMAT + "'")
   }
  }
  val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
   override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
    db.execSQL("CREATE TABLE IF NOT EXISTS local_execution_state (singletonId INTEGER NOT NULL, restoreGeneration TEXT NOT NULL, activationStatus TEXT NOT NULL, localSchemaVersion TEXT NOT NULL, PRIMARY KEY(singletonId))")
    db.execSQL("INSERT INTO local_execution_state (singletonId, restoreGeneration, activationStatus, localSchemaVersion) VALUES (0, ?, 'REQUIRES_REVIEW', 'LOCAL_EXECUTION_1')", arrayOf(java.util.UUID.randomUUID().toString()))
   }
  }
  fun open(context: Context): SleepDiaryDatabase =
   Room.databaseBuilder(context.applicationContext, SleepDiaryDatabase::class.java, "sleepdiary.db").addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6).build()
 }
}
