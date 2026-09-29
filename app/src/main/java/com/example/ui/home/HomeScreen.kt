package com.example.ui.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.BatteryFull
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.camera.NativeCameraLauncher
import com.example.data.local.ScriptEntity
import com.example.overlay.manager.OverlayManager
import com.example.ui.theme.ColorTokens
import com.example.ui.theme.ObsidianBase
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianOnSurface
import com.example.ui.theme.ObsidianOnSurfaceVariant
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceHigh
import com.example.ui.theme.ObsidianSurfaceHighest
import com.example.ui.theme.ObsidianSurfaceLow
import com.example.ui.theme.ObsidianSurfaceLowest
import com.example.ui.theme.PeachTertiaryContainer
import com.example.ui.theme.TealOnPrimary
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealSecondary
import com.example.util.FileUtils
import com.example.util.Formatters
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onNavigateToEditor: (Long) -> Unit,
    onNavigateToTeleprompter: (Long) -> Unit,
    onNavigateToCamera: (Long) -> Unit,
    onNavigateToAllScripts: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val recentScripts by viewModel.recentScripts.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val preferencesManager = com.example.PromptDeskApp.instance.preferencesManager
    val userName by preferencesManager.userName.collectAsStateWithLifecycle(initialValue = "Raja Jadav")
    val firstName = userName.trim().split(" ").firstOrNull()?.ifBlank { "Raja" } ?: "Raja"

    var showPermissionDialog by remember { mutableStateOf(false) }
    var pendingScriptIdForOverlay by remember { mutableStateOf<Long?>(null) }
    var isOverlayActive by remember { mutableStateOf(false) }

    val hasOverlay = OverlayManager.hasOverlayPermission(context)

    val startFloatingOverlay: (Long) -> Unit = { scriptId ->
        if (OverlayManager.hasOverlayPermission(context)) {
            OverlayManager.startFloatingPrompter(context, scriptId)
            isOverlayActive = true
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Floating teleprompter started! Open your Camera app.")
            }
        } else {
            pendingScriptIdForOverlay = scriptId
            showPermissionDialog = true
        }
    }

    // Auto-check on resume when returning from Android Overlay Settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (showPermissionDialog && OverlayManager.hasOverlayPermission(context)) {
                    showPermissionDialog = false
                    val id = pendingScriptIdForOverlay ?: recentScripts.firstOrNull()?.id ?: 0L
                    OverlayManager.startFloatingPrompter(context, id)
                    isOverlayActive = true
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Overlay permission granted! Teleprompter is active.")
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    // Active script for "Continue Rehearsal" block
    val activeScript = recentScripts.firstOrNull()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = ObsidianBase,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Studio Header: PromptDesk logo + Title + Subtitle + HUD Pill & Avatar
            item {
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
                            color = ObsidianSurfaceHigh,
                            border = BorderStroke(1.dp, ObsidianBorder),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = null,
                                    tint = TealPrimary,
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
                                    color = ObsidianOnSurface
                                )
                            )
                            Text(
                                text = "Home",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 12.sp,
                                    color = ObsidianOnSurfaceVariant
                                )
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // HUD ON Badge
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = ObsidianSurfaceHigh,
                            border = BorderStroke(1.dp, ObsidianBorder),
                            modifier = Modifier.clickable {
                                val targetId = activeScript?.id ?: 0L
                                startFloatingOverlay(targetId)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                PulsingDot(color = TealPrimary)
                                Text(
                                    text = if (isOverlayActive) "HUD ACTIVE" else "HUD ON",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp,
                                        color = TealPrimary
                                    )
                                )
                            }
                        }

                        // User profile / studio avatar button
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF4285F4),
                            modifier = Modifier
                                .size(34.dp)
                                .clickable { onNavigateToAllScripts() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = firstName.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            // 2. Personalized Creator Greeting & Tagline Banner
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = ObsidianSurfaceLow,
                    border = BorderStroke(1.dp, ObsidianBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ready to record, $firstName? 👋",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = ObsidianOnSurface
                                )
                            )

                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = TealPrimary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "STUDIO READY",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.5.sp,
                                        color = TealPrimary
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Use your script while recording with the camera you already have.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = ObsidianOnSurfaceVariant,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            // 3. TOP USP HERO CARD (Floating Prompter Master Callout)
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    border = BorderStroke(1.dp, ObsidianBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hero_floating_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Status badges row inside hero card
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = ObsidianSurfaceHighest,
                                border = BorderStroke(1.dp, ObsidianBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    PulsingDot(color = TealPrimary)
                                    Text(
                                        text = "FLOATING TELEPROMPTER",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp,
                                            color = TealPrimary
                                        )
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = ObsidianSurfaceHigh
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (hasOverlay) TealPrimary else Color(0xFFF5C451),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = if (hasOverlay) "Permission: Granted" else "Permission Required",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            color = ObsidianOnSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }

                        // Headline & subhead
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Record in 4K with natural eye contact.",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 28.sp,
                                    color = ObsidianOnSurface
                                )
                            )
                            Text(
                                text = "Read smoothly while recording across your native 4K Camera, Instagram, TikTok, or YouTube apps.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp,
                                    color = ObsidianOnSurfaceVariant
                                )
                            )
                        }

                        // Mini Prompter HUD Preview Card
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = ObsidianSurfaceLowest,
                            border = BorderStroke(1.dp, ObsidianBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = ObsidianSurfaceHigh,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Layers,
                                                contentDescription = null,
                                                tint = TealPrimary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = "Compact Overlay Window",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = ObsidianOnSurface
                                            )
                                        )
                                        Text(
                                            text = "Eye-line lock · 65% Opacity",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                color = ObsidianOnSurfaceVariant
                                            )
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = ObsidianSurfaceHigh
                                ) {
                                    Text(
                                        text = "145 WPM",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = TealPrimary
                                        ),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // Master Hero Button: Launch Floating Overlay
                        Button(
                            onClick = {
                                val targetId = activeScript?.id ?: 0L
                                startFloatingOverlay(targetId)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("home_floating_prompter_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TealPrimary,
                                contentColor = TealOnPrimary
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PictureInPictureAlt,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Launch Floating Overlay",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = ObsidianSurfaceLowest.copy(alpha = 0.25f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(TealOnPrimary, CircleShape)
                                        )
                                        Text(
                                            text = if (isOverlayActive) "ACTIVE" else "READY",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 10.sp,
                                                letterSpacing = 0.5.sp,
                                                color = TealOnPrimary
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Quick Stats Row (3 Rounded Surface Cards)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val scriptCount = recentScripts.size.coerceAtLeast(1)
                    QuickStatCard(
                        icon = Icons.Default.LibraryBooks,
                        value = "$scriptCount",
                        label = "Scripts",
                        modifier = Modifier.weight(1f)
                    )
                    QuickStatCard(
                        icon = Icons.Default.Speed,
                        value = "140",
                        label = "Avg WPM",
                        modifier = Modifier.weight(1f)
                    )
                    QuickStatCard(
                        icon = Icons.Default.History,
                        value = "2h 15m",
                        label = "Recorded",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 5. Continue Rehearsal (Active Script Focus Block)
            activeScript?.let { script ->
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayCircle,
                                    contentDescription = null,
                                    tint = TealPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Continue Rehearsal",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = ObsidianOnSurface
                                    )
                                )
                            }
                            Text(
                                text = "ACTIVE SCRIPT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp,
                                    color = TealPrimary
                                )
                            )
                        }

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceHigh),
                            border = BorderStroke(1.dp, ObsidianBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "UP NEXT",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TealPrimary,
                                                letterSpacing = 0.5.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = script.title,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = ObsidianOnSurface
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = ObsidianSurface,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.SmartDisplay,
                                                contentDescription = null,
                                                tint = ObsidianOnSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                // Metadata row
                                val estTime = Formatters.formatEstimatedReadingTime(script.wordCount, script.scrollSpeed)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = ObsidianSurface
                                    ) {
                                        Text(
                                            text = estTime,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                color = ObsidianOnSurfaceVariant
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text("•", color = ObsidianBorder, fontSize = 10.sp)
                                    Text(
                                        text = "${script.wordCount} words",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            color = ObsidianOnSurfaceVariant
                                        )
                                    )
                                    Text("•", color = ObsidianBorder, fontSize = 10.sp)
                                    Text(
                                        text = "Speed: ${(script.scrollSpeed * 115).toInt()} WPM",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            color = ObsidianOnSurfaceVariant
                                        )
                                    )
                                }

                                // Action Buttons Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { onNavigateToTeleprompter(script.id) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = TealPrimary,
                                            contentColor = TealOnPrimary
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Start Prompter",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }

                                    Button(
                                        onClick = { onNavigateToEditor(script.id) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ObsidianSurface,
                                            contentColor = ObsidianOnSurface
                                        ),
                                        border = BorderStroke(1.dp, ObsidianBorder)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EditNote,
                                            contentDescription = null,
                                            tint = ObsidianOnSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Edit Script",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. Recent Scripts Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Scripts",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ObsidianOnSurface
                        )
                    )

                    if (recentScripts.isNotEmpty()) {
                        Text(
                            text = "View All (${recentScripts.size})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            ),
                            modifier = Modifier
                                .clickable { onNavigateToAllScripts() }
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }

            if (recentScripts.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = ObsidianSurfaceLow,
                        border = BorderStroke(1.dp, ObsidianBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LibraryBooks,
                                contentDescription = null,
                                tint = ObsidianOnSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Your scripts will appear here",
                                style = MaterialTheme.typography.titleSmall.copy(color = ObsidianOnSurface)
                            )
                            Button(
                                onClick = { onNavigateToEditor(0L) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TealPrimary,
                                    contentColor = TealOnPrimary
                                )
                            ) {
                                Text("Create your first script", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(recentScripts.take(3), key = { it.id }) { script ->
                    ObsidianRecentScriptCard(
                        script = script,
                        onClick = { onNavigateToEditor(script.id) },
                        onPlayClick = { onNavigateToTeleprompter(script.id) },
                        onFloatingClick = { startFloatingOverlay(script.id) },
                        onEditClick = { onNavigateToEditor(script.id) },
                        onDuplicateClick = { viewModel.duplicateScript(script.id) },
                        onShareClick = { FileUtils.shareText(context, script.title, script.content) },
                        onDeleteClick = { viewModel.deleteScript(script.id) }
                    )
                }
            }

            // 7. Studio Tools (Bottom Instant Creation Dock)
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ObsidianSurfaceLow,
                    border = BorderStroke(1.dp, ObsidianBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = TealPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Studio Tools",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ObsidianOnSurface
                                    )
                                )
                            }
                            Text(
                                text = "Instant Creation",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ObsidianOnSurfaceVariant
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onNavigateToEditor(0L) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ObsidianSurfaceHigh,
                                    contentColor = ObsidianOnSurface
                                ),
                                border = BorderStroke(1.dp, ObsidianBorder)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircle,
                                    contentDescription = null,
                                    tint = TealPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "New Script",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }

                            Button(
                                onClick = {
                                    // Open editor directly in AI assisted prompt state
                                    onNavigateToEditor(0L)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ObsidianSurfaceHigh,
                                    contentColor = ObsidianOnSurface
                                ),
                                border = BorderStroke(1.dp, ObsidianBorder)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = PeachTertiaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "AI Script Writer",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Professional Overlay Permission Dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Layers, contentDescription = null, tint = TealPrimary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Display Over Other Apps", color = ObsidianOnSurface)
                }
            },
            text = {
                Text(
                    text = "PromptDesk needs permission to display your script over other apps. This allows the teleprompter to remain floating above your native 4K Camera, TikTok, or Instagram apps.",
                    color = ObsidianOnSurfaceVariant,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDialog = false
                        OverlayManager.requestOverlayPermission(context)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TealPrimary,
                        contentColor = TealOnPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Allow Overlay", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Cancel", color = ObsidianOnSurfaceVariant)
                }
            },
            containerColor = ObsidianSurfaceHigh,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun QuickStatCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = ObsidianSurface,
        border = BorderStroke(1.dp, ObsidianBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TealPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = ObsidianOnSurface
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    color = ObsidianOnSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun ObsidianRecentScriptCard(
    script: ScriptEntity,
    onClick: () -> Unit,
    onPlayClick: () -> Unit,
    onFloatingClick: () -> Unit,
    onEditClick: () -> Unit,
    onDuplicateClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    // Dynamic icon based on title / category
    val icon = when {
        script.title.contains("TikTok", ignoreCase = true) || script.title.contains("Reel", ignoreCase = true) -> Icons.Default.Bolt
        script.title.contains("Keynote", ignoreCase = true) || script.title.contains("Pitch", ignoreCase = true) -> Icons.Default.TrendingUp
        else -> Icons.Default.TrendingUp
    }
    val iconColor = when {
        script.title.contains("TikTok", ignoreCase = true) -> PeachTertiaryContainer
        script.title.contains("Keynote", ignoreCase = true) -> TealSecondary
        else -> TealPrimary
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ObsidianSurface,
        border = BorderStroke(1.dp, ObsidianBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ObsidianSurfaceHighest,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = script.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ObsidianOnSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val duration = Formatters.formatEstimatedReadingTime(script.wordCount, script.scrollSpeed)
                    Text(
                        text = "${Formatters.formatRelativeTime(script.updatedAt)} · ${script.wordCount} words · $duration",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            color = ObsidianOnSurfaceVariant
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Quick Play button
                Surface(
                    shape = CircleShape,
                    color = ObsidianSurfaceHigh,
                    modifier = Modifier
                        .size(36.dp)
                        .clickable(onClick = onPlayClick)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = TealPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = ObsidianOnSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Launch Floating HUD") },
                            leadingIcon = { Icon(Icons.Default.Layers, contentDescription = null, tint = TealPrimary) },
                            onClick = {
                                showMenu = false
                                onFloatingClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Script") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onEditClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate") },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onDuplicateClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share") },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onShareClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PulsingDot(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    Box(
        modifier = Modifier
            .size(7.dp)
            .alpha(alpha)
            .background(color, CircleShape)
    )
}
