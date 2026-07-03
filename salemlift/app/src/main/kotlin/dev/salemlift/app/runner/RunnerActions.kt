package dev.salemlift.app.runner

import dev.salemlift.domain.model.Muscle

/** All runner callbacks bundled so composable signatures stay small. */
data class RunnerActions(
    val onToggleExpanded: (Muscle) -> Unit,
    val onPickExercise: (Muscle) -> Unit,
    val onWeightText: (Muscle, String) -> Unit,
    val onAdjustWeight: (Muscle, Double) -> Unit,
    val onAdjustReps: (Muscle, Int) -> Unit,
    val onAdjustRir: (Muscle, Int) -> Unit,
    val onLogSet: (Muscle) -> Unit,
    val onDeleteSet: (Long) -> Unit,
    val onFinish: () -> Unit,
)
