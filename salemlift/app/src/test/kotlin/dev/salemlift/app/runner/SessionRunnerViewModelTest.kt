package dev.salemlift.app.runner

import dev.salemlift.app.FakeTrainingRepository
import dev.salemlift.app.TestData
import dev.salemlift.app.di.ExerciseNameResolver
import dev.salemlift.data.LoggedSet
import dev.salemlift.domain.model.Muscle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SessionRunnerViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeTrainingRepository()
    private val names = mutableMapOf("bench" to "Bench Press", "skull" to "Skullcrusher")
    private val resolver = ExerciseNameResolver { names[it] }

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository.currentSessionFlow.value = TestData.session()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() =
        SessionRunnerViewModel(
            repository = repository,
            nameResolver = resolver,
            sessionId = TestData.SESSION_ID,
            nowEpochMillis = { 1_111L },
        )

    @Test
    fun `blocks follow the session's muscle order and the first is expanded`() =
        runTest(dispatcher) {
            val vm = viewModel()
            runCurrent()
            val blocks = vm.uiState.value.blocks
            assertEquals(listOf(Muscle.CHEST, Muscle.TRICEPS), blocks.map { it.muscle })
            assertEquals(listOf(3, 2), blocks.map { it.targetSets })
            assertTrue(blocks[0].isExpanded)
            assertFalse(blocks[1].isExpanded)
            assertFalse(vm.uiState.value.canFinish)
        }

    @Test
    fun `logging a set persists it with session-wide order and emits SetLogged`() =
        runTest(dispatcher) {
            val vm = viewModel()
            val events = collectEvents(vm)
            runCurrent()

            vm.chooseExercise(Muscle.CHEST, "bench", "Bench Press")
            vm.updateWeightText(Muscle.CHEST, "62.5")
            vm.adjustReps(Muscle.CHEST, +2)
            vm.adjustRir(Muscle.CHEST, -1)
            runCurrent()
            vm.logSet(Muscle.CHEST)
            runCurrent()

            val logged = repository.setsFlow.value.single()
            assertEquals(TestData.SESSION_ID, logged.sessionId)
            assertEquals("bench", logged.exerciseId)
            assertEquals(Muscle.CHEST, logged.muscle)
            assertEquals(62.5, logged.weightKg)
            assertEquals(10, logged.reps)
            assertEquals(1, logged.rir)
            assertEquals(0, logged.orderInSession)
            assertEquals(1_111L, logged.loggedAtEpochMillis)
            assertEquals<List<SessionRunnerViewModel.Event>>(listOf(SessionRunnerViewModel.Event.SetLogged), events)
            assertTrue(vm.uiState.value.canFinish)
        }

    @Test
    fun `a second set in another muscle continues the session-wide order`() =
        runTest(dispatcher) {
            val vm = viewModel()
            runCurrent()
            vm.chooseExercise(Muscle.CHEST, "bench", "Bench Press")
            vm.chooseExercise(Muscle.TRICEPS, "skull", "Skullcrusher")
            vm.updateWeightText(Muscle.CHEST, "60")
            vm.updateWeightText(Muscle.TRICEPS, "25")
            runCurrent()
            vm.logSet(Muscle.CHEST)
            runCurrent()
            vm.logSet(Muscle.TRICEPS)
            runCurrent()

            assertEquals(listOf(0, 1), repository.setsFlow.value.map { it.orderInSession })
            val blocks = vm.uiState.value.blocks
            assertEquals(1, blocks[0].sets.size)
            assertEquals(1, blocks[1].sets.size)
        }

    @Test
    fun `deleting a logged set removes it as an explicit edit`() =
        runTest(dispatcher) {
            val vm = viewModel()
            runCurrent()
            vm.chooseExercise(Muscle.CHEST, "bench", "Bench Press")
            runCurrent()
            vm.logSet(Muscle.CHEST)
            runCurrent()
            val id = repository.setsFlow.value.single().id

            vm.deleteSet(id)
            runCurrent()
            assertTrue(repository.setsFlow.value.isEmpty())
            assertFalse(vm.uiState.value.canFinish)
        }

    @Test
    fun `logging without an exercise or with an invalid weight is ignored`() =
        runTest(dispatcher) {
            val vm = viewModel()
            runCurrent()
            vm.logSet(Muscle.CHEST)
            runCurrent()
            assertTrue(repository.setsFlow.value.isEmpty())

            vm.chooseExercise(Muscle.CHEST, "bench", "Bench Press")
            vm.updateWeightText(Muscle.CHEST, "")
            runCurrent()
            vm.logSet(Muscle.CHEST)
            runCurrent()
            assertTrue(repository.setsFlow.value.isEmpty())
        }

    @Test
    fun `rir stepper clamps to the 0 to 5 range`() =
        runTest(dispatcher) {
            val vm = viewModel()
            runCurrent()
            vm.adjustRir(Muscle.CHEST, -10)
            runCurrent()
            assertEquals(0, vm.uiState.value.blocks[0].input.rir)
            vm.adjustRir(Muscle.CHEST, +10)
            runCurrent()
            assertEquals(5, vm.uiState.value.blocks[0].input.rir)
        }

    @Test
    fun `chosen exercise is restored from logged sets after process death`() =
        runTest(dispatcher) {
            repository.setsFlow.value =
                listOf(
                    LoggedSet(
                        id = 7L,
                        sessionId = TestData.SESSION_ID,
                        exerciseId = "bench",
                        muscle = Muscle.CHEST,
                        weightKg = 60.0,
                        reps = 8,
                        rir = 2,
                        orderInSession = 0,
                        loggedAtEpochMillis = 1L,
                    ),
                )
            val vm = viewModel()
            runCurrent()
            val chest = vm.uiState.value.blocks.first { it.muscle == Muscle.CHEST }
            assertEquals(SessionRunnerViewModel.ExerciseChoice("bench", "Bench Press"), chest.exercise)
            assertNull(vm.uiState.value.blocks.first { it.muscle == Muscle.TRICEPS }.exercise)
        }

    private fun TestScope.collectEvents(vm: SessionRunnerViewModel): List<SessionRunnerViewModel.Event> {
        val events = mutableListOf<SessionRunnerViewModel.Event>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.events.toList(events) }
        return events
    }
}
