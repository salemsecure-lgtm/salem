package com.salem.worldcup2026.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Brand palette — World Cup 2026 (USA / Canada / Mexico)
val WCNavy = Color(0xFF0B1020)
val WCNavyAlt = Color(0xFF131A33)
val WCSurface = Color(0xFF1A2140)
val WCGold = Color(0xFFFFC94D)
val WCGreen = Color(0xFF2DD4A7)
val WCMagenta = Color(0xFFE8417A)
val WCBlue = Color(0xFF4D7CFE)
val WCRed = Color(0xFFFF4D5E)
val WCTextDim = Color(0xFF9AA3C2)

private val DarkColors = darkColorScheme(
    primary = WCGold,
    onPrimary = WCNavy,
    secondary = WCGreen,
    onSecondary = WCNavy,
    tertiary = WCMagenta,
    background = WCNavy,
    onBackground = Color.White,
    surface = WCSurface,
    onSurface = Color.White,
    surfaceVariant = WCNavyAlt,
    onSurfaceVariant = WCTextDim,
    error = WCRed
)

private val LightColors = lightColorScheme(
    primary = WCBlue,
    secondary = WCGreen,
    tertiary = WCMagenta
)

val HeroGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF1B2350), Color(0xFF2A1746), Color(0xFF0B1020))
)

val LiveGradient = Brush.horizontalGradient(
    colors = listOf(WCMagenta, WCRed)
)

@Composable
fun WorldCupTheme(content: @Composable () -> Unit) {
    // App is designed dark-first for the "stadium at night" look.
    MaterialTheme(
        colorScheme = DarkColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
