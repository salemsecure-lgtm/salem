package dev.salemlift.data.analytics

import dev.salemlift.domain.model.Landmarks
import dev.salemlift.domain.model.Muscle

/** One week of hard-set volume for a muscle: what was logged vs what was prescribed. */
data class WeekVolume(
    /** 1-based week within the mesocycle. */
    val week: Int,
    /** COUNT of logged_set rows crediting the muscle that week (1 set = 1 credit). */
    val performedSets: Int,
    /** SUM of the week's session_muscle_target rows for the muscle. */
    val prescribedSets: Int,
)

/** Best-set estimated 1RM for one completed session of an exercise. */
data class E1rmPoint(
    val sessionId: Long,
    /** 1-based week within the mesocycle. */
    val week: Int,
    /** 0-based day within the week. */
    val dayIndex: Int,
    /** Session completion time; used to order points chronologically. */
    val loggedAtEpochMillis: Long,
    /** Max RIR-adjusted Epley e1RM over the session's sets (DOMAIN.md §8). */
    val e1rmKg: Double,
)

/** A pickable exercise that has logged history. */
data class ExerciseRef(
    val id: String,
    val name: String,
)

/** Σ weightKg × reps over all sets logged in one week. */
data class WeekTonnage(
    val week: Int,
    val tonnageKg: Double,
)

/** Where the active mesocycle currently stands. */
data class MesoProgress(
    val mesoId: Long,
    /** Week of the next pending/in-progress session (last week when all are done). */
    val currentWeek: Int,
    /** Accumulation weeks + the deload week. */
    val totalWeeks: Int,
    val isDeloadWeek: Boolean,
    /** The current week's prescribed effort (from planned_session). */
    val targetRir: Int,
    val maxRir: Int,
    val completedSessions: Int,
    /** Planned sessions excluding SKIPPED ones (dropped by an early deload). */
    val totalSessions: Int,
)

/** Fatigue-signal counts for one week, aggregated from committed feedback. */
data class WeekFatigue(
    val week: Int,
    val stillSoreCount: Int,
    val mildJointPainCount: Int,
    val significantJointPainCount: Int,
    val performanceDownCount: Int,
)

/** How often one autoregulation rule fired, with its rationale text. */
data class RuleFireCount(
    val ruleId: String,
    val rationale: String,
    val count: Int,
)

/** The fatigue dashboard's inputs: weekly signal counts + the rule-fire histogram. */
data class FatigueSummary(
    val weeks: List<WeekFatigue>,
    val ruleFires: List<RuleFireCount>,
)

/**
 * Read-only aggregations for the analytics screens. Implementations must not
 * recompute engine math — everything is either read straight from the tracker
 * tables or computed with :domain functions (SPEC.md Gate 4 reconciliation).
 */
interface AnalyticsRepository {
    /** The active mesocycle's id, or null when none is running. */
    suspend fun activeMesoId(): Long?

    /**
     * Weekly performed vs prescribed hard sets per muscle for a mesocycle.
     * Every muscle with any prescription or logged set gets a full 1..N week
     * series (zero-filled), so charts stay continuous. One logged set credits
     * its logged muscle only; secondary-muscle credit is a later refinement.
     */
    suspend fun weeklyVolume(mesoId: Long): Map<Muscle, List<WeekVolume>>

    /** Current volume landmarks per muscle (seeded on first read, user-tunable). */
    suspend fun landmarks(): Map<Muscle, Landmarks>

    /**
     * Best-set e1RM per committed session of one mesocycle, in commit order.
     * e1RM uses the domain's RIR-adjusted Epley function — never re-derived.
     */
    suspend fun e1rmTrend(
        exerciseId: String,
        mesoId: Long,
    ): List<E1rmPoint>

    /** Distinct exercises with logged sets, for the trend picker. */
    suspend fun exercisesWithHistory(): List<ExerciseRef>

    /** Weekly tonnage (Σ weightKg × reps) for a mesocycle. */
    suspend fun tonnage(mesoId: Long): List<WeekTonnage>

    /** Progress of the active mesocycle, or null when none is running. */
    suspend fun mesoProgress(): MesoProgress?

    /** Weekly fatigue signals + rule-fire histogram for a mesocycle. */
    suspend fun fatigue(mesoId: Long): FatigueSummary
}
