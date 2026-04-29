package com.fichestu.frontend.data.remote

import com.fichestu.frontend.data.model.SessionResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface UserApi {
    @GET("api/auth/me")
    suspend fun me(
        @Header("Authorization") authorization: String
    ): Response<SessionResponse>
}
