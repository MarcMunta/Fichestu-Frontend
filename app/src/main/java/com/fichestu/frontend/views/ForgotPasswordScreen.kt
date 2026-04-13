package com.fichestu.frontend.views

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
import androidx.compose.ui.res.stringResource
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
import com.fichestu.frontend.R
import com.fichestu.frontend.ui.theme.AuthScaffold
import com.fichestu.frontend.ui.theme.CasinoOutlinedTextField
import com.fichestu.frontend.ui.theme.FichestuTheme
import com.fichestu.frontend.ui.theme.Gold
import com.fichestu.frontend.ui.theme.GoldPrimaryButton
import com.fichestu.frontend.ui.theme.TextSecondary

private enum class ForgotStep { IDLE, SENT }

@Composable
fun ForgotPasswordScreen(onBack: () -> Unit) {
    var currentStep by rememberSaveable { mutableStateOf(ForgotStep.IDLE) }
    var email by rememberSaveable { mutableStateOf("") }

    AuthScaffold(
        title = stringResource(R.string.auth_title),
        subtitle = stringResource(R.string.forgot_title)
    ) {
        BackButton(onClick = onBack)

        Spacer(Modifier.height(16.dp))

        AnimatedContent(
            targetState = currentStep,
            label = "forgot_transition"
        ) { step ->
            when (step) {
                ForgotStep.IDLE -> ForgotFormContent(
                    email = email,
                    onEmailChange = { email = it },
                    onSend = { currentStep = ForgotStep.SENT }
                )
                ForgotStep.SENT -> ForgotSentContent(
                    email = email,
                    onBack = onBack
                )
            }
        }
    }
}

// ─── PASO 1: FORMULARIO (IDLE) ────────────────────────────────────
@Composable
private fun ForgotFormContent(
    email: String,
    onEmailChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val isEmailValid = email.contains("@") && email.contains(".")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.forgot_header),
            style = MaterialTheme.typography.titleLarge.copy(
                color = Gold,
                fontWeight = FontWeight.Bold
            )
        )

        Text(
            text = stringResource(R.string.forgot_description),
            color = TextSecondary
        )

        CasinoOutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = stringResource(R.string.forgot_label_email),
            leadingIcon = { Icon(Icons.Default.Email, null, Modifier.size(20.dp)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        GoldPrimaryButton(
            text = stringResource(R.string.forgot_btn_send),
            onClick = onSend,
            enabled = isEmailValid
        )
    }
}

// ─── PASO 2: ÉXITO (SENT) ─────────────────────────────────────────
@Composable
private fun ForgotSentContent(email: String, onBack: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Gold,
            modifier = Modifier.size(64.dp)
        )

        Text(
            text = stringResource(R.string.forgot_success_title),
            style = MaterialTheme.typography.titleLarge.copy(
                color = Gold,
                fontWeight = FontWeight.Bold
            )
        )

        Text(
            text = stringResource(R.string.forgot_success_desc, email),
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        GoldPrimaryButton(
            text = stringResource(R.string.forgot_btn_back_login),
            onClick = onBack
        )
    }
}

// ─── COMPONENTE REUTILIZABLE: BOTÓN VOLVER ────────────────────────
@Composable
private fun BackButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
            tint = Gold,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = stringResource(R.string.forgot_back),
            color = Gold,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline
            )
        )
    }
}
