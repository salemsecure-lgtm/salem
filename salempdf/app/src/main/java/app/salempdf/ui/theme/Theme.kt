package app.salempdf.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Brand (SPEC §Brand): indigo primary, amber highlight accent. Distinct from
// Adobe red and coral PDF-app palettes by design.
val Indigo = Color(0xFF4F46E5)
val IndigoDim = Color(0xFFBDB9FF)
val Violet = Color(0xFF7C3AED)
val Amber = Color(0xFFFBBF24)
val AmberOn = Color(0xFF3F2E00)

private val LightColors =
    lightColorScheme(
        primary = Indigo,
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE2E0FF),
        onPrimaryContainer = Color(0xFF12006E),
        secondary = Violet,
        onSecondary = Color.White,
        tertiary = Amber,
        onTertiary = AmberOn,
        tertiaryContainer = Color(0xFFFFEFC3),
        onTertiaryContainer = Color(0xFF261A00),
    )

private val DarkColors =
    darkColorScheme(
        primary = IndigoDim,
        onPrimary = Color(0xFF21158F),
        primaryContainer = Color(0xFF3730C4),
        onPrimaryContainer = Color(0xFFE2E0FF),
        secondary = Color(0xFFCFBCFF),
        onSecondary = Color(0xFF41008B),
        tertiary = Amber,
        onTertiary = AmberOn,
        tertiaryContainer = Color(0xFF5C4300),
        onTertiaryContainer = Color(0xFFFFEFC3),
    )

@Composable
fun SalemPdfTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
