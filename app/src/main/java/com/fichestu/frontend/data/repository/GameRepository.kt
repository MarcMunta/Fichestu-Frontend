package com.fichestu.frontend.data.repository

import com.fichestu.frontend.data.i18n.AppI18n
import com.fichestu.frontend.data.model.BallOptionDto
import com.fichestu.frontend.data.model.BallPlayerDto
import com.fichestu.frontend.data.model.BallRoomDto
import com.fichestu.frontend.data.model.BattleDto
import com.fichestu.frontend.data.model.BattlePlayerDto
import com.fichestu.frontend.data.model.BattleRoundRequestDto
import com.fichestu.frontend.data.model.CooldownResponseDto
import com.fichestu.frontend.data.model.EnterBallRoomResponseDto
import com.fichestu.frontend.data.model.GameBootstrapResponse
import com.fichestu.frontend.data.model.GameMarketResponseDto
import com.fichestu.frontend.data.model.GameTokenDto
import com.fichestu.frontend.data.model.GameTradeRequest
import com.fichestu.frontend.data.model.MatchStateResponseDto
import com.fichestu.frontend.data.model.NotificationDto
import com.fichestu.frontend.data.model.NotificationListResponseDto
import com.fichestu.frontend.data.model.PickBallRequestDto
import com.fichestu.frontend.data.model.ProfileStatsDto
import com.fichestu.frontend.data.model.WalletResponseDto
import com.fichestu.frontend.data.model.WinnerImpactRequestDto
import com.fichestu.frontend.data.remote.ApiClient
import com.fichestu.frontend.game.GameRules
import com.fichestu.frontend.game.model.BadgeUi
import com.fichestu.frontend.game.model.BallOption
import com.fichestu.frontend.game.model.BallPlayer
import com.fichestu.frontend.game.model.BallRoomPhase
import com.fichestu.frontend.game.model.BallRoomUiState
import com.fichestu.frontend.game.model.BattleCardType
import com.fichestu.frontend.game.model.BattlePhase
import com.fichestu.frontend.game.model.BattlePlayer
import com.fichestu.frontend.game.model.BattleUiState
import com.fichestu.frontend.game.model.GameUiState
import com.fichestu.frontend.game.model.MainTab
import com.fichestu.frontend.game.model.MarketToken
import com.fichestu.frontend.game.model.MarketUiState
import com.fichestu.frontend.game.model.NotificationUi
import com.fichestu.frontend.game.model.ProfileStats
import com.fichestu.frontend.game.model.ProfileUiState
import com.fichestu.frontend.game.model.TokenId
import java.io.IOException

class GameRepository {

    suspend fun loadNotifications(currentState: GameUiState): Result<GameUiState> = runSafely {
        val response = ApiClient.notificationApi.list(requireAuth())
        val dto = parseResponse(response.isSuccessful, response.body(), response.errorBody()?.string(), response.code())
        currentState.withNotifications(dto)
    }

    suspend fun markNotificationsRead(currentState: GameUiState): Result<GameUiState> = runSafely {
        val response = ApiClient.notificationApi.markAllRead(requireAuth())
        parseResponse(response.isSuccessful, response.body(), response.errorBody()?.string(), response.code())
        val refreshed = ApiClient.notificationApi.list(requireAuth())
        val dto = parseResponse(refreshed.isSuccessful, refreshed.body(), refreshed.errorBody()?.string(), refreshed.code())
        currentState.withNotifications(dto)
    }

    suspend fun clearNotifications(currentState: GameUiState): Result<GameUiState> = runSafely {
        val response = ApiClient.notificationApi.clear(requireAuth())
        parseResponse(response.isSuccessful, response.body(), response.errorBody()?.string(), response.code())
        currentState.copy(notifications = emptyList(), unreadNotificationCount = 0)
    }

    suspend fun bootstrap(currentState: GameUiState?): Result<GameUiState> = runSafely {
        val auth = requireAuth()
        val response = ApiClient.gameApi.bootstrap(auth)
        val dto = parseResponse(response.isSuccessful, response.body(), response.errorBody()?.string(), response.code())
        mapBootstrap(dto, currentState)
    }

    suspend fun refreshMatch(currentState: GameUiState): Result<GameUiState> = runSafely {
        val auth = requireAuth()
        val response = ApiClient.gameApi.currentMatchState(auth)
        val dto = parseResponse(response.isSuccessful, response.body(), response.errorBody()?.string(), response.code())
        val nextBattle = mapBattle(dto.battle)
        val nextBallRoom = mapBallRoom(dto.ballRoom, currentState.ballRoom.pendingSelectedBallId)
        val matchmakingDeadlineNotReached = currentState.ballRoom.selectionDeadlineEpochMs
            ?.let { it > System.currentTimeMillis() + 250L } == true
        val shouldHoldMatchmakingUntilLocalDeadline =
            currentState.ballRoom.phase == BallRoomPhase.MATCHMAKING &&
                nextBallRoom.phase == BallRoomPhase.PICKING &&
                matchmakingDeadlineNotReached &&
                currentState.ballRoom.players.size < GameRules.ROOM_SIZE
        val stableBallRoom = if (
            currentState.ballRoom.phase == BallRoomPhase.PICKING &&
            nextBallRoom.phase == BallRoomPhase.WAITING_ENTRY
        ) {
            nextBallRoom.copy(phase = BallRoomPhase.PICKING)
        } else if (shouldHoldMatchmakingUntilLocalDeadline) {
            currentState.ballRoom.copy(
                statusMessage = "Preparando seleccion de bolas..."
            )
        } else {
            nextBallRoom
        }
        currentState.copy(
            currentMatchId = dto.matchId,
            activeTab = if (
                currentState.activeTab == MainTab.BALL_ROOM &&
                (nextBattle.phase == BattlePhase.READY || nextBattle.phase == BattlePhase.IN_PROGRESS)
            ) {
                MainTab.BATTLE
            } else {
                currentState.activeTab
            },
            ballRoom = stableBallRoom,
            battle = nextBattle,
            transientMessage = currentState.transientMessage
        ).exitBattleIfUserEliminated()
    }

    suspend fun buy(currentState: GameUiState): Result<GameUiState> = runSafely {
        val selected = currentState.market.selectedToken
        val response = ApiClient.marketApi.buy(
            authorization = requireAuth(),
            request = GameTradeRequest(token = selected.name, tokenId = selected.toTokenIdNumber(), quantity = 1)
        )
        val dto = parseResponse(response.isSuccessful, response.body(), response.errorBody()?.string(), response.code())
        currentState.copy(market = mapMarket(dto, selected), transientMessage = dto.message)
    }

    suspend fun sell(currentState: GameUiState): Result<GameUiState> = runSafely {
        val selected = currentState.market.selectedToken
        val response = ApiClient.marketApi.sell(
            authorization = requireAuth(),
            request = GameTradeRequest(token = selected.name, tokenId = selected.toTokenIdNumber(), quantity = 1)
        )
        val dto = parseResponse(response.isSuccessful, response.body(), response.errorBody()?.string(), response.code())
        currentState.copy(market = mapMarket(dto, selected), transientMessage = dto.message)
    }

    suspend fun enterBallRoom(currentState: GameUiState): Result<GameUiState> = runSafely {
        val response = ApiClient.gameApi.enterBallRoom(requireAuth())
        val dto = parseResponse(response.isSuccessful, response.body(), response.errorBody()?.string(), response.code())
        currentState.copy(
            currentMatchId = dto.matchId,
            activeTab = MainTab.BALL_ROOM,
            market = currentState.market.copy(cashBalance = dto.cashBalance),
            ballRoom = mapBallRoom(dto.ballRoom),
            battle = BattleUiState(phase = BattlePhase.LOCKED, log = listOf("Completa el sorteo de bolas para desbloquear el Battle Royale.")),
            profile = currentState.profile.copy(stats = currentState.profile.stats.copy(ballRoomsPlayed = currentState.profile.stats.ballRoomsPlayed + 1)),
            transientMessage = dto.message
        )
    }

    suspend fun cancelMatchmaking(currentState: GameUiState, matchId: Int): Result<GameUiState> = runSafely {
        val response = ApiClient.gameApi.cancelMatchmaking(requireAuth(), matchId)
        val dto = parseResponse(response.isSuccessful, response.body(), response.errorBody()?.string(), response.code())
        currentState.copy(
            currentMatchId = null,
            activeTab = MainTab.BALL_ROOM,
            market = currentState.market.copy(cashBalance = dto.cashBalance),
            ballRoom = mapBallRoom(dto.ballRoom),
            battle = BattleUiState(
                phase = BattlePhase.LOCKED,
                log = listOf("Matchmaking cancelado")
            ),
            transientMessage = dto.message
        )
    }

    suspend fun abandonMatch(currentState: GameUiState, matchId: Int): Result<GameUiState> = runSafely {
        val response = ApiClient.gameApi.abandonMatch(requireAuth(), matchId)
        val dto = parseResponse(response.isSuccessful, response.body(), response.errorBody()?.string(), response.code())
        currentState.copy(
            currentMatchId = null,
            activeTab = MainTab.BALL_ROOM,
            market = currentState.market.copy(cashBalance = dto.cashBalance),
            ballRoom = mapBallRoom(dto.ballRoom),
            battle = BattleUiState(
                phase = BattlePhase.LOCKED,
                log = listOf("Partida abandonada. Un bot ocupa tu plaza si la sala ya habia avanzado.")
            ),
            transientMessage = dto.message
        )
    }

    suspend fun pickBall(currentState: GameUiState, matchId: Int, ballId: Int): Result<GameUiState> = runSafely {
        val response = ApiClient.gameApi.pickBall(requireAuth(), matchId, PickBallRequestDto(ballId))
        val dto = parseResponse(response.isSuccessful, response.body(), response.errorBody()?.string(), response.code())
        currentState.copy(
            currentMatchId = dto.matchId,
            ballRoom = mapBallRoom(dto.ballRoom),
            battle = mapBattle(dto.battle),
            transientMessage = dto.message
        )
    }

    suspend fun revealMultipliers(currentState: GameUiState, matchId: Int): Result<GameUiState> = runSafely {
        val response = ApiClient.gameApi.revealMultipliers(requireAuth(), matchId)
        val dto = parseResponse(response.isSuccessful, response.body(), response.errorBody()?.string(), response.code())

        val me = dto.ballRoom.players.firstOrNull { it.isUser }
        val stats = if (me?.multiplier != null) {
            val total = currentState.profile.stats.totalMultiplierAccumulated + me.multiplier
            val rounds = (currentState.profile.stats.ballRoomsPlayed).coerceAtLeast(1)
            currentState.profile.stats.copy(
                bestMultiplier = maxOf(currentState.profile.stats.bestMultiplier, me.multiplier),
                totalMultiplierAccumulated = total,
                averageMultiplier = total / rounds
            )
        } else {
            currentState.profile.stats
        }

        currentState.copy(
            currentMatchId = dto.matchId,
            activeTab = MainTab.BALL_ROOM,
            ballRoom = mapBallRoom(dto.ballRoom),
            battle = mapBattle(dto.battle),
            profile = currentState.profile.copy(stats = stats, badges = recomputeBadges(stats)),
            transientMessage = dto.message
        )
    }

    suspend fun playBattleRound(currentState: GameUiState, matchId: Int): Result<GameUiState> = runSafely {
        val selectedAction = currentState.battle.selectedAction
        val selectedPower = currentState.battle.hand
            .firstOrNull { it.id == currentState.battle.selectedCardId }
            ?.power
        val response = ApiClient.gameApi.submitBattleAction(
            authorization = requireAuth(),
            matchId = matchId,
            request = BattleRoundRequestDto(
                action = selectedAction.name,
                cardPower = selectedPower,
                targetUserId = currentState.battle.selectedTargetId?.toIntOrNull()
            )
        )
        val dto = parseResponse(response.isSuccessful, response.body(), response.errorBody()?.string(), response.code())

        val nextBattle = mapBattle(dto.battle).copy(selectedAction = selectedAction)
        val nextStats = if (nextBattle.phase == BattlePhase.FINISHED) {
            val isUserWinner = nextBattle.winnerId == "user"
            currentState.profile.stats.copy(
                battlesPlayed = currentState.profile.stats.battlesPlayed + 1,
                battlesWon = currentState.profile.stats.battlesWon + if (isUserWinner) 1 else 0
            )
        } else {
            currentState.profile.stats
        }

        currentState.copy(
            currentMatchId = dto.matchId,
            battle = nextBattle,
            ballRoom = mapBallRoom(dto.ballRoom),
            profile = currentState.profile.copy(stats = nextStats, badges = recomputeBadges(nextStats)),
            transientMessage = dto.message
        ).exitBattleIfUserEliminated()
    }

    suspend fun applyWinnerImpact(currentState: GameUiState, matchId: Int): Result<GameUiState> = runSafely {
        val token = currentState.market.selectedToken
        val response = ApiClient.gameApi.applyWinnerImpact(
            authorization = requireAuth(),
            matchId = matchId,
            request = WinnerImpactRequestDto(token.name)
        )
        val dto = parseResponse(response.isSuccessful, response.body(), response.errorBody()?.string(), response.code())
        currentState.copy(
            currentMatchId = dto.matchId,
            battle = mapBattle(dto.battle),
            ballRoom = mapBallRoom(dto.ballRoom),
            transientMessage = dto.message
        )
    }

    suspend fun claimRewarded(currentState: GameUiState): Result<GameUiState> = runSafely {
        val response = ApiClient.marketApi.claimReward(requireAuth())
        val dto = parseResponse(response.isSuccessful, response.body(), response.errorBody()?.string(), response.code())
        val stats = currentState.profile.stats.copy(rewardedAdsClaimed = dto.rewardedAdsClaimed)

        currentState.copy(
            market = currentState.market.copy(cashBalance = dto.cashBalance),
            rewardedAvailable = false,
            rewardedCooldownSec = dto.rewardedCooldownSec,
            profile = currentState.profile.copy(stats = stats, badges = recomputeBadges(stats)),
            transientMessage = dto.message
        )
    }

    suspend fun closeMatch(matchId: Int): Result<Unit> = runSafely {
        val response = ApiClient.gameApi.closeMatch(requireAuth(), matchId)
        if (response.code() == 401 || response.code() == 403) {
            SessionStore.clear()
            throw SessionExpiredException()
        }
        if (!response.isSuccessful) {
            throw Exception(extractMessage(response.errorBody()?.string()) ?: "No se pudo cerrar el match")
        }
        Unit
    }

    private fun mapBootstrap(dto: GameBootstrapResponse, currentState: GameUiState?): GameUiState {
        val selected = currentState?.market?.selectedToken ?: TokenId.ROJA
        val stats = mapStats(dto.stats)
        return GameUiState(
            currentMatchId = currentState?.currentMatchId,
            activeTab = currentState?.activeTab ?: MainTab.DASHBOARD,
            market = MarketUiState(
                selectedToken = selected,
                tokens = dto.tokens.map { it.toMarketToken() },
                cashBalance = dto.cashBalance,
                lastResetDayIndex = currentState?.market?.lastResetDayIndex ?: 0L,
                resetCountdownLabel = currentState?.market?.resetCountdownLabel ?: "--:--:--"
            ),
            ballRoom = currentState?.ballRoom ?: BallRoomUiState(),
            battle = currentState?.battle ?: BattleUiState(),
            profile = ProfileUiState(
                playerName = dto.playerName,
                badges = dto.badges?.map { it.toBadgeUi() } ?: recomputeBadges(stats),
                stats = stats
            ),
            rewardedAvailable = dto.rewardedCooldownSec == 0,
            rewardedCooldownSec = dto.rewardedCooldownSec,
            transientMessage = currentState?.transientMessage ?: dto.message
        )
    }

    private fun mapMarket(dto: WalletResponseDto, selected: TokenId): MarketUiState {
        return MarketUiState(
            selectedToken = selected,
            tokens = dto.tokens.map { it.toMarketToken() },
            cashBalance = dto.cashBalance,
            lastResetDayIndex = 0L,
            resetCountdownLabel = "--:--:--"
        )
    }

    private fun mapMarket(dto: GameMarketResponseDto, selected: TokenId): MarketUiState {
        return MarketUiState(
            selectedToken = selected,
            tokens = dto.tokens.map { it.toMarketToken() },
            cashBalance = dto.cashBalance,
            lastResetDayIndex = 0L,
            resetCountdownLabel = "--:--:--"
        )
    }

    private fun mapBallRoom(
        dto: BallRoomDto,
        pendingSelectedBallId: Int? = null
    ): BallRoomUiState {
        val players = dto.players.map { it.toBallPlayer() }
        val localDeadlineEpochMs = dto.selectionDeadlineEpochMs?.let { deadline ->
            val serverNow = dto.serverNowEpochMs
            if (serverNow != null) {
                System.currentTimeMillis() + (deadline - serverNow).coerceAtLeast(0L)
            } else {
                deadline
            }
        }
        val userIds = players.filter { it.isUser }.map { it.id }.toSet()
        val userPicked = players.any { it.isUser && it.selectedBallId != null }
        val pendingAvailable = pendingSelectedBallId?.let { pending: Int ->
            dto.balls.any { it.id == pending && it.pickedBy == null }
        } == true
        return BallRoomUiState(
            phase = dto.phase.toBallRoomPhase(),
            players = players,
            balls = dto.balls.map { it.toBallOption(userIds) },
            statusMessage = dto.statusMessage,
            canRevealBattle = dto.canRevealBattle,
            selectionDeadlineEpochMs = localDeadlineEpochMs,
            pendingSelectedBallId = if (!userPicked && pendingAvailable) pendingSelectedBallId else null
        )
    }

    private fun mapBattle(dto: BattleDto): BattleUiState {
        val players = dto.players.map { it.toBattlePlayer() }
        val winnerId = players.firstOrNull { it.id == dto.winnerId }?.let { if (it.isUser) "user" else it.id }
        return BattleUiState(
            phase = dto.phase.toBattlePhase(),
            players = players,
            round = dto.round,
            log = dto.log,
            winnerId = winnerId,
            winnerName = dto.winnerName,
            winningMultiplier = dto.winningMultiplier,
            selectedAction = dto.selectedAction.toBattleCardType(),
            impactApplied = false,
            interstitialAvailable = dto.interstitialAvailable
        )
    }

    private fun mapStats(dto: ProfileStatsDto): ProfileStats {
        return ProfileStats(
            ballRoomsPlayed = dto.ballRoomsPlayed,
            battlesPlayed = dto.battlesPlayed,
            battlesWon = dto.battlesWon,
            bestMultiplier = dto.bestMultiplier,
            averageMultiplier = dto.averageMultiplier,
            rewardedAdsClaimed = dto.rewardedAdsClaimed,
            totalMultiplierAccumulated = dto.averageMultiplier * dto.ballRoomsPlayed
        )
    }

    private fun GameUiState.withNotifications(dto: NotificationListResponseDto): GameUiState {
        return copy(
            notifications = dto.notifications.map { it.toNotificationUi() },
            unreadNotificationCount = dto.unreadCount
        )
    }

    private fun GameUiState.exitBattleIfUserEliminated(): GameUiState {
        val me = battle.players.firstOrNull { it.isUser } ?: return this
        if (currentMatchId == null || me.isAlive || battle.phase == BattlePhase.LOCKED || battle.phase == BattlePhase.DEFEATED) {
            return this
        }

        val aliveRivals = battle.players.count { !it.isUser && it.isAlive }
        val placement = (aliveRivals + 1).coerceAtLeast(2)
        return copy(
            currentMatchId = null,
            activeTab = MainTab.BATTLE,
            ballRoom = BallRoomUiState(
                phase = BallRoomPhase.WAITING_ENTRY,
                statusMessage = "Paga ${GameRules.BALL_ENTRY_COST.toInt()} FTC para entrar en la sala."
            ),
            battle = battle.copy(
                phase = BattlePhase.DEFEATED,
                placement = placement,
                hand = emptyList(),
                selectedCardId = null,
                selectedTargetId = null,
                log = battle.log + "Has quedado en posicion #$placement."
            ),
            transientMessage = "Eliminado. Pulsa para volver a la entrada."
        )
    }

    private fun NotificationDto.toNotificationUi(): NotificationUi {
        return NotificationUi(
            id = id,
            title = title,
            message = message,
            type = type,
            read = read,
            createdAt = createdAt
        )
    }

    private fun GameTokenDto.toMarketToken(): MarketToken {
        return MarketToken(
            id = tokenId.toTokenId(),
            displayName = name,
            ticker = ticker,
            currentPrice = currentPrice,
            previousPrice = previousPrice,
            holdings = holdings.toInt(),
            history = history
        )
    }

    private fun BallPlayerDto.toBallPlayer(): BallPlayer = BallPlayer(
        id = id,
        nickname = nickname,
        isUser = isUser,
        selectedBallId = selectedBallId,
        multiplier = multiplier?.toSafeMultiplier()
    )

    private fun BallOptionDto.toBallOption(userIds: Set<String>): BallOption = BallOption(
        id = id,
        multiplier = multiplier.toSafeMultiplier(),
        pickedBy = if (pickedBy != null && userIds.contains(pickedBy)) "user" else pickedBy
    )

    private fun BattlePlayerDto.toBattlePlayer(): BattlePlayer = BattlePlayer(
        id = if (isUser) "user" else id,
        nickname = nickname,
        isUser = isUser,
        hp = hp,
        multiplier = multiplier.toSafeMultiplier()
    )

    private fun Double?.toSafeMultiplier(): Double {
        return (this ?: 1.0).coerceIn(GameRules.MULTIPLIER_MIN, GameRules.MULTIPLIER_MAX)
    }

    private fun Int.toTokenId(): TokenId = when (this) {
        1 -> TokenId.ROJA
        2 -> TokenId.AZUL
        3 -> TokenId.VERDE
        4 -> TokenId.DORADA
        else -> TokenId.ROJA
    }

    private fun TokenId.toTokenIdNumber(): Int = when (this) {
        TokenId.ROJA -> 1
        TokenId.AZUL -> 2
        TokenId.VERDE -> 3
        TokenId.DORADA -> 4
    }

    private fun String.toBallRoomPhase(): BallRoomPhase = when (this.uppercase()) {
        "MATCHMAKING", "WAITING_PLAYERS" -> BallRoomPhase.MATCHMAKING
        "PICKING", "READY_REVEAL" -> BallRoomPhase.PICKING
        "REVEALED" -> BallRoomPhase.REVEALED
        "READY_FOR_BATTLE" -> BallRoomPhase.READY_FOR_BATTLE
        else -> BallRoomPhase.WAITING_ENTRY
    }

    private fun String.toBattlePhase(): BattlePhase = when (this.uppercase()) {
        "READY" -> BattlePhase.READY
        "IN_PROGRESS" -> BattlePhase.IN_PROGRESS
        "DEFEATED", "ELIMINATED" -> BattlePhase.DEFEATED
        "FINISHED", "CLOSED" -> BattlePhase.FINISHED
        else -> BattlePhase.LOCKED
    }

    private fun String.toBattleCardType(): BattleCardType = when (this.uppercase()) {
        "SHIELD" -> BattleCardType.SHIELD
        "REBOUND" -> BattleCardType.REBOUND
        else -> BattleCardType.ATTACK
    }

    private fun recomputeBadges(stats: ProfileStats): List<BadgeUi> {
        val winRate = if (stats.battlesPlayed == 0) 0.0 else stats.battlesWon.toDouble() / stats.battlesPlayed
        return listOf(
            BadgeUi("Primer Knockout", "Gana tu primera batalla.", stats.battlesWon >= 1),
            BadgeUi("Sangre Fria", "Consigue multiplicador x3 o superior.", stats.bestMultiplier >= 3.0),
            BadgeUi("Trader Diario", "Juega 5 salas de bolas.", stats.ballRoomsPlayed >= 5),
            BadgeUi("Maestro Royale", "Mantiene winrate del 50% con 6 batallas.", stats.battlesPlayed >= 6 && winRate >= 0.5),
            BadgeUi("Bonus Hunter", "Reclama 3 rewarded ads.", stats.rewardedAdsClaimed >= 3)
        )
    }

    private fun com.fichestu.frontend.data.model.BadgeDto.toBadgeUi(): BadgeUi {
        return BadgeUi(
            title = title,
            description = description,
            unlocked = unlocked
        )
    }

    private fun requireAuth(): String {
        return SessionStore.authHeaderOrNull() ?: throw SessionExpiredException()
    }

    private fun <T> parseResponse(success: Boolean, body: T?, errorRaw: String?, code: Int): T {
        if (code == 401 || code == 403) {
            SessionStore.clear()
            throw SessionExpiredException()
        }
        if (success && body != null) return body
        throw Exception(AppI18n.message(extractMessage(errorRaw)) ?: AppI18n.text("server_error"))
    }

    private fun extractMessage(rawError: String?): String? {
        if (rawError.isNullOrBlank()) return null
        return runCatching {
            com.google.gson.JsonParser.parseString(rawError).asJsonObject.get("message")?.asString
        }.getOrNull()
    }

    private suspend fun <T> runSafely(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: IOException) {
            Result.failure(Exception("Error de red: verifica conexión y backend activo."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
