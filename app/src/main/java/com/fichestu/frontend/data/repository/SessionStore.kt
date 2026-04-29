package com.fichestu.frontend.data.repository

object SessionStore {
    @Volatile
    private var token: String? = null

    @Volatile
    private var displayName: String = "Jugador"

    fun setAuth(token: String?, displayName: String) {
        this.token = token
        this.displayName = displayName.ifBlank { "Jugador" }
    }

    fun clear() {
        token = null
        displayName = "Jugador"
    }

    fun authHeaderOrNull(): String? {
        val current = token?.trim().orEmpty()
        if (current.isBlank()) return null
        return "Bearer $current"
    }

    fun displayName(): String = displayName
}
