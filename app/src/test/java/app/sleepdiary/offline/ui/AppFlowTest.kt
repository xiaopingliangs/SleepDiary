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
   val saved=AssessmentWithAnswers(SafetyAssessment(id,"2026-09-09T00:00:00Z",derivedGate=SafetyGateCalculator.calculate(answers),status=if(completed)AssessmentStatus.COMPLETED else AssessmentStatus.DRAFT,isCurrent=true),answers);safety=saved;return saved
  }
  override suspend fun save(entry:SleepDiaryEntry,confirmed:Boolean)=SaveResult.Success(entry,null)
  override suspend fun acknowledgeExplanation(){}
 }
 private lateinit var model:DiaryViewModel
 private fun start(store:Store){compose.runOnUiThread{model=DiaryViewModel(store,SavedStateHandle(),clockOn("2026-09-09"))};compose.setContent{SleepDiaryTheme{SleepDiaryApp(model,{})}};compose.waitUntil(5000){model.state.value.page!=HomePage.LOADING}}
 private fun intro(){compose.onNodeWithText("下一页").performClick();compose.onNodeWithText("下一页").performClick();compose.onNodeWithText("开始使用").performClick()}
 private fun me(){compose.onNodeWithTag("public_tab_me").performClick()}
 private fun startSafety(){me();compose.onNodeWithText("重新填写安全问询").performScrollTo().performClick()}

 @Test fun onboardingCompletesAndEntersPublicTodayWithoutSafetyOrDiary(){val store=Store();start(store);intro();compose.onNodeWithText("今天还没有睡眠记录",substring=true).assertExists();compose.runOnIdle{assertTrue(store.app.hasCompletedOnboarding);assertTrue(store.rows.isEmpty());assertNull(store.safety);model.start()};compose.waitForIdle();compose.onNodeWithText("今天还没有睡眠记录",substring=true).assertExists()}

 @Test fun existingDraftDoesNotAutoOpenButRemainsReachableFromMe(){val store=Store();store.app=AppState(hasCompletedOnboarding=true);val id=UUID.randomUUID().toString();store.safety=AssessmentWithAnswers(SafetyAssessment(id,"2026-09-09T00:00:00Z",derivedGate=SafetyGate.DELAY_AUTOMATED_TREATMENT,status=AssessmentStatus.DRAFT,isCurrent=true),listOf(SafetyAnswer(id,"S03","YES",detailJson=null)));start(store);compose.onNodeWithText("今天还没有睡眠记录",substring=true).assertExists();me();compose.onNodeWithText("继续未完成的安全问询").performScrollTo().performClick();compose.waitUntil(3000){SAFETY_QUESTIONS.any{compose.onAllNodesWithText(it.text,substring=true).fetchSemanticsNodes().isNotEmpty()}}}

 @Test fun acuteSafetyAnswerPersistsBeforeHelpAndCanReturnToPublicShell(){val store=Store();start(store);intro();startSafety();compose.onNodeWithText("是").performClick();compose.onNodeWithText("否").performClick();compose.onNodeWithText("不确定").performClick();compose.onNodeWithText(ACUTE_HELP).assertExists();compose.runOnIdle{assertEquals("UNSURE",store.safety!!.answers.last().answerCode);assertEquals(AssessmentStatus.DRAFT,store.safety!!.assessment.status)};compose.onNodeWithText("返回我的").performScrollTo().performClick();compose.onNodeWithTag("public_tab_today").assertExists()}

 @Test fun completeFifteenQuestionWorkflowRemainsAvailableFromMe(){val store=Store();start(store);intro();startSafety();for(n in 1..15){when(n){1->compose.onNodeWithText("是").performClick();8->compose.onNodeWithText("没有").performClick();9->{compose.onNodeWithText("以上都没有").performScrollTo().performClick();compose.onNodeWithText("确认").performScrollTo().performClick()};11->compose.onNodeWithText("安全").performClick();else->compose.onNodeWithText("否").performClick()}};compose.onNodeWithText("← 返回我的").assertExists();compose.runOnIdle{assertEquals(15,store.safety!!.answers.size);assertEquals(AssessmentStatus.COMPLETED,store.safety!!.assessment.status)}}

 @Test fun sleepHistorySelectsDateAndDoesNotForceEditing(){val store=Store();store.app=AppState(hasCompletedOnboarding=true);store.rows=listOf(diary("2026-09-09"));start(store);compose.onNodeWithTag("public_tab_sleep").performClick();compose.onNodeWithText("日记历史").performClick();compose.onNodeWithText("修改这条记录").performScrollTo().assertExists();compose.onNodeWithText("上一周").performScrollTo().performClick();compose.onNodeWithText(HISTORY_EMPTY_TEXT).assertExists()}

 @Test fun helpIsOnlyShownAfterUserOpensDiaryForm(){val store=Store();store.app=AppState(hasCompletedOnboarding=true);start(store);compose.onNodeWithText("记录今天的睡眠").performClick();compose.onAllNodesWithText(FIELD_HELP[0]).assertCountEquals(0);compose.onNodeWithContentDescription(DiaryField.BED.label+"帮助").performClick();compose.onNodeWithText(FIELD_HELP[0]).assertExists()}

 @Test fun conflictHighlightsOnlyRelatedFieldsAndClears(){val store=Store();store.app=AppState(hasCompletedOnboarding=true);start(store);compose.onNodeWithText("记录今天的睡眠").performClick();compose.runOnIdle{model.answerTime(DiaryField.BED,java.time.LocalDateTime.parse("2026-09-08T23:00"));model.answerTime(DiaryField.TRY,java.time.LocalDateTime.parse("2026-09-08T22:40"))};val error=androidx.compose.ui.semantics.SemanticsProperties.Error;compose.onNodeWithTag("field_BED").assert(SemanticsMatcher.keyIsDefined(error));compose.onNodeWithTag("field_TRY").assert(SemanticsMatcher.keyIsDefined(error));compose.onNodeWithTag("field_SOL").assert(SemanticsMatcher.keyNotDefined(error));compose.runOnIdle{model.answerTime(DiaryField.TRY,java.time.LocalDateTime.parse("2026-09-08T23:15"))};compose.onNodeWithTag("field_BED").assert(SemanticsMatcher.keyNotDefined(error))}

 @Test fun methodsAndVersionsHaveDistinctDestinations(){val store=Store();store.app=AppState(hasCompletedOnboarding=true);start(store);me();compose.onNodeWithText("版本信息").performScrollTo().performClick();compose.onNodeWithText("App: 0.2.2",substring=true).assertExists();compose.onAllNodesWithText("本版本如何计算").assertCountEquals(0);compose.onNodeWithText("← 返回").performClick();compose.onNodeWithText("方法与依据").performScrollTo().performClick();compose.onNodeWithText("本版本如何计算").assertExists();compose.onAllNodesWithText("App: 0.2.2",substring=true).assertCountEquals(0)}

 @Test fun safetyViewAndReentryHaveExplicitReturnToMe(){val store=Store().also{it.app=AppState(hasCompletedOnboarding=true)};start(store);me();compose.onNodeWithText("查看当前安全问询").performScrollTo().performClick();compose.onNodeWithText("← 返回我的").performClick();compose.onNodeWithText("重新填写安全问询").performScrollTo().performClick();compose.onNodeWithText("← 返回我的").performClick();compose.onNodeWithText("导出完整备份 JSON").assertExists()}

 @Test fun draftCanResumeAndPreservePersistedAnswer(){val store=Store().also{it.app=AppState(hasCompletedOnboarding=true)};start(store);startSafety();compose.onNodeWithText("是").performClick();compose.runOnIdle{assertEquals("S01",store.safety!!.answers.single().questionId)};compose.onNodeWithText("← 返回我的").performClick();compose.onNodeWithText("继续未完成的安全问询").performScrollTo().performClick();compose.onNodeWithText(SAFETY_QUESTIONS[1].text).assertExists();compose.runOnUiThread{compose.activity.onBackPressedDispatcher.onBackPressed()};compose.onNodeWithText("导出完整备份 JSON").assertExists();compose.runOnIdle{assertEquals("YES",store.safety!!.answers.single().answerCode)}}

 @Test fun informationRoutesAndNestedTermsReturnToMe(){val store=Store().also{it.app=AppState(hasCompletedOnboarding=true)};start(store);me();for(label in listOf("隐私说明","方法与依据","术语表","版本信息")){compose.onNodeWithText(label).performScrollTo().performClick();compose.onNodeWithText("← 返回").performScrollTo().performClick();compose.onNodeWithText("导出完整备份 JSON").assertExists()};compose.onNodeWithText("方法与依据").performScrollTo().performClick();compose.onNodeWithText("术语表").performClick();compose.onNodeWithText("← 返回").performClick();compose.onNodeWithText("本版本如何计算").assertExists()}

 @Test fun exportConfirmationCanBeCancelledWithoutLosingMeDestination(){val store=Store().also{it.app=AppState(hasCompletedOnboarding=true)};start(store);me();for(label in listOf("导出完整备份 JSON","导出睡眠日记 CSV")){compose.onNodeWithText(label).performScrollTo().performClick();compose.onNodeWithText("取消").performClick();compose.onNodeWithText("导入完整备份").assertExists()}}

 @Test fun historyEditShowsExistingCommentAndRetainsEditedFormStateWhenCancelled(){val store=Store().also{it.app=AppState(hasCompletedOnboarding=true);it.rows=listOf(diary("2026-09-09").copy(comment="已有备注"))};start(store);compose.onNodeWithTag("public_tab_sleep").performClick();compose.onNodeWithText("日记历史").performClick();compose.onNodeWithText("修改这条记录").performScrollTo().performClick();compose.onNodeWithText("＋ 昨晚有特殊情况（可选）").performScrollTo().performClick();compose.onNodeWithTag("comment").assertTextContains("已有备注");compose.onNodeWithTag("comment").performTextReplacement("修改后的备注");compose.runOnIdle{assertEquals("修改后的备注",model.state.value.form.comment)};compose.onNodeWithText("← 返回").performClick();compose.onNodeWithText("修改这条记录").assertExists()}
}
