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
import app.sleepdiary.offline.ui.education.PublicEducationCatalog
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
    private lateinit var catalog: PublicEducationCatalog
    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(compose.activity, SleepDiaryDatabase::class.java).build()
        services = AppServices(compose.activity, db)
        catalog = PublicEducationCatalog.fromAssets(compose.activity.assets)
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
        compose.onNodeWithTag("public_tab_education").performClick()
    }
    private fun openScene(id: String) {
        compose.onNodeWithText("看看完整知识地图").performScrollTo().performClick()
        val scene = catalog.node(id)
        val chapter = catalog.node(checkNotNull(scene.parentId))
        compose.onNodeWithText("${chapter.title}　·　未开始", substring = true).performScrollTo().performClick()
        compose.onNodeWithTag("education_scene_${scene.id}").performScrollTo().performClick()
        compose.waitUntil(5000) { runBlocking {
            db.educationDao().position(catalog.versionId, "COURSE-V4")?.sceneContentId == id
        } }
    }
    @Test fun readingCandidateSceneOnlyCreatesPositionAndInProgressState() = runBlocking<Unit> {
        val diaryBefore = db.diaryDao().all(); val safetyBefore = db.safetyDao().all()
        val treatmentBefore = db.treatmentDao().episodes(); val workspaceBefore = services.workspace.all()
        start()
        val scene = catalog.nodes.values.first { it.kind == "SCENE" && catalog.children(it.id).any { child -> child.kind == "CONTENT_BLOCK" } }
        openScene(scene.id)
        assertTrue(compose.onAllNodesWithText(scene.title).fetchSemanticsNodes().isNotEmpty())
        compose.onNodeWithText("← 返回").performClick(); compose.onNodeWithText("← 返回").performClick()
        assertEquals(diaryBefore, db.diaryDao().all()); assertEquals(safetyBefore, db.safetyDao().all())
        assertEquals(treatmentBefore, db.treatmentDao().episodes()); assertEquals(workspaceBefore, services.workspace.all())
        assertTrue(services.education.snapshot().progress.none { it.contentId == scene.id })
        assertEquals(scene.id, services.education.snapshot().positions.single().sceneContentId)
        assertTrue(services.education.snapshot().committedChoices.isEmpty())
        assertNull(db.localExecutionDao().state())
        assertEquals("NONE", Versions.CBTI_PROTOCOL)
    }
    @Test fun realNotesRouteSavesAndEditsWithBackupAndPrivacyPreserved() = runBlocking<Unit> {
        start()
        val prompt = catalog.nodes.values.first { it.kind == "NOTE_PROMPT" }
        val scene = catalog.node(checkNotNull(prompt.parentId))
        openScene(scene.id)
        compose.onNodeWithTag("education_note_toggle_${prompt.id}").performScrollTo().performClick()
        val body = "我的提醒 中文 English 123,\"原文\"\n下一行"
        compose.onNodeWithTag("education_note_${prompt.id}").performTextInput(body)
        click("保存到我的学习记录")
        compose.waitUntil(5000) { runBlocking { services.workspace.all().any { it.body == body } } }
        compose.waitUntil(5000) { compose.onAllNodesWithTag("education_note_toggle_${prompt.id}").fetchSemanticsNodes().isNotEmpty() }
        val first = services.workspace.all().single()
        assertEquals(body, first.body)
        val text = services.backup.export()
        assertEquals(listOf(first), services.backup.preview(text).workspaceRecords)
        assertFalse(services.backup.csv().toString(Charsets.UTF_8).contains("我的提醒"))
        assertFalse(DiaryShare.lines(db.diaryDao().all().single()).joinToString().contains("我的提醒"))
        compose.onNodeWithTag("education_note_toggle_${prompt.id}").performScrollTo().performClick()
        compose.onNodeWithTag("education_note_${prompt.id}").assertTextContains(body)
        compose.onNodeWithTag("education_note_${prompt.id}").performTextReplacement("我修改后的提醒")
        click("保存修改")
        compose.waitUntil(5000) { runBlocking { services.workspace.all().singleOrNull()?.body == "我修改后的提醒" } }
        assertEquals(first.id, services.workspace.all().single().id)
        assertEquals("我修改后的提醒", services.workspace.all().single().body)
        services.backup.restore(text)
        assertEquals(listOf(first), services.workspace.all())
        compose.onNodeWithText("← 返回").performClick(); compose.onNodeWithText("← 返回").performClick()
        compose.onNodeWithTag("public_tab_me").performClick(); compose.onNodeWithTag("public_me_workspace").performScrollTo().performClick()
        click(prompt.title)
        compose.onNodeWithTag("workspace_body").assertTextContains(body)
    }
    @Test fun notesCancellationDoesNotCreateAnAnswerOrLoseTheSourcePage() = runBlocking<Unit> {
        start()
        val prompt = catalog.nodes.values.first { it.kind == "NOTE_PROMPT" }
        val scene = catalog.node(checkNotNull(prompt.parentId)); openScene(scene.id)
        compose.onNodeWithTag("education_note_toggle_${prompt.id}").performScrollTo().performClick()
        compose.onNodeWithTag("education_note_${prompt.id}").performTextInput("尚未保存")
        click("收起"); assertTrue(compose.onAllNodesWithText(scene.title, substring = true).fetchSemanticsNodes().isNotEmpty())
        compose.onNodeWithText("← 返回").performClick(); compose.onNodeWithText("← 返回").performClick()
        compose.onNodeWithText("← 返回").performClick()
        assertTrue(services.workspace.all().isEmpty())
        assertTrue(services.education.snapshot().records.isEmpty())
    }
    @Test fun systemBackReturnsFromEducationToToday() {
        start()
        compose.waitForIdle(); compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithTag("public_tab_today").assertIsSelected()
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
