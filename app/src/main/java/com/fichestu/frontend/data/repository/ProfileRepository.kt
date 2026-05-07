package com.fichestu.frontend.data.repository

import com.fichestu.frontend.data.model.BadgeDto
import com.fichestu.frontend.data.model.ChangePasswordRequestDto
import com.fichestu.frontend.data.model.ProfileResponseDto
import com.fichestu.frontend.data.model.ProfileStatsDto
import com.fichestu.frontend.data.model.UpdateLanguageRequestDto
import com.fichestu.frontend.data.model.UpdateProfileRequestDto
import com.fichestu.frontend.BuildConfig
import com.fichestu.frontend.data.i18n.AppI18n
import com.fichestu.frontend.data.remote.ApiClient
import com.fichestu.frontend.game.model.AppLanguage
import com.fichestu.frontend.game.model.BadgeUi
import com.fichestu.frontend.game.model.GameUiState
import com.fichestu.frontend.game.model.ProfileStats
import com.fichestu.frontend.game.model.ProfileUiState
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ProfileRepository {

    suspend fun loadProfile(currentState: GameUiState): Result<GameUiState> = runSafely {
        val auth = requireAuth()
        val response = ApiClient.profileApi.getProfile(auth)
        val dto = parseResponse(response.isSuccessful, response.body(), response.errorBody()?.string(), response.code())

        val statsResponse = ApiClient.profileApi.getStats(auth)
        val statsDto = parseResponse(
            statsResponse.isSuccessful,
            statsResponse.body(),
            statsResponse.errorBody()?.string(),
            statsResponse.code()
        ).stats

        val badgesResponse = ApiClient.profileApi.getBadges(auth)
        val badgesDto = parseResponse(
            badgesResponse.isSuccessful,
            badgesResponse.body(),
            badgesResponse.errorBody()?.string(),
            badgesResponse.code()
        ).badges
        val profileLanguage = AppLanguage.fromCode(dto.preferredLanguage)
        SessionStore.setLanguage(profileLanguage)

        currentState.copy(
            appLanguage = profileLanguage,
            profile = mapProfile(currentState.profile, dto).copy(
                stats = mapStats(statsDto),
                badges = badgesDto.map { it.toBadgeUi() }
            ),
            transientMessage = if (dto.hasPassword) {
                null
            } else {
                AppI18n.text("add_password", profileLanguage)
            }
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
            transientMessage = AppI18n.message(dto.message) ?: dto.message
        )
    }

    suspend fun saveLanguage(currentState: GameUiState, language: AppLanguage): Result<GameUiState> = runSafely {
        val response = ApiClient.profileApi.updateLanguage(
            authorization = requireAuth(),
            request = UpdateLanguageRequestDto(language = language.code)
        )
        val dto = parseResponse(response.isSuccessful, response.body(), response.errorBody()?.string(), response.code())
        val profileLanguage = AppLanguage.fromCode(dto.preferredLanguage)
        SessionStore.setLanguage(profileLanguage)

        currentState.copy(
            appLanguage = profileLanguage,
            profile = mapProfile(currentState.profile, dto),
            transientMessage = AppI18n.message(dto.message, profileLanguage) ?: dto.message
        )
    }

    suspend fun changePassword(currentState: GameUiState): Result<GameUiState> = runSafely {
        val profile = currentState.profile
        val confirmPassword = if (profile.hasPassword) profile.confirmPassword else profile.newPassword

        if (profile.newPassword != confirmPassword) {
            throw Exception(AppI18n.text("passwords_do_not_match"))
        }

        val response = ApiClient.profileApi.changePassword(
            authorization = requireAuth(),
            request = ChangePasswordRequestDto(
                currentPassword = profile.currentPassword.trim().takeIf { profile.hasPassword && it.isNotBlank() },
                newPassword = profile.newPassword,
                confirmPassword = confirmPassword
            )
        )

        if (response.code() == 401 || response.code() == 403) {
            SessionStore.clear()
            throw SessionExpiredException()
        }

        if (!response.isSuccessful) {
            val message = extractMessage(response.errorBody()?.string()) ?: "No se pudo cambiar la contraseña"
            if (!profile.hasPassword && message.contains("actual", ignoreCase = true)) {
                return@runSafely currentState.copy(
                    profile = currentState.profile.copy(
                        hasPassword = true,
                        currentPassword = "",
                        newPassword = "",
                        confirmPassword = "",
                        isSavingPassword = false
                    ),
                    transientMessage = AppI18n.text("account_has_password")
                )
            }
            throw Exception(message)
        }

        currentState.copy(
            profile = currentState.profile.copy(
                currentPassword = "",
                newPassword = "",
                confirmPassword = "",
                hasPassword = true,
                isSavingPassword = false
            ),
            transientMessage = AppI18n.message(response.body()?.message) ?: AppI18n.text("password_updated")
        )
    }

    suspend fun uploadAvatar(currentState: GameUiState, bytes: ByteArray, mimeType: String): Result<GameUiState> = runSafely {
        val mediaType = mimeType.toMediaTypeOrNull() ?: "image/jpeg".toMediaTypeOrNull()
        val body = bytes.toRequestBody(mediaType)
        val part = MultipartBody.Part.createFormData("avatar", "avatar", body)
        val response = ApiClient.profileApi.uploadAvatar(requireAuth(), part)
        val dto = parseResponse(response.isSuccessful, response.body(), response.errorBody()?.string(), response.code())

        currentState.copy(
            profile = mapProfile(currentState.profile, dto).copy(isSavingProfile = false),
            transientMessage = AppI18n.message(dto.message) ?: dto.message
        )
    }

    private fun mapProfile(current: ProfileUiState, dto: ProfileResponseDto): ProfileUiState {
        return current.copy(
            playerName = dto.username,
            username = dto.username,
            email = dto.email,
            role = dto.role,
            profilePicUrl = dto.profilePicUrl.toAbsoluteProfileUrl(),
            hasPassword = dto.hasPassword,
            editUsername = dto.username,
            editEmail = dto.email
        )
    }

    private fun String?.toAbsoluteProfileUrl(): String? {
        if (isNullOrBlank()) return null
        if (startsWith("http://", ignoreCase = true) || startsWith("https://", ignoreCase = true)) return this
        val base = BuildConfig.BASE_URL.trimEnd('/')
        return "$base${if (startsWith("/")) this else "/$this"}"
    }

    private fun mapStats(dto: ProfileStatsDto): ProfileStats {
        return ProfileStats(
            ballRoomsPlayed = dto.ballRoomsPlayed,
            battlesPlayed = dto.battlesPlayed,
            battlesWon = dto.battlesWon,
            bestMultiplier = dto.bestMultiplier,
            averageMultiplier = dto.averageMultiplier,
            rewardedAdsClaimed = dto.rewardedAdsClaimed,
            totalMultiplierAccumulated = dto.averageMultiplier * dto.ballRoomsPlayed
        )
    }

    private fun BadgeDto.toBadgeUi(): BadgeUi {
        return BadgeUi(
            title = title,
            description = description,
            unlocked = unlocked
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
        throw Exception(AppI18n.message(extractMessage(errorRaw)) ?: AppI18n.text("server_error"))
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
            Result.failure(Exception(AppI18n.text("network_error")))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
