package com.example.dospot

import android.content.Context
import android.content.SharedPreferences

class PreferenceHelper(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("DoSpotPrefs", Context.MODE_PRIVATE)

    fun saveUserId(userId: String) {
        prefs.edit().putString("USER_ID", userId).apply()
    }

    fun getUserId(): String? {
        return prefs.getString("USER_ID", null)
    }

    fun clearUser() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean {
        return getUserId() != null
    }
}