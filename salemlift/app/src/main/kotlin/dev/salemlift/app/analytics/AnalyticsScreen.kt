package dev.salemlift.app.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.salemlift.domain.model.Muscle

/** Analytics: Volume / Strength / Cycle tabs. Charts scroll vertically only. */
@Composable
fun AnalyticsScreen(
    state: AnalyticsViewModel.UiState,
    onBack: () -> Unit,
    onSelectMuscle: (Muscle) -> Unit,
    onSelectExercise: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Header(onBack)
        when (state) {
            AnalyticsViewModel.UiState.Loading ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            is AnalyticsViewModel.UiState.Ready ->
                ReadyContent(state, onSelectMuscle, onSelectExercise)
        }
    }
}

@Composable
private fun Header(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Text(text = "Analytics", style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
private fun ReadyContent(
    state: AnalyticsViewModel.UiState.Ready,
    onSelectMuscle: (Muscle) -> Unit,
    onSelectExercise: (String) -> Unit,
) {
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    TabRow(selectedTabIndex = tabIndex) {
        TAB_TITLES.forEachIndexed { index, title ->
            Tab(
                selected = tabIndex == index,
                onClick = { tabIndex = index },
                text = { Text(title) },
                modifier = Modifier.heightIn(min = 48.dp),
            )
        }
    }
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (tabIndex) {
            0 -> VolumeTab(state.volume, onSelectMuscle)
            1 -> StrengthTab(state.strength, onSelectExercise)
            else -> CycleTab(state.cycle)
        }
    }
}

/** Friendly no-data card shared by all three tabs. */
@Composable
internal fun EmptyState(
    title: String,
    body: String,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val TAB_TITLES = listOf("Volume", "Strength", "Cycle")
