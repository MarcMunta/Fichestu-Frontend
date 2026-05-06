package com.fichestu.frontend.views

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.fichestu.frontend.R
import com.fichestu.frontend.data.i18n.AppI18n
import com.fichestu.frontend.data.repository.AuthRepository
import com.fichestu.frontend.ui.theme.AuthScaffold
import com.fichestu.frontend.ui.theme.CasinoOutlinedTextField
import com.fichestu.frontend.ui.theme.Gold
import com.fichestu.frontend.ui.theme.GoldPrimaryButton
import com.fichestu.frontend.ui.theme.TextSecondary
import kotlinx.coroutines.launch

private enum class ForgotStep { EMAIL, VERIFY, DONE }

@Composable
fun ForgotPasswordScreen(onBack: () -> Unit) {
    val repository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()
    var currentStep by rememberSaveable { mutableStateOf(ForgotStep.EMAIL) }
    var email by rememberSaveable { mutableStateOf("") }
    var token by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var feedback by rememberSaveable { mutableStateOf<String?>(null) }
    var isError by rememberSaveable { mutableStateOf(false) }
    var isLoading by rememberSaveable { mutableStateOf(false) }

    AuthScaffold(
        title = stringResource(R.string.auth_title),
        subtitle = stringResource(R.string.forgot_title)
    ) {
        BackButton(onClick = onBack)

        Spacer(Modifier.height(16.dp))

        feedback?.let { message ->
            Text(
                text = AppI18n.message(message) ?: message,
                color = if (isError) MaterialTheme.colorScheme.error else Gold,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                fadeIn() + slideInVertically { it / 3 } togetherWith fadeOut()
            },
            label = "forgot_transition"
        ) { step ->
            when (step) {
                ForgotStep.EMAIL -> ForgotFormContent(
                    email = email,
                    isLoading = isLoading,
                    onEmailChange = { email = it },
                    onSend = {
                        isLoading = true
                        feedback = null
                        scope.launch {
                            repository.requestPasswordReset(email.trim())
                                .onSuccess { message ->
                                    feedback = message
                                    isError = false
                                    currentStep = ForgotStep.VERIFY
                                }
                                .onFailure { error ->
                                    feedback = AppI18n.message(error.message) ?: AppI18n.text("send_code_error")
                                    isError = true
                                }
                            isLoading = false
                        }
                    }
                )
                ForgotStep.VERIFY -> ForgotVerifyContent(
                    email = email,
                    token = token,
                    newPassword = newPassword,
                    confirmPassword = confirmPassword,
                    isLoading = isLoading,
                    onTokenChange = { token = it },
                    onNewPasswordChange = { newPassword = it },
                    onConfirmPasswordChange = { confirmPassword = it },
                    onConfirm = {
                        isLoading = true
                        feedback = null
                        scope.launch {
                            repository.confirmPasswordReset(
                                email = email.trim(),
                                token = token.trim(),
                                newPassword = newPassword,
                                confirmPassword = confirmPassword
                            )
                                .onSuccess { message ->
                                    feedback = message
                                    isError = false
                                    currentStep = ForgotStep.DONE
                                }
                                .onFailure { error ->
                                    feedback = AppI18n.message(error.message) ?: AppI18n.text("password_update_error")
                                    isError = true
                                }
                            isLoading = false
                        }
                    }
                )
                ForgotStep.DONE -> ForgotDoneContent(onBack = onBack)
            }
        }
    }
}

@Composable
private fun ForgotFormContent(
    email: String,
    isLoading: Boolean,
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
            enabled = !isLoading,
            leadingIcon = { Icon(Icons.Default.Email, null, Modifier.size(20.dp)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        GoldPrimaryButton(
            text = if (isLoading) stringResource(R.string.auth_processing) else stringResource(R.string.forgot_btn_send),
            onClick = onSend,
            enabled = isEmailValid && !isLoading
        )
    }
}

@Composable
private fun ForgotVerifyContent(
    email: String,
    token: String,
    newPassword: String,
    confirmPassword: String,
    isLoading: Boolean,
    onTokenChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onConfirm: () -> Unit
) {
    val canConfirm = token.isNotBlank() &&
        newPassword.length >= 6 &&
        confirmPassword.isNotBlank() &&
        !isLoading

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.forgot_verify_title),
            style = MaterialTheme.typography.titleLarge.copy(
                color = Gold,
                fontWeight = FontWeight.Bold
            )
        )

        Text(
            text = stringResource(R.string.forgot_verify_desc, email),
            color = TextSecondary
        )

        CasinoOutlinedTextField(
            value = token,
            onValueChange = onTokenChange,
            label = stringResource(R.string.forgot_label_token),
            enabled = !isLoading,
            leadingIcon = { Icon(Icons.Default.Email, null, Modifier.size(20.dp)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        CasinoOutlinedTextField(
            value = newPassword,
            onValueChange = onNewPasswordChange,
            label = stringResource(R.string.forgot_label_new_password),
            enabled = !isLoading,
            leadingIcon = { Icon(Icons.Default.Lock, null, Modifier.size(20.dp)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        CasinoOutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = stringResource(R.string.forgot_label_confirm_password),
            enabled = !isLoading,
            leadingIcon = { Icon(Icons.Default.Lock, null, Modifier.size(20.dp)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        GoldPrimaryButton(
            text = if (isLoading) stringResource(R.string.auth_processing) else stringResource(R.string.forgot_btn_confirm),
            onClick = onConfirm,
            enabled = canConfirm
        )
    }
}

@Composable
private fun ForgotDoneContent(onBack: () -> Unit) {
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
            text = stringResource(R.string.forgot_success_desc),
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        GoldPrimaryButton(
            text = stringResource(R.string.forgot_btn_back_login),
            onClick = onBack
        )
    }
}

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
