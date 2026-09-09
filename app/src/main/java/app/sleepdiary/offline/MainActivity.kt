package app.sleepdiary.offline

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.repository.RoomDiaryUiStore
import app.sleepdiary.offline.ui.SleepDiaryApp
import app.sleepdiary.offline.ui.diary.DiaryViewModel
import app.sleepdiary.offline.ui.theme.SleepDiaryTheme

class MainActivity:ComponentActivity() {
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        val factory=object:ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T:ViewModel> create(modelClass:Class<T>,extras:CreationExtras):T =
                DiaryViewModel(RoomDiaryUiStore(database()),extras.createSavedStateHandle()) as T
        }
        val model=ViewModelProvider(this,factory)[DiaryViewModel::class.java]
        setContent {SleepDiaryTheme {SleepDiaryApp(model,{finishAndRemoveTask()})}}
    }
    private fun database():SleepDiaryDatabase = synchronized(MainActivity::class.java) {
        sharedDatabase ?: SleepDiaryDatabase.open(applicationContext).also {sharedDatabase=it}
    }
    companion object {private var sharedDatabase:SleepDiaryDatabase?=null}
}
