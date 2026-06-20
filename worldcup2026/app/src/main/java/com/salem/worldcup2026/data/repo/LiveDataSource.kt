package com.salem.worldcup2026.data.repo

import com.salem.worldcup2026.data.model.GroupStanding
import com.salem.worldcup2026.data.model.Match
import com.salem.worldcup2026.data.model.Team
import com.salem.worldcup2026.data.model.TournamentData
import com.salem.worldcup2026.data.remote.EventDto
import com.salem.worldcup2026.data.remote.TableRowDto
import com.salem.worldcup2026.data.remote.TheSportsDbClient
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Fetches REAL World Cup data from TheSportsDB and maps it onto the app's domain
 * models. Returns null when the network is unavailable or the provider returns
 * nothing usable, so the repository can fall back to bundled data.
 */
class LiveDataSource(private val client: TheSportsDbClient = TheSportsDbClient()) {

    suspend fun fetch(): TournamentData? {
        val events = buildList {
            addAll(client.pastEvents())
            addAll(client.nextEvents())
            addAll(client.seasonEvents())
        }.distinctBy { it.idEvent }

        val rows = client.standings()
        if (events.isEmpty() && rows.isEmpty()) return null

        val teams = LinkedHashMap<String, Team>()

        // Teams from standings carry the authoritative group + crest.
        rows.forEach { r ->
            val id = r.idTeam ?: return@forEach
            teams[id] = Team(
                id = id,
                name = r.strTeam.orEmpty(),
                code = codeFor(r.strTeam),
                flag = flagFor(r.strTeam),
                group = normalizeGroup(r.strGroup),
                badgeUrl = r.strBadge.orEmpty().substringBefore("/tiny")
            )
        }
        // Teams from events fill in anyone not in the (possibly capped) table.
        events.forEach { e ->
            putTeam(teams, e.idHomeTeam, e.strHomeTeam, e.strHomeTeamBadge, e.strGroup)
            putTeam(teams, e.idAwayTeam, e.strAwayTeam, e.strAwayTeamBadge, e.strGroup)
        }

        val matches = events.mapNotNull { it.toMatch() }.sortedBy { it.kickoffEpoch }
        val standings = rows.mapNotNull { it.toStanding() }

        return TournamentData(
            teams = teams.values.toList(),
            matches = matches,
            standings = standings,
            topScorers = emptyList() // scorer feed requires a premium endpoint
        )
    }

    private fun putTeam(
        map: MutableMap<String, Team>, id: String?, name: String?, badge: String?, group: String?
    ) {
        if (id == null) return
        val existing = map[id]
        if (existing == null) {
            map[id] = Team(
                id = id, name = name.orEmpty(), code = codeFor(name),
                flag = flagFor(name), group = normalizeGroup(group),
                badgeUrl = badge.orEmpty()
            )
        } else if (existing.badgeUrl.isBlank() && !badge.isNullOrBlank()) {
            map[id] = existing.copy(badgeUrl = badge)
        }
    }

    private fun EventDto.toMatch(): Match? {
        val id = idEvent ?: return null
        val home = idHomeTeam ?: return null
        val away = idAwayTeam ?: return null
        return Match(
            id = id,
            homeId = home,
            awayId = away,
            homeScore = intHomeScore?.toIntOrNull() ?: 0,
            awayScore = intAwayScore?.toIntOrNull() ?: 0,
            statusRaw = mapStatus(strStatus),
            minute = parseMinute(strProgress, strStatus),
            kickoffEpoch = parseEpoch(strTimestamp, dateEvent, strTime),
            stage = roundLabel(intRound),
            group = normalizeGroup(strGroup),
            venue = strVenue.orEmpty(),
            city = strCity.orEmpty()
        )
    }

    private fun TableRowDto.toStanding(): GroupStanding? {
        val id = idTeam ?: return null
        return GroupStanding(
            teamId = id,
            played = intPlayed.toIntOr0(),
            won = intWin.toIntOr0(),
            drawn = intDraw.toIntOr0(),
            lost = intLoss.toIntOr0(),
            goalsFor = intGoalsFor.toIntOr0(),
            goalsAgainst = intGoalsAgainst.toIntOr0()
        )
    }
}

private fun String?.toIntOr0() = this?.trim()?.toIntOrNull() ?: 0

private fun normalizeGroup(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    // "Group A" -> "A"; already-bare letters pass through.
    return raw.removePrefix("Group").trim().take(2).trim()
}

private fun roundLabel(round: String?): String = when (round?.trim()) {
    null, "" -> "Group Stage"
    "1", "2", "3" -> "Group Stage"
    "4", "16" -> "Round of 16"
    "8" -> "Quarter-final"
    "32" -> "Round of 32"
    "2048" -> "Final"
    "1024" -> "Semi-final"
    "512" -> "Third-place"
    else -> "Group Stage"
}

/** TheSportsDB soccer status codes -> internal statusRaw. */
private fun mapStatus(status: String?): String {
    val s = status?.trim()?.uppercase() ?: return "SCHEDULED"
    return when {
        s == "NS" || s.isBlank() -> "SCHEDULED"
        s == "HT" -> "HALFTIME"
        s in setOf("FT", "AET", "PEN", "MATCH FINISHED", "FINISHED") -> "FINISHED"
        s in setOf("PST", "CANC", "ABD", "TBD", "AWD", "WO") -> "SCHEDULED"
        else -> "LIVE" // 1H, 2H, ET, LIVE, BT, P, INT, ...
    }
}

private fun parseMinute(progress: String?, status: String?): Int {
    val p = progress?.trim()?.removeSuffix("'")?.toIntOrNull()
    if (p != null) return p
    return if (mapStatus(status) == "HALFTIME") 45 else 0
}

private fun parseEpoch(timestamp: String?, date: String?, time: String?): Long {
    // strTimestamp is ISO UTC, e.g. 2026-06-20T03:00:00
    timestamp?.let {
        runCatching {
            val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            fmt.timeZone = TimeZone.getTimeZone("UTC")
            return fmt.parse(it)?.time ?: 0L
        }
    }
    if (!date.isNullOrBlank()) {
        runCatching {
            val t = if (time.isNullOrBlank()) "00:00:00" else time
            val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            fmt.timeZone = TimeZone.getTimeZone("UTC")
            return fmt.parse("$date $t")?.time ?: 0L
        }
    }
    return 0L
}

private fun codeFor(name: String?): String {
    if (name.isNullOrBlank()) return "?"
    FIFA_CODES[name]?.let { return it }
    return name.filter { it.isLetter() }.take(3).uppercase()
}

private fun flagFor(name: String?): String = FLAGS[name.orEmpty()] ?: "🏳️"

private val FIFA_CODES = mapOf(
    "Argentina" to "ARG", "France" to "FRA", "Spain" to "ESP", "England" to "ENG",
    "Brazil" to "BRA", "Portugal" to "POR", "Netherlands" to "NED", "Belgium" to "BEL",
    "Italy" to "ITA", "Germany" to "GER", "Croatia" to "CRO", "Morocco" to "MAR",
    "Colombia" to "COL", "Uruguay" to "URU", "Japan" to "JPN", "Senegal" to "SEN",
    "Switzerland" to "SUI", "Denmark" to "DEN", "Iran" to "IRN", "South Korea" to "KOR",
    "Korea Republic" to "KOR", "Austria" to "AUT", "Australia" to "AUS", "Ukraine" to "UKR",
    "Ecuador" to "ECU", "Sweden" to "SWE", "Turkey" to "TUR", "Poland" to "POL",
    "Wales" to "WAL", "Serbia" to "SRB", "Nigeria" to "NGA", "Egypt" to "EGY",
    "Peru" to "PER", "Tunisia" to "TUN", "Algeria" to "ALG", "Chile" to "CHI",
    "Cameroon" to "CMR", "Ghana" to "GHA", "Costa Rica" to "CRC", "Qatar" to "QAT",
    "Ivory Coast" to "CIV", "Paraguay" to "PAR", "Norway" to "NOR", "Saudi Arabia" to "KSA",
    "Panama" to "PAN", "New Zealand" to "NZL", "United States" to "USA", "USA" to "USA",
    "Mexico" to "MEX", "Canada" to "CAN", "Scotland" to "SCO", "South Africa" to "RSA",
    "Czech Republic" to "CZE", "Bosnia-Herzegovina" to "BIH"
)

private val FLAGS = mapOf(
    "Argentina" to "🇦🇷", "France" to "🇫🇷", "Spain" to "🇪🇸", "England" to "🏴",
    "Brazil" to "🇧🇷", "Portugal" to "🇵🇹", "Netherlands" to "🇳🇱", "Belgium" to "🇧🇪",
    "Italy" to "🇮🇹", "Germany" to "🇩🇪", "Croatia" to "🇭🇷", "Morocco" to "🇲🇦",
    "Colombia" to "🇨🇴", "Uruguay" to "🇺🇾", "Japan" to "🇯🇵", "Senegal" to "🇸🇳",
    "Switzerland" to "🇨🇭", "Denmark" to "🇩🇰", "Iran" to "🇮🇷", "South Korea" to "🇰🇷",
    "Korea Republic" to "🇰🇷", "Austria" to "🇦🇹", "Australia" to "🇦🇺", "Ukraine" to "🇺🇦",
    "Ecuador" to "🇪🇨", "Sweden" to "🇸🇪", "Turkey" to "🇹🇷", "Poland" to "🇵🇱",
    "Wales" to "🏴", "Serbia" to "🇷🇸", "Nigeria" to "🇳🇬", "Egypt" to "🇪🇬",
    "Peru" to "🇵🇪", "Tunisia" to "🇹🇳", "Algeria" to "🇩🇿", "Chile" to "🇨🇱",
    "Cameroon" to "🇨🇲", "Ghana" to "🇬🇭", "Costa Rica" to "🇨🇷", "Qatar" to "🇶🇦",
    "Ivory Coast" to "🇨🇮", "Paraguay" to "🇵🇾", "Norway" to "🇳🇴", "Saudi Arabia" to "🇸🇦",
    "Panama" to "🇵🇦", "New Zealand" to "🇳🇿", "United States" to "🇺🇸", "USA" to "🇺🇸",
    "Mexico" to "🇲🇽", "Canada" to "🇨🇦", "Scotland" to "🏴", "South Africa" to "🇿🇦",
    "Czech Republic" to "🇨🇿", "Bosnia-Herzegovina" to "🇧🇦"
)
