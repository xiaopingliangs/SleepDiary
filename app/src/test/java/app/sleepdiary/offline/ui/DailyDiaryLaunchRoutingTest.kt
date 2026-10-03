package app.sleepdiary.offline.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import app.sleepdiary.offline.clockOn
import app.sleepdiary.offline.diary
import app.sleepdiary.offline.data.dao.AssessmentWithAnswers
import app.sleepdiary.offline.data.entity.AppState
import app.sleepdiary.offline.data.entity.SleepDiaryEntry
import app.sleepdiary.offline.data.repository.DiaryUiStore
import app.sleepdiary.offline.data.repository.SaveResult
import app.sleepdiary.offline.ui.diary.*
import app.sleepdiary.offline.ui.theme.SleepDiaryTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
class DailyDiaryLaunchRoutingTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val visibleModel = mutableStateOf<DiaryViewModel?>(null)
    private var contentInstalled = false

    private class Marker : DailyDiaryRouteMarker {
        var last: LocalDate? = null
        val claims = mutableListOf<LocalDate>()
        override fun claimIfNewer(localDate: LocalDate): Boolean {
            claims += localDate
            if (last != null && !localDate.isAfter(last)) return false
            last = localDate
            return true
        }
    }

    private class Store : DiaryUiStore {
        var app = AppState(hasCompletedOnboarding = true)
        val rows = mutableListOf<SleepDiaryEntry>()
        var safetyWriteCount = 0
        override suspend fun state() = app
        override suspend fun entries() = rows.toList()
        override suspend fun currentSafety(): AssessmentWithAnswers? = null
        override suspend fun completeOnboarding() { app = app.copy(hasCompletedOnboarding = true) }
        override suspend fun save(entry: SleepDiaryEntry, confirmed: Boolean): SaveResult {
            rows.removeAll { it.id == entry.id }
            rows += entry
            return SaveResult.Success(entry, null)
        }
        override suspend fun saveSafety(id: String, answers: List<app.sleepdiary.offline.data.entity.SafetyAnswer>, completed: Boolean): AssessmentWithAnswers {
            safetyWriteCount++
            error("Public daily routing must not create Safety")
        }
        override suspend fun acknowledgeExplanation() { app = app.copy(hasSeenMinimalInteractionExplanation = true) }
    }

    private fun model(store: Store, marker: Marker, clock: Clock, handle: SavedStateHandle = SavedStateHandle()) =
        DiaryViewModel(store, handle, clock, marker) { clock.zone }

    private fun attach(value: DiaryViewModel) {
        if (!contentInstalled) {
            compose.setContent { SleepDiaryTheme { visibleModel.value?.let { SleepDiaryApp(it, {}) } } }
            contentInstalled = true
        }
        compose.runOnUiThread { visibleModel.value = value }
        compose.waitUntil(5000) { value.state.value.page != HomePage.LOADING }
    }

    private fun fill(model: DiaryViewModel, date: LocalDate) {
        model.answerTime(DiaryField.BED, date.minusDays(1).atTime(22, 0))
        model.answerTime(DiaryField.TRY, date.minusDays(1).atTime(22, 30))
        model.answerNumber(DiaryField.SOL, 15)
        model.answerNumber(DiaryField.COUNT, 1)
        model.answerNumber(DiaryField.WASO, 10)
        model.answerTime(DiaryField.FINAL, date.atTime(6, 0))
        model.answerTime(DiaryField.OUT, date.atTime(6, 30))
        model.answerNumber(DiaryField.QUALITY, 3)
    }

    @Test fun flowAFreshInstallOpensDiaryWithoutBlockingIntroductionThenNextLaunchGoesToToday() {
        val date = LocalDate.parse("2026-09-09")
        val store = Store().also { it.app = AppState() }
        val marker = Marker()
        val first = model(store, marker, clockOn(date.toString()))
        attach(first)
        assertEquals(HomePage.FORM, first.state.value.page)
        assertFalse(store.app.hasCompletedOnboarding)
        assertEquals(0, store.safetyWriteCount)
        assertEquals(date, first.state.value.form.referenceDate)
        fill(first, date)
        val savedId = first.state.value.form.id
        first.save()
        compose.waitUntil(5000) { first.state.value.page == HomePage.SUCCESS }
        assertEquals(1, store.rows.size)
        assertEquals(date.toString(), store.rows.single().sleepDate)
        first.exitAfterSuccess {}
        compose.waitUntil(5000) { !first.state.value.busy }

        val second = model(store, marker, clockOn(date.toString()))
        attach(second)
        assertEquals(HomePage.TODAY, second.state.value.page)
        assertEquals(savedId, second.state.value.today?.id)
        assertEquals(1, store.rows.size)
        assertEquals(0, store.safetyWriteCount)
    }

    @Test fun flowBExistingTodayRecordOpensForEditWithoutDuplicate() {
        val date = LocalDate.parse("2026-09-09")
        val existing = diary(date.toString())
        val store = Store().also { it.rows += existing }
        val marker = Marker()
        val first = model(store, marker, clockOn(date.toString()))
        attach(first)
        assertEquals(HomePage.FORM, first.state.value.page)
        assertEquals(existing.id, first.state.value.form.id)
        first.comment("编辑后的个人备注")
        first.save()
        compose.waitUntil(5000) { first.state.value.page == HomePage.SUCCESS }
        assertEquals(1, store.rows.size)
        assertEquals(existing.id, store.rows.single().id)
        assertEquals("编辑后的个人备注", store.rows.single().comment)
    }

    @Test fun flowCSecondAndThirdSameDayLaunchesRemainAtToday() {
        val date = LocalDate.parse("2026-09-09")
        val store = Store(); val marker = Marker()
        val first = model(store, marker, clockOn(date.toString())); attach(first)
        assertEquals(HomePage.FORM, first.state.value.page)
        val second = model(store, marker, clockOn(date.toString())); attach(second)
        assertEquals(HomePage.TODAY, second.state.value.page)
        val third = model(store, marker, clockOn(date.toString())); attach(third)
        assertEquals(HomePage.TODAY, third.state.value.page)
        assertEquals(listOf(date), marker.claims.distinct())
    }

    @Test fun flowDMidnightCreatesOneNewLocalDayRoute() {
        val marker = Marker(); val store = Store()
        val zone = ZoneId.of("Asia/Shanghai")
        val beforeMidnight = Clock.fixed(Instant.parse("2026-09-09T15:59:00Z"), zone)
        val atMidnight = Clock.fixed(Instant.parse("2026-09-09T16:00:00Z"), zone)
        val afterMidnight = Clock.fixed(Instant.parse("2026-09-09T16:01:00Z"), zone)
        val first = model(store, marker, beforeMidnight); attach(first)
        assertEquals(LocalDate.parse("2026-09-09"), first.state.value.form.referenceDate)
        first.onAppForeground()
        assertEquals(HomePage.FORM, first.state.value.page)
        val nextDay = model(store, marker, atMidnight); attach(nextDay)
        assertEquals(HomePage.FORM, nextDay.state.value.page)
        assertEquals(LocalDate.parse("2026-09-10"), nextDay.state.value.form.referenceDate)
        val repeat = model(store, marker, afterMidnight); attach(repeat)
        assertEquals(HomePage.TODAY, repeat.state.value.page)
        assertEquals(listOf(LocalDate.parse("2026-09-09"), LocalDate.parse("2026-09-10")), marker.claims.distinct())
    }

    @Test fun flowEProcessRecreationRestoresEditingFormWithoutRerouting() {
        val date = LocalDate.parse("2026-09-09")
        val marker = Marker(); val store = Store(); val handle = SavedStateHandle()
        val original = model(store, marker, clockOn(date.toString()), handle); attach(original)
        val formId = original.state.value.form.id
        val recreated = model(store, marker, clockOn(date.toString()), handle); attach(recreated)
        assertEquals(HomePage.FORM, recreated.state.value.page)
        assertEquals(formId, recreated.state.value.form.id)
        assertEquals(1, marker.claims.size)
    }

    @Test fun flowFReturningFromExternalUiSameDayDoesNotRerouteOrChangeSelectedDestination() {
        val date = LocalDate.parse("2026-09-09")
        val marker = Marker(); val store = Store()
        val current = model(store, marker, clockOn(date.toString())); attach(current)
        assertEquals(HomePage.FORM, current.state.value.page)
        current.cancelForm()
        compose.waitUntil(5000) { current.state.value.page == HomePage.TODAY }
        compose.onNodeWithTag("public_tab_education").performClick()
        current.onAppForeground() // same callback used on return from SAF, browser, or app switching
        compose.waitForIdle()
        compose.onNodeWithText("了解睡眠").assertExists()
        assertEquals(date, marker.last)
        assertTrue(store.rows.isEmpty())
    }

    @Test fun flowTimezoneDateChangeUsesMonotonicLocalDateAndDoesNotRewriteDiary() {
        val marker = Marker(); val store = Store()
        val instant = Instant.parse("2026-09-10T01:00:00Z")
        val west = ZoneId.of("America/Los_Angeles")
        val east = ZoneId.of("Asia/Tokyo")
        val westFirst = model(store, marker, Clock.fixed(instant, west)); attach(westFirst)
        assertEquals(LocalDate.of(2026, 9, 9), westFirst.state.value.form.referenceDate)
        val eastOpen = model(store, marker, Clock.fixed(instant, east)); attach(eastOpen)
        assertEquals(LocalDate.of(2026, 9, 10), eastOpen.state.value.form.referenceDate)
        val westAgain = model(store, marker, Clock.fixed(instant, west)); attach(westAgain)
        assertEquals(HomePage.TODAY, westAgain.state.value.page)
        assertTrue(store.rows.isEmpty())
        assertEquals(LocalDate.of(2026, 9, 10), marker.last)
    }
}
