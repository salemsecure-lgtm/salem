package dev.salemlift.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val DarkScheme =
    darkColorScheme(
        primary = Ember80,
        onPrimary = Ember20,
        primaryContainer = Ember30,
        onPrimaryContainer = Ember90,
        inversePrimary = Ember40,
        secondary = Sand80,
        onSecondary = Sand20,
        secondaryContainer = Sand30,
        onSecondaryContainer = Sand90,
        tertiary = Mint80,
        onTertiary = Mint20,
        tertiaryContainer = Mint30,
        onTertiaryContainer = Mint90,
        error = Error80,
        onError = Error20,
        errorContainer = Error30,
        onErrorContainer = Error90,
        background = Charcoal,
        onBackground = Parchment,
        surface = Charcoal,
        onSurface = Parchment,
        surfaceVariant = VariantDark,
        onSurfaceVariant = OnVariantDark,
        surfaceTint = Ember80,
        inverseSurface = Parchment,
        inverseOnSurface = CharcoalHighest,
        outline = OutlineDark,
        outlineVariant = VariantDark,
        scrim = Color.Black,
        surfaceContainerLowest = CharcoalLowest,
        surfaceContainerLow = CharcoalLow,
        surfaceContainer = CharcoalMid,
        surfaceContainerHigh = CharcoalHigh,
        surfaceContainerHighest = CharcoalHighest,
        surfaceBright = CharcoalHighest,
        surfaceDim = Charcoal,
    )

private val LightScheme =
    lightColorScheme(
        primary = Ember40,
        onPrimary = Color.White,
        primaryContainer = Ember90,
        onPrimaryContainer = Ember10,
        inversePrimary = Ember80,
        secondary = Sand40,
        onSecondary = Color.White,
        secondaryContainer = Sand90,
        onSecondaryContainer = Sand10,
        tertiary = Mint40,
        onTertiary = Color.White,
        tertiaryContainer = Mint90,
        onTertiaryContainer = Mint10,
        error = Error40,
        onError = Color.White,
        errorContainer = Error90,
        onErrorContainer = Error10,
        background = Linen,
        onBackground = Ink,
        surface = Linen,
        onSurface = Ink,
        surfaceVariant = LinenMid,
        onSurfaceVariant = VariantDark,
        surfaceTint = Ember40,
        inverseSurface = CharcoalHighest,
        inverseOnSurface = Linen,
        outline = OutlineLight,
        outlineVariant = OutlineVariantLight,
        scrim = Color.Black,
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = LinenLow,
        surfaceContainer = LinenMid,
        surfaceContainerHigh = LinenHigh,
        surfaceContainerHighest = LinenHighest,
        surfaceBright = Linen,
        surfaceDim = LinenHighest,
    )

/**
 * Salem Lift's Material 3 theme: original ember-on-charcoal brand palette,
 * dynamic color deliberately OFF so the brand look is stable on every device.
 */
@Composable
fun SalemTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalSalemAccents provides if (darkTheme) DarkAccents else LightAccents) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            typography = SalemTypography,
            content = content,
        )
    }
}
