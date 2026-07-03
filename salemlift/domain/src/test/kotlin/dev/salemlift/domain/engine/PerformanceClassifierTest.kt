package dev.salemlift.domain.engine

import dev.salemlift.domain.model.Performance
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

/** DOMAIN.md §4.1 performance classification and per-muscle aggregation. */
class PerformanceClassifierTest {
    @Test
    fun `e1RM up more than one percent is UP`() {
        assertEquals(Performance.UP, PerformanceClassifier.classify(102.0, 100.0))
    }

    @Test
    fun `e1RM down more than one percent is DOWN`() {
        assertEquals(Performance.DOWN, PerformanceClassifier.classify(98.0, 100.0))
    }

    @Test
    fun `within the one percent band is SAME`() {
        assertEquals(Performance.SAME, PerformanceClassifier.classify(100.9, 100.0))
        assertEquals(Performance.SAME, PerformanceClassifier.classify(99.1, 100.0))
        assertEquals(Performance.SAME, PerformanceClassifier.classify(100.0, 100.0))
    }

    @Test
    fun `threshold is tunable`() {
        assertEquals(Performance.SAME, PerformanceClassifier.classify(104.0, 100.0, thresholdPct = 5.0))
        assertEquals(Performance.UP, PerformanceClassifier.classify(106.0, 100.0, thresholdPct = 5.0))
    }

    @Test
    fun `non-positive e1RMs are rejected`() {
        assertThrows<IllegalArgumentException> { PerformanceClassifier.classify(0.0, 100.0) }
        assertThrows<IllegalArgumentException> { PerformanceClassifier.classify(100.0, 0.0) }
    }

    @Test
    fun `credit-weighted majority wins the aggregation`() {
        val votes =
            listOf(
                Performance.UP to 1.0,
                Performance.UP to 1.0,
                Performance.DOWN to 0.5,
            )
        assertEquals(Performance.UP, PerformanceClassifier.aggregate(votes))
    }

    @Test
    fun `secondary credit can outweigh a single primary`() {
        val votes =
            listOf(
                Performance.DOWN to 1.0,
                Performance.UP to 0.5,
                Performance.UP to 0.5,
                Performance.UP to 0.5,
            )
        assertEquals(Performance.UP, PerformanceClassifier.aggregate(votes))
    }

    @Test
    fun `ties resolve to SAME`() {
        val votes = listOf(Performance.UP to 1.0, Performance.DOWN to 1.0)
        assertEquals(Performance.SAME, PerformanceClassifier.aggregate(votes))
    }

    @Test
    fun `no votes resolves to SAME`() {
        assertEquals(Performance.SAME, PerformanceClassifier.aggregate(emptyList()))
    }

    @Test
    fun `non-positive credits are rejected`() {
        assertThrows<IllegalArgumentException> {
            PerformanceClassifier.aggregate(listOf(Performance.UP to 0.0))
        }
    }
}
