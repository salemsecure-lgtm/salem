package dev.salemlift.domain.model

import kotlin.math.roundToInt

/**
 * Weekly hard-set volume landmarks for one muscle (DOMAIN.md §2).
 * Invariant: MV ≤ MEV ≤ MAV ≤ MRV, all ≥ 1.
 */
public data class Landmarks(
    val mev: Int,
    val mrv: Int,
    val mv: Int = defaultMv(mev),
    val mav: Int = defaultMav(mev, mrv),
) {
    init {
        require(mv >= 1) { "MV must be at least 1, was $mv" }
        require(mv <= mev) { "MV ($mv) must not exceed MEV ($mev)" }
        require(mev <= mav) { "MEV ($mev) must not exceed MAV ($mav)" }
        require(mav <= mrv) { "MAV ($mav) must not exceed MRV ($mrv)" }
    }

    /**
     * Experience scaling (DOMAIN.md §2.1): the factor multiplies MEV and MRV
     * (MEV floor 2, MRV floor MEV); MV and MAV are re-derived from the scaled
     * values so the landmark ordering always holds.
     */
    public fun scaledFor(experience: Experience): Landmarks {
        val scaledMev = maxOf(2, (mev * experience.factor).roundToInt())
        val scaledMrv = maxOf(scaledMev, (mrv * experience.factor).roundToInt())
        return Landmarks(mev = scaledMev, mrv = scaledMrv)
    }

    public companion object {
        /** MV = round(MEV / 2), min 2 (DOMAIN.md §2). */
        public fun defaultMv(mev: Int): Int = maxOf(2, (mev / 2.0).roundToInt())

        /** MAV defaults to the MEV–MRV midpoint when not seeded explicitly. */
        public fun defaultMav(
            mev: Int,
            mrv: Int,
        ): Int = ((mev + mrv) / 2.0).roundToInt()
    }
}

/** Intermediate seed defaults (DOMAIN.md §2.1) — user-tunable, not a proprietary table. */
public object DefaultLandmarks {
    public val seeds: Map<Muscle, Landmarks> =
        mapOf(
            Muscle.CHEST to Landmarks(mev = 8, mrv = 22, mav = 16),
            Muscle.BACK to Landmarks(mev = 10, mrv = 25, mav = 18),
            Muscle.FRONT_DELTS to Landmarks(mev = 4, mrv = 16, mav = 10),
            Muscle.SIDE_DELTS to Landmarks(mev = 8, mrv = 26, mav = 18),
            Muscle.REAR_DELTS to Landmarks(mev = 6, mrv = 22, mav = 14),
            Muscle.BICEPS to Landmarks(mev = 6, mrv = 26, mav = 16),
            Muscle.TRICEPS to Landmarks(mev = 6, mrv = 18, mav = 12),
            Muscle.FOREARMS to Landmarks(mev = 4, mrv = 16, mav = 10),
            Muscle.QUADS to Landmarks(mev = 8, mrv = 20, mav = 14),
            Muscle.HAMSTRINGS to Landmarks(mev = 6, mrv = 18, mav = 12),
            Muscle.GLUTES to Landmarks(mev = 6, mrv = 20, mav = 12),
            Muscle.CALVES to Landmarks(mev = 8, mrv = 20, mav = 14),
            Muscle.ABS to Landmarks(mev = 6, mrv = 22, mav = 14),
            Muscle.TRAPS to Landmarks(mev = 6, mrv = 20, mav = 12),
        )

    /** Seeds scaled for the user's training age. */
    public fun seedsFor(experience: Experience): Map<Muscle, Landmarks> =
        seeds.mapValues { (_, landmarks) -> landmarks.scaledFor(experience) }
}
