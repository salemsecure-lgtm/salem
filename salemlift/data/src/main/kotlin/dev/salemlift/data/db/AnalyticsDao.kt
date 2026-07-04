package dev.salemlift.data.db

import androidx.room.Dao
import androidx.room.Query
import dev.salemlift.domain.model.Muscle

/** One (week, muscle) hard-set count, either performed or prescribed. */
data class WeekMuscleSetsRow(
    val week: Int,
    val muscle: Muscle,
    val sets: Int,
)

/** A logged set joined with its session's position in the mesocycle. */
data class LoggedSetWithSessionRow(
    val sessionId: Long,
    val week: Int,
    val dayIndex: Int,
    val completedAtEpochMillis: Long?,
    val exerciseId: String,
    val weightKg: Double,
    val reps: Int,
    val rir: Int,
    val loggedAtEpochMillis: Long,
)

/** A distinct exercise appearing in the set log, with its display name. */
data class ExerciseRefRow(
    val id: String,
    val name: String,
)

/** Σ weight×reps for one week of a mesocycle. */
data class WeekTonnageRow(
    val week: Int,
    val tonnageKg: Double,
)

/** Per-week fatigue-signal counts aggregated from muscle_feedback. */
data class WeekFatigueRow(
    val week: Int,
    val stillSoreCount: Int,
    val mildJointPainCount: Int,
    val significantJointPainCount: Int,
    val performanceDownCount: Int,
)

/** How often one autoregulation rule fired across a mesocycle. */
data class RuleFireRow(
    val ruleId: String,
    val rationale: String,
    val count: Int,
)

/**
 * Read-only aggregation queries for the analytics surface. Every query reads
 * the same tracker tables the engine writes, so on-screen numbers reconcile
 * with logged data by construction (SPEC.md Gate 4).
 */
@Dao
interface AnalyticsDao {
    /**
     * Performed hard sets per (week, muscle): one logged set = 1 credit to its
     * logged muscle. Committed sessions only, consistent with the e1RM trend —
     * a session that is never committed must not inflate analytics.
     */
    @Query(
        "SELECT ps.week AS week, ls.muscle AS muscle, COUNT(*) AS sets " +
            "FROM logged_set ls JOIN planned_session ps ON ls.sessionId = ps.id " +
            "WHERE ps.mesoId = :mesoId AND ps.state = 'COMPLETED' GROUP BY ps.week, ls.muscle",
    )
    suspend fun performedSetsByWeek(mesoId: Long): List<WeekMuscleSetsRow>

    /** Prescribed hard sets per (week, muscle) from the planned session targets. */
    @Query(
        "SELECT ps.week AS week, t.muscle AS muscle, SUM(t.sets) AS sets " +
            "FROM session_muscle_target t JOIN planned_session ps ON t.sessionId = ps.id " +
            "WHERE ps.mesoId = :mesoId GROUP BY ps.week, t.muscle",
    )
    suspend fun prescribedSetsByWeek(mesoId: Long): List<WeekMuscleSetsRow>

    /** Highest week number planned for the mesocycle (0 when it has no sessions). */
    @Query("SELECT COALESCE(MAX(week), 0) FROM planned_session WHERE mesoId = :mesoId")
    suspend fun maxWeek(mesoId: Long): Int

    /** One exercise's sets across the meso's committed sessions, in commit order. */
    @Query(
        "SELECT ls.sessionId AS sessionId, ps.week AS week, ps.dayIndex AS dayIndex, " +
            "ps.completedAtEpochMillis AS completedAtEpochMillis, ls.exerciseId AS exerciseId, " +
            "ls.weightKg AS weightKg, ls.reps AS reps, ls.rir AS rir, " +
            "ls.loggedAtEpochMillis AS loggedAtEpochMillis " +
            "FROM logged_set ls JOIN planned_session ps ON ls.sessionId = ps.id " +
            "WHERE ls.exerciseId = :exerciseId AND ps.mesoId = :mesoId AND ps.state = 'COMPLETED' " +
            "ORDER BY ps.completedAtEpochMillis, ls.orderInSession, ls.id",
    )
    suspend fun completedSetsForExercise(
        exerciseId: String,
        mesoId: Long,
    ): List<LoggedSetWithSessionRow>

    /** Distinct exercises with logged history; names resolved via the catalog when present. */
    @Query(
        "SELECT DISTINCT ls.exerciseId AS id, COALESCE(e.name, ls.exerciseId) AS name " +
            "FROM logged_set ls LEFT JOIN exercises e ON e.id = ls.exerciseId " +
            "ORDER BY name",
    )
    suspend fun exercisesWithHistory(): List<ExerciseRefRow>

    /** Weekly tonnage: Σ weightKg × reps over all logged sets of the week. */
    @Query(
        "SELECT ps.week AS week, SUM(ls.weightKg * ls.reps) AS tonnageKg " +
            "FROM logged_set ls JOIN planned_session ps ON ls.sessionId = ps.id " +
            "WHERE ps.mesoId = :mesoId AND ps.state = 'COMPLETED' GROUP BY ps.week ORDER BY ps.week",
    )
    suspend fun tonnageByWeek(mesoId: Long): List<WeekTonnageRow>

    @Query(
        "SELECT COUNT(*) FROM planned_session WHERE mesoId = :mesoId AND state != 'SKIPPED'",
    )
    suspend fun plannedSessionCount(mesoId: Long): Int

    @Query(
        "SELECT COUNT(*) FROM planned_session WHERE mesoId = :mesoId AND state = 'COMPLETED'",
    )
    suspend fun completedSessionCount(mesoId: Long): Int

    /** Fatigue-signal counts per week from committed feedback. */
    @Query(
        "SELECT ps.week AS week, " +
            "SUM(CASE WHEN f.soreness = 'STILL_SORE' THEN 1 ELSE 0 END) AS stillSoreCount, " +
            "SUM(CASE WHEN f.jointPain = 'MILD' THEN 1 ELSE 0 END) AS mildJointPainCount, " +
            "SUM(CASE WHEN f.jointPain = 'SIGNIFICANT' THEN 1 ELSE 0 END) AS significantJointPainCount, " +
            "SUM(CASE WHEN f.performance = 'DOWN' THEN 1 ELSE 0 END) AS performanceDownCount " +
            "FROM muscle_feedback f JOIN planned_session ps ON f.sessionId = ps.id " +
            "WHERE ps.mesoId = :mesoId GROUP BY ps.week ORDER BY ps.week",
    )
    suspend fun fatigueByWeek(mesoId: Long): List<WeekFatigueRow>

    /** Histogram of which decision rules fired, with the shared rationale text. */
    @Query(
        "SELECT d.ruleId AS ruleId, MIN(d.rationale) AS rationale, COUNT(*) AS count " +
            "FROM decision d JOIN planned_session ps ON d.sessionId = ps.id " +
            "WHERE ps.mesoId = :mesoId GROUP BY d.ruleId ORDER BY d.ruleId",
    )
    suspend fun ruleFireCounts(mesoId: Long): List<RuleFireRow>
}
