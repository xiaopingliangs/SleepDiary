package app.sleepdiary.offline.ui

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.sleepdiary.offline.application.AppServices
import app.sleepdiary.offline.application.TreatmentStateController
import app.sleepdiary.offline.data.backup.BackupRepository
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.entity.AppState
import app.sleepdiary.offline.data.entity.SafetyAnswer
import app.sleepdiary.offline.data.repository.RoomDiaryUiStore
import app.sleepdiary.offline.data.repository.SafetyRepository
import app.sleepdiary.offline.domain.treatment.TreatmentMode
import app.sleepdiary.offline.ui.diary.DiaryViewModel
import app.sleepdiary.offline.ui.diary.HomePage
import app.sleepdiary.offline.ui.theme.SleepDiaryTheme
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
class PublicLegacyDataIntegrationTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private lateinit var db: SleepDiaryDatabase
    private lateinit var services: AppServices
    private lateinit var safetyId: String

    @Before fun restoreLegacyHistoryBeforeOpeningPublicShell() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(context, SleepDiaryDatabase::class.java).build()
        db.stateAndSummaryDao().putState(AppState(hasCompletedOnboarding = true))
        safetyId = UUID.randomUUID().toString()
        SafetyRepository(db).save(safetyId, listOf(SafetyAnswer(safetyId, "S01", "NO", detailJson = null)), completed = false)
        val treatment = TreatmentStateController(
            db,
            Clock.fixed(Instant.parse("2026-09-09T12:00:00Z"), ZoneOffset.UTC)
        ).startBaseline(UUID.randomUUID().toString(), TreatmentMode.SELF_GUIDED, adultScopeConfirmed = true)
        assertTrue(treatment is app.sleepdiary.offline.application.TreatmentCommandResult.Applied)

        val backup = BackupRepository(db, context)
        val savedSnapshot = backup.snapshot()
        val text = backup.export()
        db.stateAndSummaryDao().putState(checkNotNull(db.stateAndSummaryDao().state()).copy(currentSafetyAssessmentId = null))
        db.treatmentDao().deleteAllAuditsForRestore()
        db.treatmentDao().deleteAllEpisodesForRestore()
        db.safetyDao().deleteAllAnswers()
        db.safetyDao().deleteAllAssessments()
        backup.restore(text)
        assertEquals(savedSnapshot, backup.snapshot())
        assertEquals(safetyId, db.safetyDao().byId(safetyId)?.assessment?.id)
        assertEquals(1, db.treatmentDao().episodes().size)
        services = AppServices(context, db)
    }

    @After fun close() { db.close() }

    @Test fun restoredSafetyAndTreatmentHistoryRemainStoredButPublicShellHasNoWorkflow() {
        val clock = Clock.fixed(Instant.parse("2026-09-09T12:00:00Z"), ZoneOffset.UTC)
        lateinit var model: DiaryViewModel
        compose.runOnUiThread { model = DiaryViewModel(RoomDiaryUiStore(db, clock), SavedStateHandle(), clock) }
        compose.setContent { SleepDiaryTheme { SleepDiaryApp(model, {}, services) } }
        compose.waitUntil(5000) { model.state.value.page != HomePage.LOADING }

        assertEquals(HomePage.TODAY, model.state.value.page)
        assertEquals(safetyId, model.state.value.safety?.assessment?.id)
        for (tag in listOf("public_tab_today", "public_tab_sleep", "public_tab_education", "public_tab_me")) {
            compose.onNodeWithTag(tag).performClick()
            compose.onAllNodesWithText("开始治疗").assertCountEquals(0)
            compose.onAllNodesWithText("建立治疗计划").assertCountEquals(0)
            compose.onAllNodesWithText("重新填写安全问询").assertCountEquals(0)
            compose.onAllNodesWithText("查看当前安全问询").assertCountEquals(0)
            compose.onAllNodesWithText("继续未完成的安全问询").assertCountEquals(0)
        }
        runBlocking {
            assertEquals(safetyId, db.safetyDao().byId(safetyId)?.assessment?.id)
            assertEquals(1, db.treatmentDao().episodes().size)
        }
    }
}
