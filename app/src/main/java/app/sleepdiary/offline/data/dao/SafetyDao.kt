package app.sleepdiary.offline.data.dao
import androidx.room.*
import app.sleepdiary.offline.data.entity.*

data class AssessmentWithAnswers(
 @Embedded val assessment: SafetyAssessment,
 @Relation(parentColumn = "id", entityColumn = "assessmentId") val answers: List<SafetyAnswer>
)
@Dao
interface SafetyDao {
 @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insertAssessment(value: SafetyAssessment)
 @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insertAnswers(values: List<SafetyAnswer>)
 @Transaction
 @Query("SELECT * FROM safety_assessment WHERE id = :id")
 suspend fun byId(id: String): AssessmentWithAnswers?
}
