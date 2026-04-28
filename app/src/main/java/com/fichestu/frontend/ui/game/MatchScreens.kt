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
import com.fichestu.frontend.game.GameRules
import com.fichestu.frontend.game.engine.GameEngine
import com.fichestu.frontend.game.model.BallOption
import com.fichestu.frontend.game.model.BallRoomPhase
import com.fichestu.frontend.game.model.BallRoomUiState
import com.fichestu.frontend.game.model.BattleCardType
import com.fichestu.frontend.game.model.BattlePhase
import com.fichestu.frontend.game.model.BattlePlayer
import com.fichestu.frontend.game.model.BattleUiState
import com.fichestu.frontend.game.model.MarketUiState
import com.fichestu.frontend.game.model.TokenId
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

/* ============================================================
   FICHESTU — Match flow screens (Lobby → Bolas → Reveal → Cards → Victoria)
   Wired to GameViewModel state. Backend-ready: solo lee de UiState.
   ============================================================ */

@Composable
fun BallRoomFlow(
    ballRoom: BallRoomUiState,
    cashBalance: Double,
    onEnterRoom: () -> Unit,
    onPickBall: (Int) -> Unit,
    onRevealMultipliers: () -> Unit,
    onOpenBattle: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (ballRoom.phase) {
        BallRoomPhase.WAITING_ENTRY -> LobbyView(
            cashBalance = cashBalance,
            statusMessage = ballRoom.statusMessage,
            onEnterRoom = onEnterRoom,
            modifier = modifier
        )
        BallRoomPhase.PICKING -> {
            // Auto-reveal cuando hay 10 jugadores con bola elegida
            BallsPoolView(
                ballRoom = ballRoom,
                onPickBall = onPickBall,
                onConfirm = onRevealMultipliers,
                modifier = modifier
            )
        }
        BallRoomPhase.REVEALED, BallRoomPhase.READY_FOR_BATTLE -> RevealView(
            ballRoom = ballRoom,
            onContinueToBattle = onOpenBattle,
            modifier = modifier
        )
    }
}

@Composable
fun BattleFlow(
    battle: BattleUiState,
    market: MarketUiState,
    onSelectAction: (BattleCardType) -> Unit,
    onPlayRound: () -> Unit,
    onResetCycle: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (battle.phase) {
        BattlePhase.LOCKED -> LockedView(modifier = modifier)
        BattlePhase.READY, BattlePhase.IN_PROGRESS -> ArenaView(
            battle = battle,
            selectedTokenId = market.selectedToken,
            onSelectAction = onSelectAction,
            onPlayRound = onPlayRound,
            modifier = modifier
        )
        BattlePhase.FINISHED -> VictoryView(
            battle = battle,
            market = market,
            onResetCycle = onResetCycle,
            modifier = modifier
        )
    }
}

@Composable
private fun LockedView(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        AmbientBackground(particleCount = 10)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            DisplayWhite(text = "🔒", fontSize = 80, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            DisplayGold(
                text = "BATTLE BLOQUEADO",
                fontSize = 28,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Completa el sorteo de bolas para desbloquear esta fase.",
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
    cashBalance: Double,
    statusMessage: String,
    onEnterRoom: () -> Unit,
    modifier: Modifier = Modifier
) {
    var countdown by remember { mutableIntStateOf(12) }
    var autoTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            countdown -= 1
            if (countdown <= 0) {
                if (!autoTriggered && cashBalance >= GameRules.BALL_ENTRY_COST) {
                    autoTriggered = true
                    onEnterRoom()
                }
                countdown = 12
            }
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
                text = "SALA DE PARTIDA",
                fontSize = 38,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Esperando jugadores…",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )

            CountdownRing(value = countdown, max = 12, label = "EMPIEZA EN")

            // 10 player slots in 5x2 grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(2) { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(5) { col ->
                            val idx = row * 5 + col
                            PlayerSlot(
                                initials = if (idx == 0) "TÚ" else "P${idx + 1}",
                                color = PLAYER_PALETTE[idx],
                                ready = idx < 7 || idx == 0,
                                isYou = idx == 0,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            PremiumPanel(modifier = Modifier.fillMaxWidth(), glow = true) {
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
                            text = "CÓMO SE JUEGA",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = PureWhite,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    RuleNumLine(1, "Elige 1 bola entre ${GameRules.BALL_COUNT}. Cada bola esconde un multiplicador.")
                    RuleNumLine(2, "Battle Royale de cartas. Último en pie gana.")
                    RuleNumLine(3, "El ganador aplica su multiplicador a un token del mercado.")
                }
            }

            Spacer(Modifier.weight(1f))

            Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Saldo: € ${"%.2f".format(cashBalance)}",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Gold,
                    fontWeight = FontWeight.ExtraBold
                )
            )

            BigPushButtonInternal(
                text = "PAGAR ENTRADA · € ${GameRules.BALL_ENTRY_COST.toInt()}",
                color = Gold,
                onClick = onEnterRoom,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun RuleNumLine(num: Int, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = num.toString(),
            modifier = Modifier.width(24.dp),
            style = MaterialTheme.typography.titleLarge.copy(
                color = Gold,
                fontWeight = FontWeight.ExtraBold
            )
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextSecondary,
                lineHeight = 18.sp
            )
        )
    }
}

@Composable
private fun CountdownRing(value: Int, max: Int, label: String) {
    val pct = value.toFloat() / max.toFloat()

    Box(
        modifier = Modifier.size(140.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(140.dp)) {
            // bg ring
            drawArc(
                color = PureWhite.copy(alpha = 0.10f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 10.dp.toPx())
            )
            // progress
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
            DisplayGold(text = "${value}s", fontSize = 48)
        }
    }
}

@Composable
private fun PlayerSlot(
    initials: String,
    color: Color,
    ready: Boolean,
    isYou: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
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
                RoundedCornerShape(14.dp)
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
            text = if (ready) "READY" else "WAIT",
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
    onPickBall: (Int) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    var timer by remember { mutableIntStateOf(20) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            timer = if (timer > 0) timer - 1 else 20
        }
    }

    val userPick = ballRoom.balls.firstOrNull { it.pickedBy == GameRules.USER_PLAYER_ID }
    val ballsLeft = ballRoom.balls.count { !it.isPicked }

    Box(modifier = modifier.fillMaxSize()) {
        AmbientBackground(particleCount = 18)

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 10.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    DisplayGold(text = "ELIGE TU BOLA", fontSize = 32)
                    Text(
                        text = "Una bola, un multiplicador.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
                BallsHud(timer = timer, ballsLeft = ballsLeft)
            }

            Spacer(Modifier.height(8.dp))

            // Floating field
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(Gold.copy(alpha = 0.10f), NightBlue),
                            radius = 800f
                        )
                    )
                    .border(1.dp, Gold.copy(alpha = 0.20f), RoundedCornerShape(24.dp))
            ) {
                val fieldW = maxWidth
                val fieldH = maxHeight

                FloorStars()

                // Place balls deterministically (seed by ball ids)
                val positions = remember(ballRoom.balls.size) {
                    scatteredPositions(ballRoom.balls.size, seed = 42)
                }

                ballRoom.balls.forEachIndexed { idx, ball ->
                    val (rx, ry) = positions.getOrNull(idx) ?: (0.5f to 0.5f)
                    val xDp = fieldW * rx - 28.dp
                    val yDp = fieldH * ry - 28.dp
                    BallNode(
                        ball = ball,
                        ownerColor = colorFor(ball.pickedBy, ballRoom.players),
                        ownerInitials = ballRoom.players
                            .firstOrNull { it.id == ball.pickedBy }
                            ?.let { initialsFor(it.nickname) },
                        isUserPicked = ball.pickedBy == GameRules.USER_PLAYER_ID,
                        onClick = { onPickBall(ball.id) },
                        modifier = Modifier
                            .offset { IntOffset(xDp.roundToPx(), yDp.roundToPx()) }
                            .size(56.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Footer: preview + confirm
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BallPreview(ball = userPick, modifier = Modifier.weight(1f))
                BigPushButtonInternal(
                    text = if (userPick != null) "✓ #${userPick.id}" else "ELIGE",
                    color = Gold,
                    enabled = userPick != null,
                    onClick = onConfirm
                )
            }
        }
    }
}

@Composable
private fun BallsHud(timer: Int, ballsLeft: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(NightBlue.copy(alpha = 0.7f))
                .border(1.dp, Gold.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Eyebrow(text = "TIMER")
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
                .clip(RoundedCornerShape(14.dp))
                .background(NightBlue.copy(alpha = 0.7f))
                .border(1.dp, PureWhite.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Eyebrow(text = "LIBRES")
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
            .clickable(enabled = !ball.isPicked, onClick = onClick),
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
private fun BallPreview(ball: BallOption?, modifier: Modifier = Modifier) {
    if (ball == null) {
        Box(
            modifier = modifier
                .heightIn(min = 60.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(NightBlue.copy(alpha = 0.5f))
                .border(1.dp, PureWhite.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                .padding(12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "Tu elección: —",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )
        }
        return
    }

    Row(
        modifier = modifier
            .heightIn(min = 60.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    listOf(Gold.copy(alpha = 0.18f), ChipRed.copy(alpha = 0.10f))
                )
            )
            .border(1.dp, Gold.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
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
            Eyebrow(text = "TU ELECCIÓN")
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
            DisplayGold(text = "TU BOLA", fontSize = 36, textAlign = TextAlign.Center)

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
                Eyebrow(text = "MULTIPLICADOR")
                DisplayRed(
                    text = "x${"%.2f".format(mult)}",
                    fontSize = 96,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (mult >= 5.0) "¡Suerte de campeón!" else "A pelearlo en el Battle Royale",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Gold,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            BigPushButtonInternal(
                text = "IR A BATTLE ROYALE ▸",
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
    selectedTokenId: TokenId,
    onSelectAction: (BattleCardType) -> Unit,
    onPlayRound: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        AmbientBackground(particleCount = 16)

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 10.dp)) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    DisplayRed(text = "BATTLE ROYALE", fontSize = 28)
                    Text(
                        text = "Última ficha en pie se queda con la corona.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
                TurnPill(yours = battle.phase != BattlePhase.FINISHED, round = battle.round)
            }

            Spacer(Modifier.height(8.dp))

            // Arena
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(ChipRed.copy(alpha = 0.10f), NightBlue),
                            radius = 1000f
                        )
                    )
                    .border(1.dp, ChipRed.copy(alpha = 0.20f), RoundedCornerShape(24.dp))
            ) {
                ArenaFloor()

                val opponents = battle.players.filterNot { it.isUser }
                val arc = arcPositions(opponents.size.coerceAtLeast(1))
                val w = maxWidth
                val h = maxHeight

                opponents.forEachIndexed { i, opp ->
                    val (rx, ry) = arc[i]
                    val x = w * rx - 36.dp
                    val y = h * ry
                    OpponentNode(
                        player = opp,
                        modifier = Modifier
                            .offset { IntOffset(x.roundToPx(), y.roundToPx()) }
                    )
                }

                // YOU node bottom-center
                val you = battle.players.firstOrNull { it.isUser }
                if (you != null) {
                    YouNode(
                        player = you,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 10.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Hand strip (3 cards: ATTACK / SHIELD / REBOUND)
            Eyebrow(text = "TU MANO", color = Gold)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HandCard(
                    type = BattleCardType.ATTACK,
                    selected = battle.selectedAction == BattleCardType.ATTACK,
                    onClick = { onSelectAction(BattleCardType.ATTACK) },
                    modifier = Modifier.weight(1f)
                )
                HandCard(
                    type = BattleCardType.SHIELD,
                    selected = battle.selectedAction == BattleCardType.SHIELD,
                    onClick = { onSelectAction(BattleCardType.SHIELD) },
                    modifier = Modifier.weight(1f)
                )
                HandCard(
                    type = BattleCardType.REBOUND,
                    selected = battle.selectedAction == BattleCardType.REBOUND,
                    onClick = { onSelectAction(BattleCardType.REBOUND) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(10.dp))
            BigPushButtonInternal(
                text = if (battle.phase == BattlePhase.FINISHED) "FINALIZADO" else "JUGAR RONDA",
                color = ChipRed,
                enabled = battle.phase != BattlePhase.FINISHED,
                onClick = onPlayRound,
                modifier = Modifier.fillMaxWidth()
            )

            // Battle log (last 4)
            if (battle.log.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NightBlue.copy(alpha = 0.7f))
                        .border(1.dp, PureWhite.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Eyebrow(text = "BATTLE LOG")
                        Spacer(Modifier.height(4.dp))
                        battle.log.takeLast(4).reversed().forEach { line ->
                            Text(
                                text = line,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )
                            )
                        }
                    }
                }
            }

            // Token target indicator
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Token objetivo: ${selectedTokenId.name}",
                style = MaterialTheme.typography.labelMedium.copy(color = Gold)
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
private fun TurnPill(yours: Boolean, round: Int) {
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
                text = "RONDA $round",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (yours) PureWhite else TextSecondary,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }
    }
}

@Composable
private fun OpponentNode(player: BattlePlayer, modifier: Modifier = Modifier) {
    val pct = (player.hp.toFloat() / GameRules.BATTLE_INITIAL_HP).coerceIn(0f, 1f)
    val palette = PLAYER_PALETTE[
        (player.id.hashCode().rem(PLAYER_PALETTE.size) + PLAYER_PALETTE.size) % PLAYER_PALETTE.size
    ]

    Column(
        modifier = modifier
            .alpha(if (player.isAlive) 1f else 0.4f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(palette, palette.copy(alpha = 0.7f))))
                .border(
                    2.dp,
                    if (player.isAlive) palette else Color(0xFF6E7A8C),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (player.isAlive) initialsFor(player.nickname) else "✕",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = PureWhite,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp
                )
            )
        }
        Text(
            text = player.nickname,
            style = MaterialTheme.typography.labelSmall.copy(
                color = PureWhite,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        )
        // HP bar
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(4.dp)
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
private fun YouNode(player: BattlePlayer, modifier: Modifier = Modifier) {
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
                .size(72.dp)
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
                text = "TÚ",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = PureWhite,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }
        Box(
            modifier = Modifier
                .width(160.dp)
                .height(10.dp)
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
    type: BattleCardType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    data class CardSkin(val top: Color, val bot: Color, val label: String, val icon: String)
    val skin = when (type) {
        BattleCardType.ATTACK -> CardSkin(ChipRed, ChipRedDark, "ATAQUE", "⚔")
        BattleCardType.SHIELD -> CardSkin(ShieldBlue, Color(0xFF2A78A8), "ESCUDO", "🛡")
        BattleCardType.REBOUND -> CardSkin(ReflectPurple, Color(0xFF5C3DAB), "REBOTE", "↺")
    }

    Box(
        modifier = modifier
            .height(110.dp)
            .offset(y = if (selected) (-10).dp else 0.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(listOf(skin.top, skin.bot))
            )
            .border(
                if (selected) 3.dp else 1.5.dp,
                if (selected) Gold else PureWhite.copy(alpha = 0.18f),
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Text(
            text = skin.icon,
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.displayMedium.copy(fontSize = 38.sp)
        )
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
   VICTORY (BattlePhase.FINISHED)
   ============================================================ */

@Composable
fun VictoryView(
    battle: BattleUiState,
    market: MarketUiState,
    onResetCycle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val multiplier = battle.winningMultiplier ?: 1.0
    val isUserWinner = battle.winnerId == GameRules.USER_PLAYER_ID

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
                text = "🏆 LAST CHIP STANDING 🏆",
                color = Gold
            )
            DisplayGold(
                text = if (isUserWinner) "¡VICTORIA!" else "DERROTA",
                fontSize = 56,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (isUserWinner)
                    "Tu multiplicador se aplica al token seleccionado."
                else
                    "${battle.winnerName ?: "Otro"} aplica el multiplicador.",
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
                            text = "👑",
                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 32.sp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Eyebrow(text = "MULTIPLICADOR")
                        DisplayRed(
                            text = "x${"%.2f".format(multiplier)}",
                            fontSize = 48
                        )
                    }
                }
            }

            // Token list (read-only — engine ya aplicó el impacto al ganador)
            Eyebrow(text = "TOKENS DEL MERCADO", color = Gold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(count = market.tokens.size) { idx ->
                    val token = market.tokens[idx]
                    val isTarget = token.id == market.selectedToken
                    Column(
                        modifier = Modifier
                            .width(110.dp)
                            .clip(RoundedCornerShape(14.dp))
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
                                RoundedCornerShape(14.dp)
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
                            text = "€ ${"%.2f".format(token.currentPrice)}",
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
                text = "NUEVO CICLO ▸",
                color = Gold,
                onClick = onResetCycle,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/* ============================================================
   SHARED — internal big button + scattered positions
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
            .height(54.dp)
            .alpha(if (enabled) 1f else 0.4f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 4.dp)
                .background(shadowColor, RoundedCornerShape(14.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 4.dp)
                .clip(RoundedCornerShape(14.dp))
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
