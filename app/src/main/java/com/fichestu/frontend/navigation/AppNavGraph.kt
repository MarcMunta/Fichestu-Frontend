package com.fichestu.frontend.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.fichestu.frontend.ui.AuthScreen
import com.fichestu.frontend.ui.ForgotPasswordScreen
import com.fichestu.frontend.ui.game.FichestuGameScreen

private object AppRoute {
    const val AUTH = "auth"
    const val FORGOT = "forgot"
    const val GAME = "game/{playerName}"

    fun game(playerName: String): String {
        val encoded = Uri.encode(playerName.ifBlank { "Jugador" })
        return "game/$encoded"
    }
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoute.AUTH
    ) {
        composable(AppRoute.AUTH) {
            AuthScreen(
                onForgotPassword = { navController.navigate(AppRoute.FORGOT) },
                onAuthenticated = { playerName ->
                    navController.navigate(AppRoute.game(playerName)) {
                        popUpTo(AppRoute.AUTH) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onShowGameInline = false
            )
        }

        composable(AppRoute.FORGOT) {
            ForgotPasswordScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = AppRoute.GAME,
            arguments = listOf(
                navArgument("playerName") {
                    type = NavType.StringType
                    defaultValue = "Jugador"
                }
            )
        ) { backStackEntry ->
            val playerName = Uri.decode(backStackEntry.arguments?.getString("playerName").orEmpty())
            FichestuGameScreen(
                playerName = playerName,
                onLogout = {
                    navController.navigate(AppRoute.AUTH) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
