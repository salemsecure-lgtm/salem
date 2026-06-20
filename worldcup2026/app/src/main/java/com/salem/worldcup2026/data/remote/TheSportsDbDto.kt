package com.salem.worldcup2026.data.remote

import kotlinx.serialization.Serializable

/**
 * Wire models for TheSportsDB v1 JSON. Every numeric field arrives as a
 * (sometimes null) String, so they are all decoded as String? and converted in
 * the mapping layer. Unknown keys are ignored by the Json configuration.
 */

@Serializable
data class EventsResponse(val events: List<EventDto>? = null)

@Serializable
data class TableResponse(val table: List<TableRowDto>? = null)

@Serializable
data class EventDto(
    val idEvent: String? = null,
    val strEvent: String? = null,
    val strHomeTeam: String? = null,
    val strAwayTeam: String? = null,
    val idHomeTeam: String? = null,
    val idAwayTeam: String? = null,
    val intHomeScore: String? = null,
    val intAwayScore: String? = null,
    val strHomeTeamBadge: String? = null,
    val strAwayTeamBadge: String? = null,
    val strStatus: String? = null,
    val strProgress: String? = null,
    val strTimestamp: String? = null,
    val dateEvent: String? = null,
    val strTime: String? = null,
    val strVenue: String? = null,
    val strCity: String? = null,
    val strGroup: String? = null,
    val intRound: String? = null,
    val strSeason: String? = null
)

@Serializable
data class TableRowDto(
    val idTeam: String? = null,
    val strTeam: String? = null,
    val strBadge: String? = null,
    val strGroup: String? = null,
    val intRank: String? = null,
    val intPlayed: String? = null,
    val intWin: String? = null,
    val intDraw: String? = null,
    val intLoss: String? = null,
    val intGoalsFor: String? = null,
    val intGoalsAgainst: String? = null,
    val intGoalDifference: String? = null,
    val intPoints: String? = null,
    val strForm: String? = null
)
