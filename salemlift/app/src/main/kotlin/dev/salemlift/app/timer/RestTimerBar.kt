package dev.salemlift.app.timer

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.salemlift.app.common.formatCountdown

/** Persistent bottom bar shown while a rest countdown is live. */
@Composable
fun RestTimerBar(
    state: RestTimerViewModel.UiState,
    actions: TimerActions,
) {
    if (!state.isVisible) return
    Surface(tonalElevation = 3.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (state.isFinished) "Rest over" else formatCountdown(state.remainingMillis),
                style = MaterialTheme.typography.headlineMedium,
                color =
                    if (state.isFinished) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                modifier = Modifier.weight(1f),
            )
            TextButton(
                onClick = actions.onTogglePause,
                modifier = Modifier.heightIn(min = 48.dp),
                enabled = !state.isFinished,
            ) {
                Text(if (state.isPaused) "Resume" else "Pause")
            }
            TextButton(
                onClick = actions.onAddThirtySeconds,
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Text("+30s")
            }
        }
    }
}
