package com.danstudios.reelnotes.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danstudios.reelnotes.R
import com.danstudios.reelnotes.ui.components.InstagramLoginDialog
import com.danstudios.reelnotes.ui.theme.*
import com.danstudios.reelnotes.ui.viewmodel.ReelNotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ReelNotesViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val preferredLanguage by viewModel.preferredLanguage.collectAsState()
    val isInstagramLoggedIn by viewModel.isInstagramLoggedIn.collectAsState()

    var showLoginDialog by remember { mutableStateOf(false) }

    if (showLoginDialog) {
        InstagramLoginDialog(
            onDismiss = {
                showLoginDialog = false
                viewModel.refreshInstagramLoginState()
            },
            onLoginSuccess = {
                showLoginDialog = false
                viewModel.refreshInstagramLoginState()
                Toast.makeText(context, context.getString(R.string.toast_login_success), Toast.LENGTH_SHORT).show()
            }
        )
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.btn_back),
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Section 1: Instagram Session (Optional)
            Text(
                text = stringResource(R.string.settings_instagram_title),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = NeonVioletLight
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.settings_instagram_desc),
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isInstagramLoggedIn)
                                        EmeraldSuccess.copy(alpha = 0.15f)
                                    else
                                        DarkSurface
                                )
                        ) {
                            Icon(
                                imageVector = if (isInstagramLoggedIn) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isInstagramLoggedIn) EmeraldLight else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isInstagramLoggedIn)
                                    stringResource(R.string.status_connected)
                                else
                                    stringResource(R.string.status_not_connected),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = if (isInstagramLoggedIn) EmeraldLight else TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isInstagramLoggedIn)
                                    stringResource(R.string.status_connected_desc)
                                else
                                    stringResource(R.string.status_not_connected_desc),
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (isInstagramLoggedIn) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.logoutInstagram()
                                    Toast.makeText(context, context.getString(R.string.toast_logged_out), Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                            ) {
                                Text(stringResource(R.string.btn_disconnect), fontSize = 13.sp)
                            }
                        } else {
                            Button(
                                onClick = { showLoginDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DarkSurfaceElevated,
                                    contentColor = NeonVioletLight
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                            ) {
                                Text(stringResource(R.string.btn_connect_instagram), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 2: App Language
            Text(
                text = stringResource(R.string.settings_language_title),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = NeonVioletLight
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (preferredLanguage == "fr") DarkSurfaceElevated else Color.Transparent)
                            .clickable { viewModel.updateLanguage("fr") }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = preferredLanguage == "fr",
                            onClick = { viewModel.updateLanguage("fr") },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = NeonViolet,
                                unselectedColor = TextSecondary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.lang_french),
                            fontSize = 14.sp,
                            fontWeight = if (preferredLanguage == "fr") FontWeight.Bold else FontWeight.Normal,
                            color = TextPrimary
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (preferredLanguage == "en") DarkSurfaceElevated else Color.Transparent)
                            .clickable { viewModel.updateLanguage("en") }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = preferredLanguage == "en",
                            onClick = { viewModel.updateLanguage("en") },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = NeonViolet,
                                unselectedColor = TextSecondary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.lang_english),
                            fontSize = 14.sp,
                            fontWeight = if (preferredLanguage == "en") FontWeight.Bold else FontWeight.Normal,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 3: About
            Text(
                text = stringResource(R.string.settings_about_title),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = NeonVioletLight
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.app_version),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(R.string.settings_about_desc),
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
