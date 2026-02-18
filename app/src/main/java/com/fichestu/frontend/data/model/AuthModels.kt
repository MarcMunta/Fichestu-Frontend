package com.fichestu.frontend.data.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String? = null,
    val message: String? = null,
    val success: Boolean? = null
)
