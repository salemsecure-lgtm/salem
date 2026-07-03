package dev.salemlift.app.feedback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.salemlift.app.di.AppContainer

@Composable
fun FeedbackRoute(
    container: AppContainer,
    sessionId: Long,
    onCommitted: (Long) -> Unit,
) {
    val viewModel: FeedbackViewModel =
        viewModel(key = "feedback-$sessionId") {
            FeedbackViewModel(
                repository = container.repository,
                commitStore = container.commitResultStore,
                sessionId = sessionId,
            )
        }
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is FeedbackViewModel.Event.Committed -> onCommitted(event.sessionId)
            }
        }
    }
    FeedbackScreen(
        state = state,
        actions =
            FeedbackActions(
                onSoreness = viewModel::setSoreness,
                onPump = viewModel::setPump,
                onJointPain = viewModel::setJointPain,
                onPerformance = viewModel::setPerformance,
                onCommit = { viewModel.commit() },
            ),
    )
}
