package app.sleepdiary.offline.data.entity
import androidx.room.*
import app.sleepdiary.offline.domain.model.*

@Entity(tableName = "weekly_summary_snapshot",
 foreignKeys = [ForeignKey(entity = WeeklySummarySnapshot::class, parentColumns = ["id"], childColumns = ["supersedesSummaryId"])],
 indices = [Index("supersedesSummaryId"), Index(value = ["periodIndex", "revision"], unique = true)])
data class WeeklySummarySnapshot(
 @PrimaryKey val id: String,
 val periodIndex: Int,
 val periodStartDate: String,
 val periodEndDate: String,
 val validEntryCount: Int,
 val avgSolMin: Double?,
 val avgAwakeningCount: Double?,
 val avgWasoMin: Double?,
 val avgTerminalWakefulnessMin: Double?,
 val avgTibMin: Double?,
 val avgSleepQuality: Double?,
 val sourceEntryIdsJson: String,
 val diarySchemaVersion: String = Versions.DIARY_SCHEMA,
 val weeklySummaryRuleVersion: String = Versions.WEEKLY_RULE,
 val generatedAtUtc: String,
 val revision: Int,
 val isCurrent: Boolean,
 val supersedesSummaryId: String?
)
