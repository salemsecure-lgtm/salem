package com.salem.worldcup2026.data.repo

enum class Provider { THESPORTSDB, FOOTBALL_DATA }

/**
 * Live data provider configuration. The app fetches REAL FIFA World Cup data at
 * runtime; pick a provider by supplying its key below.
 *
 * ── football-data.org (recommended for FULL data) ─────────────────────────────
 * Its free tier includes the entire FIFA World Cup competition: every match,
 * complete group standings, and the top-scorers list. Get a free key (email
 * signup, no card) at https://www.football-data.org/client/register and paste it
 * into [FOOTBALL_DATA_KEY]. When set, it is used automatically.
 *
 * ── TheSportsDB (default, zero-setup) ─────────────────────────────────────────
 * Ships with the free public key ("3"): real data, but the free tier caps how
 * many rows come back and omits live minutes. A TheSportsDB Premium key lifts
 * those caps. Used when no football-data key is provided.
 *
 * Neither minute-by-minute clock nor scorers is available on a fully free,
 * no-signup basis — full live data always needs a provider account key.
 */
object RemoteConfig {

    // ── football-data.org ──────────────────────────────────────────────
    // Paste your free key here to get the COMPLETE World Cup dataset.
    const val FOOTBALL_DATA_KEY: String = ""
    const val FOOTBALL_DATA_BASE: String = "https://api.football-data.org/v4"
    const val FOOTBALL_DATA_COMPETITION: String = "WC" // FIFA World Cup

    // ── TheSportsDB ────────────────────────────────────────────────────
    const val API_KEY: String = "3" // free public key; replace with a premium key
    const val BASE_V1: String = "https://www.thesportsdb.com/api/v1/json"
    const val LEAGUE_ID: String = "4429" // FIFA World Cup (men)
    const val SEASON: String = "2026"

    /** football-data.org wins when its key is present, else TheSportsDB. */
    val activeProvider: Provider
        get() = if (FOOTBALL_DATA_KEY.isNotBlank()) Provider.FOOTBALL_DATA else Provider.THESPORTSDB

    val isPremium: Boolean
        get() = activeProvider == Provider.FOOTBALL_DATA || (API_KEY.isNotBlank() && API_KEY != "3")
}
