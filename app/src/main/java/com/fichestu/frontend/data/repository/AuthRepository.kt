package com.fichestu.frontend.data.repository

import com.fichestu.frontend.data.model.LoginRequest
import com.fichestu.frontend.data.model.RegisterRequest
import com.fichestu.frontend.data.remote.ApiClient

class AuthRepository {
    suspend fun login(baseUrl: String, email: String, password: String): Result<String> {
        return runCatching {
            val response = ApiClient.authApi(baseUrl).login(
                LoginRequest(email = email, password = password)
            )
            if (!response.isSuccessful) {
                throw IllegalStateException("Login fallido: HTTP ${response.code()}")
            }
            response.body()?.message ?: response.body()?.token ?: "Login completado"
        }
    }

    suspend fun register(baseUrl: String, username: String, email: String, password: String): Result<String> {
        return runCatching {
            val response = ApiClient.authApi(baseUrl).register(
                RegisterRequest(username = username, email = email, password = password)
            )
            if (!response.isSuccessful) {
                throw IllegalStateException("Registro fallido: HTTP ${response.code()}")
            }
            response.body()?.message ?: "Registro completado"
        }
    }
}
