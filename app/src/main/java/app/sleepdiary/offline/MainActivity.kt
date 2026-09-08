package app.sleepdiary.offline
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import app.sleepdiary.offline.ui.theme.SleepDiaryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SleepDiaryTheme { Surface { Text(getString(R.string.app_name)) } } }
    }
}
