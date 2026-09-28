package com.danstudios.reelnotes.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.danstudios.reelnotes.R
import com.danstudios.reelnotes.ads.AdaptiveBannerAd
import com.danstudios.reelnotes.domain.model.NoteCategory
import com.danstudios.reelnotes.ui.components.RecipeChecklist
import com.danstudios.reelnotes.ui.components.StepList
import com.danstudios.reelnotes.ui.theme.*
import com.danstudios.reelnotes.ui.util.UrlLauncher
import com.danstudios.reelnotes.ui.viewmodel.ReelNotesViewModel

@Composable
fun NoteDetailScreen(
    noteId: Long,
    viewModel: ReelNotesViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val note by viewModel.getNoteById(noteId).collectAsState(initial = null)

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showRawCaption by remember { mutableStateOf(false) }

    if (note == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = NeonViolet)
        }
        return
    }

    val currentNote = note!!
    val hasThumbnail = !currentNote.thumbnailUrl.isNullOrBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Main scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Thumbnail Section
            if (hasThumbnail) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    AsyncImage(
                        model = currentNote.thumbnailUrl,
                        contentDescription = currentNote.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Vertical gradient fade from transparent to DarkBackground
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        DarkBackground.copy(alpha = 0.5f),
                                        DarkBackground
                                    )
                                )
                            )
                    )
                }
            } else {
                Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                Spacer(modifier = Modifier.height(64.dp))
            }

            // Body Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                // Category & Author Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = stringResource(currentNote.category.labelRes),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }

                    // Author Pill
                    if (!currentNote.author.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, DarkBorder, RoundedCornerShape(9999.dp))
                                .clickable {
                                    UrlLauncher.openInstagramReel(context, currentNote.reelUrl)
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "${currentNote.author} • ${stringResource(R.string.see_on_instagram)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NeonVioletLight
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title
                Text(
                    text = currentNote.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    lineHeight = 30.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Direct Instagram CTA Button in DarkSurfaceElevated with play icon
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, DarkBorderHover, RoundedCornerShape(14.dp))
                        .clickable {
                            UrlLauncher.openInstagramReel(context, currentNote.reelUrl)
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(NeonViolet.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = NeonVioletLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.open_on_instagram),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Summary Card with DarkSurface, DarkBorder, and NeonViolet highlight
                if (currentNote.summary.isNotBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(3.dp, 16.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(NeonViolet)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.section_summary),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonVioletLight
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentNote.summary,
                                fontSize = 14.sp,
                                color = TextSecondary,
                                lineHeight = 22.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Recipe Times & Servings Chips in DarkSurfaceElevated
                val data = currentNote.structuredData
                val hasTimes = data.prepTime != null || data.cookTime != null || data.servings != null
                if (hasTimes) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        data.prepTime?.let {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkSurfaceElevated)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.prep_time_format, it),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = EmeraldLight
                                )
                            }
                        }
                        data.cookTime?.let {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkSurfaceElevated)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.cook_time_format, it),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RecipeColor
                                )
                            }
                        }
                        data.servings?.let {
                            val text = if (it.any { c -> c.isLetter() }) it else stringResource(R.string.servings_format, it)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkSurfaceElevated)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = text,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CyanAccent
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Recipe Ingredients Checklist
                if (data.ingredients.isNotEmpty()) {
                    RecipeChecklist(
                        ingredients = data.ingredients,
                        onToggleIngredient = { index, isChecked ->
                            viewModel.updateIngredientChecked(currentNote, index, isChecked)
                        }
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Steps Timeline List
                if (data.steps.isNotEmpty()) {
                    val stepTitle = when (currentNote.category) {
                        NoteCategory.RECIPE -> stringResource(R.string.step_title_recipe)
                        NoteCategory.WORKOUT -> stringResource(R.string.step_title_workout)
                        else -> stringResource(R.string.step_title_default)
                    }
                    StepList(
                        title = stepTitle,
                        steps = data.steps,
                        onToggleStep = { stepNumber, isDone ->
                            viewModel.updateStepDone(currentNote, stepNumber, isDone)
                        }
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Tips Card with amber accent border
                if (data.tips.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, RecipeColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = RecipeColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.section_tips),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RecipeColor
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            for (tip in data.tips) {
                                Row(
                                    modifier = Modifier.padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "• ",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RecipeColor
                                    )
                                    Text(
                                        text = tip,
                                        fontSize = 13.sp,
                                        color = TextSecondary,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Key Takeaways Card
                if (data.keyTakeaways.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, CyanAccent.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.section_takeaways),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            for (point in data.keyTakeaways) {
                                Row(
                                    modifier = Modifier.padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "• ",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyanAccent
                                    )
                                    Text(
                                        text = point,
                                        fontSize = 13.sp,
                                        color = TextSecondary,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Tags
                if (currentNote.tags.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currentNote.tags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceElevated)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "#$tag",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = NeonVioletLight
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Expandable Original Caption
                if (currentNote.rawCaption.isNotBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                            .clickable { showRawCaption = !showRawCaption },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.section_original_caption),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary
                                )
                                Icon(
                                    imageVector = if (showRawCaption) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            AnimatedVisibility(visible = showRawCaption) {
                                Column {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = DarkBorder)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = currentNote.rawCaption,
                                        fontSize = 12.sp,
                                        color = TextTertiary,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                Spacer(modifier = Modifier.navigationBarsPadding().height(70.dp))
            }
        }

        // Circular Glass Action Buttons overlaying top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassActionButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Retour",
                onClick = onBack
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Favorite
                GlassActionButton(
                    icon = if (currentNote.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Favori",
                    tint = if (currentNote.isFavorite) GoldStar else TextPrimary,
                    onClick = { viewModel.toggleFavorite(currentNote) }
                )

                // Copy Markdown
                GlassActionButton(
                    icon = Icons.Default.ContentCopy,
                    contentDescription = "Copier",
                    onClick = {
                        clipboardManager.setText(AnnotatedString(currentNote.markdownContent))
                        Toast.makeText(context, context.getString(R.string.toast_note_copied), Toast.LENGTH_SHORT).show()
                    }
                )

                // Share
                GlassActionButton(
                    icon = Icons.Default.Share,
                    contentDescription = "Partager",
                    onClick = {
                        UrlLauncher.shareText(
                            context = context,
                            title = currentNote.title,
                            text = currentNote.markdownContent,
                            chooserTitle = context.getString(R.string.share_note_title)
                        )
                    }
                )

                // Delete
                GlassActionButton(
                    icon = Icons.Default.Delete,
                    contentDescription = "Supprimer",
                    tint = ErrorRed,
                    onClick = { showDeleteConfirm = true }
                )
            }
        }

        // Anchored Adaptive Banner Ad at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            AdaptiveBannerAd()
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.border(1.dp, DarkBorder, RoundedCornerShape(20.dp)),
            title = {
                Text(
                    text = stringResource(R.string.dialog_delete_title),
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.dialog_delete_message),
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteNote(currentNote.id)
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(stringResource(R.string.btn_delete), fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
                ) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}

@Composable
private fun GlassActionButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    tint: Color = TextPrimary,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(DarkSurface.copy(alpha = 0.82f))
            .border(1.dp, DarkBorder, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White),
                onClick = onClick
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
    }
}
