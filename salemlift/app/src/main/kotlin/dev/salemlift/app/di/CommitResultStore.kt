package dev.salemlift.app.di

import dev.salemlift.data.CommitOutcome

/**
 * In-memory hand-off of the commit result from the feedback screen to the
 * summary screen. If the process dies in between, the summary screen falls
 * back to the persisted decisions via [dev.salemlift.data.TrainingRepository.decisionsFor].
 */
class CommitResultStore {
    @Volatile
    private var stored: Pair<Long, CommitOutcome>? = null

    fun store(
        sessionId: Long,
        outcome: CommitOutcome,
    ) {
        stored = sessionId to outcome
    }

    fun outcomeFor(sessionId: Long): CommitOutcome? = stored?.takeIf { it.first == sessionId }?.second
}
