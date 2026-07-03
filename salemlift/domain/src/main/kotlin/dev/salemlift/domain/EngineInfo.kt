package dev.salemlift.domain

/**
 * Phase 0 placeholder proving the pure-JVM module wiring. The real engine
 * (per docs/DOMAIN.md) is implemented in Phase 1 after the domain model is
 * approved at Gate 0.
 */
public object EngineInfo {
    public const val VERSION: String = "0.1.0-phase0"
}

/**
 * RIR-adjusted Epley estimated 1RM (DOMAIN.md §8). Included at Phase 0 as the
 * scaffold's test-harness proof: it is the one deterministic, formula-fixed
 * piece of the domain. Re-verified against DOMAIN.md in Phase 1.
 */
public fun e1rm(
    weightKg: Double,
    reps: Int,
    rir: Int,
): Double {
    require(weightKg > 0) { "weight must be positive" }
    require(reps > 0) { "reps must be positive" }
    require(rir >= 0) { "RIR cannot be negative" }
    return weightKg * (1 + (reps + rir) / 30.0)
}
