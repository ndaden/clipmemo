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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danstudios.reelnotes.R
import com.danstudios.reelnotes.ads.AdaptiveBannerAd
import com.danstudios.reelnotes.domain.model.NoteCategory
import com.danstudios.reelnotes.ui.components.AddReelDialog
import com.danstudios.reelnotes.ui.components.AppLogo
import com.danstudios.reelnotes.ui.components.BottomBarTab
import com.danstudios.reelnotes.ui.components.CategoryChipRow
import com.danstudios.reelnotes.ui.components.FloatingBottomBar
import com.danstudios.reelnotes.ui.components.InstagramLoginDialog
import com.danstudios.reelnotes.ui.components.NoteCard
import com.danstudios.reelnotes.ui.components.OnboardingDialog
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
    val showOnboarding by viewModel.showOnboarding.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showInstagramLoginDialog by remember { mutableStateOf(false) }

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
            if (msg.contains("Paramètres") || msg.contains("Settings")) {
                val res = snackbarHostState.showSnackbar(
                    message = msg,
                    actionLabel = context.getString(R.string.tab_settings),
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
                // Sleek Header with Modern Logo
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppLogo(size = 28.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.app_name),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            letterSpacing = (-0.5).sp
                        )
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
                                text = stringResource(R.string.search_placeholder),
                                color = TextPlaceholder,
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = NeonVioletLight,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = null,
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
                            .padding(bottom = 160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Clean icon circle with refined border
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceElevated)
                                    .border(1.dp, DarkBorder, CircleShape)
                            ) {
                                val icon = when {
                                    onlyFavorites -> Icons.Outlined.StarOutline
                                    searchQuery.isNotBlank() -> Icons.Default.Search
                                    else -> Icons.Default.Description
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = NeonVioletLight,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Text(
                                text = when {
                                    onlyFavorites -> stringResource(R.string.empty_favorites_title)
                                    searchQuery.isNotBlank() -> stringResource(R.string.empty_search_title)
                                    selectedCategory != null -> stringResource(R.string.empty_category_title)
                                    else -> stringResource(R.string.empty_notes_title)
                                },
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = when {
                                    onlyFavorites -> stringResource(R.string.empty_favorites_subtitle)
                                    searchQuery.isNotBlank() -> stringResource(R.string.empty_search_subtitle)
                                    else -> stringResource(R.string.empty_notes_subtitle)
                                },
                                fontSize = 13.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 19.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 160.dp
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

            // Bottom Layout: FloatingBottomBar + Adaptive Banner Ad
            val selectedTab = if (onlyFavorites) BottomBarTab.FAVORITES else BottomBarTab.NOTES
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                AdaptiveBannerAd()
            }
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

    if (showOnboarding) {
        OnboardingDialog(
            onDismiss = { viewModel.dismissOnboarding() },
            onConnectInstagram = {
                viewModel.dismissOnboarding()
                showInstagramLoginDialog = true
            }
        )
    }

    if (showInstagramLoginDialog) {
        InstagramLoginDialog(
            onDismiss = { showInstagramLoginDialog = false },
            onLoginSuccess = { showInstagramLoginDialog = false }
        )
    }

    if (isProcessing) {
        ProcessingOverlay(status = processingStatus)
    }
}
