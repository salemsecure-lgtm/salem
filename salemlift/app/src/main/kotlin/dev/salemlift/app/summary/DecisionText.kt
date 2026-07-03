package dev.salemlift.app.summary

import dev.salemlift.domain.model.ClampBound
import dev.salemlift.domain.model.DeloadReason
import dev.salemlift.domain.model.SetDecision

/**
 * Pure formatting of the engine's [SetDecision] into the "what changed and
 * why" line, e.g. "12 → 13 sets · R7 — Recovered on time …". The rule ID is
 * always shown — the engine never changes the next session silently.
 */
object DecisionText {
    fun headline(decision: SetDecision): String =
        "${decision.previousSets} → ${decision.nextSets} sets · ${decision.ruleId} — ${decision.rationale}"

    /** Cap / clamp / swap-flag badges attached to a muscle's decision row. */
    fun badges(
        decision: SetDecision,
        isSessionCapped: Boolean = false,
    ): List<String> =
        buildList {
            if (decision.mildPainCapped) add("Increase capped — mild joint pain")
            when (decision.clampedAt) {
                ClampBound.MV -> add("Clamped at MV")
                ClampBound.MRV -> add("Clamped at MRV")
                null -> Unit
            }
            if (decision.exerciseSwapFlagged) add("Exercise swap suggested")
            if (isSessionCapped) add("Per-session cap applied")
        }

    fun deloadReasonLabel(reason: DeloadReason): String =
        when (reason) {
            DeloadReason.PLANNED_END -> "Planned end of accumulation"
            DeloadReason.MRV_STALL -> "Volume stalled at MRV"
            DeloadReason.SYSTEMIC_FATIGUE -> "Systemic fatigue"
            DeloadReason.MANUAL -> "Manual deload request"
        }
}
