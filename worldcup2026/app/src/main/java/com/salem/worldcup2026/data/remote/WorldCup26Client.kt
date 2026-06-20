package com.salem.worldcup2026.data.remote

import android.util.Log
import com.salem.worldcup2026.data.repo.RemoteConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

/**
 * HTTP client for the worldcup26.ir API. The GET endpoints are public (no auth)
 * and return the complete tournament: all 104 games (with scores and scorers),
 * all 48 teams, all 12 group tables, and the 16 stadiums.
 */
class WorldCup26Client(
    private val base: String = RemoteConfig.WC26_BASE
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private suspend fun get(path: String): String? = withContext(Dispatchers.IO) {
        val url = URL("$base/$path")
        var conn: HttpURLConnection? = null
        try {
            conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 12_000
                readTimeout = 15_000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "WC2026-Android")
            }
            if (conn.responseCode in 200..299) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else {
                Log.w(TAG, "HTTP ${conn.responseCode} for $path")
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "request failed for $path: ${e.message}")
            null
        } finally {
            conn?.disconnect()
        }
    }

    private inline fun <reified T> decode(body: String?): T? =
        body?.let { runCatching { json.decodeFromString<T>(it) }.getOrNull() }

    suspend fun teams(): List<WcTeam> =
        decode<WcTeamsResponse>(get("get/teams"))?.teams.orEmpty()

    suspend fun games(): List<WcGame> =
        decode<WcGamesResponse>(get("get/games"))?.games.orEmpty()

    suspend fun groups(): List<WcGroup> =
        decode<WcGroupsResponse>(get("get/groups"))?.groups.orEmpty()

    suspend fun stadiums(): List<WcStadium> =
        decode<WcStadiumsResponse>(get("get/stadiums"))?.stadiums.orEmpty()

    companion object { private const val TAG = "WorldCup26Client" }
}
