package com.example.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.PromptDeskApp
import com.example.ui.components.GoogleGIcon
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.Teal80
import com.example.ui.theme.TealDark40
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onSignOut: () -> Unit = {},
    onNavigateToProfileSetup: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val preferencesManager = PromptDeskApp.instance.preferencesManager

    val userName by preferencesManager.userName.collectAsStateWithLifecycle(initialValue = "Raja Jadav")
    val userEmail by preferencesManager.userEmail.collectAsStateWithLifecycle(initialValue = "rajajadavstudio@gmail.com")
    val userOccupation by preferencesManager.userOccupation.collectAsStateWithLifecycle(initialValue = "Content Creator")

    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val defaultSpeed by viewModel.defaultSpeed.collectAsStateWithLifecycle()
    val defaultFontSize by viewModel.defaultFontSize.collectAsStateWithLifecycle()
    val defaultAlignment by viewModel.defaultAlignment.collectAsStateWithLifecycle()
    val defaultCountdown by viewModel.defaultCountdown.collectAsStateWithLifecycle()
    val voiceFollowLang by viewModel.voiceFollowLang.collectAsStateWithLifecycle()
    val voiceFollowSensitivity by viewModel.voiceFollowSensitivity.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showFloatingModal by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Studio Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = com.example.ui.theme.ObsidianSurfaceHigh,
                        border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.ObsidianBorder),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = Teal80,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "PromptDesk",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 12.sp,
                                color = com.example.ui.theme.ObsidianOnSurfaceVariant
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = com.example.ui.theme.ObsidianSurfaceHigh,
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.ObsidianBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        com.example.ui.home.PulsingDot(color = Teal80)
                        Text(
                            text = "HUD ON",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = Teal80
                            )
                        )
                    }
                }
            }

            // Google Account Card Section (Account Screen)
            SettingsSection(title = "Account", icon = Icons.Default.Person) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Photo / Avatar
                    Surface(
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = Color(0xFF4285F4),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = userName.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = userEmail,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GoogleGIcon(modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Google Account • $userOccupation",
                                fontSize = 11.sp,
                                color = Teal80,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Account Actions: Edit Profile, Preferences, Privacy, Terms, Sign out
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateToProfileSetup,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.ObsidianBorder)
                    ) {
                        Text("Edit Profile", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAboutDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.ObsidianBorder)
                        ) {
                            Text("Privacy & Terms", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Button(
                            onClick = { showSignOutDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2A1515),
                                contentColor = Color(0xFFFFB4AB)
                            )
                        ) {
                            Text("Sign out", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Appearance Section
            SettingsSection(title = "Appearance", icon = Icons.Default.Palette) {
                Text("Theme Mode", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("DARK" to "Dark", "LIGHT" to "Light", "SYSTEM" to "System").forEach { (key, label) ->
                        FilterChip(
                            selected = themeMode == key,
                            onClick = { viewModel.setThemeMode(key) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Teal80,
                                selectedLabelColor = Color(0xFF042F2E)
                            )
                        )
                    }
                }
            }

            // Teleprompter Defaults Section
            SettingsSection(title = "Teleprompter Defaults", icon = Icons.Default.Speed) {
                // Default Speed
                Text(
                    text = "Default Scroll Speed: ${String.format("%.1f", defaultSpeed)}x",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )
                Slider(
                    value = defaultSpeed,
                    onValueChange = { viewModel.setDefaultSpeed(it) },
                    valueRange = 0.5f..4.0f,
                    colors = SliderDefaults.colors(thumbColor = Teal80, activeTrackColor = Teal80)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Default Font Size
                Text(
                    text = "Default Font Size: ${defaultFontSize.toInt()}sp",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )
                Slider(
                    value = defaultFontSize,
                    onValueChange = { viewModel.setDefaultFontSize(it) },
                    valueRange = 28f..80f,
                    colors = SliderDefaults.colors(thumbColor = Teal80, activeTrackColor = Teal80)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Default Alignment
                Text("Default Text Alignment", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("LEFT" to Icons.Default.FormatAlignLeft, "CENTER" to Icons.Default.FormatAlignCenter, "RIGHT" to Icons.Default.FormatAlignRight).forEach { (align, icon) ->
                        FilterChip(
                            selected = defaultAlignment == align,
                            onClick = { viewModel.setDefaultAlignment(align) },
                            label = { Text(align) },
                            leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Teal80,
                                selectedLabelColor = Color(0xFF042F2E)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Countdown timer
                Text("Start Countdown", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0 to "Off", 3 to "3s", 5 to "5s", 10 to "10s").forEach { (sec, label) ->
                        FilterChip(
                            selected = defaultCountdown == sec,
                            onClick = { viewModel.setDefaultCountdown(sec) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Teal80,
                                selectedLabelColor = Color(0xFF042F2E)
                            )
                        )
                    }
                }
            }

            // Voice Follow Section
            SettingsSection(title = "Voice Follow Mode", icon = Icons.Default.RecordVoiceOver) {
                Text("Recognition Language", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("en-US" to "English (US)", "en-IN" to "English (India)", "hi-IN" to "Hindi (हिंदी)").forEach { (code, name) ->
                        FilterChip(
                            selected = voiceFollowLang == code,
                            onClick = { viewModel.setVoiceFollowLang(code) },
                            label = { Text(name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Teal80,
                                selectedLabelColor = Color(0xFF042F2E)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Fuzzy Match Sensitivity: ${String.format("%.1f", voiceFollowSensitivity)}x",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )
                Slider(
                    value = voiceFollowSensitivity,
                    onValueChange = { viewModel.setVoiceFollowSensitivity(it) },
                    valueRange = 0.5f..2.0f,
                    colors = SliderDefaults.colors(thumbColor = Teal80, activeTrackColor = Teal80)
                )
            }

            // Floating Overlay Teleprompter Section
            SettingsSection(title = "Floating Window & Native Camera", icon = Icons.Default.PictureInPicture) {
                Text(
                    text = "PromptDesk floats directly over your device's native Camera app so you can record videos at highest resolution and quality while reading your script near the camera lens.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                val hasOverlay = com.example.overlay.manager.OverlayManager.hasOverlayPermission(context)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Permission: ", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (hasOverlay) TealDark40 else com.example.ui.theme.WarningAmber.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (hasOverlay) "Active / Allowed" else "Permission Required",
                            color = if (hasOverlay) Teal80 else com.example.ui.theme.WarningAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (hasOverlay) {
                                com.example.overlay.manager.OverlayManager.startFloatingPrompter(context, 0L)
                            } else {
                                showFloatingModal = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Teal80, contentColor = Color(0xFF042F2E)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Start Floating", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            com.example.camera.NativeCameraLauncher.launchCamera(context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Open Camera")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { showFloatingModal = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Configure Overlay Permission", color = Teal80)
                }
            }

            // Storage & Maintenance Section
            SettingsSection(title = "Storage & Cache", icon = Icons.Default.CleaningServices) {
                Text(
                    text = "Clear cached video files and temporary thumbnails to free up device space.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { viewModel.clearCache() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Clear Temporary Cache")
                }
            }

            // About & Version Section
            SettingsSection(title = "About PromptDesk", icon = Icons.Default.Info) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("PromptDesk Studio", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        Text("Version 1.0 (Build 2026)", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                    TextButton(onClick = { showAboutDialog = true }) {
                        Text("Credits & Privacy", color = Teal80)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Floating Overlay Guide Modal
    if (showFloatingModal) {
        AlertDialog(
            onDismissRequest = { showFloatingModal = false },
            title = { Text("Display Over Other Apps") },
            text = {
                Text(
                    "To show PromptDesk floating over external camera apps like TikTok or YouTube Live, Android requires the 'Display over other apps' system permission.\n\nTap below to open Android system settings."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFloatingModal = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Teal80, contentColor = Color(0xFF042F2E))
                ) {
                    Text("Open System Settings", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFloatingModal = false }) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("PromptDesk") },
            text = {
                Text(
                    "“Speak naturally. Create confidently.”\n\nPromptDesk is a full-featured creator studio teleprompter engineered for offline speed, zero jitter scrolling, and natural eye contact.\n\nDesigned for YouTubers, educators, influencers, and presenters worldwide."
                )
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Teal80, contentColor = Color(0xFF042F2E))
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }

    // Sign out Confirmation Dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = {
                Text(
                    text = "Sign out of PromptDesk?",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "You will be signed out from your Google account ($userEmail) on this device. Your local scripts remain safely saved.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        coroutineScope.launch {
                            preferencesManager.signOut()
                            onSignOut()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFBA1A1A),
                        contentColor = Color.White
                    )
                ) {
                    Text("Sign out", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            containerColor = Color(0xFF1E2023)
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = com.example.ui.theme.ObsidianSurfaceLow
        ),
        border = BorderStroke(1.dp, com.example.ui.theme.ObsidianBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = com.example.ui.theme.ObsidianSurfaceHigh,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = Teal80, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}
