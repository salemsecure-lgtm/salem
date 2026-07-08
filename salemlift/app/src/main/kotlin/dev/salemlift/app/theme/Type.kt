package dev.salemlift.app.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Gym-glanceable type scale on the system font (no bundled fonts — keeps cold
 * start light). The display styles carry tabular figures ("tnum") so the rest
 * timer and set numerals never jitter as digits tick over.
 */
private const val TABULAR_FIGURES = "tnum"

internal val SalemTypography =
    Typography(
        displayLarge =
            TextStyle(
                fontSize = 57.sp,
                lineHeight = 64.sp,
                fontWeight = FontWeight.Bold,
                fontFeatureSettings = TABULAR_FIGURES,
            ),
        displayMedium =
            TextStyle(
                fontSize = 45.sp,
                lineHeight = 52.sp,
                fontWeight = FontWeight.Bold,
                fontFeatureSettings = TABULAR_FIGURES,
            ),
        displaySmall =
            TextStyle(
                fontSize = 36.sp,
                lineHeight = 44.sp,
                fontWeight = FontWeight.Bold,
                fontFeatureSettings = TABULAR_FIGURES,
            ),
        headlineMedium =
            TextStyle(
                fontSize = 28.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        headlineSmall =
            TextStyle(
                fontSize = 24.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        titleLarge =
            TextStyle(
                fontSize = 22.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        titleMedium =
            TextStyle(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.15.sp,
            ),
        labelLarge =
            TextStyle(
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.1.sp,
                fontFeatureSettings = TABULAR_FIGURES,
            ),
    )
