package com.example.data.repository

import com.example.data.local.ScriptDao
import com.example.data.local.ScriptEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ScriptRepository(private val scriptDao: ScriptDao) {

    val allScripts: Flow<List<ScriptEntity>> = scriptDao.getAllScripts()

    val favoriteScripts: Flow<List<ScriptEntity>> = scriptDao.getFavoriteScripts()

    fun getRecentScripts(limit: Int = 5): Flow<List<ScriptEntity>> = scriptDao.getRecentScripts(limit)

    fun searchScripts(query: String): Flow<List<ScriptEntity>> = scriptDao.searchScripts(query)

    suspend fun getScriptById(id: Long): ScriptEntity? = withContext(Dispatchers.IO) {
        scriptDao.getScriptById(id)
    }

    suspend fun insertScript(
        title: String,
        content: String,
        fontSize: Float = 42f,
        scrollSpeed: Float = 1.3f,
        mirrorMode: Boolean = false,
        alignment: String = "CENTER",
        countdownSeconds: Int = 3
    ): Long = withContext(Dispatchers.IO) {
        val wordCount = calculateWordCount(content)
        val entity = ScriptEntity(
            title = title.ifBlank { "Untitled Script" },
            content = content,
            wordCount = wordCount,
            fontSize = fontSize,
            scrollSpeed = scrollSpeed,
            mirrorMode = mirrorMode,
            alignment = alignment,
            countdownSeconds = countdownSeconds,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        scriptDao.insertScript(entity)
    }

    suspend fun updateScript(script: ScriptEntity) = withContext(Dispatchers.IO) {
        val updated = script.copy(
            wordCount = calculateWordCount(script.content),
            updatedAt = System.currentTimeMillis()
        )
        scriptDao.updateScript(updated)
    }

    suspend fun updateLastPosition(id: Long, position: Int) = withContext(Dispatchers.IO) {
        scriptDao.updateLastPosition(id, position)
    }

    suspend fun toggleFavorite(id: Long, currentFavorite: Boolean) = withContext(Dispatchers.IO) {
        scriptDao.updateFavorite(id, !currentFavorite)
    }

    suspend fun duplicateScript(id: Long): Long? = withContext(Dispatchers.IO) {
        val original = scriptDao.getScriptById(id) ?: return@withContext null
        val copy = original.copy(
            id = 0,
            title = "${original.title} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            favorite = false,
            lastPosition = 0
        )
        scriptDao.insertScript(copy)
    }

    suspend fun deleteScript(id: Long) = withContext(Dispatchers.IO) {
        scriptDao.deleteScriptById(id)
    }

    private fun calculateWordCount(text: String): Int {
        if (text.isBlank()) return 0
        return text.trim().split("\\s+".toRegex()).count { it.isNotEmpty() }
    }
}
