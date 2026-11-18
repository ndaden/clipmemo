package com.danstudios.reelnotes.ui.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController

sealed class Screen(val route: String) {
    object NotesList : Screen("notes_list")
    object NoteDetail : Screen("note_detail/{noteId}") {
        fun createRoute(noteId: Long) = "note_detail/$noteId"
    }
    object Settings : Screen("settings")
}

/**
 * Prevents double-popping the start destination into a blank NavHost screen
 * when back is pressed twice quickly or during a navigation transition.
 */
internal fun canSafelyPopBack(
    currentRoute: String?,
    previousRoute: String?,
    isResumed: Boolean
): Boolean {
    if (!isResumed) return false
    if (previousRoute == null) return false
    if (currentRoute == Screen.NotesList.route) return false
    return true
}

fun NavHostController.safePopBackStack(): Boolean {
    val entry = currentBackStackEntry
    val isResumed = entry?.lifecycle?.currentState == Lifecycle.State.RESUMED
    val canPop = canSafelyPopBack(
        currentRoute = entry?.destination?.route,
        previousRoute = previousBackStackEntry?.destination?.route,
        isResumed = isResumed
    )
    return if (canPop) {
        popBackStack()
    } else {
        false
    }
}
