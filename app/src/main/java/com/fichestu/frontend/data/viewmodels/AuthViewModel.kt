package com.fichestu.frontend.data.viewmodels

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fichestu.frontend.BuildConfig
import com.fichestu.frontend.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
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

    fun updateUsername(value: String) = _uiState.update { it.copy(username = value, message = "") }
    fun updateEmail(value: String) = _uiState.update { it.copy(email = value, message = "") }
    fun updatePassword(value: String) = _uiState.update { it.copy(password = value, message = "") }

    fun toggleMode() {
        _uiState.update {
            it.copy(
                isLoginMode = !it.isLoginMode,
                message = "",
                password = "",
                isAuthenticated = false
            )
        }
    }

    fun logout() {
        _uiState.update {
            it.copy(
                isAuthenticated = false,
                token = "",
                password = "",
                message = "Sesión cerrada"
            )
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
                        current.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            token = authResult.token.orEmpty(),
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

    // --- LOGIN / REGISTRO TRADICIONAL ---
    fun submit() {
        val state = _uiState.value

        if (!isValidInput(state)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = "") }

            // Fíjate que ya NO pasamos la baseUrl, el repositorio ya sabe dónde ir
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
                            current.copy(
                                isLoading = false,
                                isAuthenticated = true,
                                token = authResult.token.orEmpty(),
                                password = "",
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
}
