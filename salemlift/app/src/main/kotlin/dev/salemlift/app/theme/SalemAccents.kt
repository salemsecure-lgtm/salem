package dev.salemlift.app.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Badge accents that have no Material role: amber for "heads-up" states
 * (deload, caps, clamps) and blue for informational ones (swap suggestions).
 * All pairs meet WCAG AA in their mode.
 */
@Immutable
data class SalemAccents(
    val warningContainer: Color,
    val onWarningContainer: Color,
    val infoContainer: Color,
    val onInfoContainer: Color,
)

internal val DarkAccents =
    SalemAccents(
        warningContainer = AmberContainerDark,
        onWarningContainer = OnAmberContainerDark,
        infoContainer = BlueContainerDark,
        onInfoContainer = OnBlueContainerDark,
    )

internal val LightAccents =
    SalemAccents(
        warningContainer = AmberContainerLight,
        onWarningContainer = OnAmberContainerLight,
        infoContainer = BlueContainerLight,
        onInfoContainer = OnBlueContainerLight,
    )

internal val LocalSalemAccents = staticCompositionLocalOf { DarkAccents }

/** The active [SalemAccents]; only valid inside [SalemTheme]. */
@Composable
fun salemAccents(): SalemAccents = LocalSalemAccents.current
