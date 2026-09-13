package app.sleepdiary.offline.application

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.sleepdiary.offline.clockOn
import app.sleepdiary.offline.diary
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.entity.*
import app.sleepdiary.offline.domain.model.*
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class) @Config(sdk = [34])
class ClinicianSummaryServiceTest {
    private lateinit var db: SleepDiaryDatabase
    private lateinit var service: ClinicianSummaryService
    private val date = LocalDate.parse("2026-09-09")
    private val secret = "中文 English 123,\"备注\"\n第二行"
    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), SleepDiaryDatabase::class.java).build()
        service = ClinicianSummaryService(db, clockOn("2026-09-13"))
    }
    @After fun close() { db.close() }
    private suspend fun safety(current: Boolean = true, id: String = "safety-secret") {
        db.safetyDao().insertAssessment(SafetyAssessment(id, "2026-09-09T00:00:00Z", derivedGate = SafetyGate.CLINICIAN_REVIEW,
            status = AssessmentStatus.DRAFT, isCurrent = current))
        db.safetyDao().insertAnswers(listOf(SafetyAnswer(id, "S09", "FLAGS_PRESENT",
            detailJson = "{\"selectedCodes\":[\"LOUD_SNORING\"]}")))
    }
    @Test fun defaultPreviewExcludesSensitivePayload() = runBlocking {
        db.diaryDao().insert(diary().copy(comment = secret)); safety()
        val text = service.prepare(ClinicianSummaryRequest(date, date))
        assertFalse(text.contains(secret)); assertFalse(text.contains("safety-secret")); assertFalse(text.contains("LOUD_SNORING"))
        assertTrue(text.contains("估计入睡耗时：20")); assertTrue(text.contains("CBT-I Protocol NONE"))
    }
    @Test fun commentsRequireSeparateOptInAndStayUnchangedInDatabase() = runBlocking {
        val original = diary().copy(comment = secret)
        db.diaryDao().insert(original); safety()
        val text = service.prepare(ClinicianSummaryRequest(date, date, includeComments = true))
        assertTrue(text.contains("中文 English 123,\"备注\"")); assertTrue(text.contains("  第二行"))
        assertFalse(text.contains("LOUD_SNORING")); assertEquals(original, db.diaryDao().byId(original.id))
    }
    @Test fun safetyOptInPreservesDraftAndExcludesRetiredAssessment() = runBlocking {
        safety(); safety(false, "retired-secret"); db.diaryDao().insert(diary().copy(comment = secret))
        val text = service.prepare(ClinicianSummaryRequest(date, date, includeCurrentSafety = true))
        assertTrue(text.contains("safety-secret")); assertTrue(text.contains("DRAFT")); assertTrue(text.contains("LOUD_SNORING"))
        assertFalse(text.contains("retired-secret")); assertFalse(text.contains("中文 English"))
        assertTrue(text.contains("不是诊断")); assertEquals(2, db.safetyDao().all().size)
    }
    @Test fun absentSafetyIsNotPresentedAsClearance() = runBlocking {
        val text = service.prepare(ClinicianSummaryRequest(date, date, includeCurrentSafety = true))
        assertTrue(text.contains("不能解释为没有安全问题"))
    }
    @Test fun inclusiveRangeCountsCalendarDaysWithoutFillingMissingDays() = runBlocking {
        listOf("2026-09-07", "2026-09-09", "2026-09-11", "2026-09-12").forEach { db.diaryDao().insert(diary(it)) }
        val text = service.prepare(ClinicianSummaryRequest(date, date.plusDays(2)))
        assertTrue(text.contains("有记录 2 天 / 3 个日历日"))
        assertFalse(text.contains("日期：2026-09-07")); assertFalse(text.contains("日期：2026-09-12"))
        assertTrue(text.indexOf("日期：2026-09-09") < text.indexOf("日期：2026-09-11"))
    }
    @Test fun emptyRangeIsAnHonestEmptyState() = runBlocking {
        val text = service.prepare(ClinicianSummaryRequest(date, date))
        assertTrue(text.contains("没有睡眠记录")); assertTrue(text.contains("有记录 0 天 / 1 个日历日"))
    }
    @Test(expected = IllegalArgumentException::class) fun reversedRangeIsRejected() {
        ClinicianSummaryRequest(date.plusDays(1), date)
    }
    @Test fun previewDoesNotChangeAfterLaterEdits() = runBlocking {
        val original = diary().copy(comment = "before")
        db.diaryDao().insert(original)
        val request = ClinicianSummaryRequest(date, date, includeComments = true)
        val snapshot = service.prepare(request)
        db.diaryDao().update(original.copy(comment = "after"))
        assertTrue(snapshot.contains("before")); assertFalse(snapshot.contains("after"))
        assertTrue(service.prepare(request).contains("after"))
    }
    @Test fun warningAndVersionProvenanceArePreserved() = runBlocking {
        val e = diary().copy(consistencyWarning = true, sleepOnsetLatencyMin = 500)
        db.diaryDao().insert(e)
        val text = service.prepare(ClinicianSummaryRequest(date, date))
        assertTrue(text.contains("估计入睡耗时：500")); assertTrue(text.contains("原值未修正"))
        assertTrue(text.contains(e.id)); assertTrue(text.contains(e.updatedAtUtc)); assertTrue(text.contains(e.diarySchemaVersion))
    }
    @Test fun blankCommentsDoNotCreateEmptyNoteSections() = runBlocking {
        db.diaryDao().insert(diary().copy(comment = " "))
        assertFalse(service.prepare(ClinicianSummaryRequest(date, date, true)).contains("备注（用户原文）"))
    }
}
