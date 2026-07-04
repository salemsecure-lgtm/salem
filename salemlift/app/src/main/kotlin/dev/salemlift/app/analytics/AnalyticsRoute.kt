package dev.salemlift.app.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.salemlift.app.di.AppContainer

@Composable
fun AnalyticsRoute(
    container: AppContainer,
    onBack: () -> Unit,
) {
    val viewModel: AnalyticsViewModel = viewModel { AnalyticsViewModel(container.analyticsRepository) }
    val state by viewModel.uiState.collectAsState()
    AnalyticsScreen(
        state = state,
        onBack = onBack,
        onSelectMuscle = viewModel::selectMuscle,
        onSelectExercise = viewModel::selectExercise,
    )
}
