package com.fichestu.frontend.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fichestu.frontend.ui.theme.CasinoAuthScaffold
import com.fichestu.frontend.ui.theme.CasinoOutlinedTextField
import com.fichestu.frontend.ui.theme.FichestuTheme
import com.fichestu.frontend.ui.theme.Gold
import com.fichestu.frontend.ui.theme.GoldPrimaryButton
import com.fichestu.frontend.ui.theme.TextSecondary

private enum class ForgotStep { IDLE, SENT }

// ─────────────────────────────────────────────────────────────────────────────
// ForgotPasswordScreen — solo UI, sin lógica de negocio
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ForgotPasswordScreen(onBack: () -> Unit) {
    var step  by rememberSaveable { mutableStateOf(ForgotStep.IDLE) }
    var email by rememberSaveable { mutableStateOf("") }
    val isEmailValid = email.contains("@") && email.contains(".")

    CasinoAuthScaffold(
        title    = "FICHESTU",
        subtitle = "RECUPERAR ACCESO"
    ) {
        // ── Botón volver ──────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onBack() }
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = Gold,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text  = "Volver",
                style = MaterialTheme.typography.bodySmall.copy(
                    color          = Gold,
                    fontWeight     = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline
                )
            )
        }

        // ── Contenido animado: IDLE ↔ SENT ───────────────────────────────
        AnimatedContent(
            targetState   = step,
            transitionSpec = {
                (fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 3 })
                    .togetherWith(fadeOut(tween(200)))
            },
            label = "forgot_step"
        ) { currentStep ->
            when (currentStep) {
                ForgotStep.IDLE -> ForgotIdleContent(
                    email          = email,
                    onEmailChange  = { email = it },
                    isEmailValid   = isEmailValid,
                    onSend         = { step = ForgotStep.SENT }
                )
                ForgotStep.SENT -> ForgotSentContent(
                    email  = email,
                    onBack = onBack
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Paso 1 — Formulario de email
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ForgotIdleContent(
    email: String,
    onEmailChange: (String) -> Unit,
    isEmailValid: Boolean,
    onSend: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text  = "¿Olvidaste tu contraseña?",
            style = MaterialTheme.typography.headlineSmall.copy(
                color      = Gold,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text  = "Introduce tu email y te enviaremos un enlace para restablecerla.",
            style = MaterialTheme.typography.bodySmall.copy(
                color      = TextSecondary,
                lineHeight = 20.sp
            )
        )
        Spacer(Modifier.height(4.dp))
        CasinoOutlinedTextField(
            value         = email,
            onValueChange = onEmailChange,
            label         = "Email registrado",
            leadingIcon   = {
                Icon(Icons.Default.Email, null, modifier = Modifier.size(20.dp))
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(Modifier.height(4.dp))
        GoldPrimaryButton(
            text    = "ENVIAR ENLACE",
            onClick  = onSend,
            enabled  = isEmailValid
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Paso 2 — Confirmación
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ForgotSentContent(email: String, onBack: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Icono check animado
        AnimatedVisibility(
            visible = true,
            enter   = scaleIn(
                spring(dampingRatio = Spring.DampingRatioMediumBouncy,
                       stiffness    = Spring.StiffnessLow)
            ) + fadeIn(tween(300))
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint     = Gold,
                modifier = Modifier.size(72.dp)
            )
        }

        Text(
            text  = "¡Enlace enviado!",
            style = MaterialTheme.typography.headlineSmall.copy(
                color      = Gold,
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = TextSecondary)) { append("Se ha enviado un email a ") }
                withStyle(SpanStyle(color = Gold, fontWeight = FontWeight.SemiBold)) {
                    append(email)
                }
                withStyle(SpanStyle(color = TextSecondary)) {
                    append("\nRevisa tu bandeja de entrada y la carpeta de spam.")
                }
            },
            style     = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
            textAlign = TextAlign.Center,
            modifier  = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        GoldPrimaryButton(text = "VOLVER AL LOGIN", onClick = onBack)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────
@Preview(name = "ForgotPassword – Idle", showBackground = true,
    backgroundColor = 0xFF0B1424, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewForgotIdle() {
    FichestuTheme { ForgotPasswordScreen(onBack = {}) }
}

@Preview(name = "ForgotPassword – Sent", showBackground = true,
    backgroundColor = 0xFF0B1424, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewForgotSent() {
    FichestuTheme {
        CasinoAuthScaffold("FICHESTU", "RECUPERAR ACCESO") {
            ForgotSentContent(email = "usuario@demo.com", onBack = {})
        }
    }
}
