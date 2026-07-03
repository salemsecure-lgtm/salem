package dev.salemlift.app.feedback

import dev.salemlift.domain.model.JointPain
import dev.salemlift.domain.model.Muscle
import dev.salemlift.domain.model.Performance
import dev.salemlift.domain.model.Pump
import dev.salemlift.domain.model.Soreness

/** Feedback callbacks bundled to keep signatures small. */
data class FeedbackActions(
    val onSoreness: (Muscle, Soreness) -> Unit,
    val onPump: (Muscle, Pump) -> Unit,
    val onJointPain: (Muscle, JointPain) -> Unit,
    val onPerformance: (Muscle, Performance) -> Unit,
    val onCommit: () -> Unit,
)
