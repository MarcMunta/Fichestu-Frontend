package com.fichestu.frontend.data.viewmodels

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fichestu.frontend.BuildConfig
import com.fichestu.frontend.data.repository.AuthRepository
import com.fichestu.frontend.data.repository.SessionStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val isLoginMode: Boolean = true,
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val token: String = "",
    val message: String = ""
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        restoreSession()
    }

    fun updateUsername(value: String) = _uiState.update { it.copy(username = value, message = "") }
    fun updateEmail(value: String) = _uiState.update { it.copy(email = value, message = "") }
    fun updatePassword(value: String) = _uiState.update { it.copy(password = value, message = "") }

    fun toggleMode() {
        _uiState.update {
            it.copy(
                isLoginMode = !it.isLoginMode,
                message = "",
                password = "",
                token = "",
                displayName = "",
                isAuthenticated = false
            )
        }
    }

    fun logout() {
        SessionStore.clear()
        _uiState.update {
            it.copy(
                isAuthenticated = false,
                token = "",
                password = "",
                displayName = "",
                message = "Sesion cerrada"
            )
        }
    }

    private fun restoreSession() {
        val existing = SessionStore.authHeaderOrNull() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = "Validando sesion...") }
            val result = repository.validateSession()
            _uiState.update { current ->
                result.fold(
                    onSuccess = { session ->
                        val displayName = session.username ?: SessionStore.displayName()
                        SessionStore.setAuth(existing.removePrefix("Bearer "), displayName)
                        current.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            displayName = displayName,
                            message = session.message ?: "Sesion restaurada"
                        )
                    },
                    onFailure = {
                        SessionStore.clear()
                        current.copy(isLoading = false, message = "")
                    }
                )
            }
        }
    }

//    // --- FUNCIÓN PARA LOGIN CON GOOGLE ---
    fun onGoogleLoginSuccess(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = "") }

            val result = repository.loginWithGoogle(idToken)

            _uiState.update { current ->
                result.fold(
                    onSuccess = { authResult ->
                        val displayName = deriveDisplayName(
                            username = current.username,
                            email = current.email
                        )
                        SessionStore.setAuth(authResult.token, displayName)
                        current.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            token = authResult.token.orEmpty(),
                            displayName = displayName,
                            message = authResult.message
                        )
                    },
                    onFailure = { error ->
                        current.copy(
                            isLoading = false,
                            message = error.message ?: "Error al conectar con Google"
                        )
                    }
                )
            }
        }
    }

    fun submit() {
        val state = _uiState.value

        if (!isValidInput(state)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = "") }

            val result = if (state.isLoginMode) {
                repository.login(
                    email = state.email.trim(),
                    password = state.password
                )
            } else {
                repository.register(
                    username = state.username.trim(),
                    email = state.email.trim(),
                    password = state.password
                )
            }

            _uiState.update { current ->
                result.fold(
                    onSuccess = { authResult ->
                        if (state.isLoginMode) {
                            val displayName = deriveDisplayName(
                                username = state.username,
                                email = state.email
                            )
                            SessionStore.setAuth(authResult.token, displayName)
                            current.copy(
                                isLoading = false,
                                isAuthenticated = true,
                                token = authResult.token.orEmpty(),
                                password = "",
                                displayName = displayName,
                                message = authResult.message
                            )
                        } else {
                            current.copy(
                                isLoading = false,
                                isLoginMode = true,
                                password = "",
                                message = "${authResult.message}. Ya puedes iniciar sesión"
                            )
                        }
                    },
                    onFailure = { error ->
                        current.copy(
                            isLoading = false,
                            message = error.message ?: "Error de conexión"
                        )
                    }
                )
            }
        }
    }

    private fun isValidInput(state: AuthUiState): Boolean {
        if (!state.isLoginMode && state.username.isBlank()) {
            _uiState.update { it.copy(message = "El usuario es obligatorio") }
            return false
        }

        val emailMatches = Patterns.EMAIL_ADDRESS.matcher(state.email.trim()).matches()
        if (state.email.isBlank() || !emailMatches) {
            _uiState.update { it.copy(message = "Introduce un email válido") }
            return false
        }

        if (state.password.isBlank()) {
            _uiState.update { it.copy(message = "La contraseña es obligatoria") }
            return false
        }

        return true
    }

    private fun deriveDisplayName(username: String, email: String): String {
        val cleanUsername = username.trim()
        if (cleanUsername.isNotBlank()) {
            return cleanUsername
        }

        val cleanEmail = email.trim()
        val localPart = cleanEmail.substringBefore('@').trim()
        return if (localPart.isNotBlank()) {
            localPart.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase() else char.toString()
            }
        } else {
            "Jugador"
        }
    }
}
