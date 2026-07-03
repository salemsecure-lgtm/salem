package dev.salemlift.domain.model

/** Which landmark bound clamped the prescription, if any. */
public enum class ClampBound { MV, MRV }

/**
 * The engine's explainable output for one muscle (DOMAIN.md §5): what it
 * decided, which rule fired, and every adjustment applied on the way.
 */
public data class SetDecision(
    /** Matched rule (e.g. "R7") — shown to the user as the "why". */
    val ruleId: String,
    /** Human-readable rationale copied from the matched rule. */
    val rationale: String,
    /** Delta produced by the rule before any post-processing. */
    val rawDelta: Int,
    /** Delta after the mild-pain cap (before clamping). */
    val cappedDelta: Int,
    /** Final prescribed weekly sets for the next session, clamped to [MV, MRV]. */
    val nextSets: Int,
    /** True when mild joint pain blocked an increase (DOMAIN.md §5 post-processing 1). */
    val mildPainCapped: Boolean,
    /** Set when the [MV, MRV] clamp changed the outcome (DOMAIN.md §5 post-processing 2). */
    val clampedAt: ClampBound?,
    /** True when the rule flags the offending exercise(s) for swap (R1). */
    val exerciseSwapFlagged: Boolean,
)

/** Why a deload was triggered (DOMAIN.md §6). */
public enum class DeloadReason { PLANNED_END, MRV_STALL, SYSTEMIC_FATIGUE, MANUAL }

/** Outcome of evaluating the deload triggers after a committed session. */
public data class DeloadDecision(
    val triggered: Boolean,
    val reasons: List<DeloadReason>,
    /** Muscles whose MRV stall counter fired trigger (b), if any. */
    val stalledMuscles: List<Muscle>,
)
