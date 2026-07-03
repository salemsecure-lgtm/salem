package dev.salemlift.domain.engine

import dev.salemlift.domain.model.ClampBound
import dev.salemlift.domain.model.JointPain
import dev.salemlift.domain.model.JointPain.MILD
import dev.salemlift.domain.model.JointPain.NONE
import dev.salemlift.domain.model.JointPain.SIGNIFICANT
import dev.salemlift.domain.model.Landmarks
import dev.salemlift.domain.model.MuscleFeedback
import dev.salemlift.domain.model.Performance
import dev.salemlift.domain.model.Performance.DOWN
import dev.salemlift.domain.model.Performance.SAME
import dev.salemlift.domain.model.Performance.UP
import dev.salemlift.domain.model.Pump
import dev.salemlift.domain.model.Pump.HIGH
import dev.salemlift.domain.model.Pump.LOW
import dev.salemlift.domain.model.Pump.MODERATE
import dev.salemlift.domain.model.Soreness
import dev.salemlift.domain.model.Soreness.NEVER_SORE
import dev.salemlift.domain.model.Soreness.RECOVERED_EARLY
import dev.salemlift.domain.model.Soreness.RECOVERED_ON_TIME
import dev.salemlift.domain.model.Soreness.STILL_SORE
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Every worked example from DOMAIN.md §5.1 (E1–E12b) as a literal test.
 * Chest landmarks throughout: MV 4, MEV 8, MAV 16, MRV 22.
 */
class AutoregDecisionTableTest {
    private val chest = Landmarks(mev = 8, mrv = 22, mav = 16)

    private fun feedback(
        soreness: Soreness,
        performance: Performance,
        pump: Pump,
        jointPain: JointPain,
    ) = MuscleFeedback(soreness, performance, pump, jointPain)

    @Test
    fun `E1 - R1 significant joint pain overrides everything`() {
        val decision = Autoregulator.decide(12, chest, feedback(RECOVERED_ON_TIME, UP, MODERATE, SIGNIFICANT))
        assertEquals("R1", decision.ruleId)
        assertEquals(-1, decision.rawDelta)
        assertEquals(11, decision.nextSets)
        assertTrue(decision.exerciseSwapFlagged)
    }

    @Test
    fun `E2 - R2 still sore and performance down backs off`() {
        val decision = Autoregulator.decide(18, chest, feedback(STILL_SORE, DOWN, LOW, NONE))
        assertEquals("R2", decision.ruleId)
        assertEquals(-1, decision.rawDelta)
        assertEquals(17, decision.nextSets)
        assertFalse(decision.exerciseSwapFlagged)
    }

    @Test
    fun `E3 - R3 sore but performing holds`() {
        val decision = Autoregulator.decide(14, chest, feedback(STILL_SORE, SAME, MODERATE, NONE))
        assertEquals("R3", decision.ruleId)
        assertEquals(0, decision.rawDelta)
        assertEquals(14, decision.nextSets)
    }

    @Test
    fun `E4 - R4 no signal far from MRV adds three`() {
        val decision = Autoregulator.decide(10, chest, feedback(NEVER_SORE, UP, LOW, NONE))
        assertEquals("R4", decision.ruleId)
        assertEquals(3, decision.rawDelta)
        assertEquals(13, decision.nextSets)
    }

    @Test
    fun `E4b - R4 same signal near MRV adds two`() {
        val decision = Autoregulator.decide(19, chest, feedback(NEVER_SORE, UP, LOW, NONE))
        assertEquals("R4", decision.ruleId)
        assertEquals(2, decision.rawDelta)
        assertEquals(21, decision.nextSets)
    }

    @Test
    fun `E5 - R5 recovered early progressing moderate pump adds two`() {
        val decision = Autoregulator.decide(12, chest, feedback(RECOVERED_EARLY, UP, MODERATE, NONE))
        assertEquals("R5", decision.ruleId)
        assertEquals(2, decision.rawDelta)
        assertEquals(14, decision.nextSets)
    }

    @Test
    fun `E6 - R6 progressing with high pump adds one`() {
        val decision = Autoregulator.decide(12, chest, feedback(NEVER_SORE, UP, HIGH, NONE))
        assertEquals("R6", decision.ruleId)
        assertEquals(1, decision.rawDelta)
        assertEquals(13, decision.nextSets)
    }

    @Test
    fun `E7 - R7 recovered on time same performance standard add`() {
        val decision = Autoregulator.decide(12, chest, feedback(RECOVERED_ON_TIME, SAME, MODERATE, NONE))
        assertEquals("R7", decision.ruleId)
        assertEquals(1, decision.rawDelta)
        assertEquals(13, decision.nextSets)
    }

    @Test
    fun `E7b - R7 on time and up adds one regardless of pump`() {
        val decision = Autoregulator.decide(16, chest, feedback(RECOVERED_ON_TIME, UP, HIGH, NONE))
        assertEquals("R7", decision.ruleId)
        assertEquals(1, decision.rawDelta)
        assertEquals(17, decision.nextSets)
    }

    @Test
    fun `E8 - R8 recovered early flat performance adds one`() {
        val decision = Autoregulator.decide(10, chest, feedback(RECOVERED_EARLY, SAME, LOW, NONE))
        assertEquals("R8", decision.ruleId)
        assertEquals(1, decision.rawDelta)
        assertEquals(11, decision.nextSets)
    }

    @Test
    fun `E9 - R9 down day without soreness holds`() {
        val decision = Autoregulator.decide(14, chest, feedback(RECOVERED_ON_TIME, DOWN, MODERATE, NONE))
        assertEquals("R9", decision.ruleId)
        assertEquals(0, decision.rawDelta)
        assertEquals(14, decision.nextSets)
    }

    @Test
    fun `E10 - mild pain caps an increase at zero`() {
        val decision = Autoregulator.decide(12, chest, feedback(RECOVERED_ON_TIME, SAME, MODERATE, MILD))
        assertEquals("R7", decision.ruleId)
        assertEquals(1, decision.rawDelta)
        assertEquals(0, decision.cappedDelta)
        assertEquals(12, decision.nextSets)
        assertTrue(decision.mildPainCapped)
    }

    @Test
    fun `E10b - mild pain does not block reductions`() {
        val decision = Autoregulator.decide(12, chest, feedback(STILL_SORE, DOWN, LOW, MILD))
        assertEquals("R2", decision.ruleId)
        assertEquals(-1, decision.rawDelta)
        assertEquals(-1, decision.cappedDelta)
        assertEquals(11, decision.nextSets)
        assertFalse(decision.mildPainCapped)
    }

    @Test
    fun `E11 - positive delta at MRV clamps to hold`() {
        val decision = Autoregulator.decide(22, chest, feedback(RECOVERED_ON_TIME, UP, MODERATE, NONE))
        assertEquals("R7", decision.ruleId)
        assertEquals(1, decision.rawDelta)
        assertEquals(22, decision.nextSets)
        assertEquals(ClampBound.MRV, decision.clampedAt)
    }

    @Test
    fun `E11b - negative delta at MV clamps to floor`() {
        val decision = Autoregulator.decide(4, chest, feedback(STILL_SORE, DOWN, LOW, NONE))
        assertEquals("R2", decision.ruleId)
        assertEquals(-1, decision.rawDelta)
        assertEquals(4, decision.nextSets)
        assertEquals(ClampBound.MV, decision.clampedAt)
    }

    @Test
    fun `unclamped decisions report no clamp bound`() {
        val decision = Autoregulator.decide(12, chest, feedback(RECOVERED_ON_TIME, SAME, MODERATE, NONE))
        assertNull(decision.clampedAt)
    }

    @Test
    fun `E12 - two consecutive down sessions at MRV reach the stall limit`() {
        val first = Autoregulator.nextStallCount(22, chest, DOWN, previousCount = 0)
        assertEquals(1, first)
        val second = Autoregulator.nextStallCount(22, chest, DOWN, previousCount = first)
        assertEquals(Autoregulator.MRV_STALL_LIMIT, second)
    }

    @Test
    fun `E12b - a non-down session at MRV resets the stall counter`() {
        val first = Autoregulator.nextStallCount(22, chest, DOWN, previousCount = 0)
        val reset = Autoregulator.nextStallCount(22, chest, SAME, previousCount = first)
        assertEquals(0, reset)
        val again = Autoregulator.nextStallCount(22, chest, DOWN, previousCount = reset)
        assertEquals(1, again)
    }

    @Test
    fun `stall counter does not accumulate below MRV`() {
        assertEquals(0, Autoregulator.nextStallCount(18, chest, DOWN, previousCount = 1))
    }

    @Test
    fun `stall counter rejects negative previous counts`() {
        assertThrows<IllegalArgumentException> {
            Autoregulator.nextStallCount(22, chest, DOWN, previousCount = -1)
        }
    }

    @Test
    fun `decide rejects non-positive current sets`() {
        assertThrows<IllegalArgumentException> {
            Autoregulator.decide(0, chest, feedback(RECOVERED_ON_TIME, SAME, MODERATE, NONE))
        }
    }

    @Test
    fun `a non-total custom rule table fails loudly`() {
        assertThrows<IllegalStateException> {
            Autoregulator.decide(
                12,
                chest,
                feedback(RECOVERED_ON_TIME, SAME, MODERATE, NONE),
                table = emptyList(),
            )
        }
    }
}
