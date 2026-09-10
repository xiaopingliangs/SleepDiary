package app.sleepdiary.offline.ui

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.sleepdiary.offline.diary
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.entity.AppState
import app.sleepdiary.offline.data.repository.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class) @Config(sdk=[28])
class RoomUiStoreTest {
    private lateinit var db:SleepDiaryDatabase
    @Before fun setup(){db=Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(),SleepDiaryDatabase::class.java).build()}
    @After fun close(){db.close()}
    @Test fun uiAdapterUsesFormalSaveAndReadsStoredUuid()=runBlocking {
        val store=RoomDiaryUiStore(db);val input=diary()
        val result=store.save(input,false) as SaveResult.Success
        assertEquals(result.entry,db.diaryDao().byId(input.id));assertEquals(input.id,store.entries().single().id)
    }
    @Test fun explanationFlagStaysInRoomAndPreservesOtherState()=runBlocking {
        val before=AppState(trackingStartDate="2026-09-09",hasSeenSharePrivacyNotice=true,lastDisplayedWeeklyPeriodIndex=4)
        db.stateAndSummaryDao().putState(before)
        RoomDiaryUiStore(db).acknowledgeExplanation()
        assertEquals(before.copy(hasSeenMinimalInteractionExplanation=true),db.stateAndSummaryDao().state())
    }
    @Test fun warningStillRequiresConfirmationAtRepositoryBoundary()=runBlocking {
        val store=RoomDiaryUiStore(db);val input=diary().copy(sleepOnsetLatencyMin=400)
        assertTrue(store.save(input,false) is SaveResult.ValidationWarning);assertTrue(store.entries().isEmpty())
        val result=store.save(input,true) as SaveResult.Success
        assertTrue(result.entry.consistencyWarning);assertEquals(400,result.entry.sleepOnsetLatencyMin)
    }

    @Test fun commentPersistsAndLoadsForEditingWithSameUuid()=runBlocking {
        val store=RoomDiaryUiStore(db)
        val input=diary().copy(comment="中文 English 123")
        val saved=store.save(input,false) as SaveResult.Success
        val loaded=store.entries().single()
        assertEquals(input.comment,app.sleepdiary.offline.ui.diary.DiaryForm.from(loaded).comment)
        val updated=store.save(loaded.copy(comment="修改后 中文 abc456"),false) as SaveResult.Success
        assertEquals(saved.entry.id,updated.entry.id)
        assertEquals("修改后 中文 abc456",db.diaryDao().byId(input.id)!!.comment)
        assertEquals(saved.entry.createdAtUtc,updated.entry.createdAtUtc)
    }

    @Test fun clearingCommentPersistsEmptyAndNullWithoutChangingIdentity()=runBlocking {
        val store=RoomDiaryUiStore(db);val first=store.save(diary().copy(comment="旧备注"),false) as SaveResult.Success
        val cleared=store.save(first.entry.copy(comment=""),false) as SaveResult.Success
        assertEquals("",db.diaryDao().byId(first.entry.id)!!.comment)
        val absent=store.save(cleared.entry.copy(comment=null),false) as SaveResult.Success
        assertNull(db.diaryDao().byId(first.entry.id)!!.comment)
        assertEquals(first.entry.id,absent.entry.id);assertEquals(1,store.entries().size)
    }
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test fun returningFromSafetyKeepsPersistedDraftAndAnswers()=runBlocking {
        kotlinx.coroutines.Dispatchers.setMain(kotlinx.coroutines.Dispatchers.Unconfined)
        try {
            val store=RoomDiaryUiStore(db);store.completeOnboarding()
            val id=java.util.UUID.randomUUID().toString()
            val expected=store.saveSafety(id,listOf(app.sleepdiary.offline.data.entity.SafetyAnswer(id,"S01","YES",detailJson=null)),false)
            val model=app.sleepdiary.offline.ui.diary.DiaryViewModel(store)
            model.state.first{it.page!=app.sleepdiary.offline.ui.diary.HomePage.LOADING}
            model.openSafety(fromSettings=true)
            val returned=kotlinx.coroutines.CompletableDeferred<Unit>()
            model.returnFromSafety{returned.complete(Unit)};returned.await()
            assertEquals(expected,store.currentSafety());assertEquals(expected,db.safetyDao().byId(id))
        } finally {kotlinx.coroutines.Dispatchers.resetMain()}
    }

}
