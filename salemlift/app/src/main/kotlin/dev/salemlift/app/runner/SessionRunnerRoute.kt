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
import dev.salemlift.domain.model.Muscle

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
    var pickerMuscle by rememberSaveable { mutableStateOf<Muscle?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                SessionRunnerViewModel.Event.SetLogged -> timerViewModel.start()
            }
        }
    }

    SessionRunnerScreen(
        state = state,
        timerState = timerState,
        actions =
            RunnerActions(
                onToggleExpanded = viewModel::toggleExpanded,
                onPickExercise = { muscle ->
                    pickerViewModel.open(muscle)
                    pickerMuscle = muscle
                },
                onWeightText = viewModel::updateWeightText,
                onAdjustWeight = viewModel::adjustWeight,
                onAdjustReps = viewModel::adjustReps,
                onAdjustRir = viewModel::adjustRir,
                onLogSet = viewModel::logSet,
                onDeleteSet = viewModel::deleteSet,
                onFinish = onFinish,
            ),
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
