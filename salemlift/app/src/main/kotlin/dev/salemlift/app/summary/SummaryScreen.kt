package dev.salemlift.app.summary

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.salemlift.app.common.displayName

@Composable
fun SummaryScreen(
    state: SummaryViewModel.UiState,
    onDone: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Text(text = "Next session, explained", style = MaterialTheme.typography.headlineSmall) }
            if (state.deloadTriggered) {
                item { DeloadBanner(reasons = state.deloadReasons) }
            }
            items(state.rows, key = { it.muscle.name }) { row -> DecisionCard(row = row) }
            state.nextSessionName?.let { next ->
                item {
                    Text(
                        text = "Up next: $next",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().padding(16.dp).heightIn(min = 56.dp),
        ) {
            Text("Done")
        }
    }
}

@Composable
private fun DeloadBanner(reasons: List<String>) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "Deload triggered", style = MaterialTheme.typography.titleMedium)
            reasons.forEach { Text(text = "• $it", style = MaterialTheme.typography.bodyMedium) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DecisionCard(row: SummaryViewModel.DecisionRow) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = row.muscle.displayName(), style = MaterialTheme.typography.titleMedium)
            Text(
                text = row.headline,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
            if (row.badges.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    row.badges.forEach { Badge(label = it) }
                }
            }
        }
    }
}

@Composable
private fun Badge(label: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
