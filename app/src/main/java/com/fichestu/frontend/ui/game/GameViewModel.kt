package com.fichestu.frontend.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fichestu.frontend.game.GameRules
import com.fichestu.frontend.game.engine.GameEngine
import com.fichestu.frontend.game.model.BadgeUi
import com.fichestu.frontend.game.model.BallRoomPhase
import com.fichestu.frontend.game.model.BallRoomUiState
import com.fichestu.frontend.game.model.BattleCardType
import com.fichestu.frontend.game.model.BattlePhase
import com.fichestu.frontend.game.model.BattleUiState
import com.fichestu.frontend.game.model.GameUiState
import com.fichestu.frontend.game.model.MainTab
import com.fichestu.frontend.game.model.ProfileStats
import com.fichestu.frontend.game.model.ProfileUiState
import com.fichestu.frontend.game.model.TokenId
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameViewModel(
    private val engine: GameEngine = GameEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var marketTickCounter = 0
    private var rewardedCooldownJob: Job? = null

    init {
        startClockAndMarketLoop()
    }

    fun initializePlayer(name: String) {
        val safe = name.trim().ifBlank { "Jugador" }
        _uiState.update { state ->
            state.copy(profile = state.profile.copy(playerName = safe))
        }
    }

    fun selectTab(tab: MainTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun selectToken(tokenId: TokenId) {
        _uiState.update { state ->
            state.copy(market = engine.selectToken(state.market, tokenId))
        }
    }

    fun buySelectedToken() {
        _uiState.update { state ->
            val (nextMarket, message) = engine.buyOneSelected(state.market)
            state.copy(market = nextMarket, transientMessage = message)
        }
    }

    fun sellSelectedToken() {
        _uiState.update { state ->
            val (nextMarket, message) = engine.sellOneSelected(state.market)
            state.copy(market = nextMarket, transientMessage = message)
        }
    }

    fun enterBallRoom() {
        _uiState.update { state ->
            if (state.market.cashBalance < GameRules.BALL_ENTRY_COST) {
                return@update state.copy(
                    transientMessage = "Saldo insuficiente: necesitas EUR ${GameRules.BALL_ENTRY_COST.toInt()} para entrar."
                )
            }

            val nextRoom = engine.createBallRoom()
            val updatedStats = state.profile.stats.copy(
                ballRoomsPlayed = state.profile.stats.ballRoomsPlayed + 1
            )
            state.copy(
                activeTab = MainTab.BALL_ROOM,
                market = state.market.copy(cashBalance = state.market.cashBalance - GameRules.BALL_ENTRY_COST),
                ballRoom = nextRoom,
                battle = BattleUiState(
                    phase = BattlePhase.LOCKED,
                    log = listOf("Completa el sorteo de bolas para desbloquear el Battle Royale.")
                ),
                profile = state.profile.copy(stats = updatedStats),
                transientMessage = "Has entrado en la sala de bolas."
            )
        }
    }

    fun pickBall(ballId: Int) {
        _uiState.update { state ->
            val (nextRoom, message) = engine.pickUserBall(state.ballRoom, ballId)
            state.copy(ballRoom = nextRoom, transientMessage = message)
        }
    }

    fun revealBallMultipliers() {
        _uiState.update { state ->
            val (nextRoom, message) = engine.revealMultipliers(state.ballRoom)
            val battle = if (nextRoom.phase == BallRoomPhase.REVEALED) {
                engine.createBattle(nextRoom)
            } else {
                state.battle
            }

            val userMultiplier = nextRoom.players.firstOrNull { it.isUser }?.multiplier
            val nextStats = if (userMultiplier != null) {
                val total = state.profile.stats.totalMultiplierAccumulated + userMultiplier
                val rounds = state.profile.stats.ballRoomsPlayed.coerceAtLeast(1)
                state.profile.stats.copy(
                    bestMultiplier = maxOf(state.profile.stats.bestMultiplier, userMultiplier),
                    totalMultiplierAccumulated = total,
                    averageMultiplier = total / rounds
                )
            } else {
                state.profile.stats
            }

            state.copy(
                ballRoom = nextRoom,
                battle = battle,
                activeTab = if (nextRoom.phase == BallRoomPhase.REVEALED) MainTab.BATTLE else state.activeTab,
                profile = state.profile.copy(stats = nextStats),
                transientMessage = message
            )
        }
    }

    fun chooseBattleAction(action: BattleCardType) {
        _uiState.update { state ->
            state.copy(battle = state.battle.copy(selectedAction = action))
        }
    }

    fun playBattleRound() {
        _uiState.update { state ->
            val (nextBattle, finishedMessage) = engine.playBattleRound(
                battle = state.battle,
                userAction = state.battle.selectedAction
            )

            var nextMarket = state.market
            var impactApplied = false
            var impactMessage: String? = finishedMessage

            if (nextBattle.phase == BattlePhase.FINISHED && nextBattle.winnerId != null && !nextBattle.impactApplied) {
                val multiplier = nextBattle.winningMultiplier ?: 1.0
                nextMarket = engine.applyWinnerImpact(
                    market = state.market,
                    tokenId = state.market.selectedToken,
                    multiplier = multiplier
                )
                impactApplied = true
                impactMessage =
                    "${nextBattle.winnerName} aplica x${engine.formatMultiplier(multiplier)} sobre ${state.market.selectedToken.name}."
            }

            val nextStats = if (nextBattle.phase == BattlePhase.FINISHED) {
                val isUserWinner = nextBattle.winnerId == GameRules.USER_PLAYER_ID
                state.profile.stats.copy(
                    battlesPlayed = state.profile.stats.battlesPlayed + 1,
                    battlesWon = state.profile.stats.battlesWon + if (isUserWinner) 1 else 0
                )
            } else {
                state.profile.stats
            }

            state.copy(
                market = nextMarket,
                battle = nextBattle.copy(impactApplied = impactApplied || nextBattle.impactApplied),
                profile = state.profile.copy(
                    stats = nextStats,
                    badges = recomputeBadges(nextStats)
                ),
                transientMessage = impactMessage
            )
        }
    }

    fun resetBattleAndRoom() {
        _uiState.update { state ->
            state.copy(
                ballRoom = BallRoomUiState(
                    phase = BallRoomPhase.WAITING_ENTRY,
                    statusMessage = "Paga EUR ${GameRules.BALL_ENTRY_COST.toInt()} para entrar en la sala."
                ),
                battle = BattleUiState(
                    phase = BattlePhase.LOCKED,
                    log = listOf("Nuevo ciclo listo. Vuelve al sorteo de bolas.")
                ),
                activeTab = MainTab.BALL_ROOM,
                transientMessage = "Battle cerrado. Preparate para una nueva sala."
            )
        }
    }

    fun claimRewardedAd() {
        val current = _uiState.value
        if (!current.rewardedAvailable || current.rewardedCooldownSec > 0) {
            _uiState.update { it.copy(transientMessage = "Rewarded no disponible todavia.") }
            return
        }

        val rewardAmount = 25.0
        _uiState.update { state ->
            val stats = state.profile.stats.copy(
                rewardedAdsClaimed = state.profile.stats.rewardedAdsClaimed + 1
            )
            state.copy(
                market = state.market.copy(cashBalance = state.market.cashBalance + rewardAmount),
                rewardedAvailable = false,
                rewardedCooldownSec = 30,
                profile = state.profile.copy(stats = stats, badges = recomputeBadges(stats)),
                transientMessage = "Rewarded completado: +EUR ${rewardAmount.toInt()}."
            )
        }

        rewardedCooldownJob?.cancel()
        rewardedCooldownJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _uiState.update { state ->
                    val remaining = (state.rewardedCooldownSec - 1).coerceAtLeast(0)
                    state.copy(
                        rewardedCooldownSec = remaining,
                        rewardedAvailable = remaining == 0
                    )
                }
                if (_uiState.value.rewardedCooldownSec == 0) break
            }
        }
    }

    fun consumeTransientMessage() {
        _uiState.update { it.copy(transientMessage = null) }
    }

    private fun initialState(): GameUiState {
        val market = engine.createInitialMarketState()
        val stats = ProfileStats()
        return GameUiState(
            activeTab = MainTab.DASHBOARD,
            market = market,
            ballRoom = BallRoomUiState(
                phase = BallRoomPhase.WAITING_ENTRY,
                statusMessage = "Paga EUR ${GameRules.BALL_ENTRY_COST.toInt()} para entrar en la sala."
            ),
            battle = BattleUiState(
                phase = BattlePhase.LOCKED,
                log = listOf("Completa primero el sorteo de bolas.")
            ),
            profile = ProfileUiState(
                playerName = "Jugador",
                badges = recomputeBadges(stats),
                stats = stats
            ),
            rewardedAvailable = true,
            rewardedCooldownSec = 0,
            transientMessage = "Mercado inicializado."
        )
    }

    private fun startClockAndMarketLoop() {
        viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val now = System.currentTimeMillis()
                _uiState.update { state ->
                    var market = engine.updateMarketCountdown(state.market, now)
                    val currentDay = engine.dayIndex(now)
                    var message: String? = state.transientMessage

                    if (market.lastResetDayIndex != currentDay) {
                        market = engine.applyDailyReset(market, now)
                        message = "Reset diario ejecutado: cartera liquidada y precios regenerados."
                    }

                    marketTickCounter += 1
                    if (marketTickCounter % 5 == 0) {
                        market = engine.simulateMarketTick(market)
                    }

                    state.copy(market = market, transientMessage = message)
                }
            }
        }
    }

    private fun recomputeBadges(stats: ProfileStats): List<BadgeUi> {
        val winRate = if (stats.battlesPlayed == 0) 0.0 else stats.battlesWon.toDouble() / stats.battlesPlayed
        return listOf(
            BadgeUi(
                title = "Primer Knockout",
                description = "Gana tu primera batalla.",
                unlocked = stats.battlesWon >= 1
            ),
            BadgeUi(
                title = "Sangre Fria",
                description = "Consigue multiplicador x10 o superior.",
                unlocked = stats.bestMultiplier >= 10.0
            ),
            BadgeUi(
                title = "Trader Diario",
                description = "Juega 5 salas de bolas.",
                unlocked = stats.ballRoomsPlayed >= 5
            ),
            BadgeUi(
                title = "Maestro Royale",
                description = "Mantiene winrate del 50% con 6 batallas.",
                unlocked = stats.battlesPlayed >= 6 && winRate >= 0.5
            ),
            BadgeUi(
                title = "Bonus Hunter",
                description = "Reclama 3 rewarded ads.",
                unlocked = stats.rewardedAdsClaimed >= 3
            )
        )
    }
}
