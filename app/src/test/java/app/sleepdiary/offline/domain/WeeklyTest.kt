package app.sleepdiary.offline.domain

import app.sleepdiary.offline.diary
import app.sleepdiary.offline.domain.weekly.*
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate

class WeeklyTest {
    private val start=LocalDate.parse("2026-12-28")
    @Test fun dayOneSevenEightCrossMonthAndYear() {
        listOf(0L to 1,6L to 1,7L to 2,14L to 3,35L to 6).forEach { (delta,index) ->
            val p=PeriodCalculator.containing(start,start.plusDays(delta))
            assertEquals(index,p.index);assertEquals(6,java.time.temporal.ChronoUnit.DAYS.between(p.start,p.end).toInt())
        }
        assertEquals(LocalDate.parse("2027-01-03"),PeriodCalculator.at(start,1).end)
    }
    @Test fun emptyWeeksDoNotStopIndex() { assertEquals(5,PeriodCalculator.containing(start,start.plusDays(28)).index) }
    @Test fun beforeStartIsRejected() { assertTrue(runCatching { PeriodCalculator.containing(start,start.minusDays(1)) }.isFailure) }
    @Test fun fullWeekAveragesAndSources() {
        val entries=(0L..6L).map { diary(start.plusDays(it).toString()).copy(sleepOnsetLatencyMin=it.toInt()+1) }
        val v=WeeklySummaryCalculator.calculate(PeriodCalculator.at(start,1),entries.reversed())
        assertEquals(7,v.validEntryCount);assertEquals(4.0,v.avgSolMin!!,0.0);assertEquals(2.0,v.avgAwakeningCount!!,0.0)
        assertEquals(30.0,v.avgWasoMin!!,0.0);assertEquals(60.0,v.avgTerminalWakefulnessMin!!,0.0)
        assertEquals(480.0,v.avgTibMin!!,0.0);assertEquals(3.0,v.avgSleepQuality!!,0.0)
        assertEquals(entries.map { it.id },v.sourceEntryIds)
    }
    @Test fun sixRecordsDoNotAverageMissingDayAsZero() {
        val entries=(0L..5L).map { diary(start.plusDays(it).toString()) }
        val v=WeeklySummaryCalculator.calculate(PeriodCalculator.at(start,1),entries)
        assertEquals(6,v.validEntryCount);assertEquals(20.0,v.avgSolMin!!,0.0)
    }
    @Test fun emptyPeriodHasNullMeans() {
        val v=WeeklySummaryCalculator.calculate(PeriodCalculator.at(start,1),emptyList())
        assertEquals(0,v.validEntryCount);assertTrue(v.sourceEntryIds.isEmpty())
        listOf(v.avgSolMin,v.avgAwakeningCount,v.avgWasoMin,v.avgTerminalWakefulnessMin,v.avgTibMin,v.avgSleepQuality).forEach { assertNull(it) }
    }
    @Test fun dayEightIsExcludedAndMeansAreNotRounded() {
        val entries=listOf(diary(start.toString()).copy(sleepOnsetLatencyMin=1),diary(start.plusDays(1).toString()).copy(sleepOnsetLatencyMin=1),
            diary(start.plusDays(2).toString()).copy(sleepOnsetLatencyMin=2),diary(start.plusDays(7).toString()).copy(sleepOnsetLatencyMin=100))
        val v=WeeklySummaryCalculator.calculate(PeriodCalculator.at(start,1),entries)
        assertEquals(3,v.validEntryCount);assertEquals(4.0/3.0,v.avgSolMin!!,0.0)
    }
    @Test fun daytimeDurations() {
        val date=start.toString()
        val e=diary(date).copy(bedEnteredAtLocal=date+"T08:00:00",trySleepAtLocal=date+"T08:20:00",finalAwakeningAtLocal=date+"T14:00:00",outOfBedAtLocal=date+"T14:30:00")
        val v=WeeklySummaryCalculator.calculate(PeriodCalculator.at(start,1),listOf(e))
        assertEquals(390.0,v.avgTibMin!!,0.0);assertEquals(30.0,v.avgTerminalWakefulnessMin!!,0.0)
    }
}
