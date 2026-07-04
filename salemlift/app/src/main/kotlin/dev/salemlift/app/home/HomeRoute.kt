package dev.salemlift.app.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.salemlift.app.di.AppContainer

@Composable
fun HomeRoute(
    container: AppContainer,
    onOpenRunner: (Long) -> Unit,
    onOpenAnalytics: () -> Unit,
) {
    val viewModel: HomeViewModel = viewModel { HomeViewModel(container.repository) }
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeViewModel.Event.NavigateToRunner -> onOpenRunner(event.sessionId)
            }
        }
    }
    HomeScreen(
        state = state,
        onStartMesocycle = viewModel::startMesocycle,
        onStartSession = viewModel::startSession,
        onOpenAnalytics = onOpenAnalytics,
    )
}
