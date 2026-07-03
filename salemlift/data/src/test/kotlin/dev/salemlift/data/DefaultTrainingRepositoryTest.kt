package dev.salemlift.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.salemlift.data.db.MesoState
import dev.salemlift.data.db.SalemDatabase
import dev.salemlift.data.db.TrackerCodecs
import dev.salemlift.domain.model.DefaultLandmarks
import dev.salemlift.domain.model.DeloadReason
import dev.salemlift.domain.model.JointPain
import dev.salemlift.domain.model.MesoConfig
import dev.salemlift.domain.model.Muscle
import dev.salemlift.domain.model.Performance
import dev.salemlift.domain.model.Pump
import dev.salemlift.domain.model.Soreness
import dev.salemlift.domain.program.SplitTemplates
import dev.salemlift.domain.program.VolumeDistributor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Robolectric integration tests: [DefaultTrainingRepository] against a real
 * Room/SQLite database, JVM-only. JUnit4 (vintage engine) because Robolectric's
 * runner is JUnit4-based.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DefaultTrainingRepositoryTest {
    private lateinit var database: SalemDatabase
    private lateinit var repository: DefaultTrainingRepository
    private var now = START_MILLIS

    private val split = SplitTemplates.upperLowerFourDay
    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        database =
            Room
                .inMemoryDatabaseBuilder(context, SalemDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        repository = DefaultTrainingRepository(database) { now }
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ---- (f) Landmark seeding ------------------------------------------------

    @Test
    fun `landmarks are seeded from defaults on first read and seeding is idempotent`() {
        runBlocking {
            val first = repository.landmarks().first()
            assertEquals(DefaultLandmarks.seeds, first)

            // A second collection (and a second repository on the same DB) must not re-seed.
            val again = DefaultTrainingRepository(database) { now }.landmarks().first()
            assertEquals(DefaultLandmarks.seeds, again)
            assertEquals(Muscle.entries.size, database.landmarkDao().count())
        }
    }

    // ---- (a) startMesocycle persistence ---------------------------------------

    @Test
    fun `startMesocycle persists five weeks of sessions with MEV week-1 targets and initial state`() {
        runBlocking {
            val mesoId = repository.startMesocycle(split, MesoConfig())

            val sessions = database.plannedSessionDao().getAllForMeso(mesoId)
            assertEquals(WEEKS * split.sessions.size, sessions.size)
            assertTrue(sessions.filter { it.week == WEEKS }.all { it.isDeload })
            assertTrue(sessions.filter { it.week < WEEKS }.none { it.isDeload })
            assertTrue(sessions.all { it.state == SessionState.PENDING })

            // Week-1 weekly volume per muscle equals MEV (DOMAIN.md §3).
            val week1 = sessions.filter { it.week == 1 }
            for (muscle in split.trainedMuscles) {
                assertEquals(
                    DefaultLandmarks.seeds.getValue(muscle).mev,
                    week1.sumOf { targetSets(it.id, muscle) },
                    "week-1 volume for $muscle",
                )
            }

            // muscle_week_state rows: weeklySets = MEV, stall 0, distribution = week-1 slots.
            val states = database.muscleWeekStateDao().getFor(mesoId)
            assertEquals(split.trainedMuscles.size, states.size)
            for (state in states) {
                assertEquals(DefaultLandmarks.seeds.getValue(state.muscle).mev, state.weeklySets)
                assertEquals(0, state.stallCount)
                val slots = split.sessions.indices.filter { state.muscle in split.sessions[it].muscles }
                val expected = slots.map { day -> targetSets(week1.first { it.dayIndex == day }.id, state.muscle) }
                assertEquals(expected, TrackerCodecs.decodeDistribution(state.distributionJson))
            }
        }
    }

    @Test
    fun `startMesocycle completes any previously active mesocycle`() {
        runBlocking {
            val firstMeso = repository.startMesocycle(split, MesoConfig())
            val secondMeso = repository.startMesocycle(split, MesoConfig())

            assertEquals(secondMeso, assertNotNull(database.mesocycleDao().getActive()).id)
            assertEquals(secondMeso, assertNotNull(repository.currentSession().first()).mesoId)
            val first = assertNotNull(database.mesocycleDao().getById(firstMeso))
            assertEquals(MesoState.COMPLETED, first.state)
        }
    }

    // ---- (b) Full loop ---------------------------------------------------------

    @Test
    fun `full loop - log sets, commit R7 feedback, next week redistributed, current week untouched`() {
        runBlocking {
            val mesoId = repository.startMesocycle(split, MesoConfig())

            val current = assertNotNull(repository.currentSession().first())
            assertEquals(1, current.week)
            assertEquals(0, current.dayIndex)
            assertEquals("Upper A", current.name)
            assertEquals(SessionState.PENDING, current.state)

            repository.markSessionStarted(current.sessionId)
            assertEquals(
                SessionState.IN_PROGRESS,
                assertNotNull(repository.currentSession().first()).state,
            )

            logChestSets(current.sessionId, count = 3)
            assertEquals(3, repository.loggedSets(current.sessionId).first().size)

            // Snapshot the rest of week 1 before committing.
            val week1 = database.plannedSessionDao().getWeek(mesoId, 1)
            val upperB = week1.first { it.dayIndex == 2 }
            val upperBTargetsBefore = database.sessionMuscleTargetDao().getFor(upperB.id)

            val fedMuscles = current.muscleTargets.keys
            now += HOUR_MILLIS
            val outcome = repository.commitSession(current.sessionId, fedMuscles.map(::standardFeedback))

            // R7 (+1) for every fed-back muscle.
            assertEquals(fedMuscles, outcome.decisions.keys)
            assertTrue(outcome.decisions.values.all { it.ruleId == "R7" && it.rawDelta == 1 })

            // Next week's targets = +1 redistributed onto week 2's sessions.
            val week2 = database.plannedSessionDao().getWeek(mesoId, 2)
            for (muscle in fedMuscles) {
                val mev = DefaultLandmarks.seeds.getValue(muscle).mev
                val slots = split.sessions.indices.filter { muscle in split.sessions[it].muscles }
                val week1Distribution =
                    slots.map { day -> targetSets(week1.first { it.dayIndex == day }.id, muscle) }
                val expected = VolumeDistributor.redistribute(week1Distribution, mev + 1).perSession
                val actual = slots.map { day -> targetSets(week2.first { it.dayIndex == day }.id, muscle) }
                assertEquals(expected, actual, "week-2 distribution for $muscle")
                assertEquals(mev + 1, actual.sum())
            }

            // Current week's other sessions keep their planned targets.
            assertEquals(
                upperBTargetsBefore.toSet(),
                database.sessionMuscleTargetDao().getFor(upperB.id).toSet(),
            )

            // Session completed, currentSession advanced to week 1 day 1.
            val committed = assertNotNull(database.plannedSessionDao().getById(current.sessionId))
            assertEquals(SessionState.COMPLETED, committed.state)
            assertEquals(now, committed.completedAtEpochMillis)
            val next = assertNotNull(repository.currentSession().first())
            assertEquals(1, next.week)
            assertEquals(1, next.dayIndex)
            assertEquals("Lower A", next.name)
            assertEquals(next, outcome.nextSession)
        }
    }

    // ---- (c) Explainability ----------------------------------------------------

    @Test
    fun `decisionsFor round-trips persisted decisions exactly`() {
        runBlocking {
            repository.startMesocycle(split, MesoConfig())
            val current = assertNotNull(repository.currentSession().first())

            // Mixed feedback so flag/delta columns are exercised: a mild-pain cap
            // (R7 +1 capped to 0) and a still-sore regression (R2 −1).
            val feedback =
                current.muscleTargets.keys.map { muscle ->
                    when (muscle) {
                        Muscle.BICEPS ->
                            FeedbackDraft(
                                muscle = muscle,
                                soreness = Soreness.RECOVERED_ON_TIME,
                                pump = Pump.MODERATE,
                                jointPain = JointPain.MILD,
                                performance = Performance.SAME,
                            )
                        Muscle.CHEST ->
                            FeedbackDraft(
                                muscle = muscle,
                                soreness = Soreness.STILL_SORE,
                                pump = Pump.LOW,
                                jointPain = JointPain.NONE,
                                performance = Performance.DOWN,
                            )
                        else -> standardFeedback(muscle)
                    }
                }
            val outcome = repository.commitSession(current.sessionId, feedback)

            val biceps = assertNotNull(outcome.decisions[Muscle.BICEPS])
            assertTrue(biceps.mildPainCapped)
            assertEquals(1, biceps.rawDelta)
            assertEquals(0, biceps.cappedDelta)
            assertEquals("R2", assertNotNull(outcome.decisions[Muscle.CHEST]).ruleId)

            assertEquals(outcome.decisions, repository.decisionsFor(current.sessionId))
        }
    }

    // ---- (e) Early deload --------------------------------------------------------

    @Test
    fun `manual deload skips remaining accumulation sessions but keeps deload week pending`() {
        runBlocking {
            val mesoId = repository.startMesocycle(split, MesoConfig())
            val current = assertNotNull(repository.currentSession().first())

            val outcome =
                repository.commitSession(
                    sessionId = current.sessionId,
                    feedback = current.muscleTargets.keys.map(::standardFeedback),
                    manualDeloadRequest = true,
                )
            assertTrue(outcome.advance.deload.triggered)
            assertEquals(listOf(DeloadReason.MANUAL), outcome.advance.deload.reasons)

            val sessions = database.plannedSessionDao().getAllForMeso(mesoId)
            val (deload, accumulation) = sessions.partition { it.isDeload }
            assertTrue(deload.all { it.state == SessionState.PENDING })
            assertEquals(SessionState.COMPLETED, accumulation.first { it.id == current.sessionId }.state)
            assertTrue(accumulation.filter { it.id != current.sessionId }.all { it.state == SessionState.SKIPPED })

            // The mesocycle continues into its deload week.
            val next = assertNotNull(outcome.nextSession)
            assertTrue(next.isDeload)
            assertEquals(WEEKS, next.week)
            assertEquals(0, next.dayIndex)
            val meso = assertNotNull(database.mesocycleDao().getById(mesoId))
            assertEquals(MesoState.ACTIVE, meso.state)
        }
    }

    @Test
    fun `committing the final deload session completes the mesocycle`() {
        runBlocking {
            val mesoId = repository.startMesocycle(split, MesoConfig())
            var outcome: CommitOutcome? = null
            repeat(WEEKS * split.sessions.size) {
                val current = assertNotNull(repository.currentSession().first())
                outcome =
                    repository.commitSession(
                        sessionId = current.sessionId,
                        feedback = current.muscleTargets.keys.map(::standardFeedback),
                    )
            }
            assertNull(assertNotNull(outcome).nextSession)
            assertNull(repository.currentSession().first())
            val meso = assertNotNull(database.mesocycleDao().getById(mesoId))
            assertEquals(MesoState.COMPLETED, meso.state)
        }
    }

    @Test
    fun `deleteSet removes a logged set`() {
        runBlocking {
            repository.startMesocycle(split, MesoConfig())
            val current = assertNotNull(repository.currentSession().first())
            val id =
                repository.logSet(
                    LoggedSet(
                        sessionId = current.sessionId,
                        exerciseId = "row",
                        muscle = Muscle.BACK,
                        weightKg = 80.0,
                        reps = 10,
                        rir = 2,
                        orderInSession = 0,
                        loggedAtEpochMillis = now,
                    ),
                )
            assertEquals(1, repository.loggedSets(current.sessionId).first().size)
            repository.deleteSet(id)
            assertEquals(0, repository.loggedSets(current.sessionId).first().size)
        }
    }

    // ---- (d) Persistence across restart ------------------------------------------

    @Test
    fun `data survives closing and reopening the same database file`() {
        runBlocking {
            val fileName = "tracker-restart-test.db"
            context.deleteDatabase(fileName)
            val firstOpen = Room.databaseBuilder(context, SalemDatabase::class.java, fileName).build()
            val mesoId =
                try {
                    DefaultTrainingRepository(firstOpen) { now }.startMesocycle(split, MesoConfig())
                } finally {
                    firstOpen.close()
                }

            val secondOpen = Room.databaseBuilder(context, SalemDatabase::class.java, fileName).build()
            try {
                val reopenedRepository = DefaultTrainingRepository(secondOpen) { now }
                assertEquals(mesoId, assertNotNull(secondOpen.mesocycleDao().getActive()).id)
                assertEquals(
                    WEEKS * split.sessions.size,
                    secondOpen.plannedSessionDao().getAllForMeso(mesoId).size,
                )
                val current = assertNotNull(reopenedRepository.currentSession().first())
                assertEquals(1, current.week)
                assertEquals(0, current.dayIndex)
            } finally {
                secondOpen.close()
                context.deleteDatabase(fileName)
            }
        }
    }

    private suspend fun logChestSets(
        sessionId: Long,
        count: Int,
    ) {
        repeat(count) { order ->
            repository.logSet(
                LoggedSet(
                    sessionId = sessionId,
                    exerciseId = "bench-press",
                    muscle = Muscle.CHEST,
                    weightKg = 100.0,
                    reps = 8,
                    rir = 3,
                    orderInSession = order,
                    loggedAtEpochMillis = now,
                ),
            )
        }
    }

    private suspend fun targetSets(
        sessionId: Long,
        muscle: Muscle,
    ): Int = database.sessionMuscleTargetDao().getFor(sessionId).firstOrNull { it.muscle == muscle }?.sets ?: 0

    private fun standardFeedback(muscle: Muscle): FeedbackDraft =
        FeedbackDraft(
            muscle = muscle,
            soreness = Soreness.RECOVERED_ON_TIME,
            pump = Pump.MODERATE,
            jointPain = JointPain.NONE,
            performance = Performance.SAME,
        )

    private companion object {
        const val WEEKS = 5
        const val START_MILLIS = 1_720_000_000_000L
        const val HOUR_MILLIS = 3_600_000L
    }
}
