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

    open var totalNotesCreatedCount: Int
        get() = prefs?.getInt(KEY_TOTAL_NOTES_CREATED_COUNT, 0) ?: 0
        set(value) {
            prefs?.edit()?.putInt(KEY_TOTAL_NOTES_CREATED_COUNT, value)?.apply()
        }

    open var notesCreatedSinceLastAd: Int
        get() = prefs?.getInt(KEY_NOTES_CREATED_SINCE_LAST_AD, 0) ?: 0
        set(value) {
            prefs?.edit()?.putInt(KEY_NOTES_CREATED_SINCE_LAST_AD, value)?.apply()
        }

    open var lastInterstitialShownAtMs: Long
        get() = prefs?.getLong(KEY_LAST_INTERSTITIAL_SHOWN_AT_MS, 0L) ?: 0L
        set(value) {
            prefs?.edit()?.putLong(KEY_LAST_INTERSTITIAL_SHOWN_AT_MS, value)?.apply()
        }

    companion object {
        private const val PREFS_NAME = "reelnotes_prefs"
        private const val KEY_PREFERRED_LANGUAGE = "preferred_language"
        private const val KEY_HAS_SEEN_ONBOARDING = "has_seen_onboarding"
        private const val KEY_TOTAL_NOTES_CREATED_COUNT = "total_notes_created_count"
        private const val KEY_NOTES_CREATED_SINCE_LAST_AD = "notes_created_since_last_ad"
        private const val KEY_LAST_INTERSTITIAL_SHOWN_AT_MS = "last_interstitial_shown_at_ms"
    }
}
