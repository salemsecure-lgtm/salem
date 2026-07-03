package dev.salemlift.app.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.salemlift.app.di.CommitResultStore
import dev.salemlift.data.CommitOutcome
import dev.salemlift.data.TrainingRepository
import dev.salemlift.domain.model.Muscle
import dev.salemlift.domain.model.SetDecision
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Post-commit summary: renders every muscle's [SetDecision] with its rule ID
 * so the user sees exactly what the engine decided and why. Reads the fresh
 * [CommitOutcome] handed over by the feedback screen; after process death it
 * falls back to the persisted decisions (without the deload verdict, which is
 * only carried on the outcome).
 */
class SummaryViewModel(
    private val repository: TrainingRepository,
    private val commitStore: CommitResultStore,
    private val sessionId: Long,
) : ViewModel() {
    data class DecisionRow(
        val muscle: Muscle,
        val headline: String,
        val badges: List<String>,
    )

    data class UiState(
        val isLoading: Boolean = true,
        val rows: List<DecisionRow> = emptyList(),
        val deloadTriggered: Boolean = false,
        val deloadReasons: List<String> = emptyList(),
        val nextSessionName: String? = null,
    )

    private val mutableState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = mutableState

    init {
        viewModelScope.launch {
            val outcome = commitStore.outcomeFor(sessionId)
            mutableState.value =
                if (outcome != null) {
                    fromOutcome(outcome)
                } else {
                    fromPersistedDecisions(repository.decisionsFor(sessionId))
                }
        }
    }

    private fun fromOutcome(outcome: CommitOutcome): UiState =
        UiState(
            isLoading = false,
            rows =
                outcome.decisions.map { (muscle, decision) ->
                    row(muscle, decision, isSessionCapped = muscle in outcome.advance.cappedMuscles)
                },
            deloadTriggered = outcome.advance.deload.triggered,
            deloadReasons = outcome.advance.deload.reasons.map(DecisionText::deloadReasonLabel),
            nextSessionName =
                outcome.nextSession?.let { "Week ${it.week} · ${it.name}" },
        )

    private fun fromPersistedDecisions(decisions: Map<Muscle, SetDecision>): UiState =
        UiState(
            isLoading = false,
            rows = decisions.map { (muscle, decision) -> row(muscle, decision, isSessionCapped = false) },
        )

    private fun row(
        muscle: Muscle,
        decision: SetDecision,
        isSessionCapped: Boolean,
    ): DecisionRow =
        DecisionRow(
            muscle = muscle,
            headline = DecisionText.headline(decision),
            badges = DecisionText.badges(decision, isSessionCapped),
        )
}
