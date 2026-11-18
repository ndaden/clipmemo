package com.danstudios.reelnotes.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

/**
 * Captures the active localized [LocalContext] and [LocalConfiguration] from the host
 * composition and re-provides them inside separate Android Dialog windows.
 */
@Composable
fun rememberLocalizedDialogProviders(): @Composable (content: @Composable () -> Unit) -> Unit {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    return { content ->
        CompositionLocalProvider(
            LocalContext provides context,
            LocalConfiguration provides configuration,
            content = content
        )
    }
}
