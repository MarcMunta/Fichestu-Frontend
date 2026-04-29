package com.fichestu.frontend.data.remote

import com.fichestu.frontend.data.model.CooldownResponseDto
import com.fichestu.frontend.data.model.GameMarketResponseDto
import com.fichestu.frontend.data.model.GameTradeRequest
import com.fichestu.frontend.data.model.WalletResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface MarketApi {
    @GET("api/game/market")
    suspend fun market(
        @Header("Authorization") authorization: String
    ): Response<GameMarketResponseDto>

    @POST("api/game/market/buy")
    suspend fun buy(
        @Header("Authorization") authorization: String,
        @Body request: GameTradeRequest
    ): Response<WalletResponseDto>

    @POST("api/game/market/sell")
    suspend fun sell(
        @Header("Authorization") authorization: String,
        @Body request: GameTradeRequest
    ): Response<WalletResponseDto>

    @POST("api/game/rewarded/claim")
    suspend fun claimReward(
        @Header("Authorization") authorization: String
    ): Response<CooldownResponseDto>
}
