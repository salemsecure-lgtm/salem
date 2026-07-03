package dev.salemlift.domain.engine

import dev.salemlift.domain.model.DefaultLandmarks
import dev.salemlift.domain.model.JointPain
import dev.salemlift.domain.model.MuscleFeedback
import dev.salemlift.domain.model.Performance
import dev.salemlift.domain.model.Pump
import dev.salemlift.domain.model.Soreness
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

/**
 * SPEC §6 budget: computing the next session for a full session's worth of
 * muscles (all 14, worst case) must take < 50 ms. The engine is pure table
 * lookups, so this passes with orders-of-magnitude headroom — the test exists
 * to catch an accidental complexity regression, not to measure precisely.
 */
class EngineComputeBudgetTest {
    @Test
    fun `full-session next-session compute stays under 50ms`() {
        val feedback =
            MuscleFeedback(Soreness.RECOVERED_ON_TIME, Performance.UP, Pump.MODERATE, JointPain.NONE)
        // Warm-up pass so JIT/classloading doesn't bill the measured run.
        for ((_, landmarks) in DefaultLandmarks.seeds) {
            Autoregulator.decide(landmarks.mev, landmarks, feedback)
        }

        val start = System.nanoTime()
        for ((_, landmarks) in DefaultLandmarks.seeds) {
            val decision = Autoregulator.decide(landmarks.mev, landmarks, feedback)
            Autoregulator.nextStallCount(decision.nextSets, landmarks, feedback.performance, 0)
        }
        DeloadTriggers.evaluate(
            finalAccumulationWeekComplete = false,
            stallCounts = emptyMap(),
            sessionPerformance = DefaultLandmarks.seeds.keys.associateWith { Performance.UP },
            manualRequest = false,
        )
        val elapsedMs = (System.nanoTime() - start) / 1_000_000.0

        assertTrue(elapsedMs < 50.0, "full-session engine compute took ${elapsedMs}ms (budget 50ms)")
    }
}
