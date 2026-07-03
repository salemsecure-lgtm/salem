package dev.salemlift.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.salemlift.app.di.AppContainer
import dev.salemlift.app.feedback.FeedbackRoute
import dev.salemlift.app.home.HomeRoute
import dev.salemlift.app.runner.SessionRunnerRoute
import dev.salemlift.app.summary.SummaryRoute

private fun NavBackStackEntry.sessionIdArg(): Long = arguments?.getLong(Routes.SESSION_ID_ARG) ?: 0L

private val sessionIdArgs = listOf(navArgument(Routes.SESSION_ID_ARG) { type = NavType.LongType })

@Composable
fun SalemNavHost(container: AppContainer) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeRoute(
                container = container,
                onOpenRunner = { navController.navigate(Routes.runner(it)) },
            )
        }
        composable(Routes.RUNNER, arguments = sessionIdArgs) { entry ->
            val sessionId = entry.sessionIdArg()
            SessionRunnerRoute(
                container = container,
                sessionId = sessionId,
                onFinish = { navController.navigate(Routes.feedback(sessionId)) },
            )
        }
        composable(Routes.FEEDBACK, arguments = sessionIdArgs) { entry ->
            FeedbackRoute(
                container = container,
                sessionId = entry.sessionIdArg(),
                onCommitted = { committedId ->
                    navController.navigate(Routes.summary(committedId)) {
                        popUpTo(Routes.HOME) { inclusive = false }
                    }
                },
            )
        }
        composable(Routes.SUMMARY, arguments = sessionIdArgs) { entry ->
            SummaryRoute(
                container = container,
                sessionId = entry.sessionIdArg(),
                onDone = { navController.popBackStack(Routes.HOME, inclusive = false) },
            )
        }
    }
}
