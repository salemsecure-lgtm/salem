package dev.salemlift.app.onboarding

import dev.salemlift.app.FakeTrainingRepository
import dev.salemlift.app.settings.FakeSettingsRepository
import dev.salemlift.domain.model.DefaultLandmarks
import dev.salemlift.domain.model.Experience
import dev.salemlift.domain.model.MesoConfig
import dev.salemlift.domain.model.Muscle
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
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OnboardingViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val trainingRepository = FakeTrainingRepository()
    private val settingsRepository = FakeSettingsRepository()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = OnboardingViewModel(trainingRepository, settingsRepository)

    @Test
    fun `starts on welcome with the full-body suggestion for the default 3 days`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            runCurrent()
            val state = viewModel.uiState.value
            assertEquals(OnboardingViewModel.Step.WELCOME, state.step)
            assertEquals(OnboardingViewModel.DEFAULT_DAYS_PER_WEEK, state.daysPerWeek)
            assertEquals(SplitTemplates.fullBodyThreeDay, state.split)
            assertEquals(SplitTemplates.all, state.splitOptions)
            assertFalse(state.isSplitOverridden)
        }

    @Test
    fun `choosing an experience applies landmark seeds and advances to schedule`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            viewModel.next() // WELCOME -> EXPERIENCE

            viewModel.chooseExperience(Experience.BEGINNER)
            runCurrent()

            assertEquals(listOf(Experience.BEGINNER), settingsRepository.appliedExperiences)
            val state = viewModel.uiState.value
            assertEquals(OnboardingViewModel.Step.SCHEDULE, state.step)
            assertEquals(Experience.BEGINNER, state.experience)
        }

    @Test
    fun `days map to the suggested splits from the spec`() {
        assertEquals(SplitTemplates.fullBodyThreeDay, OnboardingViewModel.suggestedSplit(2))
        assertEquals(SplitTemplates.fullBodyThreeDay, OnboardingViewModel.suggestedSplit(3))
        assertEquals(SplitTemplates.upperLowerFourDay, OnboardingViewModel.suggestedSplit(4))
        assertEquals(SplitTemplates.pplSixDay, OnboardingViewModel.suggestedSplit(5))
        assertEquals(SplitTemplates.pplSixDay, OnboardingViewModel.suggestedSplit(6))
    }

    @Test
    fun `changing days updates the suggestion until the user overrides`() =
        runTest(dispatcher) {
            val viewModel = viewModel()

            viewModel.chooseDays(4)
            runCurrent()
            assertEquals(SplitTemplates.upperLowerFourDay, viewModel.uiState.value.split)

            viewModel.overrideSplit(SplitTemplates.pplThreeDay)
            viewModel.chooseDays(6)
            runCurrent()

            val state = viewModel.uiState.value
            assertEquals(SplitTemplates.pplThreeDay, state.split)
            assertEquals(6, state.daysPerWeek)
            assertTrue(state.isSplitOverridden)
        }

    @Test
    fun `days outside 2-6 are rejected`() {
        val viewModel = viewModel()
        assertFailsWith<IllegalArgumentException> { viewModel.chooseDays(1) }
        assertFailsWith<IllegalArgumentException> { viewModel.chooseDays(7) }
    }

    @Test
    fun `landmark preview mirrors the settings repository for the key muscles`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            runCurrent()
            val expected =
                OnboardingViewModel.PREVIEW_MUSCLES.map { muscle ->
                    muscle to DefaultLandmarks.seeds.getValue(muscle)
                }
            assertEquals(expected, viewModel.uiState.value.landmarkPreview)
            assertTrue(Muscle.CHEST in viewModel.uiState.value.landmarkPreview.map { it.first })
        }

    @Test
    fun `start creates the first mesocycle with the chosen split and emits Started`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            val events = mutableListOf<OnboardingViewModel.Event>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.events.toList(events)
            }
            viewModel.chooseDays(4)

            viewModel.start()
            runCurrent()

            assertEquals(
                listOf(SplitTemplates.upperLowerFourDay to MesoConfig()),
                trainingRepository.startedMesocycles,
            )
            assertEquals(listOf<OnboardingViewModel.Event>(OnboardingViewModel.Event.Started), events)
            assertFalse(viewModel.uiState.value.isStarting)
        }

    @Test
    fun `back walks the steps in reverse and stops at welcome`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            viewModel.next()
            viewModel.next()
            viewModel.next()
            runCurrent()
            assertEquals(OnboardingViewModel.Step.REVIEW, viewModel.uiState.value.step)

            viewModel.back()
            runCurrent()
            assertEquals(OnboardingViewModel.Step.SCHEDULE, viewModel.uiState.value.step)

            viewModel.back()
            viewModel.back()
            viewModel.back() // extra back is a no-op at WELCOME
            runCurrent()
            assertEquals(OnboardingViewModel.Step.WELCOME, viewModel.uiState.value.step)
        }
}
