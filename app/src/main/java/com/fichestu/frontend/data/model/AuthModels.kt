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

data class ApiErrorResponse(
    val timestamp: String? = null,
    val status: Int? = null,
    val error: String? = null,
    val message: String? = null,
    val path: String? = null
)

data class AuthResult(
    val message: String,
    val token: String? = null
)

data class SessionResponse(
    val message: String? = null,
    val success: Boolean? = null,
    val userId: Int? = null,
    val username: String? = null,
    val email: String? = null,
    val role: String? = null
)

data class GoogleLoginRequest(
    val idToken: String
)
