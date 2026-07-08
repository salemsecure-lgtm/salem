package dev.salemlift.data.settings

import dev.salemlift.domain.engine.AutoregRule
import dev.salemlift.domain.engine.DefaultRules
import dev.salemlift.domain.engine.DeltaSpec
import dev.salemlift.domain.model.JointPain
import dev.salemlift.domain.model.Landmarks
import dev.salemlift.domain.model.MuscleFeedback
import dev.salemlift.domain.model.Performance
import dev.salemlift.domain.model.Pump
import dev.salemlift.domain.model.Soreness

/**
 * Delta tuning over [DefaultRules] (DOMAIN.md §5: the rule table is data).
 * Conditions are FIXED in v1 — only [DeltaSpec.Fixed] deltas may be
 * overridden. R4's [DeltaSpec.MrvDistanceScaled] row passes through untouched
 * and is surfaced read-only.
 */
object RuleTables {
    /** Sanity bounds for a tuned delta (the shipped table spans −1..+3). */
    val DELTA_RANGE: IntRange = -3..3

    /** Probe inputs for reading a Fixed delta; [DeltaSpec.Fixed.compute] ignores both. */
    private const val PROBE_SETS = 1
    private val probeLandmarks = Landmarks(mev = 2, mrv = 2)

    /** Every feedback tuple — the decision table's full input space (4·3·3·3 = 108). */
    private val allFeedback: List<MuscleFeedback> =
        Soreness.entries.flatMap { soreness ->
            Performance.entries.flatMap { performance ->
                Pump.entries.flatMap { pump ->
                    JointPain.entries.map { jointPain ->
                        MuscleFeedback(soreness, performance, pump, jointPain)
                    }
                }
            }
        }

    /** Ids of the rules whose delta may be tuned (all Fixed-delta rows: R1–R3, R5–R9). */
    val editableRuleIds: Set<String> =
        DefaultRules.table
            .filter { it.delta is DeltaSpec.Fixed }
            .map { it.id }
            .toSet()

    /** The rule's constant delta, or null when its delta is not Fixed (R4). */
    fun fixedDelta(rule: AutoregRule): Int? = (rule.delta as? DeltaSpec.Fixed)?.compute(PROBE_SETS, probeLandmarks)

    /**
     * [DefaultRules.table] with Fixed deltas replaced by [overrides] (keyed by
     * rule id). Conditions, row order, rationale, and swap flags are preserved;
     * overrides for non-Fixed or unknown ids are ignored.
     */
    fun tuned(overrides: Map<String, Int>): List<AutoregRule> =
        DefaultRules.table.map { rule ->
            val delta = overrides[rule.id]
            if (delta == null || rule.delta !is DeltaSpec.Fixed) rule else withFixedDelta(rule, delta)
        }

    /**
     * Rebuilds [rule] with a new constant delta. [AutoregRule]'s condition sets
     * are private, so each dimension's set is recovered through the public
     * [AutoregRule.matches] conjunction: a value belongs to a dimension's set
     * iff some feedback tuple carrying that value matches the rule.
     */
    private fun withFixedDelta(
        rule: AutoregRule,
        delta: Int,
    ): AutoregRule =
        AutoregRule(
            id = rule.id,
            rationale = rule.rationale,
            soreness =
                Soreness.entries.filterTo(mutableSetOf()) { value -> matchesWith(rule) { it.soreness == value } },
            performance =
                Performance.entries.filterTo(mutableSetOf()) { value -> matchesWith(rule) { it.performance == value } },
            pump = Pump.entries.filterTo(mutableSetOf()) { value -> matchesWith(rule) { it.pump == value } },
            jointPain =
                JointPain.entries.filterTo(mutableSetOf()) { value -> matchesWith(rule) { it.jointPain == value } },
            delta = DeltaSpec.Fixed(delta),
            flagsExerciseSwap = rule.flagsExerciseSwap,
        )

    private fun matchesWith(
        rule: AutoregRule,
        dimension: (MuscleFeedback) -> Boolean,
    ): Boolean = allFeedback.any { dimension(it) && rule.matches(it) }
}
