package com.fichestu.frontend.data.remote

import com.fichestu.frontend.data.model.ChangePasswordRequestDto
import com.fichestu.frontend.data.model.GenericResponseDto
import com.fichestu.frontend.data.model.ProfileBadgesResponseDto
import com.fichestu.frontend.data.model.ProfileResponseDto
import com.fichestu.frontend.data.model.ProfileStatsResponseDto
import com.fichestu.frontend.data.model.UpdateProfileRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import okhttp3.MultipartBody

interface ProfileApi {
    @GET("api/profile")
    suspend fun getProfile(
        @Header("Authorization") authorization: String
    ): Response<ProfileResponseDto>

    @GET("api/profile/badges")
    suspend fun getBadges(
        @Header("Authorization") authorization: String
    ): Response<ProfileBadgesResponseDto>

    @GET("api/profile/stats")
    suspend fun getStats(
        @Header("Authorization") authorization: String
    ): Response<ProfileStatsResponseDto>

    @PUT("api/profile")
    suspend fun updateProfile(
        @Header("Authorization") authorization: String,
        @Body request: UpdateProfileRequestDto
    ): Response<ProfileResponseDto>

    @POST("api/profile/change-password")
    suspend fun changePassword(
        @Header("Authorization") authorization: String,
        @Body request: ChangePasswordRequestDto
    ): Response<GenericResponseDto>

    @Multipart
    @POST("api/profile/avatar")
    suspend fun uploadAvatar(
        @Header("Authorization") authorization: String,
        @Part avatar: MultipartBody.Part
    ): Response<ProfileResponseDto>
}
