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
import com.fichestu.frontend.data.model.WinnerImpactRequestDto
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

    @GET("api/games/ball-room/{matchId}")
    suspend fun matchState(
        @Header("Authorization") authorization: String,
        @Path("matchId") matchId: Int
    ): Response<MatchStateResponseDto>

    @GET("api/game/match/state")
    suspend fun currentMatchState(
        @Header("Authorization") authorization: String
    ): Response<MatchStateResponseDto>

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

    @POST("api/game/matches/{matchId}/matchmaking/cancel")
    suspend fun cancelMatchmaking(
        @Header("Authorization") authorization: String,
        @Path("matchId") matchId: Int
    ): Response<EnterBallRoomResponseDto>

    @POST("api/game/matches/{matchId}/abandon")
    suspend fun abandonMatch(
        @Header("Authorization") authorization: String,
        @Path("matchId") matchId: Int
    ): Response<EnterBallRoomResponseDto>

    @POST("api/game/matches/{matchId}/reveal")
    suspend fun revealMultipliers(
        @Header("Authorization") authorization: String,
        @Path("matchId") matchId: Int
    ): Response<MatchStateResponseDto>

    @POST("api/game/matches/{matchId}/battle/round")
    suspend fun submitBattleAction(
        @Header("Authorization") authorization: String,
        @Path("matchId") matchId: Int,
        @Body request: BattleRoundRequestDto
    ): Response<MatchStateResponseDto>

    @POST("api/game/matches/{matchId}/winner-impact")
    suspend fun applyWinnerImpact(
        @Header("Authorization") authorization: String,
        @Path("matchId") matchId: Int,
        @Body request: WinnerImpactRequestDto
    ): Response<MatchStateResponseDto>

    @POST("api/games/battle/{matchId}/resolve-round")
    suspend fun resolveBattleRound(
        @Header("Authorization") authorization: String,
        @Path("matchId") matchId: Int
    ): Response<MatchStateResponseDto>

    @POST("api/game/matches/{matchId}/close")
    suspend fun closeMatch(
        @Header("Authorization") authorization: String,
        @Path("matchId") matchId: Int
    ): Response<GenericMessageResponseDto>

}
