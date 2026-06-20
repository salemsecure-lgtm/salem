package com.salem.worldcup2026.data.repo

/**
 * Live data provider configuration.
 *
 * The app fetches REAL FIFA World Cup data at runtime from TheSportsDB.
 *
 *  - Out of the box it uses the free public key ("3"). That returns real teams,
 *    fixtures, results, scores and standings, but the free tier caps how many
 *    rows come back and does not expose minute-by-minute live progress.
 *
 *  - Paste your own TheSportsDB Premium key (https://www.thesportsdb.com/api.php)
 *    to lift those caps and get full standings, every fixture, and live scores.
 *
 * League 4429 / season 2026 is the men's FIFA World Cup 2026.
 */
object RemoteConfig {
    // Free public key. Replace with your premium key for full + live data.
    const val API_KEY: String = "3"

    const val BASE_V1: String = "https://www.thesportsdb.com/api/v1/json"
    const val LEAGUE_ID: String = "4429"     // FIFA World Cup (men)
    const val SEASON: String = "2026"

    /** A premium key is anything other than the shared free key. */
    val isPremium: Boolean get() = API_KEY.isNotBlank() && API_KEY != "3"
}
