package com.fichestu.frontend.game.model

import com.fichestu.frontend.game.GameRules

enum class MainTab {
    DASHBOARD,
    BALL_ROOM,
    BATTLE,
    PROFILE
}

enum class TokenId {
    ROJA,
    AZUL,
    VERDE,
    DORADA
}

data class MarketToken(
    val id: TokenId,
    val displayName: String,
    val ticker: String,
    val currentPrice: Double,
    val previousPrice: Double,
    val holdings: Int,
    val history: List<Double>
) {
    val changePercent: Double
        get() = if (previousPrice == 0.0) 0.0 else ((currentPrice - previousPrice) / previousPrice) * 100

    val portfolioValue: Double
        get() = holdings * currentPrice
}

data class MarketUiState(
    val selectedToken: TokenId = TokenId.ROJA,
    val tokens: List<MarketToken> = emptyList(),
    val cashBalance: Double = 0.0,
    val lastResetDayIndex: Long = 0L,
    val resetCountdownLabel: String = ""
) {
    val totalBalance: Double
        get() = cashBalance + tokens.sumOf { it.portfolioValue }

    val selectedMarketToken: MarketToken?
        get() = tokens.firstOrNull { it.id == selectedToken }
}

enum class BallRoomPhase {
    WAITING_ENTRY,
    PICKING,
    REVEALED,
    READY_FOR_BATTLE
}

data class BallOption(
    val id: Int,
    val multiplier: Double,
    val pickedBy: String? = null
) {
    val isPicked: Boolean
        get() = pickedBy != null
}

data class BallPlayer(
    val id: String,
    val nickname: String,
    val isUser: Boolean,
    val selectedBallId: Int? = null,
    val multiplier: Double? = null
)

data class BallRoomUiState(
    val phase: BallRoomPhase = BallRoomPhase.WAITING_ENTRY,
    val players: List<BallPlayer> = emptyList(),
    val balls: List<BallOption> = emptyList(),
    val statusMessage: String = "Paga EUR ${GameRules.BALL_ENTRY_COST.toInt()} para entrar en la sala.",
    val canRevealBattle: Boolean = false
)

enum class BattleCardType {
    ATTACK,
    SHIELD,
    REBOUND
}

enum class BattlePhase {
    LOCKED,
    READY,
    IN_PROGRESS,
    FINISHED
}

data class BattlePlayer(
    val id: String,
    val nickname: String,
    val isUser: Boolean,
    val hp: Int = GameRules.BATTLE_INITIAL_HP,
    val multiplier: Double = 1.0,
    val shieldActive: Boolean = false,
    val reboundActive: Boolean = false
) {
    val isAlive: Boolean
        get() = hp > 0
}

data class BattleUiState(
    val phase: BattlePhase = BattlePhase.LOCKED,
    val players: List<BattlePlayer> = emptyList(),
    val round: Int = 0,
    val log: List<String> = emptyList(),
    val winnerId: String? = null,
    val winnerName: String? = null,
    val winningMultiplier: Double? = null,
    val selectedAction: BattleCardType = BattleCardType.ATTACK,
    val impactApplied: Boolean = false,
    val interstitialAvailable: Boolean = true
)

data class BadgeUi(
    val title: String,
    val description: String,
    val unlocked: Boolean
)

data class ProfileStats(
    val ballRoomsPlayed: Int = 0,
    val battlesPlayed: Int = 0,
    val battlesWon: Int = 0,
    val bestMultiplier: Double = 1.0,
    val averageMultiplier: Double = 1.0,
    val rewardedAdsClaimed: Int = 0,
    val totalMultiplierAccumulated: Double = 0.0
)

data class ProfileUiState(
    val playerName: String = "Jugador",
    val username: String = "",
    val email: String = "",
    val role: String = "USER",
    val profilePicUrl: String? = null,
    val editUsername: String = "",
    val editEmail: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isSavingProfile: Boolean = false,
    val isSavingPassword: Boolean = false,
    val badges: List<BadgeUi> = emptyList(),
    val stats: ProfileStats = ProfileStats()
)

data class GameUiState(
    val currentMatchId: Int? = null,
    val activeTab: MainTab = MainTab.DASHBOARD,
    val market: MarketUiState = MarketUiState(),
    val ballRoom: BallRoomUiState = BallRoomUiState(),
    val battle: BattleUiState = BattleUiState(),
    val profile: ProfileUiState = ProfileUiState(),
    val rewardedAvailable: Boolean = true,
    val rewardedCooldownSec: Int = 0,
    val transientMessage: String? = null
)
