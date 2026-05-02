package com.fichestu.frontend.data.repository

import android.content.Context
import android.content.SharedPreferences

object SessionStore {
    private const val PREFS_NAME = "fichestu_session"
    private const val KEY_TOKEN = "token"
    private const val KEY_DISPLAY_NAME = "display_name"

    @Volatile
    private var prefs: SharedPreferences? = null

    @Volatile
    private var token: String? = null

    @Volatile
    private var displayName: String = "Jugador"

    fun init(context: Context) {
        if (prefs != null) return
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs?.edit()?.remove(KEY_TOKEN)?.apply()
        token = null
        displayName = prefs?.getString(KEY_DISPLAY_NAME, "Jugador") ?: "Jugador"
    }

    fun setAuth(token: String?, displayName: String) {
        this.token = token
        this.displayName = displayName.ifBlank { "Jugador" }
        prefs?.edit()
            ?.remove(KEY_TOKEN)
            ?.putString(KEY_DISPLAY_NAME, this.displayName)
            ?.apply()
    }

    fun clear() {
        token = null
        displayName = "Jugador"
        prefs?.edit()?.clear()?.apply()
    }

    fun authHeaderOrNull(): String? {
        val current = token?.trim().orEmpty()
        if (current.isBlank()) return null
        return "Bearer $current"
    }

    fun displayName(): String = displayName
}
