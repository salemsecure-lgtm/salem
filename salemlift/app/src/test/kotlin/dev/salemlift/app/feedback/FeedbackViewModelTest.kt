package dev.salemlift.app.feedback

import dev.salemlift.app.FakeTrainingRepository
import dev.salemlift.app.TestData
import dev.salemlift.app.di.CommitResultStore
import dev.salemlift.data.FeedbackDraft
import dev.salemlift.domain.model.JointPain
import dev.salemlift.domain.model.Muscle
import dev.salemlift.domain.model.Performance
import dev.salemlift.domain.model.Pump
import dev.salemlift.domain.model.Soreness
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FeedbackViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeTrainingRepository()
    private val store = CommitResultStore()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository.currentSessionFlow.value = TestData.session()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = FeedbackViewModel(repository, store, TestData.SESSION_ID)

    @Test
    fun `entries are created per session muscle in order with performance defaulting to SAME`() =
        runTest(dispatcher) {
            val vm = viewModel()
            runCurrent()
            val entries = vm.uiState.value.entries
            assertEquals(listOf(Muscle.CHEST, Muscle.TRICEPS), entries.map { it.muscle })
            assertTrue(entries.all { it.performance == Performance.SAME })
            assertTrue(entries.none { it.isComplete })
        }

    @Test
    fun `commit stays disabled until every muscle is fully rated`() =
        runTest(dispatcher) {
            val vm = viewModel()
            runCurrent()
            assertFalse(vm.uiState.value.canCommit)

            vm.setSoreness(Muscle.CHEST, Soreness.RECOVERED_ON_TIME)
            vm.setPump(Muscle.CHEST, Pump.HIGH)
            vm.setJointPain(Muscle.CHEST, JointPain.NONE)
            runCurrent()
            assertFalse(vm.uiState.value.canCommit)

            vm.setSoreness(Muscle.TRICEPS, Soreness.STILL_SORE)
            vm.setPump(Muscle.TRICEPS, Pump.LOW)
            vm.setJointPain(Muscle.TRICEPS, JointPain.MILD)
            runCurrent()
            assertTrue(vm.uiState.value.canCommit)

            // An incomplete commit attempt never reaches the repository.
            assertTrue(repository.commits.isEmpty())
        }

    @Test
    fun `commit builds the FeedbackDraft list in muscle order and stores the outcome`() =
        runTest(dispatcher) {
            val outcome = TestData.outcome()
            repository.commitOutcome = outcome
            val vm = viewModel()
            val events = mutableListOf<FeedbackViewModel.Event>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.events.toList(events) }
            runCurrent()

            vm.setSoreness(Muscle.CHEST, Soreness.RECOVERED_EARLY)
            vm.setPump(Muscle.CHEST, Pump.MODERATE)
            vm.setJointPain(Muscle.CHEST, JointPain.NONE)
            vm.setPerformance(Muscle.CHEST, Performance.UP)
            vm.setSoreness(Muscle.TRICEPS, Soreness.RECOVERED_ON_TIME)
            vm.setPump(Muscle.TRICEPS, Pump.HIGH)
            vm.setJointPain(Muscle.TRICEPS, JointPain.MILD)
            vm.commit()
            runCurrent()

            val commit = repository.commits.single()
            assertEquals(TestData.SESSION_ID, commit.sessionId)
            assertFalse(commit.manualDeloadRequest)
            assertEquals(
                listOf(
                    FeedbackDraft(
                        Muscle.CHEST,
                        Soreness.RECOVERED_EARLY,
                        Pump.MODERATE,
                        JointPain.NONE,
                        Performance.UP,
                    ),
                    FeedbackDraft(
                        Muscle.TRICEPS,
                        Soreness.RECOVERED_ON_TIME,
                        Pump.HIGH,
                        JointPain.MILD,
                        Performance.SAME,
                    ),
                ),
                commit.feedback,
            )
            assertEquals(outcome, store.outcomeFor(TestData.SESSION_ID))
            val expectedEvents: List<FeedbackViewModel.Event> =
                listOf(FeedbackViewModel.Event.Committed(TestData.SESSION_ID))
            assertEquals(expectedEvents, events)
        }

    @Test
    fun `commit before rating everything is a no-op`() =
        runTest(dispatcher) {
            val vm = viewModel()
            runCurrent()
            vm.commit()
            runCurrent()
            assertTrue(repository.commits.isEmpty())
        }
}
