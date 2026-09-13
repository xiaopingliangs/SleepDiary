package app.sleepdiary.offline.application

import java.io.ByteArrayOutputStream
import java.io.IOException
import org.junit.Assert.*
import org.junit.Test

class SummaryTextWriterTest {
    @Test fun preservesUtf8AndClosesStream() {
        var closed = false
        val stream = object : ByteArrayOutputStream() { override fun close() { closed = true; super.close() } }
        val preview = "中文 English 123\r\n\"原文\",第二行"
        SummaryTextWriter.write(preview) { stream }
        assertArrayEquals(preview.toByteArray(Charsets.UTF_8), stream.toByteArray())
        assertTrue(closed)
    }
    @Test(expected = IllegalStateException::class) fun unavailableDestinationFails() {
        SummaryTextWriter.write("preview") { null }
    }
    @Test(expected = IOException::class) fun closeFailureIsFailure() {
        SummaryTextWriter.write("preview") { object : ByteArrayOutputStream() {
            override fun close() { throw IOException("synthetic close failure") }
        } }
    }
    @Test fun failedWriteStillClosesStream() {
        var closed = false
        val stream = object : java.io.OutputStream() {
            override fun write(b: Int) { throw IOException("synthetic write failure") }
            override fun close() { closed = true }
        }
        assertThrows(IOException::class.java) { SummaryTextWriter.write("preview") { stream } }
        assertTrue(closed)
    }
}
