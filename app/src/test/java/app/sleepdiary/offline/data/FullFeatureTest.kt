package app.sleepdiary.offline.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.entity.*
import app.sleepdiary.offline.data.repository.*
import app.sleepdiary.offline.data.backup.*
import app.sleepdiary.offline.domain.model.*
import app.sleepdiary.offline.domain.validation.DiaryValidator
import app.sleepdiary.offline.diary
import app.sleepdiary.offline.share.DiaryShare
import app.sleepdiary.offline.ui.*
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID
import org.json.JSONObject

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34])
class FullFeatureTest {
 private val context:Context get()=ApplicationProvider.getApplicationContext()
 private lateinit var db:SleepDiaryDatabase
 @Before fun setup(){db=Room.inMemoryDatabaseBuilder(context,SleepDiaryDatabase::class.java).build()}
 @After fun close(){db.close()}
 private fun answers(id:String)=(1..15).map {n->val q="S"+n.toString().padStart(2,'0');SafetyAnswer(id,q,when(n){1->"YES";8,9->"NONE";11->"SAFE";else->"NO"},detailJson=null)}
 @Test fun completedSafetyHasAllRawAnswers()=runBlocking {val id=UUID.randomUUID().toString();val result=SafetyRepository(db).save(id,answers(id),true);assertEquals(15,result.answers.size);assertEquals(AssessmentStatus.COMPLETED,result.assessment.status);assertEquals(SafetyGate.NO_MAJOR_FLAG,result.assessment.derivedGate)}
 @Test fun incompleteCannotBeMarkedCompleted()=runBlocking{val id=UUID.randomUUID().toString();assertTrue(runCatching{SafetyRepository(db).save(id,answers(id).take(3),true)}.isFailure);assertTrue(db.safetyDao().all().isEmpty())}
 @Test fun reassessmentPreservesHistory()=runBlocking{val r=SafetyRepository(db);val a=UUID.randomUUID().toString();r.save(a,answers(a),true);val b=UUID.randomUUID().toString();r.save(b,answers(b),true);assertEquals(2,db.safetyDao().all().size);assertFalse(db.safetyDao().byId(a)!!.assessment.isCurrent);assertEquals(b,db.stateAndSummaryDao().state()!!.currentSafetyAssessmentId)}
 @Test fun bothS03BranchesPersistImmediateDraft()=runBlocking{for(code in listOf("YES","UNSURE")){val id=UUID.randomUUID().toString();val x=SafetyRepository(db).save(id,listOf(SafetyAnswer(id,"S03",code,detailJson=null)),false);assertEquals(AssessmentStatus.DRAFT,x.assessment.status);assertEquals(SafetyGate.DELAY_AUTOMATED_TREATMENT,x.assessment.derivedGate);assertEquals(code,db.safetyDao().byId(id)!!.answers.single().answerCode)}}
 @Test fun onboardingWithoutDiaryPersistsIndependently()=runBlocking{val store=RoomDiaryUiStore(db);store.completeOnboarding();assertTrue(store.state().hasCompletedOnboarding);assertTrue(store.entries().isEmpty());assertFalse(store.state().hasSeenMinimalInteractionExplanation)}
 @Test fun onboardingWithDraftPersistsIndependently()=runBlocking{val id=UUID.randomUUID().toString();SafetyRepository(db).save(id,listOf(SafetyAnswer(id,"S03","YES",detailJson=null)),false);val store=RoomDiaryUiStore(db);store.completeOnboarding();assertTrue(store.state().hasCompletedOnboarding);assertEquals(AssessmentStatus.DRAFT,store.currentSafety()!!.assessment.status)}
 @Test fun shareContentContainsEightRawFieldsAndSavedComment(){val e=diary().copy(comment="SAVED COMMENT");val lines=DiaryShare.lines(e);assertEquals(13,lines.size);assertTrue(lines.contains("SAVED COMMENT"));assertFalse(lines.joinToString().contains("Safety"));assertFalse(lines.joinToString().contains("TST"))}
 @Test fun shareRefusesMissingOrChangedEntry()=runBlocking{val e=diary();assertTrue(runCatching{DiaryShare(context,db).prepare(e)}.isFailure);db.diaryDao().insert(e.copy(comment="changed"));assertTrue(runCatching{DiaryShare(context,db).prepare(e)}.isFailure)}
 @Test fun evidenceOnlyContainsApprovedSources(){
  assertEquals(8,EVIDENCE_SOURCES.size)
  val evidence=java.io.File("../EVIDENCE.md").readText()
  val selectedManual="https://www.mirecc.va.gov/visn19/treatmentworksforvets/docs/CBT-I-Therapist-Manual.pdf"
  val additions=EVIDENCE_SOURCES.filterNot{evidence.contains(it.url)}
  assertEquals(listOf(selectedManual),additions.map{it.url})
  assertTrue(java.io.File("../docs/PROTOCOL_SOURCE_CHECK_20260913.md").readText().contains(selectedManual))
 }
 @Test fun officialSourceOpensExternalBrowser(){val intent=officialLinkIntent(EVIDENCE_SOURCES.first().url);assertEquals(Intent.ACTION_VIEW,intent.action);assertEquals(EVIDENCE_SOURCES.first().url,intent.data.toString());assertTrue(runCatching{officialLinkIntent("https://unapproved.invalid")}.isFailure)}
 @Test fun requiredTermsPresent(){for(term in listOf("CBT-I","CSD","SOL","WASO","TIB","OSA","RLS","RBD"))assertTrue(TERMS.any{it.first.startsWith(term+" ·")})}
 @Test fun exactPairConflictReturned(){val e=diary().copy(trySleepAtLocal="2026-09-08T22:40:00");val v=DiaryValidator.validate(e);assertFalse(v.canSave);assertEquals(listOf("bedEnteredAtLocal","trySleepAtLocal"),v.conflicts.single().fields);assertEquals("‘开始尝试入睡’不能早于‘进入睡觉位置’。",v.conflicts.single().message)}
 @Test fun correctedTimeClearsOnlyRealConflict(){val e=diary().copy(outOfBedAtLocal="2026-09-09T05:00:00");assertEquals(listOf("finalAwakeningAtLocal","outOfBedAtLocal"),DiaryValidator.validate(e).conflicts.single().fields);assertTrue(DiaryValidator.validate(e.copy(outOfBedAtLocal="2026-09-09T07:00:00")).conflicts.isEmpty())}
 @Test fun runtimePermissionsRemainOffline(){val info=context.packageManager.getPackageInfo(context.packageName,android.content.pm.PackageManager.GET_PERMISSIONS);val permissions=info.requestedPermissions?.toSet()?:emptySet();listOf("android.permission.INTERNET","android.permission.ACCESS_NETWORK_STATE","android.permission.MANAGE_EXTERNAL_STORAGE").forEach{assertFalse(it in permissions)}}
 @Test fun invalidReferenceDoesNotModifyExistingData()=runBlocking{val r=BackupRepository(db,context);val o=JSONObject(java.io.File("../samples/backup-v1.1.example.json").readText());o.getJSONObject("trackingState").put("currentSafetyAssessmentId",UUID.randomUUID().toString());val before=r.snapshot();assertTrue(runCatching{r.restore(o.toString())}.isFailure);assertEquals(before,r.snapshot())}
}
