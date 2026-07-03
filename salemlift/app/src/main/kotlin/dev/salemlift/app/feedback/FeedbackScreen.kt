package dev.salemlift.app.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.salemlift.app.common.displayName
import dev.salemlift.domain.model.JointPain
import dev.salemlift.domain.model.Performance
import dev.salemlift.domain.model.Pump
import dev.salemlift.domain.model.Soreness

@Composable
fun FeedbackScreen(
    state: FeedbackViewModel.UiState,
    actions: FeedbackActions,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(text = "How did it go?", style = MaterialTheme.typography.headlineSmall)
            }
            items(state.entries, key = { it.muscle.name }) { entry ->
                MuscleFeedbackCard(entry = entry, actions = actions)
            }
        }
        Button(
            onClick = actions.onCommit,
            enabled = state.canCommit,
            modifier = Modifier.fillMaxWidth().padding(16.dp).heightIn(min = 56.dp),
        ) {
            Text(if (state.isCommitting) "Committing…" else "Commit session")
        }
    }
}

@Composable
private fun MuscleFeedbackCard(
    entry: FeedbackViewModel.MuscleEntry,
    actions: FeedbackActions,
) {
    val muscle = entry.muscle
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = muscle.displayName(), style = MaterialTheme.typography.titleMedium)
            OptionGroup(
                title = "Recovery since last time",
                options = Soreness.entries.map { it to sorenessLabel(it) },
                selected = entry.soreness,
                onSelect = { actions.onSoreness(muscle, it) },
            )
            OptionGroup(
                title = "Pump (last exercise)",
                options = Pump.entries.map { it to pumpLabel(it) },
                selected = entry.pump,
                onSelect = { actions.onPump(muscle, it) },
            )
            OptionGroup(
                title = "Joint pain",
                options = JointPain.entries.map { it to jointPainLabel(it) },
                selected = entry.jointPain,
                onSelect = { actions.onJointPain(muscle, it) },
            )
            OptionGroup(
                title = "Performance vs last time",
                options = Performance.entries.map { it to performanceLabel(it) },
                selected = entry.performance,
                onSelect = { actions.onPerformance(muscle, it) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> OptionGroup(
    title: String,
    options: List<Pair<T, String>>,
    selected: T?,
    onSelect: (T) -> Unit,
) {
    Column {
        Text(text = title, style = MaterialTheme.typography.labelLarge)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            options.forEach { (value, label) ->
                OptionChip(
                    label = label,
                    isSelected = value == selected,
                    onClick = { onSelect(value) },
                )
            }
        }
    }
}

@Composable
private fun OptionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    if (isSelected) {
        Button(onClick = onClick, modifier = Modifier.heightIn(min = 48.dp)) { Text(label) }
    } else {
        OutlinedButton(onClick = onClick, modifier = Modifier.heightIn(min = 48.dp)) { Text(label) }
    }
}

private fun sorenessLabel(value: Soreness): String =
    when (value) {
        Soreness.NEVER_SORE -> "Never got sore"
        Soreness.RECOVERED_EARLY -> "Recovered early"
        Soreness.RECOVERED_ON_TIME -> "Recovered on time"
        Soreness.STILL_SORE -> "Still sore"
    }

private fun pumpLabel(value: Pump): String =
    when (value) {
        Pump.LOW -> "Low"
        Pump.MODERATE -> "Moderate"
        Pump.HIGH -> "High"
    }

private fun jointPainLabel(value: JointPain): String =
    when (value) {
        JointPain.NONE -> "None"
        JointPain.MILD -> "Mild"
        JointPain.SIGNIFICANT -> "Significant"
    }

private fun performanceLabel(value: Performance): String =
    when (value) {
        Performance.UP -> "Up"
        Performance.SAME -> "Same"
        Performance.DOWN -> "Down"
    }
