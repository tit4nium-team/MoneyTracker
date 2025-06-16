package com.example.moneytracker.ui.theme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PrimaryColor = Color(0xFF2E86DE)
val OnPrimaryColor = Color(0xFFFFFFFF)
val SecondaryColor = Color(0xFF27AE60)
val OnSecondaryColor = Color(0xFFFFFFFF)
val BackgroundLight = Color(0xFFF9F9F9)
val SurfaceLight = Color(0xFFFFFFFF)
val BackgroundDark = Color(0xFF121212)
val SurfaceDark = Color(0xFF1E1E1E)
val SurfaceVariant = Color(0xFFF0F2F5)
val TextPrimaryLight = Color(0xFF212121)
val TextPrimaryDark = Color(0xFFFFFFFF)
val ErrorColor = Color(0xFFEF5350)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    onPrimary = OnPrimaryColor,
    primaryContainer = PrimaryColor.copy(alpha = 0.1f),
    onPrimaryContainer = PrimaryColor,
    secondary = SecondaryColor,
    onSecondary = OnSecondaryColor,
    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextPrimaryDark.copy(alpha = 0.7f),
    error = ErrorColor,
    onError = Color.White,
    errorContainer = ErrorColor.copy(alpha = 0.1f),
    onErrorContainer = ErrorColor
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = OnPrimaryColor,
    primaryContainer = PrimaryColor.copy(alpha = 0.1f),
    onPrimaryContainer = PrimaryColor,
    secondary = SecondaryColor,
    onSecondary = OnSecondaryColor,
    background = BackgroundLight,
    surface = SurfaceLight,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextPrimaryLight.copy(alpha = 0.7f),
    error = ErrorColor,
    onError = Color.White,
    errorContainer = ErrorColor.copy(alpha = 0.1f),
    onErrorContainer = ErrorColor
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