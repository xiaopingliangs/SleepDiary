package app.sleepdiary.offline.data.repository

import androidx.room.withTransaction
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.entity.*

/** UI-facing boundary; all entry writes still go through the formal repository. */
interface DiaryUiStore {
    suspend fun entries(): List<SleepDiaryEntry>
    suspend fun state(): AppState
    suspend fun save(entry:SleepDiaryEntry, confirmed:Boolean):SaveResult
    suspend fun acknowledgeExplanation()
}

class RoomDiaryUiStore(private val db:SleepDiaryDatabase):DiaryUiStore {
    private val repository=DiaryRepository(db)
    override suspend fun entries() = db.diaryDao().all()
    override suspend fun state() = db.stateAndSummaryDao().state() ?: AppState()
    override suspend fun save(entry:SleepDiaryEntry,confirmed:Boolean)=repository.save(entry,confirmed)
    override suspend fun acknowledgeExplanation() { db.withTransaction {
        val dao=db.stateAndSummaryDao()
        val state=(dao.state() ?: AppState()).copy(hasSeenMinimalInteractionExplanation=true)
        dao.putState(state)
        check(dao.state()==state)
    } }
}
