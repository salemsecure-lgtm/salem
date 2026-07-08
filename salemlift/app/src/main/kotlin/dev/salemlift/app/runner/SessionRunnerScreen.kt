package dev.salemlift.app.runner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.salemlift.app.theme.salemAccents
import dev.salemlift.app.timer.RestTimerBar
import dev.salemlift.app.timer.RestTimerViewModel
import dev.salemlift.app.timer.TimerActions
import dev.salemlift.data.SessionSummary

@Composable
fun SessionRunnerScreen(
    state: SessionRunnerViewModel.UiState,
    timerState: RestTimerViewModel.UiState,
    actions: RunnerActions,
    timerActions: TimerActions,
) {
    Scaffold(
        bottomBar = { RestTimerBar(state = timerState, actions = timerActions) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { RunnerHeader(session = state.session) }
            items(state.blocks, key = { it.muscle.name }) { block ->
                MuscleBlockCard(block = block, actions = actions)
            }
            item {
                Button(
                    onClick = actions.onFinish,
                    enabled = state.canFinish,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                ) {
                    Text("Finish session")
                }
            }
        }
    }
}

@Composable
private fun RunnerHeader(session: SessionSummary?) {
    Column {
        Text(
            text = session?.name.orEmpty(),
            style = MaterialTheme.typography.headlineSmall,
        )
        session?.let {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Week ${it.week} · target RIR ${it.effort.targetRir}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (it.isDeload) {
                    DeloadBadge(modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

/** Amber heads-up badge matching the Home screen's deload marker. */
@Composable
private fun DeloadBadge(modifier: Modifier = Modifier) {
    val accents = salemAccents()
    Surface(
        color = accents.warningContainer,
        contentColor = accents.onWarningContainer,
        shape = MaterialTheme.shapes.small,
        modifier = modifier,
    ) {
        Text(
            text = "Deload",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}
