package com.salem.worldcup2026.data.repo

import com.salem.worldcup2026.data.model.Match
import com.salem.worldcup2026.data.model.MatchStatus
import com.salem.worldcup2026.data.model.TournamentData

/**
 * Single source of truth for tournament data.
 *
 * [refresh] pulls REAL, live World Cup data from the provider ([LiveDataSource]).
 * The app is online-only: there is no bundled snapshot, so a failed fetch
 * returns null and the UI shows a loading / connection state instead.
 */
class TournamentRepository(
    private val live: LiveDataSource = LiveDataSourceFactory.create()
) {
    /** Fetch live data; returns null if the network/provider is unavailable. */
    suspend fun refresh(): TournamentData? {
        val remote = runCatching { live.fetch() }.getOrNull()
        return remote?.takeIf { it.matches.isNotEmpty() }
    }

    fun teamMap(data: TournamentData) = data.teams.associateBy { it.id }

    fun liveMatches(data: TournamentData): List<Match> =
        data.matches.filter { it.status == MatchStatus.LIVE }
            .sortedByDescending { it.minute }
}
