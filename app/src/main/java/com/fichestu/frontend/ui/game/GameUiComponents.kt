package com.fichestu.frontend.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fichestu.frontend.ui.theme.CardBorder
import com.fichestu.frontend.ui.theme.DeepBlue
import com.fichestu.frontend.ui.theme.Gold
import com.fichestu.frontend.ui.theme.GoldDark
import com.fichestu.frontend.ui.theme.GoldLight
import com.fichestu.frontend.ui.theme.InputBg
import com.fichestu.frontend.ui.theme.NightBlue
import com.fichestu.frontend.ui.theme.PanelBlue
import com.fichestu.frontend.ui.theme.PureWhite
import java.util.Locale

@Composable
fun BubbleText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.displaySmall,
    fillColor: Color = Gold,
    outlineColor: Color = DeepBlue,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE,
    outlineWidth: Dp = 1.2.dp
) {
    Box(modifier = modifier) {
        val offsets = listOf(
            Offset(-1f, -1f),
            Offset(1f, -1f),
            Offset(-1f, 1f),
            Offset(1f, 1f)
        )

        offsets.forEach { axis ->
            Text(
                text = text,
                style = style.copy(color = outlineColor),
                textAlign = textAlign,
                maxLines = maxLines,
                modifier = Modifier.offset(
                    x = outlineWidth * axis.x,
                    y = outlineWidth * axis.y
                )
            )
        }

        Text(
            text = text,
            style = style.copy(
                color = fillColor,
                shadow = Shadow(
                    color = GoldDark.copy(alpha = 0.8f),
                    offset = Offset(0f, 5f),
                    blurRadius = 6f
                )
            ),
            textAlign = textAlign,
            maxLines = maxLines
        )
    }
}

@Composable
fun ArcadePanel(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Gold.copy(alpha = 0.18f),
                spotColor = Gold.copy(alpha = 0.12f)
            )
            .border(1.5.dp, CardBorder, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = PanelBlue.copy(alpha = 0.93f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun ArcadePrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    ArcadeButtonBase(
        text = text,
        modifier = modifier,
        enabled = enabled,
        topBrush = if (enabled) {
            Brush.linearGradient(listOf(GoldLight, Gold, GoldDark))
        } else {
            Brush.linearGradient(listOf(InputBg, PanelBlue))
        },
        bottomColor = DeepBlue.copy(alpha = 0.9f),
        textColor = if (enabled) NightBlue else PureWhite.copy(alpha = 0.5f),
        onClick = onClick
    )
}

@Composable
fun ArcadeSecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    ArcadeButtonBase(
        text = text,
        modifier = modifier,
        enabled = enabled,
        topBrush = Brush.linearGradient(listOf(DeepBlue, PanelBlue)),
        bottomColor = NightBlue.copy(alpha = 0.9f),
        textColor = if (enabled) PureWhite else PureWhite.copy(alpha = 0.5f),
        onClick = onClick
    )
}

@Composable
private fun ArcadeButtonBase(
    text: String,
    modifier: Modifier,
    enabled: Boolean,
    topBrush: Brush,
    bottomColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val offsetY = if (isPressed && enabled) 4.dp else 0.dp

    Box(modifier = modifier.height(56.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = 4.dp)
                .background(bottomColor, RoundedCornerShape(18.dp))
        )

        Button(
            onClick = onClick,
            enabled = enabled,
            interactionSource = interactionSource,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = textColor,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = textColor
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
            modifier = Modifier
                .fillMaxSize()
                .offset(y = offsetY)
                .border(1.5.dp, Gold.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                .background(topBrush, RoundedCornerShape(18.dp))
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                )
            )
        }
    }
}

@Composable
fun TokenSparkChart(
    values: List<Double>,
    modifier: Modifier = Modifier,
    lineColor: Color = Gold,
    glowColor: Color = Gold.copy(alpha = 0.24f)
) {
    Canvas(modifier = modifier) {
        if (values.isEmpty()) return@Canvas

        val safeMax = values.maxOrNull() ?: 1.0
        val safeMin = values.minOrNull() ?: 0.0
        val range = (safeMax - safeMin).takeIf { it > 0.0 } ?: 1.0
        val stepX = if (values.size <= 1) size.width else size.width / (values.size - 1)

        val points = values.mapIndexed { index, value ->
            val normalized = ((value - safeMin) / range).toFloat()
            val y = size.height - normalized * size.height
            Offset(index * stepX, y)
        }

        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }

        val fillPath = Path().apply {
            addPath(path)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        val gridLines = 4
        repeat(gridLines + 1) { index ->
            val y = size.height / gridLines * index
            drawLine(
                color = PureWhite.copy(alpha = 0.08f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                listOf(glowColor, Color.Transparent),
                startY = 0f,
                endY = size.height
            )
        )

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = 3.5.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

@Composable
fun StatusTicker(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = Gold.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium.copy(
                color = PureWhite,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}

fun formatCurrency(value: Double): String {
    return String.format(Locale.US, "EUR %.2f", value)
}

fun formatPercent(value: Double): String {
    val sign = if (value >= 0.0) "+" else ""
    return String.format(Locale.US, "%s%.2f%%", sign, value)
}
