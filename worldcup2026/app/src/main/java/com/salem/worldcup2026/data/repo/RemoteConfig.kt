package com.salem.worldcup2026.data.repo

/**
 * Optional live-data provider configuration.
 *
 * The app ships fully working with bundled data. To stream real live scores,
 * drop in an API key from a football data provider (e.g. football-data.org,
 * which exposes the FIFA World Cup competition) and implement [LiveDataSource].
 * Kept intentionally simple so it can be wired without touching the UI layer.
 */
object RemoteConfig {
    // Leave blank to run purely on bundled data.
    const val API_KEY: String = ""
    const val BASE_URL: String = "https://api.football-data.org/v4/"
    val enabled: Boolean get() = API_KEY.isNotBlank()
}
