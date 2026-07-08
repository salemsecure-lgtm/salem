package dev.salemlift.app.settings

import dev.salemlift.data.settings.RuleTables
import dev.salemlift.data.settings.SettingsRepository
import dev.salemlift.domain.engine.AutoregRule
import dev.salemlift.domain.model.DefaultLandmarks
import dev.salemlift.domain.model.Experience
import dev.salemlift.domain.model.Landmarks
import dev.salemlift.domain.model.Muscle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** In-memory [SettingsRepository] for ViewModel unit tests. */
class FakeSettingsRepository : SettingsRepository {
    val landmarksFlow = MutableStateFlow(DefaultLandmarks.seeds)
    val restSecondsFlow = MutableStateFlow(SettingsRepository.DEFAULT_REST_SECONDS)
    val overrides = linkedMapOf<String, Int>()
    val updatedLandmarks = mutableListOf<Pair<Muscle, Landmarks>>()
    val appliedExperiences = mutableListOf<Experience>()
    val importedJsons = mutableListOf<String>()
    var resetCount = 0
    var exportJson = """{"version":1}"""
    var failImport = false

    override fun landmarks(): Flow<Map<Muscle, Landmarks>> = landmarksFlow

    override suspend fun updateLandmarks(
        muscle: Muscle,
        landmarks: Landmarks,
    ) {
        updatedLandmarks += muscle to landmarks
        landmarksFlow.update { it + (muscle to landmarks) }
    }

    override suspend fun applyExperienceSeeds(experience: Experience) {
        appliedExperiences += experience
        landmarksFlow.value = DefaultLandmarks.seedsFor(experience)
    }

    override suspend fun ruleTable(): List<AutoregRule> = RuleTables.tuned(overrides)

    override suspend fun updateRuleDelta(
        ruleId: String,
        delta: Int,
    ) {
        require(ruleId in RuleTables.editableRuleIds) { "not editable: $ruleId" }
        require(delta in RuleTables.DELTA_RANGE) { "delta out of range: $delta" }
        overrides[ruleId] = delta
    }

    override suspend fun resetRules() {
        resetCount++
        overrides.clear()
    }

    override fun restSeconds(): Flow<Int> = restSecondsFlow

    override suspend fun setRestSeconds(seconds: Int) {
        require(seconds in SettingsRepository.REST_SECONDS_RANGE) { "rest out of range: $seconds" }
        restSecondsFlow.value = seconds
    }

    override suspend fun exportBackup(): String = exportJson

    override suspend fun importBackup(json: String) {
        check(!failImport) { "import failure requested by test" }
        importedJsons += json
    }
}
