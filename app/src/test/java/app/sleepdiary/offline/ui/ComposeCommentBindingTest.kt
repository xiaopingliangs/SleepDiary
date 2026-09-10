package app.sleepdiary.offline.ui
import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import app.sleepdiary.offline.ui.diary.CommentTextInput
import app.sleepdiary.offline.ui.theme.SleepDiaryTheme
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34])
class ComposeCommentBindingTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 @Test fun repeatedUpdatesKeepChineseEnglishAndNumbers() {
  var value by mutableStateOf("");var redraw by mutableStateOf(false)
  compose.setContent{SleepDiaryTheme{CommentTextInput(value,{value=it},if(redraw)"备注" else "特殊情况","input")}}
  compose.onNodeWithTag("input").assertTextContains("")
  for(text in listOf("中文"," English"," 123")) {
   compose.onNodeWithTag("input").performTextInput(text)
   compose.runOnIdle{redraw=!redraw}
  }
  compose.runOnIdle{assertEquals("中文 English 123",value)}
  compose.onNodeWithTag("input").assertTextContains("中文 English 123")
 }
 @Test fun loadingAnotherRecordAndDeletingTextUpdatesModel() {
  var value by mutableStateOf("已有备注")
  compose.setContent{SleepDiaryTheme{CommentTextInput(value,{value=it},"备注","input")}}
  compose.onNodeWithTag("input").assertTextContains("已有备注")
  compose.runOnIdle{value="另一条记录"}
  compose.onNodeWithTag("input").assertTextContains("另一条记录")
  compose.onNodeWithTag("input").performTextClearance()
  compose.runOnIdle{assertEquals("",value)}
 }
}
