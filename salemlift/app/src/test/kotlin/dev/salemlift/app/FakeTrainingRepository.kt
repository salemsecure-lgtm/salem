package dev.salemlift.app

import dev.salemlift.data.CommitOutcome
import dev.salemlift.data.FeedbackDraft
import dev.salemlift.data.LoggedSet
import dev.salemlift.data.SessionState
import dev.salemlift.data.SessionSummary
import dev.salemlift.data.TrainingRepository
import dev.salemlift.domain.model.Landmarks
import dev.salemlift.domain.model.MesoConfig
import dev.salemlift.domain.model.Muscle
import dev.salemlift.domain.model.SetDecision
import dev.salemlift.domain.model.Split
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory [TrainingRepository] for ViewModel unit tests. */
class FakeTrainingRepository : TrainingRepository {
    val currentSessionFlow = MutableStateFlow<SessionSummary?>(null)
    val setsFlow = MutableStateFlow<List<LoggedSet>>(emptyList())
    val startedMesocycles = mutableListOf<Pair<Split, MesoConfig>>()
    val startedSessions = mutableListOf<Long>()

    data class Commit(
        val sessionId: Long,
        val feedback: List<FeedbackDraft>,
        val manualDeloadRequest: Boolean,
    )

    val commits = mutableListOf<Commit>()
    var commitOutcome: CommitOutcome? = null
    var persistedDecisions: Map<Muscle, SetDecision> = emptyMap()

    private var nextSetId = 1L

    override fun landmarks(): Flow<Map<Muscle, Landmarks>> = flowOf(emptyMap())

    override suspend fun startMesocycle(
        split: Split,
        config: MesoConfig,
    ): Long {
        startedMesocycles += split to config
        return 1L
    }

    override fun currentSession(): Flow<SessionSummary?> = currentSessionFlow

    override suspend fun markSessionStarted(sessionId: Long) {
        startedSessions += sessionId
        currentSessionFlow.update { session ->
            if (session?.sessionId == sessionId) session.copy(state = SessionState.IN_PROGRESS) else session
        }
    }

    override fun loggedSets(sessionId: Long): Flow<List<LoggedSet>> =
        setsFlow.map { sets -> sets.filter { it.sessionId == sessionId } }

    override suspend fun logSet(set: LoggedSet): Long {
        val id = nextSetId++
        setsFlow.update { it + set.copy(id = id) }
        return id
    }

    override suspend fun deleteSet(id: Long) {
        setsFlow.update { sets -> sets.filterNot { it.id == id } }
    }

    override suspend fun commitSession(
        sessionId: Long,
        feedback: List<FeedbackDraft>,
        manualDeloadRequest: Boolean,
    ): CommitOutcome {
        commits += Commit(sessionId, feedback, manualDeloadRequest)
        return checkNotNull(commitOutcome) { "configure commitOutcome before calling commitSession" }
    }

    override suspend fun sessionFor(sessionId: Long): SessionSummary? =
        currentSessionFlow.value?.takeIf { it.sessionId == sessionId }

    override suspend fun decisionsFor(sessionId: Long): Map<Muscle, SetDecision> = persistedDecisions
}
