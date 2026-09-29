package com.example.ui.teleprompter

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.CountdownOverlay
import com.example.ui.theme.MirrorBadgeBg
import com.example.ui.theme.MirrorBadgeText
import com.example.ui.theme.Teal80
import com.example.ui.theme.TealDark40
import com.example.util.Formatters
import kotlinx.coroutines.delay

@Composable
fun TeleprompterScreen(
    scriptId: Long,
    onNavigateBack: () -> Unit,
    viewModel: TeleprompterViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val script by viewModel.script.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val scrollSpeed by viewModel.scrollSpeed.collectAsStateWithLifecycle()
    val fontSize by viewModel.fontSize.collectAsStateWithLifecycle()
    val mirrorMode by viewModel.mirrorMode.collectAsStateWithLifecycle()
    val alignment by viewModel.alignment.collectAsStateWithLifecycle()
    val textWidthPercent by viewModel.textWidthPercent.collectAsStateWithLifecycle()
    val controlsVisible by viewModel.controlsVisible.collectAsStateWithLifecycle()
    val countdownActive by viewModel.countdownActive.collectAsStateWithLifecycle()
    val countdownDuration by viewModel.countdownDuration.collectAsStateWithLifecycle()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsStateWithLifecycle()
    val isVoiceFollowEnabled by viewModel.isVoiceFollowEnabled.collectAsStateWithLifecycle()

    var activeControlTab by remember { mutableStateOf<ControlTab?>(null) }
    val scrollState = rememberScrollState()

    // Permission launcher for Voice Follow mic
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleVoiceFollow()
        }
    }

    LaunchedEffect(scriptId) {
        viewModel.loadScript(scriptId)
    }

    // Smooth continuous scrolling loop
    LaunchedEffect(isPlaying, scrollSpeed) {
        while (isPlaying) {
            // Speed calculation: 1.0x scrolls roughly 1.5 pixels per 16ms frame (approx 90-100px/sec)
            val step = (scrollSpeed * 1.6f).toInt().coerceAtLeast(1)
            scrollState.scrollBy(step.toFloat())
            if (scrollState.maxValue > 0) {
                viewModel.updateScrollFraction(scrollState.value.toFloat() / scrollState.maxValue)
            }
            delay(16L) // 60 FPS
        }
    }

    BackHandler {
        viewModel.pausePlaying()
        onNavigateBack()
    }

    val textAlign = when (alignment) {
        "LEFT" -> TextAlign.Left
        "RIGHT" -> TextAlign.Right
        else -> TextAlign.Center
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { viewModel.togglePlayPause() },
                    onDoubleTap = { viewModel.toggleControlsVisibility() }
                )
            }
    ) {
        // Script Scrolling Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .graphicsLayer {
                    if (mirrorMode) {
                        scaleX = -1f
                    }
                }
                .padding(
                    top = 220.dp, // Space so script starts near top reading area
                    bottom = 400.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(textWidthPercent / 100f)
                    .widthIn(max = 800.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = script?.content ?: "",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = fontSize.sp,
                        lineHeight = (fontSize * 1.45f).sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        textAlign = textAlign
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Top Subtle Eye Contact Reading Guide Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .align(Alignment.TopCenter)
                .padding(top = 180.dp)
                .background(Teal80.copy(alpha = 0.25f))
        )

        // Top Floating Bar (Back button, Title, Timer, Mirror Indicator)
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        viewModel.pausePlaying()
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (mirrorMode) {
                        Surface(
                            color = MirrorBadgeBg,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "MIRROR ON",
                                color = MirrorBadgeText,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (isVoiceFollowEnabled) {
                        Surface(
                            color = TealDark40,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "VOICE FOLLOW",
                                color = Teal80,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = Formatters.formatDuration(elapsedSeconds),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Teal80,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            viewModel.pausePlaying()
                            if (com.example.overlay.manager.OverlayManager.hasOverlayPermission(context)) {
                                com.example.overlay.manager.OverlayManager.startFloatingPrompter(context, scriptId)
                                onNavigateBack()
                            } else {
                                com.example.overlay.manager.OverlayManager.requestOverlayPermission(context)
                            }
                        },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                            .size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Floating Overlay Prompter",
                            tint = Teal80,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Bottom Controls Panel (Speed, Font size, Play/Pause, Mirror, Align, Voice Follow)
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Secondary Settings Sub-panel (Speed slider / Font size slider)
                if (activeControlTab != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF141A22).copy(alpha = 0.95f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            when (activeControlTab) {
                                ControlTab.SPEED -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Scroll Speed: ${String.format("%.1f", scrollSpeed)}x",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Row {
                                            IconButton(onClick = { viewModel.adjustScrollSpeed(-0.2f) }) {
                                                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Teal80)
                                            }
                                            IconButton(onClick = { viewModel.adjustScrollSpeed(0.2f) }) {
                                                Icon(Icons.Default.Add, contentDescription = "Increase", tint = Teal80)
                                            }
                                        }
                                    }
                                    Slider(
                                        value = scrollSpeed,
                                        onValueChange = { viewModel.setScrollSpeed(it) },
                                        valueRange = 0.1f..5.0f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = Teal80,
                                            activeTrackColor = Teal80,
                                            inactiveTrackColor = Color(0xFF2A3644)
                                        )
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        SpeedPresetChip("0.8x Slow", scrollSpeed == 0.8f) { viewModel.setScrollSpeed(0.8f) }
                                        SpeedPresetChip("1.3x Normal", scrollSpeed == 1.3f) { viewModel.setScrollSpeed(1.3f) }
                                        SpeedPresetChip("2.2x Fast", scrollSpeed == 2.2f) { viewModel.setScrollSpeed(2.2f) }
                                        SpeedPresetChip("3.5x Pro", scrollSpeed == 3.5f) { viewModel.setScrollSpeed(3.5f) }
                                    }
                                }
                                ControlTab.FONT -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Font Size: ${fontSize.toInt()}sp",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Slider(
                                        value = fontSize,
                                        onValueChange = { viewModel.setFontSize(it) },
                                        valueRange = 24f..96f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = Teal80,
                                            activeTrackColor = Teal80,
                                            inactiveTrackColor = Color(0xFF2A3644)
                                        )
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        SpeedPresetChip("Small 32", fontSize == 32f) { viewModel.setFontSize(32f) }
                                        SpeedPresetChip("Medium 44", fontSize == 44f) { viewModel.setFontSize(44f) }
                                        SpeedPresetChip("Large 58", fontSize == 58f) { viewModel.setFontSize(58f) }
                                        SpeedPresetChip("XL 76", fontSize == 76f) { viewModel.setFontSize(76f) }
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                }

                // Primary Bottom Control Bar
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = com.example.ui.theme.ObsidianSurface.copy(alpha = 0.95f)
                    ),
                    border = BorderStroke(1.dp, com.example.ui.theme.ObsidianBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Restart
                        IconButton(onClick = { viewModel.restart() }) {
                            Icon(Icons.Default.Replay, contentDescription = "Restart", tint = Color.White)
                        }

                        // Speed Tab Toggle
                        IconButton(onClick = {
                            activeControlTab = if (activeControlTab == ControlTab.SPEED) null else ControlTab.SPEED
                        }) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Speed",
                                tint = if (activeControlTab == ControlTab.SPEED) Teal80 else Color.White
                            )
                        }

                        // Primary Play / Pause Button
                        FilledIconButton(
                            onClick = { viewModel.togglePlayPause() },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Teal80,
                                contentColor = Color(0xFF003730)
                            ),
                            shape = CircleShape,
                            modifier = Modifier
                                .size(56.dp)
                                .testTag("prompter_play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Font Size Tab Toggle
                        IconButton(onClick = {
                            activeControlTab = if (activeControlTab == ControlTab.FONT) null else ControlTab.FONT
                        }) {
                            Icon(
                                imageVector = Icons.Default.FormatSize,
                                contentDescription = "Font size",
                                tint = if (activeControlTab == ControlTab.FONT) Teal80 else Color.White
                            )
                        }

                        // Mirror Mode Toggle
                        IconButton(onClick = { viewModel.toggleMirrorMode() }) {
                            Icon(
                                imageVector = Icons.Default.Flip,
                                contentDescription = "Mirror mode",
                                tint = if (mirrorMode) Teal80 else Color.White
                            )
                        }

                        // Voice Follow Mic Toggle
                        IconButton(
                            onClick = {
                                val hasAudioPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasAudioPermission) {
                                    viewModel.toggleVoiceFollow()
                                } else {
                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isVoiceFollowEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = "Voice follow",
                                tint = if (isVoiceFollowEnabled) Teal80 else Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }

        // Countdown Overlay
        if (countdownActive) {
            CountdownOverlay(
                seconds = countdownDuration,
                onFinished = { viewModel.onCountdownFinished() }
            )
        }
    }
}

enum class ControlTab {
    SPEED, FONT
}

@Composable
fun SpeedPresetChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(text, fontSize = 11.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Teal80,
            selectedLabelColor = Color(0xFF042F2E),
            containerColor = Color(0xFF1E2836),
            labelColor = Color.White
        )
    )
}
