package com.fichestu.frontend.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.fichestu.frontend.game.model.AppLanguage

object SessionStore {
    private const val PREFS_NAME = "fichestu_session"
    private const val KEY_TOKEN = "token"
    private const val KEY_DISPLAY_NAME = "display_name"
    private const val KEY_LANGUAGE = "language"

    @Volatile
    private var prefs: SharedPreferences? = null

    @Volatile
    private var token: String? = null

    @Volatile
    private var displayName: String = "Jugador"

    @Volatile
    private var language: AppLanguage = AppLanguage.ES

    fun init(context: Context) {
        if (prefs != null) return
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        token = prefs?.getString(KEY_TOKEN, null)
        displayName = prefs?.getString(KEY_DISPLAY_NAME, "Jugador") ?: "Jugador"
        language = AppLanguage.fromCode(prefs?.getString(KEY_LANGUAGE, AppLanguage.ES.code))
    }

    fun setAuth(token: String?, displayName: String) {
        this.token = token?.trim()?.takeIf { it.isNotBlank() }
        this.displayName = displayName.ifBlank { "Jugador" }
        prefs?.edit()?.apply {
            if (this@SessionStore.token == null) {
                remove(KEY_TOKEN)
            } else {
                putString(KEY_TOKEN, this@SessionStore.token)
            }
            putString(KEY_DISPLAY_NAME, this@SessionStore.displayName)
            apply()
        }
    }

    fun clear() {
        token = null
        displayName = "Jugador"
        prefs?.edit()
            ?.remove(KEY_TOKEN)
            ?.remove(KEY_DISPLAY_NAME)
            ?.apply()
    }

    fun authHeaderOrNull(): String? {
        val current = token?.trim().orEmpty()
        if (current.isBlank()) return null
        return "Bearer $current"
    }

    fun displayName(): String = displayName

    fun language(): AppLanguage = language

    fun languageCode(): String = language.code

    fun setLanguage(next: AppLanguage) {
        language = next
        prefs?.edit()
            ?.putString(KEY_LANGUAGE, next.code)
            ?.apply()
    }
}
