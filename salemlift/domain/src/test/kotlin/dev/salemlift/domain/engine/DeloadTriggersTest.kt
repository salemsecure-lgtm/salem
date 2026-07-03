package dev.salemlift.domain.engine

import dev.salemlift.domain.model.DeloadReason
import dev.salemlift.domain.model.Muscle
import dev.salemlift.domain.model.Performance
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** DOMAIN.md §6 deload triggers (a)–(d). */
class DeloadTriggersTest {
    private fun evaluate(
        plannedEnd: Boolean = false,
        stallCounts: Map<Muscle, Int> = emptyMap(),
        performance: Map<Muscle, Performance> = emptyMap(),
        manual: Boolean = false,
    ) = DeloadTriggers.evaluate(plannedEnd, stallCounts, performance, manual)

    @Test
    fun `no signals means no deload`() {
        val decision =
            evaluate(
                stallCounts = mapOf(Muscle.CHEST to 1),
                performance = mapOf(Muscle.CHEST to Performance.DOWN, Muscle.BACK to Performance.DOWN),
            )
        assertFalse(decision.triggered)
        assertTrue(decision.reasons.isEmpty())
        assertTrue(decision.stalledMuscles.isEmpty())
    }

    @Test
    fun `trigger a - planned mesocycle end`() {
        val decision = evaluate(plannedEnd = true)
        assertTrue(decision.triggered)
        assertEquals(listOf(DeloadReason.PLANNED_END), decision.reasons)
    }

    @Test
    fun `trigger b - muscle at MRV with two consecutive down sessions`() {
        val decision = evaluate(stallCounts = mapOf(Muscle.CHEST to 2, Muscle.BACK to 1))
        assertTrue(decision.triggered)
        assertEquals(listOf(DeloadReason.MRV_STALL), decision.reasons)
        assertEquals(listOf(Muscle.CHEST), decision.stalledMuscles)
    }

    @Test
    fun `trigger c - performance down across three or more muscles`() {
        val decision =
            evaluate(
                performance =
                    mapOf(
                        Muscle.CHEST to Performance.DOWN,
                        Muscle.BACK to Performance.DOWN,
                        Muscle.QUADS to Performance.DOWN,
                        Muscle.BICEPS to Performance.UP,
                    ),
            )
        assertTrue(decision.triggered)
        assertEquals(listOf(DeloadReason.SYSTEMIC_FATIGUE), decision.reasons)
    }

    @Test
    fun `trigger d - manual request`() {
        val decision = evaluate(manual = true)
        assertTrue(decision.triggered)
        assertEquals(listOf(DeloadReason.MANUAL), decision.reasons)
    }

    @Test
    fun `multiple simultaneous triggers are all reported`() {
        val decision =
            evaluate(
                plannedEnd = true,
                stallCounts = mapOf(Muscle.SIDE_DELTS to 3, Muscle.CHEST to 2),
                performance =
                    mapOf(
                        Muscle.CHEST to Performance.DOWN,
                        Muscle.BACK to Performance.DOWN,
                        Muscle.QUADS to Performance.DOWN,
                    ),
                manual = true,
            )
        assertTrue(decision.triggered)
        assertEquals(
            listOf(
                DeloadReason.PLANNED_END,
                DeloadReason.MRV_STALL,
                DeloadReason.SYSTEMIC_FATIGUE,
                DeloadReason.MANUAL,
            ),
            decision.reasons,
        )
        assertEquals(listOf(Muscle.CHEST, Muscle.SIDE_DELTS), decision.stalledMuscles)
    }
}
