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
import androidx.navigation.NavHostController
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
fun AuthScreen(viewModel: AuthViewModel = viewModel(), navController: NavHostController, onGoogleClick: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var dest by rememberSaveable { mutableStateOf(AuthDest.FORM) }

    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) {
            navController.navigate("game/${state.displayName}") {
                popUpTo("auth") { inclusive = true }
            }
        }
    }

    if (!state.isAuthenticated) {
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
                if ((tab == AuthTab.LOGIN) != state.isLoginMode) {
                    viewModel.toggleMode()
                }
            },
            enabled = !state.isLoading
        )

        Spacer(Modifier.height(24.dp))

        // Animación de entrada al cambiar entre Login y Registro
        AnimatedContent(
            targetState = state.isLoginMode,
            label = "form_switch",
            transitionSpec = { slideInHorizontally() + fadeIn() togetherWith slideOutHorizontally() + fadeOut() }
        ) { isLogin ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState), // El scroll aquí permite que el contenido respire
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
private fun LoginForm(
    state: AuthUiState,
    viewModel: AuthViewModel,
    onForgotClick: () -> Unit,
    onGoogleClick: () -> Unit
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    CasinoOutlinedTextField(
        value = state.email,
        onValueChange = viewModel::updateEmail,
        label = stringResource(R.string.forgot_label_email),
        enabled = !state.isLoading,
        leadingIcon = { Icon(Icons.Default.Email, null, Modifier.size(20.dp)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
    )

    CasinoOutlinedTextField(
        value = state.password,
        onValueChange = viewModel::updatePassword,
        label = stringResource(R.string.auth_label_password),
        enabled = !state.isLoading,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
            }
        }
    )

    Text(
        text = stringResource(R.string.auth_forgot_password),
        modifier = Modifier
            .clickable(enabled = !state.isLoading) { onForgotClick() },
        style = MaterialTheme.typography.bodySmall.copy(
            color = Gold,
            textDecoration = TextDecoration.Underline
        )
    )

    GoldPrimaryButton(
        text = if (state.isLoading) stringResource(R.string.auth_processing) else stringResource(R.string.auth_btn_login),
        onClick = viewModel::submit,
        enabled = !state.isLoading && state.email.isNotBlank() && state.password.isNotBlank()
    )

    // Separador visual para Google
    Text(
        text = "— O —",
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = TextSecondary.copy(alpha = 0.5f),
        style = MaterialTheme.typography.labelSmall
    )

    Button(
        onClick = onGoogleClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        enabled = !state.isLoading,
        colors = ButtonDefaults.buttonColors(containerColor = NightBlue, contentColor = Gold),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
    ) {
        Text("CONTINUAR CON GOOGLE", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}

@Composable
private fun RegisterForm(state: AuthUiState, viewModel: AuthViewModel) {
    var termsAccepted by rememberSaveable { mutableStateOf(false) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    CasinoOutlinedTextField(
        value = state.username,
        onValueChange = viewModel::updateUsername,
        label = stringResource(R.string.auth_label_user),
        enabled = !state.isLoading,
        leadingIcon = { Icon(Icons.Default.Person, null, Modifier.size(20.dp)) }
    )

    CasinoOutlinedTextField(
        value = state.email,
        onValueChange = viewModel::updateEmail,
        label = stringResource(R.string.forgot_label_email),
        enabled = !state.isLoading,
        leadingIcon = { Icon(Icons.Default.Email, null, Modifier.size(20.dp)) }
    )

    CasinoOutlinedTextField(
        value = state.password,
        onValueChange = viewModel::updatePassword,
        label = stringResource(R.string.auth_label_password),
        enabled = !state.isLoading,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
            }
        }
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(enabled = !state.isLoading) { termsAccepted = !termsAccepted }
    ) {
        Checkbox(
            checked = termsAccepted,
            onCheckedChange = { termsAccepted = it },
            enabled = !state.isLoading,
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
        enabled = !state.isLoading && termsAccepted && state.email.isNotBlank() && state.password.isNotBlank()
    )
}

@Composable
private fun StatusMessage(message: String) {
    // Tus keywords están bien, pero asegúrate de que existan en strings.xml
    val isError = listOf(
        stringResource(R.string.error_keyword_1),
        stringResource(R.string.error_keyword_2),
        stringResource(R.string.error_keyword_3)
    ).any { message.contains(it, ignoreCase = true) }

    AnimatedVisibility(
        visible = message.isNotBlank(),
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut()
    ) {
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
}

@Composable
private fun AuthenticatedScreen(token: String, onLogout: () -> Unit) {
    // Usamos Box para poner un fondo que cubra toda la pantalla
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NightBlue), // O el degradado que prefieras
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.auth_welcome_back),
                style = MaterialTheme.typography.headlineMedium,
                color = Gold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            // Opcional: Mostrar un mensaje de éxito pequeño
            Text(
                text = "Has iniciado sesión correctamente",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(Modifier.height(32.dp))

            GoldPrimaryButton(
                text = stringResource(R.string.auth_btn_logout),
                onClick = onLogout
            )
        }
    }
}