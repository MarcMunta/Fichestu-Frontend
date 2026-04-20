package com.fichestu.frontend.data.remote

import com.fichestu.frontend.data.model.BattleRoundRequestDto
import com.fichestu.frontend.data.model.CooldownResponseDto
import com.fichestu.frontend.data.model.EnterBallRoomResponseDto
import com.fichestu.frontend.data.model.GameBootstrapResponse
import com.fichestu.frontend.data.model.GameTradeRequest
import com.fichestu.frontend.data.model.GenericMessageResponseDto
import com.fichestu.frontend.data.model.MatchStateResponseDto
import com.fichestu.frontend.data.model.PickBallRequestDto
import com.fichestu.frontend.data.model.WalletResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface GameApi {
    @GET("api/game/bootstrap")
    suspend fun bootstrap(
        @Header("Authorization") authorization: String
    ): Response<GameBootstrapResponse>

    @GET("api/game/match/state")
    suspend fun currentMatchState(
        @Header("Authorization") authorization: String
    ): Response<MatchStateResponseDto>

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

    @POST("api/game/ball-room/enter")
    suspend fun enterBallRoom(
        @Header("Authorization") authorization: String
    ): Response<EnterBallRoomResponseDto>

    @POST("api/game/matches/{matchId}/pick-ball")
    suspend fun pickBall(
        @Header("Authorization") authorization: String,
        @Path("matchId") matchId: Int,
        @Body request: PickBallRequestDto
    ): Response<MatchStateResponseDto>

    @POST("api/game/matches/{matchId}/reveal")
    suspend fun revealMultipliers(
        @Header("Authorization") authorization: String,
        @Path("matchId") matchId: Int
    ): Response<MatchStateResponseDto>

    @POST("api/game/matches/{matchId}/battle/round")
    suspend fun playBattleRound(
        @Header("Authorization") authorization: String,
        @Path("matchId") matchId: Int,
        @Body request: BattleRoundRequestDto
    ): Response<MatchStateResponseDto>

    @POST("api/game/matches/{matchId}/close")
    suspend fun closeMatch(
        @Header("Authorization") authorization: String,
        @Path("matchId") matchId: Int
    ): Response<GenericMessageResponseDto>

    @POST("api/game/rewarded/claim")
    suspend fun claimRewarded(
        @Header("Authorization") authorization: String
    ): Response<CooldownResponseDto>
}
