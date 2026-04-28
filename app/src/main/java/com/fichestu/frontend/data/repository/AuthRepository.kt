package com.fichestu.frontend.data.repository

import com.fichestu.frontend.data.model.ApiErrorResponse
import com.fichestu.frontend.data.model.AuthResponse
import com.fichestu.frontend.data.model.AuthResult
import com.fichestu.frontend.data.model.GoogleLoginRequest
import com.fichestu.frontend.data.model.LoginRequest
import com.fichestu.frontend.data.model.RegisterRequest
import com.fichestu.frontend.data.model.SessionResponse
import com.fichestu.frontend.data.remote.ApiClient
import com.google.gson.Gson
import java.io.IOException
import retrofit2.Response

class AuthRepository {

    suspend fun login(email: String, password: String): Result<AuthResult> {
        return runSafely {
            val response = ApiClient.authApi.login(
                LoginRequest(email = email, password = password)
            )
            val result = parseResponse(response, "Login correcto")
            SessionStore.setAuth(result.token, email.substringBefore('@'))
            result
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

    suspend fun loginWithGoogle(idToken: String): Result<AuthResult> {
        return runSafely {
            val response = ApiClient.authApi.loginWithGoogle(
                GoogleLoginRequest(idToken = idToken)
            )
            val result = parseResponse(response, "Login con Google correcto")
            SessionStore.setAuth(result.token, SessionStore.displayName())
            result
        }
    }

    suspend fun validateSession(): Result<SessionResponse> {
        return try {
            val auth = SessionStore.authHeaderOrNull()
                ?: return Result.failure(Exception("Sesion no iniciada"))
            val response = ApiClient.authApi.me(auth)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("No se pudo validar la sesion (${response.code()})"))
            }
        } catch (error: IOException) {
            Result.failure(Exception("Error de red: verifica conexion o servidor."))
        } catch (error: Exception) {
            Result.failure(error)
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
        }

        val rawError = response.errorBody()?.string()
        val apiError = runCatching {
            Gson().fromJson(rawError, ApiErrorResponse::class.java)
        }.getOrNull()
        val finalError = when {
            !apiError?.message.isNullOrBlank() -> apiError?.message
            response.code() == 409 -> "El usuario o email ya esta registrado"
            response.code() == 401 -> "Credenciales incorrectas"
            else -> "Error en el servidor (${response.code()})"
        }

        throw Exception(finalError)
    }

    private suspend fun runSafely(block: suspend () -> AuthResult): Result<AuthResult> {
        return try {
            Result.success(block())
        } catch (error: IOException) {
            Result.failure(Exception("Error de red: verifica conexion o servidor."))
        } catch (error: Exception) {
            Result.failure(error)
        }
    }
}
