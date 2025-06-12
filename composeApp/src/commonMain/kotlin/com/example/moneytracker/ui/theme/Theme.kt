import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Neon color palette
val NeonGreen = Color(0xFFCCFF00)
val DarkGreen = Color(0xFF1A2F00)
val BackgroundBlack = Color(0xFF121212)
val SurfaceBlack = Color(0xFF1E1E1E)
val White = Color(0xFFFFFFFF)
val Gray = Color(0xFF2F2F2F)

private val DarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = DarkGreen,
    primaryContainer = NeonGreen.copy(alpha = 0.1f),
    onPrimaryContainer = NeonGreen,
    secondary = NeonGreen,
    onSecondary = DarkGreen,
    background = BackgroundBlack,
    surface = SurfaceBlack,
    onBackground = White,
    onSurface = White,
    error = Color(0xFFFF5252),
    onError = White
)

private val LightColorScheme = lightColorScheme(
    primary = DarkGreen,
    onPrimary = White,
    primaryContainer = NeonGreen,
    onPrimaryContainer = DarkGreen,
    secondary = NeonGreen,
    onSecondary = DarkGreen,
    background = Color(0xFFF5F5F5),
    surface = White,
    onBackground = DarkGreen,
    onSurface = DarkGreen,
    error = Color(0xFFB00020),
    onError = White
)

@Composable
fun MoneyTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
} 