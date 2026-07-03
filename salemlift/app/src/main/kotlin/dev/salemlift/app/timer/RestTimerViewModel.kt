package dev.salemlift.app.timer

import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Drives the rest-timer bottom bar. All remaining-time values are recomputed
 * from the [RestTimer]'s monotonic anchor on every tick — never accumulated —
 * so drift stays under one tick (SPEC §6: < 250 ms). The anchor is persisted
 * in [SavedStateHandle], so the countdown survives rotation and process death
 * (elapsedRealtime keeps running while the process is gone).
 */
class RestTimerViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val clock: () -> Long = SystemClock::elapsedRealtime,
    private val tickIntervalMillis: Long = TICK_INTERVAL_MILLIS,
) : ViewModel() {
    data class UiState(
        val isVisible: Boolean = false,
        val remainingMillis: Long = 0L,
        val durationMillis: Long = DEFAULT_REST_MILLIS,
        val isPaused: Boolean = false,
        val isFinished: Boolean = false,
    )

    private val mutableState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = mutableState

    private var timer: RestTimer? = null
    private var tickJob: Job? = null

    init {
        restore()
    }

    /** Starts (or restarts) the countdown; called whenever a set is logged. */
    fun start(durationMillis: Long = DEFAULT_REST_MILLIS) {
        timer = RestTimer.start(durationMillis = durationMillis, nowMillis = clock())
        persist()
        startTicking()
    }

    fun togglePause() {
        val current = timer ?: return
        val now = clock()
        timer = if (current.isPaused) current.resume(now) else current.pause(now)
        persist()
        startTicking()
    }

    fun addThirtySeconds() {
        val current = timer ?: return
        timer = current.adjust(THIRTY_SECONDS_MILLIS)
        persist()
        startTicking()
    }

    fun dismiss() {
        tickJob?.cancel()
        timer = null
        persist()
        mutableState.value = UiState()
    }

    private fun startTicking() {
        tickJob?.cancel()
        tickJob =
            viewModelScope.launch {
                publish()
                var current = timer
                while (current != null && !current.isPaused && !current.isFinished(clock())) {
                    delay(tickIntervalMillis)
                    publish()
                    current = timer
                }
            }
    }

    private fun publish() {
        val current = timer
        mutableState.value =
            if (current == null) {
                UiState()
            } else {
                val now = clock()
                UiState(
                    isVisible = true,
                    remainingMillis = current.remainingMillis(now),
                    durationMillis = current.durationMillis,
                    isPaused = current.isPaused,
                    isFinished = current.isFinished(now),
                )
            }
    }

    private fun persist() {
        val current = timer
        savedStateHandle[KEY_DURATION] = current?.durationMillis
        savedStateHandle[KEY_ANCHOR] = current?.anchorMillis
        savedStateHandle[KEY_PAUSED_REMAINING] = current?.pausedRemainingMillis
    }

    private fun restore() {
        val duration: Long = savedStateHandle[KEY_DURATION] ?: return
        val anchor: Long = savedStateHandle[KEY_ANCHOR] ?: return
        timer =
            RestTimer(
                durationMillis = duration,
                anchorMillis = anchor,
                pausedRemainingMillis = savedStateHandle[KEY_PAUSED_REMAINING],
            )
        startTicking()
    }

    companion object {
        const val DEFAULT_REST_MILLIS: Long = 150_000L
        const val THIRTY_SECONDS_MILLIS: Long = 30_000L
        private const val TICK_INTERVAL_MILLIS: Long = 200L
        private const val KEY_DURATION = "rest_timer_duration"
        private const val KEY_ANCHOR = "rest_timer_anchor"
        private const val KEY_PAUSED_REMAINING = "rest_timer_paused_remaining"
    }
}
