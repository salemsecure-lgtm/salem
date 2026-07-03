package dev.salemlift.app.timer

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RestTimerTest {
    @Test
    fun `remaining time is anchor-derived, not tick-accumulated`() {
        val timer = RestTimer.start(durationMillis = 120_000, nowMillis = 1_000)
        assertEquals(120_000, timer.remainingMillis(1_000))
        assertEquals(90_000, timer.remainingMillis(31_000))
        // A wildly late tick still lands exactly right — no accumulated drift.
        assertEquals(1_000, timer.remainingMillis(120_000))
        assertEquals(0, timer.remainingMillis(121_000))
        assertEquals(0, timer.remainingMillis(999_999))
        assertTrue(timer.isFinished(121_000))
        assertFalse(timer.isFinished(120_500))
    }

    @Test
    fun `pause freezes remaining and resume re-anchors precisely`() {
        val timer = RestTimer.start(120_000, nowMillis = 0)
        val paused = timer.pause(nowMillis = 45_000)
        assertTrue(paused.isPaused)
        assertEquals(75_000, paused.remainingMillis(45_000))
        assertEquals(75_000, paused.remainingMillis(500_000)) // frozen while paused

        val resumed = paused.resume(nowMillis = 600_000)
        assertFalse(resumed.isPaused)
        assertEquals(75_000, resumed.remainingMillis(600_000))
        assertEquals(74_000, resumed.remainingMillis(601_000))
    }

    @Test
    fun `pause and resume are idempotent`() {
        val timer = RestTimer.start(60_000, nowMillis = 0)
        assertEquals(timer, timer.resume(10_000))
        val paused = timer.pause(10_000)
        assertEquals(paused, paused.pause(20_000))
    }

    @Test
    fun `adjust adds time while running and while paused, clamped at zero`() {
        val timer = RestTimer.start(60_000, nowMillis = 0)
        val extended = timer.adjust(deltaMillis = 30_000, nowMillis = 10_000)
        assertEquals(80_000, extended.remainingMillis(10_000))

        val paused = timer.pause(50_000).adjust(deltaMillis = -30_000, nowMillis = 50_000)
        assertEquals(0, paused.remainingMillis(50_000))
    }

    @Test
    fun `duration must be positive`() {
        assertThrows<IllegalArgumentException> { RestTimer.start(0, nowMillis = 0) }
    }
}
