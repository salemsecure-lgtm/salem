package dev.salemlift.app.runner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.salemlift.app.common.displayName
import dev.salemlift.app.common.formatWeight
import dev.salemlift.data.LoggedSet
import dev.salemlift.domain.model.Muscle

@Composable
fun MuscleBlockCard(
    block: SessionRunnerViewModel.MuscleBlock,
    actions: RunnerActions,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            BlockHeader(block = block, onToggle = { actions.onToggleExpanded(block.muscle) })
            if (block.isExpanded) {
                ExercisePickerButton(block = block, onPickExercise = actions.onPickExercise)
                block.sets.forEachIndexed { index, set ->
                    LoggedSetRow(index = index, set = set, onDelete = actions.onDeleteSet)
                }
                SetInputPanel(block = block, actions = actions)
            }
        }
    }
}

@Composable
private fun BlockHeader(
    block: SessionRunnerViewModel.MuscleBlock,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = block.muscle.displayName(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "${block.sets.size}/${block.targetSets} sets",
            style = MaterialTheme.typography.labelLarge,
        )
        Text(
            text = if (block.isExpanded) "▲" else "▼",
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

@Composable
private fun ExercisePickerButton(
    block: SessionRunnerViewModel.MuscleBlock,
    onPickExercise: (Muscle) -> Unit,
) {
    OutlinedButton(
        onClick = { onPickExercise(block.muscle) },
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(top = 4.dp),
    ) {
        Text(text = block.exercise?.name ?: "Choose exercise")
    }
}

@Composable
private fun LoggedSetRow(
    index: Int,
    set: LoggedSet,
    onDelete: (Long) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${index + 1}.  ${formatWeight(set.weightKg)} kg × ${set.reps} @ RIR ${set.rir}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = { onDelete(set.id) }, modifier = Modifier.size(48.dp)) {
            Text(text = "✕", color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun SetInputPanel(
    block: SessionRunnerViewModel.MuscleBlock,
    actions: RunnerActions,
) {
    val muscle = block.muscle
    Column(modifier = Modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        WeightStepperRow(
            weightText = block.input.weightText,
            onText = { actions.onWeightText(muscle, it) },
            onAdjust = { actions.onAdjustWeight(muscle, it) },
        )
        StepperRow(
            label = "Reps",
            value = "${block.input.reps}",
            onDecrement = { actions.onAdjustReps(muscle, -1) },
            onIncrement = { actions.onAdjustReps(muscle, +1) },
        )
        StepperRow(
            label = "RIR",
            value = "${block.input.rir}",
            onDecrement = { actions.onAdjustRir(muscle, -1) },
            onIncrement = { actions.onAdjustRir(muscle, +1) },
        )
        Button(
            onClick = { actions.onLogSet(muscle) },
            enabled = block.exercise != null && block.input.weightText.toDoubleOrNull() != null,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        ) {
            Text(text = "LOG SET", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun WeightStepperRow(
    weightText: String,
    onText: (String) -> Unit,
    onAdjust: (Double) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = "Weight (kg)", modifier = Modifier.weight(1f))
        StepButton(label = "−", onClick = { onAdjust(-SessionRunnerViewModel.WEIGHT_STEP_KG) })
        OutlinedTextField(
            value = weightText,
            onValueChange = onText,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center),
            modifier = Modifier.width(96.dp).padding(horizontal = 4.dp),
        )
        StepButton(label = "+", onClick = { onAdjust(+SessionRunnerViewModel.WEIGHT_STEP_KG) })
    }
}

@Composable
private fun StepperRow(
    label: String,
    value: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, modifier = Modifier.weight(1f))
        StepButton(label = "−", onClick = onDecrement)
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(64.dp),
        )
        StepButton(label = "+", onClick = onIncrement)
    }
}

@Composable
private fun StepButton(
    label: String,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        contentPadding = PaddingValues(0.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.titleLarge)
    }
}
