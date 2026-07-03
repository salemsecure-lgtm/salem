package dev.salemlift.app

import dev.salemlift.data.CommitOutcome
import dev.salemlift.data.SessionState
import dev.salemlift.data.SessionSummary
import dev.salemlift.domain.model.ClampBound
import dev.salemlift.domain.model.DeloadDecision
import dev.salemlift.domain.model.Muscle
import dev.salemlift.domain.model.SessionAdvance
import dev.salemlift.domain.model.SetDecision
import dev.salemlift.domain.model.WeekEffort

/** Shared builders for ViewModel tests. */
object TestData {
    const val SESSION_ID = 10L

    fun session(
        sessionId: Long = SESSION_ID,
        state: SessionState = SessionState.PENDING,
        muscleTargets: Map<Muscle, Int> = linkedMapOf(Muscle.CHEST to 3, Muscle.TRICEPS to 2),
        isDeload: Boolean = false,
        week: Int = 2,
        name: String = "Push",
    ): SessionSummary =
        SessionSummary(
            sessionId = sessionId,
            mesoId = 1L,
            week = week,
            dayIndex = 0,
            name = name,
            isDeload = isDeload,
            effort = WeekEffort(targetRir = 2),
            loadMultiplier = 1.0,
            state = state,
            muscleTargets = muscleTargets,
        )

    fun decision(
        ruleId: String = "R7",
        rationale: String = "Recovered on time and holding or progressing: standard weekly add",
        rawDelta: Int = 1,
        cappedDelta: Int = 1,
        previousSets: Int = 12,
        nextSets: Int = 13,
        mildPainCapped: Boolean = false,
        clampedAt: ClampBound? = null,
        exerciseSwapFlagged: Boolean = false,
    ): SetDecision =
        SetDecision(
            ruleId = ruleId,
            rationale = rationale,
            rawDelta = rawDelta,
            cappedDelta = cappedDelta,
            previousSets = previousSets,
            nextSets = nextSets,
            mildPainCapped = mildPainCapped,
            clampedAt = clampedAt,
            exerciseSwapFlagged = exerciseSwapFlagged,
        )

    fun outcome(
        decisions: Map<Muscle, SetDecision> = mapOf(Muscle.CHEST to decision()),
        cappedMuscles: Set<Muscle> = emptySet(),
        deload: DeloadDecision = DeloadDecision(triggered = false, reasons = emptyList(), stalledMuscles = emptyList()),
        nextSession: SessionSummary? = null,
    ): CommitOutcome =
        CommitOutcome(
            advance =
                SessionAdvance(
                    decisions = decisions,
                    nextStates = emptyMap(),
                    cappedMuscles = cappedMuscles,
                    deload = deload,
                ),
            decisions = decisions,
            nextSession = nextSession,
        )
}
