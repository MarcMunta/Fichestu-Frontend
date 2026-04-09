package com.fichestu.frontend

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fichestu.frontend.data.viewmodels.AuthViewModel
import com.fichestu.frontend.views.AuthScreen

@Composable
fun AppNavigation(modifier: Modifier, onGoogleLogin: (AuthViewModel) -> Unit){
    val navController: NavHostController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "auth"
    ) {
        composable(route = "auth") {
            val authViewModel: AuthViewModel = viewModel()
            AuthScreen(viewModel = authViewModel, onGoogleClick = { onGoogleLogin(authViewModel) })
        }
    }

}