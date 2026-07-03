package dev.salemlift.domain.program

import dev.salemlift.domain.model.DefaultLandmarks
import dev.salemlift.domain.model.DeloadReason
import dev.salemlift.domain.model.JointPain
import dev.salemlift.domain.model.Landmarks
import dev.salemlift.domain.model.Muscle
import dev.salemlift.domain.model.MuscleFeedback
import dev.salemlift.domain.model.MuscleWeekState
import dev.salemlift.domain.model.Performance
import dev.salemlift.domain.model.Pump
import dev.salemlift.domain.model.Soreness
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** DOMAIN.md §5/§5.2/§6 composed: commit a session → next-session states. */
class SessionAdvancerTest {
    private val landmarks = DefaultLandmarks.seeds

    private val standardFeedback =
        MuscleFeedback(Soreness.RECOVERED_ON_TIME, Performance.SAME, Pump.MODERATE, JointPain.NONE)

    @Test
    fun `standard week adds one set and redistributes freshest-first`() {
        val states = mapOf(Muscle.CHEST to MuscleWeekState(weeklySets = 8, distribution = listOf(4, 4)))
        val advance =
            SessionAdvancer.advance(states, landmarks, mapOf(Muscle.CHEST to standardFeedback))

        val decision = advance.decisions.getValue(Muscle.CHEST)
        assertEquals("R7", decision.ruleId)
        assertEquals(9, decision.nextSets)
        val next = advance.nextStates.getValue(Muscle.CHEST)
        assertEquals(9, next.weeklySets)
        assertEquals(listOf(5, 4), next.distribution)
        assertFalse(advance.deload.triggered)
        assertTrue(advance.cappedMuscles.isEmpty())
    }

    @Test
    fun `muscles without feedback carry their state over unchanged`() {
        val states =
            mapOf(
                Muscle.CHEST to MuscleWeekState(8, listOf(4, 4)),
                Muscle.QUADS to MuscleWeekState(10, listOf(5, 5), stallCount = 1),
            )
        val advance =
            SessionAdvancer.advance(states, landmarks, mapOf(Muscle.CHEST to standardFeedback))

        assertEquals(MuscleWeekState(10, listOf(5, 5), 1), advance.nextStates.getValue(Muscle.QUADS))
        assertFalse(Muscle.QUADS in advance.decisions)
    }

    @Test
    fun `single-session muscle hitting R4 is capped at plus two and flagged`() {
        val states = mapOf(Muscle.CHEST to MuscleWeekState(10, listOf(10)))
        val aggressive =
            MuscleFeedback(Soreness.NEVER_SORE, Performance.UP, Pump.LOW, JointPain.NONE)
        val advance = SessionAdvancer.advance(states, landmarks, mapOf(Muscle.CHEST to aggressive))

        assertEquals(13, advance.decisions.getValue(Muscle.CHEST).nextSets) // R4 far from MRV
        val next = advance.nextStates.getValue(Muscle.CHEST)
        assertEquals(listOf(12), next.distribution) // +2 cap
        assertEquals(12, next.weeklySets) // achieved total is authoritative
        assertTrue(Muscle.CHEST in advance.cappedMuscles)
    }

    @Test
    fun `MRV stall across two committed sessions triggers the deload`() {
        val chest = landmarks.getValue(Muscle.CHEST)
        val atMrv = MuscleWeekState(chest.mrv, listOf(11, 11), stallCount = 1)
        val down =
            MuscleFeedback(Soreness.RECOVERED_ON_TIME, Performance.DOWN, Pump.MODERATE, JointPain.NONE)
        val advance = SessionAdvancer.advance(mapOf(Muscle.CHEST to atMrv), landmarks, mapOf(Muscle.CHEST to down))

        assertEquals(2, advance.nextStates.getValue(Muscle.CHEST).stallCount)
        assertTrue(advance.deload.triggered)
        assertEquals(listOf(DeloadReason.MRV_STALL), advance.deload.reasons)
        assertEquals(listOf(Muscle.CHEST), advance.deload.stalledMuscles)
    }

    @Test
    fun `systemic fatigue across three muscles triggers the deload`() {
        val down =
            MuscleFeedback(Soreness.RECOVERED_ON_TIME, Performance.DOWN, Pump.LOW, JointPain.NONE)
        val states =
            mapOf(
                Muscle.QUADS to MuscleWeekState(10, listOf(5, 5)),
                Muscle.HAMSTRINGS to MuscleWeekState(8, listOf(4, 4)),
                Muscle.GLUTES to MuscleWeekState(8, listOf(4, 4)),
            )
        val advance =
            SessionAdvancer.advance(
                states,
                landmarks,
                states.keys.associateWith { down },
            )
        assertTrue(advance.deload.triggered)
        assertEquals(listOf(DeloadReason.SYSTEMIC_FATIGUE), advance.deload.reasons)
    }

    @Test
    fun `planned end and manual requests flow through to the deload decision`() {
        val states = mapOf(Muscle.CHEST to MuscleWeekState(8, listOf(4, 4)))
        val advance =
            SessionAdvancer.advance(
                states,
                landmarks,
                feedback = emptyMap(),
                finalAccumulationWeekComplete = true,
                manualDeloadRequest = true,
            )
        assertEquals(
            listOf(DeloadReason.PLANNED_END, DeloadReason.MANUAL),
            advance.deload.reasons,
        )
        assertTrue(advance.decisions.isEmpty())
    }

    @Test
    fun `significant joint pain reduces volume and surfaces the swap flag`() {
        val states = mapOf(Muscle.TRICEPS to MuscleWeekState(10, listOf(5, 5)))
        val painful =
            MuscleFeedback(Soreness.RECOVERED_ON_TIME, Performance.UP, Pump.MODERATE, JointPain.SIGNIFICANT)
        val advance = SessionAdvancer.advance(states, landmarks, mapOf(Muscle.TRICEPS to painful))

        val decision = advance.decisions.getValue(Muscle.TRICEPS)
        assertEquals("R1", decision.ruleId)
        assertTrue(decision.exerciseSwapFlagged)
        assertEquals(listOf(5, 4), advance.nextStates.getValue(Muscle.TRICEPS).distribution)
    }

    @Test
    fun `inputs are validated`() {
        val chestOnly = mapOf(Muscle.CHEST to MuscleWeekState(8, listOf(8)))
        assertThrows<IllegalArgumentException> {
            SessionAdvancer.advance(
                chestOnly,
                landmarks = mapOf(Muscle.QUADS to Landmarks(mev = 8, mrv = 20)),
                feedback = emptyMap(),
            )
        }
        assertThrows<IllegalArgumentException> {
            SessionAdvancer.advance(
                chestOnly,
                landmarks,
                feedback = mapOf(Muscle.QUADS to standardFeedback),
            )
        }
    }
}
