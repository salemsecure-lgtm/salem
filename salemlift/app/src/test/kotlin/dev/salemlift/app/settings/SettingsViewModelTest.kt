package dev.salemlift.app.settings

import dev.salemlift.domain.model.DefaultLandmarks
import dev.salemlift.domain.model.Experience
import dev.salemlift.domain.model.Landmarks
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SettingsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeSettingsRepository()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.viewModel(): SettingsViewModel {
        val viewModel = SettingsViewModel(repository)
        runCurrent()
        return viewModel
    }

    private fun TestScope.collectEvents(viewModel: SettingsViewModel): List<SettingsViewModel.Event> {
        val events = mutableListOf<SettingsViewModel.Event>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }
        return events
    }

    // ---- Landmarks ----------------------------------------------------------------

    @Test
    fun `editor opens with the muscle's stored landmarks`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            viewModel.openLandmarkEditor(Muscle.CHEST)
            runCurrent()
            val draft = assertNotNull(viewModel.uiState.value.editor)
            val seeds = DefaultLandmarks.seeds.getValue(Muscle.CHEST)
            assertEquals(seeds.mv, draft.mv)
            assertEquals(seeds.mev, draft.mev)
            assertEquals(seeds.mav, draft.mav)
            assertEquals(seeds.mrv, draft.mrv)
            assertTrue(draft.isValid)
        }

    @Test
    fun `an ordering violation invalidates the draft and gates save`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            viewModel.openLandmarkEditor(Muscle.CHEST) // MV 4, MEV 8, MAV 16, MRV 22
            repeat(5) { viewModel.adjustEditor(SettingsViewModel.LandmarkField.MV, +1) } // MV 9 > MEV 8
            runCurrent()
            val draft = assertNotNull(viewModel.uiState.value.editor)
            assertFalse(draft.isValid)

            viewModel.saveLandmarkEditor()
            runCurrent()

            assertTrue(repository.updatedLandmarks.isEmpty(), "invalid draft must not be saved")
            assertNotNull(viewModel.uiState.value.editor, "editor stays open on gated save")
        }

    @Test
    fun `a valid draft saves and closes the editor`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            viewModel.openLandmarkEditor(Muscle.CHEST)
            viewModel.adjustEditor(SettingsViewModel.LandmarkField.MRV, +2)
            viewModel.saveLandmarkEditor()
            runCurrent()

            assertEquals(
                listOf(Muscle.CHEST to Landmarks(mev = 8, mrv = 24, mv = 4, mav = 16)),
                repository.updatedLandmarks,
            )
            assertNull(viewModel.uiState.value.editor)
        }

    @Test
    fun `applyExperienceSeeds delegates and reports`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            val events = collectEvents(viewModel)
            viewModel.applyExperienceSeeds(Experience.BEGINNER)
            runCurrent()
            assertEquals(listOf(Experience.BEGINNER), repository.appliedExperiences)
            assertEquals(
                DefaultLandmarks.seedsFor(Experience.BEGINNER).getValue(Muscle.CHEST),
                viewModel.uiState.value.landmarks
                    .first { it.first == Muscle.CHEST }
                    .second,
            )
            val expected: List<SettingsViewModel.Event> =
                listOf(SettingsViewModel.Event.Message("Landmarks re-seeded for beginner"))
            assertEquals(expected, events)
        }

    // ---- Rules -----------------------------------------------------------------------

    @Test
    fun `rule deltas step within bounds and stop at plus-minus three`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            repeat(4) {
                viewModel.adjustRuleDelta("R7", +1)
                runCurrent()
            }
            // R7 default +1: three steps land on +3 (the cap); the fourth is ignored.
            assertEquals(3, repository.overrides["R7"])
            assertEquals(3, viewModel.uiState.value.rules.first { it.id == "R7" }.delta)

            repeat(7) {
                viewModel.adjustRuleDelta("R7", -1)
                runCurrent()
            }
            assertEquals(-3, repository.overrides["R7"])
        }

    @Test
    fun `R4 is read-only`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            val r4 = viewModel.uiState.value.rules.first { it.id == "R4" }
            assertFalse(r4.isEditable)
            assertNull(r4.delta)
            assertEquals("+2/+3 near/far MRV", r4.deltaLabel)

            viewModel.adjustRuleDelta("R4", +1)
            runCurrent()
            assertTrue(repository.overrides.isEmpty())
        }

    @Test
    fun `resetRules delegates refreshes rows and reports`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            val events = collectEvents(viewModel)
            viewModel.adjustRuleDelta("R7", +1)
            runCurrent()
            assertEquals(2, viewModel.uiState.value.rules.first { it.id == "R7" }.delta)

            viewModel.resetRules()
            runCurrent()

            assertEquals(1, repository.resetCount)
            assertEquals(1, viewModel.uiState.value.rules.first { it.id == "R7" }.delta)
            assertEquals(SettingsViewModel.Event.Message("Rules reset to defaults"), events.last())
        }

    // ---- Rest timer --------------------------------------------------------------------

    @Test
    fun `rest seconds step by fifteen and clamp to the allowed range`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            viewModel.adjustRestSeconds(+1)
            runCurrent()
            assertEquals(165, repository.restSecondsFlow.value)

            viewModel.adjustRestSeconds(+100)
            runCurrent()
            assertEquals(600, repository.restSecondsFlow.value)

            viewModel.adjustRestSeconds(-100)
            runCurrent()
            assertEquals(30, repository.restSecondsFlow.value)
        }

    // ---- Backup ---------------------------------------------------------------------------

    @Test
    fun `export hands the repository document to the writer and reports success`() =
        runTest(dispatcher) {
            repository.exportJson = """{"version":1,"landmarks":[]}"""
            val viewModel = viewModel()
            val events = collectEvents(viewModel)
            var written: String? = null

            viewModel.exportTo { written = it }
            runCurrent()

            assertEquals(repository.exportJson, written)
            assertEquals(SettingsViewModel.Event.Message("Backup exported"), events.last())
        }

    @Test
    fun `a failing writer reports export failure`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            val events = collectEvents(viewModel)
            viewModel.exportTo { error("disk full") }
            runCurrent()
            assertEquals(SettingsViewModel.Event.Message("Export failed"), events.last())
        }

    @Test
    fun `import waits for confirmation then delegates and reports`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            val events = collectEvents(viewModel)

            viewModel.importFrom { """{"version":1}""" }
            runCurrent()
            assertTrue(viewModel.uiState.value.importPending)
            assertTrue(repository.importedJsons.isEmpty(), "no import before confirmation")

            viewModel.confirmImport()
            runCurrent()

            assertEquals(listOf("""{"version":1}"""), repository.importedJsons)
            assertFalse(viewModel.uiState.value.importPending)
            assertEquals(SettingsViewModel.Event.Message("Backup imported"), events.last())
        }

    @Test
    fun `cancelling a pending import discards it`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            viewModel.importFrom { """{"version":1}""" }
            runCurrent()
            viewModel.cancelImport()
            runCurrent()
            assertFalse(viewModel.uiState.value.importPending)
            viewModel.confirmImport()
            runCurrent()
            assertTrue(repository.importedJsons.isEmpty())
        }

    @Test
    fun `a failing reader or import reports without state corruption`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            val events = collectEvents(viewModel)

            viewModel.importFrom { error("unreadable") }
            runCurrent()
            assertFalse(viewModel.uiState.value.importPending)
            assertEquals(SettingsViewModel.Event.Message("Could not read backup file"), events.last())

            repository.failImport = true
            viewModel.importFrom { """{"version":1}""" }
            runCurrent()
            viewModel.confirmImport()
            runCurrent()
            assertEquals(
                SettingsViewModel.Event.Message("Import failed — existing data unchanged"),
                events.last(),
            )
            assertFalse(viewModel.uiState.value.importPending)
        }
}
