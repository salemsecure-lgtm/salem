package dev.salemlift.app.analytics

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Color-blind-safe series colors (validated palette: worst adjacent CVD dE 41+
 * in both modes), stepped separately for light and dark surfaces. Identity is
 * never color-alone: every chart ships a text legend and a value list.
 */
@Immutable
data class ChartPalette(
    /** Series 1 (blue): performed sets, e1RM, tonnage. */
    val primary: Color,
    /** Series 2 (aqua): the prescription overlay. */
    val secondary: Color,
)

/** Palette keyed off the active Material scheme, so forced dark mode is honored. */
@Composable
fun chartPalette(): ChartPalette =
    if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) {
        ChartPalette(primary = Color(0xFF3987E5), secondary = Color(0xFF199E70))
    } else {
        ChartPalette(primary = Color(0xFF2A78D6), secondary = Color(0xFF1BAF7A))
    }
