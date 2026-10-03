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
  fun chooser(uri:android.net.Uri, title:String="分享睡眠记录"):Intent {
   require(uri.scheme=="content")
   val send=Intent(Intent.ACTION_SEND).setType("image/png").putExtra(Intent.EXTRA_STREAM,uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
   send.clipData=ClipData.newRawUri(title,uri)
   // The source task is removed immediately after hand-off; the chooser must own another task.
   return Intent.createChooser(send,title).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
  }
  fun handOff(intent:Intent,start:(Intent)->Unit,finish:()->Unit) {start(intent);finish()}
  fun lines(entry:SleepDiaryEntry,includeComment:Boolean=false)=listOf("主观睡眠记录",entry.sleepDate)+DiaryField.entries.map{it.label+"："+fieldValue(DiaryForm.from(entry),it)}+(if(includeComment)entry.comment?.takeIf{it.isNotBlank()}?.let{listOf("特殊情况",it)}.orEmpty()else emptyList())+"主观估计数据 · SleepDiary Public Edition"
 }
 internal suspend fun renderSaved(expected:SleepDiaryEntry,includeComment:Boolean=false):File {
  val entry=checkNotNull(db.diaryDao().byId(expected.id));check(entry==expected){"分享回读不一致"}
  val directory=File(context.cacheDir,"share").apply{mkdirs()}
  val file=File(directory,"SleepDiary_${entry.sleepDate}.png")
  val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply{color=Color.rgb(36,50,58);textSize=32f}
  val wrapped=lines(entry,includeComment).flatMap { line ->
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
 suspend fun prepare(expected:SleepDiaryEntry,includeComment:Boolean=false):Intent {
  val file=renderSaved(expected,includeComment)
  val uri=FileProvider.getUriForFile(context,context.packageName+".files",file)
  return chooser(uri)
 }

 fun cleanupExpired(now:Long=System.currentTimeMillis()) {File(context.cacheDir,"share").listFiles()?.filter{it.isFile&&it.extension=="png"&&now-it.lastModified()>7L*24*60*60*1000}?.forEach{it.delete()}}
}
