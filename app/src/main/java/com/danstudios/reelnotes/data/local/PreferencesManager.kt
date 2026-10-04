package com.danstudios.reelnotes.data.local

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var preferredLanguage: String
        get() = prefs.getString(KEY_PREFERRED_LANGUAGE, "fr") ?: "fr"
        set(value) = prefs.edit().putString(KEY_PREFERRED_LANGUAGE, value).apply()

    companion object {
        private const val PREFS_NAME = "reelnotes_prefs"
        private const val KEY_PREFERRED_LANGUAGE = "preferred_language"
    }
}
