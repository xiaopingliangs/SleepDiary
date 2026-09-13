package app.sleepdiary.offline.application

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.entity.*
import app.sleepdiary.offline.data.dao.AssessmentWithAnswers
import app.sleepdiary.offline.domain.model.*
import app.sleepdiary.offline.diary
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34])
class ProtocolInputCaptureTest {
    private lateinit var db:SleepDiaryDatabase
    private val from=LocalDate.parse("2026-09-01")
    private val through=LocalDate.parse("2026-09-30")
    @Before fun setup() { db=Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(),SleepDiaryDatabase::class.java).build() }
    @After fun close() { db.close() }
    private fun bytes(e:SleepDiaryEntry=diary())=RawInputEncoding.encode(from,through,listOf(e),emptyList(),null)
    @Test fun savedDiaryEditDoesNotChangeEarlierCapture()=runBlocking {
        val original=diary().copy(comment="原始 Chinese 123")
        db.diaryDao().insert(original)
        val first=ProtocolInputCapture(db).capture(from,through)
        val frozen=first.canonicalBytes()
        db.diaryDao().update(original.copy(comment="后来修改"))
        val later=ProtocolInputCapture(db).capture(from,through)
        assertArrayEquals(frozen,first.canonicalBytes());assertNotEquals(first.contentHash,later.contentHash)
        assertEquals("后来修改",db.diaryDao().all().single().comment)
    }
    @Test fun metadataDoesNotRandomizeInputHash()=runBlocking {
        db.diaryDao().insert(diary())
        val a=ProtocolInputCapture(db).capture(from,through);val b=ProtocolInputCapture(db).capture(from,through)
        assertNotEquals(a.id,b.id);assertEquals(a.contentHash,b.contentHash)
    }
    @Test fun nullEmptyWhitespaceAndUnicodeAreDifferent() {
        val original=diary()
        val values=listOf(null,""," ","中文","e\u0301","é")
        assertEquals(values.size,values.map { CapturedProtocolInput("id","now",bytes(original.copy(comment=it))).contentHash }.distinct().size)
    }
    @Test fun everyRawDiaryFieldAffectsCapture() {
        val e=diary();val original=bytes(e)
        val changes=listOf(e.copy(id="changed"),e.copy(sleepDate="2026-09-10"),e.copy(bedEnteredAtLocal=e.bedEnteredAtLocal+" "),
            e.copy(trySleepAtLocal=e.trySleepAtLocal+" "),e.copy(sleepOnsetLatencyMin=e.sleepOnsetLatencyMin+1),
            e.copy(awakeningCount=e.awakeningCount+1),e.copy(wakeAfterSleepOnsetMin=e.wakeAfterSleepOnsetMin+1),
            e.copy(finalAwakeningAtLocal=e.finalAwakeningAtLocal+" "),e.copy(outOfBedAtLocal=e.outOfBedAtLocal+" "),
            e.copy(sleepQuality=e.sleepQuality+1),e.copy(comment="changed"),e.copy(timeZoneIdAtEntry="other"),
            e.copy(createdAtUtc="other"),e.copy(updatedAtUtc="other"),e.copy(diarySchemaVersion="other"),e.copy(consistencyWarning=!e.consistencyWarning))
        changes.forEach { assertFalse(original.contentEquals(bytes(it))) }
    }
    @Test fun arraysCanonicalizeWithoutMutatingRawValues() {
        val a=diary();val b=a.copy(id="other",sleepDate="2026-09-10")
        assertArrayEquals(RawInputEncoding.encode(from,through,listOf(a,b),emptyList(),null),
            RawInputEncoding.encode(from,through,listOf(b,a),emptyList(),null))
    }
    @Test fun inputAndOutputByteArraysCannotMutateSnapshot() {
        val data=bytes();val snap=CapturedProtocolInput("id","now",data);val original=snap.canonicalBytes()
        data.fill(0);snap.canonicalBytes().fill(0)
        assertArrayEquals(original,snap.canonicalBytes())
    }
    @Test fun absentStateDoesNotBecomeDefaultState() {
        assertFalse(RawInputEncoding.encode(from,through,emptyList(),emptyList(),null).contentEquals(
            RawInputEncoding.encode(from,through,emptyList(),emptyList(),AppState())))
    }
    @Test fun safetyDraftAndRawDetailsAreCapturedWithoutCompletingAssessment() {
        val a=SafetyAssessment("assessment","2026-09-13T00:00:00Z",derivedGate=SafetyGate.NO_MAJOR_FLAG,status=AssessmentStatus.DRAFT,isCurrent=true)
        val answer=SafetyAnswer(a.id,"S01","YES",detailJson=null)
        fun encode(pair:AssessmentWithAnswers)=RawInputEncoding.encode(from,through,emptyList(),listOf(pair),null)
        val pair=AssessmentWithAnswers(a,listOf(answer))
        assertFalse(encode(pair).contentEquals(encode(pair.copy(answers=listOf(answer.copy(detailJson=" raw "))))))
        assertFalse(encode(pair).contentEquals(encode(pair.copy(assessment=a.copy(status=AssessmentStatus.COMPLETED)))))
    }
    @Test fun captureRangeIsInclusiveAndPartOfHash()=runBlocking {
        db.diaryDao().insert(diary())
        val day=LocalDate.parse(diary().sleepDate)
        val included=ProtocolInputCapture(db).capture(day,day)
        assertFalse(included.canonicalBytes().contentEquals(RawInputEncoding.encode(day,day,emptyList(),emptyList(),null)))
        assertNotEquals(included.contentHash,ProtocolInputCapture(db).capture(day.plusDays(1),day.plusDays(1)).contentHash)
    }
    @Test fun invalidRangeDuplicateIdsAndMalformedUnicodeAreRejected() {
        val original=diary()
        assertTrue(runCatching { RawInputEncoding.encode(through,from,emptyList(),emptyList(),null) }.isFailure)
        assertTrue(runCatching { RawInputEncoding.encode(from,through,listOf(original,original),emptyList(),null) }.isFailure)
        assertTrue(runCatching { bytes(diary().copy(comment=String(charArrayOf(0xD800.toChar())))) }.isFailure)
    }
}
