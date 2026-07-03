package dev.salemlift.app.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.salemlift.data.SessionState
import dev.salemlift.data.SessionSummary
import dev.salemlift.data.TrainingRepository
import dev.salemlift.domain.model.MesoConfig
import dev.salemlift.domain.model.Split
import dev.salemlift.domain.program.SplitTemplates
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Home/Today: current session card, or the split picker when no meso is active. */
class HomeViewModel(
    private val repository: TrainingRepository,
) : ViewModel() {
    sealed interface UiState {
        data object Loading : UiState

        data class NoActiveMeso(
            val splits: List<Split>,
            val isStarting: Boolean,
        ) : UiState

        data class Today(
            val session: SessionSummary,
        ) : UiState
    }

    sealed interface Event {
        data class NavigateToRunner(
            val sessionId: Long,
        ) : Event
    }

    private val isStartingMeso = MutableStateFlow(false)

    val uiState: StateFlow<UiState> =
        combine(repository.currentSession(), isStartingMeso) { session, starting ->
            if (session != null) {
                UiState.Today(session)
            } else {
                UiState.NoActiveMeso(splits = SplitTemplates.all, isStarting = starting)
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Loading)

    private val mutableEvents =
        MutableSharedFlow<Event>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val events: SharedFlow<Event> = mutableEvents

    fun startMesocycle(split: Split) {
        if (isStartingMeso.value) return
        isStartingMeso.value = true
        viewModelScope.launch {
            repository.startMesocycle(split, MesoConfig())
            isStartingMeso.value = false
        }
    }

    /** Marks a pending session started, then asks the UI to open the runner. */
    fun startSession(session: SessionSummary) {
        viewModelScope.launch {
            if (session.state == SessionState.PENDING) {
                repository.markSessionStarted(session.sessionId)
            }
            mutableEvents.emit(Event.NavigateToRunner(session.sessionId))
        }
    }
}
