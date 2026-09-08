package app.sleepdiary.offline.data.entity
import androidx.room.*
import app.sleepdiary.offline.domain.model.*

@Entity(tableName = "safety_answer", primaryKeys = ["assessmentId", "questionId"],
 foreignKeys = [ForeignKey(entity = SafetyAssessment::class, parentColumns = ["id"], childColumns = ["assessmentId"])])
data class SafetyAnswer(
 val assessmentId: String,
 val questionId: String,
 val answerCode: String,
 val safetySchemaVersion: String = Versions.SAFETY_SCHEMA,
 val detailJson: String?
)
