package dev.salemlift.app.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.salemlift.app.common.displayName
import dev.salemlift.data.SessionState
import dev.salemlift.data.SessionSummary
import dev.salemlift.domain.model.Split

@Composable
fun HomeScreen(
    state: HomeViewModel.UiState,
    onStartMesocycle: (Split) -> Unit,
    onStartSession: (SessionSummary) -> Unit,
    onOpenAnalytics: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onOpenAnalytics, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Analytics")
            }
            TextButton(onClick = onOpenSettings, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Settings")
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            when (state) {
                HomeViewModel.UiState.Loading -> LoadingBox()
                is HomeViewModel.UiState.NoActiveMeso ->
                    SplitPicker(
                        splits = state.splits,
                        isStarting = state.isStarting,
                        onStartMesocycle = onStartMesocycle,
                    )
                is HomeViewModel.UiState.Today ->
                    TodayCard(session = state.session, onStartSession = onStartSession)
            }
        }
    }
}

@Composable
private fun LoadingBox() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun SplitPicker(
    splits: List<Split>,
    isStarting: Boolean,
    onStartMesocycle: (Split) -> Unit,
) {
    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Pick a split", style = MaterialTheme.typography.headlineSmall)
        LazyColumn(
            modifier = Modifier.weight(1f).padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(splits.size) { index ->
                SplitRow(
                    split = splits[index],
                    selected = index == selectedIndex,
                    onSelect = { selectedIndex = index },
                )
            }
        }
        Button(
            onClick = { splits.getOrNull(selectedIndex)?.let(onStartMesocycle) },
            enabled = !isStarting,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        ) {
            Text(if (isStarting) "Starting…" else "Start mesocycle")
        }
    }
}

@Composable
private fun SplitRow(
    split: Split,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Card(onClick = onSelect, modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = selected, onClick = onSelect)
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(text = split.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${split.sessions.size} sessions / week",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TodayCard(
    session: SessionSummary,
    onStartSession: (SessionSummary) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Week ${session.week}", style = MaterialTheme.typography.titleMedium)
        Text(text = session.name, style = MaterialTheme.typography.headlineMedium)
        if (session.isDeload) {
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text(text = "DELOAD", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
        Text(
            text = "Target RIR ${rirLabel(session)}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
        FlowRow(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            session.muscleTargets.forEach { (muscle, sets) -> TargetChip(label = "${muscle.displayName()} · $sets") }
        }
        Button(
            onClick = { onStartSession(session) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        ) {
            Text(if (session.state == SessionState.IN_PROGRESS) "Resume session" else "Start session")
        }
    }
}

@Composable
private fun TargetChip(label: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

private fun rirLabel(session: SessionSummary): String =
    if (session.effort.maxRir > session.effort.targetRir) {
        "${session.effort.targetRir}–${session.effort.maxRir}"
    } else {
        "${session.effort.targetRir}"
    }
