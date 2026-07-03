package dev.salemlift.domain.program

import dev.salemlift.domain.model.DefaultLandmarks
import dev.salemlift.domain.model.MesoConfig
import dev.salemlift.domain.model.Muscle
import dev.salemlift.domain.model.SessionTemplate
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Phase 2 gate (SPEC §8): a full mesocycle generates with each muscle's weekly
 * volume inside its landmark range, for every shipped template.
 */
class ProgramBuilderTest {
    private val landmarks = DefaultLandmarks.seeds

    @Test
    fun `GATE - every template generates with weekly volume inside the landmark range`() {
        for (split in SplitTemplates.all) {
            val plan = ProgramBuilder.instantiate(split, MesoConfig(), landmarks)
            for (week in plan.weeks) {
                for (muscle in split.trainedMuscles) {
                    val volume = ProgramBuilder.weeklyVolume(week, muscle)
                    val marks = landmarks.getValue(muscle)
                    assertTrue(
                        volume in marks.mv..marks.mrv,
                        "${split.name} wk${week.week} $muscle: $volume outside [${marks.mv}, ${marks.mrv}]",
                    )
                }
            }
        }
    }

    @Test
    fun `week one prescribes exactly MEV for every muscle`() {
        for (split in SplitTemplates.all) {
            val plan = ProgramBuilder.instantiate(split, MesoConfig(), landmarks)
            for (muscle in split.trainedMuscles) {
                assertEquals(
                    landmarks.getValue(muscle).mev,
                    ProgramBuilder.weeklyVolume(plan.weeks.first(), muscle),
                    "${split.name} week 1 $muscle",
                )
            }
        }
    }

    @Test
    fun `deload week prescribes exactly MV for every muscle`() {
        val plan = ProgramBuilder.instantiate(SplitTemplates.fullBodyThreeDay, MesoConfig(), landmarks)
        val deload = plan.weeks.last()
        assertTrue(deload.isDeload)
        for (muscle in Muscle.entries) {
            assertEquals(landmarks.getValue(muscle).mv, ProgramBuilder.weeklyVolume(deload, muscle))
        }
    }

    @Test
    fun `session totals equal the planner target when the plus-two cap cannot bind`() {
        // The projection climbs +1/week, so the per-session +2 cap never binds
        // and weekly session sums must equal the planner's targets exactly.
        val config = MesoConfig(accumulationWeeks = 6)
        val plan = ProgramBuilder.instantiate(SplitTemplates.pplSixDay, config, landmarks)
        val mesoPlan = dev.salemlift.domain.engine.MesocyclePlanner.plan(config, landmarks)
        assertEquals(7, plan.weeks.size)
        for ((programWeek, planWeek) in plan.weeks.zip(mesoPlan.weeks)) {
            for (muscle in Muscle.entries) {
                assertEquals(
                    planWeek.setTargets.getValue(muscle),
                    ProgramBuilder.weeklyVolume(programWeek, muscle),
                    "$muscle wk${programWeek.week}",
                )
            }
        }
    }

    @Test
    fun `week-over-week per-session increases respect the plus-two cap`() {
        val plan = ProgramBuilder.instantiate(SplitTemplates.upperLowerFourDay, MesoConfig(), landmarks)
        val accumulation = plan.weeks.dropLast(1)
        for (weekIndex in 1 until accumulation.size) {
            val previous = accumulation[weekIndex - 1].sessions
            val current = accumulation[weekIndex].sessions
            for (sessionIndex in current.indices) {
                for ((muscle, sets) in current[sessionIndex].muscleSets) {
                    val before = previous[sessionIndex].muscleSets[muscle] ?: 0
                    assertTrue(
                        sets - before <= VolumeDistributor.MAX_SESSION_INCREASE,
                        "wk${weekIndex + 1} session $sessionIndex $muscle jumped $before -> $sets",
                    )
                }
            }
        }
    }

    @Test
    fun `zero-set sessions are omitted from the prescription map`() {
        // Full-body deload: front delts MV=2 spread over 3 sessions leaves one session empty.
        val plan = ProgramBuilder.instantiate(SplitTemplates.fullBodyThreeDay, MesoConfig(), landmarks)
        val deload = plan.weeks.last()
        val frontDeltSessions = deload.sessions.count { Muscle.FRONT_DELTS in it.muscleSets }
        assertEquals(2, frontDeltSessions)
    }

    @Test
    fun `custom split trains only its muscles and ignores extra landmarks`() {
        val armsOnly =
            SplitTemplates.custom(
                "Arms",
                listOf(
                    SessionTemplate("Arms A", listOf(Muscle.BICEPS, Muscle.TRICEPS)),
                    SessionTemplate("Arms B", listOf(Muscle.BICEPS, Muscle.TRICEPS)),
                ),
            )
        val plan = ProgramBuilder.instantiate(armsOnly, MesoConfig(), landmarks)
        for (week in plan.weeks) {
            assertTrue(week.sessions.all { it.muscleSets.keys.all { m -> m in armsOnly.trainedMuscles } })
        }
        assertEquals(
            landmarks.getValue(Muscle.BICEPS).mev,
            ProgramBuilder.weeklyVolume(plan.weeks.first(), Muscle.BICEPS),
        )
    }

    @Test
    fun `splits training muscles without landmarks are rejected`() {
        val partial = landmarks.filterKeys { it != Muscle.CHEST }
        assertThrows<IllegalArgumentException> {
            ProgramBuilder.instantiate(SplitTemplates.pplThreeDay, MesoConfig(), partial)
        }
    }
}
