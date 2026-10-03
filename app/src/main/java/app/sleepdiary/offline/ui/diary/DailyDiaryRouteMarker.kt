package app.sleepdiary.offline.ui.diary

import android.content.SharedPreferences
import java.time.LocalDate

/** Private UI routing state, intentionally excluded from Room and user backups. */
fun interface DailyDiaryRouteMarker {
    /** Claims a new local date once; dates earlier than the high-water mark are ignored. */
    fun claimIfNewer(localDate: LocalDate): Boolean
}

class SharedPreferencesDailyDiaryRouteMarker(
    private val preferences: SharedPreferences
) : DailyDiaryRouteMarker {
    @Synchronized
    override fun claimIfNewer(localDate: LocalDate): Boolean {
        val previous = preferences.getString(KEY, null)?.let(LocalDate::parse)
        if (previous != null && !localDate.isAfter(previous)) return false
        return preferences.edit().putString(KEY, localDate.toString()).commit()
    }

    companion object {
        const val KEY = "lastDailyAutoDiaryRouteDate"
    }
}

object NoDailyDiaryRouteMarker : DailyDiaryRouteMarker {
    override fun claimIfNewer(localDate: LocalDate) = false
}
