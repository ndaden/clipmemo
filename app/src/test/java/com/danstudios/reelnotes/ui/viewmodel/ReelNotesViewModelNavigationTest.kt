package com.danstudios.reelnotes.ui.viewmodel

import com.danstudios.reelnotes.data.local.PreferencesManager
import com.danstudios.reelnotes.data.local.ReelNoteDao
import com.danstudios.reelnotes.data.local.ReelNoteEntity
import com.danstudios.reelnotes.data.repository.ReelNoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReelNotesViewModelNavigationTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeDao = object : ReelNoteDao {
        override fun getAllNotes(): Flow<List<ReelNoteEntity>> = flowOf(emptyList())
        override fun getNoteById(id: Long): Flow<ReelNoteEntity?> = flowOf(null)
        override fun getFavoriteNotes(): Flow<List<ReelNoteEntity>> = flowOf(emptyList())
        override fun getNotesByCategory(category: String): Flow<List<ReelNoteEntity>> = flowOf(emptyList())
        override fun searchNotes(query: String): Flow<List<ReelNoteEntity>> = flowOf(emptyList())
        override suspend fun insertNote(note: ReelNoteEntity): Long = 1L
        override suspend fun updateNote(note: ReelNoteEntity) {}
        override suspend fun deleteNote(note: ReelNoteEntity) {}
        override suspend fun deleteById(id: Long) {}
        override suspend fun updateFavorite(id: Long, isFavorite: Boolean, updatedAt: Long) {}
    }

    private val fakeRepository = ReelNoteRepository(fakeDao)
    private val fakePreferences = object : PreferencesManager(null) {
        override var preferredLanguage: String = "fr"
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testProcessSharedUrlEmitsNavigateToNotesList() = runTest {
        val viewModel = ReelNotesViewModel(fakeRepository, fakePreferences)

        viewModel.processSharedUrl("https://www.instagram.com/reel/C_JGaVLuiU0")

        val event = viewModel.navigateToNotesList.firstOrNull()
        assertNotNull("Expected navigateToNotesList to emit an event when valid URL is shared", event)
    }

    @Test
    fun testProcessSharedUrlWithBlankDoesNotEmitNavigate() = runTest {
        val viewModel = ReelNotesViewModel(fakeRepository, fakePreferences)

        viewModel.processSharedUrl("   ")

        val replayCache = viewModel.navigateToNotesList.replayCache
        assertTrue("Expected no navigation event for blank input", replayCache.isEmpty())
    }

    @Test
    fun testOnboardingShownOnFirstLaunch() = runTest {
        val prefs = object : PreferencesManager(null) {
            override var hasSeenOnboarding: Boolean = false
        }
        val viewModel = ReelNotesViewModel(fakeRepository, prefs)
        assertTrue("Expected showOnboarding to be true on first launch", viewModel.showOnboarding.value)
    }

    @Test
    fun testDismissOnboardingPersistsPreference() = runTest {
        var persisted = false
        val prefs = object : PreferencesManager(null) {
            override var hasSeenOnboarding: Boolean
                get() = persisted
                set(value) { persisted = value }
        }
        val viewModel = ReelNotesViewModel(fakeRepository, prefs)
        assertTrue(viewModel.showOnboarding.value)

        viewModel.dismissOnboarding()

        assertTrue("Expected hasSeenOnboarding to be persisted as true", prefs.hasSeenOnboarding)
        assertTrue("Expected showOnboarding StateFlow to become false", !viewModel.showOnboarding.value)
    }

    @Test
    fun testOnboardingNotShownWhenAlreadySeen() = runTest {
        val prefs = object : PreferencesManager(null) {
            override var hasSeenOnboarding: Boolean = true
        }
        val viewModel = ReelNotesViewModel(fakeRepository, prefs)
        assertTrue("Expected showOnboarding to be false when already seen", !viewModel.showOnboarding.value)
    }
}
