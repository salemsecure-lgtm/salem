package dev.salemlift.data.settings

import dev.salemlift.domain.engine.Autoregulator
import dev.salemlift.domain.engine.DefaultRules
import dev.salemlift.domain.model.JointPain
import dev.salemlift.domain.model.Landmarks
import dev.salemlift.domain.model.MuscleFeedback
import dev.salemlift.domain.model.Performance
import dev.salemlift.domain.model.Pump
import dev.salemlift.domain.model.Soreness
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

/** Pure-JVM tests for the delta-tuning layer over [DefaultRules]. */
class RuleTablesTest {
    private val allFeedback: List<MuscleFeedback> =
        Soreness.entries.flatMap { s ->
            Performance.entries.flatMap { p ->
                Pump.entries.flatMap { u ->
                    JointPain.entries.map { j -> MuscleFeedback(s, p, u, j) }
                }
            }
        }

    @Test
    fun `editable ids are exactly the fixed-delta rules - R4 excluded`() {
        assertEquals(setOf("R1", "R2", "R3", "R5", "R6", "R7", "R8", "R9"), RuleTables.editableRuleIds)
    }

    @Test
    fun `fixed deltas match the DOMAIN table and R4 has none`() {
        val deltas = DefaultRules.table.associate { it.id to RuleTables.fixedDelta(it) }
        assertEquals(
            mapOf(
                "R1" to -1,
                "R2" to -1,
                "R3" to 0,
                "R4" to null,
                "R5" to 2,
                "R6" to 1,
                "R7" to 1,
                "R8" to 1,
                "R9" to 0,
            ),
            deltas,
        )
    }

    @Test
    fun `tuned without overrides returns the default rows untouched`() {
        val tuned = RuleTables.tuned(emptyMap())
        DefaultRules.table.zip(tuned).forEach { (original, result) -> assertSame(original, result) }
    }

    @Test
    fun `tuned rows preserve conditions order rationale and swap flag across the full input space`() {
        val overrides = RuleTables.editableRuleIds.associateWith { 0 }
        val tuned = RuleTables.tuned(overrides)
        assertEquals(DefaultRules.table.map { it.id }, tuned.map { it.id })
        DefaultRules.table.zip(tuned).forEach { (original, result) ->
            assertEquals(original.rationale, result.rationale, "rationale of ${original.id}")
            assertEquals(original.flagsExerciseSwap, result.flagsExerciseSwap, "swap flag of ${original.id}")
            allFeedback.forEach { feedback ->
                assertEquals(
                    original.matches(feedback),
                    result.matches(feedback),
                    "rule ${original.id} must match identically on $feedback",
                )
            }
        }
    }

    @Test
    fun `an overridden delta feeds the autoregulator through the tuned table`() {
        val tuned = RuleTables.tuned(mapOf("R7" to 2))
        val decision =
            Autoregulator.decide(
                currentSets = 10,
                landmarks = Landmarks(mev = 8, mrv = 22),
                feedback =
                    MuscleFeedback(
                        soreness = Soreness.RECOVERED_ON_TIME,
                        performance = Performance.SAME,
                        pump = Pump.MODERATE,
                        jointPain = JointPain.NONE,
                    ),
                table = tuned,
            )
        assertEquals("R7", decision.ruleId)
        assertEquals(2, decision.rawDelta)
        assertEquals(12, decision.nextSets)
    }

    @Test
    fun `overrides for R4 or unknown ids are ignored by tuned`() {
        val tuned = RuleTables.tuned(mapOf("R4" to 1, "R99" to 1))
        val r4 = tuned.first { it.id == "R4" }
        assertSame(DefaultRules.table.first { it.id == "R4" }, r4)
        assertNull(RuleTables.fixedDelta(r4))
    }
}
