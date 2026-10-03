package app.sleepdiary.offline.data.backup

import android.content.Context
import androidx.room.withTransaction
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.entity.AppState
import app.sleepdiary.offline.data.education.EducationRegistryLoader
import app.sleepdiary.offline.domain.education.EducationContentIndex
import app.sleepdiary.offline.domain.model.Versions
import org.json.JSONObject
import java.time.Instant

/** Prepared bytes are validated again at confirmation; no database mutation happens during preview. */
class BackupRepository(private val db:SleepDiaryDatabase,private val context:Context,private val checkpoint:(String)->Unit={}) {
 private val educationIndex:EducationContentIndex by lazy { EducationRegistryLoader.fromAssets(context.assets) }
 suspend fun snapshot():BackupData=db.withTransaction {
  val education=db.educationDao();val progress=education.progress();val choices=education.choices();val review=education.reviewStates();val records=education.records();val positions=education.positions()
  val versions=(progress.map{it.contentVersionId}+choices.map{it.contentVersionId}+review.map{it.contentVersionId}+records.map{it.contentVersionId}+positions.map{it.contentVersionId}).toSet()
  BackupData(db.diaryDao().all(),db.safetyDao().all(),db.stateAndSummaryDao().summaries(),db.stateAndSummaryDao().state()?:AppState(),db.workspaceDao().all(),db.inputSnapshotDao().all(),db.treatmentDao().episodes(),db.treatmentDao().audits(),educationIndex.versions(versions),progress,choices,review,records,positions)
 }
 fun preview(text:String):BackupData {
  val o=BackupSchemaValidator.parse(text)
  val file=when(o.getString("exportFormatVersion")){Versions.BACKUP_FORMAT->"backup-v3.1.schema.json";Versions.LEGACY_BACKUP_3_0->"backup-v3.schema.json";Versions.LEGACY_BACKUP_2_1->"backup-v2.1.schema.json";Versions.LEGACY_BACKUP_2_0->"backup-v2.schema.json";Versions.LEGACY_BACKUP_1_1->"backup-v1.1.schema.json";Versions.LEGACY_BACKUP_FORMAT->"backup-v1.schema.json";else->error("不支持的备份版本")}
  val schema=context.assets.open(file).bufferedReader().use{JSONObject(it.readText())}
  BackupSchemaValidator.validate(o,schema)
  return BackupCodec.decode(o,educationIndex)
 }
 suspend fun export():String {
  val text=BackupCodec.encode(snapshot()).toString(2);preview(text);return text
 }
 suspend fun restore(text:String) {
  val expected=preview(text)
  val restoredLocal=db.withTransaction {
   val state=db.stateAndSummaryDao();val safety=db.safetyDao()
   db.treatmentDao().deleteAllAuditsForRestore();db.treatmentDao().deleteAllEpisodesForRestore();checkpoint("afterTreatmentDelete")
   val education=db.educationDao();education.clearRecords();education.clearProgress();education.clearChoices();education.clearReviewStates();education.clearPositions();checkpoint("afterEducationDelete")
   db.inputSnapshotDao().deleteAllForRestore()
   db.workspaceDao().deleteAllForRestore()
   state.deleteState()
   state.summaries().sortedByDescending{it.revision}.forEach{state.deleteSummary(it.id)}
   safety.deleteAllAnswers();safety.deleteAllAssessments();db.diaryDao().deleteAll()
   checkpoint("afterDelete")
   expected.entries.forEach{db.diaryDao().insert(it)}
   checkpoint("afterEntries")
   expected.assessments.forEach{safety.insertAssessment(it.assessment);safety.insertAnswers(it.answers)}
   expected.summaries.sortedBy{it.revision}.forEach{state.insertSummary(it)}
   expected.workspaceRecords.forEach{db.workspaceDao().insert(it)};checkpoint("afterWorkspace")
   expected.educationProgress.forEach{education.putProgress(it)}
   expected.educationCommittedChoices.forEach{education.putChoice(it)}
   expected.educationReviewStates.forEach{education.putReviewState(it)}
   expected.educationRecords.forEach{education.insertRecord(it)}
   expected.educationPositions.forEach{education.putPosition(it)}
   checkpoint("afterEducation")
   expected.inputSnapshots.forEach{db.inputSnapshotDao().insert(it)};checkpoint("afterSnapshots")
   state.putState(expected.state);checkpoint("afterState")
   expected.treatmentEpisodes.forEach{db.treatmentDao().insertEpisode(it)}
   expected.treatmentAuditEvents.sortedWith(compareBy({it.episodeId},{it.toRevision})).forEach{db.treatmentDao().insertAudit(it)}
   checkpoint("afterTreatment")
   val local=app.sleepdiary.offline.data.repository.LocalExecutionRepository(db).invalidateForRestore()
   checkpoint("afterLocalState")
   check(equivalent(snapshot(),expected)){"恢复回读不一致"}
   local
  }
  check(equivalent(snapshot(),expected)){"恢复提交后回读不一致"}
  check(db.localExecutionDao().state()==restoredLocal){"本机恢复标识提交后回读不一致"}
 }
 private fun equivalent(a:BackupData,b:BackupData)=BackupCodec.encode(a,Instant.EPOCH).toString()==BackupCodec.encode(b,Instant.EPOCH).toString()
 suspend fun csv():ByteArray {
  val header="id,sleep_date,bed_entered_at_local,try_sleep_at_local,sleep_onset_latency_min,awakening_count,wake_after_sleep_onset_min,final_awakening_at_local,out_of_bed_at_local,sleep_quality_1_to_5,comment,time_zone_id_at_entry,created_at_utc,updated_at_utc,diary_schema_version,consistency_warning"
  fun quote(v:Any?):String {val s=v?.toString()?:"";return if(s.any{it==','||it=='\"'||it=='\n'||it=='\r'})"\""+s.replace("\"","\"\"")+"\"" else s}
  val rows=snapshot().entries.map{e->listOf(e.id,e.sleepDate,e.bedEnteredAtLocal,e.trySleepAtLocal,e.sleepOnsetLatencyMin,e.awakeningCount,e.wakeAfterSleepOnsetMin,e.finalAwakeningAtLocal,e.outOfBedAtLocal,e.sleepQuality,e.comment,e.timeZoneIdAtEntry,e.createdAtUtc,e.updatedAtUtc,e.diarySchemaVersion,e.consistencyWarning).joinToString(",",transform=::quote)}
  return ("\uFEFF"+header+"\r\n"+rows.joinToString("\r\n")+if(rows.isEmpty())"" else "\r\n").toByteArray(Charsets.UTF_8)
 }
}
