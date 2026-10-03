package app.sleepdiary.offline.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.sleepdiary.offline.clockOn
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.entity.AppState
import app.sleepdiary.offline.data.entity.WeeklySummarySnapshot
import app.sleepdiary.offline.data.repository.WeeklySummaryRepository
import app.sleepdiary.offline.diary
import app.sleepdiary.offline.domain.model.Versions
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.json.JSONArray
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class StaleWeeklySnapshotRepairTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private lateinit var db: SleepDiaryDatabase

    @Before fun setup() { db = Room.inMemoryDatabaseBuilder(context, SleepDiaryDatabase::class.java).build() }
    @After fun close() { db.close() }

    @Test fun staleCurrentSnapshotIsSupersededAndInvalidLegacyEntryIsExcluded() = runBlocking {
        val start = "2026-09-03"
        val valid = diary("2026-09-09")
        val malformed = diary("2026-09-08").copy(bedEnteredAtLocal = "2026-09-07T00:00:00")
        db.diaryDao().insert(valid); db.diaryDao().insert(malformed)
        db.stateAndSummaryDao().putState(AppState(trackingStartDate = start))
        val old = WeeklySummarySnapshot(
            UUID.randomUUID().toString(), 1, start, "2026-09-09", 2,
            20.0, 2.0, 30.0, 60.0, 1170.0, 3.0,
            JSONArray(listOf(valid.id, malformed.id)).toString(),
            Versions.DIARY_SCHEMA, Versions.WEEKLY_RULE, "2026-09-09T12:00:00Z", 1, true, null
        )
        db.stateAndSummaryDao().insertSummary(old)

        val current = WeeklySummaryRepository(db, clockOn("2026-09-09")).currentSummaries().single()

        assertNotEquals(old.id, current.id)
        assertEquals(2, current.revision)
        assertEquals(old.id, current.supersedesSummaryId)
        assertTrue(current.isCurrent)
        assertEquals(480.0, current.avgTibMin!!, 0.0)
        assertEquals(listOf(valid.id), JSONArray(current.sourceEntryIdsJson).let { a -> (0 until a.length()).map(a::getString) })
        assertFalse(db.stateAndSummaryDao().summaryById(old.id)!!.isCurrent)
    }
}
