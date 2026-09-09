package app.sleepdiary.offline
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.entity.SleepDiaryEntry
import app.sleepdiary.offline.share.DiaryShare
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class)
class ShareDeviceTest {
 private val context:Context get()=ApplicationProvider.getApplicationContext()
 private lateinit var db:SleepDiaryDatabase
 @Before fun setup(){db=Room.inMemoryDatabaseBuilder(context,SleepDiaryDatabase::class.java).build()}
 @After fun close(){db.close()}
 private fun diary()=SleepDiaryEntry(java.util.UUID.randomUUID().toString(),"2026-09-09","2026-09-08T23:00:00","2026-09-08T23:30:00",20,2,30,"2026-09-09T06:00:00","2026-09-09T07:00:00",3,"private comment","Asia/Shanghai","2026-09-09T00:00:00Z","2026-09-09T00:00:00Z",consistencyWarning=false)
 // Preserved real FileProvider test: Android paths/ContentResolver, not a Windows filesystem substitute.
 @Test fun shareHasReadableContentUri()=runBlocking{val e=diary();db.diaryDao().insert(e);val chooser=DiaryShare(context,db).prepare(e);assertEquals(Intent.ACTION_CHOOSER,chooser.action);assertTrue(chooser.flags and Intent.FLAG_ACTIVITY_NEW_TASK!=0);val send=chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!;assertEquals("image/png",send.type);assertTrue(send.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION!=0);val uri=send.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)!!;assertEquals("content",uri.scheme);val bytes=context.contentResolver.openInputStream(uri)!!.use{it.readBytes()};assertArrayEquals(byteArrayOf(-119,80,78,71),bytes.take(4).toByteArray())}
}
