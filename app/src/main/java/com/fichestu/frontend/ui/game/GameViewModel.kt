package com.fichestu.frontend.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fichestu.frontend.data.i18n.AppI18n
import com.fichestu.frontend.data.remote.MatchRealtimeClient
import com.fichestu.frontend.data.repository.GameRepository
import com.fichestu.frontend.data.repository.ProfileRepository
import com.fichestu.frontend.data.repository.SessionStore
import com.fichestu.frontend.data.repository.SessionExpiredException
import com.fichestu.frontend.game.GameRules
import com.fichestu.frontend.game.model.AppLanguage
import com.fichestu.frontend.game.model.BadgeUi
import com.fichestu.frontend.game.model.BallRoomPhase
import com.fichestu.frontend.game.model.BattleHandCard
import com.fichestu.frontend.game.model.BallRoomUiState
import com.fichestu.frontend.game.model.BattleCardType
import com.fichestu.frontend.game.model.BattlePhase
import com.fichestu.frontend.game.model.BattlePlayer
import com.fichestu.frontend.game.model.BattleUiState
import com.fichestu.frontend.game.model.GameUiState
import com.fichestu.frontend.game.model.MainTab
import com.fichestu.frontend.game.model.ProfileStats
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel(
    private val repository: GameRepository = GameRepository(),
    private val profileRepository: ProfileRepository = ProfileRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var rewardedCooldownJob: Job? = null
    private var autoRevealJob: Job? = null
    private var autoRestartJob: Job? = null
    private var matchRefreshRunning = false
    private var matchRefreshQueued = false
    private var nextBattleCardId = 1L
    private val submittedBattleCards = mutableMapOf<String, Long>()
    private val realtimeClient = MatchRealtimeClient { matchId, _ ->
        onRealtimeMatchChanged(matchId)
    }

    init {
        bootstrap()
        startAutoFlow()
        startPassiveRefresh()
    }

    fun initializePlayer(name: String) {
        val safe = name.trim().ifBlank { AppI18n.text("player_default") }
        _uiState.update { state ->
            state.copy(profile = state.profile.copy(playerName = safe))
        }
    }

    fun updateProfileUsername(value: String) {
        _uiState.update { state ->
            state.copy(profile = state.profile.copy(editUsername = value))
        }
    }

    fun updateProfileEmail(value: String) {
        _uiState.update { state ->
            state.copy(profile = state.profile.copy(editEmail = value))
        }
    }

    fun updateCurrentPassword(value: String) {
        _uiState.update { state ->
            state.copy(profile = state.profile.copy(currentPassword = value))
        }
    }

    fun updateNewPassword(value: String) {
        _uiState.update { state ->
            state.copy(profile = state.profile.copy(newPassword = value))
        }
    }

    fun updateConfirmPassword(value: String) {
        _uiState.update { state ->
            state.copy(profile = state.profile.copy(confirmPassword = value))
        }
    }

    fun saveProfile() {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(profile = state.profile.copy(isSavingProfile = true), transientMessage = null)
            }

            val snapshot = _uiState.value
            val result = profileRepository.saveProfile(snapshot)
            applyResult(result)
            if (result.isFailure) {
                _uiState.update { state ->
                    state.copy(profile = state.profile.copy(isSavingProfile = false))
                }
            }
        }
    }

    fun changePassword() {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(profile = state.profile.copy(isSavingPassword = true), transientMessage = null)
            }

            val snapshot = _uiState.value
            val result = profileRepository.changePassword(snapshot)
            applyResult(result)
            if (result.isFailure) {
                _uiState.update { state ->
                    state.copy(profile = state.profile.copy(isSavingPassword = false))
                }
            }
        }
    }

    fun uploadProfileAvatar(bytes: ByteArray, mimeType: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(profile = state.profile.copy(isSavingProfile = true), transientMessage = null)
            }

            val snapshot = _uiState.value
            val result = profileRepository.uploadAvatar(snapshot, bytes, mimeType)
            applyResult(result)
            if (result.isFailure) {
                _uiState.update { state ->
                    state.copy(profile = state.profile.copy(isSavingProfile = false))
                }
            }
        }
    }

    fun selectPresetAvatar(presetId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(profile = state.profile.copy(isSavingProfile = true), transientMessage = null)
            }

            val result = profileRepository.savePresetAvatar(_uiState.value, presetId)
            applyResult(result)
            if (result.isFailure) {
                _uiState.update { state ->
                    state.copy(profile = state.profile.copy(isSavingProfile = false))
                }
            }
        }
    }

    fun changeLanguage(language: AppLanguage) {
        SessionStore.setLanguage(language)
        _uiState.update { state ->
            state.copy(
                appLanguage = language,
                transientMessage = "${AppI18n.text("language", language)}: ${language.label}",
                ballRoom = state.ballRoom.copy(
                    statusMessage = AppI18n.message(state.ballRoom.statusMessage, language)
                        ?: state.ballRoom.statusMessage
                )
            )
        }
        viewModelScope.launch {
            val result = profileRepository.saveLanguage(_uiState.value, language)
            result.onSuccess { nextState ->
                _uiState.update { current ->
                    current.copy(
                        appLanguage = nextState.appLanguage,
                        profile = nextState.profile,
                        transientMessage = nextState.transientMessage
                    )
                }
            }
            result.onFailure { error ->
                if (error is SessionExpiredException) {
                    expireSession(error)
                    return@onFailure
                }
                _uiState.update { state ->
                    state.copy(transientMessage = AppI18n.message(error.message, state.appLanguage) ?: error.message)
                }
            }
        }
    }

    fun selectTab(tab: MainTab) {
        _uiState.update { state ->
            if (state.battle.phase == BattlePhase.DEFEATED && tab != MainTab.BATTLE) {
                state.copy(transientMessage = AppI18n.text("back_to_entry"))
            } else {
                state.copy(activeTab = tab)
            }
        }
    }

    fun openBallRoomTab() {
        selectTab(MainTab.BALL_ROOM)
    }

    fun selectToken(tokenId: com.fichestu.frontend.game.model.TokenId) {
        _uiState.update { state ->
            state.copy(market = state.market.copy(selectedToken = tokenId))
        }
    }

    fun buySelectedToken() {
        viewModelScope.launch {
            val snapshot = _uiState.value
            val result = repository.buy(snapshot)
            applyResult(result)
        }
    }

    fun sellSelectedToken() {
        viewModelScope.launch {
            val snapshot = _uiState.value
            val result = repository.sell(snapshot)
            applyResult(result)
        }
    }

    fun applyMinigameResult(deltaCash: Double, message: String) {
        _uiState.update { state ->
            state.copy(
                market = state.market.copy(
                    cashBalance = (state.market.cashBalance + deltaCash).coerceAtLeast(0.0),
                    reportedTotalBalance = null
                ),
                transientMessage = message
            )
        }
    }

    fun enterBallRoom() {
        viewModelScope.launch {
            val snapshot = _uiState.value
            if (snapshot.ballRoom.phase == BallRoomPhase.MATCHMAKING) {
            _uiState.update { it.copy(transientMessage = AppI18n.text("already_searching")) }
                return@launch
            }
            snapshot.currentMatchId?.let { matchId ->
                runCatching { repository.abandonMatch(snapshot, matchId) }
                _uiState.update { state ->
                    state.copy(
                        currentMatchId = null,
                        ballRoom = BallRoomUiState(
                            phase = BallRoomPhase.WAITING_ENTRY,
                            statusMessage = AppI18n.text("preparing_room")
                        ),
                        battle = BattleUiState(phase = BattlePhase.LOCKED)
                    )
                }
            }
            val result = repository.enterBallRoom(_uiState.value)
            applyResult(result)
        }
    }

    fun cancelMatchmaking() {
        viewModelScope.launch {
            val snapshot = _uiState.value
            val matchId = snapshot.currentMatchId
            if (matchId == null) {
            _uiState.update { it.copy(transientMessage = AppI18n.text("no_matchmaking")) }
                return@launch
            }

            val result = repository.abandonMatch(snapshot, matchId)
            applyResult(result)
        }
    }

    fun abandonActiveMatchForExit(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            val snapshot = _uiState.value
            val matchId = snapshot.currentMatchId
            if (matchId != null) {
                val result = repository.abandonMatch(snapshot, matchId)
                applyResult(result)
            }
            onComplete?.invoke()
        }
    }

    fun refreshActiveMatch() {
        viewModelScope.launch {
            val current = _uiState.value
            if (current.currentMatchId == null) return@launch
            val result = repository.refreshMatch(current)
            result.onSuccess { newState ->
                _uiState.value = mergeIncomingGameState(newState, current)
                syncRealtimeSubscription(_uiState.value)
            }
            result.onFailure { error ->
                if (error is SessionExpiredException) {
                    _uiState.update { it.copy(isSessionExpired = true) }
                }
            }
        }
    }

    fun startBallPicking() {
        _uiState.update { state ->
            if (state.currentMatchId == null || state.ballRoom.phase != BallRoomPhase.WAITING_ENTRY) {
                state
            } else {
                state.copy(
                    ballRoom = state.ballRoom.copy(
                        phase = BallRoomPhase.PICKING,
                        statusMessage = AppI18n.text("pick_ball_message"),
                        pendingSelectedBallId = null
                    ),
                    transientMessage = AppI18n.text("room_ready_pick_ball")
                )
            }
        }
    }

    fun pickBall(ballId: Int) {
        val snapshot = _uiState.value
        val option = snapshot.ballRoom.balls.firstOrNull { it.id == ballId }
        val alreadyPickedByUser = snapshot.ballRoom.players.any { it.isUser && it.selectedBallId == ballId }
        if (option == null || (option.isPicked && !alreadyPickedByUser)) {
            _uiState.update { it.copy(transientMessage = AppI18n.text("ball_taken")) }
            return
        }
        _uiState.update { state ->
            state.copy(ballRoom = state.ballRoom.copy(pendingSelectedBallId = ballId))
        }
    }

    fun confirmBallSelection() {
        viewModelScope.launch {
            val snapshot = _uiState.value
            val matchId = snapshot.currentMatchId
            if (matchId == null) {
                _uiState.update { it.copy(transientMessage = AppI18n.text("no_active_room")) }
                return@launch
            }
            val ballId = snapshot.ballRoom.pendingSelectedBallId
            if (ballId == null) {
                _uiState.update { it.copy(transientMessage = AppI18n.text("pick_ball_first")) }
                return@launch
            }

            val result = repository.pickBall(snapshot, matchId, ballId)
            applyResult(result)
        }
    }

    fun finishBallSelection() {
        viewModelScope.launch {
            var snapshot = _uiState.value
            val selectedBallId = snapshot.ballRoom.pendingSelectedBallId

            if (selectedBallId == null) {
                _uiState.update { it.copy(transientMessage = AppI18n.text("pick_ball_first")) }
                return@launch
            }

            val matchId = snapshot.currentMatchId
            val backendSelected = snapshot.ballRoom.players.firstOrNull { it.isUser }?.selectedBallId
            if (backendSelected != null && backendSelected != selectedBallId) {
                refreshActiveMatch()
                return@launch
            }
            val alreadyPicked = backendSelected == selectedBallId
            if (matchId != null && !alreadyPicked) {
                val pickResult = repository.pickBall(snapshot, matchId, selectedBallId)
                pickResult.onSuccess { pickedState ->
                    _uiState.value = pickedState
                    snapshot = pickedState
                }
                if (pickResult.isFailure) {
                    applyResult(pickResult)
                    return@launch
                }
            }

            if (matchId != null && _uiState.value.ballRoom.canRevealBattle) {
                applyResult(repository.revealMultipliers(_uiState.value, matchId))
            } else {
                refreshActiveMatch()
            }
        }
    }

    fun autoFinishBallSelectionOnTimeout() {
        viewModelScope.launch {
            var snapshot = _uiState.value
            if (snapshot.ballRoom.phase != BallRoomPhase.PICKING) return@launch

            val matchId = snapshot.currentMatchId
            if (matchId == null) {
            _uiState.update { it.copy(transientMessage = AppI18n.text("no_active_room")) }
                return@launch
            }

            val backendSelected = snapshot.ballRoom.players.firstOrNull { it.isUser }?.selectedBallId
            val pendingSelected = snapshot.ballRoom.pendingSelectedBallId?.takeIf { pendingId ->
                snapshot.ballRoom.balls.any { ball ->
                    ball.id == pendingId && (!ball.isPicked || backendSelected == pendingId)
                }
            }
            val selectedBallId = backendSelected
                ?: pendingSelected
                ?: snapshot.ballRoom.balls
                    .filter { !it.isPicked }
                    .randomOrNull()
                    ?.id

            if (selectedBallId == null) {
                _uiState.update { it.copy(transientMessage = AppI18n.text("no_free_balls")) }
                return@launch
            }

            if (backendSelected == null) {
                val pickResult = repository.pickBall(snapshot, matchId, selectedBallId)
                pickResult.onSuccess { pickedState ->
                    _uiState.value = pickedState.copy(
                            transientMessage = "${AppI18n.text("time_expired_ball_assigned")} #$selectedBallId"
                    )
                    snapshot = pickedState
                }
                pickResult.onFailure { error ->
                    if (error is SessionExpiredException) {
                        _uiState.update { it.copy(isSessionExpired = true) }
                    } else {
                        _uiState.update {
                    it.copy(transientMessage = AppI18n.message(error.message) ?: AppI18n.text("auto_ball_error"))
                        }
                    }
                    return@launch
                }
            }

            refreshMatchFromState(snapshot)
        }
    }

    fun revealBallMultipliers() {
        viewModelScope.launch {
            val snapshot = _uiState.value
            val matchId = snapshot.currentMatchId
            if (matchId == null) {
            _uiState.update { it.copy(transientMessage = AppI18n.text("no_active_room")) }
                return@launch
            }

            val result = repository.revealMultipliers(snapshot, matchId)
            applyResult(result)
        }
    }

    fun chooseBattleAction(action: BattleCardType) {
        _uiState.update { state ->
            state.copy(battle = state.battle.copy(selectedAction = action))
        }
    }

    fun chooseBattleCard(cardId: Long) {
        _uiState.update { state ->
            val card = state.battle.hand.firstOrNull { it.id == cardId } ?: return@update state
            state.copy(
                battle = state.battle.copy(
                    selectedCardId = cardId,
                    selectedAction = card.type
                )
            )
        }
    }

    fun chooseBattleTarget(playerId: String) {
        _uiState.update { state ->
            val target = state.battle.players.firstOrNull { it.id == playerId && !it.isUser && it.isAlive }
                ?: return@update state
            state.copy(battle = state.battle.copy(selectedTargetId = target.id))
        }
    }

    fun playBattleRound() {
        viewModelScope.launch {
            val snapshot = _uiState.value
            val matchId = snapshot.currentMatchId
            if (matchId == null) {
                if (snapshot.battle.hand.isNotEmpty()) {
                    resolveLocalBattleRound()
                } else {
                    _uiState.update { it.copy(transientMessage = AppI18n.text("no_active_battle")) }
                }
                return@launch
            }

            snapshot.battle.selectedCardId?.let { cardId ->
                submittedBattleCards[battleCardKey(matchId, snapshot.battle.round)] = cardId
            }
            val optimisticSubmittedActions = ((snapshot.battle.submittedActions ?: 0) + 1)
                .coerceAtMost(snapshot.battle.aliveHumans ?: Int.MAX_VALUE)
            _uiState.update { state ->
                if (state.currentMatchId == matchId && !state.battle.userActionSubmitted) {
                    state.copy(
                        battle = state.battle.copy(
                            userActionSubmitted = true,
                            submittedActions = optimisticSubmittedActions
                        )
                    )
                } else {
                    state
                }
            }
            val result = repository.playBattleRound(snapshot, matchId)
            result.onFailure {
                submittedBattleCards.remove(battleCardKey(matchId, snapshot.battle.round))
                _uiState.update { state ->
                    if (state.currentMatchId == matchId) {
                        state.copy(battle = snapshot.battle)
                    } else {
                        state
                    }
                }
            }
            applyBattleRoundResult(result)
        }
    }

    private fun battleCardKey(matchId: Int, round: Int): String = "$matchId:$round"

    private fun resolveLocalBattleRound() {
        _uiState.update { state ->
            val battle = ensureBattleHand(state.battle)
            if (battle.players.none { it.isUser && it.isAlive }) {
                return@update state.copy(transientMessage = AppI18n.text("not_alive"))
            }
            val card = battle.hand.firstOrNull { it.id == battle.selectedCardId } ?: battle.hand.firstOrNull()
                ?: return@update state.copy(transientMessage = AppI18n.text("no_cards"))
            val aliveOpponents = battle.players.filter { !it.isUser && it.isAlive }
            val targetId = if (card.type == BattleCardType.ATTACK) {
                battle.selectedTargetId?.takeIf { id -> aliveOpponents.any { it.id == id } }
                    ?: aliveOpponents.firstOrNull()?.id
            } else {
                null
            }

            var userShield = false
            var userRebound = false
            val roundLog = mutableListOf<String>()
            val afterUser = battle.players.map { player ->
                when {
                    player.isUser && card.type == BattleCardType.SHIELD -> {
                        userShield = true
                        roundLog += "${AppI18n.text("defense")}: bloqueas ataques enemigos."
                        player.copy(shieldActive = true, reboundActive = false)
                    }
                    player.isUser && card.type == BattleCardType.REBOUND -> {
                        userRebound = true
                        roundLog += "${AppI18n.text("rebound")}: devuelves daño enemigo."
                        player.copy(reboundActive = true, shieldActive = false)
                    }
                    targetId != null && player.id == targetId -> {
                        val nextHp = (player.hp - card.power).coerceAtLeast(0)
                        roundLog += "Atacas a ${player.nickname} con ${card.power} daño."
                        player.copy(hp = nextHp, shieldActive = false, reboundActive = false)
                    }
                    else -> player.copy(shieldActive = false, reboundActive = false)
                }
            }

            val livingBots = afterUser.filter { !it.isUser && it.isAlive }
            val botAttackers = livingBots.shuffled().take(3)
            val incoming = botAttackers.sumOf { Random.nextInt(1, 5) }
            val reducedIncoming = when {
                userShield -> (incoming - card.power).coerceAtLeast(0)
                userRebound -> incoming / 2
                else -> incoming
            }
            val reflectedDamage = if (userRebound) (card.power + (incoming / 2)).coerceAtLeast(3) else 0
            val rebounded = reflectedDamage
            val reflectedIds = botAttackers.map { it.id }.toSet()

            val afterBots = afterUser.map { player ->
                when {
                    player.isUser -> player.copy(
                        hp = (player.hp - reducedIncoming).coerceAtLeast(0),
                        shieldActive = userShield,
                        reboundActive = userRebound
                    )
                    userRebound && player.id in reflectedIds -> {
                        player.copy(hp = (player.hp - reflectedDamage).coerceAtLeast(0))
                    }
                    else -> player
                }
            }
            if (incoming > 0) {
                roundLog += "Bots te atacan: $reducedIncoming daño."
            }
            if (userRebound && botAttackers.isNotEmpty()) {
                roundLog += "${AppI18n.text("rebound_returns")} $rebounded ${AppI18n.text("damage")}."
            }

            val alivePlayers = afterBots.filter { it.isAlive }
            val winner = alivePlayers.singleOrNull()
            val newHand = battle.hand.filterNot { it.id == card.id }.plus(drawBattleCard())
            val nextSelectedCard = newHand.firstOrNull()?.id
            val nextTarget = afterBots.firstOrNull {
                !it.isUser && it.isAlive && it.id == battle.selectedTargetId
            }?.id ?: afterBots.firstOrNull { !it.isUser && it.isAlive }?.id
            val userAfterRound = afterBots.firstOrNull { it.isUser }
            if (userAfterRound != null && !userAfterRound.isAlive) {
                val placement = (afterBots.count { !it.isUser && it.isAlive } + 1).coerceAtLeast(2)
                return@update state.copy(
                    currentMatchId = null,
                    activeTab = MainTab.BATTLE,
                    ballRoom = BallRoomUiState(
                        phase = BallRoomPhase.WAITING_ENTRY,
                    statusMessage = AppI18n.text("pay_entry")
                    ),
                    battle = battle.copy(
                        phase = BattlePhase.DEFEATED,
                        players = afterBots,
                        placement = placement,
                        hand = emptyList(),
                        selectedCardId = null,
                        selectedTargetId = null,
                    log = battle.log + roundLog + "Has quedado en posición #$placement."
                    ),
                    transientMessage = AppI18n.text("eliminated_back")
                )
            }

            state.copy(
                battle = battle.copy(
                    phase = if (winner != null) BattlePhase.FINISHED else BattlePhase.IN_PROGRESS,
                    players = afterBots,
                    round = battle.round + 1,
                    log = battle.log + roundLog,
                    winnerId = winner?.let { if (it.isUser) "user" else it.id },
                    winnerName = winner?.nickname,
                    winningMultiplier = winner?.multiplier,
                    selectedAction = newHand.firstOrNull { it.id == nextSelectedCard }?.type ?: BattleCardType.ATTACK,
                    hand = newHand,
                    selectedCardId = nextSelectedCard,
                    selectedTargetId = nextTarget,
                    impactApplied = winner != null
                ),
                transientMessage = AppI18n.text("card_used")
            )
        }
    }

    private fun drawBattleHand(): List<BattleHandCard> = List(5) { drawBattleCard() }

    private fun drawBattleCard(): BattleHandCard {
        val type = when (Random.nextInt(100)) {
            in 0..74 -> BattleCardType.ATTACK
            in 75..88 -> BattleCardType.SHIELD
            else -> BattleCardType.REBOUND
        }
        return BattleHandCard(
            id = nextBattleCardId++,
            type = type,
            power = when (type) {
                BattleCardType.ATTACK -> Random.nextInt(1, 11)
                BattleCardType.SHIELD -> Random.nextInt(5, 11)
                BattleCardType.REBOUND -> Random.nextInt(3, 8)
            }
        )
    }

    private fun ensureBattleHand(battle: BattleUiState): BattleUiState {
        if (battle.phase == BattlePhase.LOCKED || battle.phase == BattlePhase.DEFEATED || battle.phase == BattlePhase.FINISHED) return battle
        val hand = if (battle.hand.size >= 5) battle.hand else battle.hand + List(5 - battle.hand.size) { drawBattleCard() }
        val selectedCardId = battle.selectedCardId?.takeIf { id -> hand.any { it.id == id } } ?: hand.firstOrNull()?.id
        val selectedTargetId = battle.selectedTargetId?.takeIf { id ->
            battle.players.any { it.id == id && !it.isUser && it.isAlive }
        } ?: battle.players.firstOrNull { !it.isUser && it.isAlive }?.id
        val selectedAction = hand.firstOrNull { it.id == selectedCardId }?.type ?: battle.selectedAction
        return battle.copy(
            hand = hand,
            selectedCardId = selectedCardId,
            selectedTargetId = selectedTargetId,
            selectedAction = selectedAction
        )
    }

    private fun mergeBattleHand(incoming: BattleUiState, previous: BattleUiState): BattleUiState {
        if (incoming.phase == BattlePhase.LOCKED || incoming.phase == BattlePhase.DEFEATED || incoming.phase == BattlePhase.FINISHED) return incoming
        val base = incoming.copy(
            hand = previous.hand.takeIf { it.isNotEmpty() } ?: incoming.hand,
            selectedCardId = previous.selectedCardId ?: incoming.selectedCardId,
            selectedTargetId = previous.selectedTargetId ?: incoming.selectedTargetId
        )
        return ensureBattleHand(base)
    }

    fun resetBattleAndRoom() {
        autoRestartJob?.cancel()
        resetBattleAndRoom(autoEnterRoom = false)
    }

    fun applyWinnerImpactAndReset() {
        viewModelScope.launch {
            autoRestartJob?.cancel()
            val snapshot = _uiState.value
            val matchId = snapshot.currentMatchId
            val userWon = snapshot.battle.phase == BattlePhase.FINISHED &&
                (snapshot.battle.winnerId == GameRules.USER_PLAYER_ID || snapshot.battle.players.any { it.isUser && it.isAlive })

            if (matchId != null && userWon && !snapshot.battle.impactApplied) {
                val result = repository.applyWinnerImpact(snapshot, matchId)
                result.onSuccess { applied ->
        _uiState.value = applied.copy(transientMessage = "${AppI18n.text("multiplier")} ${AppI18n.text("selected")}: ${snapshot.market.selectedToken.name}.")
                    resetBattleAndRoom(autoEnterRoom = false)
                }
                result.onFailure { error ->
                    if (error is SessionExpiredException) {
                        expireSession(error)
                    } else {
            _uiState.update { it.copy(transientMessage = AppI18n.message(error.message) ?: AppI18n.text("multiplier_apply_error")) }
                    }
                }
                return@launch
            }

            resetBattleAndRoom(autoEnterRoom = false)
        }
    }

    private fun resetBattleAndRoom(autoEnterRoom: Boolean) {
        viewModelScope.launch {
            val snapshot = _uiState.value
            val matchId = snapshot.currentMatchId
            if (matchId != null) {
                repository.closeMatch(matchId)
            }

            _uiState.update { state ->
                state.copy(
                    currentMatchId = null,
                    ballRoom = BallRoomUiState(
                        phase = BallRoomPhase.WAITING_ENTRY,
                            statusMessage = AppI18n.text("pay_entry")
                    ),
                    battle = BattleUiState(
                        phase = BattlePhase.LOCKED,
                        log = listOf(AppI18n.text("new_cycle_ready"))
                    ),
                    activeTab = MainTab.BALL_ROOM,
                    transientMessage = if (autoEnterRoom) {
                        AppI18n.text("match_finished_new_room")
                    } else {
                        AppI18n.text("battle_closed_ready")
                    }
                )
            }

            if (autoEnterRoom) {
                val result = repository.enterBallRoom(_uiState.value)
                applyResult(result)
            }
        }
    }

    fun claimRewardedAd() {
        val current = _uiState.value
        if (!current.rewardedAvailable || current.rewardedCooldownSec > 0) {
            _uiState.update { it.copy(transientMessage = AppI18n.text("rewarded_not_available")) }
            return
        }

        viewModelScope.launch {
            val snapshot = _uiState.value
            val result = repository.claimRewarded(snapshot)
            applyResult(result)
            if (result.isSuccess) {
                startRewardedCooldown()
            }
        }
    }

    fun consumeTransientMessage() {
        _uiState.update { it.copy(transientMessage = null) }
    }

    fun markNotificationsRead() {
        viewModelScope.launch {
            val result = repository.markNotificationsRead(_uiState.value)
            result.onSuccess { nextState ->
                mergeNotifications(nextState)
            }
            result.onFailure { error ->
                if (error is SessionExpiredException) {
                    expireSession(error)
                }
            }
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            val result = repository.clearNotifications(_uiState.value)
            result.onSuccess { nextState ->
                mergeNotifications(nextState)
            }
            result.onFailure { error ->
                if (error is SessionExpiredException) {
                    expireSession(error)
                }
            }
        }
    }

    private fun bootstrap() {
        viewModelScope.launch {
            _uiState.update { it.copy(transientMessage = AppI18n.text("loading_state")) }
            val result = repository.bootstrap(_uiState.value)
            applyResult(result)
            if (result.isSuccess) {
                loadProfile()
                refreshNotifications()
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val result = profileRepository.loadProfile(_uiState.value)
            result.onSuccess { nextState ->
                _uiState.update { current ->
                    current.copy(
                        appLanguage = nextState.appLanguage,
                        profile = nextState.profile,
                        transientMessage = nextState.transientMessage
                    )
                }
            }
            result.onFailure { error ->
                if (error is SessionExpiredException) {
                    expireSession(error)
                    return@onFailure
                }
                _uiState.update { state ->
            state.copy(transientMessage = AppI18n.message(error.message) ?: AppI18n.text("profile_load_error"))
                }
            }
        }
    }

    /**
     * Auto-flow: hace que las pantallas avancen solas cuando termina la fase.
     * - PICKING: si el usuario no elige bola en 20s, se auto-elige una al azar.
     * - canRevealBattle (todos eligieron): tras 2s, auto-revela multiplicadores.
     * - REVEALED: tras 5s, salta automáticamente al tab BATTLE.
     */
    private fun startAutoFlow() {
        // Watch canRevealBattle to auto-reveal once everyone has picked
        viewModelScope.launch {
            uiState
                .map {
                    it.ballRoom.canRevealBattle && it.ballRoom.phase == BallRoomPhase.PICKING
                }
                .distinctUntilChanged()
                .collect { ready ->
                    autoRevealJob?.cancel()
                    if (ready) {
                        autoRevealJob = viewModelScope.launch {
                            delay(2_000)
                            val now = uiState.value
                            if (now.ballRoom.canRevealBattle &&
                                now.ballRoom.phase == BallRoomPhase.PICKING
                            ) {
                                revealBallMultipliers()
                            }
                        }
                    }
                }
        }

        viewModelScope.launch {
            uiState
                .map { it.battle.phase }
                .distinctUntilChanged()
                .collect { phase ->
                    if (phase == BattlePhase.FINISHED) {
                        autoRestartJob?.cancel()
                    }
                }
        }
    }

    private fun startPassiveRefresh() {
        viewModelScope.launch {
            while (isActive) {
                val current = _uiState.value
                if (shouldRefreshMatch(current)) {
                    refreshMatchFromState(current)
                }
                delay(passiveRefreshDelayMs(_uiState.value))
            }
        }

        viewModelScope.launch {
            while (isActive) {
                delay(10_000)
                refreshNotifications()
            }
        }

        viewModelScope.launch {
            while (isActive) {
                delay(12_000)
                val current = _uiState.value
                if (current.activeTab == MainTab.DASHBOARD || current.activeTab == MainTab.BALL_ROOM) {
                    refreshMarketFromState(current)
                }
            }
        }
    }

    private fun shouldRefreshMatch(current: GameUiState): Boolean {
        if (current.currentMatchId == null) return false

        val shouldRefreshBattleTimer = current.battle.phase == BattlePhase.IN_PROGRESS
        val shouldRefreshBattle = current.activeTab == MainTab.BATTLE &&
            current.battle.phase != BattlePhase.LOCKED &&
            current.battle.hand.isEmpty()
        val shouldRefreshBallRoom = current.activeTab == MainTab.BALL_ROOM &&
            (current.ballRoom.phase == BallRoomPhase.MATCHMAKING ||
                current.ballRoom.phase == BallRoomPhase.PICKING)

        return shouldRefreshBattleTimer || shouldRefreshBattle || shouldRefreshBallRoom
    }

    private fun passiveRefreshDelayMs(current: GameUiState): Long {
        if (current.currentMatchId == null) return 2_500L

        return when {
            current.activeTab == MainTab.BALL_ROOM &&
                current.ballRoom.phase == BallRoomPhase.MATCHMAKING -> {
                val remainingMs = current.ballRoom.selectionDeadlineEpochMs
                    ?.let { it - System.currentTimeMillis() }
                when {
                    remainingMs == null -> 1_500L
                    remainingMs <= 1_000L -> 300L
                    remainingMs <= 3_500L -> 600L
                    else -> 1_500L
                }
            }
            current.activeTab == MainTab.BALL_ROOM &&
                current.ballRoom.phase == BallRoomPhase.PICKING -> 450L
            current.activeTab == MainTab.BATTLE -> 450L
            else -> 2_500L
        }
    }

    private suspend fun refreshMatchFromState(current: GameUiState) {
        if (matchRefreshRunning) {
            matchRefreshQueued = true
            return
        }
        matchRefreshRunning = true
        try {
            var snapshot = current
            do {
                matchRefreshQueued = false
                val result = repository.refreshMatch(snapshot)
                result.onSuccess { newState ->
                    _uiState.value = mergeIncomingGameState(newState, _uiState.value)
                    syncRealtimeSubscription(_uiState.value)
                }
                result.onFailure { error ->
                    if (error is SessionExpiredException) {
                        _uiState.update { it.copy(isSessionExpired = true) }
                    }
                }
                snapshot = _uiState.value
            } while (matchRefreshQueued && snapshot.currentMatchId != null)
        } finally {
            matchRefreshRunning = false
        }
    }

    private fun requestMatchRefresh() {
        viewModelScope.launch {
            val current = _uiState.value
            if (current.currentMatchId != null) {
                refreshMatchFromState(current)
            }
        }
    }

    private suspend fun refreshMarketFromState(current: GameUiState) {
        val result = repository.refreshMarket(current)
        result.onSuccess { newState ->
            _uiState.update { latest ->
                latest.copy(
                    market = newState.market.copy(selectedToken = latest.market.selectedToken),
                    rewardedAvailable = newState.rewardedAvailable,
                    rewardedCooldownSec = newState.rewardedCooldownSec,
                    profile = latest.profile.copy(stats = newState.profile.stats)
                )
            }
        }
        result.onFailure { error ->
            if (error is SessionExpiredException) {
                expireSession(error)
            }
        }
    }

    private fun startRewardedCooldown() {
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

    private fun applyResult(result: Result<GameUiState>) {
        var shouldRefreshNotifications = false
        _uiState.update { current ->
            result.fold(
                onSuccess = { success ->
                    shouldRefreshNotifications = true
                    mergeIncomingGameState(success, current)
                },
                onFailure = { error ->
                    if (error is SessionExpiredException) {
                        current.copy(
                            isSessionExpired = true,
                            transientMessage = error.message
                        )
                    } else {
            current.copy(transientMessage = AppI18n.message(error.message) ?: AppI18n.text("connection_error"))
                    }
                }
            )
        }
        if (shouldRefreshNotifications) {
            syncRealtimeSubscription(_uiState.value)
            refreshNotifications()
        }
    }

    private fun applyBattleRoundResult(result: Result<GameUiState>) {
        var shouldRefreshNotifications = false
        _uiState.update { current ->
            result.fold(
                onSuccess = { success ->
                    shouldRefreshNotifications = true
                    val next = success.copy(
                        battle = mergeBattleHand(success.battle, current.battle)
                    )
                    val roundResolved = didBattleRoundAdvance(next.battle, current.battle)
                    next.copy(
                        battle = if (roundResolved) {
                            consumeBattleCardAfterBackendRound(current.currentMatchId, next.battle, current.battle)
                        } else {
                            next.battle
                        },
                        transientMessage = if (roundResolved) {
                            AppI18n.text("card_used")
                        } else {
                            AppI18n.text("card_prepared")
                        }
                    )
                },
                onFailure = { error ->
                    if (error is SessionExpiredException) {
                        current.copy(
                            isSessionExpired = true,
                            transientMessage = error.message
                        )
                    } else {
            current.copy(transientMessage = AppI18n.message(error.message) ?: AppI18n.text("connection_error"))
                    }
                }
            )
        }
        if (shouldRefreshNotifications) {
            syncRealtimeSubscription(_uiState.value)
            refreshNotifications()
        }
    }

    private fun onRealtimeMatchChanged(matchId: Int) {
        if (_uiState.value.currentMatchId == matchId) {
            requestMatchRefresh()
        }
    }

    private fun syncRealtimeSubscription(state: GameUiState) {
        val matchId = state.currentMatchId
        val token = SessionStore.tokenOrNull()
        if (matchId == null || token == null) {
            realtimeClient.close()
            return
        }
        realtimeClient.connect(matchId, token)
    }

    override fun onCleared() {
        realtimeClient.close()
        super.onCleared()
    }

    private fun consumeBattleCardAfterBackendRound(
        matchId: Int?,
        incoming: BattleUiState,
        previous: BattleUiState
    ): BattleUiState {
        if (incoming.phase == BattlePhase.LOCKED || incoming.phase == BattlePhase.DEFEATED || incoming.phase == BattlePhase.FINISHED) {
            return incoming
        }
        val previousHand = previous.hand.takeIf { it.isNotEmpty() } ?: incoming.hand
        val usedCardId = matchId
            ?.let { submittedBattleCards.remove(battleCardKey(it, previous.round)) }
            ?: previous.selectedCardId
            ?: previousHand.firstOrNull()?.id
        val nextHand = previousHand.filterNot { it.id == usedCardId }.plus(drawBattleCard()).take(5)
        val nextSelectedCardId = nextHand.firstOrNull()?.id
        return incoming.copy(
            hand = nextHand,
            selectedCardId = nextSelectedCardId,
            selectedTargetId = incoming.players.firstOrNull { !it.isUser && it.isAlive }?.id,
            selectedAction = nextHand.firstOrNull { it.id == nextSelectedCardId }?.type ?: incoming.selectedAction
        )
    }

    private fun mergeIncomingGameState(incoming: GameUiState, previous: GameUiState): GameUiState {
        val mergedBattle = mergeBattleHand(incoming.battle, previous.battle)
        val roundAdvanced = didBattleRoundAdvance(mergedBattle, previous.battle)
        return incoming.copy(
            battle = if (roundAdvanced) {
                if (previous.battle.userActionSubmitted ||
                    previous.currentMatchId?.let { submittedBattleCards.containsKey(battleCardKey(it, previous.battle.round)) } == true
                ) {
                    consumeBattleCardAfterBackendRound(previous.currentMatchId, mergedBattle, previous.battle)
                } else {
                    mergedBattle.copy(
                        hand = previous.battle.hand.takeIf { it.isNotEmpty() } ?: mergedBattle.hand,
                        selectedCardId = previous.battle.selectedCardId ?: mergedBattle.selectedCardId,
                        selectedTargetId = previous.battle.selectedTargetId ?: mergedBattle.selectedTargetId,
                        selectedAction = previous.battle.hand
                            .firstOrNull { it.id == previous.battle.selectedCardId }
                            ?.type
                            ?: mergedBattle.selectedAction
                    )
                }
            } else {
                mergedBattle
            }
        )
    }

    private fun didBattleRoundAdvance(incoming: BattleUiState, previous: BattleUiState): Boolean {
        if (incoming.phase == BattlePhase.LOCKED || incoming.phase == BattlePhase.DEFEATED) return false
        if (previous.phase == BattlePhase.LOCKED || previous.phase == BattlePhase.DEFEATED) return false
        return incoming.phase == BattlePhase.FINISHED || incoming.round > previous.round
    }

    private fun refreshNotifications() {
        viewModelScope.launch {
            val result = repository.loadNotifications(_uiState.value)
            result.onSuccess { nextState ->
                mergeNotifications(nextState)
            }
            result.onFailure { error ->
                if (error is SessionExpiredException) {
                    expireSession(error)
                }
            }
        }
    }

    private fun mergeNotifications(nextState: GameUiState) {
        _uiState.update { current ->
            current.copy(
                notifications = nextState.notifications,
                unreadNotificationCount = nextState.unreadNotificationCount
            )
        }
    }

    private fun expireSession(error: SessionExpiredException) {
        _uiState.update { state ->
            state.copy(
                isSessionExpired = true,
                transientMessage = error.message
            )
        }
    }

    private fun initialState(): GameUiState {
        val stats = ProfileStats()
        val language = SessionStore.language()
        return GameUiState(
            appLanguage = language,
            activeTab = MainTab.DASHBOARD,
            ballRoom = BallRoomUiState(
                phase = BallRoomPhase.WAITING_ENTRY,
                statusMessage = AppI18n.text("pay_entry", language)
            ),
            battle = BattleUiState(
                phase = BattlePhase.LOCKED,
                log = listOf(AppI18n.text("complete_ball_first"))
            ),
            profile = com.fichestu.frontend.game.model.ProfileUiState(
                playerName = "Jugador",
                badges = recomputeBadges(stats),
                stats = stats
            ),
            rewardedAvailable = true,
            rewardedCooldownSec = 0,
            transientMessage = null
        )
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
}
