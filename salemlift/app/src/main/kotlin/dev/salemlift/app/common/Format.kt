package dev.salemlift.app.common

import dev.salemlift.domain.model.Muscle

/** "FRONT_DELTS" → "Front Delts". */
fun Muscle.displayName(): String =
    name
        .split('_')
        .joinToString(" ") { part ->
            part.lowercase().replaceFirstChar { it.titlecase() }
        }

/** mm:ss countdown text, rounding up so the bar never shows 0:00 while time remains. */
fun formatCountdown(remainingMillis: Long): String {
    val totalSeconds = (remainingMillis + MILLIS_PER_SECOND - 1) / MILLIS_PER_SECOND
    val minutes = totalSeconds / SECONDS_PER_MINUTE
    val seconds = totalSeconds % SECONDS_PER_MINUTE
    return "$minutes:" + seconds.toString().padStart(2, '0')
}

/** Renders 62.5 as "62.5" and 60.0 as "60". */
fun formatWeight(weightKg: Double): String =
    if (weightKg % 1.0 == 0.0) weightKg.toLong().toString() else weightKg.toString()

private const val MILLIS_PER_SECOND = 1000L
private const val SECONDS_PER_MINUTE = 60L
