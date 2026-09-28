package com.example.ui.scripts

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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOption(val label: String) {
    RECENT("Recently Modified"),
    TITLE("Alphabetical"),
    WORD_COUNT("Word Count"),
    FAVORITES("Favorites First")
}

class ScriptsViewModel(
    private val scriptRepository: ScriptRepository = PromptDeskApp.instance.scriptRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.RECENT)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _filterFavoritesOnly = MutableStateFlow(false)
    val filterFavoritesOnly: StateFlow<Boolean> = _filterFavoritesOnly.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val scripts: StateFlow<List<ScriptEntity>> = combine(
        scriptRepository.allScripts,
        _searchQuery,
        _sortOption,
        _filterFavoritesOnly
    ) { all, query, sort, favOnly ->
        var list = all

        if (favOnly) {
            list = list.filter { it.favorite }
        }

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.title.lowercase().contains(q) || it.content.lowercase().contains(q)
            }
        }

        when (sort) {
            SortOption.RECENT -> list.sortedByDescending { it.updatedAt }
            SortOption.TITLE -> list.sortedBy { it.title.lowercase() }
            SortOption.WORD_COUNT -> list.sortedByDescending { it.wordCount }
            SortOption.FAVORITES -> list.sortedWith(compareByDescending<ScriptEntity> { it.favorite }.thenByDescending { it.updatedAt })
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    fun toggleFavoritesFilter() {
        _filterFavoritesOnly.value = !_filterFavoritesOnly.value
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

    fun clearMessage() {
        _message.value = null
    }
}
