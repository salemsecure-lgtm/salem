package dev.salemlift.app.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import dev.salemlift.domain.model.Experience

/** MV/MEV/MAV/MRV steppers with live validation of the ordering invariant. */
@Composable
internal fun LandmarkEditorDialog(
    draft: SettingsViewModel.LandmarkDraft,
    onAdjust: (SettingsViewModel.LandmarkField, Int) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${draft.muscle.displayName()} landmarks") },
        text = {
            Column {
                LandmarkFieldRow("MV", draft.mv, SettingsViewModel.LandmarkField.MV, onAdjust)
                LandmarkFieldRow("MEV", draft.mev, SettingsViewModel.LandmarkField.MEV, onAdjust)
                LandmarkFieldRow("MAV", draft.mav, SettingsViewModel.LandmarkField.MAV, onAdjust)
                LandmarkFieldRow("MRV", draft.mrv, SettingsViewModel.LandmarkField.MRV, onAdjust)
                if (!draft.isValid) {
                    Text(
                        text = "Must keep MV ≤ MEV ≤ MAV ≤ MRV",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave, enabled = draft.isValid, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun LandmarkFieldRow(
    label: String,
    value: Int,
    field: SettingsViewModel.LandmarkField,
    onAdjust: (SettingsViewModel.LandmarkField, Int) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(text = "$value", style = MaterialTheme.typography.titleMedium)
        Stepper(onDecrement = { onAdjust(field, -1) }, onIncrement = { onAdjust(field, +1) })
    }
}

/** Experience choice + confirmation — re-seeding overwrites every landmark row. */
@Composable
internal fun ReseedDialog(
    onApply: (Experience) -> Unit,
    onDismiss: () -> Unit,
) {
    var selected by rememberSaveable { mutableStateOf(Experience.INTERMEDIATE) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Re-seed landmarks") },
        text = {
            Column {
                Experience.entries.forEach { experience ->
                    Row(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = experience == selected, onClick = { selected = experience })
                        Text(text = experience.name.lowercase().replaceFirstChar { it.titlecase() })
                    }
                }
                Text(
                    text = "This overwrites the landmarks of ALL muscles with the seeds for that experience.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onApply(selected) }, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Overwrite")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Cancel")
            }
        },
    )
}

@Composable
internal fun ResetRulesDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset rule deltas?") },
        text = { Text("All tuned deltas return to the shipped defaults (R1–R9).") },
        confirmButton = {
            TextButton(onClick = onConfirm, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Reset")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Cancel")
            }
        },
    )
}

/** Import is a destructive replace — always confirmed. */
@Composable
internal fun ImportConfirmDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Import backup?") },
        text = {
            Text(
                "Importing REPLACES all training data, settings, and custom exercises " +
                    "on this device with the backup's contents. This cannot be undone.",
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Replace everything")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Cancel")
            }
        },
    )
}
