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
    private val sharedSummaryTitles = mutableListOf<String>()

    @Before fun start() {
        db = Room.inMemoryDatabaseBuilder(compose.activity, SleepDiaryDatabase::class.java).build()
        services = AppServices(compose.activity, db)
        catalog = PublicEducationCatalog.fromAssets(compose.activity.assets)
        compose.setContent { SleepDiaryTheme { PublicEducationExperience(services.education, services.workspace, onShareSummary = { sharedSummaryTitles += it }) } }
        compose.waitUntil(5000) { compose.onAllNodesWithText("了解睡眠").fetchSemanticsNodes().isNotEmpty() }
    }

    @After fun close() = db.close()

    private fun openScene(id: String) {
        compose.onNodeWithText("看看完整知识地图").performScrollTo().performClick()
        val scene = catalog.node(id)
        val chapter = catalog.node(checkNotNull(scene.parentId))
        compose.onNodeWithText("${chapter.title}　·　未开始", substring = true).performScrollTo().performClick()
        compose.onNodeWithTag("education_scene_${scene.id}").performScrollTo().performClick()
        compose.waitUntil(5000) { runBlocking {
            db.educationDao().position(catalog.versionId, "COURSE-V4")?.sceneContentId == scene.id
        } }
    }

    private fun sceneWith(kind: String, predicate: (List<PublicEducationNode>) -> Boolean = { it.isNotEmpty() }): PublicEducationNode =
        catalog.nodes.values.filter { it.kind == "SCENE" }.first { scene -> predicate(catalog.children(scene.id).filter { it.kind == kind }) }

    @Test fun openingNavigatingAndResumingSceneOnlyChangesPosition() {
        val scene = sceneWith("CONTENT_BLOCK")
        openScene(scene.id)
        assertTrue(compose.onAllNodesWithText(scene.title).fetchSemanticsNodes().isNotEmpty())
        val beforeClose = runBlocking { services.education.snapshot() }
        assertEquals(scene.id, beforeClose.positions.single { it.courseId == "COURSE-V4" }.sceneContentId)
        assertTrue(beforeClose.progress.isEmpty())
        compose.onNodeWithText("← 返回").performClick()
        compose.onNodeWithText("← 返回").performClick()
        compose.activityRule.scenario.recreate()
        compose.setContent { SleepDiaryTheme { PublicEducationExperience(services.education, services.workspace) } }
        compose.waitUntil(5000) { compose.onAllNodesWithText("了解睡眠").fetchSemanticsNodes().isNotEmpty() }
        compose.waitUntil(5000) { compose.onAllNodesWithText("继续上次").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("继续上次").performScrollTo().performClick()
        val resumedTitle = EducationExperienceDefinitions.all.firstOrNull { it.sceneId == scene.id }?.title ?: scene.title
        compose.waitUntil(5000) { compose.onAllNodesWithText(resumedTitle).fetchSemanticsNodes().isNotEmpty() }
        val afterReopen = runBlocking { services.education.snapshot() }
        assertEquals(scene.id, afterReopen.positions.single { it.courseId == "COURSE-V4" }.sceneContentId)
        assertEquals(beforeClose.progress, afterReopen.progress)
        assertTrue(afterReopen.progress.isEmpty())
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
        val afterPreview = runBlocking { services.education.snapshot() }
        assertEquals(beforePreview.progress, afterPreview.progress)
        assertEquals(beforePreview.committedChoices, afterPreview.committedChoices)
        assertEquals(beforePreview.reviewStates, afterPreview.reviewStates)
        assertEquals(beforePreview.records, afterPreview.records)
        assertEquals(scene.id, afterPreview.positions.single().sceneContentId)
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
        compose.waitUntil(5000) { compose.onAllNodesWithText("编辑这条个人记录：${prompt.title}").fetchSemanticsNodes().isNotEmpty() }
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
        compose.waitUntil(5000) { compose.onAllNodesWithText("了解睡眠").fetchSemanticsNodes().isNotEmpty() }
        compose.waitUntil(5000) { compose.onAllNodesWithText("继续上次").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("继续上次").performScrollTo().performClick()
        val resumedTitle = EducationExperienceDefinitions.all.firstOrNull { it.sceneId == scene.id }?.title ?: scene.title
        compose.waitUntil(5000) { compose.onAllNodesWithText(resumedTitle).fetchSemanticsNodes().isNotEmpty() }
        assertEquals(scene.id, runBlocking { services.education.snapshot().positions.single().sceneContentId })
    }

    @Test fun understandHomeStartsWithFourCuratedExperiencesAndKeepsKnowledgeMapOptional() = runBlocking<Unit> {
        assertEquals(4, EducationExperienceDefinitions.all.size)
        EducationExperienceDefinitions.validate(catalog)
        assertEquals(listOf("CBT-I 到底是什么？", "一晚睡差，第二天真的一定完了吗？", "为什么越努力睡，反而越清醒？", "为什么固定起床时间这么重要？"), EducationExperienceDefinitions.all.map { it.title })
        compose.onNodeWithText("了解睡眠").assertExists()
        EducationExperienceDefinitions.all.forEach { compose.onNodeWithTag("experience_${it.id}").assertExists() }
        compose.onAllNodesWithText("一般睡眠教育").assertCountEquals(0)
        compose.onAllNodesWithText("11 章").assertCountEquals(0)
        assertTrue(services.education.snapshot().progress.isEmpty())
    }

    @Test fun firstFourExperiencesRenderAuthoredScreensAndReuseValidatedV4Ids() {
        EducationExperienceDefinitions.all.forEach { experience ->
            experience.contentIds.forEach(catalog::node)
            compose.onNodeWithTag("experience_${experience.id}").performScrollTo().performClick()
            compose.onNodeWithText(experience.title).assertExists()
            compose.onAllNodesWithText("PUBLIC_WITH_PRESENTATION_ADAPTATION").assertCountEquals(0)
            compose.onAllNodesWithText("一般睡眠教育").assertCountEquals(0)
            compose.onNodeWithText("← 返回").performClick()
        }
    }

    @Test fun previewIsTransientUntilUserConfirmsAndExplicitCompletePersists() = runBlocking<Unit> {
        compose.onNodeWithTag("experience_morning_time").performScrollTo().performClick()
        val prompt = checkNotNull(EducationExperienceDefinitions.all.first { it.id == "morning_time" }.choicePromptId)
        val fixed = catalog.children(prompt).first { it.id == "CHOICE-CH01-PLAN-FIXED" }
        compose.onNodeWithTag("experience_choice_${fixed.id}").performScrollTo().performClick()
        compose.onNodeWithTag("experience_preview").assertExists()
        assertTrue(services.education.snapshot().committedChoices.isEmpty())
        assertTrue(services.education.snapshot().progress.isEmpty())
        compose.onNodeWithTag("experience_commit").performScrollTo().performClick()
        compose.waitUntil(5000) { runBlocking { services.education.snapshot().committedChoices.any { it.promptContentId == prompt && it.choiceContentId == fixed.id } } }
        assertEquals(1, services.education.snapshot().committedChoices.size)
        assertTrue(services.education.snapshot().progress.isEmpty())
        compose.onNodeWithTag("experience_complete_morning_time").performScrollTo().performClick()
        compose.waitUntil(5000) { runBlocking { services.education.snapshot().progress.any { it.contentId == "SCENE-CH01-WHY-MORNING" && it.state == "COMPLETED" } } }
        assertEquals("NONE", Versions.CBTI_PROTOCOL)
        assertTrue(db.treatmentDao().episodes().isEmpty())
        assertTrue(db.diaryDao().all().isEmpty())
        assertTrue(db.safetyDao().all().isEmpty())
    }

    @Test fun optionalReflectionIsOnlySavedOnItsExplicitSaveAction() = runBlocking<Unit> {
        compose.onNodeWithTag("experience_bad_night").performScrollTo().performClick()
        val body = "今天仍然完成了一件事"
        compose.onNodeWithTag("experience_reflection").performTextInput(body)
        assertTrue(services.workspace.all().isEmpty())
        assertTrue(services.education.snapshot().records.isEmpty())
        compose.onNodeWithTag("experience_save_reflection").performScrollTo().performClick()
        compose.waitUntil(5000) { runBlocking { services.workspace.all().any { it.body == body } } }
        val record = services.workspace.all().single { it.body == body }
        val link = services.education.snapshot().records.single()
        assertEquals(record.id, link.workspaceRecordId)
        assertEquals("SCENE-CH05-CARD1", link.contentId)
    }

    @Test fun summaryImageFlowSharesOnlyExplicitlySelectedTopicTitles() {
        compose.onNodeWithText("看看完整知识地图").performScrollTo().performClick()
        compose.onNodeWithText("学习总结图片").performScrollTo().performClick()
        compose.onNodeWithTag("education_summary_share").assertIsNotEnabled()
        val first = catalog.nodes.values.filter { it.kind == "CHAPTER" }.minBy { it.payload.optInt("order", Int.MAX_VALUE) }
        compose.onNodeWithTag("education_summary_topic_${first.id}").performScrollTo().performClick()
        compose.onNodeWithTag("education_summary_share").performScrollTo().performClick()
        assertEquals(listOf(first.title), sharedSummaryTitles)
    }
}
