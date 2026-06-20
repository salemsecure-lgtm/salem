package com.salem.worldcup2026

import com.salem.worldcup2026.data.model.MatchStatus
import com.salem.worldcup2026.data.remote.FdMatchesResponse
import com.salem.worldcup2026.data.remote.FdScorersResponse
import com.salem.worldcup2026.data.remote.FdStandingsResponse
import com.salem.worldcup2026.data.repo.FootballDataSource
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Validates that the football-data.org v4 response shapes deserialize through
 * the DTOs and map onto the domain models correctly — without needing a live
 * API key. The JSON below mirrors the documented v4 structure.
 */
class FootballDataMappingTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val matchesJson = """
    {
      "matches": [
        {
          "id": 537251,
          "utcDate": "2026-06-11T19:00:00Z",
          "status": "FINISHED",
          "stage": "GROUP_STAGE",
          "group": "GROUP_A",
          "matchday": 1,
          "homeTeam": { "id": 805, "name": "Mexico", "shortName": "Mexico", "tla": "MEX", "crest": "https://crests/mex.png" },
          "awayTeam": { "id": 810, "name": "South Africa", "shortName": "South Africa", "tla": "RSA", "crest": "https://crests/rsa.png" },
          "score": { "winner": "HOME_TEAM", "duration": "REGULAR", "fullTime": { "home": 2, "away": 0 }, "halfTime": { "home": 1, "away": 0 } }
        },
        {
          "id": 537260,
          "utcDate": "2026-06-20T18:00:00Z",
          "status": "IN_PLAY",
          "stage": "GROUP_STAGE",
          "group": "GROUP_F",
          "matchday": 2,
          "homeTeam": { "id": 8601, "name": "Netherlands", "shortName": "Netherlands", "tla": "NED", "crest": "https://crests/ned.png" },
          "awayTeam": { "id": 792, "name": "Sweden", "shortName": "Sweden", "tla": "SWE", "crest": "https://crests/swe.png" },
          "score": { "winner": null, "duration": "REGULAR", "fullTime": { "home": 1, "away": 1 }, "halfTime": { "home": 0, "away": 1 } }
        },
        {
          "id": 537299,
          "utcDate": "2026-07-19T19:00:00Z",
          "status": "SCHEDULED",
          "stage": "FINAL",
          "group": null,
          "matchday": null,
          "homeTeam": { "id": null, "name": null, "shortName": null, "tla": null, "crest": null },
          "awayTeam": { "id": null, "name": null, "shortName": null, "tla": null, "crest": null },
          "score": { "winner": null, "duration": "REGULAR", "fullTime": { "home": null, "away": null }, "halfTime": { "home": null, "away": null } }
        }
      ]
    }
    """.trimIndent()

    private val standingsJson = """
    {
      "standings": [
        {
          "stage": "GROUP_STAGE",
          "type": "TOTAL",
          "group": "GROUP_A",
          "table": [
            { "position": 1, "team": { "id": 805, "name": "Mexico", "tla": "MEX", "crest": "https://crests/mex.png" }, "playedGames": 2, "won": 2, "draw": 0, "lost": 0, "points": 6, "goalsFor": 3, "goalsAgainst": 0, "goalDifference": 3 },
            { "position": 2, "team": { "id": 810, "name": "South Africa", "tla": "RSA", "crest": "https://crests/rsa.png" }, "playedGames": 2, "won": 1, "draw": 0, "lost": 1, "points": 3, "goalsFor": 2, "goalsAgainst": 2, "goalDifference": 0 }
          ]
        }
      ]
    }
    """.trimIndent()

    private val scorersJson = """
    {
      "scorers": [
        { "player": { "id": 44, "name": "Santiago Gimenez" }, "team": { "id": 805, "name": "Mexico", "crest": "https://crests/mex.png" }, "goals": 4, "assists": 1 }
      ]
    }
    """.trimIndent()

    @Test
    fun decodesAndMapsRealShapes() {
        val matches = json.decodeFromString(FdMatchesResponse.serializer(), matchesJson).matches
        val standings = json.decodeFromString(FdStandingsResponse.serializer(), standingsJson).standings
        val scorers = json.decodeFromString(FdScorersResponse.serializer(), scorersJson).scorers

        assertEquals(3, matches.size)
        assertEquals(1, standings.size)
        assertEquals(1, scorers.size)

        val data = FootballDataSource().map(matches, standings, scorers)
        assertNotNull(data)
        data!!

        // The null-team FINAL placeholder is dropped; two real matches remain.
        assertEquals(2, data.matches.size)

        val mexMatch = data.matches.first { it.id == "fd_537251" }
        assertEquals(MatchStatus.FINISHED, mexMatch.status)
        assertEquals(2, mexMatch.homeScore)
        assertEquals("A", mexMatch.group)
        assertEquals("fd_805", mexMatch.homeId)

        val liveMatch = data.matches.first { it.id == "fd_537260" }
        assertEquals(MatchStatus.LIVE, liveMatch.status)
        assertEquals("F", liveMatch.group)

        // Team carries real crest, tla code and group from standings.
        val mexico = data.teams.first { it.id == "fd_805" }
        assertEquals("Mexico", mexico.name)
        assertEquals("MEX", mexico.code)
        assertEquals("A", mexico.group)
        assertTrue(mexico.badgeUrl.startsWith("https://"))

        // Standings + scorers mapped.
        assertEquals(2, data.standings.size)
        assertEquals(6, data.standings.first { it.teamId == "fd_805" }.points)
        assertEquals(1, data.topScorers.size)
        assertEquals("Santiago Gimenez", data.topScorers.first().player)
        assertEquals(4, data.topScorers.first().goals)
    }
}
