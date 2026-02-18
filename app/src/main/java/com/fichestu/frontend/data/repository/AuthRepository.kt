package com.fichestu.frontend.data.repository

import com.fichestu.frontend.data.model.ApiErrorResponse
import com.fichestu.frontend.data.model.AuthResponse
import com.fichestu.frontend.data.model.AuthResult
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
    private val gson = Gson()

    suspend fun login(baseUrl: String, email: String, password: String): Result<AuthResult> {
        return runSafely(baseUrl) {
            val response = performRequestWithFallback(baseUrl) { api ->
                api.login(
                    request = LoginRequest(
                        email = email,
                        password = password
                    )
                )
            }

            parseSuccessfulAuthResponse(response, fallbackMessage = "Login correcto").let { result ->
                val token = result.token?.takeIf { it.isNotBlank() }
                    ?: throw IllegalStateException("El backend no devolvio token en el login")
                result.copy(token = token)
            }
        }
    }

    suspend fun register(baseUrl: String, username: String, email: String, password: String): Result<AuthResult> {
        return runSafely(baseUrl) {
            val response = performRequestWithFallback(baseUrl) { api ->
                api.register(
                    request = RegisterRequest(
                        username = username,
                        email = email,
                        password = password
                    )
                )
            }

            parseSuccessfulAuthResponse(response, fallbackMessage = "Registro completado")
        }
    }

    private suspend fun runSafely(baseUrl: String, block: suspend () -> AuthResult): Result<AuthResult> {
        return runCatching {
            block()
        }.mapFailure { error ->
            mapToUserFacingError(baseUrl = baseUrl, throwable = error)
        }
    }

    private suspend fun performRequestWithFallback(
        baseUrl: String,
        call: suspend (api: AuthApi) -> Response<AuthResponse>
    ): Response<AuthResponse> {
        val candidates = buildBaseUrlCandidates(baseUrl)
        var lastNetworkError: IOException? = null

        for (candidate in candidates) {
            try {
                return call(ApiClient.authApi(candidate))
            } catch (error: IOException) {
                lastNetworkError = error
            }
        }

        throw lastNetworkError
            ?: IllegalStateException("No se pudo conectar al backend")
    }

    private fun parseSuccessfulAuthResponse(
        response: Response<AuthResponse>,
        fallbackMessage: String
    ): AuthResult {
        if (response.isSuccessful) {
            val body = response.body()
            val success = body?.success ?: true
            if (!success) {
                throw IllegalStateException(body?.message ?: "Operacion rechazada por el backend")
            }

            return AuthResult(
                message = body?.message?.takeIf { it.isNotBlank() } ?: fallbackMessage,
                token = body?.token
            )
        }

        val backendMessage = extractBackendMessage(response)
        throw IllegalStateException(
            backendMessage ?: "Operacion fallida (HTTP ${response.code()})"
        )
    }

    private fun extractBackendMessage(response: Response<AuthResponse>): String? {
        val rawBody = response.errorBody()?.string()?.trim().orEmpty()
        if (rawBody.isBlank()) {
            return null
        }

        val authMessage = runCatching {
            gson.fromJson(rawBody, AuthResponse::class.java)?.message
        }.getOrNull()
        if (!authMessage.isNullOrBlank()) {
            return authMessage
        }

        val apiMessage = runCatching {
            gson.fromJson(rawBody, ApiErrorResponse::class.java)?.message
        }.getOrNull()
        if (!apiMessage.isNullOrBlank()) {
            return apiMessage
        }

        return null
    }

    private fun buildBaseUrlCandidates(baseUrl: String): List<String> {
        val normalizedUrl = normalizeBaseUrl(baseUrl)
        val parsed = normalizedUrl.toHttpUrlOrNull() ?: return listOf(normalizedUrl)
        if (parsed.host != EMULATOR_HOST) {
            return listOf(normalizedUrl)
        }

        val fallbackPort = when (parsed.port) {
            8080 -> 8081
            8081 -> 8080
            else -> null
        } ?: return listOf(normalizedUrl)

        val fallbackUrl = parsed.newBuilder()
            .port(fallbackPort)
            .build()
            .toString()

        return listOf(normalizedUrl, fallbackUrl).distinct()
    }

    private fun normalizeBaseUrl(baseUrl: String): String {
        val trimmed = baseUrl.trim()
        return if (trimmed.endsWith('/')) trimmed else "$trimmed/"
    }

    private fun mapToUserFacingError(baseUrl: String, throwable: Throwable): Throwable {
        if (throwable is IllegalStateException) {
            return throwable
        }

        val message = when (throwable) {
            is SocketTimeoutException -> {
                "Timeout al conectar con el backend (${normalizeBaseUrl(baseUrl)})"
            }

            is ConnectException, is UnknownHostException -> {
                "No se pudo conectar al backend (${normalizeBaseUrl(baseUrl)}). Verifica que Fichestu-Backend este levantado."
            }

            is IOException -> {
                "Error de red al conectar con el backend"
            }

            else -> {
                throwable.message ?: "Error inesperado"
            }
        }

        return IllegalStateException(message, throwable)
    }

    private inline fun <T> Result<T>.mapFailure(transform: (Throwable) -> Throwable): Result<T> {
        return fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(transform(it)) }
        )
    }

    private companion object {
        const val EMULATOR_HOST = "10.0.2.2"
    }
}
