package com.salem.worldcup2026

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Public
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
        if (state.loading) {
            Box(Modifier.padding(pad)) {
                LinearProgressIndicator(
                    color = WCGold,
                    trackColor = WCNavyAlt,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        NavHost(
            navController = nav,
            startDestination = Dest.Matches.route,
            modifier = Modifier.padding(pad)
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
}
