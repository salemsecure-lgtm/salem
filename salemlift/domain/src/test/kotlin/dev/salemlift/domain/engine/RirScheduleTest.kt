package dev.salemlift.domain.engine

import dev.salemlift.domain.model.WeekEffort
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

/** DOMAIN.md §3.1 RIR schedules. */
class RirScheduleTest {
    @Test
    fun `four week default schedule is 3-2-1-1 with final week to zero`() {
        assertEquals(
            listOf(
                WeekEffort(3),
                WeekEffort(2),
                WeekEffort(1),
                WeekEffort(1, allowZeroOnLastSet = true),
            ),
            RirSchedule.forAccumulation(4),
        )
    }

    @Test
    fun `five week schedule is 3-2-2-1-1 with final week to zero`() {
        assertEquals(
            listOf(
                WeekEffort(3),
                WeekEffort(2),
                WeekEffort(2),
                WeekEffort(1),
                WeekEffort(1, allowZeroOnLastSet = true),
            ),
            RirSchedule.forAccumulation(5),
        )
    }

    @Test
    fun `six week schedule is 3-2-2-1-1-0`() {
        assertEquals(
            listOf(
                WeekEffort(3),
                WeekEffort(2),
                WeekEffort(2),
                WeekEffort(1),
                WeekEffort(1),
                WeekEffort(0),
            ),
            RirSchedule.forAccumulation(6),
        )
    }

    @Test
    fun `deload effort is RIR 4`() {
        assertEquals(WeekEffort(4), RirSchedule.deload)
    }

    @Test
    fun `lengths outside 4 to 6 are rejected`() {
        assertThrows<IllegalArgumentException> { RirSchedule.forAccumulation(3) }
        assertThrows<IllegalArgumentException> { RirSchedule.forAccumulation(7) }
    }
}
