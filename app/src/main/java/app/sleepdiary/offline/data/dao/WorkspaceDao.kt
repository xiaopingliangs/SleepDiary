package app.sleepdiary.offline.data.dao

import androidx.room.*
import app.sleepdiary.offline.data.entity.WorkspaceRecord

@Dao interface WorkspaceDao {
    @Query("SELECT * FROM workspace_record ORDER BY id") suspend fun all(): List<WorkspaceRecord>
    @Query("SELECT * FROM workspace_record WHERE id = :id") suspend fun byId(id: String): WorkspaceRecord?
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(record: WorkspaceRecord)
    @Update(onConflict = OnConflictStrategy.ABORT) suspend fun update(record: WorkspaceRecord): Int
    @Query("DELETE FROM workspace_record") suspend fun deleteAllForRestore()
}
