package com.example.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.Teal80
import com.example.ui.theme.TealDark40

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

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
}

@Composable
fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TealDark40.copy(alpha = 0.4f),
                    modifier = Modifier.size(32.dp)
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
