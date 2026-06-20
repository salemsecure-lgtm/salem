package com.salem.worldcup2026.data.remote

import android.util.Log
import com.salem.worldcup2026.data.repo.RemoteConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

/**
 * HTTP client for football-data.org v4. Authenticates with the X-Auth-Token
 * header. Main-safe; returns null on any failure so callers can fall back.
 */
class FootballDataClient(
    private val apiKey: String = RemoteConfig.FOOTBALL_DATA_KEY,
    private val base: String = RemoteConfig.FOOTBALL_DATA_BASE,
    private val competition: String = RemoteConfig.FOOTBALL_DATA_COMPETITION
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private suspend fun get(path: String): String? = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext null
        val url = URL("$base/$path")
        var conn: HttpURLConnection? = null
        try {
            conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 12_000
                readTimeout = 12_000
                requestMethod = "GET"
                setRequestProperty("X-Auth-Token", apiKey)
                setRequestProperty("Accept", "application/json")
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

    suspend fun matches(): List<FdMatch> =
        decode<FdMatchesResponse>(get("competitions/$competition/matches"))?.matches.orEmpty()

    suspend fun standings(): List<FdStandingGroup> =
        decode<FdStandingsResponse>(get("competitions/$competition/standings"))?.standings.orEmpty()

    suspend fun scorers(): List<FdScorer> =
        decode<FdScorersResponse>(get("competitions/$competition/scorers?limit=20"))?.scorers.orEmpty()

    companion object { private const val TAG = "FootballDataClient" }
}
