package dev.salemlift.domain.model

/** Effort prescription for a week (DOMAIN.md §3.1). */
public data class WeekEffort(
    val targetRir: Int,
    /** True for "0–1" weeks: prescribed at [targetRir], final set of each exercise may go to 0. */
    val allowZeroOnLastSet: Boolean = false,
) {
    init {
        require(targetRir >= 0) { "target RIR cannot be negative, was $targetRir" }
    }
}

/** Mesocycle configuration (DOMAIN.md §3). */
public data class MesoConfig(
    val accumulationWeeks: Int = 4,
    /** Deload load drop: 10–20% off (DOMAIN.md §3), default −15%. */
    val deloadLoadMultiplier: Double = 0.85,
) {
    init {
        require(accumulationWeeks in 4..6) {
            "accumulation weeks must be 4..6, was $accumulationWeeks"
        }
        require(deloadLoadMultiplier in 0.80..0.90) {
            "deload load multiplier must be within 0.80..0.90 (a 10-20% drop), was $deloadLoadMultiplier"
        }
    }
}

/** One planned week of a mesocycle. */
public data class WeekPlan(
    /** 1-based week number within the mesocycle. */
    val week: Int,
    val isDeload: Boolean,
    val effort: WeekEffort,
    /**
     * Weekly hard-set targets per muscle. Week 1 = MEV (authoritative);
     * later accumulation weeks are the standard-progression *projection* —
     * live values come from the autoregulator as feedback is committed.
     */
    val setTargets: Map<Muscle, Int>,
    /** Multiplier on working loads (1.0 accumulation, deload uses MesoConfig's drop). */
    val loadMultiplier: Double,
)

/** A planned mesocycle: accumulation weeks then one deload week. */
public data class MesoPlan(
    val config: MesoConfig,
    val weeks: List<WeekPlan>,
) {
    val deloadWeek: WeekPlan get() = weeks.last()
}
