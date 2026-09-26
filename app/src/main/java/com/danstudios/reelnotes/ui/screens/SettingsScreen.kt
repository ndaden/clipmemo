package com.danstudios.reelnotes.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val currentApiKey by viewModel.geminiApiKey.collectAsState()
    val preferredLanguage by viewModel.preferredLanguage.collectAsState()
    val isInstagramLoggedIn by viewModel.isInstagramLoggedIn.collectAsState()

    var apiKeyInput by remember(currentApiKey) { mutableStateOf(currentApiKey) }
    var showPassword by remember { mutableStateOf(false) }

    var isTestingKey by remember { mutableStateOf(false) }
    var keyTestResult by remember { mutableStateOf<Result<String>?>(null) }
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
                Toast.makeText(context, "Connexion Instagram réussie !", Toast.LENGTH_SHORT).show()
            }
        )
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Paramètres",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
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
            // Section 1: Gemini AI
            Text(
                text = "Intelligence Artificielle Google Gemini",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
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
                        text = "Grâce à Gemini 3.6 Flash Multimodal, l'application écoute la voix du Reel, analyse les images et extrait automatiquement recettes, ingrédients précis et étapes pas à pas.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = {
                            apiKeyInput = it
                            keyTestResult = null
                        },
                        label = { Text("Clé API Google Gemini", color = TextSecondary) },
                        placeholder = { Text("AIzaSy... ou AQ...", color = TextPlaceholder) },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Key, contentDescription = null, tint = NeonVioletLight)
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showPassword) "Masquer" else "Afficher",
                                    tint = TextSecondary
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated,
                            focusedBorderColor = NeonViolet,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = NeonVioletLight
                        )
                    )

                    // Notice if key format is unusual
                    if (apiKeyInput.isNotBlank() && !apiKeyInput.startsWith("AIzaSy") && !apiKeyInput.startsWith("AQ.") && keyTestResult?.isSuccess != true) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(ErrorRed.copy(alpha = 0.12f))
                                .border(1.dp, ErrorRed.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = ErrorRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Attention : les clés Google AI Studio débutent normalement par 'AIzaSy'.",
                                fontSize = 12.sp,
                                color = ErrorRedLight
                            )
                        }
                    }

                    // Test result banner
                    keyTestResult?.let { result ->
                        Spacer(modifier = Modifier.height(10.dp))
                        val isSuccess = result.isSuccess
                        val msg = if (isSuccess) result.getOrNull() ?: "" else result.exceptionOrNull()?.message ?: ""
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSuccess) EmeraldSuccess.copy(alpha = 0.12f) else ErrorRed.copy(alpha = 0.12f)
                                )
                                .border(
                                    1.dp,
                                    if (isSuccess) EmeraldSuccess.copy(alpha = 0.3f) else ErrorRed.copy(alpha = 0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(10.dp)
                        ) {
                            Icon(
                                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isSuccess) EmeraldLight else ErrorRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                fontSize = 13.sp,
                                color = if (isSuccess) EmeraldLight else ErrorRedLight
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (apiKeyInput.isNotBlank()) {
                                    isTestingKey = true
                                    keyTestResult = null
                                    viewModel.testGeminiKey(apiKeyInput.trim()) { res ->
                                        isTestingKey = false
                                        keyTestResult = res
                                    }
                                } else {
                                    Toast.makeText(context, "Veuillez entrer une clé d'abord", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = !isTestingKey && apiKeyInput.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = TextPrimary
                            )
                        ) {
                            if (isTestingKey) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = NeonViolet)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Test...", fontSize = 13.sp)
                            } else {
                                Text("Tester la clé", fontSize = 13.sp)
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.updateApiKey(apiKeyInput.trim())
                                Toast.makeText(context, "Clé API enregistrée !", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonViolet,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Enregistrer", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/app/apikey"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            tint = NeonVioletLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Obtenir une clé Gemini gratuite (Google AI Studio)",
                            color = NeonVioletLight,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 2: Instagram Session (Optional)
            Text(
                text = "Session Instagram (Optionnel)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
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
                        text = "Certains Reels (notamment avec restrictions d'âge ou provenant de comptes privés) ne sont pas accessibles sans compte Instagram.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Status box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isInstagramLoggedIn) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isInstagramLoggedIn) EmeraldLight else TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isInstagramLoggedIn) "Connecté à Instagram" else "Non connecté",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = if (isInstagramLoggedIn) EmeraldLight else TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isInstagramLoggedIn) "Tous les Reels peuvent être analysés" else "Seuls les Reels publics sans restriction sont accessibles",
                                fontSize = 11.sp,
                                color = TextTertiary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        if (isInstagramLoggedIn) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.logoutInstagram()
                                    Toast.makeText(context, "Déconnexion effectuée", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                            ) {
                                Text("Se déconnecter", fontSize = 13.sp)
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
                                Text("Se connecter à Instagram", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 3: Langue des résumés
            Text(
                text = "Langue des résumés",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
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
                            text = "Français (par défaut)",
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
                            text = "English",
                            fontSize = 14.sp,
                            fontWeight = if (preferredLanguage == "en") FontWeight.Bold else FontWeight.Normal,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 4: Données & Démo
            Text(
                text = "Données & Démonstration",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
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
                        text = "Ajoutez des exemples de notes pré-remplies (recette de pâtes crémeuses, routine sportive, astuces tech) pour tester l'application.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.reloadSampleData()
                            Toast.makeText(context, "Exemples ajoutés avec succès !", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkSurfaceElevated,
                            contentColor = NeonVioletLight
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = NeonVioletLight,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Recharger les notes d'exemples", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 5: À propos
            Text(
                text = "À propos de ReelNotes",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
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
                        text = "ReelNotes v1.1.0",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Application Android conçue pour transformer facilement les Reels Instagram en fiches de recettes, tutoriels et notes pratiques avec analyse IA multimodale audio/vidéo.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
