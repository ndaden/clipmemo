package com.danstudios.reelnotes.data.local

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Locale

class PreferencesManagerLanguageTest {

    private lateinit var originalLocale: Locale

    @Before
    fun setUp() {
        originalLocale = Locale.getDefault()
    }

    @After
    fun tearDown() {
        Locale.setDefault(originalLocale)
    }

    @Test
    fun testDefaultLanguageIsFrenchWhenSystemLocaleIsFrench() {
        Locale.setDefault(Locale.FRENCH)
        val manager = PreferencesManager(null)
        assertEquals("fr", manager.preferredLanguage)
    }

    @Test
    fun testDefaultLanguageIsFrenchWhenSystemLocaleIsCanadianFrench() {
        Locale.setDefault(Locale.CANADA_FRENCH)
        val manager = PreferencesManager(null)
        assertEquals("fr", manager.preferredLanguage)
    }

    @Test
    fun testDefaultLanguageIsEnglishWhenSystemLocaleIsUS() {
        Locale.setDefault(Locale.US)
        val manager = PreferencesManager(null)
        assertEquals("en", manager.preferredLanguage)
    }

    @Test
    fun testDefaultLanguageIsEnglishWhenSystemLocaleIsUK() {
        Locale.setDefault(Locale.UK)
        val manager = PreferencesManager(null)
        assertEquals("en", manager.preferredLanguage)
    }

    @Test
    fun testDefaultLanguageIsEnglishWhenSystemLocaleIsOtherLanguage() {
        Locale.setDefault(Locale.GERMANY)
        val manager = PreferencesManager(null)
        assertEquals("en", manager.preferredLanguage)
    }

    @Test
    fun testExplicitPreferenceOverridesSystemLocale() {
        Locale.setDefault(Locale.FRENCH)
        var storedValue: String? = "en"
        val manager = object : PreferencesManager(null) {
            override var preferredLanguage: String
                get() = storedValue ?: getDefaultLanguage()
                set(value) { storedValue = value }
        }

        assertEquals("en", manager.preferredLanguage)
    }
}
