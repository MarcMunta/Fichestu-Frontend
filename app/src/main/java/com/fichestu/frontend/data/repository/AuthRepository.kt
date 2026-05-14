package com.fichestu.frontend.data.repository

import com.fichestu.frontend.data.model.ApiErrorResponse
import com.fichestu.frontend.data.model.AuthResponse
import com.fichestu.frontend.data.model.AuthResult
import com.fichestu.frontend.data.model.GenericResponse
import com.fichestu.frontend.data.model.GoogleLoginRequest
import com.fichestu.frontend.data.model.LoginRequest
import com.fichestu.frontend.data.model.PasswordResetConfirmRequest
import com.fichestu.frontend.data.model.PasswordResetRequest
import com.fichestu.frontend.data.model.RegisterRequest
import com.fichestu.frontend.data.model.SessionResponse
import com.fichestu.frontend.data.i18n.AppI18n
import com.fichestu.frontend.data.remote.ApiClient
import com.fichestu.frontend.game.model.AppLanguage
import com.google.gson.Gson
import java.io.IOException
import retrofit2.Response

class AuthRepository {

    suspend fun login(email: String, password: String): Result<AuthResult> {
        return runSafely {
            val response = ApiClient.authApi.login(
                LoginRequest(email = email, password = password)
            )
            val result = parseResponse(response, "Login successful", requireToken = true)
            SessionStore.setAuth(result.token, email.substringBefore('@'))
            result
        }
    }

    suspend fun register(username: String, email: String, password: String): Result<AuthResult> {
        return runSafely {
            val response = ApiClient.authApi.register(
                RegisterRequest(username = username, email = email, password = password)
            )
            parseResponse(response, "Registration complete", requireToken = false)
        }
    }

    suspend fun loginWithGoogle(idToken: String): Result<AuthResult> {
        return runSafely {
            val response = ApiClient.authApi.loginWithGoogle(
                GoogleLoginRequest(idToken = idToken)
            )
            val result = parseResponse(response, "Google login successful", requireToken = true)
            SessionStore.setAuth(result.token, SessionStore.displayName())
            result
        }
    }

    suspend fun validateSession(): Result<SessionResponse> {
        return try {
            val auth = SessionStore.authHeaderOrNull()
                ?: return Result.failure(Exception("Session not started"))
            val response = ApiClient.userApi.me(auth)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Could not validate session (${response.code()})"))
            }
        } catch (error: IOException) {
            Result.failure(Exception("Network error: check connection or server."))
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            val auth = SessionStore.authHeaderOrNull()
                ?: return Result.success(Unit)
            ApiClient.authApi.logout(auth)
            Result.success(Unit)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    suspend fun requestPasswordReset(email: String): Result<String> {
        return runMessageSafely {
            val response = ApiClient.authApi.requestPasswordReset(
                PasswordResetRequest(email = email)
            )
            parseMessageResponse(response, "Check your email to continue")
        }
    }

    suspend fun confirmPasswordReset(
        email: String,
        token: String,
        newPassword: String,
        confirmPassword: String
    ): Result<String> {
        return runMessageSafely {
            val response = ApiClient.authApi.confirmPasswordReset(
                PasswordResetConfirmRequest(
                    email = email,
                    token = token,
                    newPassword = newPassword,
                    confirmPassword = confirmPassword
                )
            )
            parseMessageResponse(response, "Password updated successfully")
        }
    }

    private fun parseResponse(
        response: Response<AuthResponse>,
        defaultMessage: String,
        requireToken: Boolean
    ): AuthResult {
        if (response.isSuccessful) {
            val body = response.body()
            val token = body?.token?.trim()
            if (requireToken && token.isNullOrBlank()) {
                throw Exception("Login failed: server did not return a session token")
            }
            return AuthResult(
                message = body?.message ?: defaultMessage,
                token = token
            )
        }

        val rawError = response.errorBody()?.string()
        val apiError = runCatching {
            Gson().fromJson(rawError, ApiErrorResponse::class.java)
        }.getOrNull()
        val finalError = when {
            !apiError?.message.isNullOrBlank() -> AppI18n.message(apiError?.message, AppLanguage.EN) ?: apiError?.message
            response.code() == 409 -> "User or email is already registered"
            response.code() == 401 -> "Wrong credentials"
            else -> "Server error (${response.code()})"
        }

        throw Exception(finalError)
    }

    private fun parseMessageResponse(
        response: Response<GenericResponse>,
        defaultMessage: String
    ): String {
        if (response.isSuccessful) {
            return response.body()?.message ?: defaultMessage
        }

        val rawError = response.errorBody()?.string()
        val apiError = runCatching {
            Gson().fromJson(rawError, ApiErrorResponse::class.java)
        }.getOrNull()
        val finalError = when {
            !apiError?.message.isNullOrBlank() -> AppI18n.message(apiError?.message, AppLanguage.EN) ?: apiError?.message
            response.code() == 400 -> "Invalid data"
            else -> "Server error (${response.code()})"
        }

        throw Exception(finalError)
    }

    private suspend fun runSafely(block: suspend () -> AuthResult): Result<AuthResult> {
        return try {
            Result.success(block())
        } catch (error: IOException) {
            Result.failure(Exception("Network error: check connection or server."))
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    private suspend fun runMessageSafely(block: suspend () -> String): Result<String> {
        return try {
            Result.success(block())
        } catch (error: IOException) {
            Result.failure(Exception("Network error: check connection or server."))
        } catch (error: Exception) {
            Result.failure(error)
        }
    }
}
