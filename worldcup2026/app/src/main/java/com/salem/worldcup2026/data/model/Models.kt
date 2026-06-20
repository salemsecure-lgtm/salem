package com.salem.worldcup2026.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Team(
    val id: String,
    val name: String,
    val code: String,        // 3-letter FIFA code, e.g. "ARG"
    val flag: String,        // emoji flag (fallback when no crest)
    val group: String,       // "A".."L"
    val fifaRank: Int = 0,
    val badgeUrl: String = ""  // real crest image from the live provider
)

enum class MatchStatus { SCHEDULED, LIVE, HALFTIME, FINISHED }

@Serializable
data class MatchEvent(
    val minute: Int,
    val type: String,        // GOAL, YELLOW, RED, SUB, VAR
    val teamId: String,
    val player: String,
    val detail: String = ""
)

@Serializable
data class Match(
    val id: String,
    val homeId: String,
    val awayId: String,
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val statusRaw: String = "SCHEDULED",
    val minute: Int = 0,             // live clock
    val kickoffEpoch: Long = 0L,     // millis UTC
    val stage: String = "Group Stage",
    val group: String = "",
    val venue: String = "",
    val city: String = "",
    val events: List<MatchEvent> = emptyList()
) {
    val status: MatchStatus
        get() = when (statusRaw.uppercase()) {
            "LIVE", "IN_PLAY" -> MatchStatus.LIVE
            "HALFTIME", "PAUSED" -> MatchStatus.HALFTIME
            "FINISHED", "FT" -> MatchStatus.FINISHED
            else -> MatchStatus.SCHEDULED
        }
}

@Serializable
data class GroupStanding(
    val teamId: String,
    val played: Int,
    val won: Int,
    val drawn: Int,
    val lost: Int,
    val goalsFor: Int,
    val goalsAgainst: Int
) {
    val points: Int get() = won * 3 + drawn
    val goalDiff: Int get() = goalsFor - goalsAgainst
}

@Serializable
data class ScorerStat(
    val player: String,
    val teamId: String,
    val goals: Int,
    val assists: Int = 0
)

@Serializable
data class TournamentData(
    val teams: List<Team> = emptyList(),
    val matches: List<Match> = emptyList(),
    val standings: List<GroupStanding> = emptyList(),
    val topScorers: List<ScorerStat> = emptyList()
)
