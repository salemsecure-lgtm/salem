package com.salem.worldcup2026.data.repo

/**
 * Live data provider configuration.
 *
 * The app fetches REAL, COMPLETE FIFA World Cup 2026 data at runtime from the
 * public worldcup26.ir API (github.com/rezarahiminia/worldcup2026): all 104
 * games with scores and goal scorers, every group table, all 48 teams with real
 * flags, and the full knockout bracket. No key or signup required.
 */
object RemoteConfig {
    const val WC26_BASE: String = "https://worldcup26.ir"
}
