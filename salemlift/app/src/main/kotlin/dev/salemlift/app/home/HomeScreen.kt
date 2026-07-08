package dev.salemlift.app.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.salemlift.app.common.displayName
import dev.salemlift.app.theme.salemAccents
import dev.salemlift.data.SessionState
import dev.salemlift.data.SessionSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeViewModel.UiState,
    onStartSession: (SessionSummary) -> Unit,
    onOpenAnalytics: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenOnboarding: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Salem Lift") },
                actions = {
                    TextButton(onClick = onOpenAnalytics, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text("Analytics")
                    }
                    TextButton(onClick = onOpenSettings, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text("Settings")
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                HomeViewModel.UiState.Loading -> LoadingBox()
                is HomeViewModel.UiState.NoActiveMeso -> SetupCard(onOpenOnboarding = onOpenOnboarding)
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

/** No active mesocycle: hand the new user to the guided setup flow. */
@Composable
private fun SetupCard(onOpenOnboarding: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(text = "No training plan yet", style = MaterialTheme.typography.headlineSmall)
                Text(
                    text =
                        "Pick your experience level and weekly schedule, and " +
                            "Salem Lift builds your first mesocycle — then " +
                            "adjusts it from every session you log.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Button(
                    onClick = onOpenOnboarding,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                ) {
                    Text(text = "Set up Salem Lift", style = MaterialTheme.typography.titleMedium)
                }
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
        Text(
            text = "Week ${session.week}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = session.name, style = MaterialTheme.typography.headlineMedium)
        if (session.isDeload) {
            DeloadBadge(modifier = Modifier.padding(top = 8.dp))
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
            Text(
                text = if (session.state == SessionState.IN_PROGRESS) "Resume session" else "Start session",
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

/** Amber heads-up badge for deload weeks (accent role, not an error). */
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
            text = "Deload week",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun TargetChip(label: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
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
