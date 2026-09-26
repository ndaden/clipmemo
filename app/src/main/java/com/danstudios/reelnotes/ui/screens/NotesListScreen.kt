package com.danstudios.reelnotes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danstudios.reelnotes.domain.model.NoteCategory
import com.danstudios.reelnotes.ui.components.AddReelDialog
import com.danstudios.reelnotes.ui.components.BottomBarTab
import com.danstudios.reelnotes.ui.components.CategoryChipRow
import com.danstudios.reelnotes.ui.components.FloatingBottomBar
import com.danstudios.reelnotes.ui.components.NoteCard
import com.danstudios.reelnotes.ui.components.ProcessingOverlay
import com.danstudios.reelnotes.ui.theme.*
import com.danstudios.reelnotes.ui.viewmodel.ReelNotesViewModel

@Composable
fun NotesListScreen(
    viewModel: ReelNotesViewModel,
    onNoteClick: (Long) -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val notes by viewModel.filteredNotes.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val onlyFavorites by viewModel.onlyFavorites.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val processingStatus by viewModel.processingStatus.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Calculate category counts from all notes
    val totalNotesCount = allNotes.size
    val categoryCounts: Map<NoteCategory?, Int> = remember(allNotes) {
        val map = mutableMapOf<NoteCategory?, Int>()
        for (cat in NoteCategory.entries) {
            map[cat] = allNotes.count { it.category == cat }
        }
        map
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            if (msg.contains("Paramètres")) {
                val res = snackbarHostState.showSnackbar(
                    message = msg,
                    actionLabel = "Paramètres",
                    duration = SnackbarDuration.Long
                )
                if (res == SnackbarResult.ActionPerformed) {
                    onSettingsClick()
                }
            } else {
                snackbarHostState.showSnackbar(message = msg, duration = SnackbarDuration.Long)
            }
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Sleek Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ReelNotes",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonViolet.copy(alpha = 0.2f))
                                .border(1.dp, NeonViolet.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "AI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonVioletLight
                            )
                        }
                    }
                }

                // Modern Rounded Search Pill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = {
                            Text(
                                text = "Rechercher une note, recette, astuce...",
                                color = TextPlaceholder,
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Rechercher",
                                tint = NeonVioletLight,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Effacer",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated,
                            focusedBorderColor = NeonViolet,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = NeonVioletLight
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Category filter row with counts
                CategoryChipRow(
                    selectedCategory = selectedCategory,
                    onlyFavorites = onlyFavorites,
                    onCategorySelected = { viewModel.setCategory(it) },
                    onToggleFavorites = { viewModel.toggleOnlyFavorites() },
                    categoryCounts = categoryCounts,
                    totalNotesCount = totalNotesCount
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Notes list or modern empty state
                if (notes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp)
                            .padding(bottom = 100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Glowing icon circle
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                NeonViolet.copy(alpha = 0.25f),
                                                DarkSurfaceElevated.copy(alpha = 0.6f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                                    .border(1.dp, NeonViolet.copy(alpha = 0.35f), CircleShape)
                            ) {
                                Text(
                                    text = when {
                                        onlyFavorites -> "⭐"
                                        searchQuery.isNotBlank() -> "🔍"
                                        else -> "✨"
                                    },
                                    fontSize = 32.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = when {
                                    onlyFavorites -> "Aucun favori pour l'instant"
                                    searchQuery.isNotBlank() -> "Aucun résultat trouvé"
                                    selectedCategory != null -> "Aucune note dans cette catégorie"
                                    else -> "Aucun Reel enregistré"
                                },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = when {
                                    onlyFavorites -> "Touchez l'étoile sur une note pour la retrouver facilement ici."
                                    searchQuery.isNotBlank() -> "Vérifiez l'orthographe ou essayez d'autres mots-clés."
                                    else -> "Partagez un Reel Instagram vers ReelNotes ou appuyez sur + pour créer votre première fiche IA."
                                },
                                fontSize = 14.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )

                            if (allNotes.isEmpty()) {
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { viewModel.reloadSampleData() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DarkSurfaceElevated,
                                        contentColor = NeonVioletLight
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                                    shape = RoundedCornerShape(14.dp),
                                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = NeonVioletLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Charger des exemples de notes",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 100.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(notes, key = { it.id }) { note ->
                            NoteCard(
                                note = note,
                                onClick = { onNoteClick(note.id) },
                                onToggleFavorite = { viewModel.toggleFavorite(note) }
                            )
                        }
                    }
                }
            }

            // Floating Bottom Bar anchored at the bottom
            val selectedTab = if (onlyFavorites) BottomBarTab.FAVORITES else BottomBarTab.NOTES
            FloatingBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    when (tab) {
                        BottomBarTab.NOTES -> {
                            if (onlyFavorites) viewModel.toggleOnlyFavorites()
                            viewModel.setCategory(null)
                        }
                        BottomBarTab.FAVORITES -> {
                            if (!onlyFavorites) viewModel.toggleOnlyFavorites()
                        }
                        BottomBarTab.SETTINGS -> {
                            onSettingsClick()
                        }
                    }
                },
                onAddClick = { showAddDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            )
        }
    }

    if (showAddDialog) {
        AddReelDialog(
            onDismiss = { showAddDialog = false },
            onSubmit = { url, caption ->
                showAddDialog = false
                viewModel.processSharedUrl(
                    sharedText = url,
                    manualCaption = caption,
                    context = context
                )
            }
        )
    }

    if (isProcessing) {
        ProcessingOverlay(status = processingStatus)
    }
}
