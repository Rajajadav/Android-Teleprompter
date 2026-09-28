package com.example.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.PromptDeskApp
import com.example.data.local.ScriptEntity
import com.example.data.repository.ScriptRepository
import com.example.domain.ai.AiScriptAssistant
import com.example.domain.ai.LocalAiScriptAssistant
import com.example.domain.ai.RewriteStyle
import com.example.domain.ai.ScriptFormat
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SaveStatus {
    object Saved : SaveStatus()
    object Saving : SaveStatus()
    object Unsaved : SaveStatus()
}

class ScriptEditorViewModel(
    private val scriptRepository: ScriptRepository = PromptDeskApp.instance.scriptRepository,
    private val aiAssistant: AiScriptAssistant = LocalAiScriptAssistant()
) : ViewModel() {

    private var currentScriptId: Long = 0L

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    private val _fontSize = MutableStateFlow(42f)
    val fontSize: StateFlow<Float> = _fontSize.asStateFlow()

    private val _scrollSpeed = MutableStateFlow(1.3f)
    val scrollSpeed: StateFlow<Float> = _scrollSpeed.asStateFlow()

    private val _alignment = MutableStateFlow("CENTER")
    val alignment: StateFlow<String> = _alignment.asStateFlow()

    private val _mirrorMode = MutableStateFlow(false)
    val mirrorMode: StateFlow<Boolean> = _mirrorMode.asStateFlow()

    private val _saveStatus = MutableStateFlow<SaveStatus>(SaveStatus.Saved)
    val saveStatus: StateFlow<SaveStatus> = _saveStatus.asStateFlow()

    private val _wordCount = MutableStateFlow(0)
    val wordCount: StateFlow<Int> = _wordCount.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    private var autosaveJob: Job? = null

    fun loadScript(scriptId: Long) {
        if (scriptId == 0L) {
            currentScriptId = 0L
            _title.value = "New Script"
            _content.value = ""
            _wordCount.value = 0
            _saveStatus.value = SaveStatus.Saved
            return
        }

        currentScriptId = scriptId
        viewModelScope.launch {
            val script = scriptRepository.getScriptById(scriptId)
            script?.let {
                _title.value = it.title
                _content.value = it.content
                _fontSize.value = it.fontSize
                _scrollSpeed.value = it.scrollSpeed
                _alignment.value = it.alignment
                _mirrorMode.value = it.mirrorMode
                _wordCount.value = it.wordCount
                _saveStatus.value = SaveStatus.Saved
            }
        }
    }

    fun onTitleChanged(newTitle: String) {
        _title.value = newTitle
        triggerDebouncedAutosave()
    }

    fun onContentChanged(newContent: String) {
        _content.value = newContent
        _wordCount.value = countWords(newContent)
        triggerDebouncedAutosave()
    }

    fun setFontSize(size: Float) {
        _fontSize.value = size
        triggerDebouncedAutosave()
    }

    fun setScrollSpeed(speed: Float) {
        _scrollSpeed.value = speed
        triggerDebouncedAutosave()
    }

    fun setAlignment(align: String) {
        _alignment.value = align
        triggerDebouncedAutosave()
    }

    fun toggleMirrorMode() {
        _mirrorMode.value = !_mirrorMode.value
        triggerDebouncedAutosave()
    }

    fun appendFormatting(prefix: String, suffix: String) {
        val current = _content.value
        _content.value = if (current.isEmpty()) "$prefix$suffix" else "$current\n$prefix\n$suffix"
        _wordCount.value = countWords(_content.value)
        triggerDebouncedAutosave()
    }

    private fun triggerDebouncedAutosave() {
        _saveStatus.value = SaveStatus.Unsaved
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            delay(800L) // 800ms debounce
            saveScript()
        }
    }

    fun saveScriptImmediately() {
        autosaveJob?.cancel()
        viewModelScope.launch {
            saveScript()
        }
    }

    private suspend fun saveScript() {
        _saveStatus.value = SaveStatus.Saving
        val t = _title.value.ifBlank { "Untitled Script" }
        val c = _content.value

        if (currentScriptId == 0L) {
            val newId = scriptRepository.insertScript(
                title = t,
                content = c,
                fontSize = _fontSize.value,
                scrollSpeed = _scrollSpeed.value,
                mirrorMode = _mirrorMode.value,
                alignment = _alignment.value
            )
            currentScriptId = newId
        } else {
            val existing = scriptRepository.getScriptById(currentScriptId)
            val updated = existing?.copy(
                title = t,
                content = c,
                fontSize = _fontSize.value,
                scrollSpeed = _scrollSpeed.value,
                mirrorMode = _mirrorMode.value,
                alignment = _alignment.value
            ) ?: ScriptEntity(
                id = currentScriptId,
                title = t,
                content = c,
                fontSize = _fontSize.value,
                scrollSpeed = _scrollSpeed.value,
                mirrorMode = _mirrorMode.value,
                alignment = _alignment.value
            )
            scriptRepository.updateScript(updated)
        }
        _saveStatus.value = SaveStatus.Saved
    }

    fun getScriptId(): Long = currentScriptId

    // AI Creator Assistant operations
    fun generateAiScript(topic: String, format: ScriptFormat) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val result = aiAssistant.generateScript(topic, format)
            result.onSuccess { generated ->
                _content.value = if (_content.value.isBlank()) generated else "${_content.value}\n\n$generated"
                _wordCount.value = countWords(_content.value)
                if (_title.value == "New Script" || _title.value.isBlank()) {
                    _title.value = topic.take(30)
                }
                triggerDebouncedAutosave()
                _uiMessage.value = "Script draft generated!"
            }.onFailure {
                _uiMessage.value = "Failed to generate: ${it.localizedMessage}"
            }
            _isAiLoading.value = false
        }
    }

    fun rewriteWithAi(style: RewriteStyle) {
        viewModelScope.launch {
            if (_content.value.isBlank()) return@launch
            _isAiLoading.value = true
            val result = aiAssistant.rewrite(_content.value, style)
            result.onSuccess {
                _content.value = it
                _wordCount.value = countWords(it)
                triggerDebouncedAutosave()
                _uiMessage.value = "Style updated to ${style.displayName}"
            }
            _isAiLoading.value = false
        }
    }

    fun shortenWithAi() {
        viewModelScope.launch {
            if (_content.value.isBlank()) return@launch
            _isAiLoading.value = true
            aiAssistant.shorten(_content.value).onSuccess {
                _content.value = it
                _wordCount.value = countWords(it)
                triggerDebouncedAutosave()
                _uiMessage.value = "Script trimmed for concise delivery"
            }
            _isAiLoading.value = false
        }
    }

    fun clearMessage() {
        _uiMessage.value = null
    }

    private fun countWords(text: String): Int {
        if (text.isBlank()) return 0
        return text.trim().split("\\s+".toRegex()).count { it.isNotEmpty() }
    }

    override fun onCleared() {
        super.onCleared()
        autosaveJob?.cancel()
    }
}
