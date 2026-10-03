package app.sleepdiary.offline.data.backup

import app.sleepdiary.offline.data.entity.*
import app.sleepdiary.offline.data.dao.AssessmentWithAnswers
import app.sleepdiary.offline.domain.model.*
import app.sleepdiary.offline.domain.safety.*
import app.sleepdiary.offline.domain.validation.DiaryValidator
import app.sleepdiary.offline.domain.weekly.PeriodCalculator
import app.sleepdiary.offline.data.repository.WorkspaceValidation
import app.sleepdiary.offline.domain.education.EducationContentIndex
import app.sleepdiary.offline.domain.education.EducationVersionDescriptor
import org.json.*
import java.time.*

internal fun JSONObject.textOrNull(k:String):String?=if(isNull(k))null else getString(k)
internal fun JSONObject.doubleOrNull(k:String):Double?=if(isNull(k))null else getDouble(k)
internal fun JSONArray.objects()=(0 until length()).map{getJSONObject(it)}
internal fun JSONArray.strings()=(0 until length()).map{getString(it)}
internal fun obj(vararg pairs:Pair<String,Any?>)=JSONObject().apply{pairs.forEach{put(it.first,it.second?:JSONObject.NULL)}}

data class BackupData(val entries:List<SleepDiaryEntry>,val assessments:List<AssessmentWithAnswers>,val summaries:List<WeeklySummarySnapshot>,val state:AppState,val workspaceRecords:List<WorkspaceRecord> = emptyList(),val inputSnapshots:List<InputSnapshot> = emptyList(),val treatmentEpisodes:List<TreatmentEpisode> = emptyList(),val treatmentAuditEvents:List<TreatmentAuditEvent> = emptyList(),val educationContentVersions:List<EducationVersionDescriptor> = emptyList(),val educationProgress:List<EducationProgress> = emptyList(),val educationCommittedChoices:List<EducationCommittedChoice> = emptyList(),val educationReviewStates:List<EducationReviewState> = emptyList(),val educationRecords:List<EducationRecord> = emptyList(),val educationPositions:List<EducationPosition> = emptyList(),val sourceBackupFormatVersion:String=Versions.BACKUP_FORMAT)

object BackupCodec {
 fun treatmentEpisodeJson(e:TreatmentEpisode)=obj("id" to e.id,"mode" to e.mode,"state" to e.state,"pausedFromState" to e.pausedFromState,"adultScopeConfirmed" to e.adultScopeConfirmed,"protocolVersion" to e.protocolVersion,"metricsVersion" to e.metricsVersion,"treatmentSafetyRuleVersion" to e.treatmentSafetyRuleVersion,"exceptionRuleVersion" to e.exceptionRuleVersion,"currentPlanId" to e.currentPlanId,"startedAtUtc" to e.startedAtUtc,"createdAtUtc" to e.createdAtUtc,"updatedAtUtc" to e.updatedAtUtc,"completedAtUtc" to e.completedAtUtc,"revision" to e.revision,"treatmentDataSchemaVersion" to e.treatmentDataSchemaVersion)
 fun treatmentEpisode(o:JSONObject)=TreatmentEpisode(o.getString("id"),o.getString("mode"),o.getString("state"),o.textOrNull("pausedFromState"),o.getBoolean("adultScopeConfirmed"),o.getString("protocolVersion"),o.getString("metricsVersion"),o.getString("treatmentSafetyRuleVersion"),o.getString("exceptionRuleVersion"),o.textOrNull("currentPlanId"),o.getString("startedAtUtc"),o.getString("createdAtUtc"),o.getString("updatedAtUtc"),o.textOrNull("completedAtUtc"),o.getInt("revision"),o.getString("treatmentDataSchemaVersion"))
 fun treatmentAuditJson(e:TreatmentAuditEvent)=obj("id" to e.id,"episodeId" to e.episodeId,"commandType" to e.commandType,"requestFingerprint" to e.requestFingerprint,"fromState" to e.fromState,"toState" to e.toState,"fromRevision" to e.fromRevision,"toRevision" to e.toRevision,"occurredAtUtc" to e.occurredAtUtc,"restoreGenerationAtCommand" to e.restoreGenerationAtCommand,"treatmentDataSchemaVersion" to e.treatmentDataSchemaVersion)
 fun treatmentAudit(o:JSONObject)=TreatmentAuditEvent(o.getString("id"),o.getString("episodeId"),o.getString("commandType"),o.getString("requestFingerprint"),o.textOrNull("fromState"),o.getString("toState"),o.getInt("fromRevision"),o.getInt("toRevision"),o.getString("occurredAtUtc"),o.getString("restoreGenerationAtCommand"),o.getString("treatmentDataSchemaVersion"))
 fun inputSnapshotJson(s:InputSnapshot)=obj("id" to s.id,"capturedAtUtc" to s.capturedAtUtc,"fromDate" to s.fromDate,"throughDate" to s.throughDate,"captureVersion" to s.captureVersion,"payloadBase64" to s.payloadBase64,"contentHash" to s.contentHash)
 fun inputSnapshot(o:JSONObject)=InputSnapshot(o.getString("id"),o.getString("capturedAtUtc"),o.getString("fromDate"),o.getString("throughDate"),o.getString("captureVersion"),o.getString("payloadBase64"),o.getString("contentHash"))

 fun workspaceJson(r:WorkspaceRecord)=obj("id" to r.id,"kind" to r.kind,"title" to r.title,"body" to r.body,
  "createdAtUtc" to r.createdAtUtc,"updatedAtUtc" to r.updatedAtUtc,"revision" to r.revision,"workspaceSchemaVersion" to r.workspaceSchemaVersion)
 fun workspace(o:JSONObject)=WorkspaceRecord(o.getString("id"),o.getString("kind"),o.getString("title"),o.getString("body"),
  o.getString("createdAtUtc"),o.getString("updatedAtUtc"),o.getInt("revision"),o.getString("workspaceSchemaVersion"))
 fun educationVersionJson(v:EducationVersionDescriptor)=obj("contentVersionId" to v.contentVersionId,"manifestHashSha256" to v.manifestHashSha256)
 fun educationVersion(o:JSONObject)=EducationVersionDescriptor(o.getString("contentVersionId"),o.getString("manifestHashSha256"))
 fun educationProgressJson(v:EducationProgress)=obj("contentVersionId" to v.contentVersionId,"contentId" to v.contentId,"state" to v.state,"updatedAtUtc" to v.updatedAtUtc,"completedAtUtc" to v.completedAtUtc,"educationDataSchemaVersion" to v.educationDataSchemaVersion)
 fun educationProgress(o:JSONObject)=EducationProgress(o.getString("contentVersionId"),o.getString("contentId"),o.getString("state"),o.getString("updatedAtUtc"),o.textOrNull("completedAtUtc"),o.getString("educationDataSchemaVersion"))
 fun educationChoiceJson(v:EducationCommittedChoice)=obj("contentVersionId" to v.contentVersionId,"promptContentId" to v.promptContentId,"choiceContentId" to v.choiceContentId,"optionId" to v.optionId,"committedAtUtc" to v.committedAtUtc,"updatedAtUtc" to v.updatedAtUtc,"educationDataSchemaVersion" to v.educationDataSchemaVersion)
 fun educationChoice(o:JSONObject)=EducationCommittedChoice(o.getString("contentVersionId"),o.getString("promptContentId"),o.getString("choiceContentId"),o.getString("optionId"),o.getString("committedAtUtc"),o.getString("updatedAtUtc"),o.getString("educationDataSchemaVersion"))
 fun educationReviewJson(v:EducationReviewState)=obj("contentVersionId" to v.contentVersionId,"contentId" to v.contentId,"reviewState" to v.reviewState,"createdAtUtc" to v.createdAtUtc,"updatedAtUtc" to v.updatedAtUtc,"reviewedAtUtc" to v.reviewedAtUtc,"educationDataSchemaVersion" to v.educationDataSchemaVersion)
 fun educationReview(o:JSONObject)=EducationReviewState(o.getString("contentVersionId"),o.getString("contentId"),o.getString("reviewState"),o.getString("createdAtUtc"),o.getString("updatedAtUtc"),o.textOrNull("reviewedAtUtc"),o.getString("educationDataSchemaVersion"))
 fun educationRecordJson(v:EducationRecord)=obj("workspaceRecordId" to v.workspaceRecordId,"contentVersionId" to v.contentVersionId,"contentId" to v.contentId,"promptId" to v.promptId,"recordKind" to v.recordKind,"createdAtUtc" to v.createdAtUtc,"educationDataSchemaVersion" to v.educationDataSchemaVersion)
 fun educationRecord(o:JSONObject)=EducationRecord(o.getString("workspaceRecordId"),o.getString("contentVersionId"),o.getString("contentId"),o.textOrNull("promptId"),o.getString("recordKind"),o.getString("createdAtUtc"),o.getString("educationDataSchemaVersion"))
 fun educationPositionJson(v:EducationPosition)=obj("contentVersionId" to v.contentVersionId,"courseId" to v.courseId,"sceneContentId" to v.sceneContentId,"updatedAtUtc" to v.updatedAtUtc,"educationDataSchemaVersion" to v.educationDataSchemaVersion)
 fun educationPosition(o:JSONObject)=EducationPosition(o.getString("contentVersionId"),o.getString("courseId"),o.getString("sceneContentId"),o.getString("updatedAtUtc"),o.getString("educationDataSchemaVersion"))
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
   "inputSnapshots" to JSONArray(data.inputSnapshots.sortedBy{it.id}.map(::inputSnapshotJson)),
   "workspaceSchemaVersion" to Versions.WORKSPACE_SCHEMA,"workspaceRecords" to JSONArray(data.workspaceRecords.sortedBy{it.id}.map(::workspaceJson)),
   "treatmentDataSchemaVersion" to Versions.TREATMENT_DATA_SCHEMA,
   "treatmentEpisodes" to JSONArray(data.treatmentEpisodes.sortedBy{it.id}.map(::treatmentEpisodeJson)),
   "treatmentAuditEvents" to JSONArray(data.treatmentAuditEvents.sortedWith(compareBy({it.episodeId},{it.toRevision})).map(::treatmentAuditJson)),
   "educationDataSchemaVersion" to Versions.EDUCATION_DATA_SCHEMA,
   "educationContentVersions" to JSONArray(data.educationContentVersions.sortedBy{it.contentVersionId}.map(::educationVersionJson)),
   "educationProgress" to JSONArray(data.educationProgress.sortedWith(compareBy({it.contentVersionId},{it.contentId})).map(::educationProgressJson)),
   "educationCommittedChoices" to JSONArray(data.educationCommittedChoices.sortedWith(compareBy({it.contentVersionId},{it.promptContentId})).map(::educationChoiceJson)),
   "educationReviewStates" to JSONArray(data.educationReviewStates.sortedWith(compareBy({it.contentVersionId},{it.contentId})).map(::educationReviewJson)),
   "educationRecords" to JSONArray(data.educationRecords.sortedBy{it.workspaceRecordId}.map(::educationRecordJson)),
   "educationPositions" to JSONArray(data.educationPositions.sortedWith(compareBy({it.contentVersionId},{it.courseId})).map(::educationPositionJson)))
 }
 fun decode(o:JSONObject,educationIndex:EducationContentIndex):BackupData {
  val format=o.getString("exportFormatVersion")
  require(format in setOf(Versions.BACKUP_FORMAT,Versions.LEGACY_BACKUP_3_0,Versions.LEGACY_BACKUP_2_1,Versions.LEGACY_BACKUP_2_0,Versions.LEGACY_BACKUP_1_1,Versions.LEGACY_BACKUP_FORMAT))
  val educationVersions=if(format==Versions.BACKUP_FORMAT){
   require(o.getString("educationDataSchemaVersion")==Versions.EDUCATION_DATA_SCHEMA)
   o.getJSONArray("educationContentVersions").objects().map(::educationVersion).also { values ->
    require(values.map{it.contentVersionId}.distinct().size==values.size)
    values.forEach(educationIndex::validateDescriptor)
   }
  } else {
   require(!o.has("educationDataSchemaVersion")&&!o.has("educationContentVersions")&&!o.has("educationProgress")&&!o.has("educationCommittedChoices")&&!o.has("educationReviewStates")&&!o.has("educationRecords")&&!o.has("educationPositions"))
   emptyList()
  }
  val records=if(format in setOf(Versions.BACKUP_FORMAT,Versions.LEGACY_BACKUP_3_0,Versions.LEGACY_BACKUP_2_1,Versions.LEGACY_BACKUP_2_0)){
   require(o.getString("workspaceSchemaVersion")==Versions.WORKSPACE_SCHEMA)
   o.getJSONArray("workspaceRecords").objects().map(::workspace)
  }else{require(!o.has("workspaceRecords")&&!o.has("workspaceSchemaVersion"));emptyList()}
  val s=o.getJSONObject("trackingState")
  val data=BackupData(o.getJSONArray("diaryEntries").objects().map(::entry),
   o.getJSONArray("safetyAssessments").objects().map {a->val id=a.getString("id")
    AssessmentWithAnswers(SafetyAssessment(id,a.getString("answeredAtUtc"),a.getString("safetySchemaVersion"),SafetyGate.valueOf(a.getString("derivedGate")),a.getString("safetyRuleVersion"),AssessmentStatus.valueOf(a.getString("status")),a.getBoolean("isCurrent")),a.getJSONArray("answers").objects().map {x->SafetyAnswer(id,x.getString("questionId"),x.getString("answerCode"),x.getString("safetySchemaVersion"),if(x.isNull("detailJson"))null else x.getJSONObject("detailJson").toString()).let {if(it.questionId=="S09")S09Codec.canonicalize(it) else it}})
   },o.getJSONArray("weeklySummaries").objects().map {v->WeeklySummarySnapshot(v.getString("id"),v.getInt("periodIndex"),v.getString("periodStartDate"),v.getString("periodEndDate"),v.getInt("validEntryCount"),v.doubleOrNull("avgSolMin"),v.doubleOrNull("avgAwakeningCount"),v.doubleOrNull("avgWasoMin"),v.doubleOrNull("avgTerminalWakefulnessMin"),v.doubleOrNull("avgTibMin"),v.doubleOrNull("avgSleepQuality"),v.getJSONArray("sourceEntryIds").toString(),v.getString("diarySchemaVersion"),v.getString("weeklySummaryRuleVersion"),v.getString("generatedAtUtc"),v.getInt("revision"),v.getBoolean("isCurrent"),v.textOrNull("supersedesSummaryId"))},
   AppState(trackingStartDate=s.textOrNull("trackingStartDate"),hasSeenMinimalInteractionExplanation=s.getBoolean("hasSeenMinimalInteractionExplanation"),hasSeenSharePrivacyNotice=s.getBoolean("hasSeenSharePrivacyNotice"),lastDisplayedWeeklyPeriodIndex=s.getInt("lastDisplayedWeeklyPeriodIndex"),currentSafetyAssessmentId=s.textOrNull("currentSafetyAssessmentId"),hasCompletedOnboarding=if(o.getString("exportFormatVersion")==Versions.LEGACY_BACKUP_FORMAT)false else s.getBoolean("hasCompletedOnboarding")))
  val snapshots=if(format in setOf(Versions.BACKUP_FORMAT,Versions.LEGACY_BACKUP_3_0,Versions.LEGACY_BACKUP_2_1))o.getJSONArray("inputSnapshots").objects().map(::inputSnapshot) else { require(!o.has("inputSnapshots"));emptyList() }
  val episodes:List<TreatmentEpisode>;val audits:List<TreatmentAuditEvent>
  if(format in setOf(Versions.BACKUP_FORMAT,Versions.LEGACY_BACKUP_3_0)){require(o.getString("treatmentDataSchemaVersion")==Versions.TREATMENT_DATA_SCHEMA);episodes=o.getJSONArray("treatmentEpisodes").objects().map(::treatmentEpisode);audits=o.getJSONArray("treatmentAuditEvents").objects().map(::treatmentAudit)}
  else{require(!o.has("treatmentDataSchemaVersion")&&!o.has("treatmentEpisodes")&&!o.has("treatmentAuditEvents"));episodes=emptyList();audits=emptyList()}
  val educationProgress=if(format==Versions.BACKUP_FORMAT)o.getJSONArray("educationProgress").objects().map(::educationProgress)else emptyList()
  val educationChoices=if(format==Versions.BACKUP_FORMAT)o.getJSONArray("educationCommittedChoices").objects().map(::educationChoice)else emptyList()
  val educationReviews=if(format==Versions.BACKUP_FORMAT)o.getJSONArray("educationReviewStates").objects().map(::educationReview)else emptyList()
  val educationRecords=if(format==Versions.BACKUP_FORMAT)o.getJSONArray("educationRecords").objects().map(::educationRecord)else emptyList()
  val educationPositions=if(format==Versions.BACKUP_FORMAT)o.getJSONArray("educationPositions").objects().map(::educationPosition)else emptyList()
  val complete=data.copy(workspaceRecords=records,inputSnapshots=snapshots,treatmentEpisodes=episodes,treatmentAuditEvents=audits,educationContentVersions=educationVersions,educationProgress=educationProgress,educationCommittedChoices=educationChoices,educationReviewStates=educationReviews,educationRecords=educationRecords,educationPositions=educationPositions,sourceBackupFormatVersion=format)
  validate(complete,educationIndex);return complete
 }
 fun validate(d:BackupData,educationIndex:EducationContentIndex) {
  require(d.treatmentEpisodes.map{it.id}.distinct().size==d.treatmentEpisodes.size)
  d.treatmentEpisodes.forEach(app.sleepdiary.offline.application.TreatmentRecordValidation::episode)
  require(d.treatmentEpisodes.count{it.completedAtUtc==null}<=1)
  require(d.treatmentAuditEvents.map{it.id}.distinct().size==d.treatmentAuditEvents.size)
  d.treatmentAuditEvents.forEach(app.sleepdiary.offline.application.TreatmentRecordValidation::audit)
  require(d.treatmentAuditEvents.all{a->d.treatmentEpisodes.any{it.id==a.episodeId}})
  d.treatmentEpisodes.forEach { episode ->
   val events=d.treatmentAuditEvents.filter{it.episodeId==episode.id}.sortedBy{it.toRevision}
   require(events.isNotEmpty() && events.first().fromRevision==0 && events.first().fromState==null)
   require(events.first().commandType=="START_BASELINE" && events.first().toState==app.sleepdiary.offline.domain.treatment.TreatmentState.BASELINE_COLLECTING.name)
   require(events.map{it.toRevision}==(1..events.size).toList())
   events.zipWithNext().forEach{(a,b)->require(a.toState==b.fromState && Instant.parse(a.occurredAtUtc)<=Instant.parse(b.occurredAtUtc))}
   events.drop(1).forEach { event ->
    val valid=when(event.commandType){
     "REQUEST_BASELINE_REVIEW"->event.fromState=="BASELINE_COLLECTING"&&event.toState=="BASELINE_REVIEW"
     "RETURN_TO_BASELINE_COLLECTION"->event.fromState=="BASELINE_REVIEW"&&event.toState=="BASELINE_COLLECTING"
     "PAUSE"->event.fromState in setOf("BASELINE_COLLECTING","BASELINE_REVIEW")&&event.toState=="USER_PAUSED"
     "RESUME"->event.fromState=="USER_PAUSED"&&event.toState in setOf("BASELINE_COLLECTING","BASELINE_REVIEW")
     else->false
    };require(valid)
   }
   require(events.last().toRevision==episode.revision && events.last().toState==episode.state)
  }
  require(d.inputSnapshots.map{it.id}.distinct().size==d.inputSnapshots.size)
  d.inputSnapshots.forEach(app.sleepdiary.offline.application.InputSnapshotValidation::validate)
  require(d.workspaceRecords.map{it.id}.distinct().size==d.workspaceRecords.size)
  d.workspaceRecords.forEach(WorkspaceValidation::record)
  require(d.entries.map{it.id}.distinct().size==d.entries.size && d.entries.map{it.sleepDate}.distinct().size==d.entries.size)
  d.entries.forEach {e->
   val v=DiaryValidator.validate(e)
   // Strict validation belongs to current create/edit. A backup must remain lossless
   // for parseable legacy chronology that an older release had already stored.
   // Do not normalize the source row during export or restore.
   val legacyChronologyErrors=setOf("sleepDate","timeOrder","timeDateRange","timeSpan")
   require(v.errors.all { it in legacyChronologyErrors }) { "日记字段格式或数值损坏" }
   LocalDate.parse(e.sleepDate)
   LocalDateTime.parse(e.bedEnteredAtLocal);LocalDateTime.parse(e.trySleepAtLocal)
   LocalDateTime.parse(e.finalAwakeningAtLocal);LocalDateTime.parse(e.outOfBedAtLocal)
   require(Instant.parse(e.createdAtUtc)<=Instant.parse(e.updatedAtUtc))
  }
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
  validateEducation(d, educationIndex)
 }

 private fun validateEducation(d:BackupData, educationIndex:EducationContentIndex) {
  val allVersions = d.educationProgress.map{it.contentVersionId} +
   d.educationCommittedChoices.map{it.contentVersionId} + d.educationReviewStates.map{it.contentVersionId} +
   d.educationRecords.map{it.contentVersionId} + d.educationPositions.map{it.contentVersionId}
  val expectedVersions = allVersions.toSet()
  require(d.educationContentVersions.map{it.contentVersionId}.distinct().size == d.educationContentVersions.size)
  require(d.educationContentVersions.map{it.contentVersionId}.toSet() == expectedVersions)
  d.educationContentVersions.forEach(educationIndex::validateDescriptor)

  fun row(version:String,schema:String) { educationIndex.descriptor(version); require(schema==Versions.EDUCATION_DATA_SCHEMA) }
  fun timestamp(value:String)=Instant.parse(value)
  require(d.educationProgress.map{it.contentVersionId to it.contentId}.distinct().size==d.educationProgress.size)
  d.educationProgress.forEach { v ->
   row(v.contentVersionId,v.educationDataSchemaVersion);educationIndex.node(v.contentVersionId,v.contentId)
   require(v.state in setOf("IN_PROGRESS","COMPLETED"))
   val updated=timestamp(v.updatedAtUtc);val completed=v.completedAtUtc?.let(::timestamp)
   require((v.state=="COMPLETED")== (completed!=null));if(completed!=null)require(completed<=updated)
  }
  require(d.educationCommittedChoices.map{it.contentVersionId to it.promptContentId}.distinct().size==d.educationCommittedChoices.size)
  d.educationCommittedChoices.forEach { v ->
   row(v.contentVersionId,v.educationDataSchemaVersion)
   educationIndex.validateChoice(v.contentVersionId,v.promptContentId,v.choiceContentId,v.optionId)
   require(timestamp(v.committedAtUtc)<=timestamp(v.updatedAtUtc))
  }
  require(d.educationReviewStates.map{it.contentVersionId to it.contentId}.distinct().size==d.educationReviewStates.size)
  d.educationReviewStates.forEach { v ->
   row(v.contentVersionId,v.educationDataSchemaVersion);educationIndex.node(v.contentVersionId,v.contentId)
   require(v.reviewState in setOf("SAVED_FOR_REVIEW","REVIEWED"))
   val created=timestamp(v.createdAtUtc);val updated=timestamp(v.updatedAtUtc);val reviewed=v.reviewedAtUtc?.let(::timestamp)
   require(created<=updated);require((v.reviewState=="REVIEWED")== (reviewed!=null));if(reviewed!=null)require(created<=reviewed&&reviewed<=updated)
  }
  require(d.educationRecords.map{it.workspaceRecordId}.distinct().size==d.educationRecords.size)
  val workspaceIds=d.workspaceRecords.map{it.id}.toSet()
  d.educationRecords.forEach { v ->
   row(v.contentVersionId,v.educationDataSchemaVersion);educationIndex.node(v.contentVersionId,v.contentId)
   require(v.workspaceRecordId in workspaceIds)
   val recordKind=v.recordKind
   require(recordKind in setOf("NOTE","REFLECTION","QUESTION_FOR_CLINICIAN","PERSONAL_REMINDER"))
   val workspace=d.workspaceRecords.single{it.id==v.workspaceRecordId}
   require(workspace.kind==when(recordKind){"NOTE"->"LEARNING_NOTE";"REFLECTION"->"REFLECTION";"QUESTION_FOR_CLINICIAN"->"CLINICIAN_NOTE";else->"CONTEXT_NOTE"})
   v.promptId?.let{prompt->require(educationIndex.node(v.contentVersionId,prompt).kind in setOf("NOTE_PROMPT","REFLECTION"))}
   timestamp(v.createdAtUtc)
  }
  require(d.educationPositions.map{it.contentVersionId to it.courseId}.distinct().size==d.educationPositions.size)
  d.educationPositions.forEach { v ->
   row(v.contentVersionId,v.educationDataSchemaVersion);educationIndex.validatePosition(v.contentVersionId,v.courseId,v.sceneContentId);timestamp(v.updatedAtUtc)
  }
 }
}
