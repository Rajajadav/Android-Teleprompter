package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scripts")
data class ScriptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val wordCount: Int = 0,
    val favorite: Boolean = false,
    val lastPosition: Int = 0, // scroll offset in pixels
    val fontSize: Float = 36f, // 24sp to 100sp
    val scrollSpeed: Float = 1.2f, // 0.1x to 5.0x
    val mirrorMode: Boolean = false,
    val alignment: String = "CENTER", // "LEFT", "CENTER", "RIGHT"
    val countdownSeconds: Int = 3,
    // Overlay specific parameters
    val overlayWidthPercent: Int = 90, // 60% to 100%
    val overlayHeightPercent: Int = 26, // 15% to 50%
    val overlayX: Int = 0,
    val overlayY: Int = 120, // default near top camera lens
    val backgroundOpacity: Float = 0.78f, // 0.2 to 1.0
    val textColorHex: String = "#FFFFFF",
    val backgroundColorHex: String = "#0D1117",
    val avoidCutout: Boolean = true,
    val autoCenter: Boolean = true,
    val readingGuide: String = "LINE" // "NONE", "LINE", "PROGRESS"
)
