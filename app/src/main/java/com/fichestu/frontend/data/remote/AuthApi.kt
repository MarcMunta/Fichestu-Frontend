package com.fichestu.frontend.data.remote

import com.fichestu.frontend.data.model.AuthResponse
import com.fichestu.frontend.data.model.GenericResponse
import com.fichestu.frontend.data.model.GoogleLoginRequest
import com.fichestu.frontend.data.model.LoginRequest
import com.fichestu.frontend.data.model.PasswordResetConfirmRequest
import com.fichestu.frontend.data.model.PasswordResetRequest
import com.fichestu.frontend.data.model.RegisterRequest
import com.fichestu.frontend.data.model.SessionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    @POST("api/auth/google")
    suspend fun loginWithGoogle(
        @Body request: GoogleLoginRequest
    ): Response<AuthResponse>

    @POST("api/auth/logout")
    suspend fun logout(
        @Header("Authorization") authorization: String
    ): Response<GenericResponse>

    @POST("api/auth/password-reset/request")
    suspend fun requestPasswordReset(
        @Body request: PasswordResetRequest
    ): Response<GenericResponse>

    @POST("api/auth/password-reset/confirm")
    suspend fun confirmPasswordReset(
        @Body request: PasswordResetConfirmRequest
    ): Response<GenericResponse>

    @GET("api/me")
    suspend fun me(
        @Header("Authorization") authorization: String
    ): Response<SessionResponse>
}
