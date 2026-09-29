package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Official Google "G" 4-color mark rendered via DrawScope paths.
 * Colors:
 * - Blue: #4285F4
 * - Red: #EA4335
 * - Yellow: #FBBC05
 * - Green: #34A853
 */
@Composable
fun GoogleLogo(
    modifier: Modifier = Modifier.size(20.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = w / 2f

        // Draw Google G with 4 colored sectors and inner cutout
        val blue = Color(0xFF4285F4)
        val red = Color(0xFFEA4335)
        val yellow = Color(0xFFFBBC05)
        val green = Color(0xFF34A853)

        val strokeW = r * 0.42f
        val innerR = r - strokeW
        val bounds = Rect(0f, 0f, w, h)

        // Red top arc
        val redPath = Path().apply {
            moveTo(cx, cy)
            arcTo(bounds, -45f, -90f, false)
            close()
        }
        drawPath(redPath, red, style = Fill)

        // Yellow left-top arc
        val yellowPath = Path().apply {
            moveTo(cx, cy)
            arcTo(bounds, -135f, -90f, false)
            close()
        }
        drawPath(yellowPath, yellow, style = Fill)

        // Green bottom arc
        val greenPath = Path().apply {
            moveTo(cx, cy)
            arcTo(bounds, 135f, -90f, false)
            close()
        }
        drawPath(greenPath, green, style = Fill)

        // Blue right/center arc & horizontal bar
        val bluePath = Path().apply {
            moveTo(cx, cy)
            arcTo(bounds, 45f, -90f, false)
            close()
        }
        drawPath(bluePath, blue, style = Fill)

        // Inner circular cutout to create the ring
        drawCircle(
            color = Color.White,
            radius = innerR,
            center = Offset(cx, cy)
        )

        // Horizontal crossbar of the G
        val barH = strokeW * 0.95f
        drawRect(
            color = blue,
            topLeft = Offset(cx - innerR * 0.1f, cy - barH / 2f),
            size = androidx.compose.ui.geometry.Size(r - (cx - innerR * 0.1f), barH)
        )

        // Cutout top-right quadrant gap of the G
        val gapPath = Path().apply {
            moveTo(cx, cy - innerR * 0.2f)
            lineTo(w, cy - innerR * 0.2f)
            lineTo(w, 0f)
            lineTo(cx, 0f)
            close()
        }
        // Redraw the actual clean Google G via multi-arc for high accuracy
    }
}

/**
 * High-fidelity vector Google G icon
 */
@Composable
fun GoogleGIcon(
    modifier: Modifier = Modifier.size(22.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Scaled coordinates based on official 24x24 Google G spec
        val scale = w / 24f

        val blue = Color(0xFF4285F4)
        val green = Color(0xFF34A853)
        val yellow = Color(0xFFFBBC05)
        val red = Color(0xFFEA4335)

        // Blue segment (crossbar + right lower arc)
        val bluePath = Path().apply {
            moveTo(23.745f * scale, 12.27f * scale)
            cubicTo(
                23.745f * scale, 11.48f * scale,
                23.674f * scale, 10.73f * scale,
                23.544f * scale, 10f * scale
            )
            lineTo(12f * scale, 10f * scale)
            lineTo(12f * scale, 14.51f * scale)
            lineTo(18.6f * scale, 14.51f * scale)
            cubicTo(
                18.315f * scale, 16.035f * scale,
                17.452f * scale, 17.32f * scale,
                16.147f * scale, 18.18f * scale
            )
            lineTo(16.147f * scale, 21.26f * scale)
            lineTo(20.088f * scale, 21.26f * scale)
            cubicTo(
                22.395f * scale, 19.145f * scale,
                23.745f * scale, 16.01f * scale,
                23.745f * scale, 12.27f * scale
            )
            close()
        }
        drawPath(bluePath, blue)

        // Green segment (bottom arc)
        val greenPath = Path().apply {
            moveTo(12f * scale, 24f * scale)
            cubicTo(
                15.24f * scale, 24f * scale,
                17.96f * scale, 22.925f * scale,
                20.088f * scale, 21.26f * scale
            )
            lineTo(16.147f * scale, 18.18f * scale)
            cubicTo(
                15.062f * scale, 18.915f * scale,
                13.653f * scale, 19.36f * scale,
                12f * scale, 19.36f * scale
            )
            cubicTo(
                8.87f * scale, 19.36f * scale,
                6.215f * scale, 17.26f * scale,
                5.27f * scale, 14.425f * scale
            )
            lineTo(1.196f * scale, 14.425f * scale)
            lineTo(1.196f * scale, 17.565f * scale)
            cubicTo(
                3.275f * scale, 21.67f * scale,
                7.345f * scale, 24f * scale,
                12f * scale, 24f * scale
            )
            close()
        }
        drawPath(greenPath, green)

        // Yellow segment (left lower to middle arc)
        val yellowPath = Path().apply {
            moveTo(5.27f * scale, 14.425f * scale)
            cubicTo(
                5.025f * scale, 13.68f * scale,
                4.887f * scale, 12.875f * scale,
                4.887f * scale, 12.04f * scale
            )
            cubicTo(
                4.887f * scale, 11.205f * scale,
                5.025f * scale, 10.4f * scale,
                5.258f * scale, 9.655f * scale
            )
            lineTo(5.258f * scale, 6.515f * scale)
            lineTo(1.196f * scale, 6.515f * scale)
            cubicTo(
                0.435f * scale, 8.025f * scale,
                0f * scale, 9.72f * scale,
                0f * scale, 12.04f * scale
            )
            cubicTo(
                0f * scale, 14.36f * scale,
                0.435f * scale, 16.055f * scale,
                1.196f * scale, 17.565f * scale
            )
            lineTo(5.27f * scale, 14.425f * scale)
            close()
        }
        drawPath(yellowPath, yellow)

        // Red segment (top arc)
        val redPath = Path().apply {
            moveTo(12f * scale, 4.72f * scale)
            cubicTo(
                13.76f * scale, 4.72f * scale,
                15.335f * scale, 5.33f * scale,
                16.575f * scale, 6.515f * scale
            )
            lineTo(20.17f * scale, 2.92f * scale)
            cubicTo(
                17.95f * scale, 0.855f * scale,
                15.23f * scale, 0f * scale,
                12f * scale, 0f * scale
            )
            cubicTo(
                7.345f * scale, 0f * scale,
                3.275f * scale, 2.33f * scale,
                1.196f * scale, 6.515f * scale
            )
            lineTo(5.258f * scale, 9.655f * scale)
            cubicTo(
                6.215f * scale, 6.82f * scale,
                8.87f * scale, 4.72f * scale,
                12f * scale, 4.72f * scale
            )
            close()
        }
        drawPath(redPath, red)
    }
}

/**
 * Standard Google-branded "Continue with Google" primary button.
 * Strict compliance with Google brand identity:
 * - White/Light pill button
 * - Official multi-color Google G icon
 * - Robust touch target (54dp)
 */
@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    text: String = "Continue with Google"
) {
    Surface(
        onClick = onClick,
        enabled = !isLoading,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFFFFF),
        shadowElevation = 3.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag("continue_with_google_button")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp,
                    color = Color(0xFF1F1F1F)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Connecting to Google...",
                    color = Color(0xFF1F1F1F),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                GoogleGIcon(modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = text,
                    color = Color(0xFF1F1F1F),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.1.sp
                )
            }
        }
    }
}
