package dev.salemlift.app.summary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.salemlift.app.di.AppContainer

@Composable
fun SummaryRoute(
    container: AppContainer,
    sessionId: Long,
    onDone: () -> Unit,
) {
    val viewModel: SummaryViewModel =
        viewModel(key = "summary-$sessionId") {
            SummaryViewModel(
                repository = container.repository,
                commitStore = container.commitResultStore,
                sessionId = sessionId,
            )
        }
    val state by viewModel.uiState.collectAsState()
    SummaryScreen(state = state, onDone = onDone)
}
