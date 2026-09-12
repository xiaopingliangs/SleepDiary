package app.sleepdiary.offline

import android.app.Application
import app.sleepdiary.offline.application.AppContainer

class SleepDiaryApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
