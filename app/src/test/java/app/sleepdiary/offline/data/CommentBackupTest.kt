package app.sleepdiary.offline.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.sleepdiary.offline.clockOn
import app.sleepdiary.offline.diary
import app.sleepdiary.offline.data.backup.*
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.entity.*
import app.sleepdiary.offline.data.repository.*
import app.sleepdiary.offline.ui.diary.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34])
class CommentBackupTest {
 private val context:Context get()=ApplicationProvider.getApplicationContext()
 private lateinit var db:SleepDiaryDatabase
 private val repo get()=BackupRepository(db,context)
 private val note="中文 English 123,\"引号\"\r\n第二行 🙂"
 @Before fun setup(){db=Room.inMemoryDatabaseBuilder(context,SleepDiaryDatabase::class.java).build()}
 @After fun close(){db.close()}
 private suspend fun seedAll() {
  db.diaryDao().insert(diary().copy(comment=note))
  db.stateAndSummaryDao().putState(AppState(trackingStartDate="2026-09-09",hasCompletedOnboarding=true))
  val a=UUID.randomUUID().toString()
  SafetyRepository(db).save(a,listOf(SafetyAnswer(a,"S01","YES",detailJson=null)),false)
  val b=UUID.randomUUID().toString()
  SafetyRepository(db).save(b,(1..15).map {n->SafetyAnswer(b,"S"+n.toString().padStart(2,'0'),when(n){1->"YES";8,9->"NONE";11->"SAFE";else->"NO"},detailJson=null)},true)
  val clock=clockOn("2026-09-20")
  WeeklySummaryRepository(db,clock).generate(1)
  val e=db.diaryDao().all().single()
  assertTrue(DiaryRepository(db,clock).save(e.copy(comment=note+"\n修改")) is SaveResult.Success)
 }
 @Test fun fullBackupRoundTripKeepsCommentsSafetyHistoryAndSummaryRevisions()=runBlocking {
  seedAll();val text=repo.export();val expected=repo.preview(text)
  assertEquals(2,expected.assessments.size);assertEquals(2,expected.summaries.size)
  assertEquals(note+"\n修改",expected.entries.single().comment)
  db.diaryDao().update(db.diaryDao().all().single().copy(comment="本机后来修改"))
  repo.restore(text)
  assertEquals(expected,repo.snapshot())
 }
 @Test fun bothBackupVersionsRestoreNullEmptyAndUnicodeComments()=runBlocking {
  for(version in listOf("BACKUP_1.0","BACKUP_1.1"))for(comment in listOf(null,"",note)) {
   val o=JSONObject(java.io.File("../samples/backup-v1.1.example.json").readText())
   o.put("exportFormatVersion",version)
   if(version=="BACKUP_1.0")o.getJSONObject("trackingState").remove("hasCompletedOnboarding")
   o.getJSONArray("diaryEntries").getJSONObject(0).put("comment",comment?:JSONObject.NULL)
   repo.restore(o.toString())
   assertEquals(comment,repo.snapshot().entries.single().comment)
   if(version=="BACKUP_1.0")assertFalse(repo.snapshot().state.hasCompletedOnboarding)
  }
 }
 @Test fun failedReplaceAtEveryCheckpointPreservesAllTablesAndComment()=runBlocking {
  seedAll();val before=repo.snapshot()
  for(v in listOf("v1","v1.1"))for(stage in listOf("afterDelete","afterEntries","afterState")) {
   val faulty=BackupRepository(db,context){if(it==stage)error("injected")}
   assertTrue(runCatching{faulty.restore(java.io.File("../samples/backup-$v.example.json").readText())}.isFailure)
   assertEquals(before,repo.snapshot())
  }
 }
 @Test fun corruptCommentInEitherVersionCannotMutateDatabase()=runBlocking {
  seedAll();val before=repo.snapshot()
  for(v in listOf("v1","v1.1")) {
   val o=JSONObject(java.io.File("../samples/backup-$v.example.json").readText())
   o.getJSONArray("diaryEntries").getJSONObject(0).put("comment",42)
   assertTrue(runCatching{repo.restore(o.toString())}.isFailure)
   assertEquals(before,repo.snapshot())
  }
 }
 @OptIn(ExperimentalCoroutinesApi::class)
 @Test fun viewModelCommentSaveReadbackAndBackupRestoreAreConsistent()=runBlocking {
  Dispatchers.setMain(Dispatchers.Unconfined)
  try {
   val store=RoomDiaryUiStore(db);store.completeOnboarding()
   val model=DiaryViewModel(store,clock=clockOn("2026-09-09"))
   model.state.first{it.page!=HomePage.LOADING}
   model.edit(diary());model.comment(note);model.save()
   val saved=model.state.first{it.page==HomePage.SUCCESS}
   assertEquals(note,db.diaryDao().byId(saved.form.id)!!.comment)
   val backup=repo.export();repo.restore(backup)
   assertEquals(note,DiaryForm.from(store.entries().single()).comment)
  } finally {Dispatchers.resetMain()}
 }
 @Test fun csvRoundTripParsingKeepsCommaQuotesNewlinesUnicodeAndEmptyCells()=runBlocking {
  for((i,c) in listOf(note,null,"","   ").withIndex())db.diaryDao().insert(diary(java.time.LocalDate.of(2026,9,9).plusDays(i.toLong()).toString()).copy(comment=c))
  val bytes=repo.csv();assertArrayEquals(byteArrayOf(-17,-69,-65),bytes.take(3).toByteArray())
  val rows=parseCsv(bytes.toString(Charsets.UTF_8).removePrefix("\uFEFF"))
  val column=rows.first().indexOf("comment");assertTrue(column>=0)
  assertEquals(listOf(note,"","","   "),rows.drop(1).map{it[column]})
  assertTrue(rows.all{it.size==rows.first().size})
 }
 private fun parseCsv(text:String):List<List<String>> {
  val rows=mutableListOf<List<String>>();var row=mutableListOf<String>();val value=StringBuilder();var quoted=false;var i=0
  while(i<text.length){val c=text[i]
   if(c=='"'){if(quoted&&i+1<text.length&&text[i+1]=='"'){value.append('"');i++}else quoted=!quoted}
   else if(!quoted&&c==','){row.add(value.toString());value.setLength(0)}
   else if(!quoted&&(c=='\r'||c=='\n')){row.add(value.toString());rows.add(row);row=mutableListOf();value.setLength(0);if(c=='\r'&&i+1<text.length&&text[i+1]=='\n')i++}
   else value.append(c)
   i++
  }
  check(!quoted);return rows
 }
}
