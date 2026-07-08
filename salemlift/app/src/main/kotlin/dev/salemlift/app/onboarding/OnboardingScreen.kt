package dev.salemlift.app.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    state: OnboardingViewModel.UiState,
    actions: OnboardingActions,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stepTitle(state.step)) },
                navigationIcon = {
                    IconButton(onClick = actions.onBack, modifier = Modifier.heightIn(min = 48.dp)) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
        ) {
            when (state.step) {
                OnboardingViewModel.Step.WELCOME -> WelcomeStep(onNext = actions.onNext)
                OnboardingViewModel.Step.EXPERIENCE -> ExperienceStep(state, actions.onChooseExperience)
                OnboardingViewModel.Step.SCHEDULE -> ScheduleStep(state, actions)
                OnboardingViewModel.Step.REVIEW -> ReviewStep(state, actions.onStart)
            }
        }
    }
}

private fun stepTitle(step: OnboardingViewModel.Step): String =
    when (step) {
        OnboardingViewModel.Step.WELCOME -> "Welcome"
        OnboardingViewModel.Step.EXPERIENCE -> "Your training age"
        OnboardingViewModel.Step.SCHEDULE -> "Your week"
        OnboardingViewModel.Step.REVIEW -> "Ready to lift"
    }

/** Full-width ≥56dp primary action shared by the steps. */
@Composable
internal fun StepPrimaryButton(
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .heightIn(min = 56.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.titleMedium)
    }
}
