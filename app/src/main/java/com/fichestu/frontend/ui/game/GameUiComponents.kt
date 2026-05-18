package com.fichestu.frontend.ui.game

import android.graphics.Paint
import android.graphics.Typeface
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
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
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
import kotlin.math.abs
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
    val shape = RoundedCornerShape(8.dp)
    Surface(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = Gold.copy(alpha = 0.10f),
                spotColor = Gold.copy(alpha = 0.08f)
            )
            .border(1.dp, CardBorder, shape),
        shape = shape,
        color = PanelBlue.copy(alpha = 0.90f)
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

    val shape = RoundedCornerShape(8.dp)

    Box(modifier = modifier.height(52.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = 3.dp)
                .background(bottomColor, shape)
        )

        Button(
            onClick = onClick,
            enabled = enabled,
            interactionSource = interactionSource,
            shape = shape,
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
                .border(1.dp, Gold.copy(alpha = 0.28f), shape)
                .background(topBrush, shape)
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
    glowColor: Color = Gold.copy(alpha = 0.24f),
    showLabels: Boolean = true
) {
    Canvas(modifier = modifier) {
        if (values.isEmpty()) return@Canvas

        val safeMax = values.maxOrNull() ?: 1.0
        val safeMin = values.minOrNull() ?: 0.0
        val rawRange = (safeMax - safeMin).takeIf { it > 0.0 } ?: (safeMax.takeIf { it > 0.0 } ?: 1.0)
        val paddingY = rawRange * 0.10
        val minY = (safeMin - paddingY).coerceAtLeast(0.0)
        val maxY = safeMax + paddingY
        val range = (maxY - minY).takeIf { it > 0.0 } ?: 1.0
        val leftPad = if (showLabels) 48.dp.toPx() else 4.dp.toPx()
        val rightPad = 8.dp.toPx()
        val topPad = 8.dp.toPx()
        val bottomPad = if (showLabels) 20.dp.toPx() else 6.dp.toPx()
        val chartWidth = (size.width - leftPad - rightPad).coerceAtLeast(1f)
        val chartHeight = (size.height - topPad - bottomPad).coerceAtLeast(1f)
        val stepX = if (values.size <= 1) 0f else chartWidth / (values.size - 1)

        val points = values.mapIndexed { index, value ->
            val normalized = ((value - minY) / range).toFloat()
            val y = topPad + chartHeight - normalized * chartHeight
            Offset(leftPad + index * stepX, y)
        }

        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }

        val fillPath = Path().apply {
            addPath(path)
            lineTo(leftPad + chartWidth, topPad + chartHeight)
            lineTo(leftPad, topPad + chartHeight)
            close()
        }

        val gridLines = 4
        repeat(gridLines + 1) { index ->
            val y = topPad + (chartHeight / gridLines) * index
            drawLine(
                color = PureWhite.copy(alpha = if (index == gridLines) 0.16f else 0.08f),
                start = Offset(leftPad, y),
                end = Offset(leftPad + chartWidth, y),
                strokeWidth = 1.dp.toPx()
            )
        }
        repeat(5) { index ->
            val x = leftPad + (chartWidth / 4f) * index
            drawLine(
                color = PureWhite.copy(alpha = 0.05f),
                start = Offset(x, topPad),
                end = Offset(x, topPad + chartHeight),
                strokeWidth = 1.dp.toPx()
            )
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                listOf(glowColor, Color.Transparent),
                startY = topPad,
                endY = topPad + chartHeight
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

        points.firstOrNull()?.let { point ->
            drawCircle(
                color = lineColor.copy(alpha = 0.42f),
                radius = 4.dp.toPx(),
                center = point
            )
        }
        points.lastOrNull()?.let { point ->
            drawCircle(
                color = lineColor,
                radius = 4.5.dp.toPx(),
                center = point
            )
        }

        if (showLabels) {
            val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.argb(178, 169, 182, 211)
                textSize = 10.sp.toPx()
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
            }
            drawIntoCanvas { canvas ->
                val native = canvas.nativeCanvas
                native.drawText(formatAxisValue(maxY), leftPad - 6.dp.toPx(), topPad + 4.dp.toPx(), labelPaint)
                native.drawText(formatAxisValue((maxY + minY) / 2), leftPad - 6.dp.toPx(), topPad + chartHeight / 2 + 4.dp.toPx(), labelPaint)
                native.drawText(formatAxisValue(minY), leftPad - 6.dp.toPx(), topPad + chartHeight + 4.dp.toPx(), labelPaint)
            }
        }
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
    return String.format(Locale.US, "%.2f FTC", value)
}

fun formatPercent(value: Double): String {
    val sign = if (value >= 0.0) "+" else ""
    return String.format(Locale.US, "%s%.2f%%", sign, value)
}

fun formatQuantity(value: Double): String {
    return if (abs(value - value.toInt()) < 0.0001) {
        value.toInt().toString()
    } else {
        String.format(Locale.US, "%.2f", value)
    }
}

private fun formatAxisValue(value: Double): String {
    return when {
        value >= 1000.0 -> String.format(Locale.US, "%.1fk", value / 1000.0)
        else -> String.format(Locale.US, "%.0f", value)
    }
}
