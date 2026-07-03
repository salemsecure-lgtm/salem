package dev.salemlift.domain.engine

import dev.salemlift.domain.model.JointPain
import dev.salemlift.domain.model.Landmarks
import dev.salemlift.domain.model.MuscleFeedback
import dev.salemlift.domain.model.Performance
import dev.salemlift.domain.model.Pump
import dev.salemlift.domain.model.Soreness

/** How a rule computes its raw set delta. */
public sealed interface DeltaSpec {
    public fun compute(
        currentSets: Int,
        landmarks: Landmarks,
    ): Int

    /** A constant delta. */
    public class Fixed(private val delta: Int) : DeltaSpec {
        override fun compute(
            currentSets: Int,
            landmarks: Landmarks,
        ): Int = delta
    }

    /**
     * R4's distance-scaled delta: [farDelta] when at least [farThreshold] sets
     * below MRV, otherwise [nearDelta] (DOMAIN.md §5).
     */
    public class MrvDistanceScaled(
        private val nearDelta: Int,
        private val farDelta: Int,
        private val farThreshold: Int,
    ) : DeltaSpec {
        override fun compute(
            currentSets: Int,
            landmarks: Landmarks,
        ): Int = if (landmarks.mrv - currentSets >= farThreshold) farDelta else nearDelta
    }
}

/**
 * One row of the autoregulation decision table (DOMAIN.md §5). A condition
 * dimension matches when the feedback value is in its set; the shipped rows
 * use [anySoreness]-style full sets for "don't care" dimensions. Rules are
 * data so the table stays user-tunable without code changes.
 */
public class AutoregRule(
    public val id: String,
    public val rationale: String,
    private val soreness: Set<Soreness> = anySoreness,
    private val performance: Set<Performance> = anyPerformance,
    private val pump: Set<Pump> = anyPump,
    private val jointPain: Set<JointPain> = anyJointPain,
    public val delta: DeltaSpec,
    public val flagsExerciseSwap: Boolean = false,
) {
    public fun matches(feedback: MuscleFeedback): Boolean =
        feedback.soreness in soreness &&
            feedback.performance in performance &&
            feedback.pump in pump &&
            feedback.jointPain in jointPain

    public companion object {
        public val anySoreness: Set<Soreness> = Soreness.entries.toSet()
        public val anyPerformance: Set<Performance> = Performance.entries.toSet()
        public val anyPump: Set<Pump> = Pump.entries.toSet()
        public val anyJointPain: Set<JointPain> = JointPain.entries.toSet()
    }
}

/** The shipped default decision table — DOMAIN.md §5 rows R1–R9, first match wins. */
public object DefaultRules {
    public val table: List<AutoregRule> =
        listOf(
            AutoregRule(
                id = "R1",
                rationale = "Significant joint pain: reduce and swap the offending exercise",
                jointPain = setOf(JointPain.SIGNIFICANT),
                delta = DeltaSpec.Fixed(-1),
                flagsExerciseSwap = true,
            ),
            AutoregRule(
                id = "R2",
                rationale = "Still sore and performance down: recovery exceeded, possible MRV",
                soreness = setOf(Soreness.STILL_SORE),
                performance = setOf(Performance.DOWN),
                delta = DeltaSpec.Fixed(-1),
            ),
            AutoregRule(
                id = "R3",
                rationale = "Still sore but performing: hold",
                soreness = setOf(Soreness.STILL_SORE),
                performance = setOf(Performance.SAME, Performance.UP),
                delta = DeltaSpec.Fixed(0),
            ),
            AutoregRule(
                id = "R4",
                rationale = "No soreness, performance up, weak pump: strongly under-dosed",
                soreness = setOf(Soreness.NEVER_SORE),
                performance = setOf(Performance.UP),
                pump = setOf(Pump.LOW),
                delta = DeltaSpec.MrvDistanceScaled(nearDelta = 2, farDelta = 3, farThreshold = 4),
            ),
            AutoregRule(
                id = "R5",
                rationale = "Recovered with room to spare and progressing: fast add",
                soreness = setOf(Soreness.NEVER_SORE, Soreness.RECOVERED_EARLY),
                performance = setOf(Performance.UP),
                pump = setOf(Pump.LOW, Pump.MODERATE),
                delta = DeltaSpec.Fixed(2),
            ),
            AutoregRule(
                id = "R6",
                rationale = "Progressing with a big pump: stimulus adequate, standard add",
                soreness = setOf(Soreness.NEVER_SORE, Soreness.RECOVERED_EARLY),
                performance = setOf(Performance.UP),
                pump = setOf(Pump.HIGH),
                delta = DeltaSpec.Fixed(1),
            ),
            AutoregRule(
                id = "R7",
                rationale = "Recovered on time and holding or progressing: standard weekly add",
                soreness = setOf(Soreness.RECOVERED_ON_TIME),
                performance = setOf(Performance.SAME, Performance.UP),
                delta = DeltaSpec.Fixed(1),
            ),
            AutoregRule(
                id = "R8",
                rationale = "Recovered early with flat performance: capacity to spare",
                soreness = setOf(Soreness.NEVER_SORE, Soreness.RECOVERED_EARLY),
                performance = setOf(Performance.SAME),
                delta = DeltaSpec.Fixed(1),
            ),
            AutoregRule(
                id = "R9",
                rationale = "Performance down without soreness: hold, don't add fatigue",
                performance = setOf(Performance.DOWN),
                delta = DeltaSpec.Fixed(0),
            ),
        )
}
