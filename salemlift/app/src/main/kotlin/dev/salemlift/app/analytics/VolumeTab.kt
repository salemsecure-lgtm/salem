package dev.salemlift.app.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.salemlift.app.common.displayName
import dev.salemlift.domain.model.Muscle

/** Volume tab: muscle chips, columns-vs-line chart with landmarks, week list. */
@Composable
internal fun VolumeTab(
    volume: AnalyticsViewModel.VolumeTabState,
    onSelectMuscle: (Muscle) -> Unit,
) {
    if (volume.isEmpty) {
        EmptyState(
            title = "No volume data yet",
            body =
                "Start a mesocycle and log working sets — weekly hard-set " +
                    "volume per muscle will show up here.",
        )
        return
    }
    MuscleChipRow(volume.muscles, volume.selected, onSelectMuscle)
    VolumeChart(weeks = volume.weeks, landmarks = volume.landmarks)
    val palette = chartPalette()
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        LegendEntry(color = palette.primary, label = "Performed sets")
        LegendEntry(color = palette.secondary, label = "Prescribed sets")
    }
    volume.landmarks?.let { marks ->
        Text(
            text = "Landmarks: MV ${marks.mv} · MEV ${marks.mev} · MAV ${marks.mav} · MRV ${marks.mrv} sets/week",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Text(text = "Performed / prescribed", style = MaterialTheme.typography.titleSmall)
    volume.weeks.forEach { week ->
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Week ${week.week}", modifier = Modifier.weight(1f))
            Text(text = "${week.performedSets} / ${week.prescribedSets} sets")
        }
    }
}

@Composable
private fun MuscleChipRow(
    muscles: List<Muscle>,
    selected: Muscle?,
    onSelectMuscle: (Muscle) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(muscles) { muscle ->
            FilterChip(
                selected = muscle == selected,
                onClick = { onSelectMuscle(muscle) },
                label = { Text(muscle.displayName()) },
                modifier = Modifier.heightIn(min = 48.dp),
            )
        }
    }
}

@Composable
private fun LegendEntry(
    color: Color,
    label: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).background(color = color, shape = CircleShape))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}
