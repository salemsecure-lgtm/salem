package dev.salemlift.app.summary

import dev.salemlift.app.TestData
import dev.salemlift.domain.model.ClampBound
import dev.salemlift.domain.model.DeloadReason
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DecisionTextTest {
    @Test
    fun `headline shows the previous to next transition with rule id and rationale`() {
        val decision = TestData.decision(ruleId = "R7", cappedDelta = 1, nextSets = 13)
        assertEquals(
            "12 → 13 sets · R7 — Recovered on time and holding or progressing: standard weekly add",
            DecisionText.headline(decision),
        )
    }

    @Test
    fun `a clamped decision keeps its exact previous value plus a clamp badge`() {
        val decision =
            TestData.decision(
                ruleId = "R8",
                cappedDelta = 2,
                previousSets = 20,
                nextSets = 20,
                clampedAt = ClampBound.MRV,
            )
        assertEquals(
            "20 → 20 sets · R8 — Recovered on time and holding or progressing: standard weekly add",
            DecisionText.headline(decision),
        )
        assertTrue("Clamped at MRV" in DecisionText.badges(decision))
    }

    @Test
    fun `badges cover mild-pain cap, swap flag, and the per-session cap`() {
        val decision = TestData.decision(mildPainCapped = true, exerciseSwapFlagged = true)
        assertEquals(
            listOf(
                "Increase capped — mild joint pain",
                "Exercise swap suggested",
                "Per-session cap applied",
            ),
            DecisionText.badges(decision, isSessionCapped = true),
        )
        assertEquals(emptyList(), DecisionText.badges(TestData.decision()))
    }

    @Test
    fun `every deload reason has a user-facing label`() {
        val labels = DeloadReason.entries.map(DecisionText::deloadReasonLabel)
        assertEquals(DeloadReason.entries.size, labels.toSet().size)
        assertTrue(labels.all { it.isNotBlank() })
    }
}
