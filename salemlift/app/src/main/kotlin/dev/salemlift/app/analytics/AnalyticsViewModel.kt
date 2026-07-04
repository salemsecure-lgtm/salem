package dev.salemlift.app.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.salemlift.data.analytics.AnalyticsRepository
import dev.salemlift.data.analytics.E1rmPoint
import dev.salemlift.data.analytics.ExerciseRef
import dev.salemlift.data.analytics.FatigueSummary
import dev.salemlift.data.analytics.MesoProgress
import dev.salemlift.data.analytics.RuleFireCount
import dev.salemlift.data.analytics.WeekFatigue
import dev.salemlift.data.analytics.WeekTonnage
import dev.salemlift.data.analytics.WeekVolume
import dev.salemlift.domain.model.Landmarks
import dev.salemlift.domain.model.Muscle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Analytics: assembles the Volume / Strength / Cycle tab states from
 * [AnalyticsRepository] aggregates. All heavy queries run once off the main
 * thread at load; muscle/exercise selections recombine the cached snapshot.
 */
class AnalyticsViewModel(
    private val repository: AnalyticsRepository,
) : ViewModel() {
    /** Volume tab: weekly performed-vs-prescribed sets for one selectable muscle. */
    data class VolumeTabState(
        val muscles: List<Muscle>,
        val selected: Muscle?,
        val weeks: List<WeekVolume>,
        val landmarks: Landmarks?,
    ) {
        val isEmpty: Boolean get() = muscles.isEmpty()
    }

    /** Strength tab: e1RM trend for one selectable exercise + weekly tonnage. */
    data class StrengthTabState(
        val exercises: List<ExerciseRef>,
        val selected: ExerciseRef?,
        val trend: List<E1rmPoint>,
        val tonnage: List<WeekTonnage>,
    ) {
        val isEmpty: Boolean get() = exercises.isEmpty() && tonnage.isEmpty()
    }

    /** Cycle tab: mesocycle progress + the fatigue dashboard inputs. */
    data class CycleTabState(
        val progress: MesoProgress?,
        val weekFatigue: List<WeekFatigue>,
        val ruleFires: List<RuleFireCount>,
    ) {
        val isEmpty: Boolean get() = progress == null
    }

    sealed interface UiState {
        data object Loading : UiState

        data class Ready(
            val volume: VolumeTabState,
            val strength: StrengthTabState,
            val cycle: CycleTabState,
        ) : UiState
    }

    private data class Snapshot(
        val weeklyVolume: Map<Muscle, List<WeekVolume>>,
        val landmarks: Map<Muscle, Landmarks>,
        val exercises: List<ExerciseRef>,
        val tonnage: List<WeekTonnage>,
        val progress: MesoProgress?,
        val fatigue: FatigueSummary,
    )

    private val snapshot = MutableStateFlow<Snapshot?>(null)
    private val selectedMuscle = MutableStateFlow<Muscle?>(null)
    private val selectedExerciseId = MutableStateFlow<String?>(null)

    /** Per-exercise trend cache so re-selecting an exercise never re-queries. */
    private val trendCache = MutableStateFlow<Map<String, List<E1rmPoint>>>(emptyMap())

    val uiState: StateFlow<UiState> =
        combine(snapshot, selectedMuscle, selectedExerciseId, trendCache) { snap, muscle, exerciseId, trends ->
            if (snap == null) {
                UiState.Loading
            } else {
                val muscles = snap.weeklyVolume.keys.toList()
                val activeMuscle = muscle?.takeIf { it in snap.weeklyVolume } ?: muscles.firstOrNull()
                val exercise =
                    snap.exercises.firstOrNull { it.id == exerciseId } ?: snap.exercises.firstOrNull()
                UiState.Ready(
                    volume =
                        VolumeTabState(
                            muscles = muscles,
                            selected = activeMuscle,
                            weeks = activeMuscle?.let(snap.weeklyVolume::get).orEmpty(),
                            landmarks = activeMuscle?.let(snap.landmarks::get),
                        ),
                    strength =
                        StrengthTabState(
                            exercises = snap.exercises,
                            selected = exercise,
                            trend = exercise?.let { trends[it.id] }.orEmpty(),
                            tonnage = snap.tonnage,
                        ),
                    cycle =
                        CycleTabState(
                            progress = snap.progress,
                            weekFatigue = snap.fatigue.weeks,
                            ruleFires = snap.fatigue.ruleFires,
                        ),
                )
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Loading)

    init {
        viewModelScope.launch { load() }
    }

    fun selectMuscle(muscle: Muscle) {
        selectedMuscle.value = muscle
    }

    fun selectExercise(exerciseId: String) {
        selectedExerciseId.value = exerciseId
        viewModelScope.launch { ensureTrendLoaded(exerciseId) }
    }

    private suspend fun load() {
        val mesoId = repository.activeMesoId()
        val loaded =
            Snapshot(
                weeklyVolume = mesoId?.let { repository.weeklyVolume(it) }.orEmpty(),
                landmarks = repository.landmarks(),
                exercises = repository.exercisesWithHistory(),
                tonnage = mesoId?.let { repository.tonnage(it) }.orEmpty(),
                progress = repository.mesoProgress(),
                fatigue =
                    mesoId?.let { repository.fatigue(it) }
                        ?: FatigueSummary(weeks = emptyList(), ruleFires = emptyList()),
            )
        snapshot.value = loaded
        loaded.exercises.firstOrNull()?.let { ensureTrendLoaded(it.id) }
    }

    private suspend fun ensureTrendLoaded(exerciseId: String) {
        if (trendCache.value.containsKey(exerciseId)) return
        val trend = repository.e1rmTrend(exerciseId)
        trendCache.update { it + (exerciseId to trend) }
    }
}
