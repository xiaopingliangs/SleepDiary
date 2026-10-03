package app.sleepdiary.offline.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.SavedStateHandle
import app.sleepdiary.offline.clockOn
import app.sleepdiary.offline.ui.diary.DiaryViewModel
import app.sleepdiary.offline.ui.diary.HomePage
import app.sleepdiary.offline.data.dao.AssessmentWithAnswers
import app.sleepdiary.offline.data.entity.*
import app.sleepdiary.offline.data.repository.SaveResult
import app.sleepdiary.offline.data.repository.DiaryUiStore as StoreContract
import app.sleepdiary.offline.ui.publicnav.PublicDestination
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
        override suspend fun entries()=emptyList<SleepDiaryEntry>()
        override suspend fun state()=AppState(hasCompletedOnboarding=true)
        override suspend fun currentSafety():AssessmentWithAnswers?=null
        override suspend fun completeOnboarding(){}
        override suspend fun save(entry:SleepDiaryEntry,confirmed:Boolean):SaveResult=SaveResult.Success(entry,null)
        override suspend fun acknowledgeExplanation(){}
    }

    @Test fun onlyFourPublicDestinationsExistAndNoTreatmentTabIsRegistered(){
        assertEquals(listOf("today","sleep","education","me"),PublicDestination.entries.map{it.route})
        assertEquals(listOf("今天","睡眠","学习","我的"),PublicDestination.entries.map{it.label})
        assertFalse(PublicDestination.entries.any{it.route.contains("treatment",ignoreCase=true)})
    }

    @Test fun rootTabsPreserveDistinctDestinationsAndEducationShellHasReturnPath(){
        lateinit var model:DiaryViewModel
        compose.runOnUiThread{model=DiaryViewModel(Store(),SavedStateHandle(),clockOn("2026-09-09"))}
        compose.setContent{SleepDiaryTheme{SleepDiaryApp(model,{})}}
        compose.waitUntil(5000){model.state.value.page!=HomePage.LOADING}
        compose.onNodeWithTag("public_tab_today").assertIsSelected()
        compose.onNodeWithTag("public_tab_sleep").performClick()
        compose.onNodeWithText("日记历史").assertExists()
        compose.onNodeWithTag("public_tab_education").performClick()
        compose.onNodeWithText("知识地图").performClick()
        compose.onNodeWithText("← 返回学习").performClick()
        compose.onNodeWithTag("public_tab_education").assertIsSelected()
        compose.onNodeWithTag("public_tab_me").performClick()
        compose.onNodeWithText("隐私说明").assertExists()
        compose.onNodeWithTag("public_tab_today").performClick()
        compose.onNodeWithText("记录今天的睡眠").assertExists()
    }
}
