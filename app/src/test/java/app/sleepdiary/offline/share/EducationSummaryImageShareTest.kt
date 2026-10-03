package app.sleepdiary.offline.share

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class EducationSummaryImageShareTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test fun imageContainsOnlyLocallySelectedEducationTitlesAndContentUri() {
        val sharing = EducationSummaryImageShare(context)
        val file = sharing.renderSelectedTitles(listOf("仅用户选择的章节名称"))
        val chooser = sharing.chooserFor(Uri.parse("content://app.sleepdiary.offline.files/share/EducationSummary_test.png"))
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        val send = chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!
        assertEquals(Intent.ACTION_SEND, send.action)
        assertEquals("image/png", send.type)
        assertTrue(send.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
        assertFalse(send.hasExtra(Intent.EXTRA_TEXT))
        val uri = send.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)!!
        assertEquals("content", uri.scheme)
        assertTrue(send.clipData!!.description.label.toString().contains("学习总结"))
        val bitmap = file.inputStream().use(BitmapFactory::decodeStream)
        assertNotNull(bitmap)
        assertEquals(EducationSummaryImageShare.WIDTH, bitmap!!.width)
        assertTrue(bitmap.height >= EducationSummaryImageShare.MIN_HEIGHT)
        bitmap.recycle()
        assertTrue(file.name.contains("EducationSummary_"))
    }

    @Test fun emptySelectionCannotCreateAnImage() {
        assertTrue(runCatching { EducationSummaryImageShare(context).prepare(emptyList()) }.isFailure)
    }
}
