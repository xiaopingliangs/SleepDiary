package app.sleepdiary.offline.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.sleepdiary.offline.data.entity.TreatmentAuditEvent
import app.sleepdiary.offline.data.entity.TreatmentEpisode

@Dao
interface TreatmentDao {
    @Query("SELECT * FROM treatment_episode ORDER BY createdAtUtc, id") suspend fun episodes(): List<TreatmentEpisode>
    @Query("SELECT * FROM treatment_episode WHERE id = :id") suspend fun episode(id: String): TreatmentEpisode?
    @Query("SELECT * FROM treatment_episode WHERE completedAtUtc IS NULL ORDER BY createdAtUtc, id") suspend fun unfinishedEpisodes(): List<TreatmentEpisode>
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insertEpisode(value: TreatmentEpisode)
    @Query("UPDATE treatment_episode SET state=:state, pausedFromState=:pausedFromState, updatedAtUtc=:updatedAtUtc, revision=:nextRevision WHERE id=:id AND revision=:expectedRevision")
    suspend fun updateState(id: String, expectedRevision: Int, nextRevision: Int, state: String, pausedFromState: String?, updatedAtUtc: String): Int
    @Query("SELECT * FROM treatment_audit_event ORDER BY occurredAtUtc, id") suspend fun audits(): List<TreatmentAuditEvent>
    @Query("SELECT * FROM treatment_audit_event WHERE id = :id") suspend fun audit(id: String): TreatmentAuditEvent?
    @Query("SELECT * FROM treatment_audit_event WHERE episodeId = :episodeId ORDER BY toRevision") suspend fun auditsForEpisode(episodeId: String): List<TreatmentAuditEvent>
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insertAudit(value: TreatmentAuditEvent)
    @Query("DELETE FROM treatment_audit_event") suspend fun deleteAllAuditsForRestore()
    @Query("DELETE FROM treatment_episode") suspend fun deleteAllEpisodesForRestore()
}
