package app.sleepdiary.offline.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.SavedStateHandle
import app.sleepdiary.offline.clockOn
import app.sleepdiary.offline.diary
import app.sleepdiary.offline.data.entity.*
import app.sleepdiary.offline.data.dao.AssessmentWithAnswers
import app.sleepdiary.offline.data.repository.*
import app.sleepdiary.offline.domain.model.*
import app.sleepdiary.offline.domain.safety.SafetyGateCalculator
import app.sleepdiary.offline.ui.diary.*
import app.sleepdiary.offline.ui.theme.SleepDiaryTheme
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.util.UUID

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],qualifiers="w411dp-h891dp-xxhdpi")
class AppFlowTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 private class Store:DiaryUiStore {
  var app=AppState();var safety:AssessmentWithAnswers?=null;var rows=emptyList<SleepDiaryEntry>()
  override suspend fun state()=app
  override suspend fun entries()=rows
  override suspend fun completeOnboarding(){app=app.copy(hasCompletedOnboarding=true)}
  override suspend fun currentSafety()=safety
  override suspend fun saveSafety(id:String,answers:List<SafetyAnswer>,completed:Boolean):AssessmentWithAnswers {
   val p=AssessmentWithAnswers(SafetyAssessment(id,"2026-09-09T00:00:00Z",derivedGate=SafetyGateCalculator.calculate(answers),status=if(completed)AssessmentStatus.COMPLETED else AssessmentStatus.DRAFT,isCurrent=true),answers);safety=p;return p
  }
  override suspend fun save(entry:SleepDiaryEntry,confirmed:Boolean)=SaveResult.Success(entry,null)
  override suspend fun acknowledgeExplanation(){}
 }
 private lateinit var model:DiaryViewModel
 private fun start(s:Store){compose.runOnUiThread{model=DiaryViewModel(s,SavedStateHandle(),clockOn("2026-09-09"))};compose.setContent{SleepDiaryTheme{SleepDiaryApp(model,{})}};compose.waitUntil(5000){model.state.value.page!=HomePage.LOADING}}
 private fun intro(){compose.onNodeWithText("下一页").performClick();compose.onNodeWithText("下一页").performClick();compose.onNodeWithText("开始").performClick()}
 @Test fun onboardingCompletesBeforeAnyDiaryOrSafetyAnswer(){val s=Store();start(s);intro();compose.onNodeWithText(SAFETY_QUESTIONS.first().text).assertExists();compose.runOnIdle{assertTrue(s.app.hasCompletedOnboarding);assertTrue(s.rows.isEmpty());assertNull(s.safety);model.start()};compose.waitForIdle();compose.onNodeWithText(SAFETY_QUESTIONS.first().text).assertExists()}
 @Test fun completedOnboardingWithDraftDoesNotRepeat(){val s=Store();s.app=AppState(hasCompletedOnboarding=true);val id=UUID.randomUUID().toString();s.safety=AssessmentWithAnswers(SafetyAssessment(id,"2026-09-09T00:00:00Z",derivedGate=SafetyGate.DELAY_AUTOMATED_TREATMENT,status=AssessmentStatus.DRAFT,isCurrent=true),listOf(SafetyAnswer(id,"S03","YES",detailJson=null)));start(s);compose.onNodeWithTag("diary_screen").assertExists();compose.runOnIdle{model.start()};compose.waitForIdle();compose.onNodeWithTag("diary_screen").assertExists()}
 @Test fun acuteAnswerSavesBeforeHelpAndDiaryRemainsAvailable(){val s=Store();start(s);intro();compose.onNodeWithText("是").performClick();compose.onNodeWithText("否").performClick();compose.onNodeWithText("不确定").performClick();compose.onNodeWithText(ACUTE_HELP).assertExists();compose.runOnIdle{assertEquals("UNSURE",s.safety!!.answers.last().answerCode);assertEquals(AssessmentStatus.DRAFT,s.safety!!.assessment.status)};compose.onNodeWithText("继续记录睡眠").performScrollTo().performClick();compose.onNodeWithTag("diary_screen").assertExists()}
 @Test fun completeFifteenQuestionWorkflow(){val s=Store();start(s);intro();for(n in 1..15){when(n){1->compose.onNodeWithText("是").performClick();8->compose.onNodeWithText("没有").performClick();9->{compose.onNodeWithText("以上都没有").performScrollTo().performClick();compose.onNodeWithText("确认").performScrollTo().performClick()};11->compose.onNodeWithText("安全").performClick();else->compose.onNodeWithText("否").performClick()}};compose.onNodeWithText("安全问询已完成").assertExists();compose.runOnIdle{assertEquals(15,s.safety!!.answers.size);assertEquals(AssessmentStatus.COMPLETED,s.safety!!.assessment.status)}}
 @Test fun historySelectsDayWithoutEnteringEdit(){val s=Store();s.app=AppState(hasCompletedOnboarding=true);s.rows=listOf(diary(LocalDate.now().toString()));start(s);compose.runOnIdle{model.continueDiary()};compose.onNodeWithText("设置").performClick();compose.onNodeWithText("历史记录").performScrollTo().performClick();compose.onNodeWithText("修改这条记录").performScrollTo().assertExists();compose.onNodeWithText("上一周").performScrollTo().performClick();compose.onNodeWithText(HISTORY_EMPTY_TEXT).assertExists()}
 @Test fun helpIsHiddenUntilRequested(){val s=Store();s.app=AppState(hasCompletedOnboarding=true);start(s);compose.runOnIdle{model.continueDiary()};compose.onAllNodesWithText(FIELD_HELP[0]).assertCountEquals(0);compose.onNodeWithContentDescription(DiaryField.BED.label+"帮助").performClick();compose.onNodeWithText(FIELD_HELP[0]).assertExists()}
 @Test fun conflictHighlightsOnlyRelatedFieldsAndClears(){val s=Store();s.app=AppState(hasCompletedOnboarding=true);start(s);compose.runOnIdle{model.continueDiary();model.answerTime(DiaryField.BED,java.time.LocalDateTime.parse("2026-09-08T23:00"));model.answerTime(DiaryField.TRY,java.time.LocalDateTime.parse("2026-09-08T22:40"))};val error=androidx.compose.ui.semantics.SemanticsProperties.Error;compose.onNodeWithTag("field_BED").assert(SemanticsMatcher.keyIsDefined(error));compose.onNodeWithTag("field_TRY").assert(SemanticsMatcher.keyIsDefined(error));compose.onNodeWithTag("field_SOL").assert(SemanticsMatcher.keyNotDefined(error));compose.runOnIdle{model.answerTime(DiaryField.TRY,java.time.LocalDateTime.parse("2026-09-08T23:15"))};compose.onNodeWithTag("field_BED").assert(SemanticsMatcher.keyNotDefined(error))}

 @Test fun methodsAndVersionsHaveSeparateDestinations(){
  val s=Store();s.app=AppState(hasCompletedOnboarding=true);start(s)
  compose.runOnIdle{model.continueDiary()}
  compose.onNodeWithTag("diary_estimate_hint").assertTextContains("夜间特意查看时间",substring=true)
  compose.onNodeWithText("设置").performClick()
  compose.onNodeWithText("版本信息").performScrollTo().performClick()
  compose.onNodeWithText("App: 0.2.1",substring=true).assertExists()
  compose.onAllNodesWithText("本版本如何计算").assertCountEquals(0)
  compose.onNodeWithText("返回").performClick()
  compose.onNodeWithText("方法与依据").performScrollTo().performClick()
  compose.onNodeWithText("本版本如何计算").assertExists()
  compose.onAllNodesWithText("App: 0.2.1",substring=true).assertCountEquals(0)
 }

 private fun settingsStore():Store=Store().also{it.app=AppState(hasCompletedOnboarding=true)}
 private fun settings(){compose.runOnIdle{model.continueDiary()};compose.onNodeWithText("设置").performClick()}
 @Test fun settingsSafetyViewAndRestartCanReturn() {
  val s=settingsStore();start(s);settings()
  for(label in listOf("查看当前安全问询","重新完成安全问询")) {
   compose.onNodeWithText(label).performScrollTo().performClick()
   compose.onNodeWithText("← 返回设置").assertIsDisplayed().performClick()
   compose.onNodeWithText("导出完整备份 JSON").assertExists()
  }
 }
 @Test fun draftCanReturnResumeAndPreserveSubmittedAnswer() {
  val s=settingsStore();start(s);settings()
  compose.onNodeWithText("重新完成安全问询").performScrollTo().performClick()
  compose.onNodeWithText("是").performClick()
  compose.runOnIdle{assertEquals("S01",s.safety!!.answers.single().questionId)}
  compose.onNodeWithText("← 返回设置").performClick()
  compose.onNodeWithText("安全问询尚未完成 · 继续").performScrollTo().performClick()
  compose.onNodeWithText(SAFETY_QUESTIONS[1].text).assertExists()
  compose.runOnUiThread{compose.activity.onBackPressedDispatcher.onBackPressed()}
  compose.onNodeWithText("导出完整备份 JSON").assertExists()
  compose.runOnIdle{assertEquals("YES",s.safety!!.answers.single().answerCode)}
 }
 @Test fun onboardingSafetyHasNoSettingsBackButton() {
  start(Store());intro();compose.onAllNodesWithText("← 返回设置").assertCountEquals(0)
 }
 @Test fun everySettingsInformationRouteAndNestedTermsReturns() {
  start(settingsStore());settings()
  for(label in listOf("历史记录","隐私说明","方法与依据","术语表","版本信息")) {
   compose.onNodeWithText(label).performScrollTo().performClick()
   compose.onNodeWithText("返回").performScrollTo().performClick()
   compose.onNodeWithText("导出完整备份 JSON").assertExists()
  }
  compose.onNodeWithText("方法与依据").performScrollTo().performClick()
  compose.onNodeWithText("术语表").performClick()
  compose.onNodeWithText("返回").performClick()
  compose.onNodeWithText("本版本如何计算").assertExists()
 }
 @Test fun exportExplanationCanCancelBackToSettings() {
  start(settingsStore());settings()
  for(label in listOf("导出完整备份 JSON","导出睡眠日记 CSV")) {
   compose.onNodeWithText(label).performScrollTo().performClick()
   compose.onNodeWithText("取消").performClick()
   compose.onNodeWithText("导入完整备份").assertExists()
  }
 }
 @Test fun historyEditHasReturnToSelectedHistoryDate() {
  val s=settingsStore();s.rows=listOf(diary(LocalDate.now().toString()).copy(comment="已有备注"));start(s)
  compose.runOnIdle{model.continueDiary()}
  compose.onNodeWithText("设置").performClick()
  compose.onNodeWithText("历史记录").performScrollTo().performClick()
  compose.onNodeWithText("修改这条记录").performScrollTo().performClick()
  compose.onNodeWithText("＋ 昨晚有特殊情况（可选）").performScrollTo().performClick()
  compose.onNodeWithTag("comment").assertTextContains("已有备注")
  compose.onNodeWithTag("comment").performTextReplacement("修改后的备注")
  compose.runOnIdle{assertEquals("修改后的备注",model.state.value.form.comment)}
  compose.onNodeWithText("← 返回历史记录").performClick()
  compose.onNodeWithText("已有备注").assertExists()
  compose.onNodeWithText("修改这条记录").assertExists()
 }

}
