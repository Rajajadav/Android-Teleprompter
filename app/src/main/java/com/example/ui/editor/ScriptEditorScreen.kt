package com.example.ui.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.ai.RewriteStyle
import com.example.domain.ai.ScriptFormat
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.Teal80
import com.example.ui.theme.TealDark40
import com.example.util.FileUtils
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScriptEditorScreen(
    scriptId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToTeleprompter: (Long) -> Unit,
    onNavigateToCamera: (Long) -> Unit,
    viewModel: ScriptEditorViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val title by viewModel.title.collectAsStateWithLifecycle()
    val content by viewModel.content.collectAsStateWithLifecycle()
    val wordCount by viewModel.wordCount.collectAsStateWithLifecycle()
    val saveStatus by viewModel.saveStatus.collectAsStateWithLifecycle()
    val alignment by viewModel.alignment.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val uiMessage by viewModel.uiMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showAiSheet by remember { mutableStateOf(false) }
    var aiTopicInput by remember { mutableStateOf("") }
    var selectedFormat by remember { mutableStateOf(ScriptFormat.YOUTUBE_LONG) }

    LaunchedEffect(scriptId) {
        viewModel.loadScript(scriptId)
    }

    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    BackHandler {
        viewModel.saveScriptImmediately()
        onNavigateBack()
    }

    val textAlign = when (alignment) {
        "LEFT" -> TextAlign.Left
        "RIGHT" -> TextAlign.Right
        else -> TextAlign.Center
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = title,
                        onValueChange = { viewModel.onTitleChanged(it) },
                        placeholder = { Text("Script Title", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("editor_title_input")
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.saveScriptImmediately()
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Save Status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        when (saveStatus) {
                            is SaveStatus.Saving -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Teal80
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Saving...",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                            is SaveStatus.Saved -> {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Saved",
                                    style = MaterialTheme.typography.labelSmall.copy(color = SuccessGreen)
                                )
                            }
                            is SaveStatus.Unsaved -> {
                                Text(
                                    text = "Editing",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }
                    }

                    // More Menu
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Share Script") },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                onClick = {
                                    showMoreMenu = false
                                    FileUtils.shareText(context, title, content)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Copy to Clipboard") },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                                onClick = {
                                    showMoreMenu = false
                                    FileUtils.copyToClipboard(context, content, title)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.imePadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Quick Formatting Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.appendFormatting("[PAUSE 2s]", "") }) {
                            Text("⏱", fontSize = 16.sp)
                        }
                        IconButton(onClick = {
                            viewModel.setAlignment(if (alignment == "LEFT") "CENTER" else if (alignment == "CENTER") "RIGHT" else "LEFT")
                        }) {
                            Icon(
                                imageVector = when (alignment) {
                                    "LEFT" -> Icons.Default.FormatAlignLeft
                                    "RIGHT" -> Icons.Default.FormatAlignRight
                                    else -> Icons.Default.FormatAlignCenter
                                },
                                contentDescription = "Alignment",
                                tint = Teal80,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        OutlinedButton(
                            onClick = { showAiSheet = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Teal80, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AI Assist", style = MaterialTheme.typography.labelSmall.copy(color = Teal80))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Word Count & Primary Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "$wordCount words",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "Est. ${Formatters.formatEstimatedReadingTime(wordCount)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Floating Overlay Prompter Button
                            Button(
                                onClick = {
                                    viewModel.saveScriptImmediately()
                                    val id = viewModel.getScriptId()
                                    if (com.example.overlay.manager.OverlayManager.hasOverlayPermission(context)) {
                                        com.example.overlay.manager.OverlayManager.startFloatingPrompter(context, id)
                                    } else {
                                        com.example.overlay.manager.OverlayManager.requestOverlayPermission(context)
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Teal80,
                                    contentColor = Color(0xFF003730)
                                ),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("floating_prompter_button")
                            ) {
                                Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Launch HUD", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            // Fullscreen Teleprompter Button
                            Button(
                                onClick = {
                                    viewModel.saveScriptImmediately()
                                    val id = viewModel.getScriptId()
                                    onNavigateToTeleprompter(id)
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = com.example.ui.theme.ObsidianSurface,
                                    contentColor = com.example.ui.theme.ObsidianOnSurface
                                ),
                                border = BorderStroke(1.dp, com.example.ui.theme.ObsidianBorder),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("start_teleprompter_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = com.example.ui.theme.ObsidianOnSurface, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Fullscreen", color = com.example.ui.theme.ObsidianOnSurface, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TextField(
                value = content,
                onValueChange = { viewModel.onContentChanged(it) },
                placeholder = {
                    Text(
                        text = "Paste or type your script here...\n\nTap 'AI Assist' below for instant script templates!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontSize = 20.sp,
                        textAlign = textAlign
                    )
                },
                textStyle = TextStyle(
                    fontSize = 22.sp,
                    lineHeight = 32.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = textAlign
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .testTag("editor_content_input")
            )
        }
    }

    // AI Creator Assistant Bottom Sheet
    if (showAiSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAiSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI Script Assistant",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    if (isAiLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Teal80)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Generate Script from Topic",
                    style = MaterialTheme.typography.labelLarge.copy(color = Teal80, fontWeight = FontWeight.SemiBold)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = aiTopicInput,
                    onValueChange = { aiTopicInput = it },
                    placeholder = { Text("e.g. 5 Productivity Habits for Creators") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.generateAiScript(aiTopicInput, selectedFormat)
                        showAiSheet = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal80, contentColor = Color(0xFF042F2E)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Draft", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Quick Edits",
                    style = MaterialTheme.typography.labelLarge.copy(color = Teal80, fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.rewriteWithAi(RewriteStyle.CONVERSATIONAL)
                            showAiSheet = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Conversational", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = {
                            viewModel.rewriteWithAi(RewriteStyle.ENERGETIC)
                            showAiSheet = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Punchy", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = {
                            viewModel.shortenWithAi()
                            showAiSheet = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Shorten", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
