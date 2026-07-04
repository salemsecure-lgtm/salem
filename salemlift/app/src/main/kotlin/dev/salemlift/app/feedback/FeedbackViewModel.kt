package dev.salemlift.app.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.salemlift.app.di.CommitResultStore
import dev.salemlift.data.FeedbackDraft
import dev.salemlift.data.TrainingRepository
import dev.salemlift.domain.model.JointPain
import dev.salemlift.domain.model.Muscle
import dev.salemlift.domain.model.Performance
import dev.salemlift.domain.model.Pump
import dev.salemlift.domain.model.Soreness
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Per-muscle feedback capture + the commit step. Soreness is phrased as
 * recovery-since-last-time; performance defaults to SAME (engine-computed
 * performance arrives in a later phase) but stays user-overridable.
 */
class FeedbackViewModel(
    private val repository: TrainingRepository,
    private val commitStore: CommitResultStore,
    private val sessionId: Long,
) : ViewModel() {
    data class MuscleEntry(
        val muscle: Muscle,
        val soreness: Soreness? = null,
        val pump: Pump? = null,
        val jointPain: JointPain? = null,
        val performance: Performance = Performance.SAME,
    ) {
        val isComplete: Boolean get() = soreness != null && pump != null && jointPain != null

        fun toDraftOrNull(): FeedbackDraft? {
            val s = soreness ?: return null
            val p = pump ?: return null
            val j = jointPain ?: return null
            return FeedbackDraft(muscle = muscle, soreness = s, pump = p, jointPain = j, performance = performance)
        }
    }

    data class UiState(
        val isLoading: Boolean = true,
        val entries: List<MuscleEntry> = emptyList(),
        val isCommitting: Boolean = false,
    ) {
        val canCommit: Boolean
            get() = !isLoading && !isCommitting && entries.isNotEmpty() && entries.all { it.isComplete }
    }

    sealed interface Event {
        data class Committed(
            val sessionId: Long,
        ) : Event
    }

    private val mutableState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = mutableState

    private val mutableEvents =
        MutableSharedFlow<Event>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val events: SharedFlow<Event> = mutableEvents

    init {
        viewModelScope.launch {
            val session = checkNotNull(repository.sessionFor(sessionId)) { "unknown session $sessionId" }
            mutableState.update { state ->
                state.copy(
                    isLoading = false,
                    entries = session.muscleTargets.keys.map { MuscleEntry(muscle = it) },
                )
            }
        }
    }

    fun setSoreness(
        muscle: Muscle,
        value: Soreness,
    ) = updateEntry(muscle) { it.copy(soreness = value) }

    fun setPump(
        muscle: Muscle,
        value: Pump,
    ) = updateEntry(muscle) { it.copy(pump = value) }

    fun setJointPain(
        muscle: Muscle,
        value: JointPain,
    ) = updateEntry(muscle) { it.copy(jointPain = value) }

    fun setPerformance(
        muscle: Muscle,
        value: Performance,
    ) = updateEntry(muscle) { it.copy(performance = value) }

    /** Runs the engine via the repository's single-transaction commit. */
    fun commit(manualDeloadRequest: Boolean = false) {
        val state = mutableState.value
        if (!state.canCommit) return
        val drafts = state.entries.mapNotNull { it.toDraftOrNull() }
        if (drafts.size != state.entries.size) return
        mutableState.update { it.copy(isCommitting = true) }
        viewModelScope.launch {
            val outcome = repository.commitSession(sessionId, drafts, manualDeloadRequest)
            commitStore.store(sessionId, outcome)
            mutableEvents.emit(Event.Committed(sessionId))
        }
    }

    private fun updateEntry(
        muscle: Muscle,
        transform: (MuscleEntry) -> MuscleEntry,
    ) {
        mutableState.update { state ->
            state.copy(entries = state.entries.map { if (it.muscle == muscle) transform(it) else it })
        }
    }
}
