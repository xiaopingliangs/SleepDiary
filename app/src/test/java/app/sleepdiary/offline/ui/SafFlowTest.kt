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
import androidx.room.Room
import app.sleepdiary.offline.clockOn
import app.sleepdiary.offline.diary
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.entity.AppState
import app.sleepdiary.offline.data.repository.RoomDiaryUiStore
import app.sleepdiary.offline.ui.diary.*
import app.sleepdiary.offline.ui.theme.SleepDiaryTheme
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.io.*

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],qualifiers="w411dp-h891dp-xxhdpi")
class SafFlowTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 private lateinit var db:SleepDiaryDatabase
 private lateinit var services:AppServices
 private lateinit var registry:ActivityResultRegistry
 private var request=0
 private lateinit var launched:Intent
 private val uri=Uri.parse("content://test.documents/document/backup")
 private val note="中文 English 123,\"备注\"\r\n第二行"
 @Before fun start() {
  db=Room.inMemoryDatabaseBuilder(compose.activity,SleepDiaryDatabase::class.java).build()
  services=AppServices(compose.activity,db)
  runBlocking{db.diaryDao().insert(diary().copy(comment=note));db.stateAndSummaryDao().putState(AppState(hasCompletedOnboarding=true,trackingStartDate="2026-09-09"))}
  registry=object:ActivityResultRegistry(){override fun <I,O> onLaunch(requestCode:Int,contract:ActivityResultContract<I,O>,input:I,options:ActivityOptionsCompat?){request=requestCode;launched=contract.createIntent(compose.activity,input)}}
  val owner=object:ActivityResultRegistryOwner{override val activityResultRegistry=registry}
  lateinit var model:DiaryViewModel
  compose.runOnUiThread{model=DiaryViewModel(RoomDiaryUiStore(db),clock=clockOn("2026-09-09"))}
  compose.setContent{CompositionLocalProvider(LocalActivityResultRegistryOwner provides owner){SleepDiaryTheme{SleepDiaryApp(model,{},services)}}}
  compose.waitUntil(5000){model.state.value.page!=HomePage.LOADING}
  compose.onNodeWithText("设置").performScrollTo().performClick()
 }
 @After fun close(){db.close()}
 private fun deliver(ok:Boolean=true){compose.runOnUiThread{registry.dispatchResult(request,if(ok)Activity.RESULT_OK else Activity.RESULT_CANCELED,if(ok)Intent().setData(uri)else null)}}
 private fun waitText(text:String){compose.waitUntil(10000){compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()}}
 private fun export(label:String){compose.onNodeWithText(label).performScrollTo().performClick();compose.onNodeWithText("选择保存位置").performClick();assertEquals(Intent.ACTION_CREATE_DOCUMENT,launched.action);assertTrue(launched.getStringExtra(Intent.EXTRA_TITLE)!!.startsWith("SleepDiary_"))}
 @Test fun jsonCreateDocumentWritesValidatedBackupWithComment() {
  val output=ByteArrayOutputStream();shadowOf(compose.activity.contentResolver).registerOutputStream(uri,output)
  export("导出完整备份 JSON");assertEquals("application/json",launched.type);assertTrue(launched.getStringExtra(Intent.EXTRA_TITLE)!!.endsWith(".json"));deliver();waitText("完整备份已导出")
  val text=output.toString("UTF-8");assertTrue(text.contains("BACKUP_1.1"));assertEquals(note,services.backup.preview(text).entries.single().comment)
 }
 @Test fun csvCreateDocumentWritesEscapedComment() {
  val output=ByteArrayOutputStream();shadowOf(compose.activity.contentResolver).registerOutputStream(uri,output)
  export("导出睡眠日记 CSV");assertEquals("text/csv",launched.type);deliver();waitText("CSV 已导出")
  assertArrayEquals(runBlocking{services.backup.csv()},output.toByteArray());assertTrue(output.toString("UTF-8").contains("\"\"备注\"\""))
 }
 @Test fun cancellingSystemPickerPreservesDatabase() {
  val before=runBlocking{services.backup.snapshot()}
  compose.onNodeWithText("导入完整备份").performScrollTo().performClick();assertEquals(Intent.ACTION_OPEN_DOCUMENT,launched.action);deliver(false);compose.waitForIdle()
  assertEquals(before,runBlocking{services.backup.snapshot()});compose.onAllNodesWithText("恢复完整备份").assertCountEquals(0)
 }
 private fun importText(text:String){shadowOf(compose.activity.contentResolver).registerInputStream(uri,ByteArrayInputStream(text.toByteArray(Charsets.UTF_8)));compose.onNodeWithText("导入完整备份").performScrollTo().performClick();assertEquals(Intent.ACTION_OPEN_DOCUMENT,launched.action);deliver()}
 @Test fun restoreRequiresConfirmationThenReplacesComment() {
  val text=runBlocking{services.backup.export()}.replace("English 123","updated 456")
  importText(text);waitText("替换并恢复");assertEquals(note,runBlocking{db.diaryDao().all().single().comment})
  compose.onNodeWithText("替换并恢复").performClick();waitText("完整备份已恢复");assertEquals(note.replace("English 123","updated 456"),runBlocking{db.diaryDao().all().single().comment})
 }
 @Test fun legacyBackupPreviewCanBeCancelledWithoutMutation() {
  val before=runBlocking{services.backup.snapshot()};importText(File("../samples/backup-v1.example.json").readText());waitText("替换并恢复");compose.onNodeWithText("取消").performClick();assertEquals(before,runBlocking{services.backup.snapshot()})
 }
 @Test fun invalidDocumentDoesNotReplaceData() {
  val before=runBlocking{services.backup.snapshot()};importText("{broken");waitText("操作未完成，数据未确认成功。请重试。");assertEquals(before,runBlocking{services.backup.snapshot()})
 }
 @Test fun failedWriteNeverReportsExportSuccess() {
  shadowOf(compose.activity.contentResolver).registerOutputStream(uri,object:OutputStream(){override fun write(b:Int){throw IOException("provider failure")}})
  export("导出完整备份 JSON");deliver();waitText("操作未完成，数据未确认成功。请重试。");compose.onAllNodesWithText("完整备份已导出").assertCountEquals(0)
 }
}
