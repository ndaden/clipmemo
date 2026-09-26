package com.danstudios.reelnotes.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.danstudios.reelnotes.ui.theme.CyanAccent
import com.danstudios.reelnotes.ui.theme.NeonViolet

@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.28f))
            .background(
                Brush.linearGradient(
                    colors = listOf(NeonViolet, CyanAccent),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(size * 0.22f)
        ) {
            val w = this.size.width
            val h = this.size.height
            val corner = w * 0.32f

            // Document body with folded top-right corner
            val docPath = Path().apply {
                moveTo(0f, 0f)
                lineTo(w - corner, 0f)
                lineTo(w, corner)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(docPath, color = Color.White.copy(alpha = 0.25f))

            // Folded corner tab
            val tabPath = Path().apply {
                moveTo(w - corner, 0f)
                lineTo(w - corner, corner)
                lineTo(w, corner)
                close()
            }
            drawPath(tabPath, color = Color.White.copy(alpha = 0.45f))

            // Play arrow in the center
            val playPath = Path().apply {
                val cx = w * 0.52f
                val cy = h * 0.54f
                val pw = w * 0.28f
                val ph = h * 0.32f
                moveTo(cx - pw * 0.45f, cy - ph * 0.5f)
                lineTo(cx + pw * 0.55f, cy)
                lineTo(cx - pw * 0.45f, cy + ph * 0.5f)
                close()
            }
            drawPath(playPath, color = Color.White)
        }
    }
}
