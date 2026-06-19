package com.salem.worldcup2026.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salem.worldcup2026.ui.components.FlagBubble
import com.salem.worldcup2026.ui.theme.*
import com.salem.worldcup2026.viewmodel.UiState

@Composable
fun TeamsScreen(state: UiState, onToggleFavorite: (String) -> Unit) {
    val teams = state.teams.values.sortedWith(compareBy({ it.group }, { it.name }))
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Teams", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
            Text(
                "Tap the bell to get goal & kickoff alerts",
                color = WCTextDim, fontSize = 12.sp
            )
        }
        items(teams, key = { it.id }) { t ->
            val fav = t.id in state.favorites
            Surface(color = WCSurface, shape = RoundedCornerShape(16.dp)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FlagBubble(t.flag, 38)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(t.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Group ${t.group} · FIFA #${t.fifaRank}",
                            color = WCTextDim, fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = { onToggleFavorite(t.id) }) {
                        Icon(
                            imageVector = if (fav) Icons.Filled.Notifications else Icons.Outlined.NotificationsNone,
                            contentDescription = "Toggle alerts",
                            tint = if (fav) WCGold else WCTextDim
                        )
                    }
                }
            }
        }
    }
}
