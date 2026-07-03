package dev.salemlift.domain.model

/**
 * A muscle's live weekly state while a mesocycle runs: its current weekly
 * prescription, how it is spread over the split's sessions, and the MRV
 * stall counter (DOMAIN.md §5).
 */
public data class MuscleWeekState(
    val weeklySets: Int,
    val distribution: List<Int>,
    val stallCount: Int = 0,
)

/**
 * The engine's full output for one committed session: per-muscle decisions
 * (with rule IDs for the UI's "why"), the resulting next states, which
 * muscles were capped by the §5.2 per-session limit, and the deload verdict.
 */
public data class SessionAdvance(
    val decisions: Map<Muscle, SetDecision>,
    val nextStates: Map<Muscle, MuscleWeekState>,
    val cappedMuscles: Set<Muscle>,
    val deload: DeloadDecision,
)
