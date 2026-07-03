package dev.salemlift.domain.engine

import dev.salemlift.domain.model.ClampBound
import dev.salemlift.domain.model.JointPain
import dev.salemlift.domain.model.Landmarks
import dev.salemlift.domain.model.MuscleFeedback
import dev.salemlift.domain.model.Performance
import dev.salemlift.domain.model.SetDecision

/**
 * The set-progression autoregulator (DOMAIN.md §5): evaluates the decision
 * table for one muscle's committed feedback and produces the next session's
 * weekly set prescription with a full explanation trail.
 */
public object Autoregulator {
    /** Consecutive performance-down sessions at MRV that fire deload trigger (b). */
    public const val MRV_STALL_LIMIT: Int = 2

    public fun decide(
        currentSets: Int,
        landmarks: Landmarks,
        feedback: MuscleFeedback,
        table: List<AutoregRule> = DefaultRules.table,
    ): SetDecision {
        require(currentSets >= 1) { "current sets must be at least 1, was $currentSets" }
        val rule =
            checkNotNull(table.firstOrNull { it.matches(feedback) }) {
                "No rule matched $feedback — the rule table must be a total function"
            }
        val rawDelta = rule.delta.compute(currentSets, landmarks)

        // Post-processing 1: mild joint pain blocks increases, allows reductions.
        val mildPainCapped = feedback.jointPain == JointPain.MILD && rawDelta > 0
        val cappedDelta = if (mildPainCapped) 0 else rawDelta

        // Post-processing 2: clamp to [MV, MRV]. "At MRV → hold" emerges here.
        val unclamped = currentSets + cappedDelta
        val nextSets = unclamped.coerceIn(landmarks.mv, landmarks.mrv)
        val clampedAt =
            when {
                unclamped > landmarks.mrv -> ClampBound.MRV
                unclamped < landmarks.mv -> ClampBound.MV
                else -> null
            }

        return SetDecision(
            ruleId = rule.id,
            rationale = rule.rationale,
            rawDelta = rawDelta,
            cappedDelta = cappedDelta,
            previousSets = currentSets,
            nextSets = nextSets,
            mildPainCapped = mildPainCapped,
            clampedAt = clampedAt,
            exerciseSwapFlagged = rule.flagsExerciseSwap,
        )
    }

    /**
     * MRV stall tracking (DOMAIN.md §5): counts consecutive performance-down
     * sessions while at (or above) MRV; any other outcome resets the counter.
     * Deload trigger (b) fires when the returned count reaches [MRV_STALL_LIMIT].
     */
    public fun nextStallCount(
        currentSets: Int,
        landmarks: Landmarks,
        performance: Performance,
        previousCount: Int,
    ): Int {
        require(previousCount >= 0) { "stall count cannot be negative, was $previousCount" }
        return if (currentSets >= landmarks.mrv && performance == Performance.DOWN) {
            previousCount + 1
        } else {
            0
        }
    }
}
