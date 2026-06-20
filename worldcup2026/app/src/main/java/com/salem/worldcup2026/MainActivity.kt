package com.salem.worldcup2026

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Public
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.salem.worldcup2026.notifications.MatchAlertWorker
import com.salem.worldcup2026.notifications.MatchNotifications
import com.salem.worldcup2026.ui.screens.*
import com.salem.worldcup2026.ui.theme.*
import com.salem.worldcup2026.viewmodel.TournamentViewModel
import java.util.concurrent.TimeUnit

private sealed class Dest(val route: String, val label: String, val icon: ImageVector) {
    data object Matches : Dest("matches", "Matches", Icons.Filled.PlayCircle)
    data object Groups : Dest("groups", "Groups", Icons.Filled.Groups)
    data object Stats : Dest("stats", "Stats", Icons.Filled.BarChart)
    data object Teams : Dest("teams", "Teams", Icons.Filled.Public)
}

class MainActivity : ComponentActivity() {

    private val requestNotifications =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MatchNotifications.ensureChannel(this)
        maybeRequestNotificationPermission()
        scheduleMatchAlerts()
        setContent { WorldCupTheme { AppRoot() } }
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun scheduleMatchAlerts() {
        val work = PeriodicWorkRequestBuilder<MatchAlertWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "wc_match_alerts", ExistingPeriodicWorkPolicy.KEEP, work
        )
    }
}

@Composable
private fun AppRoot() {
    val nav = rememberNavController()
    val vm: TournamentViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    val tabs = listOf(Dest.Matches, Dest.Groups, Dest.Stats, Dest.Teams)
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        containerColor = WCNavy,
        bottomBar = {
            // Hide bottom bar on the match-detail route.
            if (currentRoute == null || currentRoute in tabs.map { it.route }) {
                NavigationBar(containerColor = WCNavyAlt) {
                    tabs.forEach { dest ->
                        NavigationBarItem(
                            selected = currentRoute == dest.route,
                            onClick = {
                                nav.navigate(dest.route) {
                                    popUpTo(Dest.Matches.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(dest.icon, dest.label) },
                            label = { Text(dest.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = WCNavy,
                                selectedTextColor = WCGold,
                                indicatorColor = WCGold,
                                unselectedIconColor = WCTextDim,
                                unselectedTextColor = WCTextDim
                            )
                        )
                    }
                }
            }
        }
    ) { pad ->
        when {
            state.loading && !state.hasData ->
                LoadingScreen(Modifier.padding(pad))
            state.error && !state.hasData ->
                ConnectionErrorScreen(Modifier.padding(pad), onRetry = vm::refresh)
            else -> AppNavHost(nav, state, vm, Modifier.padding(pad))
        }
    }
}

@Composable
private fun AppNavHost(
    nav: androidx.navigation.NavHostController,
    state: com.salem.worldcup2026.viewmodel.UiState,
    vm: TournamentViewModel,
    modifier: Modifier
) {
    NavHost(
        navController = nav,
        startDestination = Dest.Matches.route,
        modifier = modifier
    ) {
        composable(Dest.Matches.route) {
            MatchesScreen(state) { id -> nav.navigate("match/$id") }
        }
        composable(Dest.Groups.route) { GroupsScreen(state) }
        composable(Dest.Stats.route) { StatsScreen(state) }
        composable(Dest.Teams.route) {
            TeamsScreen(state, onToggleFavorite = vm::toggleFavorite)
        }
        composable(
            "match/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { entry ->
            MatchDetailScreen(
                matchId = entry.arguments?.getString("id").orEmpty(),
                state = state,
                onBack = { nav.popBackStack() }
            )
        }
    }
}

@Composable
private fun LoadingScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = WCGold, trackColor = WCNavyAlt)
        Spacer(Modifier.height(18.dp))
        Text("Loading live data…", color = WCTextDim, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ConnectionErrorScreen(modifier: Modifier = Modifier, onRetry: () -> Unit) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.CloudOff, contentDescription = null, tint = WCTextDim, modifier = Modifier.padding(8.dp))
        Spacer(Modifier.height(12.dp))
        Text("Can't reach live scores", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            "Check your connection — the app shows live World Cup data only.",
            color = WCTextDim, fontSize = 13.sp, fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = WCGold, contentColor = WCNavy)
        ) {
            Text("Retry", fontWeight = FontWeight.Bold)
        }
    }
}
