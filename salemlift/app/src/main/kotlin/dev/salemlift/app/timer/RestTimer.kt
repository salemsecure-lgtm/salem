package dev.salemlift.app.timer

/**
 * Pure rest-timer arithmetic (SPEC §6: drift < 250 ms). Remaining time is
 * always recomputed from a monotonic-clock anchor (SystemClock.elapsedRealtime
 * in production) instead of accumulating tick deltas, so drift is bounded by
 * one UI tick regardless of how long the timer runs or how ticks get delayed.
 */
data class RestTimer(
    val durationMillis: Long,
    /** Monotonic timestamp when the timer (re)started. */
    val anchorMillis: Long,
    /** Frozen remaining time while paused; null while running. */
    val pausedRemainingMillis: Long? = null,
) {
    init {
        require(durationMillis > 0) { "duration must be positive, was $durationMillis" }
    }

    val isPaused: Boolean get() = pausedRemainingMillis != null

    fun remainingMillis(nowMillis: Long): Long {
        val paused = pausedRemainingMillis
        val remaining = paused ?: (durationMillis - (nowMillis - anchorMillis))
        return remaining.coerceAtLeast(0L)
    }

    fun isFinished(nowMillis: Long): Boolean = remainingMillis(nowMillis) == 0L

    fun pause(nowMillis: Long): RestTimer =
        if (isPaused) this else copy(pausedRemainingMillis = remainingMillis(nowMillis))

    fun resume(nowMillis: Long): RestTimer {
        val paused = pausedRemainingMillis ?: return this
        // Re-anchor so the frozen remaining time continues from now.
        return copy(
            anchorMillis = nowMillis - (durationMillis - paused),
            pausedRemainingMillis = null,
        )
    }

    /** Add or remove time (e.g. +30 s button); clamps a paused timer at zero. */
    fun adjust(deltaMillis: Long): RestTimer {
        val paused = pausedRemainingMillis
        return if (paused != null) {
            copy(pausedRemainingMillis = (paused + deltaMillis).coerceAtLeast(0L))
        } else {
            copy(anchorMillis = anchorMillis + deltaMillis)
        }
    }

    companion object {
        fun start(
            durationMillis: Long,
            nowMillis: Long,
        ): RestTimer = RestTimer(durationMillis = durationMillis, anchorMillis = nowMillis)
    }
}
