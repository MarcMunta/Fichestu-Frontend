package com.fichestu.frontend.data.remote

import com.fichestu.frontend.data.model.GenericMessageResponseDto
import com.fichestu.frontend.data.model.NotificationListResponseDto
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface NotificationApi {
    @GET("api/notifications")
    suspend fun list(
        @Header("Authorization") authorization: String
    ): Response<NotificationListResponseDto>

    @POST("api/notifications/read-all")
    suspend fun markAllRead(
        @Header("Authorization") authorization: String
    ): Response<GenericMessageResponseDto>

    @DELETE("api/notifications")
    suspend fun clear(
        @Header("Authorization") authorization: String
    ): Response<GenericMessageResponseDto>
}
