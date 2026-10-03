package app.sleepdiary.offline.ui

import kotlin.math.roundToInt

/** Formats an averaged duration while stored and calculated values remain minutes. */
object AverageDurationText {
    fun fromMinutes(minutes: Double): String {
        require(minutes >= 0.0 && minutes.isFinite())
        val rounded = minutes.roundToInt()
        val hours = rounded / 60
        val remainder = rounded % 60
        return when {
            hours == 0 -> "${remainder}分钟"
            remainder == 0 -> "${hours}小时"
            else -> "${hours}小时${remainder}分钟"
        }
    }
}
