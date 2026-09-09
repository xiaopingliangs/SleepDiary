package app.sleepdiary.offline.ui

import app.sleepdiary.offline.diary
import app.sleepdiary.offline.ui.diary.*
import java.time.*
import java.io.*
import org.junit.Test
import org.junit.Assert.*

class DiaryFormTest {
    private fun blank()=DiaryForm(referenceDate=LocalDate.parse("2026-09-09"),zoneId="Asia/Shanghai")
    @Test fun allAnswersStartNull() { val f=blank();assertNull(f.bed);assertNull(f.attempt);assertNull(f.sol);assertNull(f.count);assertNull(f.waso);assertNull(f.final);assertNull(f.out);assertNull(f.quality);assertNull(f.comment) }
    @Test fun fourAnchorsDoNotWriteAnswers() {
        val f=blank();val fields=listOf(DiaryField.BED,DiaryField.TRY,DiaryField.FINAL,DiaryField.OUT)
        val hours=listOf(LocalTime.of(22,0),LocalTime.of(22,30),LocalTime.of(6,0),LocalTime.of(6,30))
        fields.forEachIndexed {i,field -> assertEquals(hours[i],f.pickerPosition(field).toLocalTime());assertNull(f.time(field))}
        assertNull(f.toEntry(Instant.now()))
    }
    @Test fun confirmingOneTimeOnlyChangesThatAnswer() {
        val f=blank();val next=f.answerTime(DiaryField.BED,f.pickerPosition(DiaryField.BED))
        assertNotNull(next.bed);assertNull(next.attempt);assertNull(f.bed)
    }
    @Test fun reopenedPickerUsesExistingAnswer() {
        val f=blank().answerTime(DiaryField.BED,LocalDateTime.parse("2026-09-09T08:07"))
        assertEquals(f.bed,f.pickerPosition(DiaryField.BED))
    }
    @Test fun quickChoicesMapToTheirOwnIntegers() {
        var f=blank();listOf(5,10,15,20,30,45,60,90).forEach {f=f.answerNumber(DiaryField.SOL,it);assertEquals(it,f.sol)}
        listOf(0,5,10,15,20,30,45,60,90,120).forEach {f=f.answerNumber(DiaryField.WASO,it);assertEquals(it,f.waso)}
        assertEquals(90,f.sol);assertNull(f.quality)
    }
    @Test fun moreIsAnIntegerAndRangeIsEnforced() {
        listOf(6,99).forEach {assertEquals(it,blank().answerNumber(DiaryField.COUNT,it).count)}
        assertTrue(runCatching {blank().answerNumber(DiaryField.COUNT,100)}.isFailure)
    }
    @Test fun qualityHasNoDefaultAndKeepsMapping() {assertNull(blank().quality);(1..5).forEach {assertEquals(it,blank().answerNumber(DiaryField.QUALITY,it).quality)}}
    @Test fun completeDaytimeAndMidnightDatesArePreserved() {
        val night=DiaryForm.from(diary());assertEquals("2026-09-09",night.toEntry(Instant.now())!!.sleepDate)
        val day=night.copy(bed=LocalDateTime.parse("2026-09-09T08:00"),attempt=LocalDateTime.parse("2026-09-09T08:15"),final=LocalDateTime.parse("2026-09-09T14:00"),out=LocalDateTime.parse("2026-09-09T14:30"))
        assertEquals("2026-09-09T08:00",day.toEntry(Instant.now())!!.bedEnteredAtLocal)
    }
    @Test fun loadingEntryKeepsIdentityAndCreationTime() {val e=diary();val f=DiaryForm.from(e);assertEquals(e.id,f.id);assertEquals(e.createdAtUtc,f.createdAtUtc)}
    @Test fun formCanSurviveSavedStateSerializationWithoutDefaultAnswers() {
        val f=blank().answerNumber(DiaryField.SOL,15)
        val bytes=ByteArrayOutputStream().also {out ->ObjectOutputStream(out).use {it.writeObject(f)}}.toByteArray()
        val restored=ObjectInputStream(ByteArrayInputStream(bytes)).use {it.readObject()} as DiaryForm
        assertEquals(f,restored);assertNull(restored.bed);assertNull(restored.quality)
    }

    @Test fun midnightPositionFollowsConfirmedPrecedingTimeWithoutAnswering() {
        val f=blank().answerTime(DiaryField.BED,LocalDateTime.parse("2026-09-08T23:30"))
        assertEquals(LocalDateTime.parse("2026-09-09T00:15"),f.positionAt(DiaryField.TRY,LocalTime.of(0,15)))
        assertNull(f.attempt)
    }
    @Test fun laterAnswerDoesNotSilentlyRepositionEarlierDate() {
        val f=blank().answerTime(DiaryField.OUT,LocalDateTime.parse("2026-09-09T14:30"))
        assertEquals(LocalDateTime.parse("2026-09-08T08:00"),f.positionAt(DiaryField.BED,LocalTime.of(8,0)))
        assertNull(f.bed)
    }
    @Test fun editingExistingTimeDoesNotMoveItsExplicitDate() {
        val f=blank().answerTime(DiaryField.BED,LocalDateTime.parse("2026-09-09T08:00"))
        assertEquals(LocalDateTime.parse("2026-09-09T23:00"),f.positionAt(DiaryField.BED,LocalTime.of(23,0)))
    }
}
