package com.example.ui.scripts

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.ScriptEntity
import com.example.overlay.manager.OverlayManager
import com.example.ui.home.PulsingDot
import com.example.ui.theme.ObsidianBase
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianOnSurface
import com.example.ui.theme.ObsidianOnSurfaceVariant
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceHigh
import com.example.ui.theme.ObsidianSurfaceHighest
import com.example.ui.theme.ObsidianSurfaceLow
import com.example.ui.theme.ObsidianSurfaceLowest
import com.example.ui.theme.PeachTertiary
import com.example.ui.theme.PeachTertiaryContainer
import com.example.ui.theme.TealOnPrimary
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealSecondary
import com.example.util.FileUtils
import com.example.util.Formatters
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScriptsScreen(
    onNavigateToEditor: (Long) -> Unit,
    onNavigateToTeleprompter: (Long) -> Unit,
    onNavigateToCamera: (Long) -> Unit,
    viewModel: ScriptsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scripts by viewModel.scripts.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val filterFavoritesOnly by viewModel.filterFavoritesOnly.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedCategory by remember { mutableStateOf("All") }
    var showSortMenu by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val importPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importScriptFromUri(it) }
    }

    // Filter by category in UI
    val filteredScripts = scripts.filter { script ->
        when (selectedCategory) {
            "YouTube" -> script.title.contains("YouTube", ignoreCase = true) || script.content.contains("YouTube", ignoreCase = true)
            "TikTok / Reels" -> script.title.contains("TikTok", ignoreCase = true) || script.title.contains("Reel", ignoreCase = true)
            "Keynotes" -> script.title.contains("Keynote", ignoreCase = true) || script.title.contains("Pitch", ignoreCase = true)
            "Podcasts" -> script.title.contains("Podcast", ignoreCase = true) || script.content.contains("Podcast", ignoreCase = true)
            "Favorites" -> script.favorite
            else -> true
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = ObsidianBase,
        floatingActionButton = {
            // Floating Sticky "+ New Script" Pill FAB from Stitch
            Button(
                onClick = { onNavigateToEditor(0L) },
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TealPrimary,
                    contentColor = TealOnPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .padding(bottom = 60.dp, end = 4.dp)
                    .height(52.dp)
                    .testTag("fab_new_script")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(22.dp))
                    Text(
                        text = "New Script",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Studio Header (PromptDesk + Scripts + HUD ON badge)
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
                                    imageVector = Icons.Default.Description,
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
                                text = "Scripts",
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
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = ObsidianSurfaceHigh,
                            border = BorderStroke(1.dp, ObsidianBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                PulsingDot(color = TealPrimary)
                                Text(
                                    text = "HUD ON",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp,
                                        color = TealPrimary
                                    )
                                )
                            }
                        }

                        IconButton(
                            onClick = { importPicker.launch(arrayOf("text/plain")) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = "Import TXT",
                                tint = TealPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 2. Search Field
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = {
                        Text(
                            text = "Search scripts, tags, or topics...",
                            color = ObsidianOnSurfaceVariant,
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = ObsidianOnSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = ObsidianOnSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ObsidianSurfaceLow,
                        unfocusedContainerColor = ObsidianSurfaceLow,
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = ObsidianOnSurface,
                        unfocusedTextColor = ObsidianOnSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("script_search_input")
                )
            }

            // 3. Scrollable Filter Chips
            item {
                val categories = listOf("All", "YouTube", "TikTok / Reels", "Keynotes", "Podcasts", "Favorites")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = if (isSelected) TealPrimary else ObsidianSurface,
                            border = BorderStroke(1.dp, if (isSelected) TealPrimary else ObsidianBorder),
                            modifier = Modifier.clickable {
                                selectedCategory = cat
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (cat == "All") "All Scripts (${scripts.size})" else cat,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = if (isSelected) TealOnPrimary else ObsidianOnSurfaceVariant
                                    )
                                )
                                if (cat == "Favorites") {
                                    Text("★", color = if (isSelected) TealOnPrimary else PeachTertiary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // 4. AI Script Polisher & Hooks Banner
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ObsidianSurfaceLow,
                    border = BorderStroke(1.dp, ObsidianBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = TealPrimary.copy(alpha = 0.15f),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = TealPrimary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "AI Script Polisher & Hooks",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ObsidianOnSurface
                                    )
                                )
                            }
                            Text(
                                text = "Convert quick bullet notes into polished, high-cadence spoken drafts in seconds.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = ObsidianOnSurfaceVariant,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            )
                            Button(
                                onClick = { onNavigateToEditor(0L) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TealPrimary,
                                    contentColor = TealOnPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Try AI Writer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // GPT-PRO Card
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = ObsidianSurfaceHigh,
                            border = BorderStroke(1.dp, ObsidianBorder),
                            modifier = Modifier.size(62.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = TealPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "GPT-PRO",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp,
                                        color = ObsidianOnSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 5. Section Header: TELEPROMPTER QUEUE + Sort Menu
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "TELEPROMPTER QUEUE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = ObsidianOnSurface
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = ObsidianSurfaceHigh
                        ) {
                            Text(
                                text = "${filteredScripts.size} active",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ObsidianOnSurfaceVariant
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clickable { showSortMenu = true }
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Sort",
                                tint = TealPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Recent",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TealPrimary
                                )
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Recently Updated") },
                                onClick = {
                                    viewModel.setSortOption(SortOption.UPDATED_DESC)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Title A-Z") },
                                onClick = {
                                    viewModel.setSortOption(SortOption.TITLE_ASC)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Word Count (High to Low)") },
                                onClick = {
                                    viewModel.setSortOption(SortOption.WORD_COUNT_DESC)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // 6. Scripts List
            if (filteredScripts.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = ObsidianSurfaceLow,
                        border = BorderStroke(1.dp, ObsidianBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = ObsidianOnSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "No scripts found",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ObsidianOnSurface
                                )
                            )
                            Text(
                                text = "Try searching for a different keyword or reset your filter.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = ObsidianOnSurfaceVariant
                                )
                            )
                            Button(
                                onClick = {
                                    selectedCategory = "All"
                                    viewModel.onSearchQueryChanged("")
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ObsidianSurfaceHigh,
                                    contentColor = TealPrimary
                                ),
                                border = BorderStroke(1.dp, ObsidianBorder)
                            ) {
                                Text("Clear All Filters", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(filteredScripts, key = { it.id }) { script ->
                    ObsidianScriptQueueCard(
                        script = script,
                        onClick = { onNavigateToEditor(script.id) },
                        onEditClick = { onNavigateToEditor(script.id) },
                        onCloneClick = { viewModel.duplicateScript(script.id) },
                        onLaunchHudClick = {
                            if (OverlayManager.hasOverlayPermission(context)) {
                                OverlayManager.startFloatingPrompter(context, script.id)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Floating HUD launched! Open your Camera app.")
                                }
                            } else {
                                OverlayManager.requestOverlayPermission(context)
                            }
                        },
                        onFavoriteToggle = { viewModel.toggleFavorite(script) },
                        onShareClick = { FileUtils.shareText(context, script.title, script.content) },
                        onDeleteClick = { viewModel.deleteScript(script.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ObsidianScriptQueueCard(
    script: ScriptEntity,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onCloneClick: () -> Unit,
    onLaunchHudClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    // Category detection for tags from Stitch
    val (categoryTag, tagColor, secondaryTag) = when {
        script.title.contains("TikTok", ignoreCase = true) || script.title.contains("Reel", ignoreCase = true) ->
            Triple("TikTok", TealSecondary, "⚡ Fast Pacing")
        script.title.contains("Keynote", ignoreCase = true) || script.title.contains("Pitch", ignoreCase = true) ->
            Triple("Keynote", PeachTertiaryContainer, "🎤 Stage Mode")
        script.title.contains("Podcast", ignoreCase = true) ->
            Triple("Podcasts", TealPrimary, "🎙 Warm Vocal")
        else ->
            Triple("YouTube", TealPrimary, if (script.favorite) "★ Starred" else "Standard")
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = ObsidianSurfaceLow,
        border = BorderStroke(1.dp, ObsidianBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Tag row + Star + More menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = tagColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = categoryTag,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = tagColor
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }

                    Text(
                        text = secondaryTag,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            color = ObsidianOnSurfaceVariant
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onFavoriteToggle,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (script.favorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Star",
                            tint = if (script.favorite) PeachTertiary else ObsidianOnSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = ObsidianOnSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Share Script") },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onShareClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Duplicate") },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onCloneClick()
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

            // Headline Title
            Text(
                text = script.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = ObsidianOnSurface
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Preview excerpt
            val previewText = script.content.trim().replace("\n", " ")
            if (previewText.isNotEmpty()) {
                Text(
                    text = "“$previewText”",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ObsidianOnSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Details and progress meter
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val estTime = Formatters.formatEstimatedReadingTime(script.wordCount, script.scrollSpeed)
                val wpm = (script.scrollSpeed * 115).toInt()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${script.wordCount} words",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                color = ObsidianOnSurfaceVariant
                            )
                        )
                        Text("•", color = ObsidianBorder, fontSize = 10.sp)
                        Text(
                            text = "~$estTime",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                        )
                        Text("•", color = ObsidianBorder, fontSize = 10.sp)
                        Text(
                            text = "$wpm WPM",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                color = ObsidianOnSurfaceVariant
                            )
                        )
                    }

                    Text(
                        text = "Edited ${Formatters.formatRelativeTime(script.updatedAt)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = ObsidianOnSurfaceVariant.copy(alpha = 0.7f)
                        )
                    )
                }

                // Progress line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(ObsidianSurfaceHigh, RoundedCornerShape(100.dp))
                ) {
                    val progress = if (script.lastPosition > 0) 0.65f else 0.25f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(3.dp)
                            .background(TealPrimary, RoundedCornerShape(100.dp))
                    )
                }
            }

            // Action Footer (Edit, Clone, Launch HUD)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ObsidianSurface,
                        border = BorderStroke(1.dp, ObsidianBorder),
                        modifier = Modifier.clickable(onClick = onEditClick)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = null,
                                tint = ObsidianOnSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Edit",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = ObsidianOnSurface
                                )
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ObsidianSurface,
                        border = BorderStroke(1.dp, ObsidianBorder),
                        modifier = Modifier.clickable(onClick = onCloneClick)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = ObsidianOnSurface,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Clone",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = ObsidianOnSurface
                                )
                            )
                        }
                    }
                }

                Button(
                    onClick = onLaunchHudClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TealPrimary,
                        contentColor = TealOnPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 7.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Launch HUD",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}
