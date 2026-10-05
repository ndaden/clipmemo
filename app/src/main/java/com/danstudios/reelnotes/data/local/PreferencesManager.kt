package com.danstudios.reelnotes.data.local

import android.content.Context
import android.content.SharedPreferences

open class PreferencesManager(context: Context? = null) {

    private val prefs: SharedPreferences? = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    open var preferredLanguage: String
        get() = prefs?.getString(KEY_PREFERRED_LANGUAGE, null) ?: getDefaultLanguage()
        set(value) {
            prefs?.edit()?.putString(KEY_PREFERRED_LANGUAGE, value)?.apply()
        }

    open fun getDefaultLanguage(): String {
        val systemLang = java.util.Locale.getDefault().language
        return if (systemLang.startsWith("fr", ignoreCase = true)) "fr" else "en"
    }

    open var hasSeenOnboarding: Boolean
        get() = prefs?.getBoolean(KEY_HAS_SEEN_ONBOARDING, false) ?: false
        set(value) {
            prefs?.edit()?.putBoolean(KEY_HAS_SEEN_ONBOARDING, value)?.apply()
        }

    companion object {
        private const val PREFS_NAME = "reelnotes_prefs"
        private const val KEY_PREFERRED_LANGUAGE = "preferred_language"
        private const val KEY_HAS_SEEN_ONBOARDING = "has_seen_onboarding"
    }
}
