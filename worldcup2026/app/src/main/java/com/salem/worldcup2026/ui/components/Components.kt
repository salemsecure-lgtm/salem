package com.salem.worldcup2026.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salem.worldcup2026.data.model.Match
import com.salem.worldcup2026.data.model.MatchStatus
import com.salem.worldcup2026.data.model.Team
import com.salem.worldcup2026.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LivePulse(modifier: Modifier = Modifier, color: Color = WCRed) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 1f, targetValue = 0.25f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "alpha"
    )
    Box(
        modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
    )
}

@Composable
fun LiveChip() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(LiveGradient)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        LivePulse(color = Color.White)
        Spacer(Modifier.width(5.dp))
        Text("LIVE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun FlagBubble(flag: String, size: Int = 34) {
    Box(
        Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(WCNavyAlt),
        contentAlignment = Alignment.Center
    ) {
        Text(flag, fontSize = (size * 0.55).sp)
    }
}

private fun fmtKickoff(epoch: Long): String {
    if (epoch <= 0) return "TBD"
    val sdf = SimpleDateFormat("EEE d MMM • HH:mm", Locale.ENGLISH)
    return sdf.format(Date(epoch))
}

@Composable
fun MatchCard(
    match: Match,
    home: Team?,
    away: Team?,
    onClick: () -> Unit
) {
    val live = match.status == MatchStatus.LIVE || match.status == MatchStatus.HALFTIME
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = WCSurface,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (live) Modifier.border(
                    1.dp, Brush.horizontalGradient(listOf(WCMagenta, WCRed)),
                    RoundedCornerShape(20.dp)
                ) else Modifier
            )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (match.group.isNotBlank()) "Group ${match.group}" else match.stage,
                    color = WCTextDim, fontSize = 11.sp, fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                when (match.status) {
                    MatchStatus.LIVE -> LiveChip()
                    MatchStatus.HALFTIME -> StatusPill("HT", WCGold, WCNavy)
                    MatchStatus.FINISHED -> StatusPill("FT", WCNavyAlt, WCTextDim)
                    MatchStatus.SCHEDULED -> Text(
                        fmtKickoff(match.kickoffEpoch),
                        color = WCTextDim, fontSize = 11.sp, fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            TeamScoreRow(home, match.homeScore, match.status)
            Spacer(Modifier.height(10.dp))
            TeamScoreRow(away, match.awayScore, match.status)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (live) {
                    LivePulse()
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "${match.minute}'", color = WCRed,
                        fontSize = 12.sp, fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(10.dp))
                }
                Text(
                    match.venue.ifBlank { match.city },
                    color = WCTextDim, fontSize = 11.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun TeamScoreRow(team: Team?, score: Int, status: MatchStatus) {
    val show = status != MatchStatus.SCHEDULED
    Row(verticalAlignment = Alignment.CenterVertically) {
        FlagBubble(team?.flag ?: "🏳️", 30)
        Spacer(Modifier.width(12.dp))
        Text(
            team?.name ?: "TBD",
            color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (show) {
            Text(
                "$score", color = WCGold, fontSize = 22.sp, fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun StatusPill(text: String, bg: Color, fg: Color) {
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, color = fg, fontSize = 11.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun SectionHeader(title: String, accent: Color = WCGold) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(width = 4.dp, height = 18.dp)
                .clip(RoundedCornerShape(50))
                .background(accent)
        )
        Spacer(Modifier.width(10.dp))
        Text(title, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
    }
}

/** Horizontal comparison bar used on the match-detail stats panel. */
@Composable
fun ComparisonBar(label: String, home: Int, away: Int) {
    val total = (home + away).coerceAtLeast(1)
    val homeFrac = home.toFloat() / total
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Text("$home", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text(label, color = WCTextDim, fontSize = 12.sp)
            Spacer(Modifier.weight(1f))
            Text("$away", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(5.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(WCNavyAlt)
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .weight(homeFrac.coerceIn(0.02f, 0.98f))
                    .background(WCGreen)
            )
            Box(
                Modifier
                    .fillMaxHeight()
                    .weight((1f - homeFrac).coerceIn(0.02f, 0.98f))
                    .background(WCMagenta)
            )
        }
    }
}
