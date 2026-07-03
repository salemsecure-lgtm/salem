package dev.salemlift.domain.engine

import dev.salemlift.domain.model.LoadAction
import dev.salemlift.domain.model.RepRange
import dev.salemlift.domain.model.SetLog
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

/** DOMAIN.md §7 double progression. */
class DoubleProgressionTest {
    private val range = RepRange(bottom = 8, top = 12)

    @Test
    fun `top of range on all sets at target RIR adds upper body load and resets reps`() {
        val sets = listOf(SetLog(60.0, 12, 2), SetLog(60.0, 12, 2), SetLog(60.0, 13, 3))
        val action = DoubleProgression.next(sets, range, targetRir = 2, isUpperBody = true)
        assertEquals(LoadAction.AddLoad(incrementKg = 2.5, resetToReps = 8), action)
    }

    @Test
    fun `lower body progression uses the larger increment`() {
        val sets = listOf(SetLog(100.0, 12, 2), SetLog(100.0, 12, 2))
        val action = DoubleProgression.next(sets, range, targetRir = 2, isUpperBody = false)
        assertEquals(LoadAction.AddLoad(incrementKg = 5.0, resetToReps = 8), action)
    }

    @Test
    fun `top reps reached grinding below target RIR does not add load`() {
        val sets = listOf(SetLog(60.0, 12, 0), SetLog(60.0, 12, 0))
        val action = DoubleProgression.next(sets, range, targetRir = 2, isUpperBody = true)
        assertEquals(LoadAction.ReduceLoad(DoubleProgression.REDUCE_FACTOR), action)
    }

    @Test
    fun `one RIR short of target holds the load`() {
        val sets = listOf(SetLog(60.0, 10, 1), SetLog(60.0, 9, 2))
        val action = DoubleProgression.next(sets, range, targetRir = 2, isUpperBody = true)
        assertEquals(LoadAction.Hold, action)
    }

    @Test
    fun `two or more RIR short of target reduces the load five percent`() {
        val sets = listOf(SetLog(60.0, 10, 0), SetLog(60.0, 9, 1))
        val action = DoubleProgression.next(sets, range, targetRir = 2, isUpperBody = true)
        assertEquals(LoadAction.ReduceLoad(0.95), action)
    }

    @Test
    fun `way too easy mid-range suggests a load increase`() {
        val sets = listOf(SetLog(60.0, 10, 5), SetLog(60.0, 10, 6))
        val action = DoubleProgression.next(sets, range, targetRir = 2, isUpperBody = true)
        assertEquals(LoadAction.SuggestLoadIncrease, action)
    }

    @Test
    fun `standard mid-range session progresses reps`() {
        val sets = listOf(SetLog(60.0, 10, 2), SetLog(60.0, 9, 3))
        val action = DoubleProgression.next(sets, range, targetRir = 2, isUpperBody = true)
        assertEquals(LoadAction.ProgressReps, action)
    }

    @Test
    fun `empty session and negative target are rejected`() {
        assertThrows<IllegalArgumentException> {
            DoubleProgression.next(emptyList(), range, targetRir = 2, isUpperBody = true)
        }
        assertThrows<IllegalArgumentException> {
            DoubleProgression.next(listOf(SetLog(60.0, 10, 2)), range, targetRir = -1, isUpperBody = true)
        }
    }
}
