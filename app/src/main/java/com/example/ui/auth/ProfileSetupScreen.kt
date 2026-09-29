package com.example.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.PromptDeskApp
import com.example.ui.components.GoogleGIcon
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSetupScreen(
    onSetupComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val preferencesManager = PromptDeskApp.instance.preferencesManager

    val initialName by preferencesManager.userName.collectAsStateWithLifecycle(initialValue = "Raja Jadav")
    val initialEmail by preferencesManager.userEmail.collectAsStateWithLifecycle(initialValue = "rajajadavstudio@gmail.com")

    var step by remember { mutableIntStateOf(1) } // 1: Name & Role, 2: Use Cases & Referral, 3: Confirmation Summary

    var fullName by remember(initialName) { mutableStateOf(initialName) }
    var selectedRole by remember { mutableStateOf("Content Creator") }
    var customRoleText by remember { mutableStateOf("") }

    val selectedUseCases = remember {
        mutableStateOf(setOf("YouTube Videos", "Short Videos"))
    }
    var customUseCaseText by remember { mutableStateOf("") }

    var selectedReferral by remember { mutableStateOf("YouTube") }
    var customReferralText by remember { mutableStateOf("") }

    val roleOptions = listOf(
        "Content Creator",
        "YouTuber",
        "Instagram / Reels Creator",
        "Influencer",
        "Teacher / Educator",
        "Business Professional",
        "Presenter / Public Speaker",
        "Podcaster",
        "News / Media",
        "Student",
        "Freelancer",
        "Other"
    )

    val useCaseOptions = listOf(
        "YouTube Videos",
        "Instagram Reels",
        "Short Videos",
        "Online Courses",
        "Presentations",
        "Business Videos",
        "News / Announcements",
        "Speeches",
        "Interviews",
        "Other"
    )

    val referralOptions = listOf(
        "Google Search",
        "YouTube",
        "Instagram",
        "Facebook",
        "Friend / Colleague",
        "Website / Blog",
        "App Recommendation",
        "Advertisement",
        "Other"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0D10))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (step) {
                        1 -> "Step 1 of 2"
                        2 -> "Step 2 of 2"
                        else -> "Profile Summary"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF19C7B1)
                )

                Text(
                    text = when (step) {
                        1 -> "50% complete"
                        2 -> "75% complete"
                        else -> "100% ready"
                    },
                    fontSize = 12.sp,
                    color = Color(0xFF859490)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = {
                    when (step) {
                        1 -> 0.5f
                        2 -> 0.75f
                        else -> 1.0f
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Color(0xFF19C7B1),
                trackColor = Color(0xFF252B32)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Step Content
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut()
                        )
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                label = "ProfileSetupStepTransition"
            ) { targetStep ->
                when (targetStep) {
                    1 -> {
                        // Step 1: Full Name & What do you do
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "Tell us a little about you",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF5F7F8),
                                    fontSize = 26.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "This helps us personalize your PromptDesk experience.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF859490),
                                    fontSize = 14.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Full Name Section
                            Text(
                                text = "Full name",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFF5F7F8),
                                    fontSize = 14.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                placeholder = { Text("e.g. Raja Jadav", color = Color(0xFF6F7882)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("full_name_input"),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF19C7B1),
                                    unfocusedBorderColor = Color(0xFF252B32),
                                    focusedContainerColor = Color(0xFF171B21),
                                    unfocusedContainerColor = Color(0xFF171B21),
                                    focusedTextColor = Color(0xFFF5F7F8),
                                    unfocusedTextColor = Color(0xFFF5F7F8)
                                )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Use the name you want to see in PromptDesk.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF859490),
                                    fontSize = 12.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(28.dp))

                            // What do you do?
                            Text(
                                text = "What do you do?",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFF5F7F8),
                                    fontSize = 15.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Choose the option that best describes you.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF859490),
                                    fontSize = 12.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Selectable Options (Single Choice)
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                roleOptions.forEach { role ->
                                    val isSelected = selectedRole == role
                                    SelectablePill(
                                        text = role,
                                        isSelected = isSelected,
                                        onClick = { selectedRole = role }
                                    )
                                }
                            }

                            // If "Other" selected, show text field
                            AnimatedVisibility(visible = selectedRole == "Other") {
                                Column(modifier = Modifier.padding(top = 14.dp)) {
                                    Text(
                                        text = "Tell us what you do",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = Color(0xFFBBCAC5),
                                            fontSize = 13.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = customRoleText,
                                        onValueChange = { customRoleText = it },
                                        placeholder = { Text("e.g. Documentary Filmmaker", color = Color(0xFF6F7882)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF19C7B1),
                                            unfocusedBorderColor = Color(0xFF252B32),
                                            focusedContainerColor = Color(0xFF171B21),
                                            unfocusedContainerColor = Color(0xFF171B21),
                                            focusedTextColor = Color(0xFFF5F7F8),
                                            unfocusedTextColor = Color(0xFFF5F7F8)
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(30.dp))
                        }
                    }

                    2 -> {
                        // Step 2: What will you use PromptDesk for & Referral
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "What will you use PromptDesk for?",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF5F7F8),
                                    fontSize = 24.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Choose what you want to create with PromptDesk (select all that apply).",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF859490),
                                    fontSize = 14.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Multi-selection Cards with Checkmarks
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                useCaseOptions.forEach { useCase ->
                                    val isSelected = selectedUseCases.value.contains(useCase)
                                    MultiSelectCard(
                                        title = useCase,
                                        isSelected = isSelected,
                                        onClick = {
                                            val current = selectedUseCases.value.toMutableSet()
                                            if (isSelected) {
                                                if (current.size > 1) current.remove(useCase)
                                            } else {
                                                current.add(useCase)
                                            }
                                            selectedUseCases.value = current
                                        }
                                    )
                                }
                            }

                            // If Other in use cases
                            AnimatedVisibility(visible = selectedUseCases.value.contains("Other")) {
                                Column(modifier = Modifier.padding(top = 10.dp)) {
                                    Text(
                                        text = "Tell us what you want to create",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = Color(0xFFBBCAC5),
                                            fontSize = 13.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = customUseCaseText,
                                        onValueChange = { customUseCaseText = it },
                                        placeholder = { Text("e.g. Daily vlog intros", color = Color(0xFF6F7882)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF19C7B1),
                                            unfocusedBorderColor = Color(0xFF252B32),
                                            focusedContainerColor = Color(0xFF171B21),
                                            unfocusedContainerColor = Color(0xFF171B21),
                                            focusedTextColor = Color(0xFFF5F7F8),
                                            unfocusedTextColor = Color(0xFFF5F7F8)
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(28.dp))

                            // Referral Question
                            Text(
                                text = "How did you hear about PromptDesk?",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFF5F7F8),
                                    fontSize = 15.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Optional — helps us know where creators discover us.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF859490),
                                    fontSize = 12.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                referralOptions.forEach { ref ->
                                    val isSelected = selectedReferral == ref
                                    SelectablePill(
                                        text = ref,
                                        isSelected = isSelected,
                                        onClick = { selectedReferral = ref }
                                    )
                                }
                            }

                            if (selectedReferral == "Other") {
                                Column(modifier = Modifier.padding(top = 10.dp)) {
                                    Text(
                                        text = "Tell us where you found us",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = Color(0xFFBBCAC5),
                                            fontSize = 13.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = customReferralText,
                                        onValueChange = { customReferralText = it },
                                        placeholder = { Text("e.g. Tech newsletter", color = Color(0xFF6F7882)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF19C7B1),
                                            unfocusedBorderColor = Color(0xFF252B32),
                                            focusedContainerColor = Color(0xFF171B21),
                                            unfocusedContainerColor = Color(0xFF171B21),
                                            focusedTextColor = Color(0xFFF5F7F8),
                                            unfocusedTextColor = Color(0xFFF5F7F8)
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(30.dp))
                        }
                    }

                    3 -> {
                        // Step 3: Compact Confirmation Summary
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Profile",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF5F7F8)
                                )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Everything is set to personalize your studio.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF859490),
                                    fontSize = 13.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Profile Card
                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF171B21)),
                                border = BorderStroke(1.dp, Color(0xFF252B32)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(22.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Google Profile Photo
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF4285F4),
                                        modifier = Modifier.size(64.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = fullName.take(1).uppercase(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 28.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text(
                                        text = fullName.ifBlank { "Raja Jadav" },
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFF5F7F8),
                                            fontSize = 20.sp
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = initialEmail,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF859490),
                                            fontSize = 13.sp
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Role Badge
                                    Surface(
                                        shape = RoundedCornerShape(100.dp),
                                        color = Color(0xFF19C7B1).copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, Color(0xFF19C7B1).copy(alpha = 0.35f))
                                    ) {
                                        Text(
                                            text = if (selectedRole == "Other" && customRoleText.isNotBlank()) customRoleText else selectedRole,
                                            color = Color(0xFF19C7B1),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(Color(0xFF252B32))
                                    )

                                    Spacer(modifier = Modifier.height(18.dp))

                                    // Uses PromptDesk for
                                    Text(
                                        text = "Uses PromptDesk for:",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFBBCAC5),
                                            fontSize = 12.sp
                                        ),
                                        modifier = Modifier.align(Alignment.Start)
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        selectedUseCases.value.forEach { item ->
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color(0xFF1E2023),
                                                border = BorderStroke(1.dp, Color(0xFF252B32))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color(0xFF19C7B1),
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = item,
                                                        fontSize = 12.sp,
                                                        color = Color(0xFFF5F7F8),
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Edit Profile button
                            OutlinedButton(
                                onClick = { step = 1 },
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, Color(0xFF252B32)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFBBCAC5)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Edit Profile", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Bottom CTA Navigation Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                when (step) {
                    1 -> {
                        Button(
                            onClick = { step = 2 },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF19C7B1),
                                contentColor = Color(0xFF003730)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("continue_step1_button")
                        ) {
                            Text(
                                text = "Continue",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    2 -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { step = 1 },
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color(0xFF252B32)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFBBCAC5)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                            ) {
                                Text("Back", fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = { step = 3 },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF19C7B1),
                                    contentColor = Color(0xFF003730)
                                ),
                                modifier = Modifier
                                    .weight(2f)
                                    .height(52.dp)
                                    .testTag("continue_step2_button")
                            ) {
                                Text(
                                    text = "Continue",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    3 -> {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val finalRole = if (selectedRole == "Other" && customRoleText.isNotBlank()) customRoleText else selectedRole
                                    val finalReferral = if (selectedReferral == "Other" && customReferralText.isNotBlank()) customReferralText else selectedReferral
                                    preferencesManager.saveProfileSetup(
                                        name = fullName.ifBlank { "Raja Jadav" },
                                        occupation = finalRole,
                                        otherOccupation = customRoleText,
                                        useCases = selectedUseCases.value.toList(),
                                        referral = finalReferral,
                                        otherReferral = customReferralText
                                    )
                                    onSetupComplete()
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF19C7B1),
                                contentColor = Color(0xFF003730)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("start_using_promptdesk_button")
                        ) {
                            Text(
                                text = "Start Using PromptDesk",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectablePill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = if (isSelected) Color(0xFF19C7B1).copy(alpha = 0.16f) else Color(0xFF171B21),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) Color(0xFF19C7B1) else Color(0xFF252B32)
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF19C7B1),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color(0xFF19C7B1) else Color(0xFFBBCAC5)
            )
        }
    }
}

@Composable
private fun MultiSelectCard(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) Color(0xFF171B21) else Color(0xFF171B21).copy(alpha = 0.6f),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) Color(0xFF19C7B1) else Color(0xFF252B32)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) Color(0xFFF5F7F8) else Color(0xFFBBCAC5),
                    fontSize = 14.sp
                )
            )

            Surface(
                shape = CircleShape,
                color = if (isSelected) Color(0xFF19C7B1) else Color.Transparent,
                border = if (!isSelected) BorderStroke(1.5.dp, Color(0xFF3C4A46)) else null,
                modifier = Modifier.size(20.dp)
            ) {
                if (isSelected) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF003730),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}
