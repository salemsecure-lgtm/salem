package dev.salemlift.app.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.salemlift.app.di.AppContainer

@Composable
fun OnboardingRoute(
    container: AppContainer,
    onDone: () -> Unit,
    onExit: () -> Unit,
) {
    val viewModel: OnboardingViewModel =
        viewModel {
            OnboardingViewModel(
                trainingRepository = container.repository,
                settingsRepository = container.settingsRepository,
            )
        }
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                OnboardingViewModel.Event.Started -> onDone()
            }
        }
    }
    OnboardingScreen(
        state = state,
        actions =
            OnboardingActions(
                onBack = {
                    if (state.step == OnboardingViewModel.Step.WELCOME) onExit() else viewModel.back()
                },
                onNext = viewModel::next,
                onChooseExperience = viewModel::chooseExperience,
                onChooseDays = viewModel::chooseDays,
                onOverrideSplit = viewModel::overrideSplit,
                onStart = viewModel::start,
            ),
    )
}
