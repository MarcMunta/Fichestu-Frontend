package com.fichestu.frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.fichestu.frontend.views.AuthScreen
import com.fichestu.frontend.ui.theme.FichestuTheme
import com.fichestu.frontend.ui.theme.NightBlue
import com.fichestu.frontend.util.GoogleAuthHelper

class MainActivity : ComponentActivity() {

    private val googleAuthHelper by lazy { GoogleAuthHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FichestuTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(modifier = Modifier.padding(innerPadding),
                        onGoogleLogin = { viewModel ->
                            googleAuthHelper.launchGoogleLogin { token ->
                                viewModel.onGoogleLoginSuccess(token)
                            }
                        })
                }
            }
        }
    }
}
