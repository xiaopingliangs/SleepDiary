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
   val entry=diary().copy(comment="不得分享的中文 abc123")
   db.diaryDao().insert(entry)
   val share=DiaryShare(context,db);val file=share.renderSaved(entry)
   assertArrayEquals(byteArrayOf(-119,80,78,71,13,10,26,10),file.readBytes().take(8).toByteArray())
    assertFalse(DiaryShare.lines(entry).joinToString().contains(entry.comment!!))
    assertTrue(DiaryShare.lines(entry,includeComment=true).joinToString().contains(entry.comment!!))
   assertTrue(runCatching{share.renderSaved(entry.copy(comment="不同"))}.isFailure)
  } finally {db.close()}
 }
}
