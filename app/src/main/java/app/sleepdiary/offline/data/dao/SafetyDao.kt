package app.sleepdiary.offline.data.dao
import androidx.room.*
import app.sleepdiary.offline.data.entity.*

data class AssessmentWithAnswers(
 @Embedded val assessment: SafetyAssessment,
 @Relation(parentColumn = "id", entityColumn = "assessmentId") val answers: List<SafetyAnswer>
)
@Dao
interface SafetyDao {
 @Transaction @Query("SELECT * FROM safety_assessment ORDER BY id") suspend fun all(): List<AssessmentWithAnswers>
 @Update suspend fun updateAssessment(value: SafetyAssessment)
 @Query("UPDATE safety_assessment SET isCurrent = 0") suspend fun retireCurrent()
 @Query("DELETE FROM safety_answer WHERE assessmentId = :id") suspend fun deleteAnswers(id: String)
 @Query("DELETE FROM safety_answer") suspend fun deleteAllAnswers()
 @Query("DELETE FROM safety_assessment") suspend fun deleteAllAssessments()
 @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insertAssessment(value: SafetyAssessment)
 @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insertAnswers(values: List<SafetyAnswer>)
 @Transaction
 @Query("SELECT * FROM safety_assessment WHERE id = :id")
 suspend fun byId(id: String): AssessmentWithAnswers?
}
