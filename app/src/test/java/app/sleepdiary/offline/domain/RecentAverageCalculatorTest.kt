package app.sleepdiary.offline.domain

import app.sleepdiary.offline.diary
import app.sleepdiary.offline.domain.weekly.RecentAverageCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RecentAverageCalculatorTest {
    private val today = LocalDate.parse("2026-09-22")

    @Test fun sevenDayWindowIncludesTodayAndSixPreviousCalendarDays() {
        val rows = listOf(
            diary("2026-09-15").copy(sleepOnsetLatencyMin = 100),
            diary("2026-09-16").copy(sleepOnsetLatencyMin = 10),
            diary("2026-09-22").copy(sleepOnsetLatencyMin = 30)
        )
        val result = RecentAverageCalculator.calculate(today, 7, rows)
        assertEquals(2, result.validEntryCount)
        assertEquals(20.0, result.avgSolMin!!, 0.0)
    }

    @Test fun fourteenDayWindowIncludesOlderBoundaryButExcludesDayFifteen() {
        val rows = listOf(diary("2026-09-08"), diary("2026-09-09"), diary("2026-09-22"))
        val result = RecentAverageCalculator.calculate(today, 14, rows)
        assertEquals(2, result.validEntryCount)
        assertEquals(listOf(rows[1].id, rows[2].id), result.sourceEntryIds)
    }

    @Test fun missingDatesAreNotAveragedAsZero() {
        val rows = listOf(
            diary("2026-09-18").copy(sleepOnsetLatencyMin = 10),
            diary("2026-09-22").copy(sleepOnsetLatencyMin = 30)
        )
        val result = RecentAverageCalculator.calculate(today, 7, rows)
        assertEquals(2, result.validEntryCount)
        assertEquals(20.0, result.avgSolMin!!, 0.0)
    }

    @Test fun emptyWindowHasNullMeans() {
        val result = RecentAverageCalculator.calculate(today, 7, listOf(diary("2026-09-01")))
        assertEquals(0, result.validEntryCount)
        assertTrue(result.sourceEntryIds.isEmpty())
        listOf(result.avgSolMin,result.avgAwakeningCount,result.avgWasoMin,result.avgTerminalWakefulnessMin,result.avgTibMin,result.avgSleepQuality).forEach(::assertNull)
    }

    @Test fun onlySevenOrFourteenDaysAreAccepted() {
        assertTrue(runCatching { RecentAverageCalculator.calculate(today, 30, emptyList()) }.isFailure)
    }
}
