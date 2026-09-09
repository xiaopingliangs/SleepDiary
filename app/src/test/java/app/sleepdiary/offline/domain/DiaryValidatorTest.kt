package app.sleepdiary.offline.domain

import app.sleepdiary.offline.diary
import app.sleepdiary.offline.domain.validation.DiaryValidator
import org.junit.Test
import org.junit.Assert.*

class DiaryValidatorTest {
    @Test fun midnightIsValid() { assertTrue(DiaryValidator.validate(diary()).canSave) }
    @Test fun daytimeIsValid() {
        assertTrue(DiaryValidator.validate(diary().copy(bedEnteredAtLocal="2026-09-09T08:00:00", trySleepAtLocal="2026-09-09T08:20:00",
            finalAwakeningAtLocal="2026-09-09T14:00:00", outOfBedAtLocal="2026-09-09T14:30:00")).canSave)
    }
    @Test fun eachReversedPairIsHardError() {
        val e=diary()
        listOf(e.copy(bedEnteredAtLocal="2026-09-09T00:00:00"),e.copy(trySleepAtLocal="2026-09-09T07:00:00"),
            e.copy(outOfBedAtLocal="2026-09-09T05:00:00")).forEach { assertTrue("timeOrder" in DiaryValidator.validate(it).errors) }
    }
    @Test fun estimatesBeyondWindowWarnWithoutMutation() {
        val e=diary().copy(sleepOnsetLatencyMin=400)
        val result=DiaryValidator.validate(e)
        assertTrue(result.canSave); assertTrue(result.consistencyWarning); assertEquals(400,e.sleepOnsetLatencyMin)
    }
    @Test fun exactWindowDoesNotWarn() { assertFalse(DiaryValidator.validate(diary().copy(sleepOnsetLatencyMin=360)).consistencyWarning) }
    @Test fun zeroAwakeningsAndZeroWasoAreValid() {
        assertTrue(DiaryValidator.validate(diary().copy(awakeningCount=0,wakeAfterSleepOnsetMin=0)).canSave)
    }
    @Test fun allTimesCanBeEqualWithZeroEstimates() {
        val t="2026-09-09T07:00:00"
        val r=DiaryValidator.validate(diary().copy(bedEnteredAtLocal=t,trySleepAtLocal=t,finalAwakeningAtLocal=t,
            sleepOnsetLatencyMin=0,wakeAfterSleepOnsetMin=0,awakeningCount=0))
        assertTrue(r.canSave); assertFalse(r.consistencyWarning)
    }
    @Test fun numericUpperBoundsAreTechnicalNotMedicalErrors() {
        val r=DiaryValidator.validate(diary().copy(sleepOnsetLatencyMin=1440,wakeAfterSleepOnsetMin=1440,awakeningCount=99,sleepQuality=5))
        assertTrue(r.canSave); assertTrue(r.consistencyWarning)
    }
    @Test fun numericOutsideBoundsFail() {
        val e=diary()
        listOf(e.copy(sleepOnsetLatencyMin=-1),e.copy(sleepOnsetLatencyMin=1441),e.copy(wakeAfterSleepOnsetMin=-1),
            e.copy(wakeAfterSleepOnsetMin=1441),e.copy(awakeningCount=-1),e.copy(awakeningCount=100),
            e.copy(sleepQuality=0),e.copy(sleepQuality=6),e.copy(sleepOnsetLatencyMin=Int.MAX_VALUE))
            .forEach { assertFalse(DiaryValidator.validate(it).canSave) }
    }
    @Test fun malformedTimeAndDateFail() {
        assertFalse(DiaryValidator.validate(diary().copy(trySleepAtLocal="23:30")).canSave)
        assertFalse(DiaryValidator.validate(diary().copy(sleepDate="2026-02-30")).canSave)
    }
    @Test fun sleepDateMustBeOutOfBedDate() { assertFalse(DiaryValidator.validate(diary().copy(sleepDate="2026-09-08")).canSave) }
    @Test fun versionUuidAndZoneAreValidated() {
        listOf(diary().copy(id="1"),diary().copy(timeZoneIdAtEntry="invalid"),diary().copy(diarySchemaVersion="unknown"))
            .forEach { assertFalse(DiaryValidator.validate(it).canSave) }
    }
}
