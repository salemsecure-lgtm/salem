package dev.salemlift.data

import androidx.room.withTransaction
import dev.salemlift.data.db.MesocycleEntity
import dev.salemlift.data.db.PlannedSessionEntity
import dev.salemlift.data.db.SalemDatabase
import dev.salemlift.data.db.SessionMuscleTargetEntity
import dev.salemlift.data.db.TrackerCodecs
import dev.salemlift.domain.model.DeloadReason
import dev.salemlift.domain.model.Landmarks
import dev.salemlift.domain.model.MesoConfig
import dev.salemlift.domain.model.Muscle
import dev.salemlift.domain.model.SetDecision
import dev.salemlift.domain.model.Split
import dev.salemlift.domain.program.ProgramBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Room-backed [TrainingRepository]. Composes the pure :domain engine
 * ([ProgramBuilder] to instantiate mesocycles,
 * [dev.salemlift.domain.program.SessionAdvancer] to advance after each
 * committed session) with the tracker tables in [SalemDatabase]; it contains
 * no training logic of its own.
 *
 * Week-over-week semantics: a committed session's decisions update each
 * fed-back muscle's [dev.salemlift.data.db.MuscleWeekStateEntity] immediately,
 * but the new distribution is materialized only onto the NEXT week's
 * accumulation sessions — the current week's remaining sessions keep the
 * targets they were planned with, and deload-week targets stay fixed at MV
 * (DOMAIN.md §3: no autoregulation during a deload).
 *
 * @param nowEpochMillis injected clock so tests are deterministic; production
 * wiring passes `System::currentTimeMillis` (see [DatabaseProvider]).
 */
class DefaultTrainingRepository(
    private val database: SalemDatabase,
    private val nowEpochMillis: () -> Long,
) : TrainingRepository {
    private val landmarkDao get() = database.landmarkDao()
    private val mesoDao get() = database.mesocycleDao()
    private val sessionDao get() = database.plannedSessionDao()
    private val targetDao get() = database.sessionMuscleTargetDao()
    private val weekStateDao get() = database.muscleWeekStateDao()
    private val loggedSetDao get() = database.loggedSetDao()
    private val feedbackDao get() = database.muscleFeedbackDao()
    private val decisionDao get() = database.decisionDao()

    override fun landmarks(): Flow<Map<Muscle, Landmarks>> =
        flow {
            seedLandmarksIfEmpty(landmarkDao)
            emitAll(
                landmarkDao.observeAll().map { rows ->
                    rows.associate { it.muscle to it.toLandmarks() }
                },
            )
        }

    override suspend fun startMesocycle(
        split: Split,
        config: MesoConfig,
    ): Long =
        database.withTransaction {
            seedLandmarksIfEmpty(landmarkDao)
            val landmarks = landmarkDao.getAll().associate { it.muscle to it.toLandmarks() }
            val plan = ProgramBuilder.instantiate(split, config, landmarks)

            mesoDao.completeAllActive()
            val mesoId =
                mesoDao.insert(
                    MesocycleEntity(
                        startedAtEpochMillis = nowEpochMillis(),
                        accumulationWeeks = config.accumulationWeeks,
                        deloadLoadMultiplier = config.deloadLoadMultiplier,
                        splitJson = TrackerCodecs.encodeSplit(split),
                    ),
                )
            plan.weeks.forEach { week ->
                week.sessions.forEachIndexed { dayIndex, planned ->
                    val sessionId =
                        sessionDao.insert(
                            PlannedSessionEntity(
                                mesoId = mesoId,
                                week = week.week,
                                dayIndex = dayIndex,
                                name = planned.name,
                                isDeload = week.isDeload,
                                targetRir = week.effort.targetRir,
                                allowZeroOnLastSet = week.effort.allowZeroOnLastSet,
                                maxRir = week.effort.maxRir,
                                loadMultiplier = week.loadMultiplier,
                            ),
                        )
                    targetDao.upsertAll(
                        planned.muscleSets.map { (muscle, sets) ->
                            SessionMuscleTargetEntity(sessionId = sessionId, muscle = muscle, sets = sets)
                        },
                    )
                }
            }
            weekStateDao.upsertAll(initialWeekStates(mesoId, split, plan.weeks.first().sessions))
            mesoId
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun currentSession(): Flow<SessionSummary?> =
        mesoDao
            .observeActive()
            .flatMapLatest { meso ->
                if (meso == null) {
                    flowOf(null)
                } else {
                    sessionDao.observeCurrent(meso.id).flatMapLatest { session ->
                        if (session == null) {
                            flowOf(null)
                        } else {
                            targetDao.observeFor(session.id).map { targets -> session.toSummary(targets) }
                        }
                    }
                }
            }.distinctUntilChanged()

    override suspend fun markSessionStarted(sessionId: Long) {
        sessionDao.markStarted(sessionId)
    }

    override fun loggedSets(sessionId: Long): Flow<List<LoggedSet>> =
        loggedSetDao.observeFor(sessionId).map { rows -> rows.map { it.toModel() } }

    override suspend fun logSet(set: LoggedSet): Long = loggedSetDao.insert(set.toEntity())

    override suspend fun deleteSet(id: Long) {
        loggedSetDao.deleteById(id)
    }

    override suspend fun commitSession(
        sessionId: Long,
        feedback: List<FeedbackDraft>,
        manualDeloadRequest: Boolean,
    ): CommitOutcome =
        database.withTransaction {
            val session = requireNotNull(sessionDao.getById(sessionId)) { "unknown session $sessionId" }
            require(session.state == SessionState.PENDING || session.state == SessionState.IN_PROGRESS) {
                "session $sessionId is already ${session.state}"
            }
            val meso = requireNotNull(mesoDao.getById(session.mesoId)) { "unknown mesocycle ${session.mesoId}" }

            feedbackDao.upsertAll(feedback.map { it.toEntity(sessionId) })

            val advance = runAdvance(database, session, meso, feedback, manualDeloadRequest)
            decisionDao.upsertAll(advance.decisions.map { (muscle, decision) -> decision.toEntity(sessionId, muscle) })
            weekStateDao.upsertAll(
                advance.nextStates.map { (muscle, state) -> state.toEntity(meso.id, muscle) },
            )
            applyDistributionsToNextWeek(database, meso, session.week + 1, advance)

            sessionDao.markCompleted(sessionId, nowEpochMillis())
            val earlyDeload =
                advance.deload.triggered && advance.deload.reasons.any { it != DeloadReason.PLANNED_END }
            if (earlyDeload) {
                sessionDao.skipPendingAccumulation(meso.id)
            }
            if (sessionDao.countAfter(meso.id, session.week, session.dayIndex) == 0) {
                mesoDao.markCompleted(meso.id)
            }

            CommitOutcome(
                advance = advance,
                decisions = advance.decisions,
                nextSession = currentSessionSnapshot(),
            )
        }

    override suspend fun sessionFor(sessionId: Long): SessionSummary? {
        val session = sessionDao.getById(sessionId) ?: return null
        return session.toSummary(targetDao.getFor(session.id))
    }

    override suspend fun decisionsFor(sessionId: Long): Map<Muscle, SetDecision> =
        decisionDao.getFor(sessionId).associate { it.muscle to it.toModel() }

    private suspend fun currentSessionSnapshot(): SessionSummary? {
        val meso = mesoDao.getActive() ?: return null
        val session = sessionDao.getCurrent(meso.id) ?: return null
        return session.toSummary(targetDao.getFor(session.id))
    }
}
