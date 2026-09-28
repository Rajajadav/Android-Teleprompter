package com.example.ui.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.PromptDeskApp
import com.example.data.local.ScriptEntity
import com.example.data.repository.ScriptRepository
import com.example.util.FileUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val scriptRepository: ScriptRepository = PromptDeskApp.instance.scriptRepository
) : ViewModel() {

    val recentScripts: StateFlow<List<ScriptEntity>> = scriptRepository.getRecentScripts(limit = 6)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() {
        _message.value = null
    }

    fun toggleFavorite(script: ScriptEntity) {
        viewModelScope.launch {
            scriptRepository.toggleFavorite(script.id, script.favorite)
        }
    }

    fun duplicateScript(scriptId: Long) {
        viewModelScope.launch {
            scriptRepository.duplicateScript(scriptId)
            _message.value = "Script duplicated"
        }
    }

    fun deleteScript(scriptId: Long) {
        viewModelScope.launch {
            scriptRepository.deleteScript(scriptId)
            _message.value = "Script deleted"
        }
    }

    fun importScriptFromUri(uri: Uri, defaultTitle: String? = null) {
        viewModelScope.launch {
            val context = PromptDeskApp.instance
            val content = FileUtils.readTextFromUri(context, uri)
            if (!content.isNullOrBlank()) {
                val title = defaultTitle ?: "Imported Script"
                scriptRepository.insertScript(title = title, content = content)
                _message.value = "Script imported successfully"
            } else {
                _message.value = "Unable to read text from file"
            }
        }
    }
}
