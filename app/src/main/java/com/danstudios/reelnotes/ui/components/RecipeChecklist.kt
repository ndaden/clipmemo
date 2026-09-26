package com.danstudios.reelnotes.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danstudios.reelnotes.R
import com.danstudios.reelnotes.domain.model.IngredientItem
import com.danstudios.reelnotes.ui.theme.*

@Composable
fun RecipeChecklist(
    ingredients: List<IngredientItem>,
    onToggleIngredient: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCount = ingredients.size
    val checkedCount = ingredients.count { it.isChecked }
    val progress = if (totalCount > 0) checkedCount.toFloat() / totalCount.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "recipeProgress"
    )

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Section Header with count
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.section_ingredients),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = stringResource(R.string.items_checked_format, checkedCount, totalCount),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (checkedCount == totalCount && totalCount > 0) EmeraldSuccess else CyanAccent
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Live Progress Bar Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.recipe_progress),
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 12.sp,
                        color = EmeraldSuccess,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(9999.dp)),
                    color = EmeraldSuccess,
                    trackColor = DarkSurfaceElevated
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Checklist Items
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ingredients.forEachIndexed { index, item ->
                IngredientRow(
                    item = item,
                    onClick = { onToggleIngredient(index, !item.isChecked) }
                )
            }
        }
    }
}

@Composable
private fun IngredientRow(
    item: IngredientItem,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (item.isChecked) EmeraldSuccess.copy(alpha = 0.5f) else DarkBorder,
        animationSpec = tween(durationMillis = 200),
        label = "ingBorder"
    )
    val bgColor by animateColorAsState(
        targetValue = if (item.isChecked) EmeraldSuccess.copy(alpha = 0.08f) else DarkSurface,
        animationSpec = tween(durationMillis = 200),
        label = "ingBg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Custom Checkbox
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (item.isChecked) EmeraldSuccess else DarkSurfaceElevated)
                .border(1.dp, if (item.isChecked) EmeraldSuccess else DarkBorder, RoundedCornerShape(6.dp))
        ) {
            if (item.isChecked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Ingredient Name
        Text(
            text = item.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
            color = if (item.isChecked) TextTertiary else TextPrimary,
            modifier = Modifier.weight(1f)
        )

        // Quantity Badge (Amount + Unit)
        val quantity = listOfNotNull(item.amount, item.unit).filter { it.isNotBlank() }.joinToString(" ")
        if (quantity.isNotBlank()) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(CyanAccent.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = quantity,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent
                )
            }
        }
    }
}
