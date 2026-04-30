package com.fichestu.frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import com.fichestu.frontend.data.repository.SessionStore
import com.fichestu.frontend.navigation.AppNavGraph
import com.fichestu.frontend.ui.theme.FichestuTheme
import com.fichestu.frontend.ui.theme.NightBlue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SessionStore.init(this)
        enableEdgeToEdge()

        setContent {
            FichestuTheme {
                Surface(color = NightBlue) {
                    AppNavGraph()
                }
            }
        }
    }
}
