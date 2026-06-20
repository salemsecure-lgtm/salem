package com.salem.worldcup2026.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.salem.worldcup2026.data.model.Match
import com.salem.worldcup2026.data.model.MatchStatus
import com.salem.worldcup2026.data.model.Team
import com.salem.worldcup2026.data.model.TournamentData
import com.salem.worldcup2026.data.repo.TournamentRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UiState(
    val loading: Boolean = true,    // first fetch in progress, no data yet
    val refreshing: Boolean = false,
    val error: Boolean = false,     // last fetch failed and there is no data to show
    val data: TournamentData = TournamentData(),
    val teams: Map<String, Team> = emptyMap(),
    val favorites: Set<String> = emptySet()
) {
    val hasData: Boolean get() = data.matches.isNotEmpty()
}

class TournamentViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = TournamentRepository()

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        // Online-only: keep polling live data. Poll faster while a match is live.
        viewModelScope.launch {
            while (true) {
                doRefresh()
                val hasLive = _state.value.data.matches.any { it.status == MatchStatus.LIVE }
                delay(if (hasLive) 30_000 else 120_000)
            }
        }
    }

    /** Manual pull-to-refresh / retry. */
    fun refresh() {
        viewModelScope.launch { doRefresh() }
    }

    private suspend fun doRefresh() {
        _state.value = _state.value.copy(refreshing = true)
        val result = repo.refresh()
        _state.value = if (result != null) {
            _state.value.copy(
                loading = false,
                refreshing = false,
                error = false,
                data = result,
                teams = repo.teamMap(result)
            )
        } else {
            // Keep any data we already have; only surface an error when empty.
            _state.value.copy(
                loading = false,
                refreshing = false,
                error = !_state.value.hasData
            )
        }
    }

    fun toggleFavorite(teamId: String) {
        val fav = _state.value.favorites.toMutableSet()
        if (!fav.add(teamId)) fav.remove(teamId)
        _state.value = _state.value.copy(favorites = fav)
    }

    fun team(id: String): Team? = _state.value.teams[id]

    fun matchById(id: String): Match? = _state.value.data.matches.firstOrNull { it.id == id }
}
