package dev.salemlift.domain.engine

import dev.salemlift.domain.model.Landmarks
import dev.salemlift.domain.model.MesoConfig
import dev.salemlift.domain.model.MesoPlan
import dev.salemlift.domain.model.Muscle
import dev.salemlift.domain.model.WeekPlan

/**
 * Mesocycle planner (DOMAIN.md §3): every muscle starts week 1 at MEV; later
 * accumulation weeks carry the standard +1/week *projection* clamped to MRV
 * (live numbers come from the autoregulator); the final week is the deload at
 * MV volume, RIR 4–5, reduced load.
 */
public object MesocyclePlanner {
    public fun plan(
        config: MesoConfig,
        landmarks: Map<Muscle, Landmarks>,
    ): MesoPlan {
        require(landmarks.isNotEmpty()) { "at least one muscle must be planned" }

        val efforts = RirSchedule.forAccumulation(config.accumulationWeeks)
        val accumulation =
            efforts.mapIndexed { index, effort ->
                WeekPlan(
                    week = index + 1,
                    isDeload = false,
                    effort = effort,
                    setTargets =
                        landmarks.mapValues { (_, marks) ->
                            minOf(marks.mev + index, marks.mrv)
                        },
                    loadMultiplier = 1.0,
                )
            }
        val deload =
            WeekPlan(
                week = config.accumulationWeeks + 1,
                isDeload = true,
                effort = RirSchedule.deload,
                setTargets = landmarks.mapValues { (_, marks) -> marks.mv },
                loadMultiplier = config.deloadLoadMultiplier,
            )
        return MesoPlan(config = config, weeks = accumulation + deload)
    }
}
