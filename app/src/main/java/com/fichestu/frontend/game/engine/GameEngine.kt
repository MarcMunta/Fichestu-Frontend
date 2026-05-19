package com.fichestu.frontend.game.engine

import com.fichestu.frontend.game.GameRules
import com.fichestu.frontend.game.model.BallOption
import com.fichestu.frontend.game.model.BallPlayer
import com.fichestu.frontend.game.model.BallRoomPhase
import com.fichestu.frontend.game.model.BallRoomUiState
import com.fichestu.frontend.game.model.BattleCardType
import com.fichestu.frontend.game.model.BattlePhase
import com.fichestu.frontend.game.model.BattlePlayer
import com.fichestu.frontend.game.model.BattleUiState
import com.fichestu.frontend.game.model.MarketToken
import com.fichestu.frontend.game.model.MarketUiState
import com.fichestu.frontend.game.model.TokenId
import java.util.Calendar
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

class GameEngine(
    private val random: Random = Random(System.currentTimeMillis())
) {

    fun createInitialMarketState(nowMillis: Long = System.currentTimeMillis()): MarketUiState {
        val seedTokens = listOf(
            Triple(TokenId.ROJA, "Ficha Roja", "FRO"),
            Triple(TokenId.AZUL, "Ficha Azul", "FAZ"),
            Triple(TokenId.VERDE, "Ficha Verde", "FVD"),
            Triple(TokenId.DORADA, "Ficha Dorada", "FGD")
        )

        val tokens = seedTokens.mapIndexed { index, seed ->
            val base = randomPrice()
            val history = generateHistory(base)
            val latest = history.lastOrNull() ?: base
            val prev = history.getOrNull(history.lastIndex - 1) ?: latest
            MarketToken(
                id = seed.first,
                displayName = seed.second,
                ticker = seed.third,
                currentPrice = latest,
                previousPrice = prev,
                holdings = if (index == 0) 8.0 else random.nextInt(1, 6).toDouble(),
                history = history
            )
        }

        return MarketUiState(
            selectedToken = TokenId.ROJA,
            tokens = tokens,
            cashBalance = 320.0,
            lastResetDayIndex = dayIndex(nowMillis),
            resetCountdownLabel = countdownToMidnightLabel(nowMillis)
        )
    }

    fun updateMarketCountdown(market: MarketUiState, nowMillis: Long): MarketUiState {
        return market.copy(resetCountdownLabel = countdownToMidnightLabel(nowMillis))
    }

    fun dayIndex(nowMillis: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val year = cal.get(Calendar.YEAR)
        val day = cal.get(Calendar.DAY_OF_YEAR)
        return year * 1000L + day
    }

    fun applyDailyReset(market: MarketUiState, nowMillis: Long): MarketUiState {
        val liquidated = round2(market.totalBalance)
        val resetTokens = market.tokens.map { token ->
            val base = randomPrice()
            val seeded = generateHistory(base)
            val latest = seeded.lastOrNull() ?: base
            token.copy(
                currentPrice = latest,
                previousPrice = latest,
                holdings = 0.0,
                history = seeded
            )
        }

        return market.copy(
            tokens = resetTokens,
            cashBalance = liquidated,
            lastResetDayIndex = dayIndex(nowMillis),
            resetCountdownLabel = countdownToMidnightLabel(nowMillis)
        )
    }

    fun selectToken(market: MarketUiState, tokenId: TokenId): MarketUiState {
        return market.copy(selectedToken = tokenId)
    }

    fun simulateMarketTick(market: MarketUiState): MarketUiState {
        val evolved = market.tokens.map { token ->
            val factor = random.nextDouble(from = 0.95, until = 1.08)
            val newPrice = round2(max(0.5, token.currentPrice * factor))
            token.copy(
                previousPrice = token.currentPrice,
                currentPrice = newPrice,
                history = (token.history + newPrice).takeLast(GameRules.HISTORY_POINTS)
            )
        }
        return market.copy(tokens = evolved)
    }

    fun buyOneSelected(market: MarketUiState): Pair<MarketUiState, String> {
        val selected = market.selectedMarketToken
            ?: return market to "Selecciona una ficha para comprar."

        if (market.cashBalance < selected.currentPrice) {
            return market to "Saldo insuficiente para comprar ${selected.ticker}."
        }

        val updatedTokens = market.tokens.map { token ->
            if (token.id == selected.id) token.copy(holdings = token.holdings + 1) else token
        }

        return market.copy(
            tokens = updatedTokens,
            cashBalance = round2(market.cashBalance - selected.currentPrice)
        ) to "Compraste 1 ${selected.ticker}."
    }

    fun sellOneSelected(market: MarketUiState): Pair<MarketUiState, String> {
        val selected = market.selectedMarketToken
            ?: return market to "Selecciona una ficha para vender."

        if (selected.holdings <= 0) {
            return market to "No tienes unidades de ${selected.ticker} para vender."
        }

        val updatedTokens = market.tokens.map { token ->
            if (token.id == selected.id) token.copy(holdings = token.holdings - 1) else token
        }

        return market.copy(
            tokens = updatedTokens,
            cashBalance = round2(market.cashBalance + selected.currentPrice)
        ) to "Vendiste 1 ${selected.ticker}."
    }

    fun createBallRoom(): BallRoomUiState {
        val players = buildPlayers()
        val balls = (1..GameRules.BALL_COUNT).map { ballId ->
            BallOption(
                id = ballId,
                multiplier = randomMultiplier(),
                pickedBy = null
            )
        }

        return BallRoomUiState(
            phase = BallRoomPhase.PICKING,
            players = players,
            balls = balls,
            statusMessage = "Elige una bola. Cada jugador solo puede tomar una.",
            canRevealBattle = false
        )
    }

    fun pickUserBall(ballRoom: BallRoomUiState, ballId: Int): Pair<BallRoomUiState, String> {
        if (ballRoom.phase != BallRoomPhase.PICKING) {
            return ballRoom to "La sala no está en fase de selección."
        }

        val userPlayer = ballRoom.players.firstOrNull { it.id == GameRules.USER_PLAYER_ID }
            ?: return ballRoom to "No se encontró al jugador."

        if (userPlayer.selectedBallId != null) {
            return ballRoom to "Ya has elegido una bola."
        }

        val selectedBall = ballRoom.balls.firstOrNull { it.id == ballId }
            ?: return ballRoom to "Bola inválida."

        if (selectedBall.isPicked) {
            return ballRoom to "Esa bola ya fue tomada."
        }

        var room = ballRoom

        room = room.copy(
            balls = room.balls.map { ball ->
                if (ball.id == ballId) ball.copy(pickedBy = GameRules.USER_PLAYER_ID) else ball
            },
            players = room.players.map { player ->
                if (player.id == GameRules.USER_PLAYER_ID) player.copy(selectedBallId = ballId) else player
            }
        )

        room = autoPickBots(room)

        val everyonePicked = room.players.all { it.selectedBallId != null }
        return if (everyonePicked) {
            room.copy(
                canRevealBattle = true,
                statusMessage = "Todos eligieron. Revela multiplicadores para iniciar Battle Royale."
            ) to "Bola $ballId seleccionada."
        } else {
            room to "Esperando al resto de jugadores..."
        }
    }

    fun revealMultipliers(ballRoom: BallRoomUiState): Pair<BallRoomUiState, String> {
        if (!ballRoom.players.all { it.selectedBallId != null }) {
            return ballRoom to "Aún faltan jugadores por elegir bola."
        }

        val multiplierMap = ballRoom.balls.associateBy({ it.id }, { it.multiplier })
        val updatedPlayers = ballRoom.players.map { player ->
            player.copy(multiplier = multiplierMap[player.selectedBallId])
        }

        val userMultiplier = updatedPlayers.firstOrNull { it.isUser }?.multiplier ?: 1.0

        return ballRoom.copy(
            phase = BallRoomPhase.REVEALED,
            players = updatedPlayers,
            canRevealBattle = true,
            statusMessage = "Tu multiplicador es x${formatMultiplier(userMultiplier)}. Pasa al Battle Royale."
        ) to "Multiplicadores revelados."
    }

    fun createBattle(ballRoom: BallRoomUiState): BattleUiState {
        val players = ballRoom.players.map { roomPlayer ->
            BattlePlayer(
                id = roomPlayer.id,
                nickname = roomPlayer.nickname,
                isUser = roomPlayer.isUser,
                hp = GameRules.BATTLE_INITIAL_HP,
                multiplier = roomPlayer.multiplier ?: 1.0,
                shieldActive = false,
                reboundActive = false
            )
        }

        return BattleUiState(
            phase = BattlePhase.READY,
            players = players,
            round = 0,
            log = listOf("Battle listo: ${players.size} jugadores, ${GameRules.BATTLE_INITIAL_HP} HP iniciales."),
            winnerId = null,
            winnerName = null,
            winningMultiplier = null,
            selectedAction = BattleCardType.ATTACK,
            interstitialAvailable = true
        )
    }

    fun playBattleRound(
        battle: BattleUiState,
        userAction: BattleCardType
    ): Pair<BattleUiState, String?> {
        if (battle.phase == BattlePhase.LOCKED || battle.phase == BattlePhase.DEFEATED || battle.phase == BattlePhase.FINISHED) {
            return battle to null
        }

        val actionByPlayer = mutableMapOf<String, BattleCardType>()
        val alivePlayers = battle.players.filter { it.isAlive }

        alivePlayers.forEach { player ->
            actionByPlayer[player.id] = if (player.isUser) userAction else randomBattleCard()
        }

        val prepared = battle.players.map { player ->
            if (!player.isAlive) {
                player.copy(shieldActive = false, reboundActive = false)
            } else {
                when (actionByPlayer[player.id]) {
                    BattleCardType.SHIELD -> player.copy(shieldActive = true, reboundActive = false)
                    BattleCardType.REBOUND -> player.copy(shieldActive = false, reboundActive = true)
                    else -> player.copy(shieldActive = false, reboundActive = false)
                }
            }
        }

        val hpByPlayer = prepared.associate { it.id to it.hp }.toMutableMap()
        val roundLogs = mutableListOf<String>()

        prepared.filter { it.isAlive && actionByPlayer[it.id] == BattleCardType.ATTACK }.forEach { attacker ->
            val validTargets = prepared
                .filter { it.id != attacker.id && (hpByPlayer[it.id] ?: 0) > 0 }

            if (validTargets.isEmpty()) return@forEach

            val target = validTargets[random.nextInt(validTargets.size)]
            val damage = random.nextInt(
                from = GameRules.BATTLE_ATTACK_MIN,
                until = GameRules.BATTLE_ATTACK_MAX + 1
            )

            when {
                target.shieldActive -> {
                    roundLogs += "${attacker.nickname} ataca ${target.nickname} ($damage) pero el escudo bloquea."
                }

                target.reboundActive -> {
                    val newHp = max(0, (hpByPlayer[attacker.id] ?: attacker.hp) - damage)
                    hpByPlayer[attacker.id] = newHp
                    roundLogs += "${target.nickname} rebota $damage a ${attacker.nickname}."
                }

                else -> {
                    val newHp = max(0, (hpByPlayer[target.id] ?: target.hp) - damage)
                    hpByPlayer[target.id] = newHp
                    roundLogs += "${attacker.nickname} golpea ${target.nickname} por $damage."
                }
            }
        }

        val resolvedPlayers = prepared.map { player ->
            player.copy(
                hp = max(0, hpByPlayer[player.id] ?: player.hp),
                shieldActive = false,
                reboundActive = false
            )
        }

        val aliveAfterRound = resolvedPlayers.filter { it.isAlive }
        val winner = aliveAfterRound.singleOrNull()
        val nextPhase = if (winner != null || aliveAfterRound.isEmpty()) {
            BattlePhase.FINISHED
        } else {
            BattlePhase.IN_PROGRESS
        }

        val finishedMessage = when {
            winner != null -> "Ganador: ${winner.nickname} con x${formatMultiplier(winner.multiplier)}"
            aliveAfterRound.isEmpty() -> "Empate total: nadie sobrevive."
            else -> null
        }

        val finalLogs = if (finishedMessage != null) {
            roundLogs + finishedMessage
        } else {
            roundLogs
        }

        val updated = battle.copy(
            phase = nextPhase,
            players = resolvedPlayers,
            round = battle.round + 1,
            log = (battle.log + finalLogs).takeLast(24),
            winnerId = winner?.id,
            winnerName = winner?.nickname,
            winningMultiplier = winner?.multiplier,
            selectedAction = userAction,
            interstitialAvailable = false
        )

        return updated to finishedMessage
    }

    fun applyWinnerImpact(market: MarketUiState, tokenId: TokenId, multiplier: Double): MarketUiState {
        if (multiplier <= 0.0) return market

        val updated = market.tokens.map { token ->
            if (token.id != tokenId) {
                token
            } else {
                val old = token.currentPrice
                val boosted = round2(max(0.5, old * multiplier))
                token.copy(
                    previousPrice = old,
                    currentPrice = boosted,
                    history = (token.history + boosted).takeLast(GameRules.HISTORY_POINTS)
                )
            }
        }

        return market.copy(tokens = updated)
    }

    private fun autoPickBots(ballRoom: BallRoomUiState): BallRoomUiState {
        val freeBallIds = ballRoom.balls.filter { !it.isPicked }.map { it.id }.toMutableList()
        if (freeBallIds.isEmpty()) return ballRoom

        var updatedPlayers = ballRoom.players
        var updatedBalls = ballRoom.balls

        ballRoom.players.filter { !it.isUser && it.selectedBallId == null }.forEach { bot ->
            if (freeBallIds.isEmpty()) return@forEach
            val index = random.nextInt(freeBallIds.size)
            val selectedId = freeBallIds.removeAt(index)

            updatedPlayers = updatedPlayers.map { player ->
                if (player.id == bot.id) player.copy(selectedBallId = selectedId) else player
            }

            updatedBalls = updatedBalls.map { ball ->
                if (ball.id == selectedId) ball.copy(pickedBy = bot.id) else ball
            }
        }

        return ballRoom.copy(players = updatedPlayers, balls = updatedBalls)
    }

    private fun buildPlayers(): List<BallPlayer> {
        val botNames = listOf(
            "Rayo", "Magma", "Nova", "Loki", "Bora",
            "Pik", "Kron", "Vela", "Tora"
        )

        return buildList {
            add(BallPlayer(id = GameRules.USER_PLAYER_ID, nickname = "Tu", isUser = true))
            repeat(GameRules.ROOM_SIZE - 1) { index ->
                val name = botNames.getOrNull(index) ?: "Bot${index + 1}"
                add(BallPlayer(id = "bot_$index", nickname = name, isUser = false))
            }
        }
    }

    private fun randomBattleCard(): BattleCardType {
        return when (random.nextInt(11)) {
            0 -> BattleCardType.SHIELD
            1 -> BattleCardType.REBOUND
            else -> BattleCardType.ATTACK
        }
    }

    private fun randomMultiplier(): Double {
        val skewed = random.nextDouble().pow(2.8)
        val value = GameRules.MULTIPLIER_MIN + skewed * (GameRules.MULTIPLIER_MAX - GameRules.MULTIPLIER_MIN)
        return round2(value)
    }

    private fun randomPrice(): Double {
        val price = random.nextDouble(from = GameRules.TOKEN_BASE_MIN, until = GameRules.TOKEN_BASE_MAX)
        return round2(price)
    }

    private fun generateHistory(base: Double): List<Double> {
        var current = base
        return List(GameRules.HISTORY_POINTS) {
            val drift = random.nextDouble(from = 0.94, until = 1.07)
            current = max(0.5, current * drift)
            round2(current)
        }
    }

    private fun countdownToMidnightLabel(nowMillis: Long): String {
        val now = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val midnight = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diffSeconds = max(0L, (midnight.timeInMillis - now.timeInMillis) / 1000L)
        val hours = diffSeconds / 3600L
        val minutes = (diffSeconds % 3600L) / 60L
        val seconds = diffSeconds % 60L
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun round2(value: Double): Double {
        return kotlin.math.round(value * 100.0) / 100.0
    }

    fun formatMultiplier(multiplier: Double): String {
        val safe = min(max(multiplier, GameRules.MULTIPLIER_MIN), GameRules.MULTIPLIER_MAX)
        return if (safe % 1.0 == 0.0) {
            safe.toInt().toString()
        } else {
            "%.2f".format(Locale.US, safe)
        }
    }
}
