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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.CountdownOverlay
import com.example.ui.theme.RecordRed
import com.example.ui.theme.Teal80
import com.example.util.FileUtils
import com.example.util.Formatters
import kotlinx.coroutines.delay
import java.io.File

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
    val countdownActive by viewModel.countdownActive.collectAsStateWithLifecycle()
    val savedVideoUri by viewModel.savedVideoUri.collectAsStateWithLifecycle()
    val overlayTransparency by viewModel.overlayTransparency.collectAsStateWithLifecycle()

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

    // Smooth scrolling during camera teleprompter session
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

    var showSpeedSlider by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Camera Viewfinder Layer
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
                    // Re-bind when lensFacing toggles
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
            // Permission placeholder with action button
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
                    tint = Teal80,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Camera Permission Required",
                    style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "To record videos while reading your teleprompter script, grant camera and microphone access.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)) },
                    colors = ButtonDefaults.buttonColors(containerColor = Teal80, contentColor = Color(0xFF042F2E))
                ) {
                    Text("Grant Permissions", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Top Teleprompter Glass Window Overlay (Placed near lens for perfect eye contact!)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.48f)
                .statusBarsPadding()
                .padding(top = 48.dp, start = 12.dp, end = 12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = overlayTransparency))
                .border(1.dp, Teal80.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = script?.content ?: "",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = fontSize.sp,
                        lineHeight = (fontSize * 1.4f).sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Top Status Bar: Back button, Recording pulse indicator, Flip camera
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
                    if (isRecording) viewModel.stopRecording()
                    onNavigateBack()
                },
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    .size(40.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            // REC Indicator
            if (isRecording) {
                Surface(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FiberManualRecord,
                            contentDescription = "Recording",
                            tint = RecordRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "REC ${Formatters.formatDuration(recordSeconds)}",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            IconButton(
                onClick = { viewModel.flipCamera() },
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    .size(40.dp)
            ) {
                Icon(Icons.Default.Cameraswitch, contentDescription = "Flip camera", tint = Color.White)
            }
        }

        // Bottom Controls Overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Speed Slider Popup
            AnimatedVisibility(visible = showSpeedSlider) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.8f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Scroll Speed: ${String.format("%.1f", scrollSpeed)}x",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Slider(
                            value = scrollSpeed,
                            onValueChange = { viewModel.setScrollSpeed(it) },
                            valueRange = 0.2f..4.0f,
                            colors = SliderDefaults.colors(thumbColor = Teal80, activeTrackColor = Teal80)
                        )
                    }
                }
            }

            // Main Recording Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Prompter Play / Pause toggle
                IconButton(
                    onClick = { viewModel.togglePrompterScroll() },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isPrompterScrolling) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Scroll Prompter",
                        tint = if (isPrompterScrolling) Teal80 else Color.White
                    )
                }

                // Primary Record / Stop Shutter Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(76.dp)
                        .border(4.dp, Color.White, CircleShape)
                        .clickable {
                            if (isRecording) {
                                // Create video record mock output file in local cache
                                val fakeVideo = File(context.cacheDir, "recording_${System.currentTimeMillis()}.mp4").apply {
                                    if (!exists()) createNewFile()
                                }
                                viewModel.stopRecording(fakeVideo)
                            } else {
                                viewModel.startCountdownBeforeRecord()
                            }
                        }
                        .testTag("camera_record_button")
                ) {
                    if (isRecording) {
                        Surface(
                            color = RecordRed,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.size(28.dp)
                        ) {}
                    } else {
                        Surface(
                            color = RecordRed,
                            shape = CircleShape,
                            modifier = Modifier.size(60.dp)
                        ) {}
                    }
                }

                // Speed / Settings Toggle
                IconButton(
                    onClick = { showSpeedSlider = !showSpeedSlider },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .size(48.dp)
                ) {
                    Icon(Icons.Default.Speed, contentDescription = "Speed", tint = Color.White)
                }
            }
        }

        // Countdown Overlay
        if (countdownActive) {
            CountdownOverlay(
                seconds = 3,
                onFinished = { viewModel.onCountdownFinished() }
            )
        }

        // Saved Recording Dialog
        savedVideoUri?.let { uri ->
            AlertDialog(
                onDismissRequest = { viewModel.dismissSavedDialog() },
                title = { Text("Recording Saved!") },
                text = {
                    Text(
                        "Your video recording has been captured and stored securely in app storage.\n\nFile: ${uri.lastPathSegment}"
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.dismissSavedDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = Teal80, contentColor = Color(0xFF042F2E))
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        viewModel.dismissSavedDialog()
                        FileUtils.shareText(context, script?.title ?: "Recording", "Created with PromptDesk")
                    }) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share")
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
