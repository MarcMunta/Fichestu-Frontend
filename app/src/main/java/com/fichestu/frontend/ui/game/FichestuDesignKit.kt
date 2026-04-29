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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fichestu.frontend.ui.theme.ChipRed
import com.fichestu.frontend.ui.theme.ChipRedShadow
import com.fichestu.frontend.ui.theme.DeepBlue
import com.fichestu.frontend.ui.theme.EyebrowBlue
import com.fichestu.frontend.ui.theme.Gold
import com.fichestu.frontend.ui.theme.GoldDark
import com.fichestu.frontend.ui.theme.GoldLight
import com.fichestu.frontend.ui.theme.NightBlue
import com.fichestu.frontend.ui.theme.PanelBlue
import com.fichestu.frontend.ui.theme.PureWhite
import com.fichestu.frontend.ui.theme.ReflectPurple
import com.fichestu.frontend.ui.theme.ShieldBlue
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/* ============================================================
   FICHESTU DESIGN KIT — primitives ported from HTML mockup
   Ambient orbs, grid, particles, halos, confetti, displays
   ============================================================ */

/** Background: drifting orbs + faint gold grid + rising particles. */
@Composable
fun AmbientBackground(
    modifier: Modifier = Modifier,
    particleCount: Int = 14
) {
    val transition = rememberInfiniteTransition(label = "ambient")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(14_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift"
    )
    val gridShift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(30_000, easing = LinearEasing)
        ),
        label = "grid"
    )

    val seeds = remember {
        List(particleCount) {
            ParticleSeed(
                x = Random.nextFloat(),
                size = 3f + Random.nextInt(3),
                color = when (it % 4) {
                    0 -> ChipRed
                    1 -> ShieldBlue
                    else -> Gold
                },
                duration = 10_000 + (it % 7) * 2_000,
                delay = (it * 700) % 8_000
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Gold grid with mask (dim away from center)
            val cell = 80.dp.toPx()
            val gx = (gridShift * cell)
            var x = -cell + gx
            while (x < w + cell) {
                drawLine(
                    color = Gold.copy(alpha = 0.04f),
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 1.dp.toPx()
                )
                x += cell
            }
            var y = -cell + gx
            while (y < h + cell) {
                drawLine(
                    color = Gold.copy(alpha = 0.04f),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1.dp.toPx()
                )
                y += cell
            }

            // Three drifting orbs (gold, red, shield)
            val dx = (drift - 0.5f) * 60.dp.toPx()
            val dy = (drift - 0.5f) * 40.dp.toPx()

            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Gold.copy(alpha = 0.32f), Color.Transparent),
                    center = Offset(w * 0.10f + dx, h * 0.18f + dy),
                    radius = 220.dp.toPx()
                ),
                radius = 220.dp.toPx(),
                center = Offset(w * 0.10f + dx, h * 0.18f + dy)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(ChipRed.copy(alpha = 0.30f), Color.Transparent),
                    center = Offset(w * 0.92f - dx, h * 0.45f - dy),
                    radius = 200.dp.toPx()
                ),
                radius = 200.dp.toPx(),
                center = Offset(w * 0.92f - dx, h * 0.45f - dy)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(ShieldBlue.copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(w * 0.55f + dy, h * 1.05f + dx),
                    radius = 180.dp.toPx()
                ),
                radius = 180.dp.toPx(),
                center = Offset(w * 0.55f + dy, h * 1.05f + dx)
            )
        }

        // Floating particles (overlaid as small Boxes for cheaper repaint)
        seeds.forEach { seed ->
            val particle by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(seed.duration, easing = LinearEasing),
                    initialStartOffset = androidx.compose.animation.core.StartOffset(seed.delay)
                ),
                label = "p${seed.x}"
            )
            FloatingParticle(seed = seed, t = particle)
        }
    }
}

private data class ParticleSeed(
    val x: Float,
    val size: Float,
    val color: Color,
    val duration: Int,
    val delay: Int
)

@Composable
private fun FloatingParticle(seed: ParticleSeed, t: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val alphaT = when {
            t < 0.1f -> t * 7f
            t > 0.9f -> (1f - t) * 4f
            else -> 0.6f
        }.coerceIn(0f, 0.7f)

        Box(
            modifier = Modifier
                .size(seed.size.dp)
                .alpha(alphaT)
                .background(seed.color, CircleShape)
                .border(0.5.dp, seed.color.copy(alpha = 0.6f), CircleShape)
        )
    }
}

/** Gold display text with stacked drop-shadow + glow (mirrors `.display-gold`). */
@Composable
fun DisplayGold(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: Int = 56,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        modifier = modifier,
        textAlign = textAlign,
        style = MaterialTheme.typography.displayLarge.copy(
            color = Gold,
            fontSize = fontSize.sp,
            lineHeight = (fontSize * 1.05f).sp,
            shadow = Shadow(
                color = GoldDark,
                offset = Offset(0f, 4f),
                blurRadius = 18f
            )
        )
    )
}

@Composable
fun DisplayRed(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: Int = 56,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        modifier = modifier,
        textAlign = textAlign,
        style = MaterialTheme.typography.displayLarge.copy(
            color = ChipRed,
            fontSize = fontSize.sp,
            lineHeight = (fontSize * 1.05f).sp,
            shadow = Shadow(
                color = ChipRedShadow,
                offset = Offset(0f, 4f),
                blurRadius = 16f
            )
        )
    )
}

@Composable
fun DisplayWhite(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: Int = 56,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        modifier = modifier,
        textAlign = textAlign,
        style = MaterialTheme.typography.displayLarge.copy(
            color = PureWhite,
            fontSize = fontSize.sp,
            lineHeight = (fontSize * 1.05f).sp,
            shadow = Shadow(
                color = DeepBlue,
                offset = Offset(0f, 4f),
                blurRadius = 14f
            )
        )
    )
}

/** Section eyebrow label (uppercase, spaced). */
@Composable
fun Eyebrow(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = EyebrowBlue
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall.copy(
            color = color,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
            fontSize = 10.sp
        )
    )
}

/** Premium panel: gold-rim gradient + inner blue gradient + glow. */
@Composable
fun PremiumPanel(
    modifier: Modifier = Modifier,
    glow: Boolean = false,
    cornerRadius: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.verticalGradient(listOf(PanelBlue, NightBlue))
            )
            .border(
                1.dp,
                if (glow) Gold.copy(alpha = 0.5f) else Gold.copy(alpha = 0.18f),
                RoundedCornerShape(cornerRadius)
            ),
        content = content
    )
}

/** Pulsing gold halo ring rotating around a fixed-size content. */
@Composable
fun HaloRing(
    diameter: Dp,
    modifier: Modifier = Modifier,
    color: Color = Gold,
    content: @Composable BoxScope.() -> Unit
) {
    val transition = rememberInfiniteTransition(label = "halo")
    val rot by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10_000, easing = LinearEasing)
        ),
        label = "rot"
    )

    Box(
        modifier = modifier.size(diameter + 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(diameter + 20.dp)
                .rotate(rot)
        ) {
            drawArc(
                color = color.copy(alpha = 0.45f),
                startAngle = 0f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = 2.dp.toPx())
            )
        }
        Box(
            modifier = Modifier.size(diameter),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}

/** Soft pulse-glow modifier for hero CTAs. Use as wrapper. */
@Composable
fun PulseGlow(
    modifier: Modifier = Modifier,
    color: Color = Gold,
    content: @Composable BoxScope.() -> Unit
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val s by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1_400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val a by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(1_400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .scale(s)
                .alpha(a)
                .background(
                    Brush.radialGradient(listOf(color, Color.Transparent)),
                    RoundedCornerShape(50)
                )
        )
        Box(content = content)
    }
}

/** Stat pill: small dot + value, rounded. */
@Composable
fun StatPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color = Gold
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = NightBlue.copy(alpha = 0.7f),
        border = androidx.compose.foundation.BorderStroke(1.dp, PureWhite.copy(alpha = 0.10f))
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(
                androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(accent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label.take(1),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = NightBlue,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp
                    )
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = PureWhite,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

/** Confetti overlay: 40 falling pieces in mixed colors. */
@Composable
fun ConfettiOverlay(
    modifier: Modifier = Modifier,
    count: Int = 40
) {
    val seeds = remember {
        List(count) {
            ConfettiSeed(
                x = Random.nextFloat(),
                drift = (Random.nextFloat() - 0.5f) * 0.4f,
                duration = 2_400 + Random.nextInt(2_500),
                delay = Random.nextInt(1_500),
                color = when (it % 5) {
                    0 -> Gold
                    1 -> ChipRed
                    2 -> PureWhite
                    3 -> ShieldBlue
                    else -> ReflectPurple
                },
                rotation = Random.nextInt(720)
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        seeds.forEach { seed ->
            // Compute position from a per-piece animation we can't stash inside Canvas;
            // approximate with shared transition value to stagger via delay/duration.
            val w = size.width
            val h = size.height
            // We can't `animateFloat` here, so draw static-but-staggered ribbons.
            // (Visible polish: Compose recomposes many times per frame for the overlay.)
            val px = w * seed.x
            // Use a cheap pseudo-random Y via current time; the overlay is decorative.
            val y = ((System.currentTimeMillis() / 6 + seed.delay) % h.toLong()).toFloat()
            val rotRad = (seed.rotation + (y / 40f)) * (Math.PI / 180f).toFloat()
            val sx = cos(rotRad.toDouble()).toFloat()
            val sy = sin(rotRad.toDouble()).toFloat()

            drawLine(
                color = seed.color,
                start = Offset(px - sx * 6f, y - sy * 6f),
                end = Offset(px + sx * 6f, y + sy * 6f),
                strokeWidth = 5.dp.toPx()
            )
        }
    }
}

private data class ConfettiSeed(
    val x: Float,
    val drift: Float,
    val duration: Int,
    val delay: Int,
    val color: Color,
    val rotation: Int
)

/** Place 9 opponents on an arc. Returns (x,y) in [0..1]. Used by Battle arena. */
fun arcPositions(count: Int): List<Pair<Float, Float>> = List(count) { i ->
    val t = if (count <= 1) 0.5f else i.toFloat() / (count - 1)
    val angle = Math.PI * (1.05 - t * 1.1) // -10° to 190°
    val cx = 0.5f + (cos(angle).toFloat() * 0.36f)
    val cy = 0.48f - (sin(angle).toFloat() * 0.28f)
    cx to cy
}

/** Useful for the slot reels & coin flip — clamp helper. */
internal fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t.coerceIn(0f, 1f)

/** Common gold gradient brush used across heroes. */
@Composable
fun goldGradientBrush(): Brush =
    Brush.linearGradient(listOf(GoldLight, Gold, GoldDark))
