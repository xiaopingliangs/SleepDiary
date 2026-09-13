package app.sleepdiary.offline.data.dao

import androidx.room.*
import app.sleepdiary.offline.data.entity.InputSnapshot

@Dao
interface InputSnapshotDao {
    @Query("SELECT * FROM input_snapshot ORDER BY id") suspend fun all(): List<InputSnapshot>
    @Query("SELECT * FROM input_snapshot WHERE id = :id") suspend fun byId(id: String): InputSnapshot?
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(value: InputSnapshot)
    @Query("DELETE FROM input_snapshot") suspend fun deleteAllForRestore()
}
