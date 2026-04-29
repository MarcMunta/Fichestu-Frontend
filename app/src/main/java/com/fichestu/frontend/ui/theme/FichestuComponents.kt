package com.fichestu.frontend.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Enumerado para el estado de los tabs ─────────────────────────────────
enum class AuthTab { LOGIN, REGISTER }

// ─────────────────────────────────────────────────────────────────────────────
// FichestuAuthScaffold
// Fondo radial degradado DeepBlue → NightBlue + overlay dorado tenue,
// centrado vertical con la AuthCard interior rodeando el contenido.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AuthScaffold(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Fondo radial
    val backgroundBrush = Brush.radialGradient(
        colors = listOf(DeepBlue, NightBlue),
        center = Offset(0.5f * 1080f, 0.3f * 1920f),
        radius = 1400f
    )

    // ── Animación de líneas tipo ficha ───────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "chip_lines")
    // offset lateral: avanza de 0 a 1 de forma continua → movimiento suave y sin salto
    val lineOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "chip_offset"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        // ── Líneas rojas diagonales animadas (estética ficha de casino) ───
        Canvas(modifier = Modifier.fillMaxSize()) {
            val spacingPx   = 72.dp.toPx()
            val strokePx    = 2.2.dp.toPx()
            val diagLen     = size.height    // longitud suficiente para cruzar la pantalla
            val shift       = lineOffset * spacingPx
            // Ángulo de ~35° → dx/dy = tan(35°) ≈ 0.70
            val slope       = 0.70f
            val lineColor   = ChipRed.copy(alpha = 0.13f)
            val lineColor2  = ChipRed.copy(alpha = 0.07f)

            // ── Set 1: líneas hacia la derecha-abajo ──────────────────────
            var startX = -diagLen * slope - spacingPx + shift
            while (startX < size.width + spacingPx) {
                val x0 = startX
                val y0 = 0f
                val x1 = startX + diagLen * slope
                val y1 = diagLen
                drawLine(
                    color       = lineColor,
                    start       = Offset(x0, y0),
                    end         = Offset(x1, y1),
                    strokeWidth = strokePx,
                    cap         = StrokeCap.Round
                )
                startX += spacingPx
            }

            // ── Set 2: líneas opuestas (izq-abajo), más sutiles ───────────
            var startX2 = -spacingPx + shift * 0.6f
            while (startX2 < size.width + diagLen * slope + spacingPx) {
                val x0 = startX2
                val y0 = 0f
                val x1 = startX2 - diagLen * slope
                val y1 = diagLen
                drawLine(
                    color       = lineColor2,
                    start       = Offset(x0, y0),
                    end         = Offset(x1, y1),
                    strokeWidth = strokePx,
                    cap         = StrokeCap.Round
                )
                startX2 += spacingPx * 1.5f
            }
        }

        // Overlay dorado sutil
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GoldOverlay)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 324.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Animaciones de entrada: título + card ─────────────────────
            var headerVisible by remember { mutableStateOf(false) }
            var cardVisible   by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                headerVisible = true
                kotlinx.coroutines.delay(180)
                cardVisible   = true
            }

            // ── Título de la brand ────────────────────────────────────────
            AnimatedVisibility(
                visible = headerVisible,
                enter   = fadeIn(tween(400)) +
                          slideInVertically(tween(400)) { -it / 2 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.displayMedium.copy(
                            color = Gold,
                            shadow = Shadow(
                                color = GoldDark.copy(alpha = 0.9f),
                                offset = Offset(0f, 3f),
                                blurRadius = 8f
                            )
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondary,
                            letterSpacing = 2.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Auth Card con entrada elástica ────────────────────────────
            AnimatedVisibility(
                visible = cardVisible,
                enter   = fadeIn(tween(480)) +
                          scaleIn(
                              spring(dampingRatio = Spring.DampingRatioMediumBouncy,
                                     stiffness    = Spring.StiffnessLow),
                              initialScale = 0.92f
                          ) +
                          slideInVertically(tween(480)) { it / 4 }
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 24.dp,
                            shape     = RoundedCornerShape(20.dp),
                            ambientColor = Gold.copy(alpha = 0.15f),
                            spotColor    = Gold.copy(alpha = 0.10f)
                        )
                        .border(
                            width  = 1.dp,
                            brush  = Brush.linearGradient(
                                colors = listOf(GoldBorder, CardBorder, GoldBorder)
                            ),
                            shape  = RoundedCornerShape(20.dp)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    color = PanelBlue.copy(alpha = 0.90f),
                    tonalElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        content()
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ChipAuthTabs — dos chips tipo "ficha de casino" (LOGIN / REGISTER)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ChipAuthTabs(
    selected: AuthTab,
    onSelect: (AuthTab) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChipTabItem(
            label    = "LOGIN",
            isActive = selected == AuthTab.LOGIN,
            enabled  = enabled,
            modifier = Modifier.weight(1f),
            onClick  = { onSelect(AuthTab.LOGIN) }
        )
        ChipTabItem(
            label    = "REGISTRO",
            isActive = selected == AuthTab.REGISTER,
            enabled  = enabled,
            modifier = Modifier.weight(1f),
            onClick  = { onSelect(AuthTab.REGISTER) }
        )
    }
}

@Composable
private fun ChipTabItem(
    label: String,
    isActive: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(100),
        label = "chip_scale"
    )

    val chipBackground = if (isActive) {
        Brush.linearGradient(listOf(GoldLight, Gold, GoldDark))
    } else {
        Brush.linearGradient(listOf(DeepBlue, PanelBlue))
    }
    val borderColor = if (isActive) Gold.copy(alpha = 0.8f) else InputBorder.copy(alpha = 0.5f)
    val textColor   = if (isActive) NightBlue else TextSecondary

    Box(
        modifier = modifier
            .scale(scale)
            .height(44.dp)
            .border(
                width  = 1.5.dp,
                color  = borderColor,
                shape  = RoundedCornerShape(22.dp)
            )
            .background(chipBackground, RoundedCornerShape(22.dp)),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick             = onClick,
            enabled             = enabled,
            interactionSource   = interactionSource,
            colors              = ButtonDefaults.buttonColors(
                containerColor          = Color.Transparent,
                contentColor            = textColor,
                disabledContainerColor  = Color.Transparent,
                disabledContentColor    = textColor.copy(alpha = 0.5f)
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = label,
                style = TextStyle(
                    fontWeight    = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    fontSize      = 13.sp,
                    letterSpacing = 1.2.sp,
                    color         = textColor
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// GoldPrimaryButton — CTA principal con degradado dorado
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun GoldPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "cta_scale"
    )

    val gradient = Brush.linearGradient(
        colors = if (enabled) listOf(GoldLight, Gold, GoldDark)
                 else listOf(InputBorder, InputBg)
    )

    Box(
        modifier = modifier
            .scale(scale)
            .fillMaxWidth()
            .height(52.dp)
            .shadow(
                elevation = if (enabled) 8.dp else 0.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Gold.copy(alpha = 0.25f),
                spotColor    = Gold.copy(alpha = 0.20f)
            )
            .border(
                width = 1.dp,
                color = if (enabled) GoldLight.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .background(gradient, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick           = onClick,
            enabled           = enabled,
            interactionSource = interactionSource,
            colors = ButtonDefaults.buttonColors(
                containerColor         = Color.Transparent,
                contentColor           = NightBlue,
                disabledContainerColor = Color.Transparent,
                disabledContentColor   = TextSecondary
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text  = text,
                style = TextStyle(
                    fontWeight    = FontWeight.Bold,
                    fontSize      = 15.sp,
                    letterSpacing = 2.sp,
                    color         = if (enabled) NightBlue else TextSecondary
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CasinoOutlinedTextField — campo de texto con estética casino
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun CasinoOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    minLines: Int = 1
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value            = value,
            onValueChange    = onValueChange,
            label            = {
                Text(
                    text  = label,
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            enabled              = enabled,
            isError              = isError,
            singleLine           = singleLine,
            minLines             = minLines,
            visualTransformation = visualTransformation,
            keyboardOptions      = keyboardOptions,
            keyboardActions      = keyboardActions,
            trailingIcon         = trailingIcon,
            leadingIcon          = leadingIcon,
            shape                = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                // Text
                focusedTextColor    = PureWhite,
                unfocusedTextColor  = PureWhite,
                disabledTextColor   = TextSecondary,
                errorTextColor      = PureWhite,
                // Container
                focusedContainerColor    = InputBg,
                unfocusedContainerColor  = InputBg,
                disabledContainerColor   = InputBg.copy(alpha = 0.5f),
                errorContainerColor      = InputBg,
                // Border
                focusedBorderColor   = Gold,
                unfocusedBorderColor = InputBorder,
                disabledBorderColor  = InputBorder.copy(alpha = 0.3f),
                errorBorderColor     = ChipRed,
                // Label
                focusedLabelColor    = Gold,
                unfocusedLabelColor  = TextSecondary,
                errorLabelColor      = ChipRed,
                // Cursor
                cursorColor          = Gold,
                errorCursorColor     = ChipRed,
                // Supporting text
                focusedSupportingTextColor   = TextSecondary,
                unfocusedSupportingTextColor = TextSecondary,
                errorSupportingTextColor     = ChipRed,
                // Trailing icon
                focusedTrailingIconColor    = Gold,
                unfocusedTrailingIconColor  = TextSecondary,
                errorTrailingIconColor      = ChipRed,
                // Leading icon
                focusedLeadingIconColor     = Gold,
                unfocusedLeadingIconColor   = TextSecondary,
            )
        )

        if (!supportingText.isNullOrBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(
                text  = supportingText,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = if (isError) ChipRed else TextSecondary
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}
