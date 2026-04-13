package com.fichestu.frontend.data.repository

import com.fichestu.frontend.data.model.ApiErrorResponse
import com.fichestu.frontend.data.model.AuthResponse
import com.fichestu.frontend.data.model.AuthResult
import com.fichestu.frontend.data.model.GoogleLoginRequest
import com.fichestu.frontend.data.model.LoginRequest
import com.fichestu.frontend.data.model.RegisterRequest
import com.fichestu.frontend.data.remote.ApiClient
import com.fichestu.frontend.data.remote.AuthApi
import com.google.gson.Gson
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import retrofit2.Response

class AuthRepository {

    suspend fun login(email: String, password: String): Result<AuthResult> {
        return runSafely {
            val response = ApiClient.authApi.login(
                LoginRequest(email = email, password = password)
            )
            parseResponse(response, "Login correcto")
        }
    }

    suspend fun register(username: String, email: String, password: String): Result<AuthResult> {
        return runSafely {
            val response = ApiClient.authApi.register(
                RegisterRequest(username = username, email = email, password = password)
            )
            parseResponse(response, "Registro completado")
        }
    }

    // Funcion Google
    suspend fun loginWithGoogle(idToken: String): Result<AuthResult> {
        return runSafely {
            val request = GoogleLoginRequest(idToken = idToken)

            // Se lo pasamos a la API
            val response = ApiClient.authApi.loginWithGoogle(request)

            parseResponse(response, "Login con Google correcto")
        }
    }

    private fun parseResponse(
        response: Response<AuthResponse>,
        defaultMessage: String
    ): AuthResult {
        if (response.isSuccessful) {
            val body = response.body()
            return AuthResult(
                message = body?.message ?: defaultMessage,
                token = body?.token
            )
        } else {
            val rawError = response.errorBody()?.string()

            val messageFromServer = runCatching {
                com.google.gson.JsonParser.parseString(rawError)
                    .asJsonObject
                    .get("message")?.asString
            }.getOrNull()

            val finalError = when {
                !messageFromServer.isNullOrBlank() -> messageFromServer
                response.code() == 409 -> "El usuario o email ya está registrado"
                response.code() == 401 -> "Credenciales incorrectas"
                else -> "Error en el servidor (${response.code()})"
            }

            throw Exception(finalError)
        }
    }

    private suspend fun runSafely(block: suspend () -> AuthResult): Result<AuthResult> {
        return try {
            Result.success(block())
        } catch (e: IOException) {
            Result.failure(Exception("Error de red: Verifica tu conexión o si el servidor está activo."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
