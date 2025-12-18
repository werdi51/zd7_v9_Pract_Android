package com.example.belarusuniversities.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesHelper(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun saveSession(email: String, role: String) {
        prefs.edit().apply {
            putString("email", email)
            putString("role", role)
            putBoolean("is_logged_in", true)
            apply()
        }
    }

    fun clearSession() {
        prefs.edit().apply {
            clear()
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("is_logged_in", false)
    }

    fun getUserRole(): String? {
        return prefs.getString("role", null)
    }
}