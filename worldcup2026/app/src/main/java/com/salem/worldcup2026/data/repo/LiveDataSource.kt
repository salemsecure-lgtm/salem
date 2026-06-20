package com.salem.worldcup2026.data.repo

import com.salem.worldcup2026.data.model.TournamentData

/**
 * A provider of real, live World Cup data. Implementations return null when the
 * network is unavailable or the provider yields nothing usable, letting the
 * repository fall back to the bundled snapshot.
 */
interface LiveDataSource {
    suspend fun fetch(): TournamentData?
}

/** Creates the live data source (worldcup26.ir). */
object LiveDataSourceFactory {
    fun create(): LiveDataSource = WorldCup26Source()
}

/** Shared team metadata (emoji flag + FIFA code) keyed by country name. */
object TeamMeta {
    fun code(name: String?): String {
        if (name.isNullOrBlank()) return "?"
        FIFA_CODES[name]?.let { return it }
        return name.filter { it.isLetter() }.take(3).uppercase()
    }

    fun flag(name: String?): String = FLAGS[name.orEmpty()] ?: "🏳️"

    /** "Group A" / "GROUP_A" / "A" -> "A". */
    fun normalizeGroup(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        return raw.uppercase()
            .removePrefix("GROUP_")
            .removePrefix("GROUP")
            .replace("_", " ")
            .trim()
            .take(2)
            .trim()
    }
}

private val FIFA_CODES = mapOf(
    "Argentina" to "ARG", "France" to "FRA", "Spain" to "ESP", "England" to "ENG",
    "Brazil" to "BRA", "Portugal" to "POR", "Netherlands" to "NED", "Belgium" to "BEL",
    "Italy" to "ITA", "Germany" to "GER", "Croatia" to "CRO", "Morocco" to "MAR",
    "Colombia" to "COL", "Uruguay" to "URU", "Japan" to "JPN", "Senegal" to "SEN",
    "Switzerland" to "SUI", "Denmark" to "DEN", "Iran" to "IRN", "South Korea" to "KOR",
    "Korea Republic" to "KOR", "Austria" to "AUT", "Australia" to "AUS", "Ukraine" to "UKR",
    "Ecuador" to "ECU", "Sweden" to "SWE", "Turkey" to "TUR", "Türkiye" to "TUR", "Poland" to "POL",
    "Wales" to "WAL", "Serbia" to "SRB", "Nigeria" to "NGA", "Egypt" to "EGY",
    "Peru" to "PER", "Tunisia" to "TUN", "Algeria" to "ALG", "Chile" to "CHI",
    "Cameroon" to "CMR", "Ghana" to "GHA", "Costa Rica" to "CRC", "Qatar" to "QAT",
    "Ivory Coast" to "CIV", "Paraguay" to "PAR", "Norway" to "NOR", "Saudi Arabia" to "KSA",
    "Panama" to "PAN", "New Zealand" to "NZL", "United States" to "USA", "USA" to "USA",
    "Mexico" to "MEX", "Canada" to "CAN", "Scotland" to "SCO", "South Africa" to "RSA",
    "Czech Republic" to "CZE", "Czechia" to "CZE", "Bosnia-Herzegovina" to "BIH"
)

private val FLAGS = mapOf(
    "Argentina" to "🇦🇷", "France" to "🇫🇷", "Spain" to "🇪🇸", "England" to "🏴",
    "Brazil" to "🇧🇷", "Portugal" to "🇵🇹", "Netherlands" to "🇳🇱", "Belgium" to "🇧🇪",
    "Italy" to "🇮🇹", "Germany" to "🇩🇪", "Croatia" to "🇭🇷", "Morocco" to "🇲🇦",
    "Colombia" to "🇨🇴", "Uruguay" to "🇺🇾", "Japan" to "🇯🇵", "Senegal" to "🇸🇳",
    "Switzerland" to "🇨🇭", "Denmark" to "🇩🇰", "Iran" to "🇮🇷", "South Korea" to "🇰🇷",
    "Korea Republic" to "🇰🇷", "Austria" to "🇦🇹", "Australia" to "🇦🇺", "Ukraine" to "🇺🇦",
    "Ecuador" to "🇪🇨", "Sweden" to "🇸🇪", "Turkey" to "🇹🇷", "Türkiye" to "🇹🇷", "Poland" to "🇵🇱",
    "Wales" to "🏴", "Serbia" to "🇷🇸", "Nigeria" to "🇳🇬", "Egypt" to "🇪🇬",
    "Peru" to "🇵🇪", "Tunisia" to "🇹🇳", "Algeria" to "🇩🇿", "Chile" to "🇨🇱",
    "Cameroon" to "🇨🇲", "Ghana" to "🇬🇭", "Costa Rica" to "🇨🇷", "Qatar" to "🇶🇦",
    "Ivory Coast" to "🇨🇮", "Paraguay" to "🇵🇾", "Norway" to "🇳🇴", "Saudi Arabia" to "🇸🇦",
    "Panama" to "🇵🇦", "New Zealand" to "🇳🇿", "United States" to "🇺🇸", "USA" to "🇺🇸",
    "Mexico" to "🇲🇽", "Canada" to "🇨🇦", "Scotland" to "🏴", "South Africa" to "🇿🇦",
    "Czech Republic" to "🇨🇿", "Czechia" to "🇨🇿", "Bosnia-Herzegovina" to "🇧🇦"
)
