package app.sleepdiary.offline.data.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Device-local invalidation metadata, not clinical history or backup data. */
@Entity(tableName = "local_execution_state")
data class LocalExecutionState(
    @PrimaryKey val singletonId: Int = 0,
    val restoreGeneration: String,
    val activationStatus: String = "REQUIRES_REVIEW",
    val localSchemaVersion: String = "LOCAL_EXECUTION_1"
)
