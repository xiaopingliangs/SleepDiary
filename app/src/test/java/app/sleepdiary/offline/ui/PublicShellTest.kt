package app.sleepdiary.offline.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.SavedStateHandle
import app.sleepdiary.offline.clockOn
import app.sleepdiary.offline.diary
import app.sleepdiary.offline.ui.diary.DiaryViewModel
import app.sleepdiary.offline.ui.diary.HomePage
import app.sleepdiary.offline.data.dao.AssessmentWithAnswers
import app.sleepdiary.offline.data.entity.*
import app.sleepdiary.offline.data.repository.SaveResult
import app.sleepdiary.offline.data.repository.DiaryUiStore as StoreContract
import app.sleepdiary.offline.ui.publicnav.PublicDestination
import app.sleepdiary.offline.ui.publicnav.ContextualEducationPreference
import app.sleepdiary.offline.data.entity.SleepDiaryEntry
import java.util.UUID
import app.sleepdiary.offline.ui.theme.SleepDiaryTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],qualifiers="w411dp-h891dp-xxhdpi")
class PublicShellTest {
    @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()

    private class Store:StoreContract {
        var safetySaveCalls = 0
        var rows: List<SleepDiaryEntry> = emptyList()
        override suspend fun entries()=rows
        override suspend fun state()=AppState(hasCompletedOnboarding=true)
        override suspend fun currentSafety():AssessmentWithAnswers?=null
        override suspend fun saveSafety(id:String,answers:List<SafetyAnswer>,completed:Boolean):AssessmentWithAnswers { safetySaveCalls++; error("Public UI must not save Safety") }
        override suspend fun completeOnboarding(){}
        override suspend fun save(entry:SleepDiaryEntry,confirmed:Boolean):SaveResult=SaveResult.Success(entry,null)
        override suspend fun acknowledgeExplanation(){}
    }

    @Test fun onlyFourPublicDestinationsExistAndNoTreatmentTabIsRegistered(){
        assertEquals(listOf("today","sleep","education","me"),PublicDestination.entries.map{it.route})
        assertEquals(listOf("今日","历史","了解","我的"),PublicDestination.entries.map{it.label})
        assertFalse(PublicDestination.entries.any{it.route.contains("treatment",ignoreCase=true)})
    }

    @Test fun rootTabsPreserveDistinctDestinationsAndEducationShellHasReturnPath(){
        lateinit var model:DiaryViewModel
        compose.runOnUiThread{model=DiaryViewModel(Store(),SavedStateHandle(),clockOn("2026-09-09"))}
        compose.setContent{SleepDiaryTheme{SleepDiaryApp(model,{})}}
        compose.waitUntil(5000){model.state.value.page!=HomePage.LOADING}
        compose.onNodeWithTag("public_tab_today").assertIsSelected()
        compose.onNodeWithTag("public_tab_sleep").performClick()
        compose.onNodeWithTag("public_tab_sleep").assertIsSelected()
        compose.onNodeWithText("最近平均数据").assertExists()
        compose.onNodeWithText("最近 14 天").performClick()
        compose.onNodeWithText("有记录的天数：0 / 14").assertExists()
        compose.onNodeWithTag("public_tab_education").performClick()
        compose.onNodeWithTag("public_tab_education").assertIsSelected()
        compose.onNodeWithText("← 返回").performClick()
        compose.onNodeWithTag("public_tab_today").assertIsSelected()
        compose.onNodeWithTag("public_tab_education").performClick()
        compose.onNodeWithTag("public_tab_me").performClick()
        compose.onNodeWithText("隐私说明").assertExists()
        compose.onNodeWithTag("public_tab_today").performClick()
        compose.onNodeWithText("记录昨晚睡眠").assertExists()
    }

    @Test fun publicNavigationHasNoSafetyWorkflowOrAssessmentWriteAction(){
        val store=Store()
        lateinit var model:DiaryViewModel
        compose.runOnUiThread{model=DiaryViewModel(store,SavedStateHandle(),clockOn("2026-09-09"))}
        compose.setContent{SleepDiaryTheme{SleepDiaryApp(model,{})}}
        compose.waitUntil(5000){model.state.value.page!=HomePage.LOADING}
        for(tag in listOf("public_tab_today","public_tab_sleep","public_tab_education","public_tab_me")) {
            compose.onNodeWithTag(tag).performClick()
            compose.onAllNodesWithText("重新填写安全问询").assertCountEquals(0)
            compose.onAllNodesWithText("查看当前安全问询").assertCountEquals(0)
            compose.onAllNodesWithText("继续未完成的安全问询").assertCountEquals(0)
            compose.onAllNodesWithText("安全问询 ·",substring=true).assertCountEquals(0)
        }
        assertEquals(0,store.safetySaveCalls)
    }

    @Test fun recentDiaryShowsOneLocalEducationSuggestionAndOptOutPersistsWithoutChangingDiary(){
        val source = diary("2026-09-09", UUID.nameUUIDFromBytes("context-source".toByteArray()).toString()).copy(
            bedEnteredAtLocal = "2026-09-08T23:30",
            trySleepAtLocal = "2026-09-09T00:00",
            finalAwakeningAtLocal = "2026-09-09T04:00",
            outOfBedAtLocal = "2026-09-09T07:00",
            sleepOnsetLatencyMin = 30,
            wakeAfterSleepOnsetMin = 30
        )
        val store = Store().apply { rows = listOf(source) }
        lateinit var model: DiaryViewModel
        compose.runOnUiThread { model = DiaryViewModel(store, SavedStateHandle(), clockOn("2026-09-09")) }
        compose.setContent { SleepDiaryTheme { SleepDiaryApp(model, {}) } }
        compose.waitUntil(5000) { model.state.value.page != HomePage.LOADING }
        compose.onNodeWithTag("today_contextual_education").assertExists()
        compose.onNodeWithText("一晚睡得很少，第二天真的一定会很糟吗？").assertExists()
        compose.onNodeWithText("为什么会看到这个？").performClick()
        compose.onNodeWithText("匹配只在本机完成，不会上传。这不是诊断，也不是治疗建议。", substring = true).assertExists()
        compose.onNodeWithText("知道了").performClick()
        compose.onNodeWithText("了解一下").performClick()
        compose.waitUntil(5000) { compose.onAllNodesWithText("一晚睡差，第二天真的一定完了吗？").fetchSemanticsNodes().isNotEmpty() }
        assertEquals(listOf(source), store.rows)
        assertEquals(0, store.safetySaveCalls)

        compose.onNodeWithTag("public_tab_me").performClick()
        compose.onNodeWithTag("record_based_matching_toggle").performClick()
        compose.onNodeWithTag("public_tab_today").performClick()
        compose.onNodeWithTag("today_contextual_education").assertDoesNotExist()
        assertFalse(ContextualEducationPreference(compose.activity).isEnabled())
        assertEquals(listOf(source), store.rows)
    }
}
