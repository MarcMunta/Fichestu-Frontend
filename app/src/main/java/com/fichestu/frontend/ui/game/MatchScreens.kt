package com.fichestu.frontend.ui.game

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fichestu.frontend.data.i18n.AppI18n
import com.fichestu.frontend.game.GameRules
import com.fichestu.frontend.game.engine.GameEngine
import com.fichestu.frontend.game.model.BallOption
import com.fichestu.frontend.game.model.BallPlayer
import com.fichestu.frontend.game.model.BallRoomPhase
import com.fichestu.frontend.game.model.BallRoomUiState
import com.fichestu.frontend.game.model.BattleCardType
import com.fichestu.frontend.game.model.BattleHandCard
import com.fichestu.frontend.game.model.BattlePhase
import com.fichestu.frontend.game.model.BattlePlayer
import com.fichestu.frontend.game.model.BattleUiState
import com.fichestu.frontend.game.model.MarketUiState
import com.fichestu.frontend.game.model.TokenId
import com.fichestu.frontend.game.model.AppLanguage
import com.fichestu.frontend.ui.theme.AliveGreen
import com.fichestu.frontend.ui.theme.ChipRed
import com.fichestu.frontend.ui.theme.ChipRedDark
import com.fichestu.frontend.ui.theme.DeepBlue
import com.fichestu.frontend.ui.theme.Gold
import com.fichestu.frontend.ui.theme.GoldDark
import com.fichestu.frontend.ui.theme.GoldLight
import com.fichestu.frontend.ui.theme.NightBlue
import com.fichestu.frontend.ui.theme.PanelBlue
import com.fichestu.frontend.ui.theme.PureWhite
import com.fichestu.frontend.ui.theme.ReflectPurple
import com.fichestu.frontend.ui.theme.ShieldBlue
import com.fichestu.frontend.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private fun t(key: String, language: AppLanguage): String = AppI18n.text(key, language)

/* ============================================================
   FICHESTU - Match flow screens (Lobby -> Bolas -> Reveal -> Cards -> Victoria)
   Wired to GameViewModel state. Backend-ready: solo lee de UiState.
   ============================================================ */

@Composable
fun BallRoomFlow(
    ballRoom: BallRoomUiState,
    cashBalance: Double,
    language: AppLanguage,
    isInRoom: Boolean,
    onEnterRoom: () -> Unit,
    onCancelMatchmaking: () -> Unit,
    onPickBall: (Int) -> Unit,
    onFinishSelection: () -> Unit,
    onSelectionTimeout: () -> Unit,
    onMatchmakingFinished: () -> Unit,
    onOpenBattle: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (ballRoom.phase) {
        BallRoomPhase.WAITING_ENTRY, BallRoomPhase.MATCHMAKING -> LobbyView(
            ballRoom = ballRoom,
            cashBalance = cashBalance,
            language = language,
            statusMessage = ballRoom.statusMessage,
            isInRoom = isInRoom,
            onEnterRoom = onEnterRoom,
            onCancelMatchmaking = onCancelMatchmaking,
            onMatchmakingFinished = onMatchmakingFinished,
            modifier = modifier
        )
        BallRoomPhase.PICKING -> {
            // Auto-reveal cuando hay 10 jugadores con bola elegida
            BallsPoolView(
                ballRoom = ballRoom,
                language = language,
                onPickBall = onPickBall,
                onConfirm = onFinishSelection,
                onTimeout = onSelectionTimeout,
                modifier = modifier
            )
        }
        BallRoomPhase.REVEALED, BallRoomPhase.READY_FOR_BATTLE -> RevealView(
            ballRoom = ballRoom,
            language = language,
            onContinueToBattle = onOpenBattle,
            modifier = modifier
        )
    }
}

@Composable
fun BattleFlow(
    battle: BattleUiState,
    market: MarketUiState,
    language: AppLanguage,
    onSelectAction: (BattleCardType) -> Unit,
    onSelectCard: (Long) -> Unit,
    onSelectTarget: (String) -> Unit,
    onPlayRound: () -> Unit,
    onSelectToken: (TokenId) -> Unit,
    onResetCycle: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (battle.phase) {
        BattlePhase.LOCKED -> LockedView(language = language, modifier = modifier)
        BattlePhase.READY, BattlePhase.IN_PROGRESS -> ArenaView(
            battle = battle,
            language = language,
            onSelectAction = onSelectAction,
            onSelectCard = onSelectCard,
            onSelectTarget = onSelectTarget,
            onPlayRound = onPlayRound,
            modifier = modifier
        )
        BattlePhase.DEFEATED -> DefeatView(
            battle = battle,
            language = language,
            onReturnToEntry = onResetCycle,
            modifier = modifier
        )
        BattlePhase.FINISHED -> VictoryChoiceView(
            battle = battle,
            market = market,
            language = language,
            onSelectToken = onSelectToken,
            onResetCycle = onResetCycle,
            modifier = modifier
        )
    }
}

@Composable
private fun LockedView(language: AppLanguage, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        AmbientBackground(particleCount = 10)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            DisplayWhite(text = t("locked", language), fontSize = 54, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            DisplayGold(
                text = t("battle_locked", language),
                fontSize = 28,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = t("battle_locked_hint", language),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}


private val PLAYER_PALETTE = listOf(
    Gold, ChipRed, ShieldBlue, ReflectPurple, AliveGreen,
    Color(0xFFF26B76), Color(0xFFFFE873), Color(0xFFA8C0E0),
    Color(0xFF3D6098), Color(0xFFC9A300)
)

private fun colorFor(playerId: String?, players: List<com.fichestu.frontend.game.model.BallPlayer>): Color {
    if (playerId == null) return DeepBlue
    if (playerId == GameRules.USER_PLAYER_ID) return Gold
    val idx = players.indexOfFirst { it.id == playerId }.coerceAtLeast(0)
    return PLAYER_PALETTE[idx % PLAYER_PALETTE.size]
}

private fun initialsFor(nickname: String): String =
    nickname.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString("")
        .ifBlank { nickname.take(2).uppercase() }

/* ============================================================
   LOBBY (BallRoomPhase.WAITING_ENTRY)
   ============================================================ */

@Composable
fun LobbyView(
    ballRoom: BallRoomUiState,
    cashBalance: Double,
    language: AppLanguage,
    statusMessage: String,
    isInRoom: Boolean = false,
    onEnterRoom: () -> Unit,
    onCancelMatchmaking: () -> Unit,
    onMatchmakingFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var nowMs by remember { mutableStateOf(System.currentTimeMillis()) }
    var rulesExpanded by remember { mutableStateOf(false) }
    var activeDeadlineMs by remember { mutableStateOf<Long?>(null) }
    val isMatchmaking = ballRoom.phase == BallRoomPhase.MATCHMAKING
    val hasActiveLobbyMatch = isInRoom || isMatchmaking
    val maxCountdown = 15
    val serverDeadline = ballRoom.selectionDeadlineEpochMs
    val countdown = if (hasActiveLobbyMatch) {
        val deadline = activeDeadlineMs ?: serverDeadline ?: nowMs + maxCountdown * 1000L
        ((deadline - nowMs).coerceAtLeast(0L) / 1000L).toInt().coerceIn(0, maxCountdown)
    } else {
        maxCountdown
    }
    val players = ballRoom.players
    val displayedPlayerCount = maxOf(players.size, if (hasActiveLobbyMatch) 1 else 0)

    LaunchedEffect(hasActiveLobbyMatch, ballRoom.selectionDeadlineEpochMs) {
        if (hasActiveLobbyMatch && activeDeadlineMs == null) {
            activeDeadlineMs = ballRoom.selectionDeadlineEpochMs
                ?: (System.currentTimeMillis() + maxCountdown * 1000L)
        }
        if (!hasActiveLobbyMatch) {
            activeDeadlineMs = null
        }
        while (hasActiveLobbyMatch) {
            nowMs = System.currentTimeMillis()
            delay(250)
        }
    }

    LaunchedEffect(hasActiveLobbyMatch, countdown) {
        if (hasActiveLobbyMatch && countdown == 0) {
            onMatchmakingFinished()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AmbientBackground(particleCount = 14)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DisplayGold(
                text = t("room_title", language),
                fontSize = 34,
                textAlign = TextAlign.Center
            )
            Text(
                    text = "${t("entry", language)}: ${GameRules.BALL_ENTRY_COST.toInt()} FTC",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )

            CountdownRing(
                value = countdown,
                max = maxCountdown,
                language = language,
                label = when {
                    !hasActiveLobbyMatch -> t("pay_to_enter", language)
                    countdown == 0 -> t("ready", language)
                    else -> t("searching", language)
                },
                centerText = when {
                    !hasActiveLobbyMatch -> "${GameRules.BALL_ENTRY_COST.toInt()} FTC"
                    countdown == 0 -> "..."
                    else -> "${countdown}s"
                },
                helperText = when {
                    !hasActiveLobbyMatch -> null
                    countdown == 0 -> t("preparing", language)
                    else -> t("cancel", language)
                },
                enabled = hasActiveLobbyMatch || cashBalance >= GameRules.BALL_ENTRY_COST,
                onClick = if (hasActiveLobbyMatch) onCancelMatchmaking else onEnterRoom
            )

            // 10 player slots in 5x2 grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(2) { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(5) { col ->
                            val idx = row * 5 + col
                            val player = players.getOrNull(idx)
                            PlayerSlot(
                                initials = if (idx == 0) t("you", language).take(2).uppercase() else "P${idx + 1}",
                                color = PLAYER_PALETTE[idx],
                                ready = player != null || (hasActiveLobbyMatch && idx == 0),
                                isYou = player?.isUser == true || (hasActiveLobbyMatch && idx == 0),
                                language = language,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            HowToPlayTogglePanel(
                expanded = rulesExpanded,
                language = language,
                onToggle = { rulesExpanded = !rulesExpanded },
                modifier = Modifier.fillMaxWidth()
            )

            if (false) PremiumPanel(modifier = Modifier.fillMaxWidth(), glow = true) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Gold),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "?",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = NightBlue,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = t("how_to_play", language),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = PureWhite,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    RuleNumLine(1, t("rules_ball_2", language))
                    RuleNumLine(2, t("battle_subtitle", language))
                    RuleNumLine(3, t("rules_ball_3", language))
                    RuleNumLine(4, t("matchmaking_rule", language))
                    RuleNumLine(5, t("rules_ball_1", language))
                }
            }

            Spacer(Modifier.weight(1f))

            Text(
                text = if (hasActiveLobbyMatch) {
                    "${t("waiting_players_full", language)}: $displayedPlayerCount/10."
                } else {
                    AppI18n.message(statusMessage, language) ?: statusMessage
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                    text = "${t("balance", language)}: ${"%.2f".format(cashBalance)} FTC",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Gold,
                    fontWeight = FontWeight.ExtraBold
                )
            )

            if (hasActiveLobbyMatch) {
                BigPushButtonInternal(
                    text = t("cancel_matchmaking", language),
                    color = ChipRed,
                    onClick = onCancelMatchmaking,
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (cashBalance < GameRules.BALL_ENTRY_COST) {
                Text(
                    text = t("not_enough_entry", language),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ChipRed,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun HowToPlayTogglePanel(
    expanded: Boolean,
    language: AppLanguage,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    PremiumPanel(modifier = modifier.clickable(onClick = onToggle), glow = true) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Gold),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "?",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = NightBlue,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = t("how_to_play", language),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = PureWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
                Text(
                    text = if (expanded) t("hide", language) else t("view", language),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Gold,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }

            Spacer(Modifier.height(6.dp))
            if (expanded) {
                RuleNumLine(1, t("rules_ball_1", language))
                RuleNumLine(2, t("rules_ball_2", language))
                RuleNumLine(3, t("rules_ball_3", language))
            } else {
                Text(
                    text = t("open_rules", language),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun RuleNumLine(num: Int, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = num.toString(),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Gold,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 18.sp
                )
            )
        }
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextSecondary,
                lineHeight = 16.sp,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
private fun CountdownRing(
    value: Int,
    max: Int,
    language: AppLanguage,
    label: String,
    centerText: String,
    helperText: String? = null,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val safeMax = max.coerceAtLeast(1)
    val pct = value.coerceIn(0, safeMax).toFloat() / safeMax.toFloat()
    val clickModifier = if (enabled) Modifier.clickable(onClick = onClick) else Modifier.alpha(0.62f)

    Box(
        modifier = Modifier
            .size(154.dp)
            .clip(CircleShape)
            .then(clickModifier),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(154.dp)) {
            drawArc(
                color = PureWhite.copy(alpha = 0.10f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 10.dp.toPx())
            )
            drawArc(
                brush = Brush.sweepGradient(listOf(Gold, ChipRed, Gold)),
                startAngle = -90f,
                sweepAngle = 360f * pct,
                useCenter = false,
                style = Stroke(width = 10.dp.toPx())
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Eyebrow(text = label)
            DisplayGold(
                text = centerText,
                fontSize = if (centerText.endsWith("s")) 48 else 28,
                textAlign = TextAlign.Center
            )
            if (!helperText.isNullOrBlank()) {
                Text(
                    text = helperText,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.4.sp
                    )
                )
            }
            if (!enabled && !centerText.endsWith("s")) {
                Text(
                    text = t("without_balance", language),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ChipRed,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }
        }
    }
}

@Composable
private fun PlayerSlot(
    initials: String,
    color: Color,
    ready: Boolean,
    isYou: Boolean,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (ready) {
                    Brush.verticalGradient(
                        listOf(Gold.copy(alpha = 0.10f), NightBlue.copy(alpha = 0.6f))
                    )
                } else {
                    Brush.verticalGradient(listOf(NightBlue, NightBlue))
                }
            )
            .border(
                1.dp,
                if (ready) Gold.copy(alpha = 0.4f) else PureWhite.copy(alpha = 0.08f),
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(color, color.copy(alpha = 0.7f))))
                .border(
                    2.dp,
                    if (isYou) Gold else PureWhite.copy(alpha = 0.2f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = PureWhite,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp
                )
            )
        }
        Text(
            text = if (ready) t("ready_short", language) else t("wait_short", language),
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (ready) AliveGreen else TextSecondary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 9.sp,
                letterSpacing = 1.5.sp
            )
        )
    }
}

/* ============================================================
   BALLS POOL (BallRoomPhase.PICKING)
   ============================================================ */

@Composable
fun BallsPoolView(
    ballRoom: BallRoomUiState,
    language: AppLanguage,
    onPickBall: (Int) -> Unit,
    onConfirm: () -> Unit,
    onTimeout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timer = rememberBallPickCountdown(ballRoom.phase)
    val userPick = ballRoom.balls.firstOrNull { it.id == ballRoom.pendingSelectedBallId }
    val hasCommittedPick = false
    val ballsLeft = ballRoom.balls.count { !it.isPicked }
    var timeoutHandled by remember(ballRoom.phase) { mutableStateOf(false) }

    LaunchedEffect(timer) {
        if (timer == 0 && !timeoutHandled) {
            timeoutHandled = true
            onTimeout()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AmbientBackground(particleCount = 26)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.radialGradient(
                        listOf(
                            Gold.copy(alpha = 0.16f),
                            DeepBlue.copy(alpha = 0.86f),
                            NightBlue.copy(alpha = 0.98f)
                        ),
                        radius = 1_250f
                    )
                )
                .border(1.dp, Gold.copy(alpha = 0.28f), RoundedCornerShape(18.dp))
        ) {
            FloorStars()

            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    DisplayGold(text = t("choose_ball", language), fontSize = 34)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = t("choose_ball_hint", language),
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                    Text(
            text = AppI18n.message(ballRoom.statusMessage, language) ?: ballRoom.statusMessage,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PureWhite.copy(alpha = 0.72f),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
                BallsHud(timer = timer, ballsLeft = ballsLeft, language = language)
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 18.dp, end = 18.dp, top = 96.dp, bottom = 100.dp)
            ) {
                val fieldW = maxWidth
                val fieldH = maxHeight
                val nodeSize = if (maxHeight > 360.dp) 58.dp else 50.dp

                val positions = remember(ballRoom.balls.size) {
                    scatteredPositions(ballRoom.balls.size, seed = 42)
                }

                ballRoom.balls.forEachIndexed { idx, ball ->
                    val (rx, ry) = positions.getOrNull(idx) ?: (0.5f to 0.5f)
                    val xDp = (fieldW - nodeSize) * rx
                    val yDp = (fieldH - nodeSize) * ry
                    BallNode(
                        ball = ball,
                        ownerColor = colorFor(ball.pickedBy, ballRoom.players),
                        ownerInitials = ballRoom.players
                            .firstOrNull { it.id == ball.pickedBy }
                            ?.let { initialsFor(it.nickname) },
                        isUserPicked = ball.id == ballRoom.pendingSelectedBallId,
                        enabled = !ball.isPicked,
                        onClick = { onPickBall(ball.id) },
                        modifier = Modifier
                            .offset { IntOffset(xDp.roundToPx(), yDp.roundToPx()) }
                            .size(nodeSize)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BallPreview(ball = userPick, language = language, modifier = Modifier.weight(0.9f))
                BigPushButtonInternal(
                    text = confirmBallButtonText(userPick, hasCommittedPick, language),
                    color = Gold,
                    enabled = userPick != null,
                    onClick = onConfirm,
                    modifier = Modifier.weight(1.1f)
                )
            }
        }
    }
}

@Composable
private fun rememberBallPickCountdown(phase: BallRoomPhase): Int {
    var remaining by remember(phase) {
        mutableIntStateOf(20)
    }
    LaunchedEffect(phase) {
        remaining = 20
        while (phase == BallRoomPhase.PICKING && remaining > 0) {
            delay(1000)
            remaining -= 1
        }
    }
    return remaining
}

private fun confirmBallButtonText(ball: BallOption?, hasCommittedPick: Boolean, language: AppLanguage): String = when {
    hasCommittedPick && ball != null -> "${t("continue_ball", language)} #${ball.id}"
    ball != null -> "${t("select_ball", language)} #${ball.id}"
    else -> t("select_ball", language)
}

@Composable
private fun BallsHud(timer: Int, ballsLeft: Int, language: AppLanguage) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(NightBlue.copy(alpha = 0.7f))
                .border(1.dp, Gold.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Eyebrow(text = t("time", language))
            Text(
                text = "${timer}s",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = if (timer <= 5) ChipRed else Gold,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(NightBlue.copy(alpha = 0.7f))
                .border(1.dp, PureWhite.copy(alpha = 0.10f), RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Eyebrow(text = t("free", language))
            Text(
                text = "$ballsLeft/${GameRules.BALL_COUNT}",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = PureWhite,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }
    }
}

@Composable
private fun FloorStars() {
    val transition = rememberInfiniteTransition(label = "stars")
    val twinkle by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2_500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tw"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        val seed = Random(11)
        repeat(40) {
            val sx = seed.nextFloat() * size.width
            val sy = seed.nextFloat() * size.height
            drawCircle(
                color = PureWhite.copy(alpha = twinkle * 0.4f),
                radius = 1.5.dp.toPx(),
                center = Offset(sx, sy)
            )
        }
    }
}

@Composable
private fun BallNode(
    ball: BallOption,
    ownerColor: Color,
    ownerInitials: String?,
    isUserPicked: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = when {
        isUserPicked -> Triple(GoldLight, Gold, NightBlue)
        ball.isPicked -> Triple(ownerColor, ownerColor.copy(alpha = 0.7f), PureWhite)
        else -> Triple(Color(0xFF3D6098), Color(0xFF2A4A7A), PureWhite)
    }
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(palette.first, palette.second)
                )
            )
            .border(
                if (isUserPicked) 3.dp else 1.5.dp,
                if (isUserPicked) Gold else palette.first.copy(alpha = 0.4f),
                CircleShape
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = ball.id.toString(),
            style = MaterialTheme.typography.titleMedium.copy(
                color = palette.third,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )
        )
        if (ball.isPicked && !isUserPicked && ownerInitials != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(ownerColor)
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = ownerInitials,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = NightBlue,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 8.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun BallPreview(ball: BallOption?, language: AppLanguage, modifier: Modifier = Modifier) {
    if (ball == null) {
        Box(
            modifier = modifier
                .heightIn(min = 60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(NightBlue.copy(alpha = 0.5f))
                .border(1.dp, PureWhite.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                .padding(12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "${t("your_choice", language)}: -",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )
        }
        return
    }

    Row(
        modifier = modifier
            .heightIn(min = 60.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.linearGradient(
                    listOf(Gold.copy(alpha = 0.18f), ChipRed.copy(alpha = 0.10f))
                )
            )
            .border(1.dp, Gold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(GoldLight, Gold))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = ball.id.toString(),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = NightBlue,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }
        Column {
            Eyebrow(text = t("your_choice", language))
            Text(
                text = "#${ball.id}",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Gold,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }
    }
}

/* ============================================================
   REVEAL (BallRoomPhase.REVEALED)
   ============================================================ */

@Composable
fun RevealView(
    ballRoom: BallRoomUiState,
    language: AppLanguage,
    onContinueToBattle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userPlayer = ballRoom.players.firstOrNull { it.isUser }
    val userBall = ballRoom.balls.firstOrNull { it.pickedBy == GameRules.USER_PLAYER_ID }
    val mult = userPlayer?.multiplier ?: userBall?.multiplier ?: 1.0

    Box(modifier = modifier.fillMaxSize()) {
        AmbientBackground(particleCount = 28)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            DisplayGold(text = t("your_ball", language), fontSize = 36, textAlign = TextAlign.Center)

            // Big revealed ball
            Box(
                modifier = Modifier.size(220.dp),
                contentAlignment = Alignment.Center
            ) {
                val transition = rememberInfiniteTransition(label = "reveal")
                val pulse by transition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.08f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1_400, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "p"
                )
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(pulse)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(listOf(GoldLight, Gold, GoldDark))
                        )
                        .border(6.dp, GoldDark, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (userBall?.id ?: 0).toString(),
                        style = MaterialTheme.typography.displayLarge.copy(
                            color = NightBlue,
                            fontSize = 72.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Eyebrow(text = t("multiplier", language))
                DisplayRed(
                    text = "x${"%.2f".format(mult)}",
                    fontSize = 96,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (mult >= 5.0) t("lucky_champion", language) else t("fight_battle", language),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Gold,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            BigPushButtonInternal(
                text = t("go_battle", language),
                color = ChipRed,
                onClick = onContinueToBattle,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/* ============================================================
   BATTLE ARENA + HAND (BattlePhase.READY/IN_PROGRESS)
   ============================================================ */

@Composable
fun ArenaView(
    battle: BattleUiState,
    language: AppLanguage,
    onSelectAction: (BattleCardType) -> Unit,
    onSelectCard: (Long) -> Unit,
    onSelectTarget: (String) -> Unit,
    onPlayRound: () -> Unit,
    modifier: Modifier = Modifier
) {
    var logExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        AmbientBackground(particleCount = 16)

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 8.dp)) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    DisplayRed(text = t("battle_title", language), fontSize = 28)
                    Text(
                        text = t("battle_subtitle", language),
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
                TurnPill(yours = battle.phase != BattlePhase.FINISHED, round = battle.round, language = language)
            }

            Spacer(Modifier.height(8.dp))

            // Arena + optional side battle log
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.9f)
                    .heightIn(min = 365.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .weight(if (logExpanded) 3.2f else 1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.radialGradient(
                                    listOf(ChipRed.copy(alpha = 0.10f), NightBlue),
                                    radius = 1000f
                                )
                            )
                            .border(1.dp, ChipRed.copy(alpha = 0.20f), RoundedCornerShape(8.dp))
                    ) {
                        ArenaFloor()

                        val opponents = battle.players.filterNot { it.isUser }
                        val positions = opponentGridPositions(opponents.size.coerceAtLeast(1))
                        val w = maxWidth
                        val h = maxHeight

                        opponents.forEachIndexed { i, opp ->
                            val (rx, ry) = positions[i]
                            val nodeWidth = 76.dp
                            val nodeHeight = 82.dp
                            val x = (w - nodeWidth) * rx
                            val y = (h - nodeHeight) * ry
                            OpponentNode(
                                player = opp,
                                selected = opp.id == battle.selectedTargetId,
                                onClick = { onSelectTarget(opp.id) },
                                modifier = Modifier
                                    .offset { IntOffset(x.roundToPx(), y.roundToPx()) }
                                    .width(nodeWidth)
                            )
                        }

                        val you = battle.players.firstOrNull { it.isUser }
                        if (you != null) {
                            YouNode(
                                player = you,
                                language = language,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 14.dp)
                            )
                        }
                    }

                    if (logExpanded) {
                    BattleLogSidePanel(
                        log = battle.log,
                        expanded = true,
                        language = language,
                        onToggle = { logExpanded = false },
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f)
                        )
                    }
                }

                if (!logExpanded) {
                    BattleLogButton(
                        onClick = { logExpanded = true },
                        latest = battle.log.lastOrNull().orEmpty(),
                        language = language,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    )
                }
            }

            Eyebrow(text = t("your_hand", language), color = Gold)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                battle.hand.take(5).forEach { card ->
                    HandCard(
                        card = card,
                        selected = card.id == battle.selectedCardId,
                        language = language,
                        onClick = {
                            onSelectAction(card.type)
                            onSelectCard(card.id)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            BigPushButtonInternal(
                text = if (battle.phase == BattlePhase.FINISHED) t("finished", language) else t("play_round", language),
                color = ChipRed,
                enabled = battle.phase != BattlePhase.FINISHED,
                onClick = onPlayRound,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            )
        }
    }
}

@Composable
private fun BattleLogButton(
    onClick: () -> Unit,
    latest: String,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(96.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(NightBlue.copy(alpha = 0.84f))
            .border(1.dp, PureWhite.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Text(
            text = t("log", language),
            style = MaterialTheme.typography.labelSmall.copy(
                color = Gold,
                fontWeight = FontWeight.ExtraBold
            )
        )
        Text(
            text = t("view", language),
            style = MaterialTheme.typography.labelSmall.copy(
                color = PureWhite,
                fontWeight = FontWeight.ExtraBold
            )
        )
        if (latest.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = latest,
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary,
                    fontSize = 9.sp
                )
            )
        }
    }
}

@Composable
private fun BattleLogSidePanel(
    log: List<String>,
    expanded: Boolean,
    language: AppLanguage,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    LaunchedEffect(log.size, expanded) {
        if (expanded) {
            scrollState.scrollTo(scrollState.maxValue)
        }
    }

    val panelModifier = if (expanded) {
        modifier
    } else {
        modifier.clickable(onClick = onToggle)
    }

    Column(
        modifier = panelModifier
            .clip(RoundedCornerShape(8.dp))
            .background(NightBlue.copy(alpha = 0.76f))
            .border(1.dp, PureWhite.copy(alpha = 0.10f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        if (expanded) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Eyebrow(text = t("battle_log", language))
                Text(
                    text = t("hide", language),
                    modifier = Modifier.clickable(onClick = onToggle),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Gold,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true)
                    .verticalScroll(scrollState)
            ) {
                log.forEach { line ->
                    val isRoundHeader = line.matches(Regex("Ronda \\d+.*"))
                    val translatedLine = AppI18n.message(line, language) ?: line
                    Text(
                        text = translatedLine,
                        style = if (isRoundHeader) {
                            MaterialTheme.typography.labelMedium.copy(
                                color = Gold,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                letterSpacing = 1.2.sp
                            )
                        } else {
                            MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }
                    )
                    Spacer(Modifier.height(if (isRoundHeader) 6.dp else 4.dp))
                }
            }
        } else {
            Text(
                text = t("log", language),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Gold,
                    fontWeight = FontWeight.ExtraBold
                )
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = t("view", language),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = PureWhite,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }
    }
}

@Composable
private fun ArenaFloor() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // grid
        val cell = 50.dp.toPx()
        var x = 0f
        while (x < size.width) {
            drawLine(
                Gold.copy(alpha = 0.06f),
                Offset(x, 0f), Offset(x, size.height), 1.dp.toPx()
            )
            x += cell
        }
        var y = 0f
        while (y < size.height) {
            drawLine(
                Gold.copy(alpha = 0.06f),
                Offset(0f, y), Offset(size.width, y), 1.dp.toPx()
            )
            y += cell
        }
        // center ring
        drawCircle(
            color = Gold.copy(alpha = 0.15f),
            radius = 100.dp.toPx(),
            center = Offset(size.width / 2f, size.height / 2f),
            style = Stroke(width = 2.dp.toPx())
        )
        drawCircle(
            color = ChipRed.copy(alpha = 0.30f),
            radius = 60.dp.toPx(),
            center = Offset(size.width / 2f, size.height / 2f),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

@Composable
private fun TurnPill(yours: Boolean, round: Int, language: AppLanguage) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (yours) Gold.copy(alpha = 0.18f) else NightBlue.copy(alpha = 0.7f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (yours) Gold else PureWhite.copy(alpha = 0.10f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (yours) Gold else TextSecondary)
            )
            Text(
                text = "${t("round", language)} $round",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (yours) PureWhite else TextSecondary,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }
    }
}

@Composable
private fun OpponentNode(
    player: BattlePlayer,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pct = (player.hp.toFloat() / GameRules.BATTLE_INITIAL_HP).coerceIn(0f, 1f)
    val palette = PLAYER_PALETTE[
        (player.id.hashCode().rem(PLAYER_PALETTE.size) + PLAYER_PALETTE.size) % PLAYER_PALETTE.size
    ]

    Column(
        modifier = modifier
            .alpha(if (player.isAlive) 1f else 0.4f)
            .clickable(enabled = player.isAlive, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(palette, palette.copy(alpha = 0.7f))))
                .border(
                    if (selected) 4.dp else 2.dp,
                    if (selected) Gold else if (player.isAlive) palette else Color(0xFF6E7A8C),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (player.isAlive) initialsFor(player.nickname) else "X",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = PureWhite,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
            )
        }
        Text(
            text = player.nickname,
            maxLines = 1,
            style = MaterialTheme.typography.labelSmall.copy(
                color = PureWhite,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )
        // HP bar
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(NightBlue)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(pct)
                    .background(
                        when {
                            pct > 0.5f -> AliveGreen
                            pct > 0.25f -> Gold
                            else -> ChipRed
                        }
                    )
            )
        }
    }
}

@Composable
private fun YouNode(player: BattlePlayer, language: AppLanguage, modifier: Modifier = Modifier) {
    val pct = (player.hp.toFloat() / GameRules.BATTLE_INITIAL_HP).coerceIn(0f, 1f)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (player.shieldActive) {
            // Shield ring placeholder shown via border on avatar below
        }
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Gold, ChipRed)))
                .border(
                    3.dp,
                    if (player.shieldActive) ShieldBlue else Gold,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = t("you", language).uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = PureWhite,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }
        Box(
            modifier = Modifier
                .width(190.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(50))
                .background(NightBlue)
                .border(1.dp, Gold.copy(alpha = 0.4f), RoundedCornerShape(50))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(pct)
                    .background(AliveGreen)
            )
        }
        Text(
            text = "${player.hp}/${GameRules.BATTLE_INITIAL_HP} HP",
            style = MaterialTheme.typography.labelMedium.copy(
                color = PureWhite,
                fontWeight = FontWeight.ExtraBold
            )
        )
    }
}

@Composable
private fun HandCard(
    card: BattleHandCard,
    selected: Boolean,
    language: AppLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    data class CardSkin(val top: Color, val bot: Color, val label: String, val icon: String)
    val skin = when (card.type) {
        BattleCardType.ATTACK -> CardSkin(ChipRed, ChipRedDark, t("attack", language), "ATK")
        BattleCardType.SHIELD -> CardSkin(ShieldBlue, Color(0xFF2A78A8), t("defense", language), "DEF")
        BattleCardType.REBOUND -> CardSkin(ReflectPurple, Color(0xFF5C3DAB), t("rebound", language), "R")
    }

    Box(
        modifier = modifier
            .height(132.dp)
            .offset(y = if (selected) (-10).dp else 0.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.linearGradient(listOf(skin.top, skin.bot))
            )
            .border(
                if (selected) 3.dp else 1.5.dp,
                if (selected) Gold else PureWhite.copy(alpha = 0.18f),
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Text(
            text = if (card.type == BattleCardType.ATTACK) card.power.toString() else skin.icon,
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.displayMedium.copy(
                color = PureWhite,
                fontSize = if (card.type == BattleCardType.ATTACK) 52.sp else 40.sp,
                fontWeight = FontWeight.ExtraBold
            )
        )
        if (card.type == BattleCardType.ATTACK) {
            Text(
                text = skin.icon,
                modifier = Modifier.align(Alignment.TopEnd),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = PureWhite.copy(alpha = 0.8f),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 9.sp
                )
            )
        }
        Text(
            text = skin.label,
            modifier = Modifier.align(Alignment.BottomStart),
            style = MaterialTheme.typography.labelSmall.copy(
                color = PureWhite,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 9.sp,
                letterSpacing = 1.sp
            )
        )
    }
}

/* ============================================================
   DEFEAT (BattlePhase.DEFEATED)
   ============================================================ */

@Composable
fun DefeatView(
    battle: BattleUiState,
    language: AppLanguage,
    onReturnToEntry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val placement = battle.placement?.let { "#$it" } ?: "-"
    val lastRoundIndex = battle.log.indexOfLast { it.matches(Regex("Ronda \\d+.*")) }
    val lastEvents = if (lastRoundIndex >= 0) {
        battle.log.drop(lastRoundIndex)
    } else {
        battle.log.takeLast(6)
    }

    Box(modifier = modifier.fillMaxSize()) {
        AmbientBackground(particleCount = 24)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Eyebrow(text = t("battle_title", language), color = ChipRed)
            Spacer(Modifier.height(6.dp))
            DisplayRed(
                text = t("eliminated_title", language),
                fontSize = 42,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = t("defeat_description", language),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(Modifier.height(12.dp))

            PremiumPanel(modifier = Modifier.fillMaxWidth(), glow = true) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(ChipRed, ChipRedDark))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = t("ko", language),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = PureWhite,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 30.sp
                            )
                        )
                    }

                    Spacer(Modifier.width(18.dp))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Eyebrow(text = t("final_position", language), color = Gold)
                        DisplayGold(
                            text = placement,
                            fontSize = 46,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))

            PremiumPanel(modifier = Modifier.fillMaxWidth().heightIn(max = 148.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Eyebrow(text = t("latest_events", language), color = Gold)
                    if (lastEvents.isEmpty()) {
                        Text(
                            text = t("no_events", language),
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    } else {
                        lastEvents.forEach { event ->
                            val isRoundHeader = event.matches(Regex("Ronda \\d+.*"))
                            val translatedEvent = AppI18n.message(event, language) ?: event
                            Text(
                                text = if (isRoundHeader) translatedEvent else "- $translatedEvent",
                                style = if (isRoundHeader) {
                                    MaterialTheme.typography.labelMedium.copy(
                                        color = Gold,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.1.sp
                                    )
                                } else {
                                    MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                }
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))

            BigPushButtonInternal(
                text = t("return_entry", language),
                color = Gold,
                onClick = onReturnToEntry,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/* ============================================================
   VICTORY (BattlePhase.FINISHED)
   ============================================================ */

@Composable
fun VictoryView(
    battle: BattleUiState,
    market: MarketUiState,
    onSelectToken: (TokenId) -> Unit,
    onResetCycle: () -> Unit,
    language: AppLanguage = com.fichestu.frontend.data.repository.SessionStore.language(),
    modifier: Modifier = Modifier
) {
    val multiplier = battle.winningMultiplier ?: 1.0
    val isUserWinner = battle.winnerId == GameRules.USER_PLAYER_ID || battle.players.any { it.isUser && it.isAlive }
    val selectedToken = market.selectedToken

    Box(modifier = modifier.fillMaxSize()) {
        AmbientBackground(particleCount = 24)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Eyebrow(
                text = t("last_chip", language),
                color = Gold
            )
            DisplayGold(
                text = if (isUserWinner) t("victory", language) else t("defeat", language),
                fontSize = 56,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (isUserWinner)
                    t("choose_target_token", language)
                else
                    "${battle.winnerName ?: "Bot"} ${t("winner_applies", language)}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            )

            // Crown panel
            PremiumPanel(modifier = Modifier.fillMaxWidth(), glow = true) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(GoldLight, Gold))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = t("win", language),
                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 32.sp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Eyebrow(text = t("multiplier", language))
                        DisplayRed(
                            text = "x${"%.2f".format(multiplier)}",
                            fontSize = 48
                        )
                    }
                }
            }

            // Token list (read-only - engine ya aplicó el impacto al ganador)
            Eyebrow(text = t("market_tokens", language), color = Gold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(count = market.tokens.size) { idx ->
                    val token = market.tokens[idx]
                    val isTarget = token.id == market.selectedToken
                    Column(
                        modifier = Modifier
                            .width(110.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isTarget)
                                    Brush.verticalGradient(
                                        listOf(Gold.copy(alpha = 0.18f), NightBlue)
                                    )
                                else
                                    Brush.verticalGradient(listOf(NightBlue, NightBlue))
                            )
                            .border(
                                2.dp,
                                if (isTarget) Gold else PureWhite.copy(alpha = 0.08f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = token.displayName,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = PureWhite,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${"%.2f".format(token.currentPrice)} FTC",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Gold,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "${if (token.changePercent >= 0) "▲" else "▼"} ${"%.1f".format(kotlin.math.abs(token.changePercent))}%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (token.changePercent >= 0) AliveGreen else ChipRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            BigPushButtonInternal(
                text = t("new_cycle", language),
                color = Gold,
                onClick = onResetCycle,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun VictoryChoiceView(
    battle: BattleUiState,
    market: MarketUiState,
    language: AppLanguage,
    onSelectToken: (TokenId) -> Unit,
    onResetCycle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val multiplier = battle.winningMultiplier ?: 1.0
    val isUserWinner = battle.winnerId == GameRules.USER_PLAYER_ID || battle.players.any { it.isUser && it.isAlive }
    val selectedToken = market.selectedToken
    val selectedTokenName = market.tokens.firstOrNull { it.id == selectedToken }?.displayName ?: selectedToken.name

    Box(modifier = modifier.fillMaxSize()) {
        AmbientBackground(particleCount = 28)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Eyebrow(text = if (isUserWinner) t("last_chip", language) else t("battle_title", language), color = Gold)
            DisplayGold(
                text = if (isUserWinner) t("victory", language) else t("defeat", language),
                fontSize = 54,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (isUserWinner) {
                    t("choose_target_token", language)
                } else {
                    "${battle.winnerName ?: "Bot"} ${t("winner_applies", language)}"
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            )

            PremiumPanel(modifier = Modifier.fillMaxWidth(), glow = true) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(GoldLight, Gold))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isUserWinner) t("win", language) else t("ko", language),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = NightBlue,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 30.sp
                            )
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Eyebrow(text = t("multiplier", language), color = Gold)
                        DisplayRed(
                            text = "x${"%.2f".format(multiplier)}",
                            fontSize = 46
                        )
                        Text(
                            text = if (isUserWinner) "${t("current_target", language)}: $selectedTokenName" else t("winner_impact", language),
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                }
            }

            Eyebrow(
                text = if (isUserWinner) t("choose_target", language) else t("market_tokens", language),
                color = Gold
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(count = market.tokens.size) { idx ->
                    val token = market.tokens[idx]
                    val isTarget = token.id == selectedToken
                    Column(
                        modifier = Modifier
                            .width(if (isTarget) 140.dp else 120.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isTarget) {
                                    Brush.verticalGradient(listOf(Gold.copy(alpha = 0.34f), NightBlue))
                                } else {
                                    Brush.verticalGradient(listOf(NightBlue, NightBlue))
                                }
                            )
                            .border(
                                if (isTarget) 2.dp else 1.dp,
                                if (isTarget) Gold else PureWhite.copy(alpha = 0.08f),
                                RoundedCornerShape(14.dp)
                            )
                            .clickable(enabled = isUserWinner) { onSelectToken(token.id) }
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        if (isTarget) {
                            Eyebrow(text = t("objective", language), color = Gold)
                        }
                        Text(
                            text = token.displayName,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = PureWhite,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${"%.2f".format(token.currentPrice)} FTC",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Gold,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "${if (token.changePercent >= 0) "+" else ""}${"%.1f".format(token.changePercent)}%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (token.changePercent >= 0) AliveGreen else ChipRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            if (isUserWinner) {
                Text(
                    text = "${t("selected", language)}: $selectedTokenName",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Gold,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                )
            }

            Spacer(Modifier.weight(1f))

            BigPushButtonInternal(
                text = if (isUserWinner) t("apply_new_cycle", language) else t("new_cycle", language),
                color = Gold,
                onClick = onResetCycle,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/* ============================================================
   SHARED - internal big button + scattered positions
   ============================================================ */

@Composable
internal fun BigPushButtonInternal(
    text: String,
    color: Color,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shadowColor = if (color == Gold) GoldDark else ChipRedDark
    val textColor = if (color == Gold) NightBlue else PureWhite

    Box(
        modifier = modifier
            .height(50.dp)
            .alpha(if (enabled) 1f else 0.4f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 4.dp)
                .background(shadowColor, RoundedCornerShape(8.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color)
                .clickable(enabled = enabled, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = textColor,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp,
                    fontSize = 18.sp
                )
            )
        }
    }
}

/** Deterministic scattered positions in [0..1]^2 with min-distance check. */
internal fun scatteredPositions(count: Int, seed: Int = 42): List<Pair<Float, Float>> {
    val rng = Random(seed)
    val out = mutableListOf<Pair<Float, Float>>()
    val minDist2 = 0.012f
    var attempts = 0
    while (out.size < count && attempts < 5000) {
        attempts++
        val x = 0.07f + rng.nextFloat() * 0.86f
        val y = 0.07f + rng.nextFloat() * 0.86f
        val ok = out.all { (ox, oy) ->
            val dx = x - ox; val dy = y - oy
            (dx * dx + dy * dy) > minDist2
        }
        if (ok) out += (x to y)
    }
    // top-up if not enough (rare)
    while (out.size < count) {
        out += (rng.nextFloat() to rng.nextFloat())
    }
    return out
}

internal fun opponentGridPositions(count: Int): List<Pair<Float, Float>> {
    if (count <= 0) return emptyList()

    val positions = listOf(
        0.06f to 0.08f,
        0.27f to 0.08f,
        0.48f to 0.08f,
        0.69f to 0.08f,
        0.90f to 0.08f,
        0.22f to 0.48f,
        0.34f to 0.74f,
        0.66f to 0.48f,
        0.78f to 0.74f
    )

    return positions.take(count)
}
