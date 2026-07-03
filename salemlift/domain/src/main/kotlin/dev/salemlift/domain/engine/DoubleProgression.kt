package dev.salemlift.domain.engine

import dev.salemlift.domain.model.LoadAction
import dev.salemlift.domain.model.RepRange
import dev.salemlift.domain.model.SetLog

/** Within-exercise load/rep progression (DOMAIN.md §7). */
public object DoubleProgression {
    public const val UPPER_BODY_INCREMENT_KG: Double = 2.5
    public const val LOWER_BODY_INCREMENT_KG: Double = 5.0
    public const val REDUCE_FACTOR: Double = 0.95

    /** Shortfall (target − actual RIR) at which we reduce load instead of holding. */
    public const val REDUCE_SHORTFALL: Int = 2

    /** Surplus (actual − target RIR) beyond which a mid-range load bump is suggested. */
    public const val TOO_EASY_SURPLUS: Int = 2

    public fun next(
        sets: List<SetLog>,
        range: RepRange,
        targetRir: Int,
        isUpperBody: Boolean,
    ): LoadAction {
        require(sets.isNotEmpty()) { "at least one logged set is required" }
        require(targetRir >= 0) { "target RIR cannot be negative, was $targetRir" }

        // fold instead of minOf: minOf inlines an empty-collection throw that the
        // require above makes unreachable, which would break 100% branch coverage.
        val minRir = sets.fold(Int.MAX_VALUE) { acc, set -> minOf(acc, set.rir) }
        val allAtTop = sets.all { it.reps >= range.top }
        val allAtTargetOrEasier = sets.all { it.rir >= targetRir }

        return when {
            // Top of range on every set at target RIR or easier → add load, reset reps.
            allAtTop && allAtTargetOrEasier ->
                LoadAction.AddLoad(
                    incrementKg = if (isUpperBody) UPPER_BODY_INCREMENT_KG else LOWER_BODY_INCREMENT_KG,
                    resetToReps = range.bottom,
                )
            // Harder than prescribed: big shortfall reduces load, small one holds.
            minRir < targetRir ->
                if (targetRir - minRir >= REDUCE_SHORTFALL) {
                    LoadAction.ReduceLoad(REDUCE_FACTOR)
                } else {
                    LoadAction.Hold
                }
            // Way too easy mid-range: nudge the load even before the top is reached.
            minRir > targetRir + TOO_EASY_SURPLUS -> LoadAction.SuggestLoadIncrease
            // Standard double progression: keep load, add reps.
            else -> LoadAction.ProgressReps
        }
    }
}
