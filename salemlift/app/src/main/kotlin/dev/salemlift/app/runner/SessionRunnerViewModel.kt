package dev.salemlift.app.runner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.salemlift.app.common.formatWeight
import dev.salemlift.app.di.ExerciseNameResolver
import dev.salemlift.data.LoggedSet
import dev.salemlift.data.SessionSummary
import dev.salemlift.data.TrainingRepository
import dev.salemlift.domain.model.Muscle
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The live session runner: one expandable block per target muscle, set logging
 * (weight/reps/RIR), and delete-with-explicit-edit semantics. Every logged set
 * is persisted immediately via [TrainingRepository.logSet], so an in-progress
 * session survives process death and airplane mode (Gate 3).
 */
class SessionRunnerViewModel(
    private val repository: TrainingRepository,
    private val nameResolver: ExerciseNameResolver,
    private val sessionId: Long,
    private val nowEpochMillis: () -> Long = System::currentTimeMillis,
) : ViewModel() {
    data class SetInput(
        val weightText: String = "20",
        val reps: Int = 8,
        val rir: Int = 2,
    )

    data class ExerciseChoice(
        val id: String,
        val name: String,
    )

    data class MuscleBlock(
        val muscle: Muscle,
        val targetSets: Int,
        val exercise: ExerciseChoice?,
        val sets: List<LoggedSet>,
        val input: SetInput,
        val isExpanded: Boolean,
    )

    data class UiState(
        val session: SessionSummary? = null,
        val blocks: List<MuscleBlock> = emptyList(),
        val canFinish: Boolean = false,
    )

    sealed interface Event {
        /** A set was persisted — the UI (re)starts the rest timer. */
        data object SetLogged : Event
    }

    private val session = MutableStateFlow<SessionSummary?>(null)
    private val choices = MutableStateFlow<Map<Muscle, ExerciseChoice>>(emptyMap())
    private val inputs = MutableStateFlow<Map<Muscle, SetInput>>(emptyMap())
    private val expandedMuscles = MutableStateFlow<Set<Muscle>>(emptySet())

    private val mutableEvents =
        MutableSharedFlow<Event>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val events: SharedFlow<Event> = mutableEvents

    val uiState: StateFlow<UiState> =
        combine(
            session,
            repository.loggedSets(sessionId),
            choices,
            inputs,
            expandedMuscles,
        ) { current, sets, choiceMap, inputMap, expanded ->
            buildState(current, sets, choiceMap, inputMap, expanded)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState())

    init {
        viewModelScope.launch {
            // Load by id, not currentSession(): a stale back-stack entry must
            // never show the next session's targets under this session's id.
            val current = checkNotNull(repository.sessionFor(sessionId)) { "unknown session $sessionId" }
            session.value = current
            expandedMuscles.update { expanded ->
                if (expanded.isEmpty()) setOfNotNull(current.muscleTargets.keys.firstOrNull()) else expanded
            }
            // After process death, re-derive each muscle's chosen exercise from its last logged set.
            val derived =
                repository
                    .loggedSets(sessionId)
                    .first()
                    .groupBy { it.muscle }
                    .mapNotNull { (muscle, sets) ->
                        val exerciseId = sets.last().exerciseId
                        nameResolver.name(exerciseId)?.let { muscle to ExerciseChoice(exerciseId, it) }
                    }.toMap()
            choices.update { explicit -> derived + explicit }
        }
    }

    fun toggleExpanded(muscle: Muscle) {
        expandedMuscles.update { if (muscle in it) it - muscle else it + muscle }
    }

    fun chooseExercise(
        muscle: Muscle,
        exerciseId: String,
        exerciseName: String,
    ) {
        choices.update { it + (muscle to ExerciseChoice(exerciseId, exerciseName)) }
    }

    fun updateWeightText(
        muscle: Muscle,
        text: String,
    ) {
        val sanitized = text.filter { it.isDigit() || it == '.' }
        updateInput(muscle) { it.copy(weightText = sanitized) }
    }

    fun adjustWeight(
        muscle: Muscle,
        deltaKg: Double,
    ) {
        updateInput(muscle) { input ->
            val current = input.weightText.toDoubleOrNull() ?: 0.0
            input.copy(weightText = formatWeight((current + deltaKg).coerceAtLeast(0.0)))
        }
    }

    fun adjustReps(
        muscle: Muscle,
        delta: Int,
    ) {
        updateInput(muscle) { it.copy(reps = (it.reps + delta).coerceIn(MIN_REPS, MAX_REPS)) }
    }

    fun adjustRir(
        muscle: Muscle,
        delta: Int,
    ) {
        updateInput(muscle) { it.copy(rir = (it.rir + delta).coerceIn(MIN_RIR, MAX_RIR)) }
    }

    /** Persists the current input as a logged set and restarts the rest timer. */
    fun logSet(muscle: Muscle) {
        val state = uiState.value
        val block = state.blocks.firstOrNull { it.muscle == muscle } ?: return
        val exercise = block.exercise ?: return
        val weightKg = block.input.weightText.toDoubleOrNull() ?: return
        val order = state.blocks.sumOf { it.sets.size }
        viewModelScope.launch {
            repository.logSet(
                LoggedSet(
                    sessionId = sessionId,
                    exerciseId = exercise.id,
                    muscle = muscle,
                    weightKg = weightKg,
                    reps = block.input.reps,
                    rir = block.input.rir,
                    orderInSession = order,
                    loggedAtEpochMillis = nowEpochMillis(),
                ),
            )
            mutableEvents.emit(Event.SetLogged)
        }
    }

    fun deleteSet(setId: Long) {
        viewModelScope.launch { repository.deleteSet(setId) }
    }

    private fun updateInput(
        muscle: Muscle,
        transform: (SetInput) -> SetInput,
    ) {
        inputs.update { map -> map + (muscle to transform(map[muscle] ?: SetInput())) }
    }

    private fun buildState(
        current: SessionSummary?,
        sets: List<LoggedSet>,
        choiceMap: Map<Muscle, ExerciseChoice>,
        inputMap: Map<Muscle, SetInput>,
        expanded: Set<Muscle>,
    ): UiState {
        if (current == null) return UiState()
        val setsByMuscle = sets.groupBy { it.muscle }
        val blocks =
            current.muscleTargets.map { (muscle, target) ->
                MuscleBlock(
                    muscle = muscle,
                    targetSets = target,
                    exercise = choiceMap[muscle],
                    sets = setsByMuscle[muscle].orEmpty(),
                    input = inputMap[muscle] ?: SetInput(),
                    isExpanded = muscle in expanded,
                )
            }
        return UiState(session = current, blocks = blocks, canFinish = sets.isNotEmpty())
    }

    companion object {
        private const val MIN_REPS = 1
        private const val MAX_REPS = 50
        private const val MIN_RIR = 0
        private const val MAX_RIR = 5
        const val WEIGHT_STEP_KG = 2.5
    }
}
