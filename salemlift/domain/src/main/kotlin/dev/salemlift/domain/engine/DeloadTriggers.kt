package dev.salemlift.domain.engine

import dev.salemlift.domain.model.DeloadDecision
import dev.salemlift.domain.model.DeloadReason
import dev.salemlift.domain.model.Muscle
import dev.salemlift.domain.model.Performance

/** Deload trigger evaluation (DOMAIN.md §6), run after each committed session. */
public object DeloadTriggers {
    /** Trigger (c): performance down in at least this many muscles in one session. */
    public const val SYSTEMIC_DOWN_LIMIT: Int = 3

    public fun evaluate(
        finalAccumulationWeekComplete: Boolean,
        stallCounts: Map<Muscle, Int>,
        sessionPerformance: Map<Muscle, Performance>,
        manualRequest: Boolean,
    ): DeloadDecision {
        val stalledMuscles =
            stallCounts
                .filterValues { it >= Autoregulator.MRV_STALL_LIMIT }
                .keys
                .sorted()
        val systemicDownCount = sessionPerformance.values.count { it == Performance.DOWN }

        val reasons =
            buildList {
                if (finalAccumulationWeekComplete) add(DeloadReason.PLANNED_END)
                if (stalledMuscles.isNotEmpty()) add(DeloadReason.MRV_STALL)
                if (systemicDownCount >= SYSTEMIC_DOWN_LIMIT) add(DeloadReason.SYSTEMIC_FATIGUE)
                if (manualRequest) add(DeloadReason.MANUAL)
            }

        return DeloadDecision(
            triggered = reasons.isNotEmpty(),
            reasons = reasons,
            stalledMuscles = stalledMuscles,
        )
    }
}
