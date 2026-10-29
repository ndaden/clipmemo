package com.danstudios.reelnotes.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class FloatingBottomBarTabTest {
    @Test
    fun testBottomBarTabValues() {
        val tabs = BottomBarTab.entries
        assertEquals(3, tabs.size)
        assertEquals(BottomBarTab.NOTES, tabs[0])
        assertEquals(BottomBarTab.FAVORITES, tabs[1])
        assertEquals(BottomBarTab.SETTINGS, tabs[2])
    }
}
