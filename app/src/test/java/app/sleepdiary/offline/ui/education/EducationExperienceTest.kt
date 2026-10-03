package app.sleepdiary.offline.ui.education

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.room.Room
import app.sleepdiary.offline.application.AppServices
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.domain.model.Versions
import app.sleepdiary.offline.ui.theme.SleepDiaryTheme
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
class EducationExperienceTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private lateinit var db: SleepDiaryDatabase
    private lateinit var services: AppServices
    private lateinit var catalog: PublicEducationCatalog

    @Before fun start() {
        db = Room.inMemoryDatabaseBuilder(compose.activity, SleepDiaryDatabase::class.java).build()
        services = AppServices(compose.activity, db)
        catalog = PublicEducationCatalog.fromAssets(compose.activity.assets)
        compose.setContent { SleepDiaryTheme { PublicEducationExperience(services.education, services.workspace) } }
        compose.waitUntil(5000) { compose.onAllNodesWithText("知识地图").fetchSemanticsNodes().isNotEmpty() }
    }

    @After fun close() = db.close()

    private fun openScene(id: String) {
        val scene = catalog.node(id)
        val chapter = catalog.node(checkNotNull(scene.parentId))
        compose.onNodeWithText("${chapter.title}　·　未开始", substring = true).performScrollTo().performClick()
        compose.onNodeWithTag("education_scene_${scene.id}").performScrollTo().performClick()
        compose.waitUntil(5000) { runBlocking {
            db.educationDao().position(catalog.versionId, "COURSE-V4")?.sceneContentId == scene.id &&
                db.educationDao().progress(catalog.versionId, scene.id)?.state == "IN_PROGRESS"
        } }
    }

    private fun sceneWith(kind: String, predicate: (List<PublicEducationNode>) -> Boolean = { it.isNotEmpty() }): PublicEducationNode =
        catalog.nodes.values.filter { it.kind == "SCENE" }.first { scene -> predicate(catalog.children(scene.id).filter { it.kind == kind }) }

    @Test fun openingSceneUpdatesResumePositionButNeverMarksItCompleted() {
        val scene = sceneWith("CONTENT_BLOCK")
        openScene(scene.id)
        assertTrue(compose.onAllNodesWithText(scene.title).fetchSemanticsNodes().isNotEmpty())
        val state = runBlocking { services.education.snapshot() }
        assertEquals(scene.id, state.positions.single { it.courseId == "COURSE-V4" }.sceneContentId)
        assertTrue(state.progress.none { it.contentId == scene.id && it.state == "COMPLETED" })
        assertFalse(state.progress.any { it.contentId == scene.id && it.state == "COMPLETED" })
        assertTrue(state.progress.any { it.contentId == scene.id && it.state == "IN_PROGRESS" })
    }

    @Test fun completionHasExplicitMilestoneAndContinueMovesPosition() {
        val scene = sceneWith("CONTENT_BLOCK")
        openScene(scene.id)
        compose.onNodeWithTag("education_complete_${scene.id}").performScrollTo().performClick()
        compose.waitUntil(5000) { runBlocking { services.education.snapshot().progress.any { it.contentId == scene.id && it.state == "COMPLETED" } } }
        val data = runBlocking { services.education.snapshot() }
        assertEquals("COMPLETED", data.progress.single { it.contentId == scene.id }.state)
        val next = scene.nextId
        if (next != null && catalog.node(next).parentId == scene.parentId) {
            compose.waitUntil(5000) { runBlocking { db.educationDao().position(catalog.versionId, "COURSE-V4")?.sceneContentId == next } }
        }
    }

    @Test fun choicePreviewIsTransientThenExplicitCommitPersistsAndReplaces() {
        val scene = sceneWith("CHOICE") { choices -> choices.any { catalog.children(it.id).any { row -> row.kind == "REVIEW_ITEM" } } }
        val choices = catalog.children(scene.id).filter { it.kind == "CHOICE" }
        val first = choices.first { catalog.children(it.id).any { row -> row.kind == "REVIEW_ITEM" } }
        val second = choices.first { it.id != first.id }
        openScene(scene.id)
        val beforePreview = runBlocking { services.education.snapshot() }
        compose.onNodeWithText(first.title).performScrollTo().performClick()
        assertEquals(beforePreview, runBlocking { services.education.snapshot() })
        compose.onNodeWithText("尚未写入学习记录", substring = true).assertExists()
        val reviewItem = catalog.children(first.id).first { it.kind == "REVIEW_ITEM" }
        compose.onNodeWithText(reviewItem.title).assertExists()
        compose.onNodeWithTag("education_commit_${scene.id}").performScrollTo().performClick()
        compose.waitUntil(5000) { runBlocking { services.education.snapshot().committedChoices.isNotEmpty() } }
        assertEquals(first.id, runBlocking { services.education.snapshot().committedChoices.single().choiceContentId })
        compose.onNodeWithText(second.title).performScrollTo().performClick()
        assertEquals(first.id, runBlocking { services.education.snapshot().committedChoices.single().choiceContentId })
        compose.onNodeWithTag("education_commit_${scene.id}").performScrollTo().performClick()
        compose.waitUntil(5000) { runBlocking { services.education.snapshot().committedChoices.singleOrNull()?.choiceContentId == second.id } }
        assertEquals(second.id, runBlocking { services.education.snapshot().committedChoices.single().choiceContentId })
    }

    @Test fun reviewControlsAreExplicitAndDurable() {
        val scene = sceneWith("CONTENT_BLOCK")
        openScene(scene.id)
        compose.onNodeWithTag("education_review_toggle_${scene.id}").performScrollTo().performClick()
        compose.waitUntil(5000) { runBlocking { services.education.snapshot().reviewStates.any { it.contentId == scene.id && it.reviewState == "SAVED_FOR_REVIEW" } } }
        compose.waitUntil(5000) { compose.onAllNodesWithText("已复习").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("education_mark_reviewed_${scene.id}").performScrollTo().performClick()
        compose.waitUntil(5000) { runBlocking { services.education.snapshot().reviewStates.any { it.contentId == scene.id && it.reviewState == "REVIEWED" } } }
        // Once reviewed, the scene action becomes "稍后复习"; pressing it clears
        // the existing REVIEWED state (the button label is intentionally neutral).
        compose.onNodeWithTag("education_review_toggle_${scene.id}").performScrollTo().performClick()
        compose.waitUntil(5000) { runBlocking { services.education.snapshot().reviewStates.none { it.contentId == scene.id } } }
    }

    @Test fun authoredNoteCanBeSavedEditedAndUnlinkedWithoutDeletingWorkspaceText() {
        val scene = sceneWith("NOTE_PROMPT")
        val prompt = catalog.children(scene.id).first { it.kind == "NOTE_PROMPT" }
        openScene(scene.id)
        compose.onNodeWithText("写一条个人记录：${prompt.title}").performScrollTo().performClick()
        compose.onNodeWithTag("education_note_${prompt.id}").performTextInput("我的学习记录")
        compose.onNodeWithText("保存到我的学习记录").performScrollTo().performClick()
        compose.waitUntil(5000) { runBlocking { db.workspaceDao().all().any { it.body == "我的学习记录" } } }
        val note = runBlocking { db.workspaceDao().all().single { it.body == "我的学习记录" } }
        assertEquals(note.id, runBlocking { db.educationDao().record(note.id)!!.workspaceRecordId })
        compose.onNodeWithText("编辑这条个人记录：${prompt.title}").performScrollTo().performClick()
        compose.onNodeWithTag("education_note_${prompt.id}").performTextReplacement("修改后的个人记录")
        compose.onNodeWithText("保存修改").performScrollTo().performClick()
        compose.waitUntil(5000) { runBlocking { db.workspaceDao().byId(note.id)?.body == "修改后的个人记录" } }
        compose.onNodeWithText("← 返回").performClick() // Scene -> chapter
        compose.onNodeWithText("← 返回").performClick() // Chapter -> map
        compose.onNodeWithText("学习记录与笔记").performScrollTo().performClick()
        compose.onNodeWithText("只移除学习关联，保留个人记录").performScrollTo().performClick()
        compose.waitUntil(5000) { runBlocking { db.educationDao().record(note.id) == null } }
        assertEquals("修改后的个人记录", runBlocking { db.workspaceDao().byId(note.id)!!.body })
    }

    @Test fun editorStateAndPositionRemainRoomBackedAfterActivityRecreation() {
        val scene = sceneWith("CONTENT_BLOCK")
        openScene(scene.id)
        compose.activityRule.scenario.recreate()
        compose.setContent { SleepDiaryTheme { PublicEducationExperience(services.education, services.workspace) } }
        compose.waitUntil(5000) { compose.onAllNodesWithText("知识地图").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("继续上次学习").performScrollTo().performClick()
        compose.waitUntil(5000) { compose.onAllNodesWithText(scene.title).fetchSemanticsNodes().isNotEmpty() }
        assertEquals(scene.id, runBlocking { services.education.snapshot().positions.single().sceneContentId })
    }
}
