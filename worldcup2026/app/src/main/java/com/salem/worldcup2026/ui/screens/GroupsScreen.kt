package com.salem.worldcup2026.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salem.worldcup2026.data.model.GroupStanding
import com.salem.worldcup2026.ui.components.SectionHeader
import com.salem.worldcup2026.ui.components.TeamBadge
import com.salem.worldcup2026.ui.theme.*
import com.salem.worldcup2026.viewmodel.UiState

@Composable
fun GroupsScreen(state: UiState) {
    val byGroup = state.standings()
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Text(
                "Group Standings",
                color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black
            )
            Text(
                "Top 2 of each group plus 8 best third-placed teams advance",
                color = WCTextDim, fontSize = 12.sp
            )
        }
        byGroup.forEach { (group, rows) ->
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader("Group $group")
                    Surface(color = WCSurface, shape = RoundedCornerShape(18.dp)) {
                        Column(Modifier.padding(vertical = 8.dp)) {
                            TableHeader()
                            rows.forEachIndexed { i, r ->
                                StandingRow(
                                    pos = i + 1,
                                    team = state.teams[r.teamId],
                                    r = r
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TableHeader() {
    Row(
        Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("#", color = WCTextDim, fontSize = 11.sp, modifier = Modifier.width(22.dp))
        Text("Team", color = WCTextDim, fontSize = 11.sp, modifier = Modifier.weight(1f))
        Cell("P"); Cell("W"); Cell("D"); Cell("L"); Cell("GD"); Cell("Pts", bold = true)
    }
}

@Composable
private fun StandingRow(pos: Int, team: com.salem.worldcup2026.data.model.Team?, r: GroupStanding) {
    val qualifies = pos <= 2
    Row(
        Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(22.dp), contentAlignment = Alignment.CenterStart) {
            Box(
                Modifier
                    .size(width = 4.dp, height = 18.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (qualifies) WCGreen else Color.Transparent)
            )
        }
        TeamBadge(team, 24)
        Spacer(Modifier.width(8.dp))
        Text(
            team?.name ?: "TBD", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f)
        )
        Cell("${r.played}"); Cell("${r.won}"); Cell("${r.drawn}"); Cell("${r.lost}")
        Cell(if (r.goalDiff >= 0) "+${r.goalDiff}" else "${r.goalDiff}")
        Cell("${r.points}", bold = true, color = WCGold)
    }
}

@Composable
private fun RowScope.Cell(text: String, bold: Boolean = false, color: Color = Color.White) {
    Text(
        text,
        color = color,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        fontWeight = if (bold) FontWeight.Black else FontWeight.Medium,
        modifier = Modifier.width(26.dp)
    )
}

/** Groups standings sorted by FIFA tie-break order (points, GD, goals for). */
fun UiState.standings(): Map<String, List<GroupStanding>> {
    val teamGroup = teams.mapValues { it.value.group }
    return data.standings
        .groupBy { teamGroup[it.teamId] ?: "?" }
        .toSortedMap()
        .mapValues { (_, rows) ->
            rows.sortedWith(
                compareByDescending<GroupStanding> { it.points }
                    .thenByDescending { it.goalDiff }
                    .thenByDescending { it.goalsFor }
            )
        }
}
