package com.danstudios.reelnotes.ui.viewmodel

import com.danstudios.reelnotes.data.local.PreferencesManager
import com.danstudios.reelnotes.data.local.ReelNoteDao
import com.danstudios.reelnotes.data.local.ReelNoteEntity
import com.danstudios.reelnotes.data.local.toEntity
import com.danstudios.reelnotes.data.repository.ReelNoteRepository
import com.danstudios.reelnotes.domain.extractor.ExtractionErrorType
import com.danstudios.reelnotes.domain.extractor.ProcessingStage
import com.danstudios.reelnotes.domain.model.IngredientItem
import com.danstudios.reelnotes.domain.model.NoteCategory
import com.danstudios.reelnotes.domain.model.ReelNote
import com.danstudios.reelnotes.domain.model.StepItem
import com.danstudios.reelnotes.domain.model.StructuredNoteData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReelNotesSearchAndStageTest {

    private val testDispatcher = StandardTestDispatcher()

    private val sampleNote = ReelNote(
        id = 1L,
        reelUrl = "https://www.instagram.com/reel/ABC123/",
        shortcode = "ABC123",
        author = "@chefdan",
        title = "Quick Dinner",
        category = NoteCategory.RECIPE,
        summary = "Tasty meal",
        rawCaption = "Simple caption",
        markdownContent = "# Quick Dinner",
        structuredData = StructuredNoteData(
            ingredients = listOf(IngredientItem(name = "Parmigiano Reggiano", amount = "50", unit = "g")),
            steps = listOf(StepItem(stepNumber = 1, instruction = "Caramelize the shallots slowly"))
        )
    )

    private val fakeDao = object : ReelNoteDao {
        override fun getAllNotes(): Flow<List<ReelNoteEntity>> = flowOf(listOf(sampleNote.toEntity()))
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

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `filteredNotes matches note by ingredient name or step instruction`() = runTest {
        val vm = ReelNotesViewModel(ReelNoteRepository(fakeDao), PreferencesManager(null))
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.filteredNotes.collect()
        }

        vm.setSearchQuery("Parmigiano")
        testScheduler.advanceUntilIdle()
        assertEquals(1, vm.filteredNotes.value.size)

        vm.setSearchQuery("shallots")
        testScheduler.advanceUntilIdle()
        assertEquals(1, vm.filteredNotes.value.size)

        vm.setSearchQuery("nonexistent")
        testScheduler.advanceUntilIdle()
        assertTrue(vm.filteredNotes.value.isEmpty())

        collectJob.cancel()
    }

    @Test
    fun `all ProcessingStage and ExtractionErrorType entries define valid string resources`() {
        ProcessingStage.entries.forEach { stage ->
            assertTrue("Stage ${stage.name} must have labelRes", stage.labelRes != 0)
        }
        ExtractionErrorType.entries.forEach { err ->
            assertTrue("Error ${err.name} must have messageRes", err.messageRes != 0)
        }
    }
}
