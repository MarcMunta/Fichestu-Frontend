package com.fichestu.frontend.util

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

class GoogleAuthHelper(private val activity: ComponentActivity) {
    private val webClientId = "376595931736-ts5451g69bk8rd6re82o6ln1p28m4i2l.apps.googleusercontent.com"


    fun launchGoogleLogin(onTokenReceived: (String) -> Unit) {
        val credentialManager = CredentialManager.create(activity)

        val googleIdOption = GetSignInWithGoogleOption.Builder(webClientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        activity.lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = GoogleIdTokenCredential.createFrom(result.credential.data)

                // Devolvemos el token hacia afuera
                onTokenReceived(credential.idToken)
            } catch (e: Exception) {
                Log.e("GOOGLE_AUTH", "Error: ${e.message}")
            }
        }
    }
}