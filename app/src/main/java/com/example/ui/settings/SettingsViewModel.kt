package com.example.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.PromptDeskApp
import com.example.data.local.PreferencesManager
import com.example.data.repository.AuthRepository
import com.example.data.repository.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val preferencesManager: PreferencesManager = PromptDeskApp.instance.preferencesManager
    private val authRepository: AuthRepository = PromptDeskApp.instance.authRepository

    val currentUser: StateFlow<UserProfile?> = authRepository.currentUserState

    val defaultSpeed: StateFlow<Float> = preferencesManager.defaultSpeed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.3f)

    val defaultFontSize: StateFlow<Float> = preferencesManager.defaultFontSize
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 42f)

    val defaultAlignment: StateFlow<String> = preferencesManager.defaultAlignment
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "CENTER")

    val defaultCountdown: StateFlow<Int> = preferencesManager.defaultCountdown
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 3)

    val voiceFollowLang: StateFlow<String> = preferencesManager.voiceFollowLang
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en-US")

    val voiceFollowSensitivity: StateFlow<Float> = preferencesManager.voiceFollowSensitivity
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f)

    val themeMode: StateFlow<String> = preferencesManager.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "DARK")

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun setDefaultSpeed(speed: Float) {
        viewModelScope.launch { preferencesManager.setDefaultSpeed(speed) }
    }

    fun setDefaultFontSize(size: Float) {
        viewModelScope.launch { preferencesManager.setDefaultFontSize(size) }
    }

    fun setDefaultAlignment(alignment: String) {
        viewModelScope.launch { preferencesManager.setDefaultAlignment(alignment) }
    }

    fun setDefaultCountdown(seconds: Int) {
        viewModelScope.launch { preferencesManager.setDefaultCountdown(seconds) }
    }

    fun setVoiceFollowLang(lang: String) {
        viewModelScope.launch { preferencesManager.setVoiceFollowLang(lang) }
    }

    fun setVoiceFollowSensitivity(sensitivity: Float) {
        viewModelScope.launch { preferencesManager.setVoiceFollowSensitivity(sensitivity) }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { preferencesManager.setThemeMode(mode) }
    }

    fun clearCache() {
        val cacheDir = getApplication<Application>().cacheDir
        cacheDir.deleteRecursively()
        cacheDir.mkdirs()
        _message.value = "Cache cleared successfully"
    }

    fun clearMessage() {
        _message.value = null
    }

    fun signOut() {
        authRepository.signOut()
    }
}
