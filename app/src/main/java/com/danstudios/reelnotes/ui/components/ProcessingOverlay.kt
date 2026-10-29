package com.danstudios.reelnotes.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.danstudios.reelnotes.R
import com.danstudios.reelnotes.ui.theme.*

@Composable
fun ProcessingOverlay(
    status: String,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = { /* non-cancelable during extraction */ }) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = modifier
                .width(280.dp)
                .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = NeonViolet,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(44.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.overlay_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = status.ifBlank { stringResource(R.string.status_processing_generic) },
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
