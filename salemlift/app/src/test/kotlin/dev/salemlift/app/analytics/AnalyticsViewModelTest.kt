package dev.salemlift.app.analytics

import dev.salemlift.data.analytics.E1rmPoint
import dev.salemlift.data.analytics.ExerciseRef
import dev.salemlift.data.analytics.FatigueSummary
import dev.salemlift.data.analytics.MesoProgress
import dev.salemlift.data.analytics.RuleFireCount
import dev.salemlift.data.analytics.WeekFatigue
import dev.salemlift.data.analytics.WeekTonnage
import dev.salemlift.data.analytics.WeekVolume
import dev.salemlift.domain.model.Landmarks
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
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AnalyticsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeAnalyticsRepository()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `state is Loading until the snapshot loads`() =
        runTest(dispatcher) {
            val viewModel = AnalyticsViewModel(repository)
            assertIs<AnalyticsViewModel.UiState.Loading>(viewModel.uiState.value)
            runCurrent()
            assertIs<AnalyticsViewModel.UiState.Ready>(viewModel.uiState.value)
        }

    @Test
    fun `no data yields empty tab states`() =
        runTest(dispatcher) {
            val viewModel = AnalyticsViewModel(repository)
            runCurrent()

            val state = assertIs<AnalyticsViewModel.UiState.Ready>(viewModel.uiState.value)
            assertTrue(state.volume.isEmpty)
            assertNull(state.volume.selected)
            assertTrue(state.strength.isEmpty)
            assertNull(state.strength.selected)
            assertTrue(state.cycle.isEmpty)
            assertEquals(emptyList(), repository.trendRequests)
        }

    @Test
    fun `volume tab defaults to the first muscle and follows selection`() =
        runTest(dispatcher) {
            repository.mesoId = 1L
            repository.weeklyVolume =
                mapOf(
                    Muscle.CHEST to CHEST_WEEKS,
                    Muscle.BACK to BACK_WEEKS,
                )
            repository.landmarks =
                mapOf(
                    Muscle.CHEST to CHEST_MARKS,
                    Muscle.BACK to BACK_MARKS,
                )
            val viewModel = AnalyticsViewModel(repository)
            runCurrent()

            var state = assertIs<AnalyticsViewModel.UiState.Ready>(viewModel.uiState.value)
            assertEquals(listOf(Muscle.CHEST, Muscle.BACK), state.volume.muscles)
            assertEquals(Muscle.CHEST, state.volume.selected)
            assertEquals(CHEST_WEEKS, state.volume.weeks)
            assertEquals(CHEST_MARKS, state.volume.landmarks)

            viewModel.selectMuscle(Muscle.BACK)
            runCurrent()
            state = assertIs<AnalyticsViewModel.UiState.Ready>(viewModel.uiState.value)
            assertEquals(Muscle.BACK, state.volume.selected)
            assertEquals(BACK_WEEKS, state.volume.weeks)
            assertEquals(BACK_MARKS, state.volume.landmarks)
        }

    @Test
    fun `strength tab defaults to the first exercise and caches trends per exercise`() =
        runTest(dispatcher) {
            repository.mesoId = 1L
            repository.exercises = listOf(BENCH, SQUAT)
            repository.trends =
                mapOf(
                    BENCH.id to listOf(point(week = 1, e1rm = 130.0)),
                    SQUAT.id to listOf(point(week = 1, e1rm = 170.0)),
                )
            repository.tonnage = TONNAGE
            val viewModel = AnalyticsViewModel(repository)
            runCurrent()

            var state = assertIs<AnalyticsViewModel.UiState.Ready>(viewModel.uiState.value)
            assertEquals(BENCH, state.strength.selected)
            assertEquals(listOf(point(week = 1, e1rm = 130.0)), state.strength.trend)
            assertEquals(TONNAGE, state.strength.tonnage)
            assertEquals(listOf(BENCH.id), repository.trendRequests)

            viewModel.selectExercise(SQUAT.id)
            runCurrent()
            state = assertIs<AnalyticsViewModel.UiState.Ready>(viewModel.uiState.value)
            assertEquals(SQUAT, state.strength.selected)
            assertEquals(listOf(point(week = 1, e1rm = 170.0)), state.strength.trend)

            // Re-selecting an already-loaded exercise must hit the cache, not the repository.
            viewModel.selectExercise(BENCH.id)
            runCurrent()
            state = assertIs<AnalyticsViewModel.UiState.Ready>(viewModel.uiState.value)
            assertEquals(BENCH, state.strength.selected)
            assertEquals(listOf(BENCH.id, SQUAT.id), repository.trendRequests)
        }

    @Test
    fun `cycle tab carries progress and fatigue through unchanged`() =
        runTest(dispatcher) {
            repository.mesoId = 7L
            repository.progress = PROGRESS
            repository.fatigue = FATIGUE
            val viewModel = AnalyticsViewModel(repository)
            runCurrent()

            val state = assertIs<AnalyticsViewModel.UiState.Ready>(viewModel.uiState.value)
            assertEquals(PROGRESS, state.cycle.progress)
            assertEquals(FATIGUE.weeks, state.cycle.weekFatigue)
            assertEquals(FATIGUE.ruleFires, state.cycle.ruleFires)
            assertTrue(!state.cycle.isEmpty)
        }

    @Test
    fun `an unknown selected exercise falls back to the first with history`() =
        runTest(dispatcher) {
            repository.mesoId = 1L
            repository.exercises = listOf(BENCH)
            repository.trends = mapOf(BENCH.id to listOf(point(week = 2, e1rm = 140.0)))
            val viewModel = AnalyticsViewModel(repository)
            runCurrent()

            viewModel.selectExercise("no-such-exercise")
            runCurrent()

            val state = assertIs<AnalyticsViewModel.UiState.Ready>(viewModel.uiState.value)
            assertEquals(BENCH, state.strength.selected)
            assertEquals(listOf(point(week = 2, e1rm = 140.0)), state.strength.trend)
        }

    private fun point(
        week: Int,
        e1rm: Double,
    ): E1rmPoint =
        E1rmPoint(
            sessionId = week.toLong(),
            week = week,
            dayIndex = 0,
            loggedAtEpochMillis = week * 1000L,
            e1rmKg = e1rm,
        )

    private companion object {
        val CHEST_WEEKS =
            listOf(
                WeekVolume(week = 1, performedSets = 8, prescribedSets = 8),
                WeekVolume(week = 2, performedSets = 4, prescribedSets = 9),
            )
        val BACK_WEEKS =
            listOf(
                WeekVolume(week = 1, performedSets = 10, prescribedSets = 10),
                WeekVolume(week = 2, performedSets = 0, prescribedSets = 11),
            )
        val CHEST_MARKS = Landmarks(mev = 8, mrv = 22, mav = 16)
        val BACK_MARKS = Landmarks(mev = 10, mrv = 25, mav = 18)
        val BENCH = ExerciseRef(id = "bench-press", name = "Bench Press")
        val SQUAT = ExerciseRef(id = "back-squat", name = "Back Squat")
        val TONNAGE = listOf(WeekTonnage(week = 1, tonnageKg = 5015.0), WeekTonnage(week = 2, tonnageKg = 1470.0))
        val PROGRESS =
            MesoProgress(
                mesoId = 7L,
                currentWeek = 2,
                totalWeeks = 5,
                isDeloadWeek = false,
                targetRir = 2,
                maxRir = 2,
                completedSessions = 5,
                totalSessions = 20,
            )
        val FATIGUE =
            FatigueSummary(
                weeks =
                    listOf(
                        WeekFatigue(
                            week = 1,
                            stillSoreCount = 1,
                            mildJointPainCount = 1,
                            significantJointPainCount = 1,
                            performanceDownCount = 1,
                        ),
                    ),
                ruleFires =
                    listOf(
                        RuleFireCount(ruleId = "R1", rationale = "Significant joint pain", count = 1),
                        RuleFireCount(ruleId = "R7", rationale = "Standard progression", count = 3),
                    ),
            )
    }
}
