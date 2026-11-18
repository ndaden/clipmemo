package com.danstudios.reelnotes.ui.components

import com.danstudios.reelnotes.domain.model.NoteCategory
import com.danstudios.reelnotes.ui.util.NoteTextFormatter
import org.junit.Assert.assertEquals
import org.junit.Test

class UiHelpersTest {

    @Test
    fun `visibleCategories hides zero-count categories when none selected`() {
        val counts = mapOf<NoteCategory?, Int>(
            NoteCategory.RECIPE to 2,
            NoteCategory.TUTORIAL to 0,
            NoteCategory.WORKOUT to 1,
            NoteCategory.TIPS_INFO to 0,
            NoteCategory.TRAVEL to 0,
            NoteCategory.PRODUCT to 0,
            NoteCategory.GENERAL to 0
        )

        val visible = visibleCategories(counts = counts, selectedCategory = null)
        assertEquals(listOf(NoteCategory.RECIPE, NoteCategory.WORKOUT), visible)
    }

    @Test
    fun `visibleCategories keeps selectedCategory visible even if its count drops to zero`() {
        val counts = mapOf<NoteCategory?, Int>(
            NoteCategory.RECIPE to 1,
            NoteCategory.WORKOUT to 0
        )

        val visible = visibleCategories(counts = counts, selectedCategory = NoteCategory.WORKOUT)
        assertEquals(listOf(NoteCategory.RECIPE, NoteCategory.WORKOUT), visible)
    }

    @Test
    fun `formatServings localizes French and English servings cleanly`() {
        assertEquals(
            "2 servings",
            NoteTextFormatter.formatServings("2 personnes", isFrench = false) { "$it servings" }
        )
        assertEquals(
            "2 personnes",
            NoteTextFormatter.formatServings("2 servings", isFrench = true) { "$it personnes" }
        )
        assertEquals(
            "4 servings",
            NoteTextFormatter.formatServings("4", isFrench = false) { "$it servings" }
        )
        assertEquals(
            "4 personnes",
            NoteTextFormatter.formatServings("4", isFrench = true) { "$it personnes" }
        )
    }
}
