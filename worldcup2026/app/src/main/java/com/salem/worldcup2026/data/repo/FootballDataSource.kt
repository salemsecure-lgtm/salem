package com.salem.worldcup2026.data.repo

import com.salem.worldcup2026.data.model.GroupStanding
import com.salem.worldcup2026.data.model.Match
import com.salem.worldcup2026.data.model.ScorerStat
import com.salem.worldcup2026.data.model.Team
import com.salem.worldcup2026.data.model.TournamentData
import com.salem.worldcup2026.data.remote.FdMatch
import com.salem.worldcup2026.data.remote.FdScorer
import com.salem.worldcup2026.data.remote.FdStandingGroup
import com.salem.worldcup2026.data.remote.FdTeam
import com.salem.worldcup2026.data.remote.FootballDataClient
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Real World Cup data from football-data.org v4. The free tier returns the
 * COMPLETE competition: every match, full group standings, and top scorers.
 */
class FootballDataSource(
    private val client: FootballDataClient = FootballDataClient()
) : LiveDataSource {

    override suspend fun fetch(): TournamentData? =
        map(client.matches(), client.standings(), client.scorers())

    /** Pure mapping from wire models to domain models (unit-testable). */
    fun map(
        matches: List<FdMatch>,
        standingGroups: List<FdStandingGroup>,
        scorers: List<FdScorer>
    ): TournamentData? {
        if (matches.isEmpty() && standingGroups.isEmpty()) return null

        val teams = LinkedHashMap<String, Team>()

        // Group label per team comes from the standings groups.
        standingGroups.filter { it.type == null || it.type == "TOTAL" }.forEach { g ->
            val grp = TeamMeta.normalizeGroup(g.group)
            g.table.forEach { row -> putTeam(teams, row.team, grp) }
        }
        matches.forEach { m ->
            val grp = TeamMeta.normalizeGroup(m.group)
            putTeam(teams, m.homeTeam, grp)
            putTeam(teams, m.awayTeam, grp)
        }
        scorers.forEach { putTeam(teams, it.team, "") }

        val standings = standingGroups
            .filter { it.type == null || it.type == "TOTAL" }
            .flatMap { g -> g.table.mapNotNull { it.toStanding() } }

        return TournamentData(
            teams = teams.values.toList(),
            matches = matches.mapNotNull { it.toMatch() }.sortedBy { it.kickoffEpoch },
            standings = standings,
            topScorers = scorers.mapNotNull { it.toStat() }
        )
    }

    private fun teamId(t: FdTeam): String? = t.id?.let { "fd_$it" }

    private fun putTeam(map: MutableMap<String, Team>, t: FdTeam, group: String) {
        val id = teamId(t) ?: return
        val existing = map[id]
        if (existing == null) {
            map[id] = Team(
                id = id,
                name = t.name ?: t.shortName.orEmpty(),
                code = t.tla ?: TeamMeta.code(t.name),
                flag = TeamMeta.flag(t.name),
                group = group,
                badgeUrl = t.crest.orEmpty()
            )
        } else {
            // Backfill a group label / crest if a later source has one.
            val merged = existing.copy(
                group = existing.group.ifBlank { group },
                badgeUrl = existing.badgeUrl.ifBlank { t.crest.orEmpty() }
            )
            if (merged != existing) map[id] = merged
        }
    }

    private fun FdMatch.toMatch(): Match? {
        val home = teamId(homeTeam) ?: return null
        val away = teamId(awayTeam) ?: return null
        return Match(
            id = "fd_$id",
            homeId = home,
            awayId = away,
            homeScore = score.fullTime.home ?: 0,
            awayScore = score.fullTime.away ?: 0,
            statusRaw = mapStatus(status),
            minute = 0, // v4 free tier does not expose a live minute
            kickoffEpoch = parseEpoch(utcDate),
            stage = stageLabel(stage),
            group = TeamMeta.normalizeGroup(group),
            venue = "",
            city = ""
        )
    }

    private fun com.salem.worldcup2026.data.remote.FdTableRow.toStanding(): GroupStanding? {
        val id = teamId(team) ?: return null
        return GroupStanding(
            teamId = id,
            played = playedGames,
            won = won,
            drawn = draw,
            lost = lost,
            goalsFor = goalsFor,
            goalsAgainst = goalsAgainst
        )
    }

    private fun FdScorer.toStat(): ScorerStat? {
        val id = teamId(team) ?: return null
        val name = player.name ?: return null
        return ScorerStat(
            player = name,
            teamId = id,
            goals = goals ?: 0,
            assists = assists ?: 0
        )
    }
}

private fun mapStatus(status: String?): String = when (status?.uppercase()) {
    "IN_PLAY" -> "LIVE"
    "PAUSED" -> "HALFTIME"
    "FINISHED", "AWARDED" -> "FINISHED"
    else -> "SCHEDULED" // SCHEDULED, TIMED, SUSPENDED, POSTPONED, CANCELLED
}

private fun stageLabel(stage: String?): String = when (stage?.uppercase()) {
    "GROUP_STAGE", null -> "Group Stage"
    "LAST_16", "ROUND_OF_16" -> "Round of 16"
    "QUARTER_FINALS" -> "Quarter-final"
    "SEMI_FINALS" -> "Semi-final"
    "THIRD_PLACE" -> "Third-place"
    "FINAL" -> "Final"
    else -> stage.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() }
}

private fun parseEpoch(utcDate: String?): Long {
    if (utcDate.isNullOrBlank()) return 0L
    val patterns = listOf("yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'T'HH:mm:ssXXX", "yyyy-MM-dd'T'HH:mm:ss")
    for (p in patterns) {
        runCatching {
            val fmt = SimpleDateFormat(p, Locale.US)
            fmt.timeZone = TimeZone.getTimeZone("UTC")
            return fmt.parse(utcDate)?.time ?: 0L
        }
    }
    return 0L
}
