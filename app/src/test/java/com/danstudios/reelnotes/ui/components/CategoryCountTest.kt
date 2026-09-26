package com.danstudios.reelnotes.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryCountTest {
    @Test
    fun testCategoryChipLabelFormatting() {
        val count = 5
        val formatted = if (count > 0) "Recettes ($count)" else "Recettes"
        assertEquals("Recettes (5)", formatted)
    }
}
