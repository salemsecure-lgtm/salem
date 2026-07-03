package dev.salemlift.app.summary

import dev.salemlift.app.FakeTrainingRepository
import dev.salemlift.app.TestData
import dev.salemlift.app.di.CommitResultStore
import dev.salemlift.domain.model.DeloadDecision
import dev.salemlift.domain.model.DeloadReason
import dev.salemlift.domain.model.Muscle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SummaryViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeTrainingRepository()
    private val store = CommitResultStore()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = SummaryViewModel(repository, store, TestData.SESSION_ID)

    @Test
    fun `renders each decision with rule text, badges, deload banner, and next session`() =
        runTest(dispatcher) {
            store.store(
                TestData.SESSION_ID,
                TestData.outcome(
                    decisions =
                        linkedMapOf(
                            Muscle.CHEST to TestData.decision(ruleId = "R7", cappedDelta = 1, nextSets = 13),
                            Muscle.TRICEPS to TestData.decision(ruleId = "R1", exerciseSwapFlagged = true),
                        ),
                    cappedMuscles = setOf(Muscle.CHEST),
                    deload =
                        DeloadDecision(
                            triggered = true,
                            reasons = listOf(DeloadReason.MRV_STALL),
                            stalledMuscles = listOf(Muscle.CHEST),
                        ),
                    nextSession = TestData.session(sessionId = 11L, week = 3, name = "Pull"),
                ),
            )
            val vm = viewModel()
            runCurrent()

            val state = vm.uiState.value
            assertFalse(state.isLoading)
            assertEquals(listOf(Muscle.CHEST, Muscle.TRICEPS), state.rows.map { it.muscle })
            assertEquals(
                "12 → 13 sets · R7 — Recovered on time and holding or progressing: standard weekly add",
                state.rows[0].headline,
            )
            assertEquals(listOf("Per-session cap applied"), state.rows[0].badges)
            assertEquals(listOf("Exercise swap suggested"), state.rows[1].badges)
            assertTrue(state.deloadTriggered)
            assertEquals(listOf("Volume stalled at MRV"), state.deloadReasons)
            assertEquals("Week 3 · Pull", state.nextSessionName)
        }

    @Test
    fun `falls back to persisted decisions when the hand-off store is empty`() =
        runTest(dispatcher) {
            repository.persistedDecisions = mapOf(Muscle.BACK to TestData.decision(ruleId = "R5"))
            val vm = viewModel()
            runCurrent()

            val state = vm.uiState.value
            assertEquals(listOf(Muscle.BACK), state.rows.map { it.muscle })
            assertTrue("R5" in state.rows[0].headline)
            assertFalse(state.deloadTriggered)
            assertNull(state.nextSessionName)
        }

    @Test
    fun `a stored outcome for a different session is not reused`() =
        runTest(dispatcher) {
            store.store(99L, TestData.outcome())
            repository.persistedDecisions = emptyMap()
            val vm = viewModel()
            runCurrent()
            assertTrue(vm.uiState.value.rows.isEmpty())
        }
}
