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
val ErrorRed = Color(0xFFFF3B30) // Vibrant error red

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
    surfaceVariant = Gray,
    onSurfaceVariant = White.copy(alpha = 0.7f),
    error = ErrorRed,
    onError = White,
    errorContainer = ErrorRed.copy(alpha = 0.1f),
    onErrorContainer = ErrorRed
)

private val LightColorScheme = darkColorScheme( // Force dark theme for consistent neon look
    primary = NeonGreen,
    onPrimary = BackgroundBlack,
    primaryContainer = NeonGreen.copy(alpha = 0.1f),
    onPrimaryContainer = NeonGreen,
    secondary = NeonGreen,
    onSecondary = BackgroundBlack,
    background = BackgroundBlack,
    surface = SurfaceBlack,
    onBackground = White,
    onSurface = White,
    surfaceVariant = Gray,
    onSurfaceVariant = White.copy(alpha = 0.7f),
    error = ErrorRed,
    onError = White,
    errorContainer = ErrorRed.copy(alpha = 0.1f),
    onErrorContainer = ErrorRed
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