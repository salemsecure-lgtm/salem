package com.salem.worldcup2026.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.salem.worldcup2026.data.model.Match
import com.salem.worldcup2026.data.model.Team
import com.salem.worldcup2026.data.model.TournamentData
import com.salem.worldcup2026.data.repo.TournamentRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UiState(
    val loading: Boolean = true,
    val data: TournamentData = TournamentData(),
    val teams: Map<String, Team> = emptyMap(),
    val favorites: Set<String> = emptySet()
)

class TournamentViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = TournamentRepository(app.applicationContext)

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var baseData: TournamentData = TournamentData()
    private var elapsed = 0

    init {
        viewModelScope.launch {
            baseData = repo.load()
            publish()
            // Live clock: tick every 30s so live matches feel real.
            while (true) {
                delay(30_000)
                elapsed += 1
                publish()
            }
        }
    }

    private fun publish() {
        val data = repo.withLiveClock(baseData, elapsed)
        _state.value = _state.value.copy(
            loading = false,
            data = data,
            teams = repo.teamMap(data)
        )
    }

    fun toggleFavorite(teamId: String) {
        val fav = _state.value.favorites.toMutableSet()
        if (!fav.add(teamId)) fav.remove(teamId)
        _state.value = _state.value.copy(favorites = fav)
    }

    fun team(id: String): Team? = _state.value.teams[id]

    fun matchById(id: String): Match? = _state.value.data.matches.firstOrNull { it.id == id }
}
