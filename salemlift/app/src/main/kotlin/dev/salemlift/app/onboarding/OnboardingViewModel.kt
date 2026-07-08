package dev.salemlift.app.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.salemlift.data.TrainingRepository
import dev.salemlift.data.settings.SettingsRepository
import dev.salemlift.domain.model.Experience
import dev.salemlift.domain.model.Landmarks
import dev.salemlift.domain.model.MesoConfig
import dev.salemlift.domain.model.Muscle
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * First-run setup: welcome → experience level (seeds landmarks) → training
 * days + split → review → start the first mesocycle. Pure orchestration over
 * [SettingsRepository.applyExperienceSeeds] and
 * [TrainingRepository.startMesocycle]; no new persistence.
 */
class OnboardingViewModel(
    private val trainingRepository: TrainingRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    enum class Step { WELCOME, EXPERIENCE, SCHEDULE, REVIEW }

    data class UiState(
        val step: Step = Step.WELCOME,
        val experience: Experience? = null,
        val daysPerWeek: Int = DEFAULT_DAYS_PER_WEEK,
        val split: Split = suggestedSplit(DEFAULT_DAYS_PER_WEEK),
        val isSplitOverridden: Boolean = false,
        val splitOptions: List<Split> = SplitTemplates.all,
        val landmarkPreview: List<Pair<Muscle, Landmarks>> = emptyList(),
        val isStarting: Boolean = false,
    )

    sealed interface Event {
        /** The first mesocycle exists; leave onboarding. */
        data object Started : Event
    }

    private val draft = MutableStateFlow(UiState())

    val uiState: StateFlow<UiState> =
        combine(draft, settingsRepository.landmarks()) { state, landmarks ->
            state.copy(
                landmarkPreview = PREVIEW_MUSCLES.mapNotNull { muscle -> landmarks[muscle]?.let { muscle to it } },
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState())

    private val mutableEvents =
        MutableSharedFlow<Event>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val events: SharedFlow<Event> = mutableEvents

    fun next() {
        draft.update { state ->
            when (state.step) {
                Step.WELCOME -> state.copy(step = Step.EXPERIENCE)
                Step.EXPERIENCE -> state.copy(step = Step.SCHEDULE)
                Step.SCHEDULE -> state.copy(step = Step.REVIEW)
                Step.REVIEW -> state
            }
        }
    }

    fun back() {
        draft.update { state ->
            when (state.step) {
                Step.WELCOME -> state
                Step.EXPERIENCE -> state.copy(step = Step.WELCOME)
                Step.SCHEDULE -> state.copy(step = Step.EXPERIENCE)
                Step.REVIEW -> state.copy(step = Step.SCHEDULE)
            }
        }
    }

    /** Seeds every muscle's landmarks for the chosen training age, then advances. */
    fun chooseExperience(experience: Experience) {
        viewModelScope.launch {
            settingsRepository.applyExperienceSeeds(experience)
            draft.update { it.copy(experience = experience, step = Step.SCHEDULE) }
        }
    }

    /** Updates days/week; keeps a suggestion in sync unless the user overrode it. */
    fun chooseDays(days: Int) {
        require(days in DAYS_RANGE) { "days/week out of range: $days" }
        draft.update { state ->
            state.copy(
                daysPerWeek = days,
                split = if (state.isSplitOverridden) state.split else suggestedSplit(days),
            )
        }
    }

    fun overrideSplit(split: Split) {
        draft.update { it.copy(split = split, isSplitOverridden = true) }
    }

    /** Creates the first mesocycle from the chosen split (week 1 at MEV). */
    fun start() {
        if (draft.value.isStarting) return
        draft.update { it.copy(isStarting = true) }
        viewModelScope.launch {
            trainingRepository.startMesocycle(draft.value.split, MesoConfig())
            draft.update { it.copy(isStarting = false) }
            mutableEvents.emit(Event.Started)
        }
    }

    companion object {
        const val DEFAULT_DAYS_PER_WEEK: Int = 3
        val DAYS_RANGE: IntRange = 2..6

        /** Landmark rows surfaced on the review step. */
        val PREVIEW_MUSCLES: List<Muscle> =
            listOf(Muscle.CHEST, Muscle.BACK, Muscle.QUADS, Muscle.SIDE_DELTS)

        /**
         * 2–3 days → Full Body, 4 → Upper/Lower, 5–6 → PPL 6-day. Users who
         * want PPL at 3 days pick it from the full template list.
         */
        fun suggestedSplit(days: Int): Split =
            when {
                days <= FULL_BODY_MAX_DAYS -> SplitTemplates.fullBodyThreeDay
                days == UPPER_LOWER_DAYS -> SplitTemplates.upperLowerFourDay
                else -> SplitTemplates.pplSixDay
            }

        private const val FULL_BODY_MAX_DAYS = 3
        private const val UPPER_LOWER_DAYS = 4
    }
}
