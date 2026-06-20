package com.salem.worldcup2026.data.remote

import android.util.Log
import com.salem.worldcup2026.data.repo.RemoteConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

/**
 * Minimal HTTP client for TheSportsDB v1 built on HttpURLConnection so the app
 * needs no third-party networking dependency. All calls are main-safe.
 */
class TheSportsDbClient(
    private val apiKey: String = RemoteConfig.API_KEY,
    private val baseV1: String = RemoteConfig.BASE_V1
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private suspend fun get(path: String): String? = withContext(Dispatchers.IO) {
        val url = URL("$baseV1/$apiKey/$path")
        var conn: HttpURLConnection? = null
        try {
            conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 12_000
                readTimeout = 12_000
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

    /** Upcoming fixtures (next ~15 on free tier). */
    suspend fun nextEvents(): List<EventDto> =
        decode<EventsResponse>(get("eventsnextleague.php?id=${RemoteConfig.LEAGUE_ID}"))?.events.orEmpty()

    /** Recent results (last ~15 on free tier). */
    suspend fun pastEvents(): List<EventDto> =
        decode<EventsResponse>(get("eventspastleague.php?id=${RemoteConfig.LEAGUE_ID}"))?.events.orEmpty()

    /** Whole-season fixtures (capped on free tier, complete on premium). */
    suspend fun seasonEvents(): List<EventDto> =
        decode<EventsResponse>(
            get("eventsseason.php?id=${RemoteConfig.LEAGUE_ID}&s=${RemoteConfig.SEASON}")
        )?.events.orEmpty()

    /** Group standings. */
    suspend fun standings(): List<TableRowDto> =
        decode<TableResponse>(
            get("lookuptable.php?l=${RemoteConfig.LEAGUE_ID}&s=${RemoteConfig.SEASON}")
        )?.table.orEmpty()

    companion object { private const val TAG = "TheSportsDbClient" }
}
