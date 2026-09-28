package com.example.ui.teleprompter

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.PromptDeskApp
import com.example.data.local.ScriptEntity
import com.example.data.repository.ScriptRepository
import com.example.speech.VoiceFollowManager
import com.example.speech.VoiceFollowStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TeleprompterViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val scriptRepository: ScriptRepository = PromptDeskApp.instance.scriptRepository
    private val preferencesManager = PromptDeskApp.instance.preferencesManager
    val voiceFollowManager = VoiceFollowManager(application.applicationContext)

    private val _script = MutableStateFlow<ScriptEntity?>(null)
    val script: StateFlow<ScriptEntity?> = _script.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _scrollSpeed = MutableStateFlow(1.3f) // 0.1 to 5.0
    val scrollSpeed: StateFlow<Float> = _scrollSpeed.asStateFlow()

    private val _fontSize = MutableStateFlow(42f) // 24 to 96
    val fontSize: StateFlow<Float> = _fontSize.asStateFlow()

    private val _mirrorMode = MutableStateFlow(false)
    val mirrorMode: StateFlow<Boolean> = _mirrorMode.asStateFlow()

    private val _alignment = MutableStateFlow("CENTER")
    val alignment: StateFlow<String> = _alignment.asStateFlow()

    private val _textWidthPercent = MutableStateFlow(85)
    val textWidthPercent: StateFlow<Int> = _textWidthPercent.asStateFlow()

    private val _controlsVisible = MutableStateFlow(true)
    val controlsVisible: StateFlow<Boolean> = _controlsVisible.asStateFlow()

    private val _countdownActive = MutableStateFlow(false)
    val countdownActive: StateFlow<Boolean> = _countdownActive.asStateFlow()

    private val _countdownDuration = MutableStateFlow(3)
    val countdownDuration: StateFlow<Int> = _countdownDuration.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    private val _isVoiceFollowEnabled = MutableStateFlow(false)
    val isVoiceFollowEnabled: StateFlow<Boolean> = _isVoiceFollowEnabled.asStateFlow()

    private val _currentScrollFraction = MutableStateFlow(0f)
    val currentScrollFraction: StateFlow<Float> = _currentScrollFraction.asStateFlow()

    private var timerJob: Job? = null
    private var hideControlsJob: Job? = null

    init {
        // Collect voice follow matching updates
        viewModelScope.launch {
            voiceFollowManager.status.collect { status ->
                if (status is VoiceFollowStatus.WordMatched) {
                    _currentScrollFraction.value = status.targetScrollFraction
                }
            }
        }
    }

    fun loadScript(scriptId: Long) {
        viewModelScope.launch {
            val loaded = if (scriptId > 0) scriptRepository.getScriptById(scriptId) else null
            if (loaded != null) {
                _script.value = loaded
                _fontSize.value = loaded.fontSize
                _scrollSpeed.value = loaded.scrollSpeed
                _mirrorMode.value = loaded.mirrorMode
                _alignment.value = loaded.alignment
                _countdownDuration.value = loaded.countdownSeconds
                voiceFollowManager.setScript(loaded.content)
            } else {
                // Default starter text if loaded with no id
                val fallback = ScriptEntity(
                    title = "Quick Teleprompter",
                    content = """
                        Welcome to PromptDesk Teleprompter!

                        This is full distraction-free studio reading mode.

                        Look directly at the top of your screen to maintain natural eye contact with your camera lens.

                        Tap the screen once to pause or play.

                        Double tap to toggle the control panel.
                    """.trimIndent(),
                    wordCount = 42
                )
                _script.value = fallback
                voiceFollowManager.setScript(fallback.content)
            }

            // Start countdown if duration > 0
            if (_countdownDuration.value > 0) {
                _countdownActive.value = true
            } else {
                startPlaying()
            }
        }
    }

    fun onCountdownFinished() {
        _countdownActive.value = false
        startPlaying()
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pausePlaying()
        } else {
            startPlaying()
        }
    }

    fun startPlaying() {
        _isPlaying.value = true
        startTimer()
        scheduleControlsAutoHide()

        if (_isVoiceFollowEnabled.value) {
            voiceFollowManager.startListening(_currentScrollFraction.value)
        }
    }

    fun pausePlaying() {
        _isPlaying.value = false
        timerJob?.cancel()
        _controlsVisible.value = true
        hideControlsJob?.cancel()

        if (_isVoiceFollowEnabled.value) {
            voiceFollowManager.stopListening()
        }

        saveCurrentPosition()
    }

    fun restart() {
        _currentScrollFraction.value = 0f
        _elapsedSeconds.value = 0L
        if (_countdownDuration.value > 0) {
            _isPlaying.value = false
            _countdownActive.value = true
        } else {
            startPlaying()
        }
    }

    fun toggleControlsVisibility() {
        _controlsVisible.value = !_controlsVisible.value
        if (_controlsVisible.value && _isPlaying.value) {
            scheduleControlsAutoHide()
        }
    }

    fun showControls() {
        _controlsVisible.value = true
        if (_isPlaying.value) {
            scheduleControlsAutoHide()
        }
    }

    private fun scheduleControlsAutoHide() {
        hideControlsJob?.cancel()
        hideControlsJob = viewModelScope.launch {
            delay(4000L)
            if (_isPlaying.value) {
                _controlsVisible.value = false
            }
        }
    }

    fun setScrollSpeed(speed: Float) {
        _scrollSpeed.value = speed.coerceIn(0.1f, 5.0f)
        scheduleControlsAutoHide()
        saveScriptSettings()
    }

    fun adjustScrollSpeed(delta: Float) {
        setScrollSpeed((_scrollSpeed.value + delta))
    }

    fun setFontSize(size: Float) {
        _fontSize.value = size.coerceIn(24f, 96f)
        scheduleControlsAutoHide()
        saveScriptSettings()
    }

    fun toggleMirrorMode() {
        _mirrorMode.value = !_mirrorMode.value
        scheduleControlsAutoHide()
        saveScriptSettings()
    }

    fun setAlignment(align: String) {
        _alignment.value = align
        scheduleControlsAutoHide()
        saveScriptSettings()
    }

    fun setTextWidthPercent(percent: Int) {
        _textWidthPercent.value = percent.coerceIn(50, 100)
    }

    fun toggleVoiceFollow() {
        _isVoiceFollowEnabled.value = !_isVoiceFollowEnabled.value
        if (_isVoiceFollowEnabled.value) {
            if (_isPlaying.value) {
                voiceFollowManager.startListening(_currentScrollFraction.value)
            }
        } else {
            voiceFollowManager.stopListening()
        }
    }

    fun updateScrollFraction(fraction: Float) {
        _currentScrollFraction.value = fraction.coerceIn(0f, 1f)
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isPlaying.value) {
                delay(1000L)
                _elapsedSeconds.value += 1
            }
        }
    }

    private fun saveCurrentPosition() {
        val s = _script.value ?: return
        if (s.id > 0) {
            viewModelScope.launch {
                scriptRepository.updateLastPosition(s.id, (_currentScrollFraction.value * 1000).toInt())
            }
        }
    }

    private fun saveScriptSettings() {
        val s = _script.value ?: return
        if (s.id > 0) {
            viewModelScope.launch {
                val updated = s.copy(
                    fontSize = _fontSize.value,
                    scrollSpeed = _scrollSpeed.value,
                    mirrorMode = _mirrorMode.value,
                    alignment = _alignment.value
                )
                scriptRepository.updateScript(updated)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        hideControlsJob?.cancel()
        voiceFollowManager.stopListening()
        saveCurrentPosition()
    }
}
