package com.fichestu.frontend.views

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fichestu.frontend.data.viewmodels.AuthUiState
import com.fichestu.frontend.data.viewmodels.AuthViewModel
import com.fichestu.frontend.ui.theme.AuthScaffold
import kotlinx.coroutines.delay
import com.fichestu.frontend.ui.theme.AuthTab
import com.fichestu.frontend.ui.theme.CasinoOutlinedTextField
import com.fichestu.frontend.ui.theme.ChipAuthTabs
import com.fichestu.frontend.ui.theme.ChipRed
import com.fichestu.frontend.ui.theme.DeepBlue
import com.fichestu.frontend.ui.theme.FichestuTheme
import com.fichestu.frontend.ui.theme.Gold
import com.fichestu.frontend.ui.theme.GoldPrimaryButton
import com.fichestu.frontend.ui.theme.InputBg
import com.fichestu.frontend.ui.theme.InputBorder
import com.fichestu.frontend.ui.theme.NightBlue
import com.fichestu.frontend.ui.theme.TextSecondary
import com.fichestu.frontend.R

private enum class AuthDest { FORM, FORGOT }

@Composable
fun AuthScreen(viewModel: AuthViewModel = viewModel(), onGoogleClick: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var dest by remember { mutableStateOf(AuthDest.FORM) }

    if (state.isAuthenticated) {
        AuthenticatedScreen(token = state.token, onLogout = viewModel::logout)
    } else {
        AnimatedContent(targetState = dest, label = "main_nav") { currentDest ->
            when (currentDest) {
                AuthDest.FORM -> AuthScaffold(
                    title = stringResource(R.string.auth_title),
                    subtitle = stringResource(R.string.auth_subtitle)
                ) {
                    AuthFormContent(
                        state = state,
                        viewModel = viewModel,
                        onForgotClick = { dest = AuthDest.FORGOT },
                        onGoogleClick = onGoogleClick
                    )
                }
                AuthDest.FORGOT -> ForgotPasswordScreen(onBack = { dest = AuthDest.FORM })
            }
        }
    }
}

@Composable
private fun AuthFormContent(
    state: AuthUiState,
    viewModel: AuthViewModel,
    onForgotClick: () -> Unit,
    onGoogleClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxWidth()) {
        ChipAuthTabs(
            selected = if (state.isLoginMode) AuthTab.LOGIN else AuthTab.REGISTER,
            onSelect = { tab ->
                val clickingLogin = (tab == AuthTab.LOGIN)
                if (clickingLogin != state.isLoginMode) {
                    viewModel.toggleMode()
                }
            },
            enabled = !state.isLoading
        )

        Spacer(Modifier.height(24.dp))

        AnimatedContent(targetState = state.isLoginMode, label = "form_switch") { isLogin ->
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (isLogin) {
                    LoginForm(state, viewModel, onForgotClick, onGoogleClick)
                } else {
                    RegisterForm(state, viewModel)
                }

                if (state.message.isNotBlank()) {
                    StatusMessage(state.message)
                }
            }
        }
    }
}

@Composable
private fun LoginForm(state: AuthUiState, viewModel: AuthViewModel, onForgotClick: () -> Unit, onGoogleClick: () -> Unit) {
    var passwordVisible by remember { mutableStateOf(false) }

    CasinoOutlinedTextField(
        value = state.email,
        onValueChange = viewModel::updateEmail,
        label = stringResource(R.string.forgot_label_email),
        leadingIcon = { Icon(Icons.Default.Email, null, Modifier.size(20.dp)) }
    )

    CasinoOutlinedTextField(
        value = state.password,
        onValueChange = viewModel::updatePassword,
        label = stringResource(R.string.auth_label_password), // Asegúrate de tener este ID o cámbialo a uno genérico
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
            }
        }
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = stringResource(R.string.auth_forgot_password),
            modifier = Modifier.clickable { onForgotClick() },
            style = MaterialTheme.typography.bodySmall.copy(
                color = Gold,
                textDecoration = TextDecoration.Underline
            )
        )
    }

    GoldPrimaryButton(
        text = if (state.isLoading) stringResource(R.string.auth_processing) else stringResource(R.string.auth_btn_login),
        onClick = viewModel::submit,
        enabled = !state.isLoading && state.email.isNotBlank() && state.password.isNotBlank()
    )

    Spacer(Modifier.height(16.dp))

    // BOTÓN DE GOOGLE
    Button(
        onClick = onGoogleClick,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = NightBlue, contentColor = Gold),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Gold.copy(alpha = 0.5f))
    ) {
        Text("CONTINUAR CON GOOGLE", fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RegisterForm(state: AuthUiState, viewModel: AuthViewModel) {
    var termsAccepted by remember { mutableStateOf(false) }

    CasinoOutlinedTextField(
        value = state.username,
        onValueChange = viewModel::updateUsername,
        label = stringResource(R.string.auth_label_user), // ID sugerido para el XML
        leadingIcon = { Icon(Icons.Default.Person, null, Modifier.size(20.dp)) }
    )

    CasinoOutlinedTextField(
        value = state.email,
        onValueChange = viewModel::updateEmail,
        label = stringResource(R.string.forgot_label_email),
        leadingIcon = { Icon(Icons.Default.Email, null, Modifier.size(20.dp)) }
    )

    CasinoOutlinedTextField(
        value = state.password,
        onValueChange = viewModel::updatePassword,
        label = stringResource(R.string.auth_label_password),
        visualTransformation = PasswordVisualTransformation()
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = termsAccepted,
            onCheckedChange = { termsAccepted = it },
            colors = CheckboxDefaults.colors(checkedColor = Gold, uncheckedColor = InputBorder)
        )
        Text(
            text = stringResource(R.string.auth_terms),
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall
        )
    }

    GoldPrimaryButton(
        text = if (state.isLoading) stringResource(R.string.auth_processing) else stringResource(R.string.auth_btn_register),
        onClick = viewModel::submit,
        enabled = !state.isLoading && termsAccepted && state.email.isNotBlank()
    )
}

@Composable
private fun StatusMessage(message: String) {
    val err1 = stringResource(R.string.error_keyword_1)
    val err2 = stringResource(R.string.error_keyword_2)
    val err3 = stringResource(R.string.error_keyword_3)

    val isError = message.contains(err1, ignoreCase = true) ||
            message.contains(err2, ignoreCase = true) ||
            message.contains(err3, ignoreCase = true)

    Text(
        text = message,
        color = if (isError) ChipRed else Gold,
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    )
}

@Composable
private fun AuthenticatedScreen(token: String, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.auth_welcome_back),
            style = MaterialTheme.typography.headlineMedium,
            color = Gold
        )
        Spacer(Modifier.height(20.dp))
        GoldPrimaryButton(
            text = stringResource(R.string.auth_btn_logout),
            onClick = onLogout
        )
    }
}