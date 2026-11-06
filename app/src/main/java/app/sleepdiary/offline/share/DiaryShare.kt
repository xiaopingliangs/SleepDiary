package app.sleepdiary.offline.share

import android.content.*
import android.graphics.*
import androidx.core.content.FileProvider
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.entity.SleepDiaryEntry
import app.sleepdiary.offline.ui.diary.*
import java.io.File
import java.time.LocalDate

class DiaryShare(private val context:Context,private val db:SleepDiaryDatabase) {
 companion object {
  fun chooser(uri:android.net.Uri, title:String="分享睡眠记录"):Intent {
   require(uri.scheme=="content")
   val send=Intent(Intent.ACTION_SEND).setType("image/png").putExtra(Intent.EXTRA_STREAM,uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
   send.clipData=ClipData.newRawUri(title,uri)
   // The source task is removed immediately after hand-off; the chooser must own another task.
   return Intent.createChooser(send,title).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
  }
  fun handOff(intent:Intent,start:(Intent)->Unit,finish:()->Unit) {start(intent);finish()}
  fun lines(entry:SleepDiaryEntry)=listOf("主观睡眠记录",entry.sleepDate)+DiaryField.entries.map{it.label+"："+fieldValue(DiaryForm.from(entry),it)}+listOf("特殊情况",entry.comment?.takeIf{it.isNotBlank()} ?: "无记录","主观估计数据 · SleepDiary Public Edition")
 }
 internal suspend fun renderSaved(expected:SleepDiaryEntry):File {
  val entry=checkNotNull(db.diaryDao().byId(expected.id));check(entry==expected){"分享回读不一致"}
  return render("SleepDiary_${entry.sleepDate}", lines(entry))
 }
 internal suspend fun recentLines(today:LocalDate,days:Int):List<String> = RecentDiaryShare.lines(today,days,db.diaryDao().all())
 internal suspend fun renderRecent(today:LocalDate,days:Int):File = render("SleepDiary_recent_${days}_${today}", recentLines(today,days))
 private fun render(name:String,content:List<String>):File {
  val directory=File(context.cacheDir,"share").apply{mkdirs()}
  val file=File.createTempFile(name+"_",".png",directory)
  val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply{color=Color.rgb(36,50,58);textSize=32f}
  val wrapped=content.flatMap { it.lines() }.flatMap { line ->
   var rest=line
   buildList {
    while(rest.isNotEmpty()) { val count=paint.breakText(rest,true,952f,null);if(count<=0)break;add(rest.substring(0,count));rest=rest.substring(count) }
   }
  }
  val lineHeight=64
  val bitmap=Bitmap.createBitmap(1080,maxOf(1440,wrapped.size*lineHeight+160),Bitmap.Config.ARGB_8888);val canvas=Canvas(bitmap);canvas.drawColor(Color.rgb(245,247,248))
  wrapped.forEachIndexed{i,line->canvas.drawText(line,64f,90f+i*lineHeight,paint)}
  file.outputStream().use{check(bitmap.compress(Bitmap.CompressFormat.PNG,100,it))};bitmap.recycle()
  check(file.isFile && file.length()>0)
  return file
 }
 suspend fun prepare(expected:SleepDiaryEntry):Intent {
  val file=renderSaved(expected)
  val uri=FileProvider.getUriForFile(context,context.packageName+".files",file)
  return chooser(uri)
 }
 suspend fun prepareRecent(today:LocalDate,days:Int):Intent {
  val file=renderRecent(today,days)
  return chooser(FileProvider.getUriForFile(context,context.packageName+".files",file),"分享最近 $days 天睡眠记录")
 }

 fun cleanupExpired(now:Long=System.currentTimeMillis()) {File(context.cacheDir,"share").listFiles()?.filter{it.isFile&&it.extension=="png"&&now-it.lastModified()>7L*24*60*60*1000}?.forEach{it.delete()}}
}
