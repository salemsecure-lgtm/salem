package com.salem.worldcup2026.data.repo

import android.content.Context
import com.salem.worldcup2026.data.model.Match
import com.salem.worldcup2026.data.model.MatchStatus
import com.salem.worldcup2026.data.model.TournamentData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Single source of truth for tournament data.
 *
 * Loads a complete, internally-consistent World Cup 2026 dataset bundled in
 * assets/tournament.json so the app is fully functional offline. When a live
 * football data provider is configured (see [RemoteConfig]) the repository can
 * be extended to merge live scores on top of the bundled fixtures.
 */
class TournamentRepository(private val appContext: Context) {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Volatile
    private var cache: TournamentData? = null

    suspend fun load(): TournamentData = withContext(Dispatchers.IO) {
        cache?.let { return@withContext it }
        val raw = appContext.assets.open("tournament.json")
            .bufferedReader().use { it.readText() }
        val data = json.decodeFromString(TournamentData.serializer(), raw)
        cache = data
        data
    }

    /**
     * Advances the clock on any LIVE matches so the demo feels alive: the
     * minute ticks up and the match flips to FINISHED past 90'. In a production
     * build this is where [RemoteConfig]-driven polling would overwrite scores.
     */
    fun withLiveClock(data: TournamentData, elapsedMinutes: Int): TournamentData {
        if (elapsedMinutes <= 0) return data
        val matches = data.matches.map { m ->
            if (m.status != MatchStatus.LIVE) return@map m
            val newMinute = m.minute + elapsedMinutes
            if (newMinute >= 90) {
                m.copy(minute = 90, statusRaw = "FINISHED")
            } else {
                m.copy(minute = newMinute)
            }
        }
        return data.copy(matches = matches)
    }

    fun teamMap(data: TournamentData) = data.teams.associateBy { it.id }

    fun liveMatches(data: TournamentData): List<Match> =
        data.matches.filter { it.status == MatchStatus.LIVE }
            .sortedByDescending { it.minute }
}
