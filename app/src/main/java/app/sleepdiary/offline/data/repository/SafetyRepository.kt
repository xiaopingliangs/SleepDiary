package app.sleepdiary.offline.data.repository

import androidx.room.withTransaction
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.dao.AssessmentWithAnswers
import app.sleepdiary.offline.data.entity.*
import app.sleepdiary.offline.domain.model.*
import app.sleepdiary.offline.domain.safety.*
import java.time.Instant

class SafetyRepository(private val db:SleepDiaryDatabase) {
 suspend fun save(id:String,answers:List<SafetyAnswer>,completed:Boolean):AssessmentWithAnswers=db.withTransaction {
  require(answers.all{it.assessmentId==id})
  val canonical=answers.map{if(it.questionId=="S09")S09Codec.canonicalize(it)else it}
  val gate=SafetyGateCalculator.calculate(canonical)
  if(completed)require(canonical.map{it.questionId}.containsAll((1..15).map{"S"+it.toString().padStart(2,'0')}))
  val dao=db.safetyDao();val old=dao.byId(id)
  // Continuing a draft cannot silently remove its already persisted acute signal.
  old?.answers?.firstOrNull{it.questionId=="S03"&&it.answerCode!="NO"}?.let {signal->require(canonical.any{it==signal})}
  val assessment=SafetyAssessment(id,Instant.now().toString(),derivedGate=gate,status=if(completed)AssessmentStatus.COMPLETED else AssessmentStatus.DRAFT,isCurrent=true)
  dao.retireCurrent()
  if(old==null)dao.insertAssessment(assessment)else {dao.updateAssessment(assessment);dao.deleteAnswers(id)}
  dao.insertAnswers(canonical)
  val states=db.stateAndSummaryDao();val state=(states.state()?:AppState()).copy(currentSafetyAssessmentId=id);states.putState(state)
  val stored=checkNotNull(dao.byId(id));check(stored.assessment==assessment&&stored.answers.toSet()==canonical.toSet()&&states.state()==state);stored
 }
}
