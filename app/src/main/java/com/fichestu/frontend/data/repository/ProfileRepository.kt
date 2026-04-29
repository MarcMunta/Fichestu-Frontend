package com.fichestu.frontend.data.repository

import com.fichestu.frontend.data.model.ChangePasswordRequestDto
import com.fichestu.frontend.data.model.ProfileResponseDto
import com.fichestu.frontend.data.model.UpdateProfileRequestDto
import com.fichestu.frontend.data.remote.ApiClient
import com.fichestu.frontend.game.model.GameUiState
import com.fichestu.frontend.game.model.ProfileUiState
import java.io.IOException

class ProfileRepository {

    suspend fun loadProfile(currentState: GameUiState): Result<GameUiState> = runSafely {
        val response = ApiClient.profileApi.getProfile(requireAuth())
        val dto = parseResponse(response.isSuccessful, response.body(), response.errorBody()?.string(), response.code())
        currentState.copy(
            profile = currentState.profile.copy(
                playerName = dto.username,
                username = dto.username,
                email = dto.email,
                role = dto.role,
                profilePicUrl = dto.profilePicUrl,
                editUsername = dto.username,
                editEmail = dto.email
            ),
            transientMessage = null
        )
    }

    suspend fun saveProfile(currentState: GameUiState): Result<GameUiState> = runSafely {
        val profile = currentState.profile
        val response = ApiClient.profileApi.updateProfile(
            authorization = requireAuth(),
            request = UpdateProfileRequestDto(
                username = profile.editUsername.trim(),
                email = profile.editEmail.trim()
            )
        )
        val dto = parseResponse(response.isSuccessful, response.body(), response.errorBody()?.string(), response.code())
        currentState.copy(
            profile = mapProfile(currentState.profile, dto).copy(isSavingProfile = false),
            transientMessage = dto.message
        )
    }

    suspend fun changePassword(currentState: GameUiState): Result<GameUiState> = runSafely {
        val profile = currentState.profile
        if (profile.newPassword != profile.confirmPassword) {
            throw Exception("Las contraseñas no coinciden")
        }

        val response = ApiClient.profileApi.changePassword(
            authorization = requireAuth(),
            request = ChangePasswordRequestDto(
                currentPassword = profile.currentPassword.trim().takeIf { it.isNotBlank() },
                newPassword = profile.newPassword,
                confirmPassword = profile.confirmPassword
            )
        )

        if (response.code() == 401 || response.code() == 403) {
            SessionStore.clear()
            throw SessionExpiredException()
        }

        if (!response.isSuccessful) {
            throw Exception(extractMessage(response.errorBody()?.string()) ?: "No se pudo cambiar la contraseña")
        }

        currentState.copy(
            profile = currentState.profile.copy(
                currentPassword = "",
                newPassword = "",
                confirmPassword = "",
                isSavingPassword = false
            ),
            transientMessage = response.body()?.message ?: "Contraseña actualizada"
        )
    }

    private fun mapProfile(current: ProfileUiState, dto: ProfileResponseDto): ProfileUiState {
        return current.copy(
            playerName = dto.username,
            username = dto.username,
            email = dto.email,
            role = dto.role,
            profilePicUrl = dto.profilePicUrl,
            editUsername = dto.username,
            editEmail = dto.email
        )
    }

    private fun requireAuth(): String {
        return SessionStore.authHeaderOrNull() ?: throw SessionExpiredException()
    }

    private fun <T> parseResponse(success: Boolean, body: T?, errorRaw: String?, code: Int): T {
        if (code == 401 || code == 403) {
            SessionStore.clear()
            throw SessionExpiredException()
        }
        if (success && body != null) return body
        throw Exception(extractMessage(errorRaw) ?: "Error de servidor")
    }

    private fun extractMessage(rawError: String?): String? {
        if (rawError.isNullOrBlank()) return null
        return runCatching {
            com.google.gson.JsonParser.parseString(rawError).asJsonObject.get("message")?.asString
        }.getOrNull()
    }

    private suspend fun <T> runSafely(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: IOException) {
            Result.failure(Exception("Error de red: verifica conexión y backend activo."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
