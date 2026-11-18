package com.danstudios.reelnotes.ui.navigation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationSafetyTest {

    @Test
    fun `canSafelyPopBack returns false when previousRoute is null`() {
        assertFalse(
            canSafelyPopBack(
                currentRoute = Screen.NoteDetail.route,
                previousRoute = null,
                isResumed = true
            )
        )
    }

    @Test
    fun `canSafelyPopBack returns false when already on start destination NotesList`() {
        assertFalse(
            canSafelyPopBack(
                currentRoute = Screen.NotesList.route,
                previousRoute = Screen.NotesList.route,
                isResumed = true
            )
        )
    }

    @Test
    fun `canSafelyPopBack returns false when lifecycle is not resumed to prevent double-pop`() {
        assertFalse(
            canSafelyPopBack(
                currentRoute = Screen.NoteDetail.route,
                previousRoute = Screen.NotesList.route,
                isResumed = false
            )
        )
    }

    @Test
    fun `canSafelyPopBack returns true when on detail or settings with valid previous route and resumed`() {
        assertTrue(
            canSafelyPopBack(
                currentRoute = Screen.NoteDetail.route,
                previousRoute = Screen.NotesList.route,
                isResumed = true
            )
        )
        assertTrue(
            canSafelyPopBack(
                currentRoute = Screen.Settings.route,
                previousRoute = Screen.NotesList.route,
                isResumed = true
            )
        )
    }
}
