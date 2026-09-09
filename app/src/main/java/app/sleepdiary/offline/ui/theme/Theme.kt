package app.sleepdiary.offline.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SleepDiaryTheme(darkTheme:Boolean=isSystemInDarkTheme(),content:@Composable ()->Unit) {
    val colors=if(darkTheme) darkColorScheme(primary=Color(0xFF9AB4C0),onPrimary=Color(0xFF24323A),secondaryContainer=Color(0xFF2D414A),onSecondaryContainer=Color(0xFFE8EEF1),background=Color(0xFF11171A),surface=Color(0xFF182125),
        onBackground=Color(0xFFE8EEF1),onSurface=Color(0xFFE8EEF1))
    else lightColorScheme(primary=Color(0xFF5E7C8A),onPrimary=Color.White,secondaryContainer=Color(0xFFE4ECEF),onSecondaryContainer=Color(0xFF24323A),secondary=Color(0xFF8198A4),background=Color(0xFFF5F7F8),surface=Color.White,
        onBackground=Color(0xFF24323A),onSurface=Color(0xFF24323A))
    MaterialTheme(colorScheme=colors,shapes=Shapes(medium=RoundedCornerShape(18.dp),large=RoundedCornerShape(20.dp)),content=content)
}
