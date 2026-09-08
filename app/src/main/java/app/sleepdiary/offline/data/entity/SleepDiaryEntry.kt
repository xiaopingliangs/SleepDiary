package app.sleepdiary.offline.data.entity
import androidx.room.*
import app.sleepdiary.offline.domain.model.*

@Entity(tableName = "sleep_diary_entry", indices = [Index(value = ["sleepDate"], unique = true)])
data class SleepDiaryEntry(
 @PrimaryKey val id: String,
 val sleepDate: String,
 val bedEnteredAtLocal: String,
 val trySleepAtLocal: String,
 val sleepOnsetLatencyMin: Int,
 val awakeningCount: Int,
 val wakeAfterSleepOnsetMin: Int,
 val finalAwakeningAtLocal: String,
 val outOfBedAtLocal: String,
 val sleepQuality: Int,
 val comment: String?,
 val timeZoneIdAtEntry: String,
 val createdAtUtc: String,
 val updatedAtUtc: String,
 val diarySchemaVersion: String = Versions.DIARY_SCHEMA,
 val consistencyWarning: Boolean
)
