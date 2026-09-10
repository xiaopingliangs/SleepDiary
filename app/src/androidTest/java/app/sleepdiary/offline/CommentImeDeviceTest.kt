package app.sleepdiary.offline.ui

import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.SavedStateHandle
import app.sleepdiary.offline.ui.diary.*
import app.sleepdiary.offline.ui.theme.SleepDiaryTheme
import app.sleepdiary.offline.data.entity.*
import app.sleepdiary.offline.data.repository.*
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class CommentImeDeviceTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 @Test fun realImeCompositionSurvivesViewModelRecompositionAndReopen() {
  val store=object:DiaryUiStore {
   override suspend fun state()=AppState(hasCompletedOnboarding=true)
   override suspend fun entries()=emptyList<SleepDiaryEntry>()
   override suspend fun save(entry:SleepDiaryEntry,confirmed:Boolean)=SaveResult.Success(entry,null)
   override suspend fun acknowledgeExplanation(){}
  }
  lateinit var model:DiaryViewModel
  compose.runOnUiThread {model=DiaryViewModel(store,SavedStateHandle(mapOf("editing" to true)),Clock.fixed(Instant.parse("2026-09-09T00:00:00Z"),ZoneId.of("Asia/Shanghai")))}
  compose.setContent {val s by model.state.collectAsState();SleepDiaryTheme{DiaryScreen(s,model::answerTime,model::answerNumber,model::comment,model::save,model::dismissWarning,{})}}
  compose.onNodeWithText("＋ 昨晚有特殊情况（可选）").performScrollTo().performClick()
  compose.onNodeWithTag("comment").performClick()
  lateinit var editor:View
  compose.runOnIdle {
   fun find(v:View):View? {if(v.onCheckIsTextEditor())return v;if(v is ViewGroup)for(i in 0 until v.childCount)find(v.getChildAt(i))?.let{return it};return null}
   editor=checkNotNull(find(compose.activity.window.decorView));editor.requestFocus()
   editor.onCreateInputConnection(EditorInfo())!!.setComposingText("中文",1)
  }
  compose.waitForIdle()
  compose.runOnIdle {assertEquals("中文",model.state.value.form.comment);val input=editor.onCreateInputConnection(EditorInfo())!!;input.finishComposingText();input.commitText(" abc123",1)}
  compose.waitForIdle()
  compose.runOnIdle {assertEquals("中文 abc123",model.state.value.form.comment);editor.clearFocus()}
  compose.onNodeWithText("＋ 昨晚有特殊情况（可选）").performScrollTo().performClick()
  compose.onNodeWithText("＋ 昨晚有特殊情况（可选）").performScrollTo().performClick()
  compose.onNodeWithTag("comment").assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.EditableText,androidx.compose.ui.text.AnnotatedString("中文 abc123")))
 }
}
