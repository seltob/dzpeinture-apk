package com.dzpeinture.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DzNavy = Color(0xFF0E2A47)
val DzEmerald = Color(0xFF107E57)
val DzGold = Color(0xFFC9A227)
val DzSurface = Color(0xFFF6F7F9)
val DzCard = Color(0xFFFFFFFF)

private val DzColorScheme = lightColorScheme(
    primary = DzNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE8F5),
    onPrimaryContainer = DzNavy,
    secondary = DzEmerald,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7F2E7),
    onSecondaryContainer = Color(0xFF073B28),
    tertiary = DzGold,
    onTertiary = Color.White,
    background = DzSurface,
    onBackground = Color(0xFF111827),
    surface = DzCard,
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFE9EEF3),
    onSurfaceVariant = Color(0xFF4B5563)
)

@Composable
fun DzPeintureTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DzColorScheme,
        content = content
    )
}
