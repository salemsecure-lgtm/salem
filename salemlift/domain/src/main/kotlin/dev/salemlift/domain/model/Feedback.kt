package dev.salemlift.domain.model

/** Canonical muscle groups (DOMAIN.md §1.1). */
public enum class Muscle {
    CHEST,
    BACK,
    FRONT_DELTS,
    SIDE_DELTS,
    REAR_DELTS,
    BICEPS,
    TRICEPS,
    FOREARMS,
    QUADS,
    HAMSTRINGS,
    GLUTES,
    CALVES,
    ABS,
    TRAPS,
}

/** Recovery state reported at the start of the next session training the muscle (DOMAIN.md §4). */
public enum class Soreness { NEVER_SORE, RECOVERED_EARLY, RECOVERED_ON_TIME, STILL_SORE }

/** Performance vs the previous comparable session (DOMAIN.md §4.1). */
public enum class Performance { UP, SAME, DOWN }

/** Pump reported after the muscle's last exercise in the session. */
public enum class Pump { LOW, MODERATE, HIGH }

/** Joint pain reported after the muscle's last exercise in the session. */
public enum class JointPain { NONE, MILD, SIGNIFICANT }

/** Training age used to scale landmark seeds at onboarding (DOMAIN.md §2.1). */
public enum class Experience(public val factor: Double) {
    BEGINNER(0.7),
    INTERMEDIATE(1.0),
    ADVANCED(1.15),
}

/** The complete per-muscle feedback tuple the decision table consumes (DOMAIN.md §5). */
public data class MuscleFeedback(
    val soreness: Soreness,
    val performance: Performance,
    val pump: Pump,
    val jointPain: JointPain,
)
