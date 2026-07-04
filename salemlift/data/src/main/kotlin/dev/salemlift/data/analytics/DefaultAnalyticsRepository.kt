package dev.salemlift.data.analytics

import dev.salemlift.data.db.LoggedSetWithSessionRow
import dev.salemlift.data.db.SalemDatabase
import dev.salemlift.data.db.WeekMuscleSetsRow
import dev.salemlift.data.seedLandmarksIfEmpty
import dev.salemlift.data.toLandmarks
import dev.salemlift.domain.e1rm
import dev.salemlift.domain.model.Landmarks
import dev.salemlift.domain.model.Muscle

/**
 * Room-backed [AnalyticsRepository]. Pure reads: aggregation happens in SQL
 * ([dev.salemlift.data.db.AnalyticsDao]) over the same tables the tracker
 * writes, and the only computation done in Kotlin is the domain [e1rm]
 * function — so every number reconciles with logged data and engine output.
 */
class DefaultAnalyticsRepository(
    private val database: SalemDatabase,
) : AnalyticsRepository {
    private val analyticsDao get() = database.analyticsDao()
    private val landmarkDao get() = database.landmarkDao()
    private val mesoDao get() = database.mesocycleDao()
    private val sessionDao get() = database.plannedSessionDao()

    override suspend fun activeMesoId(): Long? = mesoDao.getActive()?.id

    override suspend fun weeklyVolume(mesoId: Long): Map<Muscle, List<WeekVolume>> {
        val performed = analyticsDao.performedSetsByWeek(mesoId).toLookup()
        val prescribed = analyticsDao.prescribedSetsByWeek(mesoId).toLookup()
        val weeks = analyticsDao.maxWeek(mesoId)
        if (weeks == 0) return emptyMap()
        val muscles = performed.keys.map { it.second } + prescribed.keys.map { it.second }
        return muscles.distinct().sortedBy { it.ordinal }.associateWith { muscle ->
            (1..weeks).map { week ->
                WeekVolume(
                    week = week,
                    performedSets = performed[week to muscle] ?: 0,
                    prescribedSets = prescribed[week to muscle] ?: 0,
                )
            }
        }
    }

    override suspend fun landmarks(): Map<Muscle, Landmarks> {
        seedLandmarksIfEmpty(landmarkDao)
        return landmarkDao.getAll().associate { it.muscle to it.toLandmarks() }
    }

    override suspend fun e1rmTrend(
        exerciseId: String,
        mesoId: Long,
    ): List<E1rmPoint> =
        analyticsDao
            .completedSetsForExercise(exerciseId, mesoId)
            .groupBy { it.sessionId }
            .mapNotNull { (sessionId, rows) -> bestSetPoint(sessionId, rows) }
            .sortedBy { it.loggedAtEpochMillis }

    override suspend fun exercisesWithHistory(): List<ExerciseRef> =
        analyticsDao.exercisesWithHistory().map { ExerciseRef(id = it.id, name = it.name) }

    override suspend fun tonnage(mesoId: Long): List<WeekTonnage> =
        analyticsDao.tonnageByWeek(mesoId).map { WeekTonnage(week = it.week, tonnageKg = it.tonnageKg) }

    override suspend fun mesoProgress(): MesoProgress? {
        val meso = mesoDao.getActive() ?: return null
        val current =
            sessionDao.getCurrent(meso.id)
                ?: sessionDao.getAllForMeso(meso.id).lastOrNull()
                ?: return null
        return MesoProgress(
            mesoId = meso.id,
            currentWeek = current.week,
            totalWeeks = meso.accumulationWeeks + 1,
            isDeloadWeek = current.isDeload,
            targetRir = current.targetRir,
            maxRir = current.maxRir,
            completedSessions = analyticsDao.completedSessionCount(meso.id),
            totalSessions = analyticsDao.plannedSessionCount(meso.id),
        )
    }

    override suspend fun fatigue(mesoId: Long): FatigueSummary =
        FatigueSummary(
            weeks =
                analyticsDao.fatigueByWeek(mesoId).map {
                    WeekFatigue(
                        week = it.week,
                        stillSoreCount = it.stillSoreCount,
                        mildJointPainCount = it.mildJointPainCount,
                        significantJointPainCount = it.significantJointPainCount,
                        performanceDownCount = it.performanceDownCount,
                    )
                },
            ruleFires =
                analyticsDao.ruleFireCounts(mesoId).map {
                    RuleFireCount(ruleId = it.ruleId, rationale = it.rationale, count = it.count)
                },
        )

    /** Best-set e1RM for one session; sets outside the domain preconditions are skipped. */
    private fun bestSetPoint(
        sessionId: Long,
        rows: List<LoggedSetWithSessionRow>,
    ): E1rmPoint? {
        val best =
            rows
                .filter { it.weightKg > 0 && it.reps > 0 && it.rir >= 0 }
                .maxOfOrNull { e1rm(weightKg = it.weightKg, reps = it.reps, rir = it.rir) }
                ?: return null
        val first = rows.first()
        return E1rmPoint(
            sessionId = sessionId,
            week = first.week,
            dayIndex = first.dayIndex,
            loggedAtEpochMillis = first.completedAtEpochMillis ?: rows.maxOf { it.loggedAtEpochMillis },
            e1rmKg = best,
        )
    }

    private fun List<WeekMuscleSetsRow>.toLookup(): Map<Pair<Int, Muscle>, Int> =
        associate { (it.week to it.muscle) to it.sets }
}
