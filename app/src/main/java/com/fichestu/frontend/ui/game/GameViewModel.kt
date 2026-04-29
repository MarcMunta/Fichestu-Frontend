package com.fichestu.frontend.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fichestu.frontend.data.repository.GameRepository
import com.fichestu.frontend.data.repository.ProfileRepository
import com.fichestu.frontend.data.repository.SessionExpiredException
import com.fichestu.frontend.game.model.BadgeUi
import com.fichestu.frontend.game.model.BallRoomPhase
import com.fichestu.frontend.game.model.BallRoomUiState
import com.fichestu.frontend.game.model.BattleCardType
import com.fichestu.frontend.game.model.BattlePhase
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

class GameViewModel(
    private val repository: GameRepository = GameRepository(),
    private val profileRepository: ProfileRepository = ProfileRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var rewardedCooldownJob: Job? = null
    private var autoPickJob: Job? = null
    private var autoRevealJob: Job? = null
    private var autoBattleJob: Job? = null
    private var autoRestartJob: Job? = null

    init {
        bootstrap()
        startAutoFlow()
        startPassiveRefresh()
    }

    fun initializePlayer(name: String) {
        val safe = name.trim().ifBlank { "Jugador" }
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

    fun selectTab(tab: MainTab) {
        val normalized = if (tab == MainTab.BATTLE) MainTab.BALL_ROOM else tab
        _uiState.update { it.copy(activeTab = normalized) }
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
                transientMessage = "Minijuegos locales deshabilitados para economia real. Usa mercado, bonus diario o salas."
            )
        }
    }

    fun enterBallRoom() {
        viewModelScope.launch {
            val snapshot = _uiState.value
            val result = repository.enterBallRoom(snapshot)
            applyResult(result)
        }
    }

    fun pickBall(ballId: Int) {
        viewModelScope.launch {
            val snapshot = _uiState.value
            val matchId = snapshot.currentMatchId
            if (matchId == null) {
                _uiState.update { it.copy(transientMessage = "No hay sala activa") }
                return@launch
            }

            val result = repository.pickBall(snapshot, matchId, ballId)
            applyResult(result)
        }
    }

    fun revealBallMultipliers() {
        viewModelScope.launch {
            val snapshot = _uiState.value
            val matchId = snapshot.currentMatchId
            if (matchId == null) {
                _uiState.update { it.copy(transientMessage = "No hay sala activa") }
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

    fun playBattleRound() {
        viewModelScope.launch {
            val snapshot = _uiState.value
            val matchId = snapshot.currentMatchId
            if (matchId == null) {
                _uiState.update { it.copy(transientMessage = "No hay battle activa") }
                return@launch
            }

            val result = repository.playBattleRound(snapshot, matchId)
            applyResult(result)
        }
    }

    fun resetBattleAndRoom() {
        autoRestartJob?.cancel()
        resetBattleAndRoom(autoEnterRoom = false)
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
                        statusMessage = "Paga EUR 10 para entrar en la sala."
                    ),
                    battle = BattleUiState(
                        phase = BattlePhase.LOCKED,
                        log = listOf("Nuevo ciclo listo. Vuelve al sorteo de bolas.")
                    ),
                    activeTab = MainTab.BALL_ROOM,
                    transientMessage = if (autoEnterRoom) {
                        "Partida finalizada. Abriendo nueva sala..."
                    } else {
                        "Battle cerrada. Preparado para una nueva sala."
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
            _uiState.update { it.copy(transientMessage = "Rewarded no disponible todavia.") }
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

    private fun bootstrap() {
        viewModelScope.launch {
            _uiState.update { it.copy(transientMessage = "Cargando estado...") }
            val result = repository.bootstrap(_uiState.value)
            applyResult(result)
            if (result.isSuccess) {
                loadProfile()
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val result = profileRepository.loadProfile(_uiState.value)
            result.onSuccess { _uiState.value = it }
            result.onFailure { error ->
                if (error is SessionExpiredException) {
                    expireSession(error)
                    return@onFailure
                }
                _uiState.update { state ->
                    state.copy(transientMessage = error.message ?: "No se pudo cargar el perfil")
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
        // Watch BallRoom.phase transitions
        viewModelScope.launch {
            uiState
                .map { it.ballRoom.phase }
                .distinctUntilChanged()
                .collect { phase ->
                    autoPickJob?.cancel()
                    autoBattleJob?.cancel()

                    when (phase) {
                        BallRoomPhase.PICKING -> {
                            autoPickJob = viewModelScope.launch {
                                delay(20_000)
                                val now = uiState.value
                                val userPicked = now.ballRoom.players
                                    .firstOrNull { it.isUser }?.selectedBallId != null
                                if (now.ballRoom.phase == BallRoomPhase.PICKING && !userPicked) {
                                    val available = now.ballRoom.balls.filter { !it.isPicked }
                                    if (available.isNotEmpty()) {
                                        pickBall(available.random().id)
                                    }
                                }
                            }
                        }
                        BallRoomPhase.REVEALED -> {
                            autoBattleJob = viewModelScope.launch {
                                delay(5_000)
                                if (uiState.value.ballRoom.phase == BallRoomPhase.REVEALED) {
                                    selectTab(MainTab.BALL_ROOM)
                                }
                            }
                        }
                        else -> Unit
                    }
                }
        }

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
                    autoRestartJob?.cancel()
                    if (phase == BattlePhase.FINISHED) {
                        autoRestartJob = viewModelScope.launch {
                            delay(4_500)
                            if (uiState.value.battle.phase == BattlePhase.FINISHED) {
                                resetBattleAndRoom(autoEnterRoom = true)
                            }
                        }
                    }
                }
        }
    }

    private fun startPassiveRefresh() {
        viewModelScope.launch {
            while (isActive) {
                delay(5000)
                val current = _uiState.value
                if (current.currentMatchId != null || current.activeTab == MainTab.DASHBOARD) {
                    val result = repository.refreshMatch(current)
                    result.onSuccess { newState ->
                        _uiState.value = newState
                    }
                }
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
        _uiState.update { current ->
            result.fold(
                onSuccess = { success -> success },
                onFailure = { error ->
                    if (error is SessionExpiredException) {
                        current.copy(
                            isSessionExpired = true,
                            transientMessage = error.message
                        )
                    } else {
                        current.copy(transientMessage = error.message ?: "Error de conexión")
                    }
                }
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
        return GameUiState(
            activeTab = MainTab.DASHBOARD,
            ballRoom = BallRoomUiState(
                phase = BallRoomPhase.WAITING_ENTRY,
                statusMessage = "Paga EUR 10 para entrar en la sala."
            ),
            battle = BattleUiState(
                phase = BattlePhase.LOCKED,
                log = listOf("Completa primero el sorteo de bolas.")
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
            BadgeUi("Sangre Fria", "Consigue multiplicador x10 o superior.", stats.bestMultiplier >= 10.0),
            BadgeUi("Trader Diario", "Juega 5 salas de bolas.", stats.ballRoomsPlayed >= 5),
            BadgeUi("Maestro Royale", "Mantiene winrate del 50% con 6 batallas.", stats.battlesPlayed >= 6 && winRate >= 0.5),
            BadgeUi("Bonus Hunter", "Reclama 3 rewarded ads.", stats.rewardedAdsClaimed >= 3)
        )
    }
}
