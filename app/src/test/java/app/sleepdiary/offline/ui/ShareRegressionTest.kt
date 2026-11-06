package app.sleepdiary.offline.ui

import android.content.Intent
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.diary
import app.sleepdiary.offline.share.DiaryShare
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34]) @GraphicsMode(GraphicsMode.Mode.NATIVE)
class ShareRegressionTest {
 @Test fun chooserUsesIndependentTaskAndTemporaryContentGrant() {
  val uri=Uri.parse("content://app.sleepdiary.offline.files/share/test.png")
  val chooser=DiaryShare.chooser(uri)
  assertTrue(chooser.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
  assertTrue(chooser.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
  val send=chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!
  assertEquals(Intent.ACTION_SEND,send.action);assertEquals("image/png",send.type)
  assertEquals(uri,send.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))
  assertEquals(uri,send.clipData!!.getItemAt(0).uri)
  assertTrue(send.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
  assertTrue(runCatching{DiaryShare.chooser(Uri.parse("file:///private/test.png"))}.isFailure)
 }
 @Test fun finishOnlyFollowsSuccessfulHandOff() {
  val calls=mutableListOf<String>()
  DiaryShare.handOff(Intent(),{calls+="start"},{calls+="finish"})
  assertEquals(listOf("start","finish"),calls);calls.clear()
  assertTrue(runCatching{DiaryShare.handOff(Intent(),{error("no activity")},{calls+="finish"})}.isFailure)
  assertTrue(calls.isEmpty())
 }
 @Test fun savedReadbackCreatesPngButMismatchCannotShare()=runBlocking {
  val context=ApplicationProvider.getApplicationContext<android.content.Context>()
  val db=Room.inMemoryDatabaseBuilder(context,SleepDiaryDatabase::class.java).build()
  try {
   val entry=diary().copy(comment="特殊情况中文 abc123\n第二行")
   db.diaryDao().insert(entry)
   val share=DiaryShare(context,db);val file=share.renderSaved(entry)
   assertArrayEquals(byteArrayOf(-119,80,78,71,13,10,26,10),file.readBytes().take(8).toByteArray())
    assertTrue(DiaryShare.lines(entry).joinToString().contains(entry.comment!!))
   assertTrue(runCatching{share.renderSaved(entry.copy(comment="不同"))}.isFailure)
  } finally {db.close()}
 }
 @Test fun absentOrBlankDailyCommentShowsNoRecord() {
  for (comment in listOf(null,"","  \n")) {
   val lines=DiaryShare.lines(diary().copy(comment=comment))
   assertEquals("无记录",lines[lines.indexOf("特殊情况")+1])
  }
 }
 @Test fun multilineFourteenDayNotesRenderWithoutFixedHeightClipping()=runBlocking {
  val context=ApplicationProvider.getApplicationContext<android.content.Context>()
  val db=Room.inMemoryDatabaseBuilder(context,SleepDiaryDatabase::class.java).build()
  try {
   val date=java.time.LocalDate.parse("2026-10-27")
   for(offset in 0L..13L) db.diaryDao().insert(diary(date.minusDays(offset).toString()).copy(
    comment=if(offset%3L==0L) "示例特殊情况：这是第 ${offset+1} 条记录。\n保留第二行原文。" else null))
   val share=DiaryShare(context,db)
   val folder=java.io.File("../work/sharing-preview-20261027").apply { mkdirs() }
   val daily=share.renderSaved(db.diaryDao().all().last())
   daily.copyTo(java.io.File(folder,"daily.png"),overwrite=true)
   for(days in listOf(7,14)) {
    val file=share.renderRecent(date,days)
    val bitmap=android.graphics.BitmapFactory.decodeFile(file.absolutePath)
    assertEquals(1080,bitmap.width)
    if(days==14) assertTrue(bitmap.height>1440)
    bitmap.recycle()
    file.copyTo(java.io.File(folder,"recent-$days.png"),overwrite=true)
   }
  } finally { db.close() }
 }
 @Test fun recentShareReadsLatestDatabaseRowsAndCreatesDistinctPngFiles()=runBlocking {
  val context=ApplicationProvider.getApplicationContext<android.content.Context>()
  val db=Room.inMemoryDatabaseBuilder(context,SleepDiaryDatabase::class.java).build()
  try {
   val date=java.time.LocalDate.parse("2026-09-22")
   val entry=diary(date.toString()).copy(comment="旧备注",sleepOnsetLatencyMin=10)
   db.diaryDao().insert(entry)
   val share=DiaryShare(context,db)
   assertTrue(share.recentLines(date,7).contains("平均估计入睡耗时（分钟）：10.0"))
   db.diaryDao().update(entry.copy(comment="新备注",sleepOnsetLatencyMin=40))
   val lines=share.recentLines(date,7)
   assertTrue(lines.contains("新备注"));assertFalse(lines.contains("旧备注"))
   assertTrue(lines.contains("平均估计入睡耗时（分钟）：40.0"))
   val files=listOf(share.renderRecent(date,7),share.renderRecent(date,14),share.renderRecent(date,7))
   assertEquals(3,files.distinct().size)
   files.forEach { assertArrayEquals(byteArrayOf(-119,80,78,71,13,10,26,10),it.readBytes().take(8).toByteArray()) }
  } finally { db.close() }
 }
}
