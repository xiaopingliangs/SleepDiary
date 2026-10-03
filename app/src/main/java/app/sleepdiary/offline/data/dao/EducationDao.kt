package app.sleepdiary.offline.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import app.sleepdiary.offline.data.entity.EducationCommittedChoice
import app.sleepdiary.offline.data.entity.EducationPosition
import app.sleepdiary.offline.data.entity.EducationProgress
import app.sleepdiary.offline.data.entity.EducationRecord
import app.sleepdiary.offline.data.entity.EducationReviewState

@Dao
interface EducationDao {
    @Query("SELECT * FROM education_progress ORDER BY contentVersionId, contentId")
    suspend fun progress(): List<EducationProgress>
    @Query("SELECT * FROM education_progress WHERE contentVersionId = :version AND contentId = :contentId")
    suspend fun progress(version: String, contentId: String): EducationProgress?
    @Upsert suspend fun putProgress(value: EducationProgress)
    @Query("DELETE FROM education_progress") suspend fun clearProgress()

    @Query("SELECT * FROM education_committed_choice ORDER BY contentVersionId, promptContentId")
    suspend fun choices(): List<EducationCommittedChoice>
    @Query("SELECT * FROM education_committed_choice WHERE contentVersionId = :version AND promptContentId = :promptId")
    suspend fun choice(version: String, promptId: String): EducationCommittedChoice?
    @Upsert suspend fun putChoice(value: EducationCommittedChoice)
    @Query("DELETE FROM education_committed_choice WHERE contentVersionId = :version AND promptContentId = :promptId")
    suspend fun clearChoice(version: String, promptId: String): Int
    @Query("DELETE FROM education_committed_choice") suspend fun clearChoices()

    @Query("SELECT * FROM education_review_state ORDER BY contentVersionId, contentId")
    suspend fun reviewStates(): List<EducationReviewState>
    @Query("SELECT * FROM education_review_state WHERE contentVersionId = :version AND contentId = :contentId")
    suspend fun reviewState(version: String, contentId: String): EducationReviewState?
    @Upsert suspend fun putReviewState(value: EducationReviewState)
    @Query("DELETE FROM education_review_state WHERE contentVersionId = :version AND contentId = :contentId")
    suspend fun clearReviewState(version: String, contentId: String): Int
    @Query("DELETE FROM education_review_state") suspend fun clearReviewStates()

    @Query("SELECT * FROM education_record ORDER BY contentVersionId, contentId, workspaceRecordId")
    suspend fun records(): List<EducationRecord>
    @Query("SELECT * FROM education_record WHERE workspaceRecordId = :workspaceRecordId")
    suspend fun record(workspaceRecordId: String): EducationRecord?
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insertRecord(value: EducationRecord)
    @Upsert suspend fun putRecord(value: EducationRecord)
    @Query("DELETE FROM education_record WHERE workspaceRecordId = :workspaceRecordId")
    suspend fun unlinkRecord(workspaceRecordId: String): Int
    @Query("DELETE FROM education_record") suspend fun clearRecords()

    @Query("SELECT * FROM education_position ORDER BY contentVersionId, courseId")
    suspend fun positions(): List<EducationPosition>
    @Query("SELECT * FROM education_position WHERE contentVersionId = :version AND courseId = :courseId")
    suspend fun position(version: String, courseId: String): EducationPosition?
    @Upsert suspend fun putPosition(value: EducationPosition)
    @Query("DELETE FROM education_position WHERE contentVersionId = :version AND courseId = :courseId")
    suspend fun clearPosition(version: String, courseId: String): Int
    @Query("DELETE FROM education_position") suspend fun clearPositions()
}
