package dev.salemlift.domain.engine

import dev.salemlift.domain.model.Performance

/**
 * Performance comparison from RIR-adjusted e1RM (DOMAIN.md §4.1).
 *
 * §4.1's auxiliary clauses are subsumed by the e1RM threshold: a one-rep or
 * one-RIR difference at equal load always moves RIR-adjusted e1RM by more
 * than the 1% band, so "same load/reps at lower effort" classifies UP and
 * "failed prescribed reps at target RIR" classifies DOWN without extra rules.
 */
public object PerformanceClassifier {
    public const val DEFAULT_THRESHOLD_PCT: Double = 1.0

    public fun classify(
        currentE1rm: Double,
        previousE1rm: Double,
        thresholdPct: Double = DEFAULT_THRESHOLD_PCT,
    ): Performance {
        require(currentE1rm > 0) { "current e1RM must be positive, was $currentE1rm" }
        require(previousE1rm > 0) { "previous e1RM must be positive, was $previousE1rm" }
        val changePct = (currentE1rm - previousE1rm) / previousE1rm * 100.0
        return when {
            changePct > thresholdPct -> Performance.UP
            changePct < -thresholdPct -> Performance.DOWN
            else -> Performance.SAME
        }
    }

    /**
     * Per-muscle aggregation (DOMAIN.md §4.1): credit-weighted majority across
     * the muscle's exercises; ties (including no data) resolve to SAME.
     */
    public fun aggregate(votes: List<Pair<Performance, Double>>): Performance {
        if (votes.isEmpty()) return Performance.SAME
        val weights = mutableMapOf<Performance, Double>()
        for ((performance, credit) in votes) {
            require(credit > 0) { "exercise credit must be positive, was $credit" }
            weights[performance] = (weights[performance] ?: 0.0) + credit
        }
        val maxWeight = weights.values.max()
        val winners = weights.filterValues { it == maxWeight }.keys
        return if (winners.size == 1) winners.first() else Performance.SAME
    }
}
