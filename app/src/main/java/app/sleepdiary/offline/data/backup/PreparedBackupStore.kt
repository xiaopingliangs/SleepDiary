package app.sleepdiary.offline.data.backup

import java.io.File
import java.io.FileOutputStream

/** Short-lived private-cache staging keeps validation ahead of the SAF destination picker. */
internal object PreparedBackupStore {
    private const val PREFIX = "sleepdiary-backup-"
    private const val SUFFIX = ".pending"

    fun create(cacheDir: File, contents: String): String {
        require(contents.isNotEmpty()) { "导出内容为空" }
        val file = File.createTempFile(PREFIX, SUFFIX, cacheDir)
        try {
            FileOutputStream(file).use { it.write(contents.toByteArray(Charsets.UTF_8)); it.fd.sync() }
            return file.canonicalPath
        } catch (failure: Exception) {
            file.delete()
            throw failure
        }
    }

    fun read(cacheDir: File, path: String): ByteArray {
        val file = File(path).canonicalFile
        require(file.parentFile == cacheDir.canonicalFile && file.name.startsWith(PREFIX) && file.name.endsWith(SUFFIX))
        val bytes = file.readBytes()
        require(bytes.isNotEmpty()) { "备份暂存为空" }
        return bytes
    }

    fun delete(cacheDir: File, path: String?) {
        if (path == null) return
        val file = runCatching { File(path).canonicalFile }.getOrNull() ?: return
        if (file.parentFile == cacheDir.canonicalFile && file.name.startsWith(PREFIX) && file.name.endsWith(SUFFIX)) file.delete()
    }

    /** A process death while a picker is open must not leave sensitive staging files indefinitely. */
    fun cleanupExpired(cacheDir: File, now: Long = System.currentTimeMillis(), maxAgeMillis: Long = 24L * 60 * 60 * 1000) {
        cacheDir.listFiles()?.filter { it.isFile && it.name.startsWith(PREFIX) && it.name.endsWith(SUFFIX) && now - it.lastModified() > maxAgeMillis }
            ?.forEach(File::delete)
    }
}
