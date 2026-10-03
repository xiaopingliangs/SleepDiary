package app.sleepdiary.offline.ui.diary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.sleepdiary.offline.data.entity.SleepDiaryEntry
import app.sleepdiary.offline.data.repository.*
import app.sleepdiary.offline.domain.validation.DiaryValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

enum class HomePage { LOADING, INTRO, SAFETY, HELP, SAFETY_RESULT, TODAY, FORM, SUCCESS, WEEKLY }
data class DiaryUiState(
    val form:DiaryForm, val page:HomePage=HomePage.LOADING, val introPage:Int=0,
    val busy:Boolean=false, val message:String?=null, val missing:DiaryField?=null,
    val warning:Boolean=false, val entries:List<SleepDiaryEntry> = emptyList(),
    val today:SleepDiaryEntry?=null, val firstSuccess:Boolean=false,
    val safety:app.sleepdiary.offline.data.dao.AssessmentWithAnswers?=null,
    val safetyFromSettings:Boolean=false,
    val weekly:app.sleepdiary.offline.data.entity.WeeklySummarySnapshot?=null
)

class DiaryViewModel(
    private val store:DiaryUiStore,
    private val saved:SavedStateHandle=SavedStateHandle(),
    private val clock:Clock=Clock.systemDefaultZone(),
    private val dailyDiaryRouteMarker: DailyDiaryRouteMarker = NoDailyDiaryRouteMarker,
    private val zoneProvider: () -> ZoneId = { clock.zone }
):ViewModel() {
    private fun localZone() = zoneProvider()
    private fun localDate() = LocalDate.ofInstant(clock.instant(), localZone())
    private fun claimDailyRouteIfNeeded() {
        if (mutable.value.page != HomePage.TODAY) return
        val date = localDate()
        if (!dailyDiaryRouteMarker.claimIfNewer(date)) return
        val existing = mutable.value.entries.firstOrNull { it.sleepDate == date.toString() }
        if (existing != null) edit(existing) else beginNewDiary()
    }
    private val mutable=MutableStateFlow(DiaryUiState(saved.get<DiaryForm>("form") ?: DiaryForm(referenceDate=localDate(),zoneId=localZone().id),introPage=saved["introPage"] ?: 0,safetyFromSettings=saved["safetyFromSettings"] ?: false))
    val state=mutable.asStateFlow()
    init { start() }
    fun afterRestore() { saved.remove<Boolean>("editing");saved.remove<DiaryForm>("form");saved.remove<Int>("introPage");saved.remove<Boolean>("safetyFromSettings");saved.remove<String>("safetyPage");mutable.value=DiaryUiState(DiaryForm(referenceDate=localDate(),zoneId=localZone().id));start() }
    fun start() { viewModelScope.launch {
        try {
            val entries=store.entries();val appState=store.state()
            val safety=store.currentSafety()
            val today=entries.firstOrNull { it.sleepDate==localDate().toString() }
            val page=when {
                !appState.hasCompletedOnboarding -> HomePage.INTRO
                saved.get<Boolean>("editing")==true -> HomePage.FORM
                else -> HomePage.TODAY
            }
            mutable.value=mutable.value.copy(page=page,entries=entries,today=today,safety=safety,message=null)
            if (page == HomePage.TODAY) claimDailyRouteIfNeeded()
        } catch(c:CancellationException){throw c} catch(_:Exception){mutable.value=mutable.value.copy(message="读取记录失败，请重试")}
    } }
    /** Re-evaluates a new local date on app foreground; same-day Activity recreation is a no-op. */
    fun onAppForeground() {
        if (mutable.value.page != HomePage.TODAY) return
        viewModelScope.launch {
            try {
                val entries = store.entries()
                val date = localDate()
                val today = entries.firstOrNull { it.sleepDate == date.toString() }
                mutable.value = mutable.value.copy(
                    entries = entries,
                    today = today,
                    form = mutable.value.form.copy(referenceDate = date, zoneId = localZone().id)
                )
                claimDailyRouteIfNeeded()
            } catch (c: CancellationException) { throw c }
            catch (_: Exception) { mutable.value = mutable.value.copy(message = "读取记录失败，请重试") }
        }
    }
    fun nextIntro() {
        if(mutable.value.introPage<2) { val page=mutable.value.introPage+1;saved["introPage"]=page;mutable.value=mutable.value.copy(introPage=page) }
        else { if(mutable.value.busy)return;mutable.value=mutable.value.copy(busy=true);viewModelScope.launch {
            try {store.completeOnboarding();saved["introDone"]=true;mutable.value=mutable.value.copy(page=HomePage.TODAY,busy=false);claimDailyRouteIfNeeded()}
            catch(c:CancellationException){throw c}catch(_:Exception){mutable.value=mutable.value.copy(busy=false,message="说明状态保存失败，请重试")}
        } }
    }
    private fun diaryPage()=if(mutable.value.today!=null)HomePage.TODAY else HomePage.FORM
    fun continueDiary(){clearSafetyOrigin();mutable.value=mutable.value.copy(page=diaryPage(),message=null,safetyFromSettings=false)}
    private fun clearSafetyOrigin(){saved.remove<Boolean>("safetyFromSettings");saved.remove<String>("safetyPage")}
    fun returnFromSafety(onReturned:()->Unit) {
        if(mutable.value.busy)return
        mutable.value=mutable.value.copy(busy=true)
        viewModelScope.launch {try {
            val current=store.currentSafety()
            clearSafetyOrigin()
            mutable.value=mutable.value.copy(safety=current,page=HomePage.TODAY,safetyFromSettings=false,busy=false,message=null)
            onReturned()
        }catch(c:CancellationException){throw c}catch(_:Exception){mutable.value=mutable.value.copy(busy=false,message="读取安全问询失败，请重试")}}
    }
    fun openSafety(newAssessment:Boolean=false,fromSettings:Boolean=false){
        saved["safetyFromSettings"]=fromSettings;saved["safetyPage"]="SAFETY"
        if(newAssessment)mutable.value=mutable.value.copy(safety=null)
        mutable.value=mutable.value.copy(page=HomePage.SAFETY,message=null,safetyFromSettings=fromSettings)
    }
    fun viewSafety(){saved["safetyFromSettings"]=true;saved["safetyPage"]="SAFETY_RESULT";mutable.value=mutable.value.copy(page=HomePage.SAFETY_RESULT,safetyFromSettings=true)}
    fun saveSafety(id:String,answers:List<app.sleepdiary.offline.data.entity.SafetyAnswer>,completed:Boolean,onSaved:()->Unit) {
        if(mutable.value.busy)return;mutable.value=mutable.value.copy(busy=true,message=null)
        viewModelScope.launch {try{
            val result=store.saveSafety(id,answers,completed)
            val acute=answers.lastOrNull()?.let {it.questionId=="S03"&&it.answerCode!="NO"}==true
            mutable.value=mutable.value.copy(safety=result,busy=false,page=if(acute)HomePage.HELP else if(completed)HomePage.SAFETY_RESULT else HomePage.SAFETY)
            saved["safetyPage"]=mutable.value.page.name
            onSaved()
        }catch(c:CancellationException){throw c}catch(_:Exception){mutable.value=mutable.value.copy(busy=false,message="安全问询保存失败，请重试")}}
    }
    fun edit(entry:SleepDiaryEntry){updateForm(DiaryForm.from(entry));mutable.value=mutable.value.copy(page=HomePage.FORM)}
    fun beginNewDiary() {
        if (mutable.value.busy) return
        val now = localDate()
        val form = DiaryForm(referenceDate = now, zoneId = localZone().id)
        saved["form"] = form; saved["editing"] = true
        mutable.value = mutable.value.copy(form = form, page = HomePage.FORM, message = null, missing = null, warning = false)
    }
    fun editToday() { mutable.value.today?.let { updateForm(DiaryForm.from(it));mutable.value=mutable.value.copy(page=HomePage.FORM) } }
    private fun updateForm(form:DiaryForm) {
        if(mutable.value.busy) return
        saved["form"]=form;saved["editing"]=true
        mutable.value=mutable.value.copy(form=form,message=null,missing=null,warning=false)
    }
    fun cancelForm() {
        if (mutable.value.busy) return
        saved.remove<Boolean>("editing"); saved.remove<DiaryForm>("form")
        mutable.value = mutable.value.copy(page = HomePage.TODAY, message = null, missing = null, warning = false)
        onAppForeground()
    }
    fun returnToToday(onReturned: () -> Unit = {}) {
        if (mutable.value.page !in listOf(HomePage.SUCCESS, HomePage.WEEKLY) || mutable.value.busy) return
        mutable.value = mutable.value.copy(busy = true)
        viewModelScope.launch {
            try {
                mutable.value.weekly?.let { store.acknowledgeWeekly(it.periodIndex) }
                if (mutable.value.firstSuccess) store.acknowledgeExplanation()
                mutable.value = mutable.value.copy(page = HomePage.TODAY, busy = false)
                onReturned()
            } catch (c: CancellationException) { throw c }
            catch (_: Exception) { mutable.value = mutable.value.copy(busy = false, message = "状态更新失败，请重试") }
        }
    }
    fun answerTime(field:DiaryField,value:java.time.LocalDateTime)=updateForm(mutable.value.form.answerTime(field,value))
    fun answerNumber(field:DiaryField,value:Int)=updateForm(mutable.value.form.answerNumber(field,value))
    fun comment(value:String)=updateForm(mutable.value.form.copy(comment=value))
    fun dismissWarning(){mutable.value=mutable.value.copy(warning=false)}
    fun save(confirmWarning:Boolean=false) {
        if(mutable.value.busy || mutable.value.page!=HomePage.FORM) return
        val form=mutable.value.form
        val missing=form.firstUnanswered()
        if(missing!=null) {mutable.value=mutable.value.copy(missing=missing,message="请先完成这项记录");return}
        val input=checkNotNull(form.toEntry(clock.instant()))
        val validation=DiaryValidator.validate(input)
        if(!validation.canSave) {mutable.value=mutable.value.copy(message="请检查日期、时间先后顺序和输入范围，按原本的估计调整。",warning=false);return}
        if(validation.consistencyWarning && !confirmWarning) {mutable.value=mutable.value.copy(warning=true);return}
        mutable.value=mutable.value.copy(busy=true,message=null,warning=false)
        viewModelScope.launch {
            try {
                val first=!store.state().hasSeenMinimalInteractionExplanation
                when(val result=store.save(input.copy(consistencyWarning=validation.consistencyWarning && confirmWarning),confirmWarning)) {
                    is SaveResult.Success -> {
                        val entries=(mutable.value.entries.filterNot { it.id==result.entry.id }+result.entry).sortedBy { it.sleepDate }
                        saved["editing"]=false;saved["form"]=DiaryForm.from(result.entry)
                        mutable.value=mutable.value.copy(form=DiaryForm.from(result.entry),page=if(result.pendingSummary==null)HomePage.SUCCESS else HomePage.WEEKLY,weekly=result.pendingSummary,busy=false,firstSuccess=first,entries=entries,today=result.entry)
                    }
                    is SaveResult.ValidationWarning -> mutable.value=mutable.value.copy(busy=false,warning=true)
                    is SaveResult.Failure -> mutable.value=mutable.value.copy(busy=false,message=if(result.reason==SaveFailure.BEFORE_TRACKING_START)
                        "这个日期早于当前记录周期的起点。为了保持既有周数据可追溯，当前版本不能把历史记录移动到周期起点以前。" else "保存失败，请重试")
                }
            } catch(c:CancellationException){throw c} catch(_:Exception){mutable.value=mutable.value.copy(busy=false,message="保存失败，请重试")}
        }
    }
    fun exitAfterSuccess(exit:()->Unit) {
        if(mutable.value.page !in listOf(HomePage.SUCCESS,HomePage.WEEKLY) || mutable.value.busy) return
        mutable.value=mutable.value.copy(busy=true)
        viewModelScope.launch {
            try { mutable.value.weekly?.let {store.acknowledgeWeekly(it.periodIndex)}; if(mutable.value.firstSuccess) store.acknowledgeExplanation();mutable.value=mutable.value.copy(busy=false);exit() }
            catch(c:CancellationException){throw c} catch(_:Exception){mutable.value=mutable.value.copy(busy=false,message="保存失败，请重试")}
        }
    }
}
