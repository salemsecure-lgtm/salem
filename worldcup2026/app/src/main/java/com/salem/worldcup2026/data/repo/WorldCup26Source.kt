package com.salem.worldcup2026.data.repo

import com.salem.worldcup2026.data.model.GroupStanding
import com.salem.worldcup2026.data.model.Match
import com.salem.worldcup2026.data.model.MatchEvent
import com.salem.worldcup2026.data.model.ScorerStat
import com.salem.worldcup2026.data.model.Team
import com.salem.worldcup2026.data.model.TournamentData
import com.salem.worldcup2026.data.remote.WcGame
import com.salem.worldcup2026.data.remote.WcGroup
import com.salem.worldcup2026.data.remote.WcStadium
import com.salem.worldcup2026.data.remote.WcTeam
import com.salem.worldcup2026.data.remote.WorldCup26Client
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Real, COMPLETE World Cup 2026 data from the worldcup26.ir API
 * (rezarahiminia/worldcup2026). No key required: all 104 games with scores and
 * goal scorers, every group table, all 48 teams (with real flag images), and
 * the knockout bracket. Goal scorers are parsed into a per-match event timeline
 * and aggregated into the top-scorers leaderboard.
 */
class WorldCup26Source(
    private val client: WorldCup26Client = WorldCup26Client()
) : LiveDataSource {

    override suspend fun fetch(): TournamentData? =
        map(client.teams(), client.games(), client.groups(), client.stadiums())

    /** Pure mapping from wire models to domain models (unit-testable). */
    fun map(
        teams: List<WcTeam>,
        games: List<WcGame>,
        groups: List<WcGroup>,
        stadiums: List<WcStadium>
    ): TournamentData? {
        if (teams.isEmpty() && games.isEmpty()) return null

        val domainTeams = teams.mapNotNull { t ->
            val id = t.id ?: return@mapNotNull null
            Team(
                id = id,
                name = t.name_en.orEmpty(),
                code = t.fifa_code ?: TeamMeta.code(t.name_en),
                flag = TeamMeta.flag(t.name_en),
                group = TeamMeta.normalizeGroup(t.groups),
                // The API's flag field is a real flag image; show it as the crest.
                badgeUrl = t.flag.orEmpty()
            )
        }

        val venues = stadiums.associate {
            (it.id ?: "") to Pair(it.name_en.orEmpty(), it.city_en.orEmpty())
        }

        val matches = games.mapNotNull { it.toMatch(venues) }.sortedBy { it.kickoffEpoch }

        val standings = groups.flatMap { g ->
            g.teams.mapNotNull { row ->
                val id = row.team_id ?: return@mapNotNull null
                GroupStanding(
                    teamId = id,
                    played = row.mp.toIntOr0(),
                    won = row.w.toIntOr0(),
                    drawn = row.d.toIntOr0(),
                    lost = row.l.toIntOr0(),
                    goalsFor = row.gf.toIntOr0(),
                    goalsAgainst = row.ga.toIntOr0()
                )
            }
        }

        return TournamentData(
            teams = domainTeams,
            matches = matches,
            standings = standings,
            topScorers = topScorers(matches)
        )
    }

    private fun WcGame.toMatch(venues: Map<String, Pair<String, String>>): Match? {
        val id = id ?: return null
        val home = home_team_id ?: return null
        val away = away_team_id ?: return null
        val events = buildList {
            addAll(parseScorers(home_scorers).map { it.toEvent(home) })
            addAll(parseScorers(away_scorers).map { it.toEvent(away) })
        }.sortedBy { it.minute }
        val venue = venues[stadium_id]
        return Match(
            id = id,
            homeId = home,
            awayId = away,
            homeScore = home_score.toIntOr0(),
            awayScore = away_score.toIntOr0(),
            statusRaw = mapStatus(finished, time_elapsed),
            minute = liveMinute(time_elapsed),
            kickoffEpoch = parseEpoch(local_date),
            stage = stageLabel(type),
            group = TeamMeta.normalizeGroup(group),
            venue = venue?.first.orEmpty(),
            city = venue?.second.orEmpty(),
            events = events
        )
    }

    private fun topScorers(matches: List<Match>): List<ScorerStat> {
        val tally = LinkedHashMap<Pair<String, String>, Int>() // (player, teamId) -> goals
        matches.forEach { m ->
            m.events.filter { it.type == "GOAL" && it.detail != "OG" }.forEach { ev ->
                val key = ev.player to ev.teamId
                tally[key] = (tally[key] ?: 0) + 1
            }
        }
        return tally.entries
            .map { ScorerStat(player = it.key.first, teamId = it.key.second, goals = it.value) }
            .sortedByDescending { it.goals }
            .take(20)
    }
}

/** A parsed goal: minute, player, own-goal flag. */
data class ParsedGoal(val minute: Int, val player: String, val ownGoal: Boolean)

private fun ParsedGoal.toEvent(teamId: String) = MatchEvent(
    minute = minute,
    type = "GOAL",
    teamId = teamId,
    player = player,
    detail = if (ownGoal) "OG" else ""
)

/**
 * Parses the API's scorer strings, e.g.
 *   {"D. Bobadilla 7'(OG)","F. Balogun 31'","F. Balogun 45'+5'"}
 * which mix straight and curly quotes and stoppage-time notation. Returns the
 * base minute and player for each goal.
 */
fun parseScorers(raw: String?): List<ParsedGoal> {
    if (raw == null) return emptyList()
    var s = raw.trim().trim('"')
    if (s.equals("null", ignoreCase = true) || s.isBlank()) return emptyList()
    s = s.trim('{', '}')
    s = s.replace(Regex("[“”„‟\"]"), "")
    if (s.isBlank()) return emptyList()
    return s.split(',').mapNotNull { part ->
        val entry = part.trim()
        if (entry.isBlank()) return@mapNotNull null
        val ownGoal = entry.contains("(OG)", ignoreCase = true)
        val cleaned = entry.replace(Regex("\\(OG\\)", RegexOption.IGNORE_CASE), "").trim()
        val match = Regex("(\\d+)").find(cleaned) ?: return@mapNotNull null
        val minute = match.value.toIntOrNull() ?: return@mapNotNull null
        val name = cleaned.substring(0, match.range.first).trim().trimEnd('-').trim()
        if (name.isBlank()) null else ParsedGoal(minute, name, ownGoal)
    }
}

private fun String?.toIntOr0() = this?.trim()?.toIntOrNull() ?: 0

private fun mapStatus(finished: String?, timeElapsed: String?): String {
    val te = timeElapsed?.trim()?.lowercase() ?: ""
    return when {
        finished.equals("TRUE", ignoreCase = true) || te == "finished" -> "FINISHED"
        te == "notstarted" || te.isBlank() -> "SCHEDULED"
        te.contains("ht") -> "HALFTIME"
        else -> "LIVE"
    }
}

private fun liveMinute(timeElapsed: String?): Int {
    val te = timeElapsed?.trim()?.lowercase() ?: return 0
    if (te == "notstarted" || te == "finished" || te.contains("ht")) return 0
    return Regex("(\\d+)").find(te)?.value?.toIntOrNull() ?: 0
}

private fun stageLabel(type: String?): String = when (type?.trim()?.lowercase()) {
    "group", null, "" -> "Group Stage"
    "r32" -> "Round of 32"
    "r16" -> "Round of 16"
    "qf" -> "Quarter-final"
    "sf" -> "Semi-final"
    "third" -> "Third-place"
    "final" -> "Final"
    else -> "Group Stage"
}

private fun parseEpoch(localDate: String?): Long {
    if (localDate.isNullOrBlank()) return 0L
    // "MM/dd/yyyy HH:mm" — parsed in the device timezone so the displayed time
    // matches the source string.
    return runCatching {
        SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US).parse(localDate)?.time ?: 0L
    }.getOrDefault(0L)
}
