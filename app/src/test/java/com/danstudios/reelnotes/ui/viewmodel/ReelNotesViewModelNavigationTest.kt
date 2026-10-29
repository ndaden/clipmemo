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

    @Test
    fun testInterstitialOnDetailExitFollowsGraceFrequencyAndCooldownPolicy() = runTest {
        val prefs = object : PreferencesManager(null) {
            override var totalNotesCreatedCount: Int = 0
            override var notesCreatedSinceLastAd: Int = 0
            override var lastInterstitialShownAtMs: Long = 0L
        }
        val viewModel = ReelNotesViewModel(fakeRepository, prefs)
        val t0 = 1_000_000L

        // Browsing an existing note without creating one should never show an interstitial
        assertTrue(!viewModel.consumeInterstitialOnDetailExit(t0))

        // Note 1 (Grace period): no ad on exit
        viewModel.recordNoteCreated()
        assertTrue(!viewModel.consumeInterstitialOnDetailExit(t0 + 10_000L))

        // Note 2 (Grace period): no ad on exit
        viewModel.recordNoteCreated()
        assertTrue(!viewModel.consumeInterstitialOnDetailExit(t0 + 20_000L))

        // Note 3 (3rd note, grace period over): shows ad on exit!
        val firstAdTime = t0 + 30_000L
        viewModel.recordNoteCreated()
        assertTrue(viewModel.consumeInterstitialOnDetailExit(firstAdTime))

        // Exiting detail again (e.g. after viewing an existing note) does not re-trigger ad
        assertTrue(!viewModel.consumeInterstitialOnDetailExit(firstAdTime + 600_000L))

        // Note 4 & Note 5: frequency cap (1 every 3 notes) -> no ad
        viewModel.recordNoteCreated()
        assertTrue(!viewModel.consumeInterstitialOnDetailExit(firstAdTime + 600_000L))
        viewModel.recordNoteCreated()
        assertTrue(!viewModel.consumeInterstitialOnDetailExit(firstAdTime + 700_000L))

        // Note 6 created only 2 minutes after firstAdTime -> blocked by 5-min cooldown
        viewModel.recordNoteCreated()
        assertTrue(!viewModel.consumeInterstitialOnDetailExit(firstAdTime + 2 * 60_000L))

        // Note 7 created 6 minutes after firstAdTime -> cooldown elapsed and >= 3 notes since last ad -> shows ad!
        viewModel.recordNoteCreated()
        assertTrue(viewModel.consumeInterstitialOnDetailExit(firstAdTime + 6 * 60_000L))
    }

    @Test
    fun testInterstitialCountersNotResetWhenAdNotReadyOnExit() = runTest {
        val prefs = object : PreferencesManager(null) {
            override var totalNotesCreatedCount: Int = 2
            override var notesCreatedSinceLastAd: Int = 2
            override var lastInterstitialShownAtMs: Long = 0L
        }
        val viewModel = ReelNotesViewModel(fakeRepository, prefs)
        val t0 = 1_000_000L

        // 3rd note created, eligible for ad, but ad is not loaded (offline)
        viewModel.recordNoteCreated()
        assertTrue(
            !viewModel.consumeInterstitialOnDetailExit(
                nowMs = t0,
                isAdAvailable = false
            )
        )

        // Next note (4th note) when ad becomes ready should now show the ad immediately on exit
        viewModel.recordNoteCreated()
        assertTrue(
            viewModel.consumeInterstitialOnDetailExit(
                nowMs = t0 + 30_000L,
                isAdAvailable = true
            )
        )
    }
}
