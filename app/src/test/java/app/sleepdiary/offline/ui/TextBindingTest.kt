package app.sleepdiary.offline.ui

import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import androidx.test.core.app.ApplicationProvider
import app.sleepdiary.offline.ui.diary.ModelBoundEditText
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34])
class TextBindingTest {
 @Test fun repeatedStaleModelCannotEraseImeComposition() {
  val editor=ModelBoundEditText(ApplicationProvider.getApplicationContext())
  val changes=mutableListOf<String>()
  editor.addTextChangedListener(object:TextWatcher {
   override fun beforeTextChanged(s:CharSequence?,start:Int,count:Int,after:Int){}
   override fun afterTextChanged(s:Editable?){}
   override fun onTextChanged(s:CharSequence?,start:Int,before:Int,count:Int){if(!editor.syncingModel)changes+=s.toString()}
  })
  editor.syncValue("");editor.requestFocus()
  val ime=editor.onCreateInputConnection(EditorInfo())!!
  ime.setComposingText("中文",1);editor.syncValue("")
  assertEquals("中文",editor.text.toString());assertEquals(listOf("中文"),changes)
  editor.syncValue("中文");ime.finishComposingText();ime.commitText(" abc123",1);editor.syncValue("中文")
  assertEquals("中文 abc123",editor.text.toString())
 }
 @Test fun loadingAnotherRecordWorksWithoutResettingUnchangedSelection() {
  val editor=ModelBoundEditText(ApplicationProvider.getApplicationContext())
  editor.syncValue("已有备注");editor.setSelection(1);editor.syncValue("已有备注")
  assertEquals(1,editor.selectionStart)
  editor.syncValue("另一条记录");assertEquals("另一条记录",editor.text.toString())
 }
 @Test fun everySafetyExplanationIsSpecificIncludingFollowUp() {
  val explanations=SAFETY_QUESTIONS.map{it.help}+S07B_HELP
  assertEquals(16,explanations.distinct().size)
  assertTrue(explanations.all{it.length>25})
  assertTrue(SAFETY_QUESTIONS.single{it.id=="S09"}.help.contains("不诊断 OSA"))
 }
}
