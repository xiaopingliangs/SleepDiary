package app.sleepdiary.offline.domain.education

import app.sleepdiary.offline.diary
import app.sleepdiary.offline.data.entity.SleepDiaryEntry
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class ContextualEducationMatcherTest {
    private fun row(date: LocalDate, id: String, estimate: Int): SleepDiaryEntry {
        val bed = date.minusDays(1).atTime(22, 0)
        val attempt = bed.plusMinutes(30)
        val final = attempt.plusMinutes(estimate.toLong() + 10 + 10)
        val out = final.plusMinutes(30)
        return diary(date.toString(), UUID.nameUUIDFromBytes(id.toByteArray()).toString()).copy(
            bedEnteredAtLocal = bed.toString(), trySleepAtLocal = attempt.toString(),
            finalAwakeningAtLocal = final.toString(), outOfBedAtLocal = out.toString(),
            sleepOnsetLatencyMin = 10, wakeAfterSleepOnsetMin = 10
        )
    }

    @Test fun estimateIsNeutralIntervalLessEstimatedAwakeTime() {
        val entry = row(LocalDate.parse("2026-09-09"), "entry", 240)
        assertEquals(240, ContextualEducationMatcher.estimatedSleepDurationMinutes(entry))
        assertEquals("2026-09-09T02:50", entry.finalAwakeningAtLocal)
    }

    @Test fun invalidChronologyAndNegativeEstimateAreNotCalculated() {
        val valid = row(LocalDate.parse("2026-09-09"), "entry", 240)
        val malformed = valid.copy(bedEnteredAtLocal = "2026-09-08T00:00")
        assertNull(ContextualEducationMatcher.estimatedSleepDurationMinutes(malformed))
        val negative = valid.copy(
            bedEnteredAtLocal = "2026-09-09T00:00",
            trySleepAtLocal = "2026-09-09T00:10",
            finalAwakeningAtLocal = "2026-09-09T00:29",
            outOfBedAtLocal = "2026-09-09T01:00",
            sleepOnsetLatencyMin = 20,
            wakeAfterSleepOnsetMin = 0,
            consistencyWarning = true
        )
        assertNull(ContextualEducationMatcher.estimatedSleepDurationMinutes(negative))
    }

    @Test fun exactContentBoundaryMatchesOneExperienceAndAboveBoundaryDoesNot() {
        val today = LocalDate.parse("2026-09-09")
        val at = row(today, "at", 240)
        val suggestion = checkNotNull(ContextualEducationMatcher.match(today, listOf(at)))
        assertEquals(ContextualEducationMatcher.RULE_ID, suggestion.ruleId)
        assertEquals("bad_night", suggestion.experienceId)
        assertEquals(UUID.nameUUIDFromBytes("at".toByteArray()).toString(), suggestion.sourceEntryId)
        assertEquals("一晚睡得很少，第二天真的一定会很糟吗？", suggestion.question)
        assertNull(ContextualEducationMatcher.match(today, listOf(row(today, "above", 241))))
    }

    @Test fun usesOnlyMostRecentValidEntryWithinSevenDaysAndNeverMutatesSource() {
        val today = LocalDate.parse("2026-09-09")
        val old = row(today.minusDays(6), "old", 240)
        val invalidRecent = row(today, "invalid", 240).copy(outOfBedAtLocal = "2026-09-08T07:00")
        val before = listOf(old, invalidRecent)
        val suggestion = checkNotNull(ContextualEducationMatcher.match(today, before))
        assertEquals(UUID.nameUUIDFromBytes("old".toByteArray()).toString(), suggestion.sourceEntryId)
        assertEquals(before, listOf(old, invalidRecent))
        assertNull(ContextualEducationMatcher.match(today, listOf(row(today.minusDays(7), "too-old", 200))))
    }

    @Test fun userCanDisableMatchingAndOutputIsAtMostOneSuggestion() {
        val today = LocalDate.parse("2026-09-09")
        val rows = listOf(row(today.minusDays(1), "a", 180), row(today, "b", 200))
        assertNull(ContextualEducationMatcher.match(today, rows, enabled = false))
        assertEquals(UUID.nameUUIDFromBytes("b".toByteArray()).toString(), ContextualEducationMatcher.match(today, rows)?.sourceEntryId)
    }
}
