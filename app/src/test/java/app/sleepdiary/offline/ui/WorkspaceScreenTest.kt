package app.sleepdiary.offline.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.repository.*
import app.sleepdiary.offline.ui.workspace.*
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34], qualifiers="w411dp-h891dp-xxhdpi")
class WorkspaceScreenTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private lateinit var db: SleepDiaryDatabase
    private lateinit var repo: WorkspaceRepository
    private var back = false
    @Before fun setup() {
        db=Room.inMemoryDatabaseBuilder(compose.activity,SleepDiaryDatabase::class.java).build()
        repo=WorkspaceRepository(db)
        compose.setContent { WorkspaceScreen(repo) { back=true } }
        compose.waitUntil(5000) { compose.onAllNodesWithText("正在处理…").fetchSemanticsNodes().isEmpty() }
    }
    @After fun close() { db.close() }
    private fun create() { compose.onNodeWithText("新建学习笔记").performScrollTo().performClick() }
    private fun save() {
        compose.onNodeWithText("保存个人记录").performScrollTo().performClick()
        compose.waitUntil(5000) { compose.onAllNodesWithText("个人记录已保存").fetchSemanticsNodes().isNotEmpty() }
    }
    @Test fun emptyInitialBodyAndSettingsBack() {
        compose.onNodeWithText("返回设置").performClick(); assertTrue(back)
        create(); compose.onNodeWithTag("workspace_body").assertTextContains("")
        assertTrue(runBlocking { repo.all() }.isEmpty())
    }
    @Test fun mixedTextSavedLoadedAndEditedWithoutChangingIdentity() {
        create()
        compose.onNodeWithTag("workspace_title").performTextInput("记录标题")
        compose.onNodeWithTag("workspace_body").performTextInput("中文")
        compose.onNodeWithTag("workspace_body").performTextInput(" English 123\n下一行")
        save()
        val first=runBlocking { repo.all().single() }
        assertEquals("中文 English 123\n下一行",first.body)
        compose.onNodeWithText("记录标题").performScrollTo().performClick()
        compose.onNodeWithTag("workspace_body").assertTextContains(first.body)
        compose.onNodeWithTag("workspace_body").performTextReplacement("修改后的内容")
        save()
        val edited=runBlocking { repo.all().single() }
        assertEquals(first.id,edited.id);assertEquals(first.createdAtUtc,edited.createdAtUtc)
        assertEquals(2,edited.revision);assertEquals("修改后的内容",edited.body)
    }
    @Test fun blankBodyRejectedAndDraftRemains() {
        create()
        compose.onNodeWithTag("workspace_title").performTextInput("保留标题")
        compose.onNodeWithText("保存个人记录").performScrollTo().performClick()
        compose.waitUntil(5000) { compose.onAllNodesWithTag("workspace_message").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("workspace_title").assertTextContains("保留标题")
        assertTrue(runBlocking { repo.all() }.isEmpty())
    }
    @Test fun discardRequiresConfirmationAndDoesNotWrite() {
        create();compose.onNodeWithTag("workspace_body").performTextInput("未保存草稿")
        compose.onNodeWithText("返回记录列表").performScrollTo().performClick()
        compose.onNodeWithText("继续编辑").performClick()
        compose.onNodeWithTag("workspace_body").assertTextContains("未保存草稿")
        compose.onNodeWithText("返回记录列表").performScrollTo().performClick()
        compose.onNodeWithText("放弃编辑").performClick()
        compose.onNodeWithText("返回设置").assertExists()
        assertTrue(runBlocking { repo.all() }.isEmpty())
    }
    @Test fun savedStateRecreatesDraftAndConcurrencyToken() {
        val state=SavedStateHandle()
        lateinit var original:WorkspaceModel
        compose.runOnUiThread { original=WorkspaceModel(repo,state) }
        compose.waitUntil(5000) { compose.runOnIdle { !original.busy } }
        compose.runOnUiThread {
            original.create(WorkspaceKind.REFLECTION)
            original.title("标题")
            original.body("中文 composing English 123")
        }
        val copied=SavedStateHandle(state.keys().associateWith { state.get<Any?>(it) })
        lateinit var recreated:WorkspaceModel
        compose.runOnUiThread { recreated=WorkspaceModel(repo,copied) }
        compose.waitUntil(5000) { compose.runOnIdle { !recreated.busy } }
        assertEquals(original.draft,recreated.draft);assertTrue(recreated.dirty)
        assertTrue(runBlocking { repo.all() }.isEmpty())
    }
}
