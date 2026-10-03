package app.sleepdiary.offline.ui.diary

import androidx.test.core.app.ApplicationProvider
import android.content.Context
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DailyDiaryRouteMarkerTest {
    @Test fun persistentMarkerClaimsEachNewerLocalDateOnlyOnce() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("route-marker-test", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        val marker = SharedPreferencesDailyDiaryRouteMarker(prefs)
        val day9 = LocalDate.parse("2026-09-09")
        val day10 = LocalDate.parse("2026-09-10")
        assertTrue(marker.claimIfNewer(day9))
        assertFalse(marker.claimIfNewer(day9))
        assertTrue(marker.claimIfNewer(day10))
        assertFalse(marker.claimIfNewer(day9))
        assertEquals(day10.toString(), prefs.getString(SharedPreferencesDailyDiaryRouteMarker.KEY, null))
    }
}
