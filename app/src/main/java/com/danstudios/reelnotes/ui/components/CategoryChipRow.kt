package com.danstudios.reelnotes.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danstudios.reelnotes.domain.model.NoteCategory
import com.danstudios.reelnotes.ui.theme.*

@Composable
fun CategoryChipRow(
    selectedCategory: NoteCategory?,
    onlyFavorites: Boolean,
    onCategorySelected: (NoteCategory?) -> Unit,
    onToggleFavorites: () -> Unit,
    categoryCounts: Map<NoteCategory?, Int> = emptyMap(),
    totalNotesCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "✨ Tout" Chip
        val isAllSelected = selectedCategory == null && !onlyFavorites
        ModernChip(
            label = "✨ Tout",
            count = if (totalNotesCount > 0) totalNotesCount else categoryCounts[null],
            isSelected = isAllSelected,
            onClick = {
                if (onlyFavorites) onToggleFavorites()
                onCategorySelected(null)
            }
        )

        // Specific category chips
        for (category in NoteCategory.entries) {
            val isSelected = selectedCategory == category && !onlyFavorites
            val count = categoryCounts[category]
            ModernChip(
                label = "${category.iconEmoji} ${category.label}",
                count = count,
                isSelected = isSelected,
                onClick = {
                    if (onlyFavorites) onToggleFavorites()
                    if (selectedCategory == category) {
                        onCategorySelected(null)
                    } else {
                        onCategorySelected(category)
                    }
                }
            )
        }
    }
}

@Composable
private fun ModernChip(
    label: String,
    count: Int?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) NeonViolet.copy(alpha = 0.25f) else DarkSurface,
        animationSpec = tween(durationMillis = 200),
        label = "chipBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) NeonViolet else DarkBorder,
        animationSpec = tween(durationMillis = 200),
        label = "chipBorder"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) TextPrimary else TextSecondary,
        animationSpec = tween(durationMillis = 200),
        label = "chipText"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(9999.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(9999.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = textColor
        )

        if (count != null && count > 0) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(if (isSelected) NeonViolet.copy(alpha = 0.4f) else DarkSurfaceElevated)
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "$count",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) TextPrimary else TextTertiary
                )
            }
        }
    }
}
