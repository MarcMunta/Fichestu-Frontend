package com.fichestu.frontend.ui

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
import kotlinx.coroutines.delay
import com.fichestu.frontend.ui.game.FichestuGameScreen
import com.fichestu.frontend.ui.theme.AuthTab
import com.fichestu.frontend.ui.theme.CasinoAuthScaffold
import com.fichestu.frontend.ui.theme.CasinoOutlinedTextField
import com.fichestu.frontend.ui.theme.ChipAuthTabs
import com.fichestu.frontend.ui.theme.ChipRed
import com.fichestu.frontend.ui.theme.DeepBlue
import com.fichestu.frontend.ui.theme.FichestuTheme
import com.fichestu.frontend.ui.theme.Gold
import com.fichestu.frontend.ui.theme.GoldDark
import com.fichestu.frontend.ui.theme.GoldLight
import com.fichestu.frontend.ui.theme.GoldPrimaryButton
import com.fichestu.frontend.ui.theme.InputBg
import com.fichestu.frontend.ui.theme.InputBorder
import com.fichestu.frontend.ui.theme.NightBlue
import com.fichestu.frontend.ui.theme.TextSecondary

private enum class AuthDest { FORM, FORGOT }

// ─────────────────────────────────────────────────────────────────────────────
// AuthScreen — punto de entrada único (Login / Register / Authenticated)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AuthScreen(
    viewModel: AuthViewModel = viewModel(),
    onForgotPassword: () -> Unit = {},
    onAuthenticated: (String) -> Unit = {},
    onShowGameInline: Boolean = true
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (!onShowGameInline && state.isAuthenticated) {
        LaunchedEffect(state.isAuthenticated, state.displayName) {
            onAuthenticated(state.displayName)
        }
    }

    if (!onShowGameInline) {
        CasinoAuthScaffold(
            title = "FICHESTU",
            subtitle = "CASINO  •  CRYPTO  •  MINIGAMES"
        ) {
            AuthFormContent(
                state = state,
                onUpdateUsername = viewModel::updateUsername,
                onUpdateEmail = viewModel::updateEmail,
                onUpdatePassword = viewModel::updatePassword,
                onToggleMode = viewModel::toggleMode,
                onSubmit = viewModel::submit,
                onForgotPassword = onForgotPassword
            )
        }
        return
    }

    // UI-only navigation: FORM ↔ FORGOT (no ViewModel)
    var dest by remember { mutableStateOf(AuthDest.FORM) }

    if (state.isAuthenticated) {
        FichestuGameScreen(
            playerName = state.displayName,
            onLogout = viewModel::logout
        )
    } else {
        // Transición horizontal entre pantallas (type-safe)
        AnimatedContent(
            targetState   = dest,
            transitionSpec = {
                val toRight = targetState == AuthDest.FORM
                (slideInHorizontally(tween(350)) { if (toRight) -it else it } +
                 fadeIn(tween(300)))
                    .togetherWith(
                        slideOutHorizontally(tween(300)) { if (toRight) it else -it } +
                        fadeOut(tween(200))
                    )
            },
            label = "screen_nav"
        ) { currentDest ->
            when (currentDest) {
                AuthDest.FORM ->
                    CasinoAuthScaffold(
                        title    = "FICHESTU",
                        subtitle = "CASINO  •  CRYPTO  •  MINIGAMES"
                    ) {
                        AuthFormContent(
                            state            = state,
                            onUpdateUsername = viewModel::updateUsername,
                            onUpdateEmail    = viewModel::updateEmail,
                            onUpdatePassword = viewModel::updatePassword,
                            onToggleMode     = viewModel::toggleMode,
                            onSubmit         = viewModel::submit,
                            onForgotPassword = { dest = AuthDest.FORGOT }
                        )
                    }
                AuthDest.FORGOT ->
                    ForgotPasswordScreen(onBack = { dest = AuthDest.FORM })
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AuthFormContent — contenido de la card con animaciones de tab y stagger
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AuthFormContent(
    state: AuthUiState,
    onUpdateUsername: (String) -> Unit,
    onUpdateEmail: (String) -> Unit,
    onUpdatePassword: (String) -> Unit,
    onToggleMode: () -> Unit,
    onSubmit: () -> Unit,
    onForgotPassword: () -> Unit
) {
    var passwordVisible  by rememberSaveable { mutableStateOf(false) }
    var confirmPassword  by rememberSaveable { mutableStateOf("") }
    var confirmVisible   by rememberSaveable { mutableStateOf(false) }
    var termsAccepted    by rememberSaveable { mutableStateOf(false) }

    val confirmError    = !state.isLoginMode && confirmPassword.isNotEmpty()
                          && confirmPassword != state.password
    val isSubmitEnabled = !state.isLoading && (state.isLoginMode || termsAccepted)

    // ── Stagger: cuántos items son visibles al entrar/cambiar tab ────────
    // Se resetea al cambiar de modo → animación de entrada de cada campo
    val fieldCount      = if (state.isLoginMode) 5 else 7
    var visibleCount    by remember(state.isLoginMode) { mutableIntStateOf(0) }
    LaunchedEffect(state.isLoginMode) {
        visibleCount = 0
        repeat(fieldCount) { i ->
            delay(60L + i * 70L)
            visibleCount = i + 1
        }
    }

    val scrollState = rememberScrollState()

    // ── Tabs tipo ficha ──────────────────────────────────────────────────
    AnimatedVisibility(
        visible = visibleCount >= 1,
        enter   = fadeIn(tween(250)) + slideInVertically(tween(250)) { -it / 2 }
    ) {
        ChipAuthTabs(
            selected = if (state.isLoginMode) AuthTab.LOGIN else AuthTab.REGISTER,
            onSelect = { tab ->
                val isLogin = tab == AuthTab.LOGIN
                if (isLogin != state.isLoginMode) {
                    confirmPassword = ""; termsAccepted = false
                    passwordVisible = false; confirmVisible = false
                    onToggleMode()
                }
            },
            enabled = !state.isLoading
        )
    }

    Spacer(Modifier.height(24.dp))

    // ── Cuerpo del formulario con slide entre Login y Register ───────────
    AnimatedContent(
        targetState   = state.isLoginMode,
        transitionSpec = {
            // Login→Register: slide a la derecha; Register→Login: slide a la izquierda
            val toLogin = targetState
            (slideInHorizontally(tween(320)) { if (toLogin) -it else it } +
             fadeIn(tween(280)))
                .togetherWith(
                    slideOutHorizontally(tween(280)) { if (toLogin) it else -it } +
                    fadeOut(tween(200))
                )
        },
        label = "tab_content"
    ) { isLogin ->
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (isLogin) {
                // ─── LOGIN ───────────────────────────────────────────────

                AnimatedVisibility(visibleCount >= 2,
                    enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 3 }) {
                    CasinoOutlinedTextField(
                        value = state.email, onValueChange = onUpdateEmail, label = "Email",
                        enabled = !state.isLoading,
                        leadingIcon = { Icon(Icons.Default.Email, null, Modifier.size(20.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                }

                AnimatedVisibility(visibleCount >= 3,
                    enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 3 }) {
                    CasinoOutlinedTextField(
                        value = state.password, onValueChange = onUpdatePassword,
                        label = "Contraseña", enabled = !state.isLoading,
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                                    if (passwordVisible) "Ocultar" else "Mostrar"
                                )
                            }
                        }
                    )
                }

                AnimatedVisibility(visibleCount >= 4,
                    enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 3 }) {
                    Text(
                        text     = "¿Olvidaste la contraseña?",
                        style    = MaterialTheme.typography.bodySmall.copy(
                            color = Gold, textDecoration = TextDecoration.Underline
                        ),
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable(enabled = !state.isLoading) { onForgotPassword() }
                            .padding(vertical = 2.dp)
                    )
                }

                Spacer(Modifier.height(4.dp))

                AnimatedVisibility(visibleCount >= 4,
                    enter = fadeIn(tween(250)) + slideInVertically(
                        spring(dampingRatio = Spring.DampingRatioMediumBouncy,
                               stiffness    = Spring.StiffnessLow)
                    ) { it / 2 }) {
                    GoldPrimaryButton(
                        text    = if (state.isLoading) "PROCESANDO…" else "ENTRAR",
                        onClick  = onSubmit,
                        enabled  = isSubmitEnabled
                    )
                }

                AnimatedVisibility(visibleCount >= 5,
                    enter = fadeIn(tween(280)) + slideInVertically(tween(280)) { it / 3 }) {
                    GooglePlaceholderButton(enabled = false)
                }

            } else {
                // ─── REGISTER ────────────────────────────────────────────

                AnimatedVisibility(visibleCount >= 2,
                    enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 3 }) {
                    CasinoOutlinedTextField(
                        value = state.username, onValueChange = onUpdateUsername, label = "Usuario",
                        enabled = !state.isLoading,
                        leadingIcon = { Icon(Icons.Default.Person, null, Modifier.size(20.dp)) }
                    )
                }

                AnimatedVisibility(visibleCount >= 3,
                    enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 3 }) {
                    CasinoOutlinedTextField(
                        value = state.email, onValueChange = onUpdateEmail, label = "Email",
                        enabled = !state.isLoading,
                        leadingIcon = { Icon(Icons.Default.Email, null, Modifier.size(20.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                }

                AnimatedVisibility(visibleCount >= 4,
                    enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 3 }) {
                    CasinoOutlinedTextField(
                        value = state.password, onValueChange = onUpdatePassword,
                        label = "Contraseña", enabled = !state.isLoading,
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                                    if (passwordVisible) "Ocultar" else "Mostrar"
                                )
                            }
                        }
                    )
                }

                AnimatedVisibility(visibleCount >= 5,
                    enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 3 }) {
                    CasinoOutlinedTextField(
                        value = confirmPassword, onValueChange = { confirmPassword = it },
                        label = "Confirmar contraseña", isError = confirmError,
                        supportingText = if (confirmError) "Las contraseñas no coinciden" else null,
                        enabled = !state.isLoading,
                        visualTransformation = if (confirmVisible) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { confirmVisible = !confirmVisible }) {
                                Icon(
                                    if (confirmVisible) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                                    if (confirmVisible) "Ocultar" else "Mostrar"
                                )
                            }
                        }
                    )
                }

                AnimatedVisibility(visibleCount >= 6,
                    enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 3 }) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()) {
                        Checkbox(
                            checked = termsAccepted,
                            onCheckedChange = { if (!state.isLoading) termsAccepted = it },
                            enabled = !state.isLoading,
                            colors  = CheckboxDefaults.colors(
                                checkedColor   = Gold,
                                uncheckedColor = InputBorder,
                                checkmarkColor = NightBlue
                            )
                        )
                        Text(
                            buildAnnotatedString {
                                append("Acepto los ")
                                withStyle(SpanStyle(color = Gold, fontWeight = FontWeight.SemiBold,
                                    textDecoration = TextDecoration.Underline)) {
                                    append("Términos y Condiciones")
                                }
                            },
                            style    = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                            modifier = Modifier.clickable(enabled = !state.isLoading) {
                                termsAccepted = !termsAccepted
                            }
                        )
                    }
                }

                AnimatedVisibility(visibleCount >= 6,
                    enter = fadeIn(tween(250)) + slideInVertically(
                        spring(dampingRatio = Spring.DampingRatioMediumBouncy,
                               stiffness    = Spring.StiffnessLow)
                    ) { it / 2 }) {
                    GoldPrimaryButton(
                        text    = if (state.isLoading) "PROCESANDO…" else "CREAR CUENTA",
                        onClick  = onSubmit,
                        enabled  = isSubmitEnabled
                    )
                }

                AnimatedVisibility(visibleCount >= 7,
                    enter = fadeIn(tween(280)) + slideInVertically(tween(280)) { it / 3 }) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = TextSecondary)) { append("¿Ya tienes cuenta? ") }
                            withStyle(SpanStyle(color = Gold, fontWeight = FontWeight.Bold,
                                textDecoration = TextDecoration.Underline)) { append("Entrar") }
                        },
                        style    = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clickable(enabled = !state.isLoading) {
                                confirmPassword = ""; termsAccepted = false; onToggleMode()
                            }
                            .padding(vertical = 4.dp)
                    )
                }
            }

            // ── Mensaje de error / éxito ─────────────────────────────────
            if (state.message.isNotBlank()) {
                val isError = listOf("error", "inválido", "incorrecto", "fallido")
                    .any { state.message.contains(it, ignoreCase = true) }
                AnimatedVisibility(
                    visible = state.message.isNotBlank(),
                    enter   = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 2 },
                    exit    = fadeOut(tween(200)) + slideOutVertically(tween(200)) { it / 2 }
                ) {
                    Text(
                        text  = state.message,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isError) ChipRed else Gold,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// GooglePlaceholderButton — placeholder visual deshabilitado
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun GooglePlaceholderButton(enabled: Boolean = false) {
    Button(
        onClick        = {},
        enabled        = enabled,
        shape          = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor         = InputBg,
            contentColor           = TextSecondary,
            disabledContainerColor = InputBg.copy(alpha = 0.5f),
            disabledContentColor   = TextSecondary.copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .border(1.dp, InputBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
    ) {
        Text(
            text  = "Acceder con Google  (próximamente)",
            style = MaterialTheme.typography.labelMedium.copy(
                color = TextSecondary.copy(alpha = 0.45f), letterSpacing = 0.5.sp
            )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AuthenticatedScreen
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AuthenticatedScreen(token: String, onLogout: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(DeepBlue, NightBlue),
                    center = Offset(540f, 600f),
                    radius = 1400f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text  = "¡Sesión iniciada!",
                style = MaterialTheme.typography.displaySmall.copy(color = Gold),
                textAlign = TextAlign.Center
            )
            Text(
                text  = "Token: ${if (token.length > 32) token.take(32) + "…" else token}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary, textAlign = TextAlign.Center
                )
            )
            GoldPrimaryButton(text = "CERRAR SESIÓN", onClick = onLogout)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

@Preview(name = "Login – Casino Dark", showBackground = true,
    backgroundColor = 0xFF0B1424, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewLoginScreen() {
    FichestuTheme {
        CasinoAuthScaffold(title = "FICHESTU", subtitle = "CASINO  •  CRYPTO  •  MINIGAMES") {
            ChipAuthTabs(selected = AuthTab.LOGIN, onSelect = {})
            Spacer(Modifier.height(24.dp))
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                CasinoOutlinedTextField(value = "demo@fichestu.com", onValueChange = {}, label = "Email",
                    leadingIcon = { Icon(Icons.Default.Email, null, modifier = Modifier.size(20.dp)) })
                CasinoOutlinedTextField(value = "••••••••", onValueChange = {}, label = "Contraseña")
                Text("¿Olvidaste la contraseña?",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Gold, textDecoration = TextDecoration.Underline),
                    modifier = Modifier.align(Alignment.End))
                Spacer(Modifier.height(4.dp))
                GoldPrimaryButton(text = "ENTRAR", onClick = {})
                GooglePlaceholderButton(enabled = false)
            }
        }
    }
}

@Preview(name = "Register – Casino Dark", showBackground = true,
    backgroundColor = 0xFF0B1424, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewRegisterScreen() {
    FichestuTheme {
        CasinoAuthScaffold(title = "FICHESTU", subtitle = "CASINO  •  CRYPTO  •  MINIGAMES") {
            ChipAuthTabs(selected = AuthTab.REGISTER, onSelect = {})
            Spacer(Modifier.height(24.dp))
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                CasinoOutlinedTextField(value = "miUsuario", onValueChange = {}, label = "Usuario",
                    leadingIcon = { Icon(Icons.Default.Person, null, modifier = Modifier.size(20.dp)) })
                CasinoOutlinedTextField(value = "demo@fichestu.com", onValueChange = {}, label = "Email")
                CasinoOutlinedTextField(value = "••••••••", onValueChange = {}, label = "Contraseña")
                CasinoOutlinedTextField(value = "••••••••", onValueChange = {}, label = "Confirmar contraseña")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = true, onCheckedChange = {},
                        colors = CheckboxDefaults.colors(checkedColor = Gold, checkmarkColor = NightBlue))
                    Text(buildAnnotatedString {
                        append("Acepto los ")
                        withStyle(SpanStyle(color = Gold)) { append("Términos y Condiciones") }
                    }, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                }
                GoldPrimaryButton(text = "CREAR CUENTA", onClick = {})
            }
        }
    }
}
