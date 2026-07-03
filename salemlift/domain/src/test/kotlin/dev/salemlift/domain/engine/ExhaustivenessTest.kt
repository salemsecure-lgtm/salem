package dev.salemlift.domain.engine

import dev.salemlift.domain.model.JointPain
import dev.salemlift.domain.model.Landmarks
import dev.salemlift.domain.model.MuscleFeedback
import dev.salemlift.domain.model.Performance
import dev.salemlift.domain.model.Pump
import dev.salemlift.domain.model.Soreness
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * DOMAIN.md §5: the rule table must be a total function — all
 * 4 × 3 × 3 × 3 = 108 feedback combinations have a first-matching rule —
 * and the [MV, MRV] clamp invariant must hold for every combination at
 * every in-range starting volume.
 */
class ExhaustivenessTest {
    private val chest = Landmarks(mev = 8, mrv = 22, mav = 16)

    private val allCombinations =
        Soreness.entries.flatMap { soreness ->
            Performance.entries.flatMap { performance ->
                Pump.entries.flatMap { pump ->
                    JointPain.entries.map { jointPain ->
                        MuscleFeedback(soreness, performance, pump, jointPain)
                    }
                }
            }
        }

    @Test
    fun `there are exactly 108 feedback combinations`() {
        assertEquals(108, allCombinations.size)
        assertEquals(108, allCombinations.toSet().size)
    }

    @Test
    fun `dont-care dimension sets cover their full enums`() {
        assertEquals(Soreness.entries.toSet(), AutoregRule.anySoreness)
        assertEquals(Performance.entries.toSet(), AutoregRule.anyPerformance)
        assertEquals(Pump.entries.toSet(), AutoregRule.anyPump)
        assertEquals(JointPain.entries.toSet(), AutoregRule.anyJointPain)
    }

    @Test
    fun `every combination has a first-matching rule`() {
        for (feedback in allCombinations) {
            val match = DefaultRules.table.firstOrNull { it.matches(feedback) }
            assertNotNull(match, "No rule matched $feedback")
        }
    }

    @Test
    fun `every decision stays within MV and MRV from any in-range volume`() {
        for (feedback in allCombinations) {
            for (sets in chest.mv..chest.mrv) {
                val decision = Autoregulator.decide(sets, chest, feedback)
                assertTrue(
                    decision.nextSets in chest.mv..chest.mrv,
                    "Decision left [MV, MRV]: $feedback at $sets sets -> ${decision.nextSets}",
                )
            }
        }
    }

    @Test
    fun `raw deltas never exceed the documented extremes`() {
        for (feedback in allCombinations) {
            val decision = Autoregulator.decide(12, chest, feedback)
            assertTrue(
                decision.rawDelta in -1..3,
                "Raw delta out of documented range for $feedback: ${decision.rawDelta}",
            )
        }
    }

    @Test
    fun `mild pain never allows an increase`() {
        for (feedback in allCombinations.filter { it.jointPain == JointPain.MILD }) {
            val decision = Autoregulator.decide(12, chest, feedback)
            assertTrue(
                decision.cappedDelta <= 0,
                "Mild joint pain allowed an increase for $feedback",
            )
        }
    }

    @Test
    fun `significant pain always reduces and flags a swap`() {
        for (feedback in allCombinations.filter { it.jointPain == JointPain.SIGNIFICANT }) {
            val decision = Autoregulator.decide(12, chest, feedback)
            assertEquals("R1", decision.ruleId)
            assertEquals(-1, decision.rawDelta)
            assertTrue(decision.exerciseSwapFlagged)
        }
    }
}
