package dev.salemlift.app.home

import dev.salemlift.app.FakeTrainingRepository
import dev.salemlift.app.TestData
import dev.salemlift.data.SessionState
import dev.salemlift.domain.model.MesoConfig
import dev.salemlift.domain.program.SplitTemplates
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
import kotlin.test.assertIs

class HomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeTrainingRepository()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `no active meso shows the shipped split templates`() =
        runTest(dispatcher) {
            val viewModel = HomeViewModel(repository)
            runCurrent()
            val state = assertIs<HomeViewModel.UiState.NoActiveMeso>(viewModel.uiState.value)
            assertEquals(SplitTemplates.all, state.splits)
        }

    @Test
    fun `a pending session is surfaced as today`() =
        runTest(dispatcher) {
            repository.currentSessionFlow.value = TestData.session()
            val viewModel = HomeViewModel(repository)
            runCurrent()
            val state = assertIs<HomeViewModel.UiState.Today>(viewModel.uiState.value)
            assertEquals(TestData.SESSION_ID, state.session.sessionId)
        }

    @Test
    fun `start mesocycle delegates to the repository with the default config`() =
        runTest(dispatcher) {
            val viewModel = HomeViewModel(repository)
            viewModel.startMesocycle(SplitTemplates.pplThreeDay)
            runCurrent()
            assertEquals(listOf(SplitTemplates.pplThreeDay to MesoConfig()), repository.startedMesocycles)
        }

    @Test
    fun `start session marks a pending session started then navigates to the runner`() =
        runTest(dispatcher) {
            val session = TestData.session(state = SessionState.PENDING)
            repository.currentSessionFlow.value = session
            val viewModel = HomeViewModel(repository)
            val events = mutableListOf<HomeViewModel.Event>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.events.toList(events)
            }

            viewModel.startSession(session)
            runCurrent()

            assertEquals(listOf(TestData.SESSION_ID), repository.startedSessions)
            val expected: List<HomeViewModel.Event> =
                listOf(HomeViewModel.Event.NavigateToRunner(TestData.SESSION_ID))
            assertEquals(expected, events)
        }

    @Test
    fun `resuming an in-progress session skips markSessionStarted`() =
        runTest(dispatcher) {
            val session = TestData.session(state = SessionState.IN_PROGRESS)
            repository.currentSessionFlow.value = session
            val viewModel = HomeViewModel(repository)
            val events = mutableListOf<HomeViewModel.Event>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.events.toList(events)
            }

            viewModel.startSession(session)
            runCurrent()

            assertEquals(emptyList<Long>(), repository.startedSessions.toList())
            val expected: List<HomeViewModel.Event> =
                listOf(HomeViewModel.Event.NavigateToRunner(TestData.SESSION_ID))
            assertEquals(expected, events)
        }
}
