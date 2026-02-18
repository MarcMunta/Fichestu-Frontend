package com.fichestu.frontend.ui

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
    val message: String = ""
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun updateUsername(value: String) = _uiState.update { it.copy(username = value) }
    fun updateEmail(value: String) = _uiState.update { it.copy(email = value) }
    fun updatePassword(value: String) = _uiState.update { it.copy(password = value) }

    fun toggleMode() {
        _uiState.update { it.copy(isLoginMode = !it.isLoginMode, message = "") }
    }

    fun submit() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank() || (!state.isLoginMode && state.username.isBlank())) {
            _uiState.update { it.copy(message = "Completa los campos obligatorios") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = "") }

            val result = if (state.isLoginMode) {
                repository.login(BuildConfig.BASE_URL, state.email.trim(), state.password)
            } else {
                repository.register(BuildConfig.BASE_URL, state.username.trim(), state.email.trim(), state.password)
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    message = result.getOrElse { error -> error.message ?: "Error de conexión" }
                )
            }
        }
    }
}
