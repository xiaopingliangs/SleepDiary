package app.sleepdiary.offline.ui
import app.sleepdiary.offline.data.dao.AssessmentWithAnswers
import app.sleepdiary.offline.domain.model.SafetyGate
import app.sleepdiary.offline.domain.model.AssessmentStatus

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.SavedStateHandle
import app.sleepdiary.offline.clockOn
import app.sleepdiary.offline.diary
import app.sleepdiary.offline.data.entity.*
import app.sleepdiary.offline.data.repository.*
import app.sleepdiary.offline.ui.diary.*
import app.sleepdiary.offline.ui.theme.SleepDiaryTheme
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],qualifiers="w411dp-h891dp-xxhdpi")
class NavigationComposeTest {
    @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
    private class Store:DiaryUiStore {
        var rows=listOf<SleepDiaryEntry>();var fail=false;var acknowledged=false
        override suspend fun entries()=rows
        var app=AppState(hasCompletedOnboarding=true)
        var safety:AssessmentWithAnswers?=AssessmentWithAnswers(SafetyAssessment("existing","2026-09-09T00:00:00Z",derivedGate=SafetyGate.NO_MAJOR_FLAG,status=AssessmentStatus.DRAFT,isCurrent=true),emptyList())
        override suspend fun state()=app
        override suspend fun completeOnboarding(){app=app.copy(hasCompletedOnboarding=true)}
        override suspend fun currentSafety()=safety
        override suspend fun save(entry:SleepDiaryEntry,confirmed:Boolean):SaveResult =
            if(fail) SaveResult.Failure(SaveFailure.WRITE_FAILED,entry) else SaveResult.Success(entry,null)
        override suspend fun acknowledgeExplanation(){acknowledged=true}
    }
    private lateinit var model:DiaryViewModel
    private fun start(store:Store,intro:Boolean=false,onExit:()->Unit={}) {
        if(intro)store.app=store.app.copy(hasCompletedOnboarding=false)
        compose.runOnUiThread {model=DiaryViewModel(store,SavedStateHandle(mapOf("introDone" to !intro)),clockOn("2026-09-09"))}
        compose.setContent {SleepDiaryTheme {SleepDiaryApp(model,onExit)}}
        compose.waitUntil(5_000) {model.state.value.page!=HomePage.LOADING}
    }
    @Test fun threeIntroPagesThenSettingsAndHistoryAreNavigable() {
        start(Store(),intro=true)
        compose.onNodeWithText("下一页").performClick();compose.onNodeWithText("数据留在本机").assertIsDisplayed()
        compose.onNodeWithText("下一页").performClick();compose.onNodeWithText("先回答一次安全问题").assertIsDisplayed()
        compose.onNodeWithText("开始").performClick();compose.onNodeWithText("设置").performClick()
        compose.onNodeWithText("导出完整备份 JSON").assertIsEnabled()
        compose.onNodeWithText("历史记录").performClick();compose.onNodeWithText("这一天没有睡眠记录").assertIsDisplayed()
        compose.onNodeWithText("返回").performClick();compose.onNodeWithText("返回").performClick()
        compose.onNodeWithTag("time_BED").assertExists()
    }
    @Test fun failedSaveStaysInFormAndVerifiedSuccessShowsExit() {
        val store=Store().also {it.fail=true};var exited=false;start(store,onExit={exited=true})
        compose.runOnIdle {
            val f=DiaryForm.from(diary())
            listOf(DiaryField.BED,DiaryField.TRY,DiaryField.FINAL,DiaryField.OUT).forEach {model.answerTime(it,f.time(it)!!)}
            model.answerNumber(DiaryField.SOL,20);model.answerNumber(DiaryField.COUNT,2);model.answerNumber(DiaryField.WASO,30);model.answerNumber(DiaryField.QUALITY,3)
        }
        compose.onNodeWithTag("save_diary").performClick()
        compose.onNodeWithText("保存失败，请重试").assertIsDisplayed();compose.onAllNodesWithText("记录已保存").assertCountEquals(0)
        compose.runOnIdle {store.fail=false}
        compose.onNodeWithTag("save_diary").performClick();compose.onNodeWithText("记录已保存").assertIsDisplayed()
        compose.onNodeWithText(FIRST_SAVE_EXPLANATION).assertExists();compose.onNodeWithText("退出").performClick()
        compose.runOnIdle {assertTrue(exited);assertTrue(store.acknowledged)}
    }
    @Test fun todayEntryCanBeLoadedForEditingWithoutAnotherUuid() {
        val entry=diary();start(Store().also {it.rows=listOf(entry)})
        compose.onNodeWithText("今天的睡眠记录已经完成").assertIsDisplayed()
        compose.onNodeWithText("继续查看").performClick()
        compose.onNodeWithTag("time_BED").assertTextContains("09-08 23:00")
        compose.runOnIdle {assertEquals(entry.id,model.state.value.form.id);assertEquals(entry.createdAtUtc,model.state.value.form.createdAtUtc)}
    }
}
