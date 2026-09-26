package com.danstudios.reelnotes.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeColorsTest {
    @Test
    fun testDarkPremiumPaletteValues() {
        assertEquals(Color(0xFF0A0B0E), DarkBackground)
        assertEquals(Color(0xFF14161D), DarkSurface)
        assertEquals(Color(0xFF1C1F2B), DarkSurfaceElevated)
        assertEquals(Color(0xFF232733), DarkBorder)
        assertEquals(Color(0xFF8B5CF6), NeonViolet)
        assertEquals(Color(0xFF10B981), EmeraldSuccess)
        assertEquals(Color(0xFF06B6D4), CyanAccent)
        assertEquals(Color(0xFFFBBF24), GoldStar)
        assertEquals(Color(0xFFF8FAFC), TextPrimary)
    }
}
