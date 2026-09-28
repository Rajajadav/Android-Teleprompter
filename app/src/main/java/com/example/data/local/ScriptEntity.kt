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
    val lastPosition: Int = 0, // scroll offset or line position
    val fontSize: Float = 42f, // 24sp to 96sp
    val scrollSpeed: Float = 1.5f, // 0.1x to 5.0x
    val mirrorMode: Boolean = false,
    val alignment: String = "CENTER", // "LEFT", "CENTER", "RIGHT"
    val countdownSeconds: Int = 3
)
