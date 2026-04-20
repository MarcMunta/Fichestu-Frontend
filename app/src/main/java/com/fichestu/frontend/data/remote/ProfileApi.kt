package com.fichestu.frontend.data.remote

import com.fichestu.frontend.data.model.ChangePasswordRequestDto
import com.fichestu.frontend.data.model.GenericResponseDto
import com.fichestu.frontend.data.model.ProfileResponseDto
import com.fichestu.frontend.data.model.UpdateProfileRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

interface ProfileApi {
    @GET("api/profile")
    suspend fun getProfile(
        @Header("Authorization") authorization: String
    ): Response<ProfileResponseDto>

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
}
