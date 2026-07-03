package dev.salemlift.app.timer

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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

class RestTimerViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private var now = 0L

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Tests must dismiss any still-running timer before returning: runTest's
    // cleanup advances virtual time until idle, and the tick loop never idles
    // while the fake clock stays frozen.
    private fun viewModel(handle: SavedStateHandle = SavedStateHandle()) =
        RestTimerViewModel(savedStateHandle = handle, clock = { now })

    @Test
    fun `ticks recompute remaining time from the clock anchor, not accumulated delays`() =
        runTest(dispatcher) {
            val vm = viewModel()
            vm.start(150_000)
            runCurrent()
            assertEquals(150_000, vm.uiState.value.remainingMillis)

            // The wall clock jumps 5 s while only 1 s of ticks get scheduled —
            // an anchored recompute lands exactly right, with zero drift.
            now = 5_000
            advanceTimeBy(1_000)
            runCurrent()
            assertEquals(145_000, vm.uiState.value.remainingMillis)
            assertTrue(vm.uiState.value.isVisible)
            assertFalse(vm.uiState.value.isFinished)
            vm.dismiss()
        }

    @Test
    fun `pause freezes remaining and resume continues from the frozen value`() =
        runTest(dispatcher) {
            val vm = viewModel()
            vm.start(150_000)
            runCurrent()

            now = 30_000
            vm.togglePause()
            runCurrent()
            assertTrue(vm.uiState.value.isPaused)
            assertEquals(120_000, vm.uiState.value.remainingMillis)

            now = 90_000
            advanceTimeBy(5_000)
            runCurrent()
            assertEquals(120_000, vm.uiState.value.remainingMillis)

            vm.togglePause()
            runCurrent()
            assertFalse(vm.uiState.value.isPaused)
            assertEquals(120_000, vm.uiState.value.remainingMillis)

            now = 91_000
            advanceTimeBy(1_000)
            runCurrent()
            assertEquals(119_000, vm.uiState.value.remainingMillis)
            vm.dismiss()
        }

    @Test
    fun `plus thirty seconds extends a running countdown`() =
        runTest(dispatcher) {
            val vm = viewModel()
            vm.start(150_000)
            vm.addThirtySeconds()
            runCurrent()
            assertEquals(180_000, vm.uiState.value.remainingMillis)
            vm.dismiss()
        }

    @Test
    fun `plus thirty seconds also extends a paused countdown`() =
        runTest(dispatcher) {
            val vm = viewModel()
            vm.start(150_000)
            runCurrent()
            now = 10_000
            vm.togglePause()
            vm.addThirtySeconds()
            runCurrent()
            assertEquals(170_000, vm.uiState.value.remainingMillis)
            assertTrue(vm.uiState.value.isPaused)
        }

    @Test
    fun `countdown finishes at zero and reports isFinished`() =
        runTest(dispatcher) {
            val vm = viewModel()
            vm.start(1_000)
            runCurrent()
            now = 2_000
            advanceTimeBy(400)
            runCurrent()
            assertEquals(0, vm.uiState.value.remainingMillis)
            assertTrue(vm.uiState.value.isFinished)
        }

    @Test
    fun `anchor persisted in SavedStateHandle survives process death`() =
        runTest(dispatcher) {
            val handle = SavedStateHandle()
            val first = viewModel(handle)
            first.start(150_000)
            runCurrent()

            // "Process death": a new ViewModel restores from the same handle
            // 30 s of monotonic time later.
            now = 30_000
            val second = viewModel(handle)
            runCurrent()
            assertTrue(second.uiState.value.isVisible)
            assertEquals(120_000, second.uiState.value.remainingMillis)
            first.dismiss()
            second.dismiss()
        }

    @Test
    fun `dismiss hides the timer and clears the persisted anchor`() =
        runTest(dispatcher) {
            val handle = SavedStateHandle()
            val vm = viewModel(handle)
            vm.start(150_000)
            runCurrent()
            vm.dismiss()
            runCurrent()
            assertFalse(vm.uiState.value.isVisible)

            val restored = viewModel(handle)
            runCurrent()
            assertFalse(restored.uiState.value.isVisible)
        }
}
