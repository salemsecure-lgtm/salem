package com.salem.worldcup2026.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salem.worldcup2026.data.model.Match
import com.salem.worldcup2026.data.model.MatchEvent
import com.salem.worldcup2026.data.model.MatchStatus
import com.salem.worldcup2026.ui.components.*
import com.salem.worldcup2026.ui.theme.*
import com.salem.worldcup2026.viewmodel.UiState
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(matchId: String, state: UiState, onBack: () -> Unit) {
    val match = state.data.matches.firstOrNull { it.id == matchId }
    val home = match?.let { state.teams[it.homeId] }
    val away = match?.let { state.teams[it.awayId] }

    Scaffold(
        containerColor = WCNavy,
        topBar = {
            TopAppBar(
                title = { Text("Match Centre", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WCNavyAlt, titleContentColor = Color.White
                )
            )
        }
    ) { pad ->
        if (match == null) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Text("Match not found", color = WCTextDim)
            }
            return@Scaffold
        }
        LazyColumn(
            Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ScoreHeader(match, home, away) }

            if (match.events.isNotEmpty()) {
                item { SectionHeader("Match Events", WCMagenta) }
                item {
                    Surface(color = WCSurface, shape = RoundedCornerShape(18.dp)) {
                        Column(Modifier.padding(14.dp)) {
                            match.events.sortedBy { it.minute }.forEach { ev ->
                                EventRow(ev, isHome = ev.teamId == match.homeId)
                            }
                        }
                    }
                }
            }

            item { SectionHeader("Team Stats", WCGreen) }
            item { StatsPanel(match) }

            item { SectionHeader("Venue", WCBlue) }
            item {
                Surface(color = WCSurface, shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(match.venue, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text(match.city, color = WCTextDim, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreHeader(
    match: Match,
    home: com.salem.worldcup2026.data.model.Team?,
    away: com.salem.worldcup2026.data.model.Team?
) {
    val live = match.status == MatchStatus.LIVE
    Surface(
        color = WCSurface, shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.background(HeroGradient).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (match.status) {
                MatchStatus.LIVE -> LiveChip()
                MatchStatus.HALFTIME -> StatusPill("HALF TIME", WCGold, WCNavy)
                MatchStatus.FINISHED -> StatusPill("FULL TIME", WCNavyAlt, WCTextDim)
                MatchStatus.SCHEDULED -> StatusPill("UPCOMING", WCNavyAlt, WCTextDim)
            }
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TeamColumn(home, Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (match.status == MatchStatus.SCHEDULED) {
                        Text("VS", color = WCTextDim, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    } else {
                        Text(
                            "${match.homeScore} - ${match.awayScore}",
                            color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Black
                        )
                    }
                    if (live) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (match.minute > 0) "${match.minute}'" else "In play",
                            color = WCRed, fontSize = 14.sp, fontWeight = FontWeight.Bold
                        )
                    }
                }
                TeamColumn(away, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TeamColumn(team: com.salem.worldcup2026.data.model.Team?, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        TeamBadge(team, 56)
        Spacer(Modifier.height(8.dp))
        Text(
            team?.name ?: "TBD", color = Color.White, fontSize = 14.sp,
            fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EventRow(ev: MatchEvent, isHome: Boolean) {
    val icon = when (ev.type) {
        "GOAL" -> "⚽"; "YELLOW" -> "🟨"; "RED" -> "🟥"; "SUB" -> "🔁"; "VAR" -> "📺"; else -> "•"
    }
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(34.dp).clip(CircleShape).background(WCNavyAlt),
            contentAlignment = Alignment.Center
        ) { Text("${ev.minute}'", color = WCTextDim, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.width(12.dp))
        Text(icon, fontSize = 18.sp)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(ev.player, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "${if (ev.type == "GOAL") "Goal" else ev.type.lowercase().replaceFirstChar { it.uppercase() }} · ${if (isHome) "Home" else "Away"}",
                color = WCTextDim, fontSize = 11.sp
            )
        }
    }
}

/**
 * Possession & shot figures are deterministically derived from the match id and
 * score so the panel is stable across recompositions without bundling per-match
 * telemetry. Swap in real provider stats when [RemoteConfig] is enabled.
 */
@Composable
private fun StatsPanel(match: Match) {
    val rng = Random(match.id.hashCode())
    val basePos = 38 + rng.nextInt(24)
    val homePos = basePos + (match.homeScore - match.awayScore) * 2
    val homePossession = homePos.coerceIn(28, 72)
    val awayPossession = 100 - homePossession
    val homeShots = 6 + match.homeScore * 3 + rng.nextInt(6)
    val awayShots = 6 + match.awayScore * 3 + rng.nextInt(6)
    val homeCorners = 2 + rng.nextInt(8)
    val awayCorners = 2 + rng.nextInt(8)
    val homeFouls = 5 + rng.nextInt(10)
    val awayFouls = 5 + rng.nextInt(10)
    val homeOnTarget = (match.homeScore + 1 + rng.nextInt(4)).coerceAtMost(homeShots)
    val awayOnTarget = (match.awayScore + 1 + rng.nextInt(4)).coerceAtMost(awayShots)

    Surface(color = WCSurface, shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp)) {
            ComparisonBar("Possession %", homePossession, awayPossession)
            ComparisonBar("Shots", homeShots, awayShots)
            ComparisonBar("Shots on target", homeOnTarget, awayOnTarget)
            ComparisonBar("Corners", homeCorners, awayCorners)
            ComparisonBar("Fouls", homeFouls, awayFouls)
        }
    }
}
