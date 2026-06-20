package com.salem.worldcup2026.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salem.worldcup2026.data.model.MatchStatus
import com.salem.worldcup2026.ui.components.FlagBubble
import com.salem.worldcup2026.ui.components.SectionHeader
import com.salem.worldcup2026.ui.theme.*
import com.salem.worldcup2026.viewmodel.UiState

@Composable
fun StatsScreen(state: UiState) {
    val played = state.data.matches.count { it.status == MatchStatus.FINISHED }
    val goals = state.data.matches
        .filter { it.status != MatchStatus.SCHEDULED }
        .sumOf { it.homeScore + it.awayScore }
    val avg = if (played > 0) "%.2f".format(goals.toDouble() / played) else "0"
    val cards = state.data.matches.sumOf { m -> m.events.count { it.type == "YELLOW" || it.type == "RED" } }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Tournament Stats", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BigStat("$goals", "Goals", WCGold, Modifier.weight(1f))
                BigStat("$avg", "Goals / match", WCGreen, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BigStat("$played", "Matches played", WCBlue, Modifier.weight(1f))
                BigStat("$cards", "Cards shown", WCMagenta, Modifier.weight(1f))
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            SectionHeader("Golden Boot Race")
        }
        itemsIndexed(state.data.topScorers) { i, s ->
            ScorerRow(
                rank = i + 1,
                name = s.player,
                flag = state.teams[s.teamId]?.flag ?: "🏳️",
                team = state.teams[s.teamId]?.code ?: "",
                goals = s.goals,
                assists = s.assists,
                maxGoals = state.data.topScorers.firstOrNull()?.goals ?: 1
            )
        }
    }
}

@Composable
private fun BigStat(value: String, label: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(color = WCSurface, shape = RoundedCornerShape(18.dp), modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(value, color = accent, fontSize = 30.sp, fontWeight = FontWeight.Black)
            Text(label, color = WCTextDim, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ScorerRow(
    rank: Int, name: String, flag: String, team: String,
    goals: Int, assists: Int, maxGoals: Int
) {
    Surface(color = WCSurface, shape = RoundedCornerShape(16.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val medal = when (rank) {
                1 -> WCGold; 2 -> Color(0xFFC0C0C0); 3 -> Color(0xFFCD7F32); else -> WCNavyAlt
            }
            Box(
                Modifier.size(28.dp).clip(CircleShape).background(medal),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$rank",
                    color = if (rank <= 3) WCNavy else WCTextDim,
                    fontSize = 13.sp, fontWeight = FontWeight.Black
                )
            }
            Spacer(Modifier.width(12.dp))
            FlagBubble(flag, 28)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text("$team · $assists assists", color = WCTextDim, fontSize = 11.sp)
                Spacer(Modifier.height(5.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(WCNavyAlt)
                ) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(goals.toFloat() / maxGoals.coerceAtLeast(1))
                            .clip(RoundedCornerShape(50))
                            .background(WCGold)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Text("$goals", color = WCGold, fontSize = 22.sp, fontWeight = FontWeight.Black)
        }
    }
}
