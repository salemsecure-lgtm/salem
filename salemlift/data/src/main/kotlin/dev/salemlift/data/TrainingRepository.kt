package dev.salemlift.data

import dev.salemlift.domain.model.JointPain
import dev.salemlift.domain.model.MesoConfig
import dev.salemlift.domain.model.Muscle
import dev.salemlift.domain.model.Performance
import dev.salemlift.domain.model.Pump
import dev.salemlift.domain.model.SessionAdvance
import dev.salemlift.domain.model.SetDecision
import dev.salemlift.domain.model.Soreness
import dev.salemlift.domain.model.Split
import dev.salemlift.domain.model.WeekEffort
import kotlinx.coroutines.flow.Flow

/** A planned training day surfaced to the UI. */
data class SessionSummary(
    val sessionId: Long,
    val mesoId: Long,
    val week: Int,
    val dayIndex: Int,
    val name: String,
    val isDeload: Boolean,
    val effort: WeekEffort,
    val loadMultiplier: Double,
    val state: SessionState,
    /** Prescribed sets per muscle for this session. */
    val muscleTargets: Map<Muscle, Int>,
)

enum class SessionState { PENDING, IN_PROGRESS, COMPLETED, SKIPPED }

/** One logged working set. */
data class LoggedSet(
    val id: Long = 0,
    val sessionId: Long,
    val exerciseId: String,
    val muscle: Muscle,
    val weightKg: Double,
    val reps: Int,
    val rir: Int,
    val orderInSession: Int,
    val loggedAtEpochMillis: Long,
)

/** Per-muscle feedback captured for a session (soreness may arrive later). */
data class FeedbackDraft(
    val muscle: Muscle,
    val soreness: Soreness,
    val pump: Pump,
    val jointPain: JointPain,
    val performance: Performance,
)

/** What the engine decided at commit, persisted for the UI's "why" history. */
data class CommitOutcome(
    val advance: SessionAdvance,
    /** Decisions keyed by muscle, as persisted (identical to advance.decisions). */
    val decisions: Map<Muscle, SetDecision>,
    /** The next planned session, if the mesocycle continues. */
    val nextSession: SessionSummary?,
)

/**
 * The tracker's persistence facade. All suspend/Flow; implementations keep
 * I/O off the main thread (Room generates that for suspend/Flow queries).
 */
interface TrainingRepository {
    /** Persist landmark seeds if absent, then observe them. */
    fun landmarks(): Flow<Map<Muscle, dev.salemlift.domain.model.Landmarks>>

    /** Instantiate + persist a new mesocycle from a split (ends any active one). */
    suspend fun startMesocycle(
        split: Split,
        config: MesoConfig,
    ): Long

    /** The active mesocycle's next pending/in-progress session, if any. */
    fun currentSession(): Flow<SessionSummary?>

    suspend fun markSessionStarted(sessionId: Long)

    /** Sets logged so far for a session, in order. */
    fun loggedSets(sessionId: Long): Flow<List<LoggedSet>>

    suspend fun logSet(set: LoggedSet): Long

    suspend fun deleteSet(id: Long)

    /**
     * Commit a session: persist feedback, run the engine (SessionAdvancer),
     * persist decisions + next states, materialize the next session's targets,
     * mark the session completed — all in one Room transaction.
     */
    suspend fun commitSession(
        sessionId: Long,
        feedback: List<FeedbackDraft>,
        manualDeloadRequest: Boolean = false,
    ): CommitOutcome

    /** Decisions recorded for a committed session (explainability history). */
    suspend fun decisionsFor(sessionId: Long): Map<Muscle, SetDecision>
}
