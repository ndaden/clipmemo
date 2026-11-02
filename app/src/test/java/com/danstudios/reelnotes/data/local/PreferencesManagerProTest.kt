package com.danstudios.reelnotes.data.local

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PreferencesManagerProTest {

    @Test
    fun `isPro defaults to false when context is null`() {
        val manager = PreferencesManager(null)
        assertFalse(manager.isPro)
    }

    @Test
    fun `isPro can be toggled in overridden mock manager`() {
        var backingValue = false
        val manager = object : PreferencesManager(null) {
            override var isPro: Boolean
                get() = backingValue
                set(value) { backingValue = value }
        }

        assertFalse(manager.isPro)
        manager.isPro = true
        assertTrue(manager.isPro)
    }
}
