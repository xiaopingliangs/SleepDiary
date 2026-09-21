package app.sleepdiary.offline.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.activity.ComponentActivity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import app.sleepdiary.offline.ui.diary.*
import app.sleepdiary.offline.ui.theme.SleepDiaryTheme
import app.sleepdiary.offline.diary
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.*
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],qualifiers="w411dp-h891dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DiaryComposeTest {
    @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
    private fun form()=DiaryForm(referenceDate=LocalDate.parse("2026-09-09"),zoneId="Asia/Shanghai")
    private fun screen(dark:Boolean=false,fontScale:Float=1f,onNumber:(DiaryField,Int)->Unit={_,_->}) {
        compose.setContent {SleepDiaryTheme(dark) {
            val density=LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,fontScale)) {
                Surface(Modifier.fillMaxSize(),color=MaterialTheme.colorScheme.background) {DiaryScreen(DiaryUiState(form(),page=HomePage.FORM),{_,_->},onNumber,{}, {}, {}, {})}
            }
        }}
    }
    @Test fun openingAndCancellingAnchorLeavesAnswerUnfilled() {
        screen();compose.onNodeWithTag("time_BED").performClick()
        compose.onNodeWithTag("picker_candidate").assertTextContains("22:00",substring=true)
        compose.onNodeWithTag("picker_cancel").performClick()
        compose.onNodeWithTag("time_BED").assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription,"未填写"))
    }
    @Test fun explicitConfirmationAloneEmitsAnswer() {
        var answer:LocalDateTime?=null
        compose.setContent {SleepDiaryTheme {TimePickerDialog(DiaryField.BED,form().pickerPosition(DiaryField.BED),onDismiss={},onConfirm={answer=it})}}
        compose.runOnIdle {assertNull(answer)}
        compose.onNodeWithTag("picker_confirm").performClick()
        compose.runOnIdle {assertEquals(LocalDateTime.parse("2026-09-08T22:00"),answer)}
    }
    @Test fun reopenedPickerUsesExistingPreciseTime() {
        compose.setContent {SleepDiaryTheme {TimePickerDialog(DiaryField.BED,LocalDateTime.parse("2026-09-09T08:07"),onDismiss={},onConfirm={})}}
        compose.onNodeWithTag("picker_candidate").assertTextContains("08:07",substring=true)
    }
    @Test fun advancedDatePickerChangesDateWithoutTyping() {
        var answer:LocalDateTime?=null
        compose.setContent {SleepDiaryTheme {TimePickerDialog(DiaryField.BED,form().pickerPosition(DiaryField.BED),true,{}, {answer=it})}}
        compose.onNodeWithTag("picker_date").assertExists()
        compose.runOnIdle {
            fun find(view:android.view.View):android.widget.DatePicker? {
                if(view is android.widget.DatePicker)return view
                if(view is android.view.ViewGroup)for(i in 0 until view.childCount)find(view.getChildAt(i))?.let{return it}
                return null
            }
            val dialog=org.robolectric.shadows.ShadowDialog.getLatestDialog()
            checkNotNull(find(dialog.window!!.decorView)).updateDate(2026,8,10)
        }
        compose.onNodeWithTag("picker_confirm").performScrollTo().performClick()
        compose.runOnIdle {assertEquals(LocalDate.parse("2026-09-10"),answer!!.toLocalDate())}
    }
    @Test fun advancedPickerSupportsDaytimeFullDate() {
        var answer:LocalDateTime?=null
        compose.setContent {SleepDiaryTheme {TimePickerDialog(DiaryField.BED,LocalDateTime.parse("2026-09-09T22:00"),true,{}, {answer=it})}}
        compose.onNodeWithTag("picker_date").assertExists()
        compose.onNodeWithTag("hour_wheel").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.SetProgress){it(8f)}
        compose.onNodeWithTag("minute_wheel").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.SetProgress){it(7f)}
        compose.onNodeWithTag("picker_confirm").performClick()
        compose.runOnIdle {assertEquals(LocalDateTime.parse("2026-09-09T08:07"),answer)}
    }
    @Test fun solAndWasoChoicesHaveCorrectCallbacks() {
        val answers=mutableListOf<Pair<DiaryField,Int>>()
        screen(onNumber={f,n->answers+=f to n})
        compose.onNodeWithTag("diary_fields").performScrollToIndex(DiaryField.SOL.ordinal)
        compose.onNodeWithTag("input_SOL").performClick()
        compose.onNodeWithTag("choice_SOL_15").performClick()
        compose.onNodeWithTag("diary_fields").performScrollToIndex(DiaryField.WASO.ordinal)
        compose.onNodeWithTag("input_WASO").performClick()
        compose.onNodeWithTag("choice_WASO_0").performClick()
        compose.runOnIdle {assertEquals(listOf(DiaryField.SOL to 15,DiaryField.WASO to 0),answers)}
    }
    @Test fun moreRequiresExplicitInteger() {
        var answer:Int?=null
        compose.setContent {SleepDiaryTheme {NumberPickerDialog(DiaryField.COUNT,null,{}, {answer=it})}}
        compose.onNodeWithTag("number_confirm").assertIsNotEnabled()
        compose.onNodeWithTag("number_input").performTextReplacement("5+")
        compose.onNodeWithTag("number_confirm").assertIsNotEnabled()
        compose.onNodeWithTag("number_input").performTextReplacement("99")
        compose.onNodeWithTag("number_confirm").performClick()
        compose.runOnIdle {assertEquals(99,answer)}
    }
    @Test fun qualityIsNotPreselectedAndAllFiveChoicesExist() {
        screen();compose.onNodeWithTag("diary_fields").performScrollToIndex(DiaryField.QUALITY.ordinal)
        compose.onNodeWithTag("input_QUALITY").performClick()
        (1..5).forEach {compose.onNodeWithTag("choice_QUALITY_"+it).assertExists().assertIsNotSelected()}
    }
    @Test fun coreControlsHaveAccessibleLabelsAndMinimumTouchSize() {
        screen()
        compose.onNodeWithTag("time_BED").assertContentDescriptionEquals(DiaryField.BED.label).assertHeightIsAtLeast(48.dp)
        compose.onNodeWithTag("save_diary").assertHeightIsAtLeast(48.dp)
        compose.onNodeWithTag("diary_fields").performScrollToIndex(DiaryField.SOL.ordinal)
        compose.onNodeWithTag("input_SOL").performClick()
        compose.onNodeWithTag("choice_SOL_5").assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp)
        compose.onNodeWithTag("choice_SOL_5").assertContentDescriptionEquals(DiaryField.SOL.label+" 5分钟")
    }
    @Test fun noUnapprovedSleepFieldsExistAcrossForm() {
        screen()
        DiaryField.entries.forEach {field ->
            compose.onNodeWithTag("diary_fields").performScrollToIndex(field.ordinal)
            listOf("TST","SE","总睡眠时长","总睡眠时间","睡眠评分").forEach {compose.onAllNodesWithText(it,substring=true).assertCountEquals(0)}
        }
    }
    @Test fun todayScreenLetsUserChooseLiveSevenOrFourteenDayAverages() {
        val today=diary("2026-09-22")
        compose.setContent {SleepDiaryTheme {TodayScreen(DiaryUiState(form(),page=HomePage.TODAY,entries=listOf(today),today=today),{},{},{},{})}}
        compose.onNodeWithText("查看最近平均数据").performClick()
        compose.onNodeWithText("有记录的天数：1 / 7").assertIsDisplayed()
        compose.onNodeWithText("最近 14 天").performClick()
        compose.onNodeWithText("有记录的天数：1 / 14").assertIsDisplayed()
        compose.onAllNodesWithText("TST",substring=true).assertCountEquals(0)
        compose.onAllNodesWithText("SE",substring=true).assertCountEquals(0)
    }
    @Test fun lightModeRenders() {screen();compose.onNodeWithTag("save_diary").assertIsDisplayed();capture("light")}
    @Test fun darkModeRenders() {screen(dark=true);compose.onNodeWithTag("time_BED").assertIsDisplayed();capture("dark")}
    @Test fun enlargedFontRemainsScrollableAndOperable() {
        screen(fontScale=1.5f);compose.onNodeWithTag("save_diary").assertIsDisplayed()
        compose.onNodeWithTag("diary_fields").performScrollToIndex(DiaryField.QUALITY.ordinal)
        compose.onNodeWithTag("input_QUALITY").performClick()
        compose.onNodeWithTag("choice_QUALITY_5").performScrollTo().assertIsDisplayed().assertHeightIsAtLeast(48.dp)
        capture("large-font")
    }
    private fun capture(name:String) {
        lateinit var image:android.graphics.Bitmap
        compose.runOnIdle {
            val view=compose.activity.window.decorView
            image=android.graphics.Bitmap.createBitmap(view.width,view.height,android.graphics.Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(image))
        }
        assertTrue(image.width>0 && image.height>0)
        val directory=File("../work/ui-phase1-renders").apply {mkdirs()}
        File(directory,name+".png").outputStream().use {image.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)}
    }
}
