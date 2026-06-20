package com.salem.worldcup2026.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.salem.worldcup2026.data.model.Match
import com.salem.worldcup2026.data.model.MatchStatus
import com.salem.worldcup2026.data.model.Team
import com.salem.worldcup2026.data.model.TournamentData
import com.salem.worldcup2026.data.repo.DataOrigin
import com.salem.worldcup2026.data.repo.TournamentRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val origin: DataOrigin = DataOrigin.OFFLINE,
    val data: TournamentData = TournamentData(),
    val teams: Map<String, Team> = emptyMap(),
    val favorites: Set<String> = emptySet()
)

class TournamentViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = TournamentRepository(app.applicationContext)

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        // Show bundled data instantly, then pull live data and keep polling.
        viewModelScope.launch {
            val offline = repo.loadBundled()
            _state.value = _state.value.copy(
                loading = false,
                data = offline,
                teams = repo.teamMap(offline)
            )
            while (true) {
                refresh()
                // Poll faster while a match is live, slower otherwise.
                val hasLive = _state.value.data.matches.any { it.status == MatchStatus.LIVE }
                delay(if (hasLive) 30_000 else 120_000)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(refreshing = true)
            val result = repo.refresh()
            _state.value = _state.value.copy(
                loading = false,
                refreshing = false,
                origin = result.origin,
                data = result.data,
                teams = repo.teamMap(result.data)
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
