package app.sleepdiary.offline.data
import android.content.Context
import android.content.ContentValues
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.backup.*
import app.sleepdiary.offline.domain.model.*
import kotlinx.coroutines.runBlocking
import org.json.*
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.UUID

@RunWith(RobolectricTestRunner::class) @Config(sdk=[28])
class MigrationTest {
 @Test fun migrationPreservesAllFiveTables()=runBlocking {
  val context=ApplicationProvider.getApplicationContext<Context>();val name="migration-${UUID.randomUUID()}.db"
  val file=context.getDatabasePath(name);file.parentFile!!.mkdirs()
  val legacy=android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(file,null)
  val schema=JSONObject(File("schemas/app.sleepdiary.offline.data.db.SleepDiaryDatabase/1.json").readText()).getJSONObject("database")
  val entities=schema.getJSONArray("entities")
  for(i in 0 until entities.length()){val e=entities.getJSONObject(i);legacy.execSQL(e.getString("createSql").replace("\${TABLE_NAME}",e.getString("tableName")));val indices=e.optJSONArray("indices")?:JSONArray();for(j in 0 until indices.length())legacy.execSQL(indices.getJSONObject(j).getString("createSql").replace("\${TABLE_NAME}",e.getString("tableName")))}
  fun put(table:String,o:JSONObject){val cv=ContentValues();o.keys().forEach{k->when(val v=o.get(k)){JSONObject.NULL->cv.putNull(k);is Boolean->cv.put(k,v);is Int->cv.put(k,v);is Number->cv.put(k,v.toDouble());else->cv.put(k,v.toString())}};legacy.insertOrThrow(table,null,cv)}
  val sample=JSONObject(File("../samples/backup-v1.example.json").readText());val entry=sample.getJSONArray("diaryEntries").getJSONObject(0);put("sleep_diary_entry",entry)
  val assessment=sample.getJSONArray("safetyAssessments").getJSONObject(0);val answers=assessment.getJSONArray("answers");assessment.remove("answers");put("safety_assessment",assessment)
  for(i in 0 until answers.length()){val a=answers.getJSONObject(i);a.put("assessmentId",assessment.getString("id"));if(!a.has("detailJson"))a.put("detailJson",JSONObject.NULL);put("safety_answer",a)}
  val state=sample.getJSONObject("trackingState");state.put("singletonId",0);state.put("backupFormatVersion","BACKUP_1.0");put("app_state",state)
  val summaryId=UUID.randomUUID().toString()
  put("weekly_summary_snapshot",JSONObject().put("id",summaryId).put("periodIndex",1).put("periodStartDate","2026-09-08").put("periodEndDate","2026-09-14").put("validEntryCount",1).put("avgSolMin",30.0).put("avgAwakeningCount",3.0).put("avgWasoMin",55.0).put("avgTerminalWakefulnessMin",50.0).put("avgTibMin",475.0).put("avgSleepQuality",2.0).put("sourceEntryIdsJson",JSONArray().put(entry.getString("id")).toString()).put("diarySchemaVersion",Versions.DIARY_SCHEMA).put("weeklySummaryRuleVersion",Versions.WEEKLY_RULE).put("generatedAtUtc","2026-09-14T00:00:00Z").put("revision",1).put("isCurrent",true).put("supersedesSummaryId",JSONObject.NULL))
  legacy.version=1;legacy.close()
  val db=Room.databaseBuilder(context,SleepDiaryDatabase::class.java,name).addMigrations(SleepDiaryDatabase.MIGRATION_1_2).build()
  try {assertEquals(BackupCodec.entry(entry),db.diaryDao().all().single());assertEquals(answers.length(),db.safetyDao().byId(assessment.getString("id"))!!.answers.size);assertEquals(summaryId,db.stateAndSummaryDao().summaries().single().id);val s=db.stateAndSummaryDao().state()!!;assertFalse(s.hasCompletedOnboarding);assertTrue(s.hasSeenMinimalInteractionExplanation);assertEquals(Versions.BACKUP_FORMAT,s.backupFormatVersion)
   db.stateAndSummaryDao().putState(s.copy(hasCompletedOnboarding=true))
  } finally {db.close()}
  val reopened=Room.databaseBuilder(context,SleepDiaryDatabase::class.java,name).build();try{assertTrue(reopened.stateAndSummaryDao().state()!!.hasCompletedOnboarding)}finally{reopened.close();context.deleteDatabase(name)}
 }
}

