package com.fichestu.frontend.data.model

data class GameBootstrapResponse(
    val message: String,
    val success: Boolean,
    val userId: Int,
    val playerName: String,
    val cashBalance: Double,
    val rewardedCooldownSec: Int,
    val tokens: List<GameTokenDto>,
    val stats: ProfileStatsDto
)

data class GameTokenDto(
    val tokenId: Int,
    val name: String,
    val ticker: String,
    val colorCode: String,
    val currentPrice: Double,
    val previousPrice: Double,
    val holdings: Double,
    val history: List<Double>
)

data class ProfileStatsDto(
    val ballRoomsPlayed: Int,
    val battlesPlayed: Int,
    val battlesWon: Int,
    val bestMultiplier: Double,
    val averageMultiplier: Double,
    val rewardedAdsClaimed: Int
)

data class GameTradeRequest(
    val token: String,
    val quantity: Int = 1
)

data class WalletResponseDto(
    val message: String,
    val success: Boolean,
    val cashBalance: Double,
    val totalBalance: Double,
    val tokens: List<GameTokenDto>
)

data class EnterBallRoomResponseDto(
    val message: String,
    val success: Boolean,
    val matchId: Int,
    val cashBalance: Double,
    val ballRoom: BallRoomDto
)

data class BallRoomDto(
    val phase: String,
    val statusMessage: String,
    val canRevealBattle: Boolean,
    val players: List<BallPlayerDto>,
    val balls: List<BallOptionDto>
)

data class BallPlayerDto(
    val id: String,
    val nickname: String,
    val isUser: Boolean,
    val selectedBallId: Int?,
    val multiplier: Double?
)

data class BallOptionDto(
    val id: Int,
    val multiplier: Double?,
    val pickedBy: String?
)

data class PickBallRequestDto(
    val ballId: Int
)

data class MatchStateResponseDto(
    val message: String,
    val success: Boolean,
    val matchId: Int?,
    val ballRoom: BallRoomDto,
    val battle: BattleDto
)

data class BattleRoundRequestDto(
    val action: String,
    val selectedToken: String
)

data class BattleDto(
    val phase: String,
    val round: Int,
    val winnerId: String?,
    val winnerName: String?,
    val winningMultiplier: Double?,
    val selectedAction: String,
    val interstitialAvailable: Boolean,
    val log: List<String>,
    val players: List<BattlePlayerDto>
)

data class BattlePlayerDto(
    val id: String,
    val nickname: String,
    val isUser: Boolean,
    val hp: Int,
    val multiplier: Double,
    val isAlive: Boolean
)

data class CooldownResponseDto(
    val message: String,
    val success: Boolean,
    val cashBalance: Double,
    val rewardedCooldownSec: Int,
    val rewardedAdsClaimed: Int
)

data class GenericMessageResponseDto(
    val message: String,
    val success: Boolean
)
