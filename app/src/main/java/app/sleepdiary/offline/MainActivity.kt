package app.sleepdiary.offline

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.sleepdiary.offline.ui.SleepDiaryApp
import app.sleepdiary.offline.ui.diary.DiaryViewModel
import app.sleepdiary.offline.ui.diary.SharedPreferencesDailyDiaryRouteMarker
import app.sleepdiary.offline.ui.theme.SleepDiaryTheme
import java.time.ZoneId

class MainActivity:ComponentActivity() {
    private var diaryModel: DiaryViewModel? = null
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as SleepDiaryApplication).container
        val factory=object:ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T:ViewModel> create(modelClass:Class<T>,extras:CreationExtras):T =
                DiaryViewModel(
                    container.diaryStore,
                    extras.createSavedStateHandle(),
                    dailyDiaryRouteMarker = SharedPreferencesDailyDiaryRouteMarker(
                        getSharedPreferences("public_daily_routing", MODE_PRIVATE)
                    ),
                    zoneProvider = { ZoneId.systemDefault() }
                ) as T
        }
        val model=ViewModelProvider(this,factory)[DiaryViewModel::class.java]
        diaryModel = model
        val services=container.services
        services.share.cleanupExpired()
        setContent {SleepDiaryTheme {SleepDiaryApp(model,{finishAndRemoveTask()},services)}}
    }

    override fun onResume() {
        super.onResume()
        diaryModel?.onAppForeground()
    }
}
