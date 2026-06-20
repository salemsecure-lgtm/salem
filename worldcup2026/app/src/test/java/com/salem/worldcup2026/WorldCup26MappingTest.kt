package com.salem.worldcup2026

import com.salem.worldcup2026.data.model.MatchStatus
import com.salem.worldcup2026.data.remote.WcGamesResponse
import com.salem.worldcup2026.data.remote.WcGroupsResponse
import com.salem.worldcup2026.data.remote.WcStadiumsResponse
import com.salem.worldcup2026.data.remote.WcTeamsResponse
import com.salem.worldcup2026.data.repo.WorldCup26Source
import com.salem.worldcup2026.data.repo.parseScorers
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Validates the worldcup26.ir mapping and the goal-scorer parser against the
 * real (quirky) response shapes, offline.
 */
class WorldCup26MappingTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Test
    fun parsesMixedQuoteScorerStrings() {
        assertEquals(emptyList<Any>(), parseScorers("null"))
        assertEquals(emptyList<Any>(), parseScorers("\"null\""))
        assertEquals(emptyList<Any>(), parseScorers("{}"))

        val curly = parseScorers("{“J. Quiñones 9'”,”R. Jiménez 67'”}")
        assertEquals(2, curly.size)
        assertEquals(9, curly[0].minute)
        assertEquals("J. Quiñones", curly[0].player)
        assertFalse(curly[0].ownGoal)

        val withOgAndStoppage =
            parseScorers("{\"D. Bobadilla 7'(OG)\",\"F. Balogun 31'\",\"F. Balogun 45'+5'\"}")
        assertEquals(3, withOgAndStoppage.size)
        assertTrue(withOgAndStoppage[0].ownGoal)
        assertEquals(7, withOgAndStoppage[0].minute)
        assertEquals("F. Balogun", withOgAndStoppage[1].player)
        assertEquals(45, withOgAndStoppage[2].minute) // base minute of 45'+5'
    }

    @Test
    fun mapsCompleteTournament() {
        val teams = json.decodeFromString(
            WcTeamsResponse.serializer(),
            """{"teams":[
              {"id":"1","name_en":"Mexico","flag":"https://flagcdn.com/w80/mx.png","fifa_code":"MEX","iso2":"MX","groups":"A"},
              {"id":"2","name_en":"South Africa","flag":"https://flagcdn.com/w80/za.png","fifa_code":"RSA","iso2":"ZA","groups":"A"}
            ]}"""
        ).teams
        val games = json.decodeFromString(
            WcGamesResponse.serializer(),
            """{"games":[
              {"id":"1","home_team_id":"1","away_team_id":"2","home_score":"2","away_score":"0",
               "home_scorers":"{\"J. Quinones 9'\",\"R. Jimenez 67'\"}","away_scorers":"null",
               "group":"A","matchday":"1","local_date":"06/11/2026 13:00","stadium_id":"1",
               "finished":"TRUE","time_elapsed":"finished","type":"group"},
              {"id":"50","home_team_id":"1","away_team_id":"2","home_score":"1","away_score":"1",
               "home_scorers":"{\"R. Jimenez 22'\"}","away_scorers":"{\"T. Mokoena 70'\"}",
               "group":"A","matchday":"2","local_date":"06/15/2026 18:00","stadium_id":"1",
               "finished":"FALSE","time_elapsed":"75","type":"group"}
            ]}"""
        ).games
        val groups = json.decodeFromString(
            WcGroupsResponse.serializer(),
            """{"groups":[
              {"name":"A","teams":[
                {"team_id":"1","mp":"1","w":"1","l":"0","d":"0","pts":"3","gf":"2","ga":"0","gd":"2"},
                {"team_id":"2","mp":"1","w":"0","l":"1","d":"0","pts":"0","gf":"0","ga":"2","gd":"-2"}
              ]}
            ]}"""
        ).groups
        val stadiums = json.decodeFromString(
            WcStadiumsResponse.serializer(),
            """{"stadiums":[{"id":"1","name_en":"Estadio Azteca","city_en":"Mexico City"}]}"""
        ).stadiums

        val data = WorldCup26Source().map(teams, games, groups, stadiums)
        assertNotNull(data); data!!

        assertEquals(2, data.teams.size)
        val mex = data.teams.first { it.id == "1" }
        assertEquals("Mexico", mex.name)
        assertEquals("MEX", mex.code)
        assertEquals("A", mex.group)
        assertTrue(mex.badgeUrl.startsWith("https://flagcdn.com"))

        // Match 1: finished, venue resolved, two goal events on the home side.
        val m1 = data.matches.first { it.id == "1" }
        assertEquals(MatchStatus.FINISHED, m1.status)
        assertEquals("Estadio Azteca", m1.venue)
        assertEquals("Mexico City", m1.city)
        assertEquals(2, m1.events.size)
        assertTrue(m1.events.all { it.teamId == "1" })

        // Match 50: live with a parsed minute and one event per side.
        val m50 = data.matches.first { it.id == "50" }
        assertEquals(MatchStatus.LIVE, m50.status)
        assertEquals(75, m50.minute)
        assertEquals(2, m50.events.size)

        // Standings mapped from the group table.
        assertEquals(2, data.standings.size)
        assertEquals(3, data.standings.first { it.teamId == "1" }.points)

        // Top scorers aggregated from goal events (R. Jimenez scored in both).
        val jimenez = data.topScorers.firstOrNull { it.player == "R. Jimenez" }
        assertNotNull(jimenez)
        assertEquals(2, jimenez!!.goals)
    }
}
