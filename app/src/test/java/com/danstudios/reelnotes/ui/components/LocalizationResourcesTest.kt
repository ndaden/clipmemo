package com.danstudios.reelnotes.ui.components

import com.danstudios.reelnotes.domain.model.NoteCategory
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalizationResourcesTest {
    @Test
    fun testAllCategoriesHaveValidLabelRes() {
        for (cat in NoteCategory.entries) {
            assertTrue("Category ${cat.name} should have a non-zero labelRes", cat.labelRes != 0)
        }
    }

    @Test
    fun testAllBottomBarTabsHaveValidLabelRes() {
        for (tab in BottomBarTab.entries) {
            assertTrue("Tab ${tab.name} should have a non-zero labelRes", tab.labelRes != 0)
        }
    }
}
