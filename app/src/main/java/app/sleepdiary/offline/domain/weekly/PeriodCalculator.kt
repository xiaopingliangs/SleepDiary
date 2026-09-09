package app.sleepdiary.offline.domain.weekly

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class DiaryPeriod(val index: Int, val start: LocalDate, val end: LocalDate)

object PeriodCalculator {
    fun containing(trackingStartDate: LocalDate, date: LocalDate): DiaryPeriod {
        val days = ChronoUnit.DAYS.between(trackingStartDate, date)
        require(days >= 0) { "Date precedes trackingStartDate" }
        return at(trackingStartDate, Math.toIntExact(days / 7 + 1))
    }
    fun at(trackingStartDate: LocalDate, index: Int): DiaryPeriod {
        require(index >= 1)
        val start = trackingStartDate.plusDays((index.toLong() - 1) * 7)
        return DiaryPeriod(index, start, start.plusDays(6))
    }
}
