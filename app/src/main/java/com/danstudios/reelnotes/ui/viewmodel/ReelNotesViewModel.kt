package com.danstudios.reelnotes.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.danstudios.reelnotes.ads.AdPlacementPolicy
import com.danstudios.reelnotes.data.local.PreferencesManager
import com.danstudios.reelnotes.data.network.GeminiSummarizer
import com.danstudios.reelnotes.data.network.InstagramSessionManager
import com.danstudios.reelnotes.data.repository.ReelNoteRepository
import com.danstudios.reelnotes.domain.extractor.InstagramRestrictedException
import com.danstudios.reelnotes.domain.extractor.ReelExtractionPipeline
import com.danstudios.reelnotes.domain.model.NoteCategory
import com.danstudios.reelnotes.domain.model.ReelNote
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ReelNotesViewModel(
    private val repository: ReelNoteRepository,
    private val preferences: PreferencesManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<NoteCategory?>(null)
    val selectedCategory: StateFlow<NoteCategory?> = _selectedCategory.asStateFlow()

    private val _onlyFavorites = MutableStateFlow(false)
    val onlyFavorites: StateFlow<Boolean> = _onlyFavorites.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _processingStatus = MutableStateFlow("")
    val processingStatus: StateFlow<String> = _processingStatus.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _preferredLanguage = MutableStateFlow(preferences.preferredLanguage)
    val preferredLanguage: StateFlow<String> = _preferredLanguage.asStateFlow()

    private val _isInstagramLoggedIn = MutableStateFlow(InstagramSessionManager.isLoggedIn())
    val isInstagramLoggedIn: StateFlow<Boolean> = _isInstagramLoggedIn.asStateFlow()

    private val _newlyCreatedNoteId = MutableSharedFlow<Long>()
    val newlyCreatedNoteId: SharedFlow<Long> = _newlyCreatedNoteId.asSharedFlow()

    private val _navigateToNotesList = MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 1)
    val navigateToNotesList: SharedFlow<Unit> = _navigateToNotesList.asSharedFlow()

    private val _showOnboarding = MutableStateFlow(!preferences.hasSeenOnboarding)
    val showOnboarding: StateFlow<Boolean> = _showOnboarding.asStateFlow()

    /**
     * Indique qu'une nouvelle note vient d'etre creee et que l'interstitiel doit
     * etre evalue au moment ou l'utilisateur quitte l'ecran de detail (Approche 2).
     */
    private var hasPendingPostCreationAdCheck: Boolean = false

    /**
     * Texte partage d'un Reel dont l'extraction a echoue faute d'acces.
     *
     * Non nul tant que l'utilisateur n'a pas choisi une issue : se connecter a
     * Instagram pour relancer l'extraction, ou coller lui-meme la legende.
     */
    private val _restrictedReelInput = MutableStateFlow<String?>(null)
    val restrictedReelInput: StateFlow<String?> = _restrictedReelInput.asStateFlow()

    // All notes unfiltered for counts
    val allNotes: StateFlow<List<ReelNote>> = repository.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notes filtered by search, category, and favorites
    val filteredNotes: StateFlow<List<ReelNote>> = combine(
        allNotes,
        _searchQuery,
        _selectedCategory,
        _onlyFavorites
    ) { allNotes, query, category, favOnly ->
        allNotes.filter { note ->
            val matchesCategory = (category == null || note.category == category)
            val matchesFav = (!favOnly || note.isFavorite)
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                note.title.contains(query, ignoreCase = true) ||
                note.summary.contains(query, ignoreCase = true) ||
                note.author?.contains(query, ignoreCase = true) == true ||
                note.tags.any { it.contains(query, ignoreCase = true) } ||
                note.rawCaption.contains(query, ignoreCase = true)
            }
            matchesCategory && matchesFav && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getNoteById(id: Long): Flow<ReelNote?> {
        return repository.getNoteById(id)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: NoteCategory?) {
        _selectedCategory.value = category
    }

    fun toggleOnlyFavorites() {
        _onlyFavorites.value = !_onlyFavorites.value
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun updateLanguage(lang: String) {
        preferences.preferredLanguage = lang
        _preferredLanguage.value = lang
    }

    fun dismissOnboarding() {
        preferences.hasSeenOnboarding = true
        _showOnboarding.value = false
    }

    /**
     * Enregistre la creation d'une note et arme la verification d'interstitiel
     * pour le retour depuis l'ecran de detail.
     */
    fun recordNoteCreated() {
        preferences.totalNotesCreatedCount += 1
        preferences.notesCreatedSinceLastAd += 1
        hasPendingPostCreationAdCheck = true
    }

    /**
     * Consomme la verification d'interstitiel au retour de la fiche detail.
     * Retourne true uniquement si l'utilisateur vient de creer une note ET que
     * la politique d'affichage (grace, frequence 1/3, cooldown 5 min) est satisfaite.
     */
    fun consumeInterstitialOnDetailExit(
        nowMs: Long = System.currentTimeMillis(),
        isAdAvailable: Boolean = true
    ): Boolean {
        if (!hasPendingPostCreationAdCheck) return false
        hasPendingPostCreationAdCheck = false

        if (!isAdAvailable) return false

        val shouldShow = AdPlacementPolicy.shouldShowInterstitial(
            totalNotesCreated = preferences.totalNotesCreatedCount,
            notesSinceLastAd = preferences.notesCreatedSinceLastAd,
            lastInterstitialShownAtMs = preferences.lastInterstitialShownAtMs,
            nowMs = nowMs
        )
        if (shouldShow) {
            preferences.notesCreatedSinceLastAd = 0
            preferences.lastInterstitialShownAtMs = nowMs
        }
        return shouldShow
    }

    fun dismissRestrictedReel() {
        _restrictedReelInput.value = null
    }

    /** Relance l'extraction du Reel bloque, typiquement apres une connexion Instagram reussie. */
    fun retryRestrictedReel(context: Context? = null) {
        val pending = _restrictedReelInput.value ?: return
        _restrictedReelInput.value = null
        processSharedUrl(sharedText = pending, context = context)
    }

    fun refreshInstagramLoginState() {
        _isInstagramLoggedIn.value = InstagramSessionManager.isLoggedIn()
    }

    fun logoutInstagram() {
        InstagramSessionManager.logout {
            _isInstagramLoggedIn.value = false
        }
    }

    fun toggleFavorite(note: ReelNote) {
        viewModelScope.launch {
            repository.toggleFavorite(note.id, !note.isFavorite)
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
        }
    }

    fun updateIngredientChecked(note: ReelNote, ingredientIndex: Int, isChecked: Boolean) {
        viewModelScope.launch {
            val currentIngredients = note.structuredData.ingredients.toMutableList()
            if (ingredientIndex in currentIngredients.indices) {
                currentIngredients[ingredientIndex] = currentIngredients[ingredientIndex].copy(isChecked = isChecked)
                val updatedData = note.structuredData.copy(ingredients = currentIngredients)
                repository.updateNote(note.copy(structuredData = updatedData))
            }
        }
    }

    fun updateStepDone(note: ReelNote, stepNumber: Int, isDone: Boolean) {
        viewModelScope.launch {
            val currentSteps = note.structuredData.steps.toMutableList()
            val stepIndex = currentSteps.indexOfFirst { it.stepNumber == stepNumber }
            if (stepIndex != -1) {
                currentSteps[stepIndex] = currentSteps[stepIndex].copy(isDone = isDone)
                val updatedData = note.structuredData.copy(steps = currentSteps)
                repository.updateNote(note.copy(structuredData = updatedData))
            }
        }
    }

    fun processSharedUrl(
        sharedText: String,
        manualCaption: String? = null,
        context: Context? = null,
        onFinished: ((Long) -> Unit)? = null
    ) {
        if (sharedText.isBlank()) return

        _navigateToNotesList.tryEmit(Unit)

        viewModelScope.launch {
            _isProcessing.value = true
            _processingStatus.value = "Récupération du Reel Instagram..."
            try {
                val note = ReelExtractionPipeline.processReel(
                    sharedInput = sharedText,
                    preferredLanguage = preferences.preferredLanguage,
                    manualCaption = manualCaption,
                    context = context,
                    onProgressUpdate = { status ->
                        _processingStatus.value = status
                    }
                )

                _processingStatus.value = "Enregistrement dans vos notes..."
                val savedId = repository.saveNote(note)
                recordNoteCreated()
                _newlyCreatedNoteId.emit(savedId)
                onFinished?.invoke(savedId)
            } catch (e: InstagramRestrictedException) {
                // Pas de snackbar : l'ecran propose un choix explicite entre
                // connexion Instagram et collage manuel de la legende.
                // Si l'utilisateur avait deja fourni une legende, le probleme
                // ne vient pas de l'acces : on retombe sur un message simple.
                if (manualCaption.isNullOrBlank()) {
                    _restrictedReelInput.value = sharedText
                } else {
                    _errorMessage.value = e.message
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Erreur lors du traitement du Reel"
            } finally {
                _isProcessing.value = false
                _processingStatus.value = ""
            }
        }
    }

    class Factory(
        private val repository: ReelNoteRepository,
        private val preferences: PreferencesManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReelNotesViewModel(repository, preferences) as T
        }
    }
}
