package dev.salemlift.app.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.salemlift.data.analytics.MesoProgress
import dev.salemlift.data.analytics.RuleFireCount
import dev.salemlift.data.analytics.WeekFatigue

/** Cycle tab: mesocycle progress card + the fatigue dashboard. */
@Composable
internal fun CycleTab(cycle: AnalyticsViewModel.CycleTabState) {
    val progress = cycle.progress
    if (progress == null) {
        EmptyState(
            title = "No active mesocycle",
            body =
                "Start a mesocycle from the home screen — cycle progress and " +
                    "the fatigue dashboard will show up here.",
        )
        return
    }
    ProgressCard(progress)
    if (cycle.weekFatigue.isEmpty() && cycle.ruleFires.isEmpty()) {
        Text(
            text = "Commit a session with feedback to populate the fatigue dashboard.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    if (cycle.weekFatigue.isNotEmpty()) {
        Text(text = "Fatigue signals per week", style = MaterialTheme.typography.titleSmall)
        FatigueTable(cycle.weekFatigue)
    }
    if (cycle.ruleFires.isNotEmpty()) {
        Text(text = "Autoregulation rules fired", style = MaterialTheme.typography.titleSmall)
        RuleHistogram(cycle.ruleFires)
    }
}

@Composable
private fun ProgressCard(progress: MesoProgress) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Week ${progress.currentWeek} of ${progress.totalWeeks}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                if (progress.isDeloadWeek) {
                    DeloadBadge()
                }
            }
            Text(text = "Target RIR ${rirLabel(progress)}", style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "Sessions completed: ${progress.completedSessions} of ${progress.totalSessions}",
                style = MaterialTheme.typography.bodyMedium,
            )
            LinearProgressIndicator(
                progress = { sessionFraction(progress) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun DeloadBadge() {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = "DELOAD",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun FatigueTable(weeks: List<WeekFatigue>) {
    FatigueRow(week = "Week", sore = "Sore", mild = "Mild pain", significant = "Sig. pain", down = "Perf down")
    weeks.forEach { row ->
        FatigueRow(
            week = "${row.week}",
            sore = "${row.stillSoreCount}",
            mild = "${row.mildJointPainCount}",
            significant = "${row.significantJointPainCount}",
            down = "${row.performanceDownCount}",
        )
    }
}

@Composable
private fun FatigueRow(
    week: String,
    sore: String,
    mild: String,
    significant: String,
    down: String,
) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        listOf(week, sore, mild, significant, down).forEach { cell ->
            Text(text = cell, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun RuleHistogram(ruleFires: List<RuleFireCount>) {
    val maxCount = ruleFires.maxOf { it.count }
    val palette = chartPalette()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ruleFires.forEach { rule ->
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = rule.ruleId,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth(fraction = rule.count.toFloat() / maxCount)
                                    .height(10.dp)
                                    .background(color = palette.primary, shape = RoundedCornerShape(4.dp)),
                        )
                    }
                    Text(
                        text = "${rule.count}",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                Text(
                    text = rule.rationale,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun sessionFraction(progress: MesoProgress): Float =
    if (progress.totalSessions == 0) 0f else progress.completedSessions.toFloat() / progress.totalSessions

private fun rirLabel(progress: MesoProgress): String =
    if (progress.maxRir > progress.targetRir) {
        "${progress.targetRir}–${progress.maxRir}"
    } else {
        "${progress.targetRir}"
    }
