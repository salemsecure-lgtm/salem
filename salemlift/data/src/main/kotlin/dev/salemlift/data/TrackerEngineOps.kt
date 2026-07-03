package dev.salemlift.data

import dev.salemlift.data.db.LandmarkDao
import dev.salemlift.data.db.LandmarkEntity
import dev.salemlift.data.db.MesocycleEntity
import dev.salemlift.data.db.MuscleWeekStateEntity
import dev.salemlift.data.db.PlannedSessionEntity
import dev.salemlift.data.db.SalemDatabase
import dev.salemlift.data.db.SessionMuscleTargetEntity
import dev.salemlift.data.db.TrackerCodecs
import dev.salemlift.domain.model.DefaultLandmarks
import dev.salemlift.domain.model.MuscleFeedback
import dev.salemlift.domain.model.PlannedSession
import dev.salemlift.domain.model.SessionAdvance
import dev.salemlift.domain.model.Split
import dev.salemlift.domain.program.SessionAdvancer

// Transaction-scoped helpers composed by [DefaultTrainingRepository]; they hold
// no state and must be called inside an enclosing Room transaction.

/** Inserts the intermediate default landmarks (DOMAIN.md §2.1) when the table is empty. */
internal suspend fun seedLandmarksIfEmpty(landmarkDao: LandmarkDao) {
    if (landmarkDao.count() == 0) {
        landmarkDao.upsertAll(
            DefaultLandmarks.seeds.map { (muscle, landmarks) ->
                LandmarkEntity(
                    muscle = muscle,
                    mv = landmarks.mv,
                    mev = landmarks.mev,
                    mav = landmarks.mav,
                    mrv = landmarks.mrv,
                )
            },
        )
    }
}

/** Loads engine inputs from the tracker tables and runs [SessionAdvancer.advance]. */
internal suspend fun runAdvance(
    database: SalemDatabase,
    session: PlannedSessionEntity,
    meso: MesocycleEntity,
    feedback: List<FeedbackDraft>,
    manualDeloadRequest: Boolean,
): SessionAdvance {
    val landmarks = database.landmarkDao().getAll().associate { it.muscle to it.toLandmarks() }
    val states = database.muscleWeekStateDao().getFor(meso.id).associate { it.muscle to it.toModel() }
    val lastDayOfWeek = database.plannedSessionDao().getWeek(meso.id, session.week).maxOf { it.dayIndex }
    val finalAccumulationWeekComplete =
        !session.isDeload && session.week == meso.accumulationWeeks && session.dayIndex == lastDayOfWeek
    return SessionAdvancer.advance(
        states = states,
        landmarks = landmarks,
        feedback =
            feedback.associate {
                it.muscle to MuscleFeedback(it.soreness, it.performance, it.pump, it.jointPain)
            },
        finalAccumulationWeekComplete = finalAccumulationWeekComplete,
        manualDeloadRequest = manualDeloadRequest,
    )
}

/**
 * Materializes each fed-back muscle's new distribution onto next week's
 * accumulation sessions (week-over-week semantics: the current week's remaining
 * sessions keep their planned targets, and deload weeks stay fixed at MV).
 * Distribution slot order = the split's session indices that train the muscle,
 * matching [dev.salemlift.domain.program.ProgramBuilder] and [SessionAdvancer];
 * a slot's target row is upserted when its sets are > 0 and removed when 0.
 */
internal suspend fun applyDistributionsToNextWeek(
    database: SalemDatabase,
    meso: MesocycleEntity,
    nextWeek: Int,
    advance: SessionAdvance,
) {
    val targetDao = database.sessionMuscleTargetDao()
    val sessions = database.plannedSessionDao().getWeek(meso.id, nextWeek).filterNot { it.isDeload }
    if (sessions.isEmpty()) return
    val split = TrackerCodecs.decodeSplit(meso.splitJson)
    val sessionsByDay = sessions.associateBy { it.dayIndex }
    for (muscle in advance.decisions.keys) {
        val state = advance.nextStates[muscle] ?: continue
        val slots = split.sessions.indices.filter { muscle in split.sessions[it].muscles }
        slots.forEachIndexed { slotPosition, sessionIndex ->
            val target = sessionsByDay[sessionIndex] ?: return@forEachIndexed
            val sets = state.distribution.getOrElse(slotPosition) { 0 }
            if (sets > 0) {
                targetDao.upsertAll(
                    listOf(SessionMuscleTargetEntity(sessionId = target.id, muscle = muscle, sets = sets)),
                )
            } else {
                targetDao.delete(target.id, muscle)
            }
        }
    }
}

/** Week-1 per-muscle state: weekly sets at MEV, distribution = the plan's week-1 slots. */
internal fun initialWeekStates(
    mesoId: Long,
    split: Split,
    weekOneSessions: List<PlannedSession>,
): List<MuscleWeekStateEntity> =
    split.trainedMuscles.map { muscle ->
        val slots = split.sessions.indices.filter { muscle in split.sessions[it].muscles }
        val distribution = slots.map { weekOneSessions[it].muscleSets[muscle] ?: 0 }
        MuscleWeekStateEntity(
            mesoId = mesoId,
            muscle = muscle,
            weeklySets = distribution.sum(),
            distributionJson = TrackerCodecs.encodeDistribution(distribution),
            stallCount = 0,
        )
    }
