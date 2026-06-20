package com.salem.worldcup2026.data.repo

enum class Provider { WORLDCUP26, FOOTBALL_DATA, THESPORTSDB }

/**
 * Live data provider configuration. The app fetches REAL FIFA World Cup data at
 * runtime. By default it needs no setup at all.
 *
 * ── worldcup26.ir (default, COMPLETE data, no key) ────────────────────────────
 * Public API from github.com/rezarahiminia/worldcup2026. Returns the entire
 * tournament — all 104 games with scores and goal scorers, every group table,
 * all 48 teams with real flags, and the full knockout bracket — with no signup.
 * This is the default and requires nothing.
 *
 * ── football-data.org (alternative) ───────────────────────────────────────────
 * Set [FOOTBALL_DATA_KEY] (free key, email signup) to use it instead. Full World
 * Cup competition on the free tier.
 *
 * ── TheSportsDB (alternative) ─────────────────────────────────────────────────
 * Selectable via [PROVIDER_OVERRIDE]. Free public key returns real but capped
 * data; a premium key lifts the caps.
 *
 * Set [PROVIDER_OVERRIDE] to force a specific provider; otherwise the active one
 * is chosen automatically (football-data.org when its key is set, else
 * worldcup26.ir).
 */
object RemoteConfig {

    /** Force a provider, or null to auto-select. */
    val PROVIDER_OVERRIDE: Provider? = null

    // ── worldcup26.ir ──────────────────────────────────────────────────
    const val WC26_BASE: String = "https://worldcup26.ir"

    // ── football-data.org ──────────────────────────────────────────────
    const val FOOTBALL_DATA_KEY: String = ""
    const val FOOTBALL_DATA_BASE: String = "https://api.football-data.org/v4"
    const val FOOTBALL_DATA_COMPETITION: String = "WC"

    // ── TheSportsDB ────────────────────────────────────────────────────
    const val API_KEY: String = "3"
    const val BASE_V1: String = "https://www.thesportsdb.com/api/v1/json"
    const val LEAGUE_ID: String = "4429"
    const val SEASON: String = "2026"

    val activeProvider: Provider
        get() = PROVIDER_OVERRIDE ?: when {
            FOOTBALL_DATA_KEY.isNotBlank() -> Provider.FOOTBALL_DATA
            else -> Provider.WORLDCUP26
        }

    val isPremium: Boolean
        get() = activeProvider != Provider.THESPORTSDB ||
            (API_KEY.isNotBlank() && API_KEY != "3")
}
