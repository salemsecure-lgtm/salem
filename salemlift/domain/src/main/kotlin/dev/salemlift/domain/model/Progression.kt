package dev.salemlift.domain.model

/** Working rep range for double progression (DOMAIN.md §7). */
public data class RepRange(
    val bottom: Int,
    val top: Int,
) {
    init {
        require(bottom >= 1) { "rep range bottom must be at least 1, was $bottom" }
        require(top > bottom) { "rep range top ($top) must exceed bottom ($bottom)" }
    }
}

/** One logged working set. */
public data class SetLog(
    val weightKg: Double,
    val reps: Int,
    val rir: Int,
) {
    init {
        require(weightKg > 0) { "weight must be positive, was $weightKg" }
        require(reps >= 1) { "reps must be at least 1, was $reps" }
        require(rir >= 0) { "RIR cannot be negative, was $rir" }
    }
}

/** Next-session load action from double progression (DOMAIN.md §7). */
public sealed interface LoadAction {
    /** Top of range hit on all sets at target RIR or easier: add load, reset reps. */
    public data class AddLoad(val incrementKg: Double, val resetToReps: Int) : LoadAction

    /** Sets came in harder than prescribed by ≥ 2 RIR: drop the load. */
    public data class ReduceLoad(val factor: Double) : LoadAction

    /** Sets slightly harder than prescribed: keep load, don't push reps. */
    public data object Hold : LoadAction

    /** Way too easy mid-range (actual RIR > target + 2): nudge the load up. */
    public data object SuggestLoadIncrease : LoadAction

    /** Standard path: keep load, add reps toward the top of the range. */
    public data object ProgressReps : LoadAction
}
