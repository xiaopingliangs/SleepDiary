package app.sleepdiary.offline.data
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.backup.*
import app.sleepdiary.offline.data.entity.*
import app.sleepdiary.offline.data.repository.SafetyRepository
import app.sleepdiary.offline.diary
import app.sleepdiary.offline.domain.model.*
import kotlinx.coroutines.runBlocking
import org.json.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.UUID

@RunWith(RobolectricTestRunner::class) @Config(sdk=[28])
class BackupTest {
 private val context:Context get()=ApplicationProvider.getApplicationContext()
 private lateinit var db:SleepDiaryDatabase
 private val repo get()=BackupRepository(db,context)
 @Before fun setup(){db=Room.inMemoryDatabaseBuilder(context,SleepDiaryDatabase::class.java).build()}
 @After fun close(){db.close()}
 private fun sample(v:String)=File("../samples/backup-$v.example.json").readText()
 private suspend fun seed(done:Boolean=true){db.diaryDao().insert(diary());db.stateAndSummaryDao().putState(AppState(trackingStartDate="2026-09-09",hasCompletedOnboarding=done))}
 @Test fun freshStateDoesNotClaimOnboarding(){assertFalse(AppState().hasCompletedOnboarding)}
 @Test fun previousExamplePasses(){assertTrue(repo.preview(sample("v1.1")).state.hasCompletedOnboarding)}
 @Test fun legacyExampleDefaultsFalse(){assertFalse(repo.preview(sample("v1")).state.hasCompletedOnboarding)}
 @Test fun trueRoundTripReplacesData()=runBlocking{seed();val expected=repo.snapshot();val text=repo.export();assertEquals("BACKUP_2.1",JSONObject(text).getString("exportFormatVersion"));db.diaryDao().insert(diary("2026-09-10"));repo.restore(text);assertEquals(expected,repo.snapshot())}
 @Test fun falseRoundTrip()=runBlocking{seed(false);val text=repo.export();db.stateAndSummaryDao().putState(checkNotNull(db.stateAndSummaryDao().state()).copy(hasCompletedOnboarding=true));repo.restore(text);assertFalse(repo.snapshot().state.hasCompletedOnboarding)}
 @Test fun legacyRestore()=runBlocking{seed();repo.restore(sample("v1"));assertEquals("2026-09-08",repo.snapshot().entries.single().sleepDate);assertFalse(repo.snapshot().state.hasCompletedOnboarding);assertEquals(AssessmentStatus.DRAFT,repo.snapshot().assessments.single().assessment.status)}
 @Test fun bothCorruptVersionsPreserveDatabase()=runBlocking{seed();val before=repo.snapshot();for(v in listOf("v1","v1.1")){val o=JSONObject(sample(v));o.getJSONArray("diaryEntries").getJSONObject(0).put("sleepQuality",9);assertTrue(runCatching{repo.restore(o.toString())}.isFailure);assertEquals(before,repo.snapshot())}}
 @Test fun rollbackAtEveryMutationStage()=runBlocking{seed();val before=repo.snapshot();for(stage in listOf("afterDelete","afterEntries","afterState")){val faulty=BackupRepository(db,context){if(it==stage)error("injected")};assertTrue(runCatching{faulty.restore(sample("v1.1"))}.isFailure);assertEquals(before,repo.snapshot())}}
 @Test fun newFormatRequiresBoolean(){val o=JSONObject(sample("v1.1"));o.getJSONObject("trackingState").remove("hasCompletedOnboarding");assertTrue(runCatching{repo.preview(o.toString())}.isFailure)}
 @Test fun unknownVersionRejected(){assertTrue(runCatching{repo.preview("{\"exportFormatVersion\":\"BACKUP_9\"}")}.isFailure)}
 @Test fun duplicateAndLenientJsonRejected(){for(s in listOf("{\"a\":1,\"a\":2}","{a:1}","{\"a\":1} trailing","{\"a\":NaN}"))assertTrue(s,runCatching{BackupSchemaValidator.parse(s)}.isFailure)}
 @Test fun csvBomAndEscaping()=runBlocking{db.diaryDao().insert(diary().copy(comment="中文,\"备注\"\n第二行"));val b=repo.csv();assertArrayEquals(byteArrayOf(-17,-69,-65),b.take(3).toByteArray());val text=b.toString(Charsets.UTF_8);assertTrue(text.contains("\"中文,\"\"备注\"\"\n第二行\""));assertFalse(text.contains("TST"));assertFalse(text.contains("Safety"))}
 @Test fun draftAndOnboardingIndependent()=runBlocking{db.stateAndSummaryDao().putState(AppState(hasCompletedOnboarding=true));val id=UUID.randomUUID().toString();SafetyRepository(db).save(id,listOf(SafetyAnswer(id,"S03","YES",detailJson=null)),false);assertTrue(repo.snapshot().state.hasCompletedOnboarding);assertTrue(repo.snapshot().entries.isEmpty())}
 @Test fun acuteSignalPreserved()=runBlocking{val id=UUID.randomUUID().toString();val r=SafetyRepository(db);r.save(id,listOf(SafetyAnswer(id,"S03","UNSURE",detailJson=null)),false);assertTrue(runCatching{r.save(id,listOf(SafetyAnswer(id,"S03","NO",detailJson=null)),false)}.isFailure);assertEquals("UNSURE",db.safetyDao().byId(id)!!.answers.single().answerCode)}

 @Test fun actualPickerMinuteValuesCanExportWithoutChangingStoredEntry()=runBlocking {
  val raw=diary().copy(bedEnteredAtLocal="2026-09-08T23:00",trySleepAtLocal="2026-09-08T23:30",finalAwakeningAtLocal="2026-09-09T06:00",outOfBedAtLocal="2026-09-09T07:00")
  db.diaryDao().insert(raw);db.stateAndSummaryDao().putState(AppState(trackingStartDate="2026-09-09",hasCompletedOnboarding=true))
  val text=repo.export();val exported=JSONObject(text).getJSONArray("diaryEntries").getJSONObject(0)
  assertEquals("2026-09-08T23:00:00",exported.getString("bedEnteredAtLocal"));assertEquals(raw,db.diaryDao().byId(raw.id));assertEquals(java.time.LocalDateTime.parse(raw.bedEnteredAtLocal),java.time.LocalDateTime.parse(repo.preview(text).entries.single().bedEnteredAtLocal))
 }
}

