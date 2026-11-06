package app.sleepdiary.offline.share

import app.sleepdiary.offline.diary
import app.sleepdiary.offline.domain.weekly.RecentAverageCalculator
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class RecentDiaryShareTest {
    private val today = LocalDate.parse("2026-09-22")

    @Test fun bothWindowsUseRealRowsAndListOnlyNonemptyCommentsOnTheirDates() {
        val rows = listOf(
            diary("2026-09-08").copy(comment="窗口之外"),
            diary("2026-09-09").copy(comment="十四天边界", sleepOnsetLatencyMin=60),
            diary("2026-09-16").copy(comment="七天边界\n第二行", sleepOnsetLatencyMin=10),
            diary("2026-09-21").copy(comment="  ", sleepOnsetLatencyMin=20),
            diary("2026-09-22").copy(comment="今天的情况", sleepOnsetLatencyMin=30),
            diary("2026-09-23").copy(comment="未来记录")
        )
        for (days in listOf(7,14)) {
            val lines=RecentDiaryShare.lines(today,days,rows)
            assertFalse(lines.contains("窗口之外"));assertFalse(lines.contains("未来记录"))
            assertFalse(lines.contains("  "))
            assertEquals(days==14,lines.contains("十四天边界"))
            assertEquals("2026-09-16",lines[lines.indexOf("七天边界\n第二行")-1])
            assertEquals("2026-09-22",lines[lines.indexOf("今天的情况")-1])
            assertTrue(lines.contains("平均估计入睡耗时（分钟）：${if(days==7) "20.0" else "30.0"}"))
            assertTrue(lines.contains("有记录的天数：${if(days==7) 3 else 4} / $days"))
        }
    }

    @Test fun emptyCommentsOmitWholeSectionAndEmptyWindowDoesNotInventZeros() {
        val lines=RecentDiaryShare.lines(today,7,listOf(diary("2026-09-22").copy(comment=null)))
        assertFalse(lines.any { it.contains("特殊情况") })
        val empty=RecentDiaryShare.lines(today,14,emptyList())
        assertTrue(empty.contains("有记录的天数：0 / 14"))
        assertTrue(empty.contains("平均估计入睡耗时（分钟）：无记录"))
        assertFalse(empty.any { it.contains("特殊情况") })
    }

    @Test fun allSixMeansAreCalculatedFromRawRowsAndInvalidChronologyCannotInflateThem() {
        val a=diary("2026-09-16").copy(sleepOnsetLatencyMin=10,awakeningCount=1,wakeAfterSleepOnsetMin=20,sleepQuality=2)
        val b=diary("2026-09-22").copy(sleepOnsetLatencyMin=30,awakeningCount=3,wakeAfterSleepOnsetMin=40,sleepQuality=4,outOfBedAtLocal="2026-09-22T06:30:00")
        val invalid=diary("2026-09-20").copy(bedEnteredAtLocal="2026-09-19T00:00:00",comment="保留原文")
        for (days in listOf(7,14)) {
            val result=RecentAverageCalculator.calculate(today,days,listOf(a,b,invalid))
            assertEquals(listOf(a.id,b.id),result.sourceEntryIds)
            assertEquals(20.0,result.avgSolMin!!,0.0)
            assertEquals(2.0,result.avgAwakeningCount!!,0.0)
            assertEquals(30.0,result.avgWasoMin!!,0.0)
            assertEquals(45.0,result.avgTerminalWakefulnessMin!!,0.0)
            assertEquals(465.0,result.avgTibMin!!,0.0)
            assertEquals(3.0,result.avgSleepQuality!!,0.0)
            val lines=RecentDiaryShare.lines(today,days,listOf(a,b,invalid))
            assertTrue(lines.contains("保留原文"))
            assertEquals("2026-09-20",lines[lines.indexOf("保留原文")-1])
        }
    }
}
