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
 private fun me(){compose.onNodeWithTag("public_tab_me").performClick()}
 private fun startSafety(){me();compose.onNodeWithText("重新填写安全问询").performScrollTo().performClick()}

 @Test fun freshInstallStartsWithoutBlockingIntroAndOffersTheGuideFromMe(){val store=Store();start(store);assertEquals(HomePage.TODAY,model.state.value.page);compose.onNodeWithText("记录昨晚睡眠").assertExists();compose.onNodeWithText("按记忆大概填写即可，不需要为了几分钟的精确度去查手表或反复校准。").assertExists();compose.onAllNodesWithText("下一页").assertCountEquals(0);assertFalse(store.app.hasCompletedOnboarding);assertTrue(store.rows.isEmpty());assertNull(store.safety);me();compose.onNodeWithText("使用说明").performScrollTo().performClick();compose.onNodeWithText("睡眠日记").assertExists();compose.onNodeWithText("下一页").performClick();compose.onNodeWithText("下一页").performClick();compose.onNodeWithText("完成查看").performClick();compose.waitUntil(5000){store.app.hasCompletedOnboarding};compose.onNodeWithText("完整备份").assertExists()}

 @Test fun existingDraftRemainsStoredButIsNotExposedInPublicUI(){val store=Store();store.app=AppState(hasCompletedOnboarding=true);val id=UUID.randomUUID().toString();store.safety=AssessmentWithAnswers(SafetyAssessment(id,"2026-09-09T00:00:00Z",derivedGate=SafetyGate.DELAY_AUTOMATED_TREATMENT,status=AssessmentStatus.DRAFT,isCurrent=true),listOf(SafetyAnswer(id,"S03","YES",detailJson=null)));start(store);compose.onNodeWithText("记录昨晚睡眠").assertExists();me();compose.onAllNodesWithText("继续未完成的安全问询").assertCountEquals(0);compose.onAllNodesWithText("重新填写安全问询").assertCountEquals(0);compose.onAllNodesWithText("安全问询 ·",substring=true).assertCountEquals(0);assertEquals("YES",store.safety!!.answers.single().answerCode)}

 @Test fun firstLaunchAndNavigationNeverStartSafetyAssessment(){val store=Store();start(store);assertEquals(HomePage.TODAY,model.state.value.page);compose.onNodeWithTag("public_tab_me").performClick();compose.onAllNodesWithText("重新填写安全问询").assertCountEquals(0);compose.onAllNodesWithText("查看当前安全问询").assertCountEquals(0);compose.onAllNodesWithText("继续未完成的安全问询").assertCountEquals(0);assertNull(store.safety)}

 @Test fun publicNavigationCannotCreateSafetyAssessment(){val store=Store();store.app=AppState(hasCompletedOnboarding=true);start(store);for(tag in listOf("public_tab_today","public_tab_sleep","public_tab_education","public_tab_me")){compose.onNodeWithTag(tag).performClick();compose.onAllNodesWithText("重新填写安全问卷").assertCountEquals(0);compose.onAllNodesWithText("重新填写安全问询").assertCountEquals(0)};assertNull(store.safety)}

 @Test fun sleepHistorySelectsDateAndDoesNotForceEditingOrOfferExports(){val store=Store();store.app=AppState(hasCompletedOnboarding=true);store.rows=listOf(diary("2026-09-09"));start(store);compose.onNodeWithTag("public_tab_sleep").performClick();compose.onNodeWithText("日记历史").performClick();compose.onNodeWithText("查看详情").performScrollTo().assertExists();compose.onNodeWithText("更正明显错误").assertDoesNotExist();compose.onAllNodesWithText("导出睡眠日记 CSV").assertCountEquals(0);compose.onNodeWithText("上一周").performScrollTo().performClick();compose.onNodeWithText(HISTORY_EMPTY_TEXT).assertExists()}
 @Test fun todayIsDailyContextOnlyAndMeUsesReadableExportNames(){val store=Store();store.app=AppState(hasCompletedOnboarding=true);start(store);compose.onNodeWithText("记录昨晚睡眠").assertExists();compose.onAllNodesWithText("阅读睡眠与 CBT-I 知识").assertCountEquals(0);compose.onAllNodesWithText("查看睡眠历史与总结").assertCountEquals(0);me();compose.onNodeWithText("完整备份").assertExists();compose.onNodeWithText("用于换手机或恢复全部数据").assertExists();compose.onNodeWithText("睡眠记录表").assertExists();compose.onNodeWithText("适合用 Excel / WPS 查看 · CSV").assertExists();compose.onNodeWithText("可阅读的学习记录").assertExists();compose.onNodeWithText("学习数据导出").assertExists()}

 @Test fun helpIsOnlyShownAfterUserOpensDiaryForm(){val store=Store();store.app=AppState(hasCompletedOnboarding=true);start(store);compose.onNodeWithText("记录昨晚睡眠").performClick();compose.onAllNodesWithText(FIELD_HELP[0]).assertCountEquals(0);compose.onNodeWithContentDescription(DiaryField.BED.label+"帮助").performClick();compose.onNodeWithText(FIELD_HELP[0]).assertExists()}

 @Test fun conflictHighlightsOnlyRelatedFieldsAndClears(){val store=Store();store.app=AppState(hasCompletedOnboarding=true);start(store);compose.onNodeWithText("记录昨晚睡眠").performClick();compose.runOnIdle{model.answerTime(DiaryField.BED,java.time.LocalDateTime.parse("2026-09-08T23:00"));model.answerTime(DiaryField.TRY,java.time.LocalDateTime.parse("2026-09-08T22:40"))};val error=androidx.compose.ui.semantics.SemanticsProperties.Error;compose.onNodeWithTag("field_BED").assert(SemanticsMatcher.keyIsDefined(error));compose.onNodeWithTag("field_TRY").assert(SemanticsMatcher.keyIsDefined(error));compose.onNodeWithTag("field_SOL").assert(SemanticsMatcher.keyNotDefined(error));compose.runOnIdle{model.answerTime(DiaryField.TRY,java.time.LocalDateTime.parse("2026-09-08T23:15"))};compose.onNodeWithTag("field_BED").assert(SemanticsMatcher.keyNotDefined(error))}

 @Test fun methodsAndVersionsHaveDistinctDestinations(){val store=Store();store.app=AppState(hasCompletedOnboarding=true);start(store);me();compose.onNodeWithText("版本信息").performScrollTo().performClick();compose.onNodeWithText("App: 0.2.2",substring=true).assertExists();compose.onAllNodesWithText("本版本如何计算").assertCountEquals(0);compose.onNodeWithText("← 返回").performClick();compose.onNodeWithText("方法与依据").performScrollTo().performClick();compose.onNodeWithText("本版本如何计算").assertExists();compose.onAllNodesWithText("App: 0.2.2",substring=true).assertCountEquals(0)}

 @Test fun persistedSafetyHistoryIsNotAnActionablePublicRoute(){val store=Store().also{it.app=AppState(hasCompletedOnboarding=true)};val id=UUID.randomUUID().toString();store.safety=AssessmentWithAnswers(SafetyAssessment(id,"2026-09-09T00:00:00Z",derivedGate=SafetyGate.NO_MAJOR_FLAG,status=AssessmentStatus.COMPLETED,isCurrent=true),emptyList());start(store);me();compose.onAllNodesWithText("查看当前安全问询").assertCountEquals(0);compose.onAllNodesWithText("重新填写安全问询").assertCountEquals(0);compose.onNodeWithText("完整备份").assertExists();assertEquals(id,store.safety!!.assessment.id)}

 @Test fun legacySafetyDraftIsPreservedButCannotBeResumedFromPublicUi(){val store=Store().also{it.app=AppState(hasCompletedOnboarding=true)};val id=UUID.randomUUID().toString();store.safety=AssessmentWithAnswers(SafetyAssessment(id,"2026-09-09T00:00:00Z",derivedGate=SafetyGate.DELAY_AUTOMATED_TREATMENT,status=AssessmentStatus.DRAFT,isCurrent=true),listOf(SafetyAnswer(id,"S01","YES",detailJson=null)));start(store);me();compose.onAllNodesWithText("继续未完成的安全问询").assertCountEquals(0);assertEquals("YES",store.safety!!.answers.single().answerCode);assertEquals(id,store.safety!!.assessment.id)}

 @Test fun informationRoutesAndNestedTermsReturnToMe(){val store=Store().also{it.app=AppState(hasCompletedOnboarding=true)};start(store);me();for(label in listOf("隐私说明","方法与依据","术语表","版本信息")){compose.onNodeWithText(label).performScrollTo().performClick();compose.onNodeWithText("← 返回").performScrollTo().performClick();compose.onNodeWithText("完整备份").assertExists()};compose.onNodeWithText("方法与依据").performScrollTo().performClick();compose.onNodeWithText("术语表").performClick();compose.onNodeWithText("← 返回").performClick();compose.onNodeWithText("本版本如何计算").assertExists()}

 @Test fun exportConfirmationCanBeCancelledWithoutLosingMeDestination(){val store=Store().also{it.app=AppState(hasCompletedOnboarding=true)};start(store);me();for(label in listOf("完整备份","睡眠记录表")){compose.onNodeWithText(label).performScrollTo().performClick();compose.onNodeWithText("取消").performClick();compose.onNodeWithText("导入完整备份").assertExists()}}

 @Test fun todaySavedDiaryOpensReadOnlyAndExplicitCorrectionLoadsExistingComment(){val store=Store().also{it.app=AppState(hasCompletedOnboarding=true);it.rows=listOf(diary("2026-09-09").copy(comment="已有备注"))};start(store);compose.onNodeWithText("查看记录").performClick();compose.onNodeWithText("已有备注",substring=true).assertExists();compose.onNodeWithText("更正明显错误").performScrollTo().performClick();compose.onNodeWithText("只有明显填错或漏记重要情况时才需要更正。",substring=true).assertExists();compose.onNodeWithText("继续更正").performClick();compose.onNodeWithText("＋ 昨晚有特殊情况（可选）").performScrollTo().performClick();compose.onNodeWithTag("comment").assertTextContains("已有备注");compose.onNodeWithTag("comment").performTextReplacement("修改后的备注");compose.runOnIdle{assertEquals("修改后的备注",model.state.value.form.comment)};compose.onNodeWithText("← 返回").performClick()}
}
