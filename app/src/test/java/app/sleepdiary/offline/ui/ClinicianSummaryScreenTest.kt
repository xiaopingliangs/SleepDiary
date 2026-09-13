package app.sleepdiary.offline.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.room.Room
import app.sleepdiary.offline.application.ClinicianSummaryService
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.diary
import app.sleepdiary.offline.ui.clinician.ClinicianPreviewModel
import app.sleepdiary.offline.ui.clinician.ClinicianSummaryScreen
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class) @Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
class ClinicianSummaryScreenTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private lateinit var db: SleepDiaryDatabase
    private var back = false
    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(compose.activity, SleepDiaryDatabase::class.java).build()
        runBlocking { db.diaryDao().insert(diary().copy(comment = "private-note")) }
        compose.setContent { ClinicianSummaryScreen(ClinicianSummaryService(db)) { back = true } }
    }
    @After fun close() { db.close() }
    private fun dates() {
        compose.onNodeWithTag("summary_from").performTextReplacement("2026-09-09")
        compose.onNodeWithTag("summary_through").performTextReplacement("2026-09-09")
    }
    private fun prepare() {
        compose.onNodeWithText("生成本机预览").performScrollTo().performClick()
        compose.waitUntil(5000) { compose.onAllNodesWithTag("summary_preview").fetchSemanticsNodes().isNotEmpty() }
    }
    @Test fun defaultsArePrivateAndBackWorks() {
        compose.onNodeWithTag("summary_comments").assertIsOff()
        compose.onNodeWithTag("summary_safety").assertIsOff()
        compose.onNodeWithText("返回设置").performClick(); assertTrue(back)
    }
    @Test fun previewChangesOnlyAfterExplicitSelectionAndRegeneration() {
        dates(); prepare()
        compose.onNodeWithTag("summary_preview").assertTextContains("有记录 1 天", substring = true)
        compose.onNodeWithTag("summary_preview").assert(SemanticsMatcher("excludes comment") {
            !it.config[androidx.compose.ui.semantics.SemanticsProperties.Text].joinToString().contains("private-note")
        })
        compose.onNodeWithTag("summary_comments").performScrollTo().performClick()
        compose.onNodeWithTag("summary_preview").assertDoesNotExist()
        prepare(); compose.onNodeWithTag("summary_preview").assertTextContains("private-note", substring = true)
    }
    @Test fun invalidDatesDoNotProduceSummary() {
        compose.onNodeWithTag("summary_from").performTextReplacement("invalid")
        compose.onNodeWithText("生成本机预览").performScrollTo().performClick()
        compose.onNodeWithText("请检查日期格式和先后顺序。").assertExists()
        compose.onNodeWithTag("summary_preview").assertDoesNotExist()
    }
    @Test fun dateChangeInvalidatesPreviousSnapshot() {
        dates(); prepare()
        compose.onNodeWithTag("summary_through").performScrollTo().performTextReplacement("2026-09-10")
        compose.onNodeWithTag("summary_preview").assertDoesNotExist()
    }
    @Test fun newProcessModelHasNoOldSensitivePreview() {
        val previous = ClinicianPreviewModel().also { it.prepared("private snapshot") }
        assertEquals("private snapshot", previous.preview)
        assertNull(ClinicianPreviewModel().preview)
        previous.invalidate(); assertNull(previous.preview)
    }
}
