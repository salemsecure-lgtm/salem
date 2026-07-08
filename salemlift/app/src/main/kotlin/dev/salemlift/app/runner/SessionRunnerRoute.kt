package dev.salemlift.app.runner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.salemlift.app.di.AppContainer
import dev.salemlift.app.picker.ExercisePickerDialog
import dev.salemlift.app.picker.ExercisePickerViewModel
import dev.salemlift.app.timer.RestTimerViewModel
import dev.salemlift.app.timer.TimerActions
import dev.salemlift.data.settings.SettingsRepository
import dev.salemlift.domain.model.Muscle

private const val MILLIS_PER_SECOND = 1_000L

@Composable
fun SessionRunnerRoute(
    container: AppContainer,
    sessionId: Long,
    onFinish: () -> Unit,
) {
    val viewModel: SessionRunnerViewModel =
        viewModel(key = "runner-$sessionId") {
            SessionRunnerViewModel(
                repository = container.repository,
                nameResolver = container.exerciseNameResolver,
                sessionId = sessionId,
            )
        }
    val timerViewModel: RestTimerViewModel =
        viewModel(key = "rest-timer-$sessionId") { RestTimerViewModel(savedStateHandle = createSavedStateHandle()) }
    val pickerViewModel: ExercisePickerViewModel =
        viewModel { ExercisePickerViewModel(container.exerciseDao) }

    val state by viewModel.uiState.collectAsState()
    val timerState by timerViewModel.uiState.collectAsState()
    // The user-tunable default rest duration (Settings → Rest timer).
    val restSeconds by container.settingsRepository
        .restSeconds()
        .collectAsState(initial = SettingsRepository.DEFAULT_REST_SECONDS)
    var pickerMuscle by rememberSaveable { mutableStateOf<Muscle?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                SessionRunnerViewModel.Event.SetLogged ->
                    timerViewModel.start(durationMillis = restSeconds * MILLIS_PER_SECOND)
            }
        }
    }

    SessionRunnerScreen(
        state = state,
        timerState = timerState,
        actions =
            runnerActions(viewModel, onFinish) { muscle ->
                pickerViewModel.open(muscle)
                pickerMuscle = muscle
            },
        timerActions =
            TimerActions(
                onTogglePause = timerViewModel::togglePause,
                onAddThirtySeconds = timerViewModel::addThirtySeconds,
            ),
    )

    pickerMuscle?.let { muscle ->
        ExercisePickerDialog(
            muscle = muscle,
            viewModel = pickerViewModel,
            onSelect = { exercise ->
                viewModel.chooseExercise(muscle, exercise.id, exercise.name)
                pickerMuscle = null
            },
            onDismiss = { pickerMuscle = null },
        )
    }
}

private fun runnerActions(
    viewModel: SessionRunnerViewModel,
    onFinish: () -> Unit,
    onPickExercise: (Muscle) -> Unit,
): RunnerActions =
    RunnerActions(
        onToggleExpanded = viewModel::toggleExpanded,
        onPickExercise = onPickExercise,
        onWeightText = viewModel::updateWeightText,
        onAdjustWeight = viewModel::adjustWeight,
        onAdjustReps = viewModel::adjustReps,
        onAdjustRir = viewModel::adjustRir,
        onLogSet = viewModel::logSet,
        onDeleteSet = viewModel::deleteSet,
        onFinish = onFinish,
    )
