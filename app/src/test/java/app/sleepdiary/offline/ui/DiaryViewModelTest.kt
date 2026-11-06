package app.sleepdiary.offline.ui
import app.sleepdiary.offline.data.dao.AssessmentWithAnswers
import app.sleepdiary.offline.domain.model.SafetyGate
import app.sleepdiary.offline.domain.model.AssessmentStatus

import androidx.lifecycle.SavedStateHandle
import app.sleepdiary.offline.clockOn
import app.sleepdiary.offline.diary
import app.sleepdiary.offline.data.entity.*
import app.sleepdiary.offline.data.repository.*
import app.sleepdiary.offline.ui.diary.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class DiaryViewModelTest {
    private val dispatcher=StandardTestDispatcher()
    @Before fun main(){Dispatchers.setMain(dispatcher)}
    @After fun reset(){Dispatchers.resetMain()}
    class Store:DiaryUiStore {
        var rows=listOf<SleepDiaryEntry>();var app=AppState(hasCompletedOnboarding=true);var calls=0;var confirmed=false;var input:SleepDiaryEntry?=null
        var pending:WeeklySummarySnapshot?=null;var displayed:Int?=null
        override suspend fun acknowledgeWeekly(index:Int){displayed=index}
        var fail=false;var throwSave=false;var throwRead=false;var acknowledged=false
        override suspend fun entries():List<SleepDiaryEntry> {if(throwRead) error("private detail");return rows}
        var safety:AssessmentWithAnswers?=AssessmentWithAnswers(SafetyAssessment("existing","2026-09-09T00:00:00Z",derivedGate=SafetyGate.NO_MAJOR_FLAG,status=AssessmentStatus.DRAFT,isCurrent=true),emptyList())
        override suspend fun currentSafety()=safety
        override suspend fun completeOnboarding(){app=app.copy(hasCompletedOnboarding=true)}
        override suspend fun state()=app
        override suspend fun save(entry:SleepDiaryEntry,confirmed:Boolean):SaveResult {
            calls++;this.confirmed=confirmed;input=entry;delay(10)
            if(throwSave) error("private database detail")
            if(fail) return SaveResult.Failure(SaveFailure.WRITE_FAILED,entry)
            rows=rows.filterNot {it.id==entry.id}+entry
            return SaveResult.Success(entry,pending)
        }
        override suspend fun acknowledgeExplanation(){acknowledged=true;app=app.copy(hasSeenMinimalInteractionExplanation=true)}
    }
    private fun vm(store:Store=Store(),handle:SavedStateHandle=SavedStateHandle(mapOf("introDone" to true)))=DiaryViewModel(store,handle,clockOn("2026-09-09"))
    private fun fill(vm:DiaryViewModel,sol:Int=20) {
        if (vm.state.value.page == HomePage.TODAY) vm.beginNewDiary()
        val form=DiaryForm.from(diary())
        listOf(DiaryField.BED,DiaryField.TRY,DiaryField.FINAL,DiaryField.OUT).forEach {vm.answerTime(it,form.time(it)!!)}
        vm.answerNumber(DiaryField.SOL,sol);vm.answerNumber(DiaryField.COUNT,2);vm.answerNumber(DiaryField.WASO,30);vm.answerNumber(DiaryField.QUALITY,3)
    }
    @Test fun missingAnswersNeverReachRepository()=runTest(dispatcher) {val s=Store();val m=vm(s);advanceUntilIdle();m.beginNewDiary();m.save();advanceUntilIdle();assertEquals(0,s.calls);assertEquals(DiaryField.BED,m.state.value.missing);assertEquals("请先完成这项记录",m.state.value.message)}
    @Test fun warningReturnKeepsFormAndDoesNotSave()=runTest(dispatcher) {val s=Store();val m=vm(s);advanceUntilIdle();fill(m,400);m.save();assertTrue(m.state.value.warning);m.dismissWarning();assertFalse(m.state.value.warning);assertEquals(400,m.state.value.form.sol);assertEquals(0,s.calls)}
    @Test fun warningConfirmationPreservesRawEstimates()=runTest(dispatcher) {val s=Store();val m=vm(s);advanceUntilIdle();fill(m,400);m.save();m.save(true);advanceUntilIdle();assertTrue(s.confirmed);assertTrue(s.input!!.consistencyWarning);assertEquals(400,s.input!!.sleepOnsetLatencyMin);assertEquals(30,s.input!!.wakeAfterSleepOnsetMin);assertEquals(HomePage.SUCCESS,m.state.value.page)}
    @Test fun failureKeepsFormAndCannotExit()=runTest(dispatcher) {val s=Store().also {it.fail=true};val m=vm(s);advanceUntilIdle();fill(m);val f=m.state.value.form;m.save();advanceUntilIdle();assertEquals(f,m.state.value.form);assertEquals(HomePage.FORM,m.state.value.page);assertEquals("保存失败，请重试",m.state.value.message);var exited=false;m.exitAfterSuccess{exited=true};advanceUntilIdle();assertFalse(exited)}
    @Test fun exceptionDoesNotExposeStackAndRetryUsesSameUuid()=runTest(dispatcher) {val s=Store().also {it.throwSave=true};val m=vm(s);advanceUntilIdle();fill(m);val id=m.state.value.form.id;m.save();advanceUntilIdle();assertEquals("保存失败，请重试",m.state.value.message);s.throwSave=false;m.save();advanceUntilIdle();assertEquals(id,s.input!!.id);assertEquals(HomePage.SUCCESS,m.state.value.page)}
    @Test fun successOnlyAppearsAfterRepositoryCompletes()=runTest(dispatcher) {val m=vm();advanceUntilIdle();fill(m);m.save();assertEquals(HomePage.FORM,m.state.value.page);assertTrue(m.state.value.busy);advanceUntilIdle();assertEquals(HomePage.SUCCESS,m.state.value.page);assertTrue(m.state.value.firstSuccess)}
    @Test fun rapidSaveIsSerialized()=runTest(dispatcher) {val s=Store();val m=vm(s);advanceUntilIdle();fill(m);m.save();m.save();advanceUntilIdle();assertEquals(1,s.calls)}
    @Test fun existingTodayLoadsAndEditsSameUuid()=runTest(dispatcher) {val e=diary();val s=Store().also {it.rows=listOf(e)};val m=vm(s);advanceUntilIdle();assertEquals(HomePage.TODAY,m.state.value.page);m.editToday();m.comment("changed");m.save();advanceUntilIdle();assertEquals(e.id,s.input!!.id);assertEquals(e.createdAtUtc,s.input!!.createdAtUtc)}
    @Test fun invalidTimeOrderNeverReachesRepository()=runTest(dispatcher) {val s=Store();val m=vm(s);advanceUntilIdle();fill(m);m.answerTime(DiaryField.OUT,java.time.LocalDateTime.parse("2026-09-09T05:00"));m.save();advanceUntilIdle();assertEquals(0,s.calls);assertEquals(HomePage.FORM,m.state.value.page)}
    @Test fun freshInstallEntersTodayWithoutWaitingForOptionalGuide()=runTest(dispatcher) {val store=Store().also{it.app=AppState();it.safety=null};val m=vm(store,handle=SavedStateHandle());advanceUntilIdle();assertEquals(HomePage.TODAY,m.state.value.page);assertFalse(store.app.hasCompletedOnboarding);assertNull(store.safety)}
    @Test fun restoredDraftRetainsNullsAndIdentity()=runTest(dispatcher) {val handle=SavedStateHandle(mapOf("introDone" to true));val m=vm(handle=handle);advanceUntilIdle();m.answerNumber(DiaryField.SOL,15);val recreated=vm(handle=handle);advanceUntilIdle();assertEquals(m.state.value.form,recreated.state.value.form);assertNull(recreated.state.value.form.bed)}
    @Test fun exitAcknowledgesFirstExplanationWithoutChangingDiary()=runTest(dispatcher) {val s=Store();val m=vm(s);advanceUntilIdle();fill(m);m.save();advanceUntilIdle();var exit=false;m.exitAfterSuccess{exit=true};advanceUntilIdle();assertTrue(exit);assertTrue(s.acknowledged);assertEquals(1,s.calls)}
    @Test fun subsequentSuccessOmitsFirstExplanation()=runTest(dispatcher) {val s=Store().also {it.app=AppState(hasSeenMinimalInteractionExplanation=true,hasCompletedOnboarding=true)};val m=vm(s);advanceUntilIdle();fill(m);m.save();advanceUntilIdle();assertFalse(m.state.value.firstSuccess)}
    @Test fun startupReadFailureDoesNotPretendDatabaseIsEmpty()=runTest(dispatcher) {val s=Store().also {it.throwRead=true};val m=vm(s);advanceUntilIdle();assertEquals(HomePage.LOADING,m.state.value.page);assertEquals("读取记录失败，请重试",m.state.value.message);s.throwRead=false;m.start();advanceUntilIdle();assertEquals(HomePage.TODAY,m.state.value.page)}

    @Test fun pendingWeeklySummaryNeverInterruptsSaveOrMarksItselfDisplayed()=runTest(dispatcher) {
        val s=Store();s.pending=WeeklySummarySnapshot("summary",1,"2026-09-03","2026-09-09",1,20.0,2.0,30.0,60.0,480.0,3.0,"[]",generatedAtUtc="2026-09-09T00:00:00Z",revision=1,isCurrent=true,supersedesSummaryId=null)
        val m=vm(s);advanceUntilIdle();fill(m);m.save();advanceUntilIdle();assertEquals(HomePage.SUCCESS,m.state.value.page);assertNull(m.state.value.weekly);assertNull(s.displayed)
        m.exitAfterSuccess{};advanceUntilIdle();assertNull(s.displayed)
    }
    @Test fun editingAndSavingOldDiaryDoesNotMoveStatisticsDateOrTodaysEntry()=runTest(dispatcher) {
        val today=diary();val old=diary("2026-09-01")
        val s=Store().apply { rows=listOf(old,today) };val m=vm(s);advanceUntilIdle()
        m.edit(old);m.comment("更正旧日记");m.save();advanceUntilIdle()
        assertEquals(java.time.LocalDate.parse("2026-09-09"),m.state.value.referenceDate)
        assertEquals(today,m.state.value.today)
        m.returnToToday();advanceUntilIdle()
        assertEquals(today,m.state.value.today)
        assertEquals(java.time.LocalDate.parse("2026-09-09"),m.state.value.referenceDate)
    }
    @Test fun recentRefreshUsesCurrentLocalDayAndLatestStoredRowsEvenWithOldForm()=runTest(dispatcher) {
        var now=java.time.Instant.parse("2026-09-09T15:59:00Z")
        val zone=java.time.ZoneId.of("Asia/Shanghai")
        val clock=object:java.time.Clock() {
            override fun instant()=now
            override fun getZone()=zone
            override fun withZone(value:java.time.ZoneId):java.time.Clock=java.time.Clock.fixed(now,value)
        }
        val s=Store().apply { rows=listOf(diary("2026-09-09")) }
        val m=DiaryViewModel(s,SavedStateHandle(),clock);advanceUntilIdle()
        m.edit(diary("2026-09-01"))
        now=java.time.Instant.parse("2026-09-09T16:01:00Z")
        s.rows=listOf(diary("2026-09-10").copy(sleepOnsetLatencyMin=40))
        m.refreshRecentRecords()
        assertEquals(java.time.LocalDate.parse("2026-09-10"),m.state.value.referenceDate)
        assertEquals(s.rows,m.state.value.entries)
        assertEquals(s.rows.single(),m.state.value.today)
        assertEquals(java.time.LocalDate.parse("2026-09-01"),m.state.value.form.referenceDate)
        val recreated=DiaryViewModel(s,SavedStateHandle(mapOf("form" to m.state.value.form,"editing" to true)),clock)
        advanceUntilIdle()
        assertEquals(java.time.LocalDate.parse("2026-09-10"),recreated.state.value.referenceDate)
    }
    @Test fun restoreDiscardsStaleUnsavedForm()=runTest(dispatcher) {val s=Store();val m=vm(s);advanceUntilIdle();m.answerNumber(DiaryField.SOL,90);val id=m.state.value.form.id;m.afterRestore();advanceUntilIdle();assertNull(m.state.value.form.sol);assertNotEquals(id,m.state.value.form.id)}

    @Test fun commentStartsEmptyAndUpdatesWithoutLosingOtherFields()=runTest(dispatcher) {
        val m=vm();advanceUntilIdle();m.beginNewDiary();assertNull(m.state.value.form.comment)
        m.answerNumber(DiaryField.SOL,15)
        for(text in listOf("中文","中文 English","中文 English 123","")) {
            m.comment(text);assertEquals(text,m.state.value.form.comment);assertEquals(15,m.state.value.form.sol)
        }
    }
    @Test fun serializedUnsavedCommentRestoresIntoFreshViewModel()=runTest(dispatcher) {
        val m=vm();advanceUntilIdle();m.beginNewDiary();m.comment("未保存 中文 abc123")
        val bytes=java.io.ByteArrayOutputStream().also{java.io.ObjectOutputStream(it).use{out->out.writeObject(m.state.value.form)}}.toByteArray()
        val restored=java.io.ObjectInputStream(java.io.ByteArrayInputStream(bytes)).use{it.readObject()} as DiaryForm
        val next=vm(handle=SavedStateHandle(mapOf("form" to restored,"editing" to true)))
        advanceUntilIdle();assertEquals(HomePage.FORM,next.state.value.page);assertEquals(m.state.value.form,next.state.value.form)
    }
    @Test fun legacySafetyRouteSavedStateIsIgnoredWhileHistoricalDataRemainsReadable()=runTest(dispatcher) {
        val s=Store();val handle=SavedStateHandle(mapOf("safetyFromSettings" to true,"safetyPage" to "SAFETY"));val expected=s.safety
        val next=vm(s,handle);advanceUntilIdle()
        assertEquals(HomePage.TODAY,next.state.value.page)
        assertEquals(expected,next.state.value.safety)
    }

}
