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
 @Test fun migrationPreservesAllFiveTables()=verifyMigration(1)
 @Test fun versionTwoMigrationPreservesOnboardingAndAllFiveTables()=verifyMigration(2)
 @Test fun versionThreeMigrationPreservesWorkspace()=verifyMigration(3)
 private fun verifyMigration(from: Int)=runBlocking {
  val context=ApplicationProvider.getApplicationContext<Context>();val name="migration-${UUID.randomUUID()}.db"
  val file=context.getDatabasePath(name);file.parentFile!!.mkdirs()
  val legacy=android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(file,null)
  val schema=JSONObject(File("schemas/app.sleepdiary.offline.data.db.SleepDiaryDatabase/$from.json").readText()).getJSONObject("database")
  val entities=schema.getJSONArray("entities")
  for(i in 0 until entities.length()){val e=entities.getJSONObject(i);legacy.execSQL(e.getString("createSql").replace("\${TABLE_NAME}",e.getString("tableName")));val indices=e.optJSONArray("indices")?:JSONArray();for(j in 0 until indices.length())legacy.execSQL(indices.getJSONObject(j).getString("createSql").replace("\${TABLE_NAME}",e.getString("tableName")))}
  fun put(table:String,o:JSONObject){val cv=ContentValues();o.keys().forEach{k->when(val v=o.get(k)){JSONObject.NULL->cv.putNull(k);is Boolean->cv.put(k,v);is Int->cv.put(k,v);is Number->cv.put(k,v.toDouble());else->cv.put(k,v.toString())}};legacy.insertOrThrow(table,null,cv)}
  val sample=JSONObject(File("../samples/backup-v1.example.json").readText());val entry=sample.getJSONArray("diaryEntries").getJSONObject(0);put("sleep_diary_entry",entry)
  val assessment=sample.getJSONArray("safetyAssessments").getJSONObject(0);val answers=assessment.getJSONArray("answers");assessment.remove("answers");put("safety_assessment",assessment)
  for(i in 0 until answers.length()){val a=answers.getJSONObject(i);a.put("assessmentId",assessment.getString("id"));if(!a.has("detailJson"))a.put("detailJson",JSONObject.NULL);put("safety_answer",a)}
  val state=sample.getJSONObject("trackingState");state.put("singletonId",0);state.put("backupFormatVersion",if(from==1) "BACKUP_1.0" else if(from==2) "BACKUP_1.1" else "BACKUP_2.0");if(from>=2)state.put("hasCompletedOnboarding",true);put("app_state",state)
  val summaryId=UUID.randomUUID().toString()
  put("weekly_summary_snapshot",JSONObject().put("id",summaryId).put("periodIndex",1).put("periodStartDate","2026-09-08").put("periodEndDate","2026-09-14").put("validEntryCount",1).put("avgSolMin",30.0).put("avgAwakeningCount",3.0).put("avgWasoMin",55.0).put("avgTerminalWakefulnessMin",50.0).put("avgTibMin",475.0).put("avgSleepQuality",2.0).put("sourceEntryIdsJson",JSONArray().put(entry.getString("id")).toString()).put("diarySchemaVersion",Versions.DIARY_SCHEMA).put("weeklySummaryRuleVersion",Versions.WEEKLY_RULE).put("generatedAtUtc","2026-09-14T00:00:00Z").put("revision",1).put("isCurrent",true).put("supersedesSummaryId",JSONObject.NULL))
  val legacyWorkspace=if(from==3)JSONObject(File("../samples/backup-v2.example.json").readText()).getJSONArray("workspaceRecords").getJSONObject(0) else null
  if(legacyWorkspace!=null)put("workspace_record",legacyWorkspace)
  legacy.version=from;legacy.close()
  val db=Room.databaseBuilder(context,SleepDiaryDatabase::class.java,name).addMigrations(SleepDiaryDatabase.MIGRATION_1_2,SleepDiaryDatabase.MIGRATION_2_3,SleepDiaryDatabase.MIGRATION_3_4).build()
  try {assertEquals(BackupCodec.entry(entry),db.diaryDao().all().single());assertEquals(answers.length(),db.safetyDao().byId(assessment.getString("id"))!!.answers.size);assertEquals(summaryId,db.stateAndSummaryDao().summaries().single().id);val s=db.stateAndSummaryDao().state()!!;assertEquals(from>=2,s.hasCompletedOnboarding);assertTrue(s.hasSeenMinimalInteractionExplanation);assertEquals(Versions.BACKUP_FORMAT,s.backupFormatVersion)
   assertTrue(db.inputSnapshotDao().all().isEmpty())
   if(legacyWorkspace==null)assertTrue(db.workspaceDao().all().isEmpty()) else assertEquals(BackupCodec.workspace(legacyWorkspace),db.workspaceDao().all().single())
   db.workspaceDao().insert(app.sleepdiary.offline.data.entity.WorkspaceRecord(UUID.randomUUID().toString(),"LEARNING_NOTE","测试","保留内容","2026-09-13T00:00:00Z","2026-09-13T00:00:00Z"))
   db.stateAndSummaryDao().putState(s.copy(hasCompletedOnboarding=true))
  } finally {db.close()}
  val reopened=Room.databaseBuilder(context,SleepDiaryDatabase::class.java,name).build();try{assertTrue(reopened.stateAndSummaryDao().state()!!.hasCompletedOnboarding);assertEquals("保留内容",reopened.workspaceDao().all().single{it.title=="测试"}.body);assertEquals(BackupCodec.entry(entry),reopened.diaryDao().all().single())}finally{reopened.close();context.deleteDatabase(name)}
 }
}
