package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Teal80
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CountdownOverlay(
    seconds: Int,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (seconds <= 0) {
        LaunchedEffect(Unit) { onFinished() }
        return
    }

    var currentCount by remember { mutableIntStateOf(seconds) }

    LaunchedEffect(currentCount) {
        if (currentCount > 0) {
            delay(1000L)
            currentCount -= 1
        } else {
            delay(400L)
            onFinished()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onFinished // Tap to skip countdown
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = currentCount,
            transitionSpec = {
                (fadeIn() + scaleIn(initialScale = 0.5f)) togetherWith (fadeOut() + scaleOut(targetScale = 1.3f))
            },
            label = "countdown"
        ) { count ->
            val text = if (count > 0) count.toString() else "GO!"
            val color = if (count > 0) Teal80 else Color.White
            Text(
                text = text,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 110.sp,
                    fontWeight = FontWeight.Black,
                    color = color
                )
            )
        }
    }
}
