package app.sleepdiary.offline.data.repository

import androidx.room.withTransaction
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.entity.*

/** UI-facing boundary; all entry writes still go through the formal repository. */
interface DiaryUiStore {
    suspend fun entries(): List<SleepDiaryEntry>
    suspend fun state(): AppState
    suspend fun save(entry:SleepDiaryEntry, confirmed:Boolean):SaveResult
    suspend fun completeOnboarding() { error("Onboarding store required") }
    suspend fun currentSafety():app.sleepdiary.offline.data.dao.AssessmentWithAnswers? = null
    suspend fun saveSafety(id:String,answers:List<SafetyAnswer>,completed:Boolean):app.sleepdiary.offline.data.dao.AssessmentWithAnswers { error("Safety store required") }
    suspend fun acknowledgeWeekly(index:Int) {}
    suspend fun acknowledgeExplanation()
}

class RoomDiaryUiStore(private val db:SleepDiaryDatabase, clock: java.time.Clock = java.time.Clock.systemDefaultZone()):DiaryUiStore {
    private val repository=DiaryRepository(db, clock)
    override suspend fun entries() = db.diaryDao().all()
    override suspend fun state() = db.stateAndSummaryDao().state() ?: AppState()
    override suspend fun save(entry:SleepDiaryEntry,confirmed:Boolean)=repository.save(entry,confirmed)
    override suspend fun completeOnboarding() { db.withTransaction {
        val dao=db.stateAndSummaryDao();val value=(dao.state()?:AppState()).copy(hasCompletedOnboarding=true)
        dao.putState(value);check(dao.state()==value)
    } }
    override suspend fun currentSafety()=state().currentSafetyAssessmentId?.let {db.safetyDao().byId(it)}
    override suspend fun saveSafety(id:String,answers:List<SafetyAnswer>,completed:Boolean)=SafetyRepository(db).save(id,answers,completed)
    override suspend fun acknowledgeWeekly(index:Int) { db.withTransaction {
        val dao=db.stateAndSummaryDao();check(dao.summaries().any{it.periodIndex==index})
        val value=(dao.state()?:AppState()).copy(lastDisplayedWeeklyPeriodIndex=index);dao.putState(value);check(dao.state()==value)
    } }
    override suspend fun acknowledgeExplanation() { db.withTransaction {
        val dao=db.stateAndSummaryDao()
        val state=(dao.state() ?: AppState()).copy(hasSeenMinimalInteractionExplanation=true)
        dao.putState(state)
        check(dao.state()==state)
    } }
}
