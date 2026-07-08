package dev.salemlift.app.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.salemlift.app.common.displayName
import dev.salemlift.domain.model.Experience
import dev.salemlift.domain.model.Split

@Composable
internal fun WelcomeStep(onNext: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = "Salem Lift", style = MaterialTheme.typography.headlineMedium)
        Text(
            text =
                "Salem Lift plans your hypertrophy training and adjusts it week " +
                    "by week from what you log — sets climb while you recover, " +
                    "back off when you don't, and every change is explained.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text =
                "Fully offline. No account, no network permission — everything " +
                    "stays on this device.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text =
                "A personal training tool, not medical advice. For pain or " +
                    "injuries, see a professional.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    StepPrimaryButton(label = "Get started", onClick = onNext)
}

@Composable
internal fun ExperienceStep(
    state: OnboardingViewModel.UiState,
    onChooseExperience: (Experience) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "How long have you been lifting? This sets your starting weekly volumes — all editable later.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Experience.entries.forEach { experience ->
            ExperienceCard(
                experience = experience,
                isSelected = experience == state.experience,
                onSelect = { onChooseExperience(experience) },
            )
        }
    }
}

@Composable
private fun ExperienceCard(
    experience: Experience,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    Card(onClick = onSelect, modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = isSelected, onClick = onSelect)
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(text = experienceTitle(experience), style = MaterialTheme.typography.titleMedium)
                Text(text = experienceBody(experience), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

private fun experienceTitle(experience: Experience): String =
    when (experience) {
        Experience.BEGINNER -> "New to lifting"
        Experience.INTERMEDIATE -> "Lifting for a while"
        Experience.ADVANCED -> "Seasoned lifter"
    }

private fun experienceBody(experience: Experience): String =
    when (experience) {
        Experience.BEGINNER -> "Under a year of steady training. You grow on less, so volumes start lower."
        Experience.INTERMEDIATE -> "One to three years of consistent training. Standard starting volumes."
        Experience.ADVANCED -> "Several years of hard training. Higher volume ceilings from week one."
    }

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ScheduleStep(
    state: OnboardingViewModel.UiState,
    actions: OnboardingActions,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = "How many days a week can you train?", style = MaterialTheme.typography.bodyLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OnboardingViewModel.DAYS_RANGE.forEach { days ->
                FilterChip(
                    selected = days == state.daysPerWeek,
                    onClick = { actions.onChooseDays(days) },
                    label = { Text("$days") },
                    modifier = Modifier.heightIn(min = 48.dp),
                )
            }
        }
        Text(
            text = if (state.isSplitOverridden) "Your pick" else "Suggested for ${state.daysPerWeek} days",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        state.splitOptions.forEach { split ->
            SplitCard(
                split = split,
                isSelected = split == state.split,
                onSelect = { actions.onOverrideSplit(split) },
            )
        }
    }
    StepPrimaryButton(label = "Continue", onClick = actions.onNext)
}

@Composable
private fun SplitCard(
    split: Split,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    Card(onClick = onSelect, modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = isSelected, onClick = onSelect)
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(text = split.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${split.sessions.size} sessions / week",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
internal fun ReviewStep(
    state: OnboardingViewModel.UiState,
    onStart: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SummaryRow(label = "Split", value = state.split.name)
        SummaryRow(label = "Experience", value = state.experience?.let(::experienceTitle) ?: "—")
        Text(
            text = "Starting weekly volumes",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        state.landmarkPreview.forEach { (muscle, landmarks) ->
            SummaryRow(
                label = muscle.displayName(),
                value = "MEV ${landmarks.mev} → MRV ${landmarks.mrv} sets",
            )
        }
        Text(
            text = "Week 1 starts at each muscle's MEV. Every landmark is editable in Settings.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    StepPrimaryButton(
        label = if (state.isStarting) "Starting…" else "Start first mesocycle",
        enabled = !state.isStarting,
        onClick = onStart,
    )
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}
