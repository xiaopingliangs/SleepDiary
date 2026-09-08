package app.sleepdiary.offline.data.entity
import androidx.room.*
import app.sleepdiary.offline.domain.model.*

@Entity(tableName = "safety_assessment")
data class SafetyAssessment(
 @PrimaryKey val id: String,
 val answeredAtUtc: String,
 val safetySchemaVersion: String = Versions.SAFETY_SCHEMA,
 val derivedGate: SafetyGate,
 val safetyRuleVersion: String = Versions.SAFETY_RULE,
 val status: AssessmentStatus,
 val isCurrent: Boolean
)
