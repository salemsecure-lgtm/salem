package dev.salemlift.domain.program

import dev.salemlift.domain.engine.MesocyclePlanner
import dev.salemlift.domain.model.Landmarks
import dev.salemlift.domain.model.MesoConfig
import dev.salemlift.domain.model.Muscle
import dev.salemlift.domain.model.PlannedSession
import dev.salemlift.domain.model.ProgramPlan
import dev.salemlift.domain.model.ProgramWeek
import dev.salemlift.domain.model.Split

/**
 * Instantiates a mesocycle for a split (DOMAIN.md §3 + §5.2): the engine's
 * MesoPlan supplies per-muscle weekly targets; each muscle's weekly sets are
 * then distributed across the sessions that train it — evenly, remainder to
 * the earliest (freshest) session, with week-over-week per-session increases
 * chained through [VolumeDistributor.redistribute] so the +2 cap holds across
 * the projection.
 */
public object ProgramBuilder {
    public fun instantiate(
        split: Split,
        config: MesoConfig,
        landmarks: Map<Muscle, Landmarks>,
    ): ProgramPlan {
        val trained = split.trainedMuscles
        val missing = trained - landmarks.keys
        require(missing.isEmpty()) { "split trains muscles without landmarks: $missing" }

        val mesoPlan = MesocyclePlanner.plan(config, landmarks.filterKeys { it in trained })

        // Sessions (by index) that train each muscle.
        val sessionsOf: Map<Muscle, List<Int>> =
            trained.associateWith { muscle ->
                split.sessions.indices.filter { muscle in split.sessions[it].muscles }
            }

        val previousDistributions = mutableMapOf<Muscle, List<Int>>()
        val weeks =
            mesoPlan.weeks.map { weekPlan ->
                // Per-session set map, built muscle by muscle.
                val sessionSets = List(split.sessions.size) { mutableMapOf<Muscle, Int>() }
                for ((muscle, weeklyTarget) in weekPlan.setTargets) {
                    val slots = sessionsOf.getValue(muscle)
                    val previous = previousDistributions[muscle]
                    val distribution =
                        if (previous == null || weekPlan.isDeload) {
                            // Week 1 and the deload are fresh distributions, not deltas.
                            VolumeDistributor.distribute(weeklyTarget, slots.size)
                        } else {
                            VolumeDistributor.redistribute(previous, weeklyTarget).perSession
                        }
                    previousDistributions[muscle] = distribution
                    slots.forEachIndexed { slotPosition, sessionIndex ->
                        val sets = distribution[slotPosition]
                        if (sets > 0) {
                            sessionSets[sessionIndex][muscle] = sets
                        }
                    }
                }
                ProgramWeek(
                    week = weekPlan.week,
                    isDeload = weekPlan.isDeload,
                    effort = weekPlan.effort,
                    loadMultiplier = weekPlan.loadMultiplier,
                    sessions =
                        split.sessions.mapIndexed { index, template ->
                            PlannedSession(name = template.name, muscleSets = sessionSets[index].toMap())
                        },
                )
            }
        return ProgramPlan(split = split, config = config, weeks = weeks)
    }

    /** A week's total prescribed sets for one muscle (the Phase 2 gate metric). */
    public fun weeklyVolume(
        week: ProgramWeek,
        muscle: Muscle,
    ): Int = week.sessions.sumOf { it.muscleSets[muscle] ?: 0 }
}
