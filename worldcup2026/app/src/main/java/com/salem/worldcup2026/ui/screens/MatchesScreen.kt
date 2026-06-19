package com.salem.worldcup2026.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salem.worldcup2026.data.model.Match
import com.salem.worldcup2026.data.model.MatchStatus
import com.salem.worldcup2026.ui.components.LivePulse
import com.salem.worldcup2026.ui.components.MatchCard
import com.salem.worldcup2026.ui.theme.*
import com.salem.worldcup2026.viewmodel.UiState

private enum class MatchTab(val label: String) { LIVE("Live"), UPCOMING("Upcoming"), RESULTS("Results") }

@Composable
fun MatchesScreen(state: UiState, onMatch: (String) -> Unit) {
    var tab by remember { mutableStateOf(MatchTab.LIVE) }
    val live = state.data.matches.filter { it.status == MatchStatus.LIVE || it.status == MatchStatus.HALFTIME }
        .sortedByDescending { it.minute }
    val upcoming = state.data.matches.filter { it.status == MatchStatus.SCHEDULED }
        .sortedBy { it.kickoffEpoch }
    val results = state.data.matches.filter { it.status == MatchStatus.FINISHED }
        .sortedByDescending { it.kickoffEpoch }

    val shown = when (tab) {
        MatchTab.LIVE -> live
        MatchTab.UPCOMING -> upcoming
        MatchTab.RESULTS -> results
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item { HeroHeader(liveCount = live.size) }
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MatchTab.values().forEach { t ->
                    val selected = t == tab
                    val count = when (t) {
                        MatchTab.LIVE -> live.size
                        MatchTab.UPCOMING -> upcoming.size
                        MatchTab.RESULTS -> results.size
                    }
                    FilterChip(
                        selected = selected,
                        onClick = { tab = t },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (t == MatchTab.LIVE && live.isNotEmpty()) {
                                    LivePulse(); Spacer(Modifier.width(6.dp))
                                }
                                Text("${t.label} ($count)", fontWeight = FontWeight.SemiBold)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WCGold,
                            selectedLabelColor = WCNavy,
                            containerColor = WCSurface,
                            labelColor = WCTextDim
                        ),
                        border = null
                    )
                }
            }
        }
        if (shown.isEmpty()) {
            item { EmptyState(tab.label) }
        }
        items(shown, key = { it.id }) { m ->
            Box(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                MatchCard(
                    match = m,
                    home = state.teams[m.homeId],
                    away = state.teams[m.awayId],
                    onClick = { onMatch(m.id) }
                )
            }
        }
    }
}

@Composable
private fun HeroHeader(liveCount: Int) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(HeroGradient)
            .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 22.dp)
    ) {
        Column {
            Text(
                "FIFA WORLD CUP",
                color = WCGold, fontSize = 13.sp, fontWeight = FontWeight.Black,
                letterSpacing = 3.sp
            )
            Text(
                "2026",
                color = Color.White, fontSize = 46.sp, fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "United States · Canada · Mexico",
                color = WCTextDim, fontSize = 13.sp, fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatBadge("48", "Teams", WCGreen)
                StatBadge("104", "Matches", WCBlue)
                StatBadge(
                    if (liveCount > 0) "$liveCount" else "—",
                    "Live now",
                    if (liveCount > 0) WCRed else WCTextDim
                )
            }
        }
    }
}

@Composable
private fun StatBadge(value: String, label: String, accent: Color) {
    Column(
        Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(value, color = accent, fontSize = 20.sp, fontWeight = FontWeight.Black)
        Text(label, color = WCTextDim, fontSize = 11.sp)
    }
}

@Composable
private fun EmptyState(label: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("⚽", fontSize = 40.sp)
        Spacer(Modifier.height(8.dp))
        Text("No $label matches right now", color = WCTextDim, fontSize = 14.sp)
    }
}
