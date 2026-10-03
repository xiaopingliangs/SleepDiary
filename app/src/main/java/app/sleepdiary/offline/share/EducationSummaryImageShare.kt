package app.sleepdiary.offline.share

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.content.FileProvider
import java.io.File

/** Creates a local image containing only chapter titles explicitly selected by the user. */
class EducationSummaryImageShare(private val context: Context) {
    fun prepare(selectedTopicTitles: List<String>): Intent {
        val file = renderSelectedTitles(selectedTopicTitles)
        val uri = FileProvider.getUriForFile(context, context.packageName + ".files", file)
        check(uri.scheme == "content")
        return chooserFor(uri)
    }

    internal fun renderSelectedTitles(selectedTopicTitles: List<String>): File {
        val titles = selectedTopicTitles.map(String::trim).filter(String::isNotEmpty).distinct()
        require(titles.isNotEmpty()) { "请至少选择一个学习主题" }
        require(titles.size <= MAX_TOPICS) { "所选主题过多" }
        return render(titles)
    }

    internal fun chooserFor(uri: Uri): Intent {
        require(uri.scheme == "content")
        return DiaryShare.chooser(uri, "分享学习总结图片")
    }

    private fun render(titles: List<String>): File {
        val file = File(context.cacheDir, "share/EducationSummary_${System.currentTimeMillis()}.png")
        check(file.parentFile?.mkdirs() == true || file.parentFile?.isDirectory == true)
        val body = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(52, 67, 77)
            textSize = 34f
            typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
        }
        val wrapped = titles.map { wrap(it, body, WIDTH - SIDE * 2 - 40f) }
        val contentHeight = wrapped.sumOf { it.size * LINE_HEIGHT + 10 }
        val height = (TOP + BOTTOM + contentHeight).coerceAtLeast(MIN_HEIGHT)
        val bitmap = Bitmap.createBitmap(WIDTH, height, Bitmap.Config.ARGB_8888)
        try {
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.rgb(247, 248, 250))
            val heading = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(35, 53, 65)
                textSize = 46f
                typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
            }
            canvas.drawText("睡眠知识学习摘要", SIDE.toFloat(), 100f, heading)
            canvas.drawText("我选择分享的学习主题", SIDE.toFloat(), 168f, body)
            var y = TOP.toFloat()
            wrapped.forEach { lines ->
                canvas.drawCircle(SIDE + 10f, y - 12f, 7f, body)
                lines.forEach { line ->
                    canvas.drawText(line, SIDE + 32f, y, body)
                    y += LINE_HEIGHT
                }
                y += 10f
            }
            val footer = Paint(body).apply { textSize = 25f; color = Color.rgb(105, 117, 126) }
            canvas.drawText("SleepDiary · 一般睡眠教育", SIDE.toFloat(), height - 44f, footer)
            file.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        } finally { bitmap.recycle() }
        check(file.isFile && file.length() > 0)
        return file
    }

    private fun wrap(text: String, paint: Paint, width: Float): List<String> {
        val lines = mutableListOf<String>()
        var line = StringBuilder()
        text.codePoints().forEach { point ->
            val chars = String(Character.toChars(point))
            if (line.isNotEmpty() && paint.measureText(line.toString() + chars) > width) {
                lines += line.toString()
                line = StringBuilder()
            }
            line.append(chars)
        }
        if (line.isNotEmpty()) lines += line.toString()
        return lines
    }

    companion object {
        const val MAX_TOPICS = 11
        const val WIDTH = 1080
        const val MIN_HEIGHT = 800
        const val TOP = 280
        const val BOTTOM = 110
        const val LINE_HEIGHT = 68
        const val SIDE = 64
    }
}
