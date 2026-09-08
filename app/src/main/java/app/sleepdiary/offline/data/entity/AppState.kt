package app.sleepdiary.offline.data.entity
import androidx.room.*
import app.sleepdiary.offline.domain.model.*

@Entity(tableName = "app_state",
 foreignKeys = [ForeignKey(entity = SafetyAssessment::class, parentColumns = ["id"], childColumns = ["currentSafetyAssessmentId"])],
 indices = [Index("currentSafetyAssessmentId")])
data class AppState(
 @PrimaryKey val singletonId: Int = 0,
 val backupFormatVersion: String = Versions.BACKUP_FORMAT,
 val trackingStartDate: String? = null,
 val hasSeenMinimalInteractionExplanation: Boolean = false,
 val hasSeenSharePrivacyNotice: Boolean = false,
 val lastDisplayedWeeklyPeriodIndex: Int = 0,
 val currentSafetyAssessmentId: String? = null
) { init { require(singletonId == 0) { "AppState has a single row" } } }
