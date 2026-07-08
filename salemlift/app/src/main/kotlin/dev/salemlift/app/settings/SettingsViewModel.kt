package dev.salemlift.app.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.salemlift.data.settings.RuleTables
import dev.salemlift.data.settings.SettingsRepository
import dev.salemlift.domain.engine.AutoregRule
import dev.salemlift.domain.model.Experience
import dev.salemlift.domain.model.Landmarks
import dev.salemlift.domain.model.Muscle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Settings: landmark editing, rule-delta tuning, rest-timer preference, and
 * backup export/import. File I/O stays in the route (SAF launchers hand
 * suspend read/write lambdas into [exportTo]/[importFrom]).
 */
class SettingsViewModel(
    private val repository: SettingsRepository,
) : ViewModel() {
    enum class LandmarkField { MV, MEV, MAV, MRV }

    /** Editor draft for one muscle's landmarks; save is gated on [isValid]. */
    data class LandmarkDraft(
        val muscle: Muscle,
        val mv: Int,
        val mev: Int,
        val mav: Int,
        val mrv: Int,
    ) {
        /** The DOMAIN.md §2 ordering invariant: 1 ≤ MV ≤ MEV ≤ MAV ≤ MRV. */
        val isValid: Boolean = mv >= 1 && mv <= mev && mev <= mav && mav <= mrv
    }

    /** One decision-table row for display; [delta] is null for R4 (not Fixed). */
    data class RuleRow(
        val id: String,
        val rationale: String,
        val delta: Int?,
        val isEditable: Boolean,
        val deltaLabel: String,
    )

    data class UiState(
        val landmarks: List<Pair<Muscle, Landmarks>> = emptyList(),
        val rules: List<RuleRow> = emptyList(),
        val restSeconds: Int = SettingsRepository.DEFAULT_REST_SECONDS,
        val editor: LandmarkDraft? = null,
        val importPending: Boolean = false,
    )

    sealed interface Event {
        data class Message(
            val text: String,
        ) : Event
    }

    private val rules = MutableStateFlow<List<RuleRow>>(emptyList())
    private val editor = MutableStateFlow<LandmarkDraft?>(null)
    private val pendingImportJson = MutableStateFlow<String?>(null)

    val uiState: StateFlow<UiState> =
        combine(
            repository.landmarks(),
            rules,
            repository.restSeconds(),
            editor,
            pendingImportJson,
        ) { landmarks, ruleRows, restSeconds, draft, pendingImport ->
            UiState(
                landmarks = landmarks.entries.sortedBy { it.key.ordinal }.map { it.key to it.value },
                rules = ruleRows,
                restSeconds = restSeconds,
                editor = draft,
                importPending = pendingImport != null,
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState())

    private val mutableEvents =
        MutableSharedFlow<Event>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val events: SharedFlow<Event> = mutableEvents

    init {
        viewModelScope.launch { refreshRules() }
    }

    // ---- Landmarks ------------------------------------------------------------

    fun openLandmarkEditor(muscle: Muscle) {
        val landmarks = uiState.value.landmarks.firstOrNull { it.first == muscle }?.second ?: return
        editor.value =
            LandmarkDraft(
                muscle = muscle,
                mv = landmarks.mv,
                mev = landmarks.mev,
                mav = landmarks.mav,
                mrv = landmarks.mrv,
            )
    }

    fun adjustEditor(
        field: LandmarkField,
        step: Int,
    ) {
        editor.update { draft ->
            draft?.run {
                when (field) {
                    LandmarkField.MV -> copy(mv = (mv + step).coerceIn(LANDMARK_RANGE))
                    LandmarkField.MEV -> copy(mev = (mev + step).coerceIn(LANDMARK_RANGE))
                    LandmarkField.MAV -> copy(mav = (mav + step).coerceIn(LANDMARK_RANGE))
                    LandmarkField.MRV -> copy(mrv = (mrv + step).coerceIn(LANDMARK_RANGE))
                }
            }
        }
    }

    /** No-op while the draft violates the ordering invariant (save stays disabled). */
    fun saveLandmarkEditor() {
        val draft = editor.value ?: return
        if (!draft.isValid) return
        viewModelScope.launch {
            repository.updateLandmarks(
                draft.muscle,
                Landmarks(mev = draft.mev, mrv = draft.mrv, mv = draft.mv, mav = draft.mav),
            )
            editor.value = null
        }
    }

    fun dismissLandmarkEditor() {
        editor.value = null
    }

    /** Overwrites ALL landmarks with the experience-scaled seeds (confirmed in the UI). */
    fun applyExperienceSeeds(experience: Experience) {
        viewModelScope.launch {
            repository.applyExperienceSeeds(experience)
            mutableEvents.emit(Event.Message("Landmarks re-seeded for ${experience.name.lowercase()}"))
        }
    }

    // ---- Rules ------------------------------------------------------------------

    /** Steps an editable rule's delta, ignoring steps that leave −3..+3. */
    fun adjustRuleDelta(
        ruleId: String,
        step: Int,
    ) {
        val row = uiState.value.rules.firstOrNull { it.id == ruleId } ?: return
        val current = row.delta
        if (!row.isEditable || current == null) return
        val next = current + step
        if (next !in RuleTables.DELTA_RANGE) return
        viewModelScope.launch {
            repository.updateRuleDelta(ruleId, next)
            refreshRules()
        }
    }

    fun resetRules() {
        viewModelScope.launch {
            repository.resetRules()
            refreshRules()
            mutableEvents.emit(Event.Message("Rules reset to defaults"))
        }
    }

    // ---- Rest timer ---------------------------------------------------------------

    /** Steps the default rest duration in 15 s increments within 30–600 s. */
    fun adjustRestSeconds(steps: Int) {
        val current = uiState.value.restSeconds
        val next = (current + steps * REST_STEP_SECONDS).coerceIn(SettingsRepository.REST_SECONDS_RANGE)
        if (next == current) return
        viewModelScope.launch { repository.setRestSeconds(next) }
    }

    // ---- Backup ---------------------------------------------------------------------

    /** Exports the backup document through the route-provided writer (SAF stream). */
    fun exportTo(write: suspend (String) -> Unit) {
        viewModelScope.launch {
            reportOutcome("Backup exported", "Export failed") { write(repository.exportBackup()) }
        }
    }

    /** Reads a candidate document; import waits for [confirmImport] (destructive replace). */
    fun importFrom(read: suspend () -> String) {
        viewModelScope.launch {
            val result = runCatching { read() }
            result.exceptionOrNull()?.let { if (it is CancellationException) throw it }
            result.fold(
                onSuccess = { pendingImportJson.value = it },
                onFailure = { mutableEvents.emit(Event.Message("Could not read backup file")) },
            )
        }
    }

    fun confirmImport() {
        val json = pendingImportJson.value ?: return
        pendingImportJson.value = null
        viewModelScope.launch {
            reportOutcome("Backup imported", "Import failed — existing data unchanged") {
                repository.importBackup(json)
                refreshRules()
            }
        }
    }

    fun cancelImport() {
        pendingImportJson.value = null
    }

    // ---- Internals --------------------------------------------------------------------

    private suspend fun refreshRules() {
        rules.value = repository.ruleTable().map(::toRuleRow)
    }

    private suspend fun reportOutcome(
        successMessage: String,
        failureMessage: String,
        block: suspend () -> Unit,
    ) {
        val result = runCatching { block() }
        result.exceptionOrNull()?.let { if (it is CancellationException) throw it }
        mutableEvents.emit(Event.Message(if (result.isSuccess) successMessage else failureMessage))
    }

    private companion object {
        const val REST_STEP_SECONDS = 15
        val LANDMARK_RANGE = 1..99
    }
}

private fun toRuleRow(rule: AutoregRule): SettingsViewModel.RuleRow {
    val delta = RuleTables.fixedDelta(rule)
    return SettingsViewModel.RuleRow(
        id = rule.id,
        rationale = rule.rationale,
        delta = delta,
        isEditable = rule.id in RuleTables.editableRuleIds,
        deltaLabel = if (delta == null) R4_DELTA_LABEL else formatDelta(delta),
    )
}

private fun formatDelta(delta: Int): String = if (delta > 0) "+$delta" else delta.toString()

/** R4's distance-scaled delta is not editable in v1 (DOMAIN.md §5). */
private const val R4_DELTA_LABEL = "+2/+3 near/far MRV"
