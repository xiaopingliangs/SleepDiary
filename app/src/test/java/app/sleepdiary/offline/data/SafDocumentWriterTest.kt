package app.sleepdiary.offline.data

import android.net.Uri
import app.sleepdiary.offline.data.backup.SafDocumentWriter
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class SafDocumentWriterTest {
    private val uri = Uri.parse("content://test/document/1")

    @Test fun writesPreparedBytesAndDoesNotDeleteOnSuccess() {
        val bytes = byteArrayOf(1, 2, 3)
        val output = ByteArrayOutputStream()
        var deleted = false
        SafDocumentWriter.write(uri, bytes, { output }, { deleted = true; 1 })
        assertArrayEquals(bytes, output.toByteArray())
        assertFalse(deleted)
    }

    @Test fun failedProviderWriteAttemptsToDeletePartialTarget() {
        var deleted: Uri? = null
        val failing = object : ByteArrayOutputStream() {
            override fun write(b: ByteArray, off: Int, len: Int) { throw IllegalStateException("provider failed") }
        }
        assertTrue(runCatching { SafDocumentWriter.write(uri, byteArrayOf(1), { failing }, { deleted = it; 1 }) }.isFailure)
        assertEquals(uri, deleted)
    }

    @Test fun emptyPayloadIsRejectedBeforeOpeningDestination() {
        var opened = false
        assertTrue(runCatching { SafDocumentWriter.write(uri, byteArrayOf(), { opened = true; ByteArrayOutputStream() }, { 0 }) }.isFailure)
        assertFalse(opened)
    }
}
