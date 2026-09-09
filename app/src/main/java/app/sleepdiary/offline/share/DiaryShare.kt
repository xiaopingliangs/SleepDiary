package app.sleepdiary.offline.share

import android.content.*
import android.graphics.*
import androidx.core.content.FileProvider
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.entity.SleepDiaryEntry
import app.sleepdiary.offline.ui.diary.*
import java.io.File

class DiaryShare(private val context:Context,private val db:SleepDiaryDatabase) {
 companion object {
  fun lines(entry:SleepDiaryEntry)=listOf("主观睡眠记录",entry.sleepDate)+DiaryField.entries.map{it.label+"："+fieldValue(DiaryForm.from(entry),it)}+"主观估计数据 · SleepDiary V0.1"
 }
 suspend fun prepare(expected:SleepDiaryEntry):Intent {
  val entry=checkNotNull(db.diaryDao().byId(expected.id));check(entry==expected){"分享回读不一致"}
  val directory=File(context.cacheDir,"share").apply{mkdirs()}
  val file=File(directory,"SleepDiary_${entry.sleepDate}.png")
  val bitmap=Bitmap.createBitmap(1080,1440,Bitmap.Config.ARGB_8888);val canvas=Canvas(bitmap);canvas.drawColor(Color.rgb(245,247,248))
  val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply{color=Color.rgb(36,50,58);textSize=32f}
  lines(entry).forEachIndexed{i,line->canvas.drawText(line,64f,110f+i*112f,paint)}
  file.outputStream().use{check(bitmap.compress(Bitmap.CompressFormat.PNG,100,it))};bitmap.recycle()
  val uri=FileProvider.getUriForFile(context,context.packageName+".files",file)
  val send=Intent(Intent.ACTION_SEND).setType("image/png").putExtra(Intent.EXTRA_STREAM,uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
  send.clipData=ClipData.newRawUri("睡眠记录",uri)
  return Intent.createChooser(send,"分享睡眠记录").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
 }
 fun cleanupExpired(now:Long=System.currentTimeMillis()) {File(context.cacheDir,"share").listFiles()?.filter{it.isFile&&it.extension=="png"&&now-it.lastModified()>7L*24*60*60*1000}?.forEach{it.delete()}}
}
