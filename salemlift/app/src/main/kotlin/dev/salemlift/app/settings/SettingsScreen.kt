package dev.salemlift.app.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.salemlift.app.common.displayName
import dev.salemlift.app.common.formatCountdown
import dev.salemlift.domain.model.Landmarks
import dev.salemlift.domain.model.Muscle

/** Plain Material 3 settings screen; restyling is a later phase's concern. */
@Composable
fun SettingsScreen(
    state: SettingsViewModel.UiState,
    appVersion: String,
    snackbarHostState: SnackbarHostState,
    actions: SettingsActions,
) {
    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item(key = "header") { SettingsHeader(onBack = actions.onBack) }
            item(key = "landmarks-title") { SectionTitle("Landmarks") }
            items(state.landmarks, key = { "landmark-${it.first.name}" }) { (muscle, landmarks) ->
                LandmarkRow(muscle = muscle, landmarks = landmarks, onClick = { actions.onOpenLandmark(muscle) })
            }
            item(key = "reseed") { ReseedRow(onApplyExperience = actions.onApplyExperience) }
            item(key = "rules-title") { SectionTitle("Autoregulation rules") }
            items(state.rules, key = { "rule-${it.id}" }) { rule ->
                RuleRow(rule = rule, onAdjust = actions.onAdjustRuleDelta)
            }
            item(key = "reset-rules") { ResetRulesRow(onResetRules = actions.onResetRules) }
            item(key = "rest-title") { SectionTitle("Rest timer") }
            item(key = "rest") { RestTimerRow(restSeconds = state.restSeconds, onAdjust = actions.onAdjustRestSeconds) }
            item(key = "backup-title") { SectionTitle("Backup") }
            item(key = "backup") { BackupSection(onExport = actions.onExport, onImport = actions.onImport) }
            item(key = "about-title") { SectionTitle("About") }
            item(key = "about") { AboutSection(appVersion = appVersion) }
        }
    }
    state.editor?.let { draft ->
        LandmarkEditorDialog(
            draft = draft,
            onAdjust = actions.onEditorAdjust,
            onSave = actions.onEditorSave,
            onDismiss = actions.onEditorDismiss,
        )
    }
    if (state.importPending) {
        ImportConfirmDialog(onConfirm = actions.onConfirmImport, onCancel = actions.onCancelImport)
    }
}

@Composable
private fun SettingsHeader(onBack: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) {
            Text("Back")
        }
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun LandmarkRow(
    muscle: Muscle,
    landmarks: Landmarks,
    onClick: () -> Unit,
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = muscle.displayName(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "MV ${landmarks.mv} · MEV ${landmarks.mev} · MAV ${landmarks.mav} · MRV ${landmarks.mrv}",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun RuleRow(
    rule: SettingsViewModel.RuleRow,
    onAdjust: (String, Int) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f).padding(vertical = 6.dp)) {
                Text(text = "${rule.id} · ${rule.deltaLabel}", style = MaterialTheme.typography.titleSmall)
                Text(text = rule.rationale, style = MaterialTheme.typography.bodySmall)
            }
            if (rule.isEditable) {
                Stepper(
                    onDecrement = { onAdjust(rule.id, -1) },
                    onIncrement = { onAdjust(rule.id, +1) },
                )
            }
        }
    }
}

@Composable
private fun RestTimerRow(
    restSeconds: Int,
    onAdjust: (Int) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f).padding(vertical = 6.dp)) {
                Text(
                    text = "Default rest ${formatCountdown(restSeconds * MILLIS_PER_SECOND)}",
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(text = "15 s steps, 30 s to 10 min", style = MaterialTheme.typography.bodySmall)
            }
            Stepper(onDecrement = { onAdjust(-1) }, onIncrement = { onAdjust(+1) })
        }
    }
}

/** Shared −/+ stepper with 48dp touch targets. */
@Composable
internal fun Stepper(
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onDecrement, modifier = Modifier.heightIn(min = 48.dp)) {
            Text("−", style = MaterialTheme.typography.titleLarge)
        }
        TextButton(onClick = onIncrement, modifier = Modifier.heightIn(min = 48.dp)) {
            Text("+", style = MaterialTheme.typography.titleLarge)
        }
    }
}

private const val MILLIS_PER_SECOND = 1_000L
