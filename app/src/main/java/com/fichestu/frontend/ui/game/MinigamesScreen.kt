package com.fichestu.frontend.ui.game

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fichestu.frontend.R
import com.fichestu.frontend.ui.theme.ChipRed
import com.fichestu.frontend.ui.theme.ChipRedDark
import com.fichestu.frontend.ui.theme.CoinRedLight
import com.fichestu.frontend.ui.theme.DeepBlue
import com.fichestu.frontend.ui.theme.DiceFace
import com.fichestu.frontend.ui.theme.Gold
import com.fichestu.frontend.ui.theme.GoldDark
import com.fichestu.frontend.ui.theme.GoldLight
import com.fichestu.frontend.ui.theme.AliveGreen
import com.fichestu.frontend.ui.theme.NightBlue
import com.fichestu.frontend.ui.theme.PanelBlue
import com.fichestu.frontend.ui.theme.PureWhite
import com.fichestu.frontend.ui.theme.ReflectPurple
import com.fichestu.frontend.ui.theme.ShieldBlue
import com.fichestu.frontend.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.pow
import kotlin.random.Random

private enum class MiniGame(
    @StringRes val titleRes: Int,
    @StringRes val tagRes: Int,
    @StringRes val descriptionRes: Int,
    val accent: Color,
    val icon: String
) {
    HUB(R.string.mini_title_upper, R.string.mini_tag_arcade, R.string.mini_title_upper, Gold, "*"),
    SLOTS(R.string.mini_slots_title, R.string.mini_slots_tag, R.string.mini_slots_description, Gold, "$"),
    CRASH(R.string.mini_crash_title, R.string.mini_crash_tag, R.string.mini_crash_description, ChipRed, "!"),
    COIN(R.string.mini_coin_title, R.string.mini_coin_tag, R.string.mini_coin_description, Gold, "H"),
    DICE(R.string.mini_dice_title, R.string.mini_dice_tag, R.string.mini_dice_description, ShieldBlue, "7")
}

@Composable
fun MinigamesTab(
    cashBalance: Double,
    onResult: (deltaCash: Double, message: String) -> Unit
) {
    var current by remember { mutableStateOf(MiniGame.HUB) }

    Box(modifier = Modifier.fillMaxSize()) {
        AmbientBackground(particleCount = 18)

        AnimatedContent(
            targetState = current,
            transitionSpec = {
                (fadeIn(tween(220)) + scaleIn(tween(220), initialScale = 0.96f))
                    .togetherWith(fadeOut(tween(160)) + scaleOut(tween(160), targetScale = 1.03f))
            },
            label = "mini_screen",
            modifier = Modifier.fillMaxSize()
        ) { game ->
            when (game) {
                MiniGame.HUB -> MinigamesHub(cashBalance = cashBalance, onPick = { current = it })
                MiniGame.SLOTS -> SlotsGame(
                    cashBalance = cashBalance,
                    onBack = { current = MiniGame.HUB },
                    onResult = onResult
                )
                MiniGame.CRASH -> CrashGame(
                    cashBalance = cashBalance,
                    onBack = { current = MiniGame.HUB },
                    onResult = onResult
                )
                MiniGame.COIN -> CoinGame(
                    cashBalance = cashBalance,
                    onBack = { current = MiniGame.HUB },
                    onResult = onResult
                )
                MiniGame.DICE -> DiceGame(
                    cashBalance = cashBalance,
                    onBack = { current = MiniGame.HUB },
                    onResult = onResult
                )
            }
        }
    }
}

@Composable
private fun MinigamesHub(cashBalance: Double, onPick: (MiniGame) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Eyebrow(text = stringResource(R.string.mini_tag_arcade_room), color = Gold)
            DisplayGold(
                text = stringResource(R.string.mini_title_upper),
                fontSize = 42,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            StatPill(
                label = stringResource(R.string.mini_balance_label),
                value = formatCurrency(cashBalance),
                accent = Gold
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            val games = listOf(MiniGame.SLOTS, MiniGame.CRASH, MiniGame.COIN, MiniGame.DICE)
            items(games.size) { index ->
                val game = games[index]
                GameCard(game = game, onClick = { onPick(game) })
            }
        }
    }
}

@Composable
private fun GameCard(game: MiniGame, onClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "card_shimmer")
    val shimmer by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing)),
        label = "shimmer"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.verticalGradient(listOf(PanelBlue, NightBlue)))
            .border(1.5.dp, game.accent.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sweepX = size.width * shimmer
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, PureWhite.copy(alpha = 0.11f), Color.Transparent),
                    start = Offset(sweepX - 70f, 0f),
                    end = Offset(sweepX + 70f, size.height)
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(GoldLight, game.accent, GoldDark))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = game.icon,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = NightBlue,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
                Spacer(Modifier.width(10.dp))
                Eyebrow(text = stringResource(game.tagRes), color = game.accent)
            }

            Column {
                Text(
                    text = stringResource(game.titleRes).uppercase(Locale.US),
                    style = MaterialTheme.typography.displaySmall.copy(
                        color = PureWhite,
                        fontSize = 22.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(game.descriptionRes),
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }
    }
}

@Composable
private fun MiniTopBar(title: String, tag: String, accent: Color, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Surface(
            modifier = Modifier.clickable(onClick = onBack),
            shape = RoundedCornerShape(50),
            color = NightBlue.copy(alpha = 0.6f),
            border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.18f))
        ) {
            Text(
                text = stringResource(R.string.mini_back),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = PureWhite,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Eyebrow(text = tag, color = accent)
            Text(
                text = title.uppercase(Locale.US),
                style = MaterialTheme.typography.displaySmall.copy(
                    color = accent,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }
    }
}

private val slotSymbols = listOf("$", "D", "R", "X", "7", "C")
private val slotColors = listOf(Gold, ShieldBlue, ChipRed, GoldLight, ReflectPurple, GoldDark)

@Composable
private fun SlotsGame(
    cashBalance: Double,
    onBack: () -> Unit,
    onResult: (Double, String) -> Unit
) {
    val context = LocalContext.current
    var reels by remember { mutableStateOf(intArrayOf(0, 1, 4)) }
    var spinning by remember { mutableStateOf(false) }
    var bet by remember { mutableIntStateOf(50) }
    var resultText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(spinning) {
        if (!spinning) return@LaunchedEffect

        val end = System.currentTimeMillis() + 1800
        while (System.currentTimeMillis() < end) {
            reels = IntArray(3) { Random.nextInt(slotSymbols.size) }
            delay(70)
        }

        reels = if (Random.nextFloat() < 0.25f) {
            val symbol = Random.nextInt(slotSymbols.size)
            intArrayOf(symbol, symbol, symbol)
        } else {
            IntArray(3) { Random.nextInt(slotSymbols.size) }
        }

        val win = reels[0] == reels[1] && reels[1] == reels[2]
        val delta = if (win) bet * 12.0 else -bet.toDouble()
        resultText = if (win) {
            context.getString(R.string.mini_slots_win, delta.toInt())
        } else {
            context.getString(R.string.mini_slots_loss, delta.toInt())
        }
        onResult(
            delta,
            if (win) {
                context.getString(R.string.mini_slots_msg_win, delta.toInt())
            } else {
                context.getString(R.string.mini_slots_msg_loss, delta.toInt())
            }
        )
        spinning = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MiniTopBar(
            stringResource(R.string.mini_slots_title),
            stringResource(R.string.mini_slots_top_tag),
            Gold,
            onBack
        )

        PremiumPanel(modifier = Modifier.fillMaxWidth(), glow = true) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Eyebrow(text = stringResource(R.string.mini_slots_jackpot))
                DisplayGold(text = stringResource(R.string.mini_slots_jackpot_value), fontSize = 25)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.verticalGradient(listOf(PanelBlue, NightBlue)))
                .border(3.dp, Gold, RoundedCornerShape(28.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CabinetBulbs(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(vertical = 18.dp)
            ) {
                reels.forEachIndexed { index, symbol ->
                    Reel(symbolIndex = symbol, spinning = spinning, delayMs = index * 180)
                }
            }
            CabinetBulbs(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(10, 50, 100, 500).forEach { value ->
                BetChip(value = value, selected = bet == value, enabled = !spinning) { bet = value }
            }
        }

        BigPushButton(
            text = if (spinning) {
                stringResource(R.string.mini_slots_spinning)
            } else {
                stringResource(R.string.mini_slots_spin, bet)
            },
            color = ChipRed,
            enabled = !spinning,
            onClick = {
                if (cashBalance < bet) {
                    onResult(0.0, context.getString(R.string.mini_msg_insufficient_bet, bet))
                } else {
                    resultText = null
                    spinning = true
                }
            }
        )

        resultText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = if (it.startsWith("+")) AliveGreen else ChipRed,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }
    }
}

@Composable
private fun CabinetBulbs(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "bulbs")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bulb_alpha"
    )
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceEvenly) {
        repeat(10) { index ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .alpha(if (index % 2 == 0) alpha else 1.35f - alpha)
                    .background(if (index % 2 == 0) Gold else ChipRed, CircleShape)
            )
        }
    }
}

@Composable
private fun Reel(symbolIndex: Int, spinning: Boolean, delayMs: Int) {
    val transition = rememberInfiniteTransition(label = "reel")
    val shake by transition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(140, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = androidx.compose.animation.core.StartOffset(delayMs)
        ),
        label = "reel_shake"
    )

    Box(
        modifier = Modifier
            .size(width = 92.dp, height = 156.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(NightBlue, PanelBlue, NightBlue)))
            .border(2.dp, Gold.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = slotSymbols[symbolIndex],
            modifier = Modifier
                .alpha(if (spinning) 0.5f else 1f)
                .padding(top = if (spinning) shake.dp else 0.dp),
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 64.sp,
                color = slotColors[symbolIndex],
                fontWeight = FontWeight.ExtraBold
            )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Gold.copy(alpha = 0.10f), Color.Transparent)))
        )
    }
}

@Composable
private fun CrashGame(
    cashBalance: Double,
    onBack: () -> Unit,
    onResult: (Double, String) -> Unit
) {
    val context = LocalContext.current
    val stake = 100
    var multiplier by remember { mutableFloatStateOf(1.0f) }
    var running by remember { mutableStateOf(false) }
    var crashed by remember { mutableStateOf(false) }
    var cashed by remember { mutableStateOf(false) }
    var crashAt by remember { mutableFloatStateOf(2.2f) }
    var settled by remember { mutableStateOf(false) }

    LaunchedEffect(running, crashed, cashed) {
        if (!running || crashed || cashed) return@LaunchedEffect
        while (running && !crashed && !cashed) {
            delay(60)
            multiplier += 0.04f + multiplier * 0.012f
            if (multiplier >= crashAt) crashed = true
        }
    }

    LaunchedEffect(crashed, cashed) {
        if (settled) return@LaunchedEffect
        when {
            crashed -> {
                settled = true
                running = false
                onResult(-stake.toDouble(), context.getString(R.string.mini_crash_msg_bust, stake))
            }
            cashed -> {
                val delta = (stake * multiplier).toInt() - stake
                settled = true
                running = false
                onResult(delta.toDouble(), context.getString(R.string.mini_crash_msg_cash_out, delta))
            }
        }
    }

    fun resetRound() {
        multiplier = 1.0f
        running = false
        crashed = false
        cashed = false
        settled = false
        crashAt = 1.5f + Random.nextFloat() * 6.5f
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MiniTopBar(
            stringResource(R.string.mini_crash_title),
            stringResource(R.string.mini_crash_tag),
            ChipRed,
            onBack
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.radialGradient(
                        listOf(ChipRed.copy(alpha = 0.15f), NightBlue),
                        center = Offset(0f, Float.POSITIVE_INFINITY),
                        radius = 800f
                    )
                )
                .border(2.dp, if (crashed) ChipRed else Gold.copy(alpha = 0.34f), RoundedCornerShape(24.dp))
        ) {
            CrashChart(multiplier = multiplier, crashed = crashed, modifier = Modifier.fillMaxSize())
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (crashed) {
                    DisplayRed(
                        text = stringResource(R.string.mini_crash_bust),
                        fontSize = 86,
                        textAlign = TextAlign.Center
                    )
                } else {
                    DisplayGold(text = String.format(Locale.US, "%.2fx", multiplier), fontSize = 86)
                }
            }
        }

        when {
            crashed || cashed -> BigPushButton(
                stringResource(R.string.mini_crash_new_round),
                color = Gold,
                onClick = ::resetRound
            )
            running -> BigPushButton(
                text = stringResource(R.string.mini_crash_cash_out, (stake * multiplier).toInt()),
                color = ChipRed,
                onClick = { cashed = true }
            )
            else -> BigPushButton(
                text = stringResource(R.string.mini_crash_start, stake),
                color = Gold,
                onClick = {
                    if (cashBalance < stake) {
                        onResult(0.0, context.getString(R.string.mini_crash_msg_insufficient))
                    } else {
                        resetRound()
                        running = true
                    }
                }
            )
        }
    }
}

@Composable
private fun CrashChart(multiplier: Float, crashed: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.padding(16.dp)) {
        val w = size.width
        val h = size.height
        val color = if (crashed) ChipRed else Gold
        val cell = 60.dp.toPx()

        var gx = 0f
        while (gx < w) {
            drawLine(Gold.copy(alpha = 0.10f), Offset(gx, 0f), Offset(gx, h), 1.dp.toPx())
            gx += cell
        }
        var gy = 0f
        while (gy < h) {
            drawLine(Gold.copy(alpha = 0.10f), Offset(0f, gy), Offset(w, gy), 1.dp.toPx())
            gy += cell
        }

        val path = Path()
        val fill = Path()
        val steps = 70
        for (i in 0..steps) {
            val t = i / steps.toFloat()
            val x = t * w
            val y = h - t.toDouble().pow(1.6).toFloat() * (multiplier - 1f) * h * 0.12f
            val yy = y.coerceAtLeast(8f)
            if (i == 0) {
                path.moveTo(x, yy)
                fill.moveTo(x, yy)
            } else {
                path.lineTo(x, yy)
                fill.lineTo(x, yy)
            }
        }
        fill.lineTo(w, h)
        fill.lineTo(0f, h)
        fill.close()
        drawPath(fill, brush = Brush.verticalGradient(listOf(color.copy(alpha = 0.36f), Color.Transparent)))
        drawPath(path, color = color, style = Stroke(width = 4.dp.toPx()))
    }
}

@Composable
private fun CoinGame(
    cashBalance: Double,
    onBack: () -> Unit,
    onResult: (Double, String) -> Unit
) {
    val context = LocalContext.current
    var pickedSide by remember { mutableStateOf<String?>(null) }
    var flipping by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<String?>(null) }
    var resultText by remember { mutableStateOf<String?>(null) }
    val stake = 100

    LaunchedEffect(flipping) {
        if (!flipping) return@LaunchedEffect
        delay(1500)
        val finalResult = if (Random.nextBoolean()) "heads" else "tails"
        val won = finalResult == pickedSide
        val delta = if (won) 200.0 else -stake.toDouble()
        result = finalResult
        resultText = if (won) {
            context.getString(R.string.mini_coin_win)
        } else {
            context.getString(R.string.mini_coin_loss)
        }
        onResult(
            delta,
            if (won) {
                context.getString(R.string.mini_coin_msg_win)
            } else {
                context.getString(R.string.mini_coin_msg_loss, stake)
            }
        )
        flipping = false
    }

    val transition = rememberInfiniteTransition(label = "coin")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1800f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing)),
        label = "coin_rotation"
    )
    val won = result != null && result == pickedSide

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MiniTopBar(
            stringResource(R.string.mini_coin_title),
            stringResource(R.string.mini_coin_top_tag),
            Gold,
            onBack
        )

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            DuelistAvatar(
                name = stringResource(R.string.mini_coin_you),
                initials = stringResource(R.string.mini_coin_you_initials),
                winnerLabel = stringResource(R.string.mini_coin_winner),
                color = Gold,
                winning = won,
                losing = result != null && !won
            )
            DisplayRed(text = stringResource(R.string.mini_coin_vs), fontSize = 36)
            DuelistAvatar(
                name = stringResource(R.string.mini_coin_opponent),
                initials = stringResource(R.string.mini_coin_opponent_initials),
                winnerLabel = stringResource(R.string.mini_coin_winner),
                color = ChipRed,
                winning = result != null && !won,
                losing = won
            )
        }

        Box(modifier = Modifier.size(220.dp), contentAlignment = Alignment.Center) {
            HaloRing(diameter = 205.dp, color = if (result == "tails") ChipRed else Gold) {
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .rotate(if (flipping) rotation else 0f)
                        .clip(CircleShape)
                        .background(
                            if (result == "tails") {
                                Brush.radialGradient(listOf(CoinRedLight, ChipRed, ChipRedDark))
                            } else {
                                Brush.radialGradient(listOf(GoldLight, Gold, GoldDark))
                            }
                        )
                        .border(8.dp, if (result == "tails") ChipRedDark else GoldDark, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (flipping) "?" else if (result == "tails") "T" else "H",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 72.sp,
                            color = if (result == "tails") PureWhite else NightBlue,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BigPushButton(
                text = stringResource(R.string.mini_coin_heads_button),
                color = Gold,
                enabled = !flipping,
                onClick = {
                    if (cashBalance < stake) onResult(0.0, context.getString(R.string.mini_coin_msg_insufficient))
                    else {
                        pickedSide = "heads"
                        result = null
                        resultText = null
                        flipping = true
                    }
                },
                modifier = Modifier.weight(1f)
            )
            BigPushButton(
                text = stringResource(R.string.mini_coin_tails_button),
                color = ChipRed,
                enabled = !flipping,
                onClick = {
                    if (cashBalance < stake) onResult(0.0, context.getString(R.string.mini_coin_msg_insufficient))
                    else {
                        pickedSide = "tails"
                        result = null
                        resultText = null
                        flipping = true
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }

        resultText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = if (it.startsWith("GANASTE")) AliveGreen else ChipRed,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }
    }
}

@Composable
private fun DuelistAvatar(
    name: String,
    initials: String,
    winnerLabel: String,
    color: Color,
    winning: Boolean,
    losing: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.alpha(if (losing) 0.42f else 1f)) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(color, color.copy(alpha = 0.66f))))
                .border(3.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = PureWhite,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium.copy(color = PureWhite, fontWeight = FontWeight.ExtraBold)
        )
        if (winning) {
            Text(
                text = winnerLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = AliveGreen,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
            )
        }
    }
}

@Composable
private fun DiceGame(
    cashBalance: Double,
    onBack: () -> Unit,
    onResult: (Double, String) -> Unit
) {
    val context = LocalContext.current
    var roll by remember { mutableStateOf(intArrayOf(3, 5)) }
    var rolling by remember { mutableStateOf(false) }
    var bet by remember { mutableStateOf("over") }
    var resultText by remember { mutableStateOf<String?>(null) }
    val stake = 100

    LaunchedEffect(rolling) {
        if (!rolling) return@LaunchedEffect
        val end = System.currentTimeMillis() + 1300
        while (System.currentTimeMillis() < end) {
            roll = intArrayOf(1 + Random.nextInt(6), 1 + Random.nextInt(6))
            delay(80)
        }

        val finalRoll = intArrayOf(1 + Random.nextInt(6), 1 + Random.nextInt(6))
        roll = finalRoll
        val sum = finalRoll[0] + finalRoll[1]
        val won = when (bet) {
            "under" -> sum < 7
            "seven" -> sum == 7
            else -> sum > 7
        }
        val tie = sum == 7 && bet != "seven"
        val delta = when {
            tie -> 0.0
            won && bet == "seven" -> 600.0
            won -> 180.0
            else -> -stake.toDouble()
        }
        resultText = when {
            tie -> context.getString(R.string.mini_dice_tie)
            won -> context.getString(R.string.mini_dice_win, delta.toInt())
            else -> context.getString(R.string.mini_dice_loss)
        }
        if (delta != 0.0) {
            onResult(
                delta,
                if (delta > 0) {
                    context.getString(R.string.mini_dice_msg_win, delta.toInt())
                } else {
                    context.getString(R.string.mini_dice_msg_loss, stake)
                }
            )
        } else {
            onResult(0.0, context.getString(R.string.mini_dice_msg_tie))
        }
        rolling = false
    }

    val sum = roll[0] + roll[1]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MiniTopBar(
            stringResource(R.string.mini_dice_title),
            stringResource(R.string.mini_dice_top_tag),
            ShieldBlue,
            onBack
        )

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Die(value = roll[0], rolling = rolling)
            DisplayRed(text = "+", fontSize = 36)
            Die(value = roll[1], rolling = rolling)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Eyebrow(text = stringResource(R.string.mini_dice_sum))
            DisplayGold(text = if (rolling) "??" else sum.toString(), fontSize = 70)
            resultText?.takeIf { !rolling }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = when {
                            it.startsWith("EMPATE") -> Gold
                            it.startsWith("GANASTE") -> AliveGreen
                            else -> ChipRed
                        },
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BetTile(
                stringResource(R.string.mini_dice_under),
                stringResource(R.string.mini_dice_under_desc),
                stringResource(R.string.mini_dice_multiplier_18),
                bet == "under",
                ShieldBlue,
                enabled = !rolling
            ) { bet = "under" }
            BetTile(
                stringResource(R.string.mini_dice_lucky),
                stringResource(R.string.mini_dice_lucky_desc),
                stringResource(R.string.mini_dice_multiplier_60),
                bet == "seven",
                Gold,
                enabled = !rolling
            ) { bet = "seven" }
            BetTile(
                stringResource(R.string.mini_dice_over),
                stringResource(R.string.mini_dice_over_desc),
                stringResource(R.string.mini_dice_multiplier_18),
                bet == "over",
                ChipRed,
                enabled = !rolling
            ) { bet = "over" }
        }

        BigPushButton(
            text = if (rolling) {
                stringResource(R.string.mini_dice_rolling)
            } else {
                stringResource(R.string.mini_dice_roll)
            },
            color = Gold,
            enabled = !rolling,
            onClick = {
                if (cashBalance < stake) {
                    onResult(0.0, context.getString(R.string.mini_dice_msg_insufficient))
                } else {
                    resultText = null
                    rolling = true
                }
            }
        )
    }
}

@Composable
private fun Die(value: Int, rolling: Boolean) {
    val transition = rememberInfiniteTransition(label = "die")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(420, easing = LinearEasing)),
        label = "die_rotation"
    )

    Box(
        modifier = Modifier
            .size(100.dp)
            .rotate(if (rolling) rotation else 0f)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(PureWhite, DiceFace)))
            .border(3.dp, Gold, RoundedCornerShape(16.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val dots = when (value) {
                1 -> listOf(0.5f to 0.5f)
                2 -> listOf(0.25f to 0.25f, 0.75f to 0.75f)
                3 -> listOf(0.25f to 0.25f, 0.5f to 0.5f, 0.75f to 0.75f)
                4 -> listOf(0.25f to 0.25f, 0.75f to 0.25f, 0.25f to 0.75f, 0.75f to 0.75f)
                5 -> listOf(0.25f to 0.25f, 0.75f to 0.25f, 0.5f to 0.5f, 0.25f to 0.75f, 0.75f to 0.75f)
                6 -> listOf(0.25f to 0.25f, 0.75f to 0.25f, 0.25f to 0.5f, 0.75f to 0.5f, 0.25f to 0.75f, 0.75f to 0.75f)
                else -> emptyList()
            }
            dots.forEach { (x, y) ->
                drawCircle(color = NightBlue, radius = 7.dp.toPx(), center = Offset(size.width * x, size.height * y))
            }
        }
    }
}

@Composable
private fun BetChip(value: Int, selected: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) Gold.copy(alpha = 0.15f) else NightBlue.copy(alpha = 0.6f),
        border = BorderStroke(2.dp, if (selected) Gold else PureWhite.copy(alpha = 0.10f))
    ) {
        Text(
            text = value.toString(),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.headlineSmall.copy(
                color = PureWhite,
                fontWeight = FontWeight.ExtraBold
            )
        )
    }
}

@Composable
private fun BetTile(
    label: String,
    desc: String,
    mult: String,
    active: Boolean,
    color: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (active) color.copy(alpha = 0.18f) else NightBlue.copy(alpha = 0.5f),
        border = BorderStroke(2.dp, if (active) color else PureWhite.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = PureWhite,
                    fontWeight = FontWeight.ExtraBold
                )
            )
            Text(text = desc, style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
            Text(
                text = mult,
                style = MaterialTheme.typography.titleMedium.copy(color = color, fontWeight = FontWeight.ExtraBold)
            )
        }
    }
}

@Composable
private fun BigPushButton(
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
            .fillMaxWidth()
            .height(56.dp)
            .alpha(if (enabled) 1f else 0.4f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 6.dp)
                .background(shadowColor, RoundedCornerShape(16.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color)
                .clickable(enabled = enabled, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = textColor,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                )
            )
        }
    }
}
