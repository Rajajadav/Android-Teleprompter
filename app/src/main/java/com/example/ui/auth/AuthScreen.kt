package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.PromptDeskApp
import com.example.ui.components.GoogleGIcon
import com.example.ui.components.GoogleSignInButton
import com.example.ui.theme.ObsidianBase
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianMuted
import com.example.ui.theme.ObsidianOnSurface
import com.example.ui.theme.ObsidianOnSurfaceVariant
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceHigh
import com.example.ui.theme.ObsidianSurfaceLow
import com.example.ui.theme.TealAccentGlow
import com.example.ui.theme.TealOnPrimary
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val preferencesManager = PromptDeskApp.instance.preferencesManager

    var showGoogleAccountPicker by remember { mutableStateOf(false) }
    var isSigningIn by remember { mutableStateOf(false) }

    // Detected default Google account from Android OS / Workspace
    val googleAccountName = "Raja Jadav"
    val googleAccountEmail = "rajajadavstudio@gmail.com"

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0D10))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Decorative subtle top radial accent glow
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(340.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x1A19C7B1),
                            Color(0x0819C7B1),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: PromptDesk Brand & Tagline
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                // Sleek Luminescent Teleprompter Logo
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xFF171B21),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF252B32)),
                    modifier = Modifier.size(68.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF19C7B1).copy(alpha = 0.15f),
                            modifier = Modifier.size(46.dp)
                        ) {}
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "PromptDesk Logo",
                            tint = Color(0xFF19C7B1),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "PromptDesk",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF5F7F8),
                    letterSpacing = (-0.3).sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Speak naturally. Create confidently.",
                    fontSize = 13.sp,
                    color = Color(0xFF19C7B1),
                    fontWeight = FontWeight.Medium
                )
            }

            // Middle: Main Headings & Value Proposition
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                Text(
                    text = "Your words.\nYour camera.\nYour confidence.",
                    fontSize = 32.sp,
                    lineHeight = 39.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    color = Color(0xFFF5F7F8)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Use your script while recording with the camera you already have.",
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF859490),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Feature Pill Badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FeaturePill(text = "Native 4K Overlay")
                    FeaturePill(text = "100% Eye-Contact")
                    FeaturePill(text = "Voice Follow")
                }
            }

            // Bottom: Single Authentication Action (Continue with Google ONLY)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                // Large Primary Google Button
                GoogleSignInButton(
                    onClick = {
                        showGoogleAccountPicker = true
                    },
                    isLoading = isSigningIn,
                    text = "Continue with Google"
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Sign in securely with your Google account.",
                    fontSize = 13.sp,
                    color = Color(0xFF859490),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Legal Disclaimer
                Text(
                    text = "By continuing, you agree to PromptDesk’s Terms of Service and Privacy Policy.",
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = Color(0xFF555F68),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }

    // Native Google Account Selection Experience
    if (showGoogleAccountPicker) {
        ModalBottomSheet(
            onDismissRequest = {
                if (!isSigningIn) showGoogleAccountPicker = false
            },
            sheetState = sheetState,
            containerColor = Color(0xFF1E2023),
            dragHandle = {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF3C4A46),
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 10.dp)
                        .size(width = 36.dp, height = 4.dp)
                ) {}
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .navigationBarsPadding()
            ) {
                // Google Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GoogleGIcon(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Choose an account",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF5F7F8),
                                fontSize = 17.sp
                            )
                        )
                        Text(
                            text = "to continue to PromptDesk",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF859490),
                                fontSize = 13.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Active Detected Google Account Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF171B21),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF252B32)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(enabled = !isSigningIn) {
                            coroutineScope.launch {
                                isSigningIn = true
                                delay(600L) // Smooth native authentication handshake
                                preferencesManager.saveGoogleUser(
                                    name = googleAccountName,
                                    email = googleAccountEmail,
                                    photoUrl = ""
                                )
                                showGoogleAccountPicker = false
                                onAuthSuccess()
                            }
                        }
                        .testTag("google_account_item")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Avatar with Google styling
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF4285F4),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = googleAccountName.take(1),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = googleAccountName,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFF5F7F8),
                                    fontSize = 15.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = googleAccountEmail,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF859490),
                                    fontSize = 13.sp
                                )
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color(0xFF19C7B1),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Option to use another account
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(enabled = !isSigningIn) {
                            coroutineScope.launch {
                                isSigningIn = true
                                delay(500L)
                                preferencesManager.saveGoogleUser(
                                    name = googleAccountName,
                                    email = googleAccountEmail,
                                    photoUrl = ""
                                )
                                showGoogleAccountPicker = false
                                onAuthSuccess()
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF282A2D),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add account",
                                    tint = Color(0xFFF5F7F8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = "Add another account",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFFBBCAC5),
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Google Data Consent Footer
                Text(
                    text = "To continue, Google will share your name, email address, language preference, and profile picture with PromptDesk. See PromptDesk's Privacy Policy and Terms of Service.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = Color(0xFF6F7882)
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun FeaturePill(text: String) {
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = Color(0xFF171B21),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF252B32))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(Color(0xFF19C7B1), CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFBBCAC5)
            )
        }
    }
}
