package com.salem.worldcup2026.data.remote

import kotlinx.serialization.Serializable

/**
 * Wire models for the football-data.org v4 API. v4 uses flat structures and
 * proper JSON types (ints are ints, nullable where a value is unknown).
 */

@Serializable
data class FdMatchesResponse(val matches: List<FdMatch> = emptyList())

@Serializable
data class FdStandingsResponse(val standings: List<FdStandingGroup> = emptyList())

@Serializable
data class FdScorersResponse(val scorers: List<FdScorer> = emptyList())

@Serializable
data class FdMatch(
    val id: Long = 0,
    val utcDate: String? = null,
    val status: String? = null,     // SCHEDULED, TIMED, IN_PLAY, PAUSED, FINISHED, ...
    val stage: String? = null,      // GROUP_STAGE, LAST_16, ...
    val group: String? = null,      // GROUP_A ...
    val matchday: Int? = null,
    val homeTeam: FdTeam = FdTeam(),
    val awayTeam: FdTeam = FdTeam(),
    val score: FdScore = FdScore()
)

@Serializable
data class FdTeam(
    val id: Long? = null,
    val name: String? = null,
    val shortName: String? = null,
    val tla: String? = null,        // three-letter abbreviation
    val crest: String? = null
)

@Serializable
data class FdScore(
    val winner: String? = null,
    val duration: String? = null,
    val fullTime: FdScoreLine = FdScoreLine(),
    val halfTime: FdScoreLine = FdScoreLine()
)

@Serializable
data class FdScoreLine(val home: Int? = null, val away: Int? = null)

@Serializable
data class FdStandingGroup(
    val stage: String? = null,
    val type: String? = null,       // TOTAL, HOME, AWAY
    val group: String? = null,      // GROUP_A ...
    val table: List<FdTableRow> = emptyList()
)

@Serializable
data class FdTableRow(
    val position: Int = 0,
    val team: FdTeam = FdTeam(),
    val playedGames: Int = 0,
    val won: Int = 0,
    val draw: Int = 0,
    val lost: Int = 0,
    val points: Int = 0,
    val goalsFor: Int = 0,
    val goalsAgainst: Int = 0,
    val goalDifference: Int = 0
)

@Serializable
data class FdScorer(
    val player: FdPlayer = FdPlayer(),
    val team: FdTeam = FdTeam(),
    val goals: Int? = null,
    val assists: Int? = null
)

@Serializable
data class FdPlayer(val id: Long? = null, val name: String? = null)
