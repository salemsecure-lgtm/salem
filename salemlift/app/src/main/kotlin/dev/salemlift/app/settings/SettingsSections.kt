package dev.salemlift.app.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.salemlift.domain.model.Experience

/** "Re-seed for experience" entry: overwrites all landmarks, so it confirms first. */
@Composable
internal fun ReseedRow(onApplyExperience: (Experience) -> Unit) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    TextButton(
        onClick = { showDialog = true },
        modifier = Modifier.heightIn(min = 48.dp),
    ) {
        Text("Re-seed for experience…")
    }
    if (showDialog) {
        ReseedDialog(
            onApply = { experience ->
                showDialog = false
                onApplyExperience(experience)
            },
            onDismiss = { showDialog = false },
        )
    }
}

/** "Reset to defaults" entry for the rule table, with confirmation. */
@Composable
internal fun ResetRulesRow(onResetRules: () -> Unit) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    TextButton(
        onClick = { showDialog = true },
        modifier = Modifier.heightIn(min = 48.dp),
    ) {
        Text("Reset to defaults")
    }
    if (showDialog) {
        ResetRulesDialog(
            onConfirm = {
                showDialog = false
                onResetRules()
            },
            onDismiss = { showDialog = false },
        )
    }
}

@Composable
internal fun BackupSection(
    onExport: () -> Unit,
    onImport: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                text = "Backups are a single JSON file with all your training data and custom exercises.",
                style = MaterialTheme.typography.bodySmall,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onExport, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("Export backup")
                }
                TextButton(onClick = onImport, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("Import backup")
                }
            }
            Text(
                text = "Importing replaces everything currently stored on this device.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
internal fun AboutSection(appVersion: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(text = "Salem Lift $appVersion", style = MaterialTheme.typography.titleSmall)
            Text(
                text =
                    "Offline-only: the app requests no network permission and never sends " +
                        "data anywhere. Everything lives on this device.",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text =
                    "Exercise catalog: free-exercise-db, an open dataset released under " +
                        "The Unlicense (public domain). See exercises-LICENSE.md in the app assets.",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text =
                    "Salem Lift is a personal training tool, not medical advice. " +
                        "Consult a professional for pain or injuries.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
