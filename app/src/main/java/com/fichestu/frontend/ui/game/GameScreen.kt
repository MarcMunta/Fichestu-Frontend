package com.fichestu.frontend.ui.game

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fichestu.frontend.R
import com.fichestu.frontend.game.GameRules
import com.fichestu.frontend.game.engine.GameEngine
import com.fichestu.frontend.game.model.BallOption
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
import com.fichestu.frontend.game.model.ProfileStats
import com.fichestu.frontend.game.model.ProfileUiState
import com.fichestu.frontend.game.model.TokenId
import com.fichestu.frontend.ui.theme.ChipRed
import com.fichestu.frontend.ui.theme.DeepBlue
import com.fichestu.frontend.ui.theme.Gold
import com.fichestu.frontend.ui.theme.GoldDark
import com.fichestu.frontend.ui.theme.NightBlue
import com.fichestu.frontend.ui.theme.PanelBlue
import com.fichestu.frontend.ui.theme.PureWhite
import com.fichestu.frontend.ui.theme.TextSecondary
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun FichestuGameScreen(
    playerName: String,
    onLogout: () -> Unit,
    viewModel: GameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(playerName) {
        viewModel.initializePlayer(playerName)
    }

    LaunchedEffect(uiState.isSessionExpired) {
        if (uiState.isSessionExpired) {
            onLogout()
        }
    }

    LaunchedEffect(uiState.transientMessage) {
        if (!uiState.transientMessage.isNullOrBlank()) {
            delay(2600)
            viewModel.consumeTransientMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DeepBlue, NightBlue)))
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        GameBackgroundPattern()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GameTopBar(
                playerName = uiState.profile.playerName,
                totalBalance = uiState.market.totalBalance,
                onLogout = onLogout
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = uiState.activeTab,
                    transitionSpec = {
                        val forward = targetState.ordinal >= initialState.ordinal
                        (slideInHorizontally(tween(260)) { if (forward) it else -it } + fadeIn(tween(200)))
                            .togetherWith(slideOutHorizontally(tween(260)) { if (forward) -it else it } + fadeOut(tween(160)))
                    },
                    label = "main_tab_animation"
                ) { tab ->
                    when (tab) {
                        MainTab.DASHBOARD -> DashboardTab(
                            market = uiState.market,
                            rewardedAvailable = uiState.rewardedAvailable,
                            rewardedCooldownSec = uiState.rewardedCooldownSec,
                            onSelectToken = viewModel::selectToken,
                            onBuy = viewModel::buySelectedToken,
                            onSell = viewModel::sellSelectedToken,
                            onEnterBallRoom = viewModel::enterBallRoom,
                            onClaimRewarded = viewModel::claimRewardedAd
                        )

                        MainTab.BALL_ROOM -> BallRoomFlow(
                            ballRoom = uiState.ballRoom,
                            cashBalance = uiState.market.cashBalance,
                            onEnterRoom = viewModel::enterBallRoom,
                            onPickBall = viewModel::pickBall,
                            onRevealMultipliers = viewModel::revealBallMultipliers,
                            onOpenBattle = { viewModel.selectTab(MainTab.BATTLE) }
                        )

                        MainTab.BATTLE -> BattleFlow(
                            battle = uiState.battle,
                            market = uiState.market,
                            onSelectAction = viewModel::chooseBattleAction,
                            onPlayRound = viewModel::playBattleRound,
                            onResetCycle = viewModel::resetBattleAndRoom
                        )

                        MainTab.MINIGAMES -> MinigamesTab(
                            cashBalance = uiState.market.cashBalance,
                            onResult = viewModel::applyMinigameResult
                        )

                        MainTab.PROFILE -> ProfileTab(
                            profile = uiState.profile,
                            onUsernameChange = viewModel::updateProfileUsername,
                            onEmailChange = viewModel::updateProfileEmail,
                            onSaveProfile = viewModel::saveProfile,
                            onCurrentPasswordChange = viewModel::updateCurrentPassword,
                            onNewPasswordChange = viewModel::updateNewPassword,
                            onConfirmPasswordChange = viewModel::updateConfirmPassword,
                            onChangePassword = viewModel::changePassword,
                            onLogout = onLogout
                        )
                    }
                }
            }

            BottomGameNav(
                activeTab = uiState.activeTab,
                onSelect = viewModel::selectTab
            )
        }

        AnimatedVisibility(
            visible = !uiState.transientMessage.isNullOrBlank(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 82.dp),
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(180))
        ) {
            StatusTicker(
                text = uiState.transientMessage.orEmpty(),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .heightIn(min = 42.dp)
            )
        }
    }
}

@Composable
private fun GameTopBar(
    playerName: String,
    totalBalance: Double,
    onLogout: () -> Unit
) {
    ArcadePanel(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Gold)
                    .border(2.dp, GoldDark, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = playerName.take(1).uppercase(Locale.US),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = NightBlue,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                BubbleText(
                    text = playerName,
                    style = MaterialTheme.typography.headlineSmall,
                    fillColor = PureWhite,
                    outlineColor = DeepBlue
                )
                Text(
                    text = "Saldo total: ${formatCurrency(totalBalance)}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Gold,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            ArcadeSecondaryButton(
                text = "SALIR",
                modifier = Modifier.width(96.dp),
                onClick = onLogout
            )
        }
    }
}

@Composable
private fun DashboardTab(
    market: MarketUiState,
    rewardedAvailable: Boolean,
    rewardedCooldownSec: Int,
    onSelectToken: (TokenId) -> Unit,
    onBuy: () -> Unit,
    onSell: () -> Unit,
    onEnterBallRoom: () -> Unit,
    onClaimRewarded: () -> Unit
) {
    val selected = market.selectedMarketToken ?: return

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 4.dp)
    ) {
        item {
            BubbleText(
                text = "Mercado de Fichas",
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 30.sp),
                fillColor = Gold,
                outlineColor = DeepBlue
            )
        }

        item {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isWide = maxWidth >= 760.dp
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SelectedTokenPanel(
                            token = selected,
                            resetCountdown = market.resetCountdownLabel,
                            modifier = Modifier.weight(1.5f)
                        )
                        TokenSidePanel(
                            tokens = market.tokens,
                            selectedId = market.selectedToken,
                            onSelectToken = onSelectToken,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SelectedTokenPanel(
                            token = selected,
                            resetCountdown = market.resetCountdownLabel,
                            modifier = Modifier.fillMaxWidth()
                        )
                        TokenSidePanel(
                            tokens = market.tokens,
                            selectedId = market.selectedToken,
                            onSelectToken = onSelectToken,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        item {
            ArcadePanel {
                Text(
                    text = "Acciones Rapidas",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ArcadePrimaryButton(
                        text = "BUY 1",
                        modifier = Modifier.weight(1f),
                        onClick = onBuy
                    )
                    ArcadeSecondaryButton(
                        text = "SELL 1",
                        modifier = Modifier.weight(1f),
                        onClick = onSell
                    )
                }
                Spacer(Modifier.height(10.dp))
                ArcadePrimaryButton(
                    text = if (rewardedAvailable) {
                        "REWARDED +EUR 25"
                    } else {
                        "REWARDED ${rewardedCooldownSec}s"
                    },
                    enabled = rewardedAvailable,
                    onClick = onClaimRewarded
                )
                Spacer(Modifier.height(10.dp))
                ArcadeSecondaryButton(
                    text = "ENTRAR SORTEO BOLAS (EUR ${GameRules.BALL_ENTRY_COST.toInt()})",
                    onClick = onEnterBallRoom
                )
            }
        }

        item {
            ArcadePanel {
                Text(
                    text = "Reglas Criticas del Mercado",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Gold,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(Modifier.height(8.dp))
                RuleLine("Cron diario 00:00 vende todas tus fichas a saldo.")
                RuleLine("Tras reset diario, cada precio base cae entre EUR 5 y EUR 500.")
                RuleLine("El ganador del Battle aplica su multiplicador al token seleccionado.")
                RuleLine("Sala de bolas: 10 jugadores y 50 bolas unicas.")
            }
        }
    }
}

@Composable
private fun SelectedTokenPanel(
    token: MarketToken,
    resetCountdown: String,
    modifier: Modifier = Modifier
) {
    val positive = token.changePercent >= 0

    ArcadePanel(modifier = modifier) {
        BubbleText(
            text = token.displayName,
            style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp),
            fillColor = Gold,
            outlineColor = DeepBlue
        )
        Text(
            text = token.ticker,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp
            )
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = formatCurrency(token.currentPrice),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Text(
                    text = formatPercent(token.changePercent),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (positive) Gold else ChipRed,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            StatusTicker(
                text = "Reset: $resetCountdown",
                modifier = Modifier.width(148.dp)
            )
        }
        Spacer(Modifier.height(10.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(18.dp),
            color = NightBlue.copy(alpha = 0.58f)
        ) {
            TokenSparkChart(
                values = token.history,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Holdings: ${token.holdings}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = PureWhite,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = "Valor: ${formatCurrency(token.portfolioValue)}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Gold,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun TokenSidePanel(
    tokens: List<MarketToken>,
    selectedId: TokenId,
    onSelectToken: (TokenId) -> Unit,
    modifier: Modifier = Modifier
) {
    ArcadePanel(modifier = modifier) {
        Text(
            text = "Panel Lateral",
            style = MaterialTheme.typography.titleMedium.copy(
                color = Gold,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tokens.forEach { token ->
                TokenListItem(
                    token = token,
                    selected = token.id == selectedId,
                    onClick = { onSelectToken(token.id) }
                )
            }
        }
    }
}

@Composable
private fun TokenListItem(
    token: MarketToken,
    selected: Boolean,
    onClick: () -> Unit
) {
    val changePositive = token.changePercent >= 0
    val containerColor = if (selected) Gold.copy(alpha = 0.16f) else DeepBlue.copy(alpha = 0.42f)
    val borderColor = if (selected) Gold else PureWhite.copy(alpha = 0.12f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = token.displayName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = token.ticker,
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(token.currentPrice),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Gold,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = formatPercent(token.changePercent),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (changePositive) Gold else ChipRed,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun BallRoomTab(
    ballRoom: BallRoomUiState,
    cashBalance: Double,
    onEnterRoom: () -> Unit,
    onPickBall: (Int) -> Unit,
    onRevealMultipliers: () -> Unit,
    onOpenBattle: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 4.dp)
    ) {
        item {
            BubbleText(
                text = "Sorteo de Bolas",
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 30.sp),
                fillColor = Gold,
                outlineColor = DeepBlue
            )
        }

        item {
            ArcadePanel {
                Text(
                    text = "Entrada: EUR ${GameRules.BALL_ENTRY_COST.toInt()} | Sala: ${GameRules.ROOM_SIZE} jugadores | Bolas: ${GameRules.BALL_COUNT}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Multiplicadores ocultos entre x${GameRules.MULTIPLIER_MIN} y x${GameRules.MULTIPLIER_MAX.toInt()}.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
                Spacer(Modifier.height(6.dp))
                StatusTicker(
                    text = ballRoom.statusMessage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 42.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Saldo actual: ${formatCurrency(cashBalance)}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Gold,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        if (ballRoom.phase == BallRoomPhase.WAITING_ENTRY) {
            item {
                ArcadePrimaryButton(
                    text = "PAGAR ENTRADA Y ABRIR SALA",
                    onClick = onEnterRoom
                )
            }
        } else {
            item {
                ArcadePanel {
                    Text(
                        text = "Jugadores",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Gold,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ballRoom.players.forEach { player ->
                            PlayerBallStatusItem(
                                nickname = player.nickname,
                                isUser = player.isUser,
                                ballId = player.selectedBallId,
                                multiplier = player.multiplier
                            )
                        }
                    }
                }
            }

            item {
                ArcadePanel {
                    Text(
                        text = "Elige tu bola",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = PureWhite,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    BallGrid(
                        balls = ballRoom.balls,
                        phase = ballRoom.phase,
                        onPickBall = onPickBall
                    )
                }
            }

            if (ballRoom.phase == BallRoomPhase.PICKING && ballRoom.canRevealBattle) {
                item {
                    ArcadePrimaryButton(
                        text = "REVELAR MULTIPLICADORES",
                        onClick = onRevealMultipliers
                    )
                }
            }

            if (ballRoom.phase == BallRoomPhase.REVEALED) {
                item {
                    ArcadePrimaryButton(
                        text = "IR A BATTLE ROYALE",
                        onClick = onOpenBattle
                    )
                }
            }
        }
    }
}

@Composable
private fun BallGrid(
    balls: List<BallOption>,
    phase: BallRoomPhase,
    onPickBall: (Int) -> Unit
) {
    val engine = GameEngine()

    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = true
    ) {
        items(balls.size) { index ->
            val ball = balls[index]
            val isUserPick = ball.pickedBy == GameRules.USER_PLAYER_ID
            val enabled = !ball.isPicked && phase == BallRoomPhase.PICKING

            Surface(
                modifier = Modifier
                    .size(58.dp)
                    .clickable(enabled = enabled) { onPickBall(ball.id) },
                shape = CircleShape,
                color = when {
                    isUserPick -> Gold
                    ball.isPicked -> ChipRed.copy(alpha = 0.75f)
                    else -> DeepBlue
                }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(2.dp, Gold.copy(alpha = 0.45f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val label = when {
                        phase == BallRoomPhase.REVEALED && ball.isPicked -> "x${engine.formatMultiplier(ball.multiplier)}"
                        else -> ball.id.toString()
                    }

                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = if (isUserPick) NightBlue else PureWhite,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerBallStatusItem(
    nickname: String,
    isUser: Boolean,
    ballId: Int?,
    multiplier: Double?
) {
    val border = if (isUser) Gold else PureWhite.copy(alpha = 0.18f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = DeepBlue.copy(alpha = 0.55f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, border, RoundedCornerShape(14.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isUser) "$nickname (Tu)" else nickname,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = PureWhite,
                    fontWeight = FontWeight.SemiBold
                )
            )

            val status = when {
                multiplier != null -> "x${"%.2f".format(Locale.US, multiplier)}"
                ballId != null -> "Bola $ballId"
                else -> "Sin bola"
            }
            Text(
                text = status,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (multiplier != null) Gold else TextSecondary,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun BattleTab(
    battle: BattleUiState,
    selectedTokenId: TokenId,
    onSelectAction: (BattleCardType) -> Unit,
    onPlayRound: () -> Unit,
    onResetCycle: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 4.dp)
    ) {
        item {
            BubbleText(
                text = "Battle Royale",
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 30.sp),
                fillColor = Gold,
                outlineColor = DeepBlue
            )
        }

        item {
            ArcadePanel {
                val alive = battle.players.count { it.isAlive }
                Text(
                    text = "Ronda ${battle.round} | Vivos: $alive | Token objetivo: ${selectedTokenId.name}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(Modifier.height(8.dp))
                if (battle.phase == BattlePhase.LOCKED) {
                    Text(
                        text = "Completa antes el sorteo de bolas para desbloquear esta fase.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                    )
                } else {
                    Text(
                        text = "HP inicial obligatorio: ${GameRules.BATTLE_INITIAL_HP}. Ataques entre ${GameRules.BATTLE_ATTACK_MIN} y ${GameRules.BATTLE_ATTACK_MAX}.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            }
        }

        if (battle.phase != BattlePhase.LOCKED) {
            item {
                ArcadePanel {
                    Text(
                        text = "Elige tu carta",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Gold,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionCardButton(
                            label = "ATAQUE",
                            selected = battle.selectedAction == BattleCardType.ATTACK,
                            modifier = Modifier.weight(1f),
                            onClick = { onSelectAction(BattleCardType.ATTACK) }
                        )
                        ActionCardButton(
                            label = "ESCUDO",
                            selected = battle.selectedAction == BattleCardType.SHIELD,
                            modifier = Modifier.weight(1f),
                            onClick = { onSelectAction(BattleCardType.SHIELD) }
                        )
                        ActionCardButton(
                            label = "REBOTE",
                            selected = battle.selectedAction == BattleCardType.REBOUND,
                            modifier = Modifier.weight(1f),
                            onClick = { onSelectAction(BattleCardType.REBOUND) }
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    ArcadePrimaryButton(
                        text = if (battle.phase == BattlePhase.FINISHED) "BATTLE FINALIZADO" else "JUGAR RONDA",
                        enabled = battle.phase != BattlePhase.FINISHED,
                        onClick = onPlayRound
                    )
                }
            }

            if (battle.interstitialAvailable && battle.phase != BattlePhase.FINISHED) {
                item {
                    ArcadePanel {
                        Text(
                            text = "Interstitial listo: aqui puedes lanzar anuncio entre rondas.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Gold,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            item {
                ArcadePanel {
                    Text(
                        text = "Estado de jugadores",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = PureWhite,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        battle.players.forEach { player ->
                            BattlePlayerItem(player = player)
                        }
                    }
                }
            }

            item {
                ArcadePanel {
                    Text(
                        text = "Log del combate",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Gold,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    val lines = battle.log.takeLast(10).reversed()
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        lines.forEach { line ->
                            Text(
                                text = line,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                        }
                    }
                }
            }
        }

        if (battle.phase == BattlePhase.FINISHED) {
            item {
                ArcadePanel {
                    BubbleText(
                        text = battle.winnerName?.let { "Winner: $it" } ?: "Sin ganador",
                        style = MaterialTheme.typography.headlineMedium,
                        fillColor = Gold,
                        outlineColor = DeepBlue
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (battle.winningMultiplier != null) {
                            "Impacto aplicado al mercado con x${"%.2f".format(Locale.US, battle.winningMultiplier)}."
                        } else {
                            "No se aplico impacto por empate total."
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(color = PureWhite)
                    )
                    Spacer(Modifier.height(10.dp))
                    ArcadeSecondaryButton(
                        text = "NUEVO CICLO",
                        onClick = onResetCycle
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionCardButton(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    if (selected) {
        ArcadePrimaryButton(
            text = label,
            modifier = modifier,
            onClick = onClick
        )
    } else {
        ArcadeSecondaryButton(
            text = label,
            modifier = modifier,
            onClick = onClick
        )
    }
}

@Composable
private fun BattlePlayerItem(player: BattlePlayer) {
    val hpFraction = (player.hp.toFloat() / GameRules.BATTLE_INITIAL_HP.toFloat()).coerceIn(0f, 1f)
    val hpColor = when {
        player.hp <= 0 -> ChipRed.copy(alpha = 0.8f)
        player.hp <= 15 -> ChipRed
        player.hp <= 30 -> Gold
        else -> Gold
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = DeepBlue.copy(alpha = 0.42f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (player.isUser) Gold else PureWhite.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (player.isUser) "${player.nickname} (Tu)" else player.nickname,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = "${player.hp}/${GameRules.BATTLE_INITIAL_HP} HP",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = if (player.hp > 0) Gold else ChipRed,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(NightBlue)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(hpFraction)
                        .clip(RoundedCornerShape(50))
                        .background(hpColor)
                )
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = "Multiplicador: x${"%.2f".format(Locale.US, player.multiplier)}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
private fun ProfileTab(
    profile: ProfileUiState,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onSaveProfile: () -> Unit,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 4.dp)
    ) {
        item {
            BubbleText(
                text = "Perfil",
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 30.sp),
                fillColor = Gold,
                outlineColor = DeepBlue
            )
        }

        item {
            ArcadePanel {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!profile.profilePicUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = profile.profilePicUrl,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .border(3.dp, GoldDark, CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(Gold)
                                .border(3.dp, GoldDark, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile.username.take(1).uppercase(Locale.US),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = NightBlue,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        BubbleText(
                            text = profile.username.ifBlank { profile.playerName },
                            style = MaterialTheme.typography.headlineMedium,
                            fillColor = PureWhite,
                            outlineColor = DeepBlue
                        )
                        Text(
                            text = profile.email,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary
                            )
                        )
                        Text(
                            text = "Rol: ${profile.role}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Gold,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        item {
            ArcadePanel {
                Text(
                    text = "Editar perfil",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Gold,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(Modifier.height(8.dp))
                ArcadeTextField(
                    value = profile.editUsername,
                    label = "Username",
                    leading = Icons.Default.Person,
                    keyboardType = KeyboardType.Text,
                    onValueChange = onUsernameChange
                )
                Spacer(Modifier.height(8.dp))
                ArcadeTextField(
                    value = profile.editEmail,
                    label = "Email",
                    leading = Icons.Default.Email,
                    keyboardType = KeyboardType.Email,
                    onValueChange = onEmailChange
                )
                Spacer(Modifier.height(10.dp))
                ArcadePrimaryButton(
                    text = if (profile.isSavingProfile) "GUARDANDO..." else "GUARDAR CAMBIOS",
                    enabled = !profile.isSavingProfile,
                    onClick = onSaveProfile
                )
            }
        }

        item {
            ArcadePanel {
                Text(
                    text = "Cambiar contraseña",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Gold,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(Modifier.height(8.dp))
                ArcadeTextField(
                    value = profile.currentPassword,
                    label = "Contrasena actual (si tienes una)",
                    leading = Icons.Default.Lock,
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                    onValueChange = onCurrentPasswordChange
                )
                Spacer(Modifier.height(8.dp))
                ArcadeTextField(
                    value = profile.newPassword,
                    label = "Nueva contraseña",
                    leading = Icons.Default.Lock,
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                    onValueChange = onNewPasswordChange
                )
                Spacer(Modifier.height(8.dp))
                ArcadeTextField(
                    value = profile.confirmPassword,
                    label = "Repite contraseña",
                    leading = Icons.Default.Lock,
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                    onValueChange = onConfirmPasswordChange
                )
                Spacer(Modifier.height(10.dp))
                ArcadePrimaryButton(
                    text = if (profile.isSavingPassword) "ACTUALIZANDO..." else "ACTUALIZAR CONTRASEÑA",
                    enabled = !profile.isSavingPassword,
                    onClick = onChangePassword
                )
            }
        }

        item {
            ArcadeSecondaryButton(
                text = "CERRAR SESION",
                onClick = onLogout
            )
        }
    }
}

@Composable
private fun ArcadeTextField(
    value: String,
    label: String,
    leading: ImageVector,
    keyboardType: KeyboardType,
    isPassword: Boolean = false,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(leading, contentDescription = null) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = PureWhite,
            unfocusedTextColor = PureWhite,
            focusedBorderColor = Gold,
            unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f),
            focusedLabelColor = Gold,
            unfocusedLabelColor = TextSecondary,
            focusedLeadingIconColor = Gold,
            unfocusedLeadingIconColor = TextSecondary
        )
    )
}

@Composable
private fun RuleLine(text: String) {
    Text(
        text = "- $text",
        style = MaterialTheme.typography.bodySmall.copy(
            color = TextSecondary,
            lineHeight = 18.sp
        )
    )
}

@Composable
private fun BottomGameNav(
    activeTab: MainTab,
    onSelect: (MainTab) -> Unit
) {
    val items = listOf(
        BottomItem(MainTab.DASHBOARD, stringResource(R.string.nav_market), Icons.Default.Home),
        BottomItem(MainTab.BALL_ROOM, stringResource(R.string.nav_balls), Icons.Default.Casino),
        BottomItem(MainTab.BATTLE, stringResource(R.string.nav_battle), Icons.Default.SportsEsports),
        BottomItem(MainTab.MINIGAMES, stringResource(R.string.nav_minigames), Icons.Default.PlayArrow),
        BottomItem(MainTab.PROFILE, stringResource(R.string.nav_profile), Icons.Default.Person)
    )

    ArcadePanel(contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.forEach { item ->
                val selected = item.tab == activeTab
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clickable { onSelect(item.tab) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (selected) Gold.copy(alpha = 0.24f) else DeepBlue.copy(alpha = 0.45f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                1.dp,
                                if (selected) Gold else PureWhite.copy(alpha = 0.12f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = if (selected) Gold else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (selected) PureWhite else TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

private data class BottomItem(
    val tab: MainTab,
    val label: String,
    val icon: ImageVector
)

@Composable
private fun GameBackgroundPattern() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val spacing = 84.dp.toPx()
        var x = -size.height
        while (x < size.width + size.height) {
            drawLine(
                color = ChipRed.copy(alpha = 0.10f),
                start = Offset(x, 0f),
                end = Offset(x + size.height, size.height),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            x += spacing
        }

        val spacing2 = 110.dp.toPx()
        var x2 = 0f
        while (x2 < size.width + size.height) {
            drawLine(
                color = Gold.copy(alpha = 0.05f),
                start = Offset(x2, 0f),
                end = Offset(x2 - size.height, size.height),
                strokeWidth = 1.4.dp.toPx(),
                cap = StrokeCap.Round
            )
            x2 += spacing2
        }
    }
}
