package dev.salemlift.app.analytics

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.salemlift.app.common.formatWeight
import dev.salemlift.data.analytics.ExerciseRef
import kotlin.math.roundToInt

/** Strength tab: exercise picker, e1RM trend line, weekly tonnage columns. */
@Composable
internal fun StrengthTab(
    strength: AnalyticsViewModel.StrengthTabState,
    onSelectExercise: (String) -> Unit,
) {
    if (strength.isEmpty) {
        EmptyState(
            title = "No strength data yet",
            body =
                "Log working sets and commit sessions — e1RM trends and " +
                    "weekly tonnage will show up here.",
        )
        return
    }
    strength.selected?.let { selected ->
        ExercisePicker(
            exercises = strength.exercises,
            selectedName = selected.name,
            onSelectExercise = onSelectExercise,
        )
        Text(text = "Estimated 1RM (RIR-adjusted Epley)", style = MaterialTheme.typography.titleSmall)
        if (strength.trend.isEmpty()) {
            Text(
                text = "No committed sessions for this exercise yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            E1rmChart(points = strength.trend)
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Best e1RM", modifier = Modifier.weight(1f))
                Text(text = "${formatKg(strength.trend.maxOf { it.e1rmKg })} kg")
            }
        }
    }
    if (strength.tonnage.isNotEmpty()) {
        Text(text = "Weekly tonnage", style = MaterialTheme.typography.titleSmall)
        TonnageChart(weeks = strength.tonnage)
        strength.tonnage.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Week ${week.week}", modifier = Modifier.weight(1f))
                Text(text = "${formatKg(week.tonnageKg)} kg")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExercisePicker(
    exercises: List<ExerciseRef>,
    selectedName: String,
    onSelectExercise: (String) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Exercise") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .menuAnchor(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            exercises.forEach { exercise ->
                DropdownMenuItem(
                    text = { Text(exercise.name) },
                    onClick = {
                        expanded = false
                        onSelectExercise(exercise.id)
                    },
                    modifier = Modifier.heightIn(min = 48.dp),
                )
            }
        }
    }
}

/** kg rounded to one decimal, trailing ".0" dropped. */
private fun formatKg(value: Double): String = formatWeight((value * 10).roundToInt() / 10.0)
