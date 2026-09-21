package app.sleepdiary.offline.ui

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.room.Room
import app.sleepdiary.offline.application.AppServices
import app.sleepdiary.offline.clockOn
import app.sleepdiary.offline.diary
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.entity.AppState
import app.sleepdiary.offline.data.repository.RoomDiaryUiStore
import app.sleepdiary.offline.domain.model.Versions
import app.sleepdiary.offline.share.DiaryShare
import app.sleepdiary.offline.ui.diary.*
import app.sleepdiary.offline.ui.learning.*
import app.sleepdiary.offline.ui.theme.SleepDiaryTheme
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
class LearningFlowTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private lateinit var db: SleepDiaryDatabase
    private lateinit var services: AppServices
    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(compose.activity, SleepDiaryDatabase::class.java).build()
        services = AppServices(compose.activity, db)
        runBlocking {
            db.diaryDao().insert(diary())
            db.stateAndSummaryDao().putState(AppState(hasCompletedOnboarding = true, trackingStartDate = "2026-09-09"))
        }
    }
    @After fun close() { db.close() }
    private fun click(text: String) { compose.onNodeWithText(text).performScrollTo().performClick() }
    private fun waitText(text: String) {
        compose.waitUntil(10000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
    }
    private fun start() {
        lateinit var model: DiaryViewModel
        compose.runOnUiThread { model = DiaryViewModel(RoomDiaryUiStore(db), clock = clockOn("2026-09-09")) }
        compose.setContent { SleepDiaryTheme { SleepDiaryApp(model, {}, services) } }
        compose.waitUntil(5000) { compose.runOnIdle { model.state.value.page != HomePage.LOADING } }
        click("设置"); click("睡眠学习")
    }
    @Test fun readingEveryCardAndBackLeavesAllDataUnchanged() = runBlocking<Unit> {
        val before = services.backup.snapshot()
        start()
        LearningContent.cards.forEach {
            click(it.title)
            compose.onNodeWithText(it.body).assertExists()
            click("返回学习目录")
        }
        click("返回设置")
        compose.onNodeWithText("导出完整备份 JSON").assertExists()
        assertEquals(before, services.backup.snapshot())
        assertNull(db.localExecutionDao().state())
        assertEquals("NONE", Versions.CBTI_PROTOCOL)
    }
    @Test fun realNotesRouteSavesAndEditsWithBackupAndPrivacyPreserved() = runBlocking<Unit> {
        start(); click("留下自己的提醒"); click("打开个人记录")
        waitText("新建想法记录"); click("新建想法记录")
        val body = "我的提醒 中文 English 123,\"原文\"\n下一行"
        compose.onNodeWithTag("workspace_title").performTextInput("我的提醒")
        compose.onNodeWithTag("workspace_body").performTextInput(body)
        click("保存个人记录"); waitText("个人记录已保存")
        val first = services.workspace.all().single()
        assertEquals(body, first.body)
        val text = services.backup.export()
        assertEquals(listOf(first), services.backup.preview(text).workspaceRecords)
        assertFalse(services.backup.csv().toString(Charsets.UTF_8).contains("我的提醒"))
        assertFalse(DiaryShare.lines(db.diaryDao().all().single()).joinToString().contains("我的提醒"))
        click("我的提醒")
        compose.onNodeWithTag("workspace_body").assertTextContains(body)
        compose.onNodeWithTag("workspace_body").performTextReplacement("我修改后的提醒")
        click("保存个人记录"); waitText("个人记录已保存")
        assertEquals(first.id, services.workspace.all().single().id)
        assertEquals("我修改后的提醒", services.workspace.all().single().body)
        services.backup.restore(text)
        assertEquals(listOf(first), services.workspace.all())
        click("返回学习内容")
        compose.onNodeWithText(LearningContent.byId("COG-03")!!.body).assertExists()
        click("返回学习目录"); click("返回设置")
        compose.onNodeWithText("导出完整备份 JSON").assertExists()
    }
    @Test fun notesCancellationDoesNotCreateAnAnswerOrLoseTheSourcePage() = runBlocking<Unit> {
        val before = services.backup.snapshot()
        start(); click("认识睡眠担忧"); click("打开个人记录")
        waitText("新建学习笔记"); click("新建学习笔记")
        compose.onNodeWithTag("workspace_body").performTextInput("尚未保存")
        click("返回记录列表"); compose.onNodeWithText("继续编辑").performClick()
        compose.onNodeWithTag("workspace_body").assertTextContains("尚未保存")
        click("返回记录列表"); compose.onNodeWithText("放弃编辑").performClick(); click("返回学习内容")
        compose.onNodeWithText(LearningContent.SAFETY_TEXT).assertExists()
        assertEquals(before, services.backup.snapshot())
    }
    @Test fun systemBackReturnsFromDetailThenToSettings() {
        start(); click("认识睡眠担忧")
        compose.waitForIdle(); compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("返回设置").assertExists()
        compose.waitForIdle(); compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("导出完整备份 JSON").assertExists()
    }
    @Test fun selectedContentSurvivesSavedStateRestorationWithoutWriting() = runBlocking<Unit> {
        val tester = StateRestorationTester(compose)
        tester.setContent { LearningScreen({}, {}, {}) }
        click("为以后做好准备")
        tester.emulateSavedInstanceStateRestore()
        compose.onNodeWithText(LearningContent.byId("MAINT-01")!!.body).assertExists()
        assertTrue(services.workspace.all().isEmpty())
    }
    @Test fun sourceIsExplicitBrowserActionAndUnknownLinksAreRejected() {
        var opened: String? = null
        compose.setContent { LearningScreen({}, {}, { opened = it }) }
        click("放松不是一场考试")
        assertNull(opened)
        click("用浏览器查看官方来源")
        val intent = officialLinkIntent(checkNotNull(opened))
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals(LearningContent.SOURCE_URL, intent.data.toString())
        assertTrue(runCatching { officialLinkIntent("https://example.com/untrusted") }.isFailure)
    }
    @Test fun catalogHasStableUniqueIdsAndSourcePagesWithinManual() {
        assertEquals(6, LearningContent.cards.size)
        assertEquals(LearningContent.cards.size, LearningContent.cards.map { it.id }.toSet().size)
        LearningContent.cards.forEach {
            assertTrue(it.title.isNotBlank() && it.body.isNotBlank())
            assertTrue(it.pages.isNotEmpty() && it.pages.all { page -> page in 1..114 })
        }
        assertNull(LearningContent.byId("unknown"))
        assertEquals(listOf(63), LearningContent.byId("COG-SAFETY-01")!!.pages)
    }
}
