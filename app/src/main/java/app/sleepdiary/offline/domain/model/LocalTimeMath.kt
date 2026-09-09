package app.sleepdiary.offline.domain.model

import java.time.Duration
import java.time.LocalDateTime

/** Subjective local times: no timezone conversion or treatment calculation. */
object LocalTimeMath {
    fun minutesBetween(start: LocalDateTime, end: LocalDateTime): Double {
        val duration = Duration.between(start, end)
        return duration.seconds / 60.0 + duration.nano / 60_000_000_000.0
    }
}
