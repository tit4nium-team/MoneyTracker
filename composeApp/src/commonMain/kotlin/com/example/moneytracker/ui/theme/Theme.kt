import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Modern color palette
val Primary = Color(0xFF6200EE)
val PrimaryDark = Color(0xFF3700B3)
val Secondary = Color(0xFF03DAC6)
val SecondaryDark = Color(0xFF018786)
val Background = Color(0xFFF5F5F5)
val BackgroundDark = Color(0xFF121212)
val Surface = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF1E1E1E)
val Error = Color(0xFFB00020)
val ErrorDark = Color(0xFFCF6679)
val Success = Color(0xFF4CAF50)
val SuccessDark = Color(0xFF1B5E20)
val OnPrimary = Color(0xFFFFFFFF)
val OnSecondary = Color(0xFF000000)
val OnBackground = Color(0xFF000000)
val OnBackgroundDark = Color(0xFFFFFFFF)
val OnSurface = Color(0xFF000000)
val OnSurfaceDark = Color(0xFFFFFFFF)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = OnPrimary,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryDark,
    onSecondaryContainer = OnSecondary,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    error = ErrorDark,
    onError = OnPrimary,
    errorContainer = Error.copy(alpha = 0.1f),
    onErrorContainer = Error
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = OnPrimary,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryDark,
    onSecondaryContainer = OnSecondary,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    error = Error,
    onError = OnPrimary,
    errorContainer = Error.copy(alpha = 0.1f),
    onErrorContainer = Error
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