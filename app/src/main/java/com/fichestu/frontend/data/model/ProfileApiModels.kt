package com.fichestu.frontend.data.model

data class ProfileResponseDto(
    val message: String,
    val success: Boolean,
    val username: String,
    val email: String,
    val role: String,
    val profilePicUrl: String?,
    val hasPassword: Boolean = true
)

data class ProfileBadgesResponseDto(
    val message: String,
    val success: Boolean,
    val badges: List<BadgeDto>
)

data class ProfileStatsResponseDto(
    val message: String,
    val success: Boolean,
    val stats: ProfileStatsDto
)

data class UpdateProfileRequestDto(
    val username: String,
    val email: String
)

data class ChangePasswordRequestDto(
    val currentPassword: String? = null,
    val newPassword: String,
    val confirmPassword: String
)

data class GenericResponseDto(
    val message: String,
    val success: Boolean
)
