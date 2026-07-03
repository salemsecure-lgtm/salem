package dev.salemlift.app.picker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.salemlift.app.common.displayName
import dev.salemlift.data.db.ExerciseEntity
import dev.salemlift.domain.model.Muscle

/**
 * Exercise picker for one muscle block: defaults to the muscle's primary
 * movers, and switches to a literal name search while the query is non-blank.
 */
@Composable
fun ExercisePickerDialog(
    muscle: Muscle,
    viewModel: ExercisePickerViewModel,
    onSelect: (ExerciseEntity) -> Unit,
    onDismiss: () -> Unit,
) {
    val exercises by viewModel.exercises.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Exercise for ${muscle.displayName()}",
                    style = MaterialTheme.typography.titleMedium,
                )
                OutlinedTextField(
                    value = query,
                    onValueChange = viewModel::setQuery,
                    singleLine = true,
                    placeholder = { Text("Search all exercises") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                )
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(exercises, key = { it.id }) { exercise ->
                        ExerciseRow(exercise = exercise, onSelect = onSelect)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseRow(
    exercise: ExerciseEntity,
    onSelect: (ExerciseEntity) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clickable { onSelect(exercise) }
                .padding(vertical = 8.dp),
    ) {
        Text(text = exercise.name, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = "${exercise.primaryMuscle.displayName()} · ${exercise.equipment.name.lowercase().replace('_', ' ')}",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
