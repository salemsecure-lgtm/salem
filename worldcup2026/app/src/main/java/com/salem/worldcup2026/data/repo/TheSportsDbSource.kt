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
 * Real World Cup data from TheSportsDB. Works on the free public key (real but
 * capped data, no live minute) and unlocks full + live data with a premium key.
 */
class TheSportsDbSource(
    private val client: TheSportsDbClient = TheSportsDbClient()
) : LiveDataSource {

    override suspend fun fetch(): TournamentData? {
        val events = buildList {
            addAll(client.pastEvents())
            addAll(client.nextEvents())
            addAll(client.seasonEvents())
        }.distinctBy { it.idEvent }

        val rows = client.standings()
        if (events.isEmpty() && rows.isEmpty()) return null

        val teams = LinkedHashMap<String, Team>()

        rows.forEach { r ->
            val id = r.idTeam ?: return@forEach
            teams[id] = Team(
                id = id,
                name = r.strTeam.orEmpty(),
                code = TeamMeta.code(r.strTeam),
                flag = TeamMeta.flag(r.strTeam),
                group = TeamMeta.normalizeGroup(r.strGroup),
                badgeUrl = r.strBadge.orEmpty().substringBefore("/tiny")
            )
        }
        events.forEach { e ->
            putTeam(teams, e.idHomeTeam, e.strHomeTeam, e.strHomeTeamBadge, e.strGroup)
            putTeam(teams, e.idAwayTeam, e.strAwayTeam, e.strAwayTeamBadge, e.strGroup)
        }

        return TournamentData(
            teams = teams.values.toList(),
            matches = events.mapNotNull { it.toMatch() }.sortedBy { it.kickoffEpoch },
            standings = rows.mapNotNull { it.toStanding() },
            topScorers = emptyList()
        )
    }

    private fun putTeam(
        map: MutableMap<String, Team>, id: String?, name: String?, badge: String?, group: String?
    ) {
        if (id == null) return
        val existing = map[id]
        if (existing == null) {
            map[id] = Team(
                id = id, name = name.orEmpty(), code = TeamMeta.code(name),
                flag = TeamMeta.flag(name), group = TeamMeta.normalizeGroup(group),
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
            group = TeamMeta.normalizeGroup(strGroup),
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

private fun mapStatus(status: String?): String {
    val s = status?.trim()?.uppercase() ?: return "SCHEDULED"
    return when {
        s == "NS" || s.isBlank() -> "SCHEDULED"
        s == "HT" -> "HALFTIME"
        s in setOf("FT", "AET", "PEN", "MATCH FINISHED", "FINISHED") -> "FINISHED"
        s in setOf("PST", "CANC", "ABD", "TBD", "AWD", "WO") -> "SCHEDULED"
        else -> "LIVE"
    }
}

private fun parseMinute(progress: String?, status: String?): Int {
    val p = progress?.trim()?.removeSuffix("'")?.toIntOrNull()
    if (p != null) return p
    return if (mapStatus(status) == "HALFTIME") 45 else 0
}

private fun parseEpoch(timestamp: String?, date: String?, time: String?): Long {
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
