package app.sleepdiary.offline.data.backup

import android.net.Uri
import java.io.OutputStream

/** Writes already validated bytes; if provider I/O fails, best-effort remove its partial document. */
internal object SafDocumentWriter {
    fun write(
        uri: Uri,
        bytes: ByteArray,
        open: (Uri) -> OutputStream?,
        delete: (Uri) -> Int
    ) {
        require(bytes.isNotEmpty()) { "导出内容为空" }
        try {
            val output = checkNotNull(open(uri)) { "无法打开所选文件" }
            output.use { it.write(bytes); it.flush() }
        } catch (failure: Exception) {
            runCatching { delete(uri) }
            throw failure
        }
    }
}
