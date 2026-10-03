package app.sleepdiary.offline.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.*
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
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
import org.robolectric.Shadows.shadowOf
import java.io.ByteArrayOutputStream
import java.io.IOException

@RunWith(RobolectricTestRunner::class) @Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
class ClinicianSummaryScreenTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private lateinit var db: SleepDiaryDatabase
    private var back = false
    private lateinit var registry: ActivityResultRegistry
    private lateinit var restoration: StateRestorationTester
    private lateinit var launched: Intent
    private var requestCode = 0
    private var launches = 0
    private val uri = Uri.parse("content://test.documents/document/clinician-summary")
    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(compose.activity, SleepDiaryDatabase::class.java).build()
        runBlocking { db.diaryDao().insert(diary().copy(comment = "private-note")) }
        registry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(code: Int, contract: ActivityResultContract<I, O>, input: I, options: ActivityOptionsCompat?) {
                requestCode = code; launched = contract.createIntent(compose.activity, input); launches++
            }
        }
        val owner = object : ActivityResultRegistryOwner { override val activityResultRegistry = registry }
        restoration = StateRestorationTester(compose)
        restoration.setContent { CompositionLocalProvider(LocalActivityResultRegistryOwner provides owner) {
            ClinicianSummaryScreen(ClinicianSummaryService(db)) { back = true }
        } }
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
        compose.onNodeWithText("返回我的").performClick(); assertTrue(back)
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
    private fun previewText() = compose.onNodeWithTag("summary_preview").fetchSemanticsNode()
        .config[androidx.compose.ui.semantics.SemanticsProperties.Text].single().text
    private fun launchExport() {
        compose.onNodeWithText("导出这份预览 TXT").performScrollTo().performClick()
        compose.onNodeWithText("选择保存位置").performClick()
        assertEquals(Intent.ACTION_CREATE_DOCUMENT, launched.action)
        assertEquals("text/plain", launched.type)
        assertEquals("SleepDiary_clinician_summary.txt", launched.getStringExtra(Intent.EXTRA_TITLE))
    }
    private fun deliver(ok: Boolean = true) { compose.runOnUiThread {
        registry.dispatchResult(requestCode, if(ok) Activity.RESULT_OK else Activity.RESULT_CANCELED,
            if(ok) Intent().setData(uri) else null)
    } }
    private fun waitForMessage(text: String) { compose.waitUntil(5000) {
        compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
    } }
    @Test fun exportWritesExactReviewedUtf8SnapshotWithoutDefaultComment() {
        val output = ByteArrayOutputStream()
        shadowOf(compose.activity.contentResolver).registerOutputStream(uri, output)
        dates(); prepare(); val preview = previewText(); launchExport(); deliver()
        waitForMessage("就诊摘要已导出")
        assertArrayEquals(preview.toByteArray(Charsets.UTF_8), output.toByteArray())
        assertFalse(output.toString("UTF-8").contains("private-note"))
    }
    @Test fun explicitlySelectedCommentIsIncludedWithoutRereadingChangedData() {
        val output = ByteArrayOutputStream()
        shadowOf(compose.activity.contentResolver).registerOutputStream(uri, output)
        dates(); compose.onNodeWithTag("summary_comments").performScrollTo().performClick(); prepare()
        val preview = previewText()
        runBlocking { val original = db.diaryDao().all().single(); db.diaryDao().update(original.copy(comment = "unreviewed edit")) }
        launchExport(); deliver(); waitForMessage("就诊摘要已导出")
        assertArrayEquals(preview.toByteArray(Charsets.UTF_8), output.toByteArray())
        assertTrue(output.toString("UTF-8").contains("private-note"))
        assertFalse(output.toString("UTF-8").contains("unreviewed edit"))
    }
    @Test fun cancellingFilePickerDoesNotWriteOrChangeDatabase() {
        val output = ByteArrayOutputStream()
        shadowOf(compose.activity.contentResolver).registerOutputStream(uri, output)
        val before = runBlocking { db.diaryDao().all() }
        dates(); prepare(); launchExport(); deliver(false); compose.waitForIdle()
        assertEquals(0, output.size()); assertEquals(before, runBlocking { db.diaryDao().all() })
        compose.onAllNodesWithText("就诊摘要已导出").assertCountEquals(0)
        compose.onNodeWithTag("summary_comments").assertIsEnabled()
    }
    @Test fun selectedSafetyExportsSavedDraftButDoesNotImplicitlyIncludeComment() {
        runBlocking {
            db.safetyDao().insertAssessment(app.sleepdiary.offline.data.entity.SafetyAssessment(
                "test-current-safety", "2026-09-09T00:00:00Z",
                derivedGate = app.sleepdiary.offline.domain.model.SafetyGate.CLINICIAN_REVIEW,
                status = app.sleepdiary.offline.domain.model.AssessmentStatus.DRAFT, isCurrent = true))
            db.safetyDao().insertAnswers(listOf(app.sleepdiary.offline.data.entity.SafetyAnswer(
                "test-current-safety", "S01", "UNSURE", detailJson = null)))
        }
        val output = ByteArrayOutputStream()
        shadowOf(compose.activity.contentResolver).registerOutputStream(uri, output)
        dates(); compose.onNodeWithTag("summary_safety").performScrollTo().performClick(); prepare()
        val preview = previewText(); launchExport(); deliver(); waitForMessage("就诊摘要已导出")
        assertArrayEquals(preview.toByteArray(Charsets.UTF_8), output.toByteArray())
        assertTrue(output.toString("UTF-8").contains("test-current-safety"))
        assertTrue(output.toString("UTF-8").contains("DRAFT"))
        assertFalse(output.toString("UTF-8").contains("private-note"))
    }
    @Test fun cancellingWarningDoesNotOpenPicker() {
        dates(); prepare()
        compose.onNodeWithText("导出这份预览 TXT").performScrollTo().performClick()
        compose.onNodeWithText("取消").performClick()
        assertEquals(0, launches)
    }
    @Test fun closeFailureDoesNotReportSuccess() {
        shadowOf(compose.activity.contentResolver).registerOutputStream(uri, object : ByteArrayOutputStream() {
            override fun close() { throw IOException("synthetic close failure") }
        })
        dates(); prepare(); launchExport(); deliver()
        waitForMessage("导出未完成，请重试；文件可能未完整写入。")
        compose.onAllNodesWithText("就诊摘要已导出").assertCountEquals(0)
        compose.onNodeWithTag("summary_preview").assertExists()
    }
    @Test fun pendingPickerRestorationKeepsSameSnapshotAndDoesNotRelaunch() {
        val output = ByteArrayOutputStream()
        shadowOf(compose.activity.contentResolver).registerOutputStream(uri, output)
        dates(); prepare(); val preview = previewText(); launchExport()
        restoration.emulateSavedInstanceStateRestore()
        assertEquals(1, launches)
        compose.onNodeWithTag("summary_comments").assertIsNotEnabled()
        deliver(); waitForMessage("就诊摘要已导出")
        assertArrayEquals(preview.toByteArray(Charsets.UTF_8), output.toByteArray())
    }
    @Test fun missingSnapshotAfterProcessLossNeverOpensDestination() {
        val newModel = ClinicianPreviewModel()
        var opened = false
        newModel.receiveExport { opened = true; ByteArrayOutputStream() }
        assertFalse(opened)
        assertEquals("预览已失效，请重新生成摘要后导出。", newModel.exportMessage)
    }
    @Test fun unpreparedAndDuplicateExportRequestsAreRejected() {
        val model = ClinicianPreviewModel()
        assertFalse(model.stageExport())
        model.prepared("snapshot")
        assertTrue(model.stageExport()); assertFalse(model.stageExport())
        model.receiveExport(null)
        assertNull(model.pendingExport); assertTrue(model.stageExport())
        model.launchFailed(); assertNull(model.pendingExport)
    }
}
