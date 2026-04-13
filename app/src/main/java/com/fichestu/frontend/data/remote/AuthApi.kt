package com.fichestu.frontend.data.remote

import com.fichestu.frontend.data.model.AuthResponse
import com.fichestu.frontend.data.model.GoogleLoginRequest
import com.fichestu.frontend.data.model.LoginRequest
import com.fichestu.frontend.data.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
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
}
