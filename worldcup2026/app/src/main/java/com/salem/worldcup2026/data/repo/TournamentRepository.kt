package com.salem.worldcup2026.data.repo

import android.content.Context
import com.salem.worldcup2026.data.model.Match
import com.salem.worldcup2026.data.model.MatchStatus
import com.salem.worldcup2026.data.model.TournamentData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/** Where the currently displayed data came from. */
enum class DataOrigin { LIVE, OFFLINE }

data class LoadResult(val data: TournamentData, val origin: DataOrigin)

/**
 * Single source of truth for tournament data.
 *
 * [refresh] pulls REAL World Cup data from the live provider ([LiveDataSource]).
 * If the network is unavailable or the provider returns nothing, it falls back
 * to the bundled snapshot in assets/tournament.json so the app still opens.
 */
class TournamentRepository(
    private val appContext: Context,
    private val live: LiveDataSource = LiveDataSourceFactory.create()
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Volatile private var bundled: TournamentData? = null

    suspend fun loadBundled(): TournamentData = withContext(Dispatchers.IO) {
        bundled?.let { return@withContext it }
        val raw = appContext.assets.open("tournament.json")
            .bufferedReader().use { it.readText() }
        json.decodeFromString(TournamentData.serializer(), raw).also { bundled = it }
    }

    /** Fetch live data, falling back to the bundled snapshot on failure. */
    suspend fun refresh(): LoadResult {
        val remote = runCatching { live.fetch() }.getOrNull()
        return if (remote != null && remote.matches.isNotEmpty()) {
            LoadResult(remote, DataOrigin.LIVE)
        } else {
            LoadResult(loadBundled(), DataOrigin.OFFLINE)
        }
    }

    fun teamMap(data: TournamentData) = data.teams.associateBy { it.id }

    fun liveMatches(data: TournamentData): List<Match> =
        data.matches.filter { it.status == MatchStatus.LIVE }
            .sortedByDescending { it.minute }
}
