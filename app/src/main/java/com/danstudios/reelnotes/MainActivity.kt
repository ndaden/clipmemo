package com.danstudios.reelnotes

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.danstudios.reelnotes.ui.navigation.Screen
import com.danstudios.reelnotes.ui.screens.NoteDetailScreen
import com.danstudios.reelnotes.ui.screens.NotesListScreen
import com.danstudios.reelnotes.ui.screens.SettingsScreen
import com.danstudios.reelnotes.ui.theme.DarkBackground
import com.danstudios.reelnotes.ui.theme.ReelNotesTheme
import com.danstudios.reelnotes.ui.viewmodel.ReelNotesViewModel
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val app by lazy { application as ReelNotesApp }
    private val viewModel: ReelNotesViewModel by viewModels {
        ReelNotesViewModel.Factory(app.repository, app.preferences)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        handleIncomingIntent(intent)

        setContent {
            val preferredLanguage by viewModel.preferredLanguage.collectAsState()
            val locale = remember(preferredLanguage) {
                if (preferredLanguage.startsWith("en", ignoreCase = true)) Locale.ENGLISH else Locale.FRENCH
            }
            val baseContext = LocalContext.current
            val localizedConfiguration = remember(preferredLanguage, locale) {
                Configuration(baseContext.resources.configuration).apply {
                    setLocale(locale)
                }
            }
            val localizedContext = remember(preferredLanguage, locale, baseContext) {
                val configContext = baseContext.createConfigurationContext(localizedConfiguration)
                object : android.content.ContextWrapper(baseContext) {
                    override fun getResources(): android.content.res.Resources = configContext.resources
                    override fun createConfigurationContext(overrideConfiguration: Configuration): android.content.Context {
                        return configContext.createConfigurationContext(overrideConfiguration)
                    }
                }
            }

            CompositionLocalProvider(
                LocalConfiguration provides localizedConfiguration,
                LocalContext provides localizedContext
            ) {
                ReelNotesTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = DarkBackground
                    ) {
                        val navController = rememberNavController()

                        // Automatically navigate to note details when a new note is processed
                        LaunchedEffect(Unit) {
                            viewModel.newlyCreatedNoteId.collectLatest { noteId ->
                                navController.navigate(Screen.NoteDetail.createRoute(noteId))
                            }
                        }

                        NavHost(
                            navController = navController,
                            startDestination = Screen.NotesList.route
                        ) {
                            composable(Screen.NotesList.route) {
                                NotesListScreen(
                                    viewModel = viewModel,
                                    onNoteClick = { noteId ->
                                        navController.navigate(Screen.NoteDetail.createRoute(noteId))
                                    },
                                    onSettingsClick = {
                                        navController.navigate(Screen.Settings.route)
                                    }
                                )
                            }

                            composable(
                                route = Screen.NoteDetail.route,
                                arguments = listOf(navArgument("noteId") { type = NavType.LongType })
                            ) { backStackEntry ->
                                val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
                                NoteDetailScreen(
                                    noteId = noteId,
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(Screen.Settings.route) {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return

        if (Intent.ACTION_SEND == intent.action && intent.type != null) {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                ?: intent.getStringExtra(Intent.EXTRA_SUBJECT)

            if (!sharedText.isNullOrBlank()) {
                viewModel.processSharedUrl(
                    sharedText = sharedText,
                    context = this
                )
            }
        }
    }
}
