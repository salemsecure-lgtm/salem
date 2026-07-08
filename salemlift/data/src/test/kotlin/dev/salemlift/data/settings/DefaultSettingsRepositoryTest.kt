package dev.salemlift.data.settings

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.salemlift.data.DefaultTrainingRepository
import dev.salemlift.data.FeedbackDraft
import dev.salemlift.data.LoggedSet
import dev.salemlift.data.catalog.Equipment
import dev.salemlift.data.db.ExerciseEntity
import dev.salemlift.data.db.SalemDatabase
import dev.salemlift.domain.engine.DefaultRules
import dev.salemlift.domain.model.DefaultLandmarks
import dev.salemlift.domain.model.Experience
import dev.salemlift.domain.model.JointPain
import dev.salemlift.domain.model.Landmarks
import dev.salemlift.domain.model.MesoConfig
import dev.salemlift.domain.model.Muscle
import dev.salemlift.domain.model.Performance
import dev.salemlift.domain.model.Pump
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
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Robolectric integration tests: [DefaultSettingsRepository] against a real
 * Room/SQLite database, sharing tables with [DefaultTrainingRepository] so the
 * settings → engine wiring is exercised end to end.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DefaultSettingsRepositoryTest {
    private lateinit var database: SalemDatabase
    private lateinit var settings: DefaultSettingsRepository
    private lateinit var training: DefaultTrainingRepository
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
        settings = DefaultSettingsRepository(database)
        training = DefaultTrainingRepository(database) { now }
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ---- Landmarks -------------------------------------------------------------

    @Test
    fun `landmark update round-trips through the shared landmark table`() {
        runBlocking {
            val edited = Landmarks(mev = 10, mrv = 24, mv = 5, mav = 18)
            settings.updateLandmarks(Muscle.CHEST, edited)

            assertEquals(edited, settings.landmarks().first()[Muscle.CHEST])
            // The tracker reads the same rows.
            assertEquals(edited, training.landmarks().first()[Muscle.CHEST])
            // Other muscles keep their seeds.
            assertEquals(DefaultLandmarks.seeds[Muscle.BACK], settings.landmarks().first()[Muscle.BACK])
        }
    }

    @Test
    fun `invalid landmark ordering cannot be constructed so stored rows stay valid`() {
        runBlocking {
            settings.landmarks().first() // seed
            assertFailsWith<IllegalArgumentException> { Landmarks(mev = 8, mrv = 10, mv = 2, mav = 12) }
            assertFailsWith<IllegalArgumentException> { Landmarks(mev = 4, mrv = 20, mv = 6, mav = 12) }
            assertEquals(DefaultLandmarks.seeds, settings.landmarks().first())
        }
    }

    @Test
    fun `applyExperienceSeeds overwrites every muscle row`() {
        runBlocking {
            settings.updateLandmarks(Muscle.CHEST, Landmarks(mev = 12, mrv = 30, mv = 6, mav = 20))
            settings.applyExperienceSeeds(Experience.BEGINNER)
            assertEquals(DefaultLandmarks.seedsFor(Experience.BEGINNER), settings.landmarks().first())
        }
    }

    // ---- Rule overrides → engine -------------------------------------------------

    @Test
    fun `rule override changes what commitSession decides`() {
        runBlocking {
            // Tune R7 (default +1) to +2, then commit standard R7 feedback.
            settings.updateRuleDelta("R7", 2)
            training.startMesocycle(split, MesoConfig())
            val current = assertNotNull(training.currentSession().first())

            val outcome =
                training.commitSession(current.sessionId, current.muscleTargets.keys.map(::standardFeedback))

            assertTrue(outcome.decisions.isNotEmpty())
            outcome.decisions.forEach { (muscle, decision) ->
                assertEquals("R7", decision.ruleId, "rule for $muscle")
                assertEquals(2, decision.rawDelta, "tuned raw delta for $muscle")
                assertEquals(
                    DefaultLandmarks.seeds.getValue(muscle).mev + 2,
                    decision.nextSets,
                    "next sets for $muscle",
                )
            }
        }
    }

    @Test
    fun `resetRules restores the shipped deltas`() {
        runBlocking {
            settings.updateRuleDelta("R7", 3)
            assertEquals(3, RuleTables.fixedDelta(settings.ruleTable().first { it.id == "R7" }))

            settings.resetRules()

            assertEquals(1, RuleTables.fixedDelta(settings.ruleTable().first { it.id == "R7" }))
            settings.ruleTable().zip(DefaultRules.table).forEach { (tuned, default) ->
                assertEquals(RuleTables.fixedDelta(default), RuleTables.fixedDelta(tuned))
            }
        }
    }

    @Test
    fun `updateRuleDelta validates range editability and rule id`() {
        runBlocking {
            assertFailsWith<IllegalArgumentException> { settings.updateRuleDelta("R7", 4) }
            assertFailsWith<IllegalArgumentException> { settings.updateRuleDelta("R7", -4) }
            assertFailsWith<IllegalArgumentException> { settings.updateRuleDelta("R4", 2) }
            assertFailsWith<IllegalArgumentException> { settings.updateRuleDelta("R99", 1) }
            assertTrue(database.ruleOverrideDao().getAll().isEmpty())
        }
    }

    // ---- Rest pref -----------------------------------------------------------------

    @Test
    fun `rest pref defaults to 150 and round-trips`() {
        runBlocking {
            assertEquals(SettingsRepository.DEFAULT_REST_SECONDS, settings.restSeconds().first())
            settings.setRestSeconds(240)
            assertEquals(240, settings.restSeconds().first())
            assertFailsWith<IllegalArgumentException> { settings.setRestSeconds(29) }
            assertFailsWith<IllegalArgumentException> { settings.setRestSeconds(601) }
            assertEquals(240, settings.restSeconds().first())
        }
    }

    // ---- Backup/export -----------------------------------------------------------------

    @Test
    fun `export then wipe then import preserves a mid-meso state exactly`() {
        runBlocking {
            // Arrange a mid-meso state: tuned rule, rest pref, custom exercise,
            // one committed session with decisions, one in-progress session with sets.
            database.exerciseDao().insertAll(listOf(seededExercise(), customExercise()))
            settings.updateRuleDelta("R7", 2)
            settings.setRestSeconds(240)
            val mesoId = training.startMesocycle(split, MesoConfig())

            val first = assertNotNull(training.currentSession().first())
            training.commitSession(first.sessionId, first.muscleTargets.keys.map(::standardFeedback))
            val second = assertNotNull(training.currentSession().first())
            training.markSessionStarted(second.sessionId)
            training.logSet(loggedSet(second.sessionId, order = 0))
            training.logSet(loggedSet(second.sessionId, order = 1))

            val currentBefore = assertNotNull(training.currentSession().first())
            val setsBefore = training.loggedSets(second.sessionId).first()
            val decisionsBefore = training.decisionsFor(first.sessionId)
            val weekStatesBefore = database.muscleWeekStateDao().getFor(mesoId).toSet()
            val targetsBefore = database.sessionMuscleTargetDao().getAll().toSet()
            val landmarksBefore = settings.landmarks().first()

            val json = settings.exportBackup()
            assertTrue(json.contains(CUSTOM_EXERCISE_ID), "custom exercises must be exported")
            assertFalse(json.contains(SEEDED_EXERCISE_ID), "the seeded catalog must not be exported")

            // Wipe: clobber everything the backup covers.
            training.startMesocycle(split, MesoConfig())
            settings.applyExperienceSeeds(Experience.ADVANCED)
            settings.resetRules()
            settings.setRestSeconds(90)
            database.exerciseDao().deleteCustom()

            settings.importBackup(json)

            assertEquals(currentBefore, training.currentSession().first())
            assertEquals(setsBefore, training.loggedSets(second.sessionId).first())
            assertEquals(decisionsBefore, training.decisionsFor(first.sessionId))
            assertEquals(weekStatesBefore, database.muscleWeekStateDao().getFor(mesoId).toSet())
            assertEquals(targetsBefore, database.sessionMuscleTargetDao().getAll().toSet())
            assertEquals(landmarksBefore, settings.landmarks().first())
            assertEquals(240, settings.restSeconds().first())
            assertEquals(2, RuleTables.fixedDelta(settings.ruleTable().first { it.id == "R7" }))
            assertNotNull(database.exerciseDao().getById(CUSTOM_EXERCISE_ID), "custom exercise restored")
            assertNotNull(database.exerciseDao().getById(SEEDED_EXERCISE_ID), "seeded catalog untouched by import")
        }
    }

    @Test
    fun `importBackup rejects an unsupported version and leaves data unchanged`() {
        runBlocking {
            training.startMesocycle(split, MesoConfig())
            val before = assertNotNull(training.currentSession().first())
            assertFailsWith<IllegalArgumentException> { settings.importBackup("""{"version":99}""") }
            assertEquals(before, training.currentSession().first())
        }
    }

    @Test
    fun `importBackup rejects out-of-range or non-editable rule overrides before writing`() {
        runBlocking {
            settings.updateRuleDelta("R7", 2)
            val outOfRange = settings.exportBackup().replace(""""delta":2""", """"delta":9""")
            assertFailsWith<IllegalArgumentException> { settings.importBackup(outOfRange) }
            val nonEditable = settings.exportBackup().replace(""""ruleId":"R7"""", """"ruleId":"R4"""")
            assertFailsWith<IllegalArgumentException> { settings.importBackup(nonEditable) }
            // The pre-write validation left the original override intact.
            val r7 = settings.ruleTable().first { it.id == "R7" }
            assertEquals(2, r7.delta.compute(10, DefaultLandmarks.seeds.getValue(Muscle.CHEST)))
        }
    }

    // ---- Helpers -----------------------------------------------------------------

    private fun standardFeedback(muscle: Muscle): FeedbackDraft =
        FeedbackDraft(
            muscle = muscle,
            soreness = Soreness.RECOVERED_ON_TIME,
            pump = Pump.MODERATE,
            jointPain = JointPain.NONE,
            performance = Performance.SAME,
        )

    private fun loggedSet(
        sessionId: Long,
        order: Int,
    ): LoggedSet =
        LoggedSet(
            sessionId = sessionId,
            exerciseId = CUSTOM_EXERCISE_ID,
            muscle = Muscle.QUADS,
            weightKg = 100.0,
            reps = 8,
            rir = 2,
            orderInSession = order,
            loggedAtEpochMillis = now,
        )

    private fun customExercise(): ExerciseEntity =
        ExerciseEntity(
            id = CUSTOM_EXERCISE_ID,
            name = "Custom Hack Squat",
            primaryMuscle = Muscle.QUADS,
            secondaryMuscles = setOf(Muscle.GLUTES),
            equipment = Equipment.MACHINE,
            level = "beginner",
            category = "strength",
            cues = listOf("Brace before descending"),
            isCustom = true,
        )

    private fun seededExercise(): ExerciseEntity =
        ExerciseEntity(
            id = SEEDED_EXERCISE_ID,
            name = "Barbell Bench Press",
            primaryMuscle = Muscle.CHEST,
            secondaryMuscles = setOf(Muscle.TRICEPS),
            equipment = Equipment.BARBELL,
            level = "intermediate",
            category = "strength",
            cues = listOf("Feet planted"),
            isCustom = false,
        )

    private companion object {
        const val START_MILLIS = 1_720_000_000_000L
        const val CUSTOM_EXERCISE_ID = "custom-hack-squat"
        const val SEEDED_EXERCISE_ID = "seeded-bench-press"
    }
}
