package com.fichestu.frontend.navigation

import android.app.Activity
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.remember
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.fichestu.frontend.data.viewmodels.AuthViewModel
import com.fichestu.frontend.views.AuthScreen
import com.fichestu.frontend.views.ForgotPasswordScreen
import com.fichestu.frontend.ui.game.FichestuGameScreen
import com.fichestu.frontend.util.GoogleAuthHelper


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
    val context = LocalContext.current as ComponentActivity
    val googleAuthHelper = remember { GoogleAuthHelper(context) }

    NavHost(
        navController = navController,
        startDestination = AppRoute.AUTH
    ) {
        composable(AppRoute.AUTH) {
            val authViewModel: AuthViewModel = viewModel()

            AuthScreen(
                viewModel = authViewModel,
                navController = navController,
                onGoogleClick = {
                    googleAuthHelper.launchGoogleLogin { token ->
                        authViewModel.onGoogleLoginSuccess(token)
                    }
                }
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
