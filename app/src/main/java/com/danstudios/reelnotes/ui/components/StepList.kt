package com.danstudios.reelnotes.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danstudios.reelnotes.domain.model.StepItem
import com.danstudios.reelnotes.ui.theme.*

@Composable
fun StepList(
    title: String,
    steps: List<StepItem>,
    onToggleStep: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        steps.forEachIndexed { index, step ->
            val isLast = index == steps.size - 1

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onToggleStep(step.stepNumber, !step.isDone) }
                    ),
                verticalAlignment = Alignment.Top
            ) {
                // Timeline Column: Circle Badge + Connecting Line
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(32.dp)
                ) {
                    val circleBg by animateColorAsState(
                        targetValue = if (step.isDone) EmeraldSuccess else DarkSurfaceElevated,
                        animationSpec = tween(durationMillis = 200),
                        label = "stepCircleBg"
                    )
                    val circleBorder by animateColorAsState(
                        targetValue = if (step.isDone) EmeraldSuccess else DarkBorder,
                        animationSpec = tween(durationMillis = 200),
                        label = "stepCircleBorder"
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(circleBg)
                            .border(1.dp, circleBorder, CircleShape)
                    ) {
                        Text(
                            text = "${step.stepNumber}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (step.isDone) Color.White else TextSecondary
                        )
                    }

                    if (!isLast) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(44.dp)
                                .background(DarkBorder)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Step Instruction Text
                Text(
                    text = step.instruction,
                    fontSize = 14.sp,
                    color = if (step.isDone) TextTertiary else TextPrimary,
                    textDecoration = if (step.isDone) TextDecoration.LineThrough else TextDecoration.None,
                    lineHeight = 20.sp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = if (isLast) 0.dp else 20.dp)
                )
            }
        }
    }
}
