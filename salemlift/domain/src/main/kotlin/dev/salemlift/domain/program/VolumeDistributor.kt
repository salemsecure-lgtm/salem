package dev.salemlift.domain.program

import dev.salemlift.domain.model.Distribution

/**
 * Distributes a muscle's weekly set target across the sessions that train it
 * (DOMAIN.md §5.2): as evenly as possible, remainder to the earliest sessions
 * (where the muscle is freshest), and week-over-week per-session increases
 * capped at +2.
 */
public object VolumeDistributor {
    /** Max per-session increase for one muscle between consecutive weeks (DOMAIN.md §5.2). */
    public const val MAX_SESSION_INCREASE: Int = 2

    /** Fresh, even distribution: e.g. 13 sets over 2 sessions → [7, 6]. */
    public fun distribute(
        weeklySets: Int,
        sessionCount: Int,
    ): List<Int> {
        require(weeklySets >= 0) { "weekly sets cannot be negative, was $weeklySets" }
        require(sessionCount >= 1) { "session count must be at least 1, was $sessionCount" }
        val base = weeklySets / sessionCount
        val remainder = weeklySets % sessionCount
        return List(sessionCount) { index -> if (index < remainder) base + 1 else base }
    }

    /**
     * Moves an existing distribution toward a new weekly target. Increases go
     * +1 at a time to the earliest sessions, never exceeding the previous
     * session value + [MAX_SESSION_INCREASE]; if the cap absorbs the whole
     * delta the result is flagged [Distribution.capped]. Decreases come off
     * the latest sessions first (keep early sessions fresh), never below 0.
     */
    public fun redistribute(
        previous: List<Int>,
        newWeeklyTarget: Int,
    ): Distribution {
        require(previous.isNotEmpty()) { "previous distribution must not be empty" }
        require(previous.all { it >= 0 }) { "previous distribution cannot contain negatives: $previous" }
        require(newWeeklyTarget >= 0) { "weekly target cannot be negative, was $newWeeklyTarget" }

        val current = previous.toMutableList()
        var delta = newWeeklyTarget - current.sum()

        while (delta > 0) {
            // Round-robin +1: the session with the smallest increase so far,
            // ties resolving to the earliest (freshest) session.
            val index =
                current.indices
                    .filter { current[it] - previous[it] < MAX_SESSION_INCREASE }
                    .minByOrNull { current[it] - previous[it] }
                    ?: return Distribution(current.toList(), capped = true)
            current[index] = current[index] + 1
            delta--
        }
        if (delta < 0) {
            // Invariant: toRemove <= sum(current), so the loop always ends by
            // toRemove reaching 0 before the index can run out.
            var toRemove = -delta
            var index = current.size - 1
            while (toRemove > 0) {
                val take = minOf(toRemove, current[index])
                current[index] = current[index] - take
                toRemove -= take
                index--
            }
        }
        return Distribution(current.toList(), capped = false)
    }
}
