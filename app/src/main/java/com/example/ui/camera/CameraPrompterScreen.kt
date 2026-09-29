package com.example.ui.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.BatteryFull
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.home.PulsingDot
import com.example.ui.theme.ObsidianBase
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianOnSurface
import com.example.ui.theme.ObsidianOnSurfaceVariant
import com.example.ui.theme.ObsidianOutlineVariant
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceHigh
import com.example.ui.theme.ObsidianSurfaceHighest
import com.example.ui.theme.ObsidianSurfaceLow
import com.example.ui.theme.ObsidianSurfaceLowest
import com.example.ui.theme.RecordRed
import com.example.ui.theme.TealOnPrimary
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealSecondary
import com.example.util.Formatters
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun CameraPrompterScreen(
    scriptId: Long,
    onNavigateBack: () -> Unit,
    viewModel: CameraPrompterViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val script by viewModel.script.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val isPrompterScrolling by viewModel.isPrompterScrolling.collectAsStateWithLifecycle()
    val scrollSpeed by viewModel.scrollSpeed.collectAsStateWithLifecycle()
    val fontSize by viewModel.fontSize.collectAsStateWithLifecycle()
    val lensFacing by viewModel.lensFacing.collectAsStateWithLifecycle()
    val recordSeconds by viewModel.recordSeconds.collectAsStateWithLifecycle()

    var showTipCard by remember { mutableStateOf(true) }
    var isWidgetLocked by remember { mutableStateOf(false) }
    var isWidgetMinimized by remember { mutableStateOf(false) }
    var isMirrored by remember { mutableStateOf(false) }
    var opacityPercent by remember { mutableIntStateOf(85) }
    var selectedZoom by remember { mutableStateOf("1x") }

    // Floating drag offset
    var offsetY by remember { mutableFloatStateOf(0f) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] == true
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }
    }

    LaunchedEffect(scriptId) {
        viewModel.loadScript(scriptId)
    }

    val scrollState = rememberScrollState()

    // Continuous auto-scrolling
    LaunchedEffect(isPrompterScrolling, scrollSpeed) {
        while (isPrompterScrolling) {
            val step = (scrollSpeed * 1.5f).toInt().coerceAtLeast(1)
            scrollState.scrollBy(step.toFloat())
            delay(16L)
        }
    }

    BackHandler {
        if (isRecording) {
            viewModel.stopRecording()
        }
        onNavigateBack()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1. Camera Viewfinder Layer
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val cameraSelector = CameraSelector.Builder()
                                .requireLensFacing(lensFacing)
                                .build()

                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                        } catch (e: Exception) {
                            Log.e("CameraPrompter", "Camera bind failed: ${e.message}")
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize(),
                update = { previewView ->
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(previewView.context)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val cameraSelector = CameraSelector.Builder()
                                .requireLensFacing(lensFacing)
                                .build()

                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                        } catch (e: Exception) {
                            Log.e("CameraPrompter", "Rebind camera error: ${e.message}")
                        }
                    }, ContextCompat.getMainExecutor(previewView.context))
                }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Camera Permission Required",
                    style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "To record videos while reading your teleprompter script, grant camera access.",
                    color = ObsidianOnSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)) },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary, contentColor = TealOnPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Grant Permissions", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Top Vignette gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent)
                    )
                )
        )

        // Bottom Vignette gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .align(Alignment.BottomCenter)
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                    )
                )
        )

        // 2. Android Camera System HUD - Top Status & Toolbars
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            // Top Bar: Time, Punch-hole Camera Vector Marker, WiFi/Battery
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (isRecording) viewModel.stopRecording()
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .size(34.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Physical Camera Punch-hole & Eye-Contact Anchor Indicator
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = TealPrimary.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, TealPrimary.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        PulsingDot(color = TealPrimary)
                        Text(
                            text = "LENS VECTOR",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                                color = TealPrimary
                            )
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Wifi, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Icon(Icons.Outlined.BatteryFull, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Native Camera Top Controls: Flash, HDR 16:9 4K 60, Settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.FlashOff, contentDescription = "Flash", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }

                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, ObsidianBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("HDR", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Box(modifier = Modifier.size(3.dp).background(ObsidianOutlineVariant, CircleShape))
                        Text("16:9", color = TealPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("4K 60", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Box(modifier = Modifier.size(3.dp).background(ObsidianOutlineVariant, CircleShape))
                        Icon(Icons.Default.Timer, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // 3. FLOATING PROMPTDESK STUDIO GLASS WIDGET
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(0, offsetY.roundToInt()) }
                .statusBarsPadding()
                .padding(top = 100.dp, start = 14.dp, end = 14.dp)
                .align(Alignment.TopCenter)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF171B21).copy(alpha = opacityPercent / 100f),
                border = BorderStroke(1.dp, TealPrimary.copy(alpha = 0.25f)),
                shadowElevation = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header Strip: Audio Voice Tracking, Drag Grip, Window Controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(isWidgetLocked) {
                                if (!isWidgetLocked) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        offsetY = (offsetY + dragAmount.y).coerceIn(-40f, 320f)
                                    }
                                }
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Voice Sync status
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = ObsidianSurfaceLow
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                EqualizerBars()
                                Text(
                                    text = "VOICE SYNC",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp,
                                        color = TealPrimary
                                    )
                                )
                            }
                        }

                        // Center: Tactile Drag Grip (4 dots)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(ObsidianSurfaceHighest.copy(alpha = 0.5f), RoundedCornerShape(100.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            repeat(4) {
                                Box(
                                    modifier = Modifier
                                        .size(3.5.dp)
                                        .background(ObsidianOnSurfaceVariant, CircleShape)
                                )
                            }
                        }

                        // Right: Lock, Minimize, Close
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { isWidgetLocked = !isWidgetLocked },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (isWidgetLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = "Lock",
                                    tint = if (isWidgetLocked) TealPrimary else ObsidianOnSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            IconButton(
                                onClick = { isWidgetMinimized = !isWidgetMinimized },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (isWidgetMinimized) Icons.Default.UnfoldMore else Icons.Default.UnfoldLess,
                                    contentDescription = "Minimize",
                                    tint = ObsidianOnSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

                    if (!isWidgetMinimized) {
                        // Teleprompter Reading Viewport
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = ObsidianSurfaceLowest.copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, ObsidianBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                // Eye-Contact Guideline Hairline
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .align(Alignment.Center)
                                        .background(
                                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                                colors = listOf(Color.Transparent, TealPrimary.copy(alpha = 0.5f), Color.Transparent)
                                            )
                                        )
                                )

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(scrollState)
                                        .graphicsLayer {
                                            if (isMirrored) scaleX = -1f
                                        }
                                        .padding(horizontal = 14.dp, vertical = 20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = script?.content ?: "Shoot natively in Camera without breaking eye contact. The floating engine adapts to your pacing, keeping your script locked right beneath the camera sensor.",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = fontSize.sp,
                                            lineHeight = (fontSize * 1.35f).sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White,
                                            textAlign = TextAlign.Center
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        // Floating HUD Control Strip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // WPM Velocity Stepper
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ObsidianSurfaceLow,
                                border = BorderStroke(1.dp, ObsidianBorder)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.setScrollSpeed((scrollSpeed - 0.2f).coerceAtLeast(0.4f)) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "-", tint = Color.White, modifier = Modifier.size(13.dp))
                                    }
                                    Text(
                                        text = "${(scrollSpeed * 115).toInt()} WPM",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color.White
                                        ),
                                        modifier = Modifier.width(54.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    IconButton(
                                        onClick = { viewModel.setScrollSpeed((scrollSpeed + 0.2f).coerceAtMost(3.0f)) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "+", tint = Color.White, modifier = Modifier.size(13.dp))
                                    }
                                }
                            }

                            // Primary Play / Pause Button Anchor
                            Surface(
                                shape = CircleShape,
                                color = TealPrimary,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clickable { viewModel.togglePrompterScrolling() },
                                shadowElevation = 4.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isPrompterScrolling) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play/Pause",
                                        tint = TealOnPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // Quick Toggles: Font, Opacity, Mirror
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ObsidianSurfaceLow,
                                border = BorderStroke(1.dp, ObsidianBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.setFontSize(if (fontSize >= 44f) 28f else fontSize + 4f) },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(Icons.Default.FormatSize, contentDescription = "Font", tint = ObsidianOnSurfaceVariant, modifier = Modifier.size(15.dp))
                                    }
                                    Text(
                                        text = "$opacityPercent%",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TealPrimary
                                        ),
                                        modifier = Modifier
                                            .clickable {
                                                opacityPercent = if (opacityPercent <= 50) 95 else opacityPercent - 15
                                            }
                                            .padding(horizontal = 4.dp)
                                    )
                                    IconButton(
                                        onClick = { isMirrored = !isMirrored },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(Icons.Default.Flip, contentDescription = "Mirror", tint = if (isMirrored) TealPrimary else ObsidianOnSurfaceVariant, modifier = Modifier.size(15.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tactical Micro-Banner: Eye-Contact Value Prop
            if (showTipCard && !isWidgetMinimized) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ObsidianSurfaceHigh.copy(alpha = 0.95f),
                    border = BorderStroke(1.dp, ObsidianBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = TealPrimary.copy(alpha = 0.2f),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Visibility, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(14.dp))
                                }
                            }
                            Text(
                                text = "Pro tip: Keep widget under 20mm from lens for authentic eye-line.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = ObsidianOnSurfaceVariant
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = "Got it",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            ),
                            modifier = Modifier
                                .clickable { showTipCard = false }
                                .padding(start = 6.dp, end = 2.dp)
                        )
                    }
                }
            }
        }

        // 4. Android Camera Lower HUD: REC pill, Zoom selector, Shutter deck, Mode carousel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Live Recording Time Pill
            Surface(
                shape = RoundedCornerShape(100.dp),
                color = ObsidianSurfaceLowest.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, ObsidianBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(if (isRecording) RecordRed else ObsidianOnSurfaceVariant, CircleShape)
                    )
                    Text(
                        text = "REC",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            color = if (isRecording) RecordRed else ObsidianOnSurfaceVariant
                        )
                    )
                    Box(modifier = Modifier.size(1.dp, 10.dp).background(ObsidianBorder))
                    Text(
                        text = Formatters.formatDuration(recordSeconds),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }

            // Camera Lens Focal Zoom Selector (.6x, 1x, 2x, 5x)
            Surface(
                shape = RoundedCornerShape(100.dp),
                color = ObsidianSurfaceLowest.copy(alpha = 0.75f),
                border = BorderStroke(1.dp, ObsidianBorder)
            ) {
                Row(
                    modifier = Modifier.padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    listOf(".6x", "1x", "2x", "5x").forEach { zoom ->
                        val isSelected = selectedZoom == zoom
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) ObsidianSurfaceHighest else Color.Transparent,
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { selectedZoom = zoom }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = zoom,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) TealPrimary else ObsidianOnSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Shutter Strip & Media Thumbnails
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Floating Overlay shortcut / Gallery cue
                Surface(
                    shape = CircleShape,
                    color = ObsidianSurfaceHigh,
                    border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .size(46.dp)
                        .clickable {
                            if (com.example.overlay.manager.OverlayManager.hasOverlayPermission(context)) {
                                com.example.overlay.manager.OverlayManager.startFloatingPrompter(context, scriptId)
                                com.example.camera.NativeCameraLauncher.launchCamera(context)
                                onNavigateBack()
                            } else {
                                com.example.overlay.manager.OverlayManager.requestOverlayPermission(context)
                            }
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Layers, contentDescription = "Floating Overlay", tint = TealPrimary, modifier = Modifier.size(22.dp))
                    }
                }

                // Center: Big Camera Record Shutter Button
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color.Transparent, CircleShape)
                        .border(3.dp, Color.White.copy(alpha = 0.45f), CircleShape)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = RecordRed,
                        modifier = Modifier
                            .size(56.dp)
                            .clickable {
                                if (isRecording) {
                                    viewModel.stopRecording()
                                } else {
                                    viewModel.startRecording()
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isRecording) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(Color.White, RoundedCornerShape(4.dp))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .background(Color.White.copy(alpha = 0.25f), CircleShape)
                                )
                            }
                        }
                    }
                }

                // Right: Camera Switcher
                Surface(
                    shape = CircleShape,
                    color = ObsidianSurfaceLowest.copy(alpha = 0.8f),
                    border = BorderStroke(1.dp, ObsidianBorder),
                    modifier = Modifier
                        .size(46.dp)
                        .clickable { viewModel.flipCamera() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Flip camera",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Mode Carousel
            Row(
                horizontalArrangement = Arrangement.spacedBy(22.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text("PHOTO", fontSize = 11.sp, color = ObsidianOnSurfaceVariant)
                Text("VIDEO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TealPrimary, letterSpacing = 0.5.sp)
                Text("PRO-CINEMA", fontSize = 11.sp, color = ObsidianOnSurfaceVariant)
                Text("PORTRAIT", fontSize = 11.sp, color = ObsidianOnSurfaceVariant)
            }
        }
    }
}

@Composable
fun EqualizerBars() {
    val infiniteTransition = rememberInfiniteTransition(label = "eq")
    val bar1 by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(400), repeatMode = RepeatMode.Reverse), label = "b1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.4f,
        animationSpec = infiniteRepeatable(tween(550), repeatMode = RepeatMode.Reverse), label = "b2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(350), repeatMode = RepeatMode.Reverse), label = "b3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(11.dp)
    ) {
        Box(modifier = Modifier.width(2.dp).height((10 * bar1).dp).background(TealPrimary, CircleShape))
        Box(modifier = Modifier.width(2.dp).height((11 * bar2).dp).background(TealPrimary, CircleShape))
        Box(modifier = Modifier.width(2.dp).height((9 * bar3).dp).background(TealPrimary, CircleShape))
    }
}
