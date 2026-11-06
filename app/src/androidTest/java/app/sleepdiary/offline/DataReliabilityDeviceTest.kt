package app.sleepdiary.offline

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.entity.*
import app.sleepdiary.offline.data.repository.*
import app.sleepdiary.offline.data.backup.BackupRepository
import app.sleepdiary.offline.share.DiaryShare
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.time.*
import java.util.UUID

/** Writes only a unique test database, never the user's sleepdiary.db. */
@RunWith(AndroidJUnit4::class)
class DataReliabilityDeviceTest {
 private val context:Context get()=ApplicationProvider.getApplicationContext()
 private lateinit var db:SleepDiaryDatabase
 private lateinit var name:String
 private val start=LocalDate.of(2026,8,1)
 private val note="特殊情况 中文 English 123,\"引号\"\r\n第二行"
 private fun clock(day:Int)=Clock.fixed(start.plusDays(day-1L).atTime(12,0).atZone(ZoneId.of("Asia/Shanghai")).toInstant(),ZoneId.of("Asia/Shanghai"))
 private fun entry(day:Int):SleepDiaryEntry {
  val d=start.plusDays(day-1L)
  return SleepDiaryEntry(UUID.randomUUID().toString(),d.toString(),"${d.minusDays(1)}T23:00:00","${d.minusDays(1)}T23:30:00",20,2,30,"${d}T06:00:00","${d}T07:00:00",3,note,"Asia/Shanghai",clock(day).instant().toString(),clock(day).instant().toString(),consistencyWarning=false)
 }
 @Before fun setup(){name="device-test-${UUID.randomUUID()}.db";open()}
 private fun open(){db=Room.databaseBuilder(context,SleepDiaryDatabase::class.java,name).addMigrations(SleepDiaryDatabase.MIGRATION_1_2,SleepDiaryDatabase.MIGRATION_2_3,SleepDiaryDatabase.MIGRATION_3_4,SleepDiaryDatabase.MIGRATION_4_5,SleepDiaryDatabase.MIGRATION_5_6,SleepDiaryDatabase.MIGRATION_6_7).build()}
 @After fun close(){db.close();context.deleteDatabase(name)}
 private suspend fun save(day:Int):SaveResult.Success {
  val result=DiaryRepository(db,clock(day)).save(entry(day));assertTrue(result.toString(),result is SaveResult.Success)
  return result as SaveResult.Success
 }
 private suspend fun thirtyDays(){for(day in 1..30){val result=save(day);if(day%7==0){assertEquals(day/7,result.pendingSummary!!.periodIndex);assertEquals(7,result.pendingSummary.validEntryCount);RoomDiaryUiStore(db).acknowledgeWeekly(day/7)}else assertNull("day $day",result.pendingSummary)}}
 @Test fun thirtyDaysPersistWithFourSevenDaySummaries()=runBlocking {
  thirtyDays();val before=BackupRepository(db,context).snapshot();assertEquals(30,before.entries.size);assertEquals(listOf(1,2,3,4),before.summaries.map{it.periodIndex}.sorted())
  before.summaries.forEach{assertEquals(20.0,it.avgSolMin!!,0.00001);assertEquals(480.0,it.avgTibMin!!,0.00001)}
  db.close();open();assertEquals(before,BackupRepository(db,context).snapshot())
 }
 @Test fun missedSeventhDayAndEmptyWeek()=runBlocking {
  for(day in 1..6)save(day)
  assertEquals(6,save(8).pendingSummary!!.validEntryCount);RoomDiaryUiStore(db).acknowledgeWeekly(1)
  assertEquals(2,save(29).pendingSummary!!.periodIndex);RoomDiaryUiStore(db).acknowledgeWeekly(2)
  assertNull(save(30).pendingSummary)
 }
 @Test fun completeBackupLegacyAndRollbackPreserveEveryTable()=runBlocking {
  thirtyDays();RoomDiaryUiStore(db).completeOnboarding()
  for(complete in listOf(false,true)){val id=UUID.randomUUID().toString();val answers=if(!complete)listOf(SafetyAnswer(id,"S03","UNSURE",detailJson=null))else (1..15).map{n->SafetyAnswer(id,"S"+n.toString().padStart(2,'0'),when(n){1->"YES";8,9->"NONE";11->"SAFE";else->"NO"},detailJson=null)};SafetyRepository(db).save(id,answers,complete)}
  val original=db.diaryDao().all().first();assertTrue(DiaryRepository(db,clock(30)).save(original.copy(comment=note+" edited")) is SaveResult.Success)
  val repo=BackupRepository(db,context);val text=repo.export();val before=repo.snapshot();assertEquals("BACKUP_3.1",JSONObject(text).getString("exportFormatVersion"));assertEquals(2,before.assessments.size);assertEquals(5,before.summaries.size)
  repo.restore(text);assertEquals(before,repo.snapshot())
  val legacy=JSONObject(text).put("exportFormatVersion","BACKUP_1.0");legacy.getJSONObject("trackingState").remove("hasCompletedOnboarding")
  repo.restore(legacy.toString());assertFalse(repo.snapshot().state.hasCompletedOnboarding);assertEquals(before.entries,repo.snapshot().entries);repo.restore(text)
  for(document in listOf(text,legacy.toString())) {
   for(stage in listOf("afterDelete","afterEntries","afterState")){assertTrue(runCatching{BackupRepository(db,context){if(it==stage)error("injected")}.restore(document)}.isFailure);assertEquals(before,repo.snapshot())}
   val bad=JSONObject(document);bad.getJSONArray("diaryEntries").getJSONObject(0).put("comment",42);assertTrue(runCatching{repo.restore(bad.toString())}.isFailure);assertEquals(before,repo.snapshot())
  }
 }
 @Test fun editedAndClearedCommentPersistsAndIsIncludedInShare()=runBlocking {
  val saved=save(1).entry;val lines=DiaryShare.lines(saved);assertTrue(lines.joinToString().contains(note));assertFalse(lines.joinToString().contains("Safety"))
  for(value in listOf("改过的备注 123",null,"")){val result=DiaryRepository(db,clock(2)).save(saved.copy(comment=value)) as SaveResult.Success;assertEquals(value,db.diaryDao().byId(saved.id)!!.comment);assertEquals(saved.createdAtUtc,result.entry.createdAtUtc)}
 }
 @Test fun csvEscapesFreeText()=runBlocking {
  save(1);val text=BackupRepository(db,context).csv().toString(Charsets.UTF_8);assertTrue(text.startsWith("\uFEFF"));assertTrue(text.contains("\""+note.replace("\"","\"\"")+"\""));assertFalse(text.contains("SafetyAnswer"))
 }
}
