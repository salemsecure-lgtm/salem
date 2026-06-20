package com.salem.worldcup2026.data.remote

import kotlinx.serialization.Serializable

/**
 * Wire models for the worldcup26.ir REST API (rezarahiminia/worldcup2026).
 * All values arrive as strings. Endpoints return a single wrapper object.
 */

@Serializable
data class WcTeamsResponse(val teams: List<WcTeam> = emptyList())

@Serializable
data class WcGamesResponse(val games: List<WcGame> = emptyList())

@Serializable
data class WcGroupsResponse(val groups: List<WcGroup> = emptyList())

@Serializable
data class WcStadiumsResponse(val stadiums: List<WcStadium> = emptyList())

@Serializable
data class WcTeam(
    val id: String? = null,
    val name_en: String? = null,
    val flag: String? = null,        // real flag image URL (flagcdn)
    val fifa_code: String? = null,
    val iso2: String? = null,
    val groups: String? = null       // "A".."L"
)

@Serializable
data class WcGame(
    val id: String? = null,
    val home_team_id: String? = null,
    val away_team_id: String? = null,
    val home_score: String? = null,
    val away_score: String? = null,
    val home_scorers: String? = null,
    val away_scorers: String? = null,
    val group: String? = null,
    val matchday: String? = null,
    val local_date: String? = null,  // "MM/dd/yyyy HH:mm"
    val stadium_id: String? = null,
    val finished: String? = null,    // "TRUE" / "FALSE"
    val time_elapsed: String? = null,// "notstarted" / "finished" / live minute
    val type: String? = null,        // group, r32, r16, qf, sf, third, final
    val home_team_name_en: String? = null,
    val away_team_name_en: String? = null
)

@Serializable
data class WcGroup(
    val name: String? = null,        // "A".."L"
    val teams: List<WcGroupTeam> = emptyList()
)

@Serializable
data class WcGroupTeam(
    val team_id: String? = null,
    val mp: String? = null,
    val w: String? = null,
    val l: String? = null,
    val d: String? = null,
    val pts: String? = null,
    val gf: String? = null,
    val ga: String? = null,
    val gd: String? = null
)

@Serializable
data class WcStadium(
    val id: String? = null,
    val name_en: String? = null,
    val city_en: String? = null
)
