package dev.salemlift.data.analytics

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.salemlift.data.DefaultTrainingRepository
import dev.salemlift.data.FeedbackDraft
import dev.salemlift.data.LoggedSet
import dev.salemlift.data.db.SalemDatabase
import dev.salemlift.domain.model.DefaultLandmarks
import dev.salemlift.domain.model.JointPain
import dev.salemlift.domain.model.MesoConfig
import dev.salemlift.domain.model.Muscle
import dev.salemlift.domain.model.Performance
import dev.salemlift.domain.model.Pump
import dev.salemlift.domain.model.SetDecision
import dev.salemlift.domain.model.Soreness
import dev.salemlift.domain.program.SplitTemplates
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
 * Gate-4 reconciliation tests: a KNOWN set/feedback pattern is logged through
 * [DefaultTrainingRepository] (real Room, injected clock), then every analytics
 * aggregate is asserted against HAND-COMPUTED values.
 *
 * Hand-computed scenario (split = Upper/Lower 4-day, MesoConfig() → 4+1 weeks;
 * landmarks: CHEST mv4/mev8/mrv22, BICEPS mv3/mev6/mrv26, QUADS mv4/mev8/mrv20):
 *
 * Week 1 Upper A: bench-press 100×8@3, 100×8@3, 102.5×6@2 (CHEST);
 *   curl 40×10@2 ×2 (BICEPS). Commit: CHEST → R7 (+1 → wk2 9);
 *   BICEPS STILL_SORE+DOWN+MILD → R2 (−1 → wk2 5).
 * Week 1 Lower A: squat 120×5@2 ×2 (QUADS). Commit: QUADS SIGNIFICANT pain →
 *   R1 (−1 → wk2 7).
 * Week 1 Upper B: bench-press 100×8@2. Commit: no feedback.
 * Week 1 Lower B: nothing logged. Commit: no feedback.
 * Week 2 Upper A: bench-press 105×8@2, 105×6@1. Commit: no feedback.
 *
 * Expected:
 * - e1RM (weight × (1 + (reps+RIR)/30)), best set per completed session:
 *   wk1 UpperA max(136.667, 136.667, 129.833) = 136.667; wk1 UpperB 133.333;
 *   wk2 UpperA max(140.0, 129.5) = 140.0.
 * - Tonnage: wk1 = 2215 + 800 + 1200 + 800 = 5015; wk2 = 840 + 630 = 1470.
 * - Weekly volume (performed/prescribed): CHEST 4/8, 2/9, 0/10, 0/11, 0/4;
 *   BICEPS 2/6, 0/5, 0/8, 0/9, 0/3; QUADS 2/8, 0/7, 0/10, 0/11, 0/4
 *   (weeks 3-4 = planner projection MEV+2/MEV+3, week 5 = deload at MV).
 * - Fatigue wk1: stillSore 1, mild 1, significant 1, perfDown 1; rules R1/R2/R7 ×1.
 * - Progress: week 2 of 5, RIR 2, 5/20 sessions completed.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DefaultAnalyticsRepositoryTest {
    private lateinit var database: SalemDatabase
    private lateinit var tracker: DefaultTrainingRepository
    private lateinit var analytics: DefaultAnalyticsRepository
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
        tracker = DefaultTrainingRepository(database) { now }
        analytics = DefaultAnalyticsRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `analytics aggregates reconcile with a hand-computed training log`() {
        runBlocking {
            val seeded = seedScenario()
            assertEquals(seeded.mesoId, analytics.activeMesoId())
            assertWeeklyVolume(seeded.mesoId)
            assertE1rmTrend(seeded.mesoId)
            assertTonnageAndExercises(seeded.mesoId)
            assertFatigue(seeded)
            assertEquals(
                MesoProgress(
                    mesoId = seeded.mesoId,
                    currentWeek = 2,
                    totalWeeks = 5,
                    isDeloadWeek = false,
                    targetRir = 2,
                    maxRir = 2,
                    completedSessions = 5,
                    totalSessions = 20,
                ),
                analytics.mesoProgress(),
            )
        }
    }

    private suspend fun assertWeeklyVolume(mesoId: Long) {
        val volume = analytics.weeklyVolume(mesoId)
        assertEquals(split.trainedMuscles, volume.keys)
        assertEquals(
            listOf(
                WeekVolume(1, performedSets = 4, prescribedSets = 8),
                WeekVolume(2, performedSets = 2, prescribedSets = 9),
                WeekVolume(3, performedSets = 0, prescribedSets = 10),
                WeekVolume(4, performedSets = 0, prescribedSets = 11),
                WeekVolume(5, performedSets = 0, prescribedSets = 4),
            ),
            volume[Muscle.CHEST],
        )
        assertEquals(
            listOf(
                WeekVolume(1, performedSets = 2, prescribedSets = 6),
                WeekVolume(2, performedSets = 0, prescribedSets = 5),
                WeekVolume(3, performedSets = 0, prescribedSets = 8),
                WeekVolume(4, performedSets = 0, prescribedSets = 9),
                WeekVolume(5, performedSets = 0, prescribedSets = 3),
            ),
            volume[Muscle.BICEPS],
        )
        assertEquals(
            listOf(
                WeekVolume(1, performedSets = 2, prescribedSets = 8),
                WeekVolume(2, performedSets = 0, prescribedSets = 7),
                WeekVolume(3, performedSets = 0, prescribedSets = 10),
                WeekVolume(4, performedSets = 0, prescribedSets = 11),
                WeekVolume(5, performedSets = 0, prescribedSets = 4),
            ),
            volume[Muscle.QUADS],
        )
    }

    private suspend fun assertE1rmTrend(mesoId: Long) {
        val trend = analytics.e1rmTrend(BENCH, mesoId)
        assertEquals(3, trend.size)
        assertEquals(listOf(1 to 0, 1 to 2, 2 to 0), trend.map { it.week to it.dayIndex })
        // Hand-computed: 100x(1+11/30), 100x(1+10/30), 105x(1+10/30).
        assertEquals(100.0 * 41 / 30, trend[0].e1rmKg, TOLERANCE)
        assertEquals(100.0 * 40 / 30, trend[1].e1rmKg, TOLERANCE)
        assertEquals(140.0, trend[2].e1rmKg, TOLERANCE)
        // Ordered by the injected clock's commit times.
        assertEquals(
            listOf(START_MILLIS + HOUR, START_MILLIS + 3 * HOUR, START_MILLIS + 5 * HOUR),
            trend.map { it.loggedAtEpochMillis },
        )
    }

    private suspend fun assertTonnageAndExercises(mesoId: Long) {
        assertEquals(
            listOf(ExerciseRef(SQUAT, SQUAT), ExerciseRef(BENCH, BENCH), ExerciseRef(CURL, CURL)),
            analytics.exercisesWithHistory(),
        )
        val tonnage = analytics.tonnage(mesoId)
        assertEquals(listOf(1, 2), tonnage.map { it.week })
        // wk1: (800+800+615) chest + 800 biceps + 1200 quads + 800 chest = 5015.
        assertEquals(5015.0, tonnage[0].tonnageKg, TOLERANCE)
        // wk2: 840 + 630 = 1470.
        assertEquals(1470.0, tonnage[1].tonnageKg, TOLERANCE)
    }

    private suspend fun assertFatigue(seeded: Seeded) {
        val fatigue = analytics.fatigue(seeded.mesoId)
        assertEquals(
            listOf(
                WeekFatigue(
                    week = 1,
                    stillSoreCount = 1,
                    mildJointPainCount = 1,
                    significantJointPainCount = 1,
                    performanceDownCount = 1,
                ),
            ),
            fatigue.weeks,
        )
        // Rule histogram carries the engine's own rationale strings.
        assertEquals(
            listOf(
                RuleFireCount("R1", seeded.quadsDecision.rationale, 1),
                RuleFireCount("R2", seeded.bicepsDecision.rationale, 1),
                RuleFireCount("R7", seeded.chestDecision.rationale, 1),
            ),
            fatigue.ruleFires,
        )
    }

    @Test
    fun `e1rmTrend ignores sets logged in sessions that were never committed`() {
        runBlocking {
            seedScenario()
            // A bench set in the still-pending week-2 Lower A session must not appear.
            val pending = assertNotNull(tracker.currentSession().first())
            log(pending.sessionId, BENCH, Muscle.CHEST, weightKg = 200.0, reps = 1, rir = 0)

            val mesoId = assertNotNull(analytics.activeMesoId())
            assertEquals(3, analytics.e1rmTrend(BENCH, mesoId).size)
            // Volume and tonnage are committed-sessions-only too — the pending
            // 200 kg set must not inflate either (consistency with the trend).
            val chestWeek2 = analytics.weeklyVolume(mesoId).getValue(Muscle.CHEST).first { it.week == 2 }
            assertEquals(2, chestWeek2.performedSets)
            assertEquals(1470.0, analytics.tonnage(mesoId).first { it.week == 2 }.tonnageKg, TOLERANCE)
        }
    }

    @Test
    fun `everything is empty or null when no mesocycle exists`() {
        runBlocking {
            assertNull(analytics.activeMesoId())
            assertNull(analytics.mesoProgress())
            assertEquals(emptyMap(), analytics.weeklyVolume(mesoId = 1))
            assertEquals(emptyList(), analytics.e1rmTrend(BENCH, mesoId = 1))
            assertEquals(emptyList(), analytics.exercisesWithHistory())
            assertEquals(emptyList(), analytics.tonnage(mesoId = 1))
            assertEquals(FatigueSummary(weeks = emptyList(), ruleFires = emptyList()), analytics.fatigue(mesoId = 1))
        }
    }

    @Test
    fun `landmarks read seeds the defaults exactly like the tracker`() {
        runBlocking {
            assertEquals(DefaultLandmarks.seeds, analytics.landmarks())
        }
    }

    private data class Seeded(
        val mesoId: Long,
        val chestDecision: SetDecision,
        val bicepsDecision: SetDecision,
        val quadsDecision: SetDecision,
    )

    /** Runs the documented scenario, asserting the engine fired R7/R2/R1 as designed. */
    private suspend fun seedScenario(): Seeded {
        val mesoId = tracker.startMesocycle(split, MesoConfig())
        val (chestDecision, bicepsDecision) = commitWeek1UpperA()
        val quadsDecision = commitWeek1LowerA()

        // Week 1, Upper B: one bench set, no feedback.
        val upperB1 = assertNotNull(tracker.currentSession().first())
        log(upperB1.sessionId, BENCH, Muscle.CHEST, weightKg = 100.0, reps = 8, rir = 2)
        now += HOUR
        tracker.commitSession(upperB1.sessionId, emptyList())

        // Week 1, Lower B: nothing logged.
        val lowerB1 = assertNotNull(tracker.currentSession().first())
        now += HOUR
        tracker.commitSession(lowerB1.sessionId, emptyList())

        // Week 2, Upper A.
        val upperA2 = assertNotNull(tracker.currentSession().first())
        assertEquals(2, upperA2.week)
        log(upperA2.sessionId, BENCH, Muscle.CHEST, weightKg = 105.0, reps = 8, rir = 2)
        log(upperA2.sessionId, BENCH, Muscle.CHEST, weightKg = 105.0, reps = 6, rir = 1)
        now += HOUR
        tracker.commitSession(upperA2.sessionId, emptyList())

        assertTrue(assertNotNull(tracker.currentSession().first()).week == 2)
        return Seeded(mesoId, chestDecision, bicepsDecision, quadsDecision)
    }

    /** Week 1, Upper A: chest R7 (+1), biceps R2 (−1, mild pain). */
    private suspend fun commitWeek1UpperA(): Pair<SetDecision, SetDecision> {
        val upperA1 = assertNotNull(tracker.currentSession().first())
        log(upperA1.sessionId, BENCH, Muscle.CHEST, weightKg = 100.0, reps = 8, rir = 3)
        log(upperA1.sessionId, BENCH, Muscle.CHEST, weightKg = 100.0, reps = 8, rir = 3)
        log(upperA1.sessionId, BENCH, Muscle.CHEST, weightKg = 102.5, reps = 6, rir = 2)
        log(upperA1.sessionId, CURL, Muscle.BICEPS, weightKg = 40.0, reps = 10, rir = 2)
        log(upperA1.sessionId, CURL, Muscle.BICEPS, weightKg = 40.0, reps = 10, rir = 2)
        now += HOUR
        val outcome =
            tracker.commitSession(
                upperA1.sessionId,
                listOf(
                    FeedbackDraft(
                        muscle = Muscle.CHEST,
                        soreness = Soreness.RECOVERED_ON_TIME,
                        pump = Pump.MODERATE,
                        jointPain = JointPain.NONE,
                        performance = Performance.SAME,
                    ),
                    FeedbackDraft(
                        muscle = Muscle.BICEPS,
                        soreness = Soreness.STILL_SORE,
                        pump = Pump.LOW,
                        jointPain = JointPain.MILD,
                        performance = Performance.DOWN,
                    ),
                ),
            )
        val chestDecision = assertNotNull(outcome.decisions[Muscle.CHEST])
        val bicepsDecision = assertNotNull(outcome.decisions[Muscle.BICEPS])
        assertEquals("R7", chestDecision.ruleId)
        assertEquals("R2", bicepsDecision.ruleId)
        return chestDecision to bicepsDecision
    }

    /** Week 1, Lower A: quads R1 (−1, significant joint pain). */
    private suspend fun commitWeek1LowerA(): SetDecision {
        val lowerA1 = assertNotNull(tracker.currentSession().first())
        log(lowerA1.sessionId, SQUAT, Muscle.QUADS, weightKg = 120.0, reps = 5, rir = 2)
        log(lowerA1.sessionId, SQUAT, Muscle.QUADS, weightKg = 120.0, reps = 5, rir = 2)
        now += HOUR
        val outcome =
            tracker.commitSession(
                lowerA1.sessionId,
                listOf(
                    FeedbackDraft(
                        muscle = Muscle.QUADS,
                        soreness = Soreness.NEVER_SORE,
                        pump = Pump.LOW,
                        jointPain = JointPain.SIGNIFICANT,
                        performance = Performance.SAME,
                    ),
                ),
            )
        val quadsDecision = assertNotNull(outcome.decisions[Muscle.QUADS])
        assertEquals("R1", quadsDecision.ruleId)
        return quadsDecision
    }

    /** Order within the session is assigned automatically per session. */
    private suspend fun log(
        sessionId: Long,
        exerciseId: String,
        muscle: Muscle,
        weightKg: Double,
        reps: Int,
        rir: Int = 2,
    ) {
        val order = orderBySession.getOrDefault(sessionId, 0)
        orderBySession[sessionId] = order + 1
        tracker.logSet(
            LoggedSet(
                sessionId = sessionId,
                exerciseId = exerciseId,
                muscle = muscle,
                weightKg = weightKg,
                reps = reps,
                rir = rir,
                orderInSession = order,
                loggedAtEpochMillis = now,
            ),
        )
    }

    private val orderBySession = mutableMapOf<Long, Int>()

    private companion object {
        const val START_MILLIS = 1_720_000_000_000L
        const val HOUR = 3_600_000L
        const val TOLERANCE = 1e-9
        const val BENCH = "bench-press"
        const val CURL = "dumbbell-curl"
        const val SQUAT = "back-squat"
    }
}
