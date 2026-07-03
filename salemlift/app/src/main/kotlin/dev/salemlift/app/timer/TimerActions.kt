package dev.salemlift.app.timer

/** Rest-timer bar callbacks bundled to keep signatures small. */
data class TimerActions(
    val onTogglePause: () -> Unit,
    val onAddThirtySeconds: () -> Unit,
)
