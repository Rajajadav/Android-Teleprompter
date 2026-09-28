package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {

    fun formatDuration(seconds: Long): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format(Locale.US, "%02d:%02d", mins, secs)
    }

    fun formatEstimatedReadingTime(wordCount: Int, speedMultiplier: Float = 1.0f): String {
        val wpm = (130 * speedMultiplier).coerceAtLeast(60f)
        val minutes = (wordCount / wpm)
        val totalSeconds = (minutes * 60).toInt()
        val mins = totalSeconds / 60
        val secs = totalSeconds % 60
        return if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 0 -> "${days}d ago"
            hours > 0 -> "${hours}h ago"
            minutes > 0 -> "${minutes}m ago"
            else -> "Just now"
        }
    }
}
