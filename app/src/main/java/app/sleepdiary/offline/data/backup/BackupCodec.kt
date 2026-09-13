package app.sleepdiary.offline.data.backup

import app.sleepdiary.offline.data.entity.*
import app.sleepdiary.offline.data.dao.AssessmentWithAnswers
import app.sleepdiary.offline.domain.model.*
import app.sleepdiary.offline.domain.safety.*
import app.sleepdiary.offline.domain.validation.DiaryValidator
import app.sleepdiary.offline.domain.weekly.PeriodCalculator
import app.sleepdiary.offline.data.repository.WorkspaceValidation
import org.json.*
import java.time.*

internal fun JSONObject.textOrNull(k:String):String?=if(isNull(k))null else getString(k)
internal fun JSONObject.doubleOrNull(k:String):Double?=if(isNull(k))null else getDouble(k)
internal fun JSONArray.objects()=(0 until length()).map{getJSONObject(it)}
internal fun JSONArray.strings()=(0 until length()).map{getString(it)}
internal fun obj(vararg pairs:Pair<String,Any?>)=JSONObject().apply{pairs.forEach{put(it.first,it.second?:JSONObject.NULL)}}

data class BackupData(val entries:List<SleepDiaryEntry>,val assessments:List<AssessmentWithAnswers>,val summaries:List<WeeklySummarySnapshot>,val state:AppState,val workspaceRecords:List<WorkspaceRecord> = emptyList())

object BackupCodec {
 fun workspaceJson(r:WorkspaceRecord)=obj("id" to r.id,"kind" to r.kind,"title" to r.title,"body" to r.body,
  "createdAtUtc" to r.createdAtUtc,"updatedAtUtc" to r.updatedAtUtc,"revision" to r.revision,"workspaceSchemaVersion" to r.workspaceSchemaVersion)
 fun workspace(o:JSONObject)=WorkspaceRecord(o.getString("id"),o.getString("kind"),o.getString("title"),o.getString("body"),
  o.getString("createdAtUtc"),o.getString("updatedAtUtc"),o.getInt("revision"),o.getString("workspaceSchemaVersion"))
 private fun backupLocalTime(value:String):String {
  val time=java.time.LocalDateTime.parse(value)
  require(time.nano==0){"备份时间不支持小数秒"}
  return time.format(java.time.format.DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss"))
 }
 fun entryJson(e:SleepDiaryEntry)=obj("id" to e.id,"sleepDate" to e.sleepDate,"bedEnteredAtLocal" to backupLocalTime(e.bedEnteredAtLocal),"trySleepAtLocal" to backupLocalTime(e.trySleepAtLocal),
  "sleepOnsetLatencyMin" to e.sleepOnsetLatencyMin,"awakeningCount" to e.awakeningCount,"wakeAfterSleepOnsetMin" to e.wakeAfterSleepOnsetMin,
  "finalAwakeningAtLocal" to backupLocalTime(e.finalAwakeningAtLocal),"outOfBedAtLocal" to backupLocalTime(e.outOfBedAtLocal),"sleepQuality" to e.sleepQuality,"comment" to e.comment,
  "timeZoneIdAtEntry" to e.timeZoneIdAtEntry,"createdAtUtc" to e.createdAtUtc,"updatedAtUtc" to e.updatedAtUtc,"diarySchemaVersion" to e.diarySchemaVersion,"consistencyWarning" to e.consistencyWarning)
 fun entry(o:JSONObject)=SleepDiaryEntry(o.getString("id"),o.getString("sleepDate"),o.getString("bedEnteredAtLocal"),o.getString("trySleepAtLocal"),o.getInt("sleepOnsetLatencyMin"),o.getInt("awakeningCount"),o.getInt("wakeAfterSleepOnsetMin"),o.getString("finalAwakeningAtLocal"),o.getString("outOfBedAtLocal"),o.getInt("sleepQuality"),o.textOrNull("comment"),o.getString("timeZoneIdAtEntry"),o.getString("createdAtUtc"),o.getString("updatedAtUtc"),o.getString("diarySchemaVersion"),o.getBoolean("consistencyWarning"))
 fun encode(data:BackupData,now:Instant=Instant.now()):JSONObject {
  val s=data.state
  return obj("exportFormatVersion" to Versions.BACKUP_FORMAT,"appVersion" to Versions.APP,"exportedAtUtc" to now.toString(),"diarySchemaVersion" to Versions.DIARY_SCHEMA,"safetySchemaVersion" to Versions.SAFETY_SCHEMA,"safetyRuleVersion" to Versions.SAFETY_RULE,"weeklySummaryRuleVersion" to Versions.WEEKLY_RULE,"cbtiProtocolVersion" to Versions.CBTI_PROTOCOL,
   "diaryEntries" to JSONArray(data.entries.sortedBy{it.sleepDate}.map(::entryJson)),
   "safetyAssessments" to JSONArray(data.assessments.sortedBy{it.assessment.id}.map { pair -> val a=pair.assessment
    obj("id" to a.id,"answeredAtUtc" to a.answeredAtUtc,"safetySchemaVersion" to a.safetySchemaVersion,"safetyRuleVersion" to a.safetyRuleVersion,"status" to a.status.name,"derivedGate" to a.derivedGate.name,"isCurrent" to a.isCurrent,
     "answers" to JSONArray(pair.answers.sortedBy{it.questionId}.map {answer->
      val detail=if(answer.questionId=="S09")S09Codec.canonicalize(answer).detailJson else answer.detailJson
      obj("questionId" to answer.questionId,"answerCode" to answer.answerCode,"safetySchemaVersion" to answer.safetySchemaVersion,"detailJson" to detail?.let(::JSONObject))
     })) }),
   "weeklySummaries" to JSONArray(data.summaries.sortedWith(compareBy({it.periodIndex},{it.revision})).map {v->obj("id" to v.id,"periodIndex" to v.periodIndex,"periodStartDate" to v.periodStartDate,"periodEndDate" to v.periodEndDate,"validEntryCount" to v.validEntryCount,"avgSolMin" to v.avgSolMin,"avgAwakeningCount" to v.avgAwakeningCount,"avgWasoMin" to v.avgWasoMin,"avgTerminalWakefulnessMin" to v.avgTerminalWakefulnessMin,"avgTibMin" to v.avgTibMin,"avgSleepQuality" to v.avgSleepQuality,"sourceEntryIds" to JSONArray(v.sourceEntryIdsJson),"diarySchemaVersion" to v.diarySchemaVersion,"weeklySummaryRuleVersion" to v.weeklySummaryRuleVersion,"generatedAtUtc" to v.generatedAtUtc,"revision" to v.revision,"isCurrent" to v.isCurrent,"supersedesSummaryId" to v.supersedesSummaryId)}),
   "trackingState" to obj("trackingStartDate" to s.trackingStartDate,"hasSeenMinimalInteractionExplanation" to s.hasSeenMinimalInteractionExplanation,"hasSeenSharePrivacyNotice" to s.hasSeenSharePrivacyNotice,"lastDisplayedWeeklyPeriodIndex" to s.lastDisplayedWeeklyPeriodIndex,"currentSafetyAssessmentId" to s.currentSafetyAssessmentId,"hasCompletedOnboarding" to s.hasCompletedOnboarding),
   "workspaceSchemaVersion" to Versions.WORKSPACE_SCHEMA,"workspaceRecords" to JSONArray(data.workspaceRecords.sortedBy{it.id}.map(::workspaceJson)))
 }
 fun decode(o:JSONObject):BackupData {
  val format=o.getString("exportFormatVersion")
  require(format in setOf(Versions.BACKUP_FORMAT,Versions.LEGACY_BACKUP_1_1,Versions.LEGACY_BACKUP_FORMAT))
  val records=if(format==Versions.BACKUP_FORMAT){
   require(o.getString("workspaceSchemaVersion")==Versions.WORKSPACE_SCHEMA)
   o.getJSONArray("workspaceRecords").objects().map(::workspace)
  }else{require(!o.has("workspaceRecords")&&!o.has("workspaceSchemaVersion"));emptyList()}
  val s=o.getJSONObject("trackingState")
  val data=BackupData(o.getJSONArray("diaryEntries").objects().map(::entry),
   o.getJSONArray("safetyAssessments").objects().map {a->val id=a.getString("id")
    AssessmentWithAnswers(SafetyAssessment(id,a.getString("answeredAtUtc"),a.getString("safetySchemaVersion"),SafetyGate.valueOf(a.getString("derivedGate")),a.getString("safetyRuleVersion"),AssessmentStatus.valueOf(a.getString("status")),a.getBoolean("isCurrent")),a.getJSONArray("answers").objects().map {x->SafetyAnswer(id,x.getString("questionId"),x.getString("answerCode"),x.getString("safetySchemaVersion"),if(x.isNull("detailJson"))null else x.getJSONObject("detailJson").toString()).let {if(it.questionId=="S09")S09Codec.canonicalize(it) else it}})
   },o.getJSONArray("weeklySummaries").objects().map {v->WeeklySummarySnapshot(v.getString("id"),v.getInt("periodIndex"),v.getString("periodStartDate"),v.getString("periodEndDate"),v.getInt("validEntryCount"),v.doubleOrNull("avgSolMin"),v.doubleOrNull("avgAwakeningCount"),v.doubleOrNull("avgWasoMin"),v.doubleOrNull("avgTerminalWakefulnessMin"),v.doubleOrNull("avgTibMin"),v.doubleOrNull("avgSleepQuality"),v.getJSONArray("sourceEntryIds").toString(),v.getString("diarySchemaVersion"),v.getString("weeklySummaryRuleVersion"),v.getString("generatedAtUtc"),v.getInt("revision"),v.getBoolean("isCurrent"),v.textOrNull("supersedesSummaryId"))},
   AppState(trackingStartDate=s.textOrNull("trackingStartDate"),hasSeenMinimalInteractionExplanation=s.getBoolean("hasSeenMinimalInteractionExplanation"),hasSeenSharePrivacyNotice=s.getBoolean("hasSeenSharePrivacyNotice"),lastDisplayedWeeklyPeriodIndex=s.getInt("lastDisplayedWeeklyPeriodIndex"),currentSafetyAssessmentId=s.textOrNull("currentSafetyAssessmentId"),hasCompletedOnboarding=if(o.getString("exportFormatVersion")==Versions.LEGACY_BACKUP_FORMAT)false else s.getBoolean("hasCompletedOnboarding")))
  val complete=data.copy(workspaceRecords=records)
  validate(complete);return complete
 }
 fun validate(d:BackupData) {
  require(d.workspaceRecords.map{it.id}.distinct().size==d.workspaceRecords.size)
  d.workspaceRecords.forEach(WorkspaceValidation::record)
  require(d.entries.map{it.id}.distinct().size==d.entries.size && d.entries.map{it.sleepDate}.distinct().size==d.entries.size)
  d.entries.forEach {e->val v=DiaryValidator.validate(e);require(v.canSave);require(!v.consistencyWarning||e.consistencyWarning);require(Instant.parse(e.createdAtUtc)<=Instant.parse(e.updatedAtUtc))}
  require(d.assessments.map{it.assessment.id}.distinct().size==d.assessments.size)
  val current=d.assessments.filter{it.assessment.isCurrent};require(current.size<=1)
  require(d.state.currentSafetyAssessmentId==current.singleOrNull()?.assessment?.id)
  d.assessments.forEach {p->require(SafetyGateCalculator.calculate(p.answers)==p.assessment.derivedGate)
   if(p.assessment.status==AssessmentStatus.COMPLETED)require(p.answers.map{it.questionId}.toSet().containsAll((1..15).map{"S"+it.toString().padStart(2,'0')}))
  }
  val start=d.state.trackingStartDate?.let(LocalDate::parse)
  require(d.entries.isEmpty()||start!=null);require(d.summaries.isEmpty()||start!=null)
  if(start!=null)require(d.entries.all{LocalDate.parse(it.sleepDate)>=start})
  require(d.summaries.map{it.id}.distinct().size==d.summaries.size)
  d.summaries.groupBy{it.periodIndex}.forEach {(_,items)->
   val chain=items.sortedBy{it.revision};require(chain.map{it.revision}==(1..chain.size).toList());require(chain.count{it.isCurrent}==1 && chain.last().isCurrent)
   chain.forEachIndexed {i,v->require(v.supersedesSummaryId==chain.getOrNull(i-1)?.id)}
  }
  d.summaries.forEach {v->
   val period=PeriodCalculator.at(checkNotNull(start),v.periodIndex);require(v.periodStartDate==period.start.toString()&&v.periodEndDate==period.end.toString())
   val ids=JSONArray(v.sourceEntryIdsJson).strings();require(ids.size==v.validEntryCount&&ids.distinct().size==ids.size&&ids.all{id->d.entries.any{it.id==id}})
   val means=listOf(v.avgSolMin,v.avgAwakeningCount,v.avgWasoMin,v.avgTerminalWakefulnessMin,v.avgTibMin,v.avgSleepQuality)
   require(if(v.validEntryCount==0)means.all{it==null}else means.all{it!=null})
   // Old revisions describe previous values/dates; never recalculate or overwrite them during restore.
  }
  require(d.state.lastDisplayedWeeklyPeriodIndex==0||d.summaries.any{it.periodIndex==d.state.lastDisplayedWeeklyPeriodIndex})
 }
}
