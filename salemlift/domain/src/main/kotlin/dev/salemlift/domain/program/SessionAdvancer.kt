package dev.salemlift.domain.program

import dev.salemlift.domain.engine.AutoregRule
import dev.salemlift.domain.engine.Autoregulator
import dev.salemlift.domain.engine.DefaultRules
import dev.salemlift.domain.engine.DeloadTriggers
import dev.salemlift.domain.model.Landmarks
import dev.salemlift.domain.model.Muscle
import dev.salemlift.domain.model.MuscleFeedback
import dev.salemlift.domain.model.MuscleWeekState
import dev.salemlift.domain.model.SessionAdvance

/**
 * Advances the plan after a committed session (DOMAIN.md §5 + §5.2 + §6):
 * runs the decision table per muscle with feedback, redistributes each
 * muscle's new weekly target across its sessions under the +2 cap, updates
 * MRV stall counters, and evaluates the deload triggers. Pure — persistence
 * and UI live elsewhere.
 */
public object SessionAdvancer {
    public fun advance(
        states: Map<Muscle, MuscleWeekState>,
        landmarks: Map<Muscle, Landmarks>,
        feedback: Map<Muscle, MuscleFeedback>,
        finalAccumulationWeekComplete: Boolean = false,
        manualDeloadRequest: Boolean = false,
        table: List<AutoregRule> = DefaultRules.table,
    ): SessionAdvance {
        val statesWithoutLandmarks = states.keys - landmarks.keys
        require(statesWithoutLandmarks.isEmpty()) {
            "muscles without landmarks: $statesWithoutLandmarks"
        }
        val feedbackWithoutState = feedback.keys - states.keys
        require(feedbackWithoutState.isEmpty()) {
            "feedback for muscles outside the plan: $feedbackWithoutState"
        }

        val decisions =
            feedback.mapValues { (muscle, muscleFeedback) ->
                Autoregulator.decide(
                    currentSets = states.getValue(muscle).weeklySets,
                    landmarks = landmarks.getValue(muscle),
                    feedback = muscleFeedback,
                    table = table,
                )
            }

        val cappedMuscles = mutableSetOf<Muscle>()
        val nextStates =
            states.mapValues { (muscle, state) ->
                val decision = decisions[muscle]
                if (decision == null) {
                    // Muscle not trained/rated this session: state carries over.
                    state
                } else {
                    val redistribution =
                        VolumeDistributor.redistribute(state.distribution, decision.nextSets)
                    if (redistribution.capped) {
                        cappedMuscles.add(muscle)
                    }
                    MuscleWeekState(
                        // The achieved total is authoritative when the cap binds.
                        weeklySets = redistribution.total,
                        distribution = redistribution.perSession,
                        stallCount =
                            Autoregulator.nextStallCount(
                                currentSets = state.weeklySets,
                                landmarks = landmarks.getValue(muscle),
                                performance = feedback.getValue(muscle).performance,
                                previousCount = state.stallCount,
                            ),
                    )
                }
            }

        val deload =
            DeloadTriggers.evaluate(
                finalAccumulationWeekComplete = finalAccumulationWeekComplete,
                stallCounts = nextStates.mapValues { it.value.stallCount },
                sessionPerformance = feedback.mapValues { it.value.performance },
                manualRequest = manualDeloadRequest,
            )

        return SessionAdvance(
            decisions = decisions,
            nextStates = nextStates,
            cappedMuscles = cappedMuscles.toSet(),
            deload = deload,
        )
    }
}
