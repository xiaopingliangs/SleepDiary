package app.sleepdiary.offline.ui.theme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun SleepDiaryTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) darkColorScheme(
        primary = Color(0xFF9AB4C0), background = Color(0xFF11171A), surface = Color(0xFF182125)
    ) else lightColorScheme(
        primary = Color(0xFF5E7C8A), background = Color(0xFFF5F7F8), surface = Color.White
    )
    MaterialTheme(colorScheme = colors, content = content)
}
