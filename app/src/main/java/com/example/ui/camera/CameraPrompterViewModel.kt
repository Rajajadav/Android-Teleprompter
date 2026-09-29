package com.example.ui.camera

import android.app.Application
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.PromptDeskApp
import com.example.data.local.ScriptEntity
import com.example.data.repository.ScriptRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class CameraPrompterViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val scriptRepository: ScriptRepository = PromptDeskApp.instance.scriptRepository

    private val _script = MutableStateFlow<ScriptEntity?>(null)
    val script: StateFlow<ScriptEntity?> = _script.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isPrompterScrolling = MutableStateFlow(false)
    val isPrompterScrolling: StateFlow<Boolean> = _isPrompterScrolling.asStateFlow()

    private val _scrollSpeed = MutableStateFlow(1.2f)
    val scrollSpeed: StateFlow<Float> = _scrollSpeed.asStateFlow()

    private val _fontSize = MutableStateFlow(32f)
    val fontSize: StateFlow<Float> = _fontSize.asStateFlow()

    private val _lensFacing = MutableStateFlow(CameraSelector.LENS_FACING_FRONT)
    val lensFacing: StateFlow<Int> = _lensFacing.asStateFlow()

    private val _recordSeconds = MutableStateFlow(0L)
    val recordSeconds: StateFlow<Long> = _recordSeconds.asStateFlow()

    private val _savedVideoUri = MutableStateFlow<Uri?>(null)
    val savedVideoUri: StateFlow<Uri?> = _savedVideoUri.asStateFlow()

    private val _countdownActive = MutableStateFlow(false)
    val countdownActive: StateFlow<Boolean> = _countdownActive.asStateFlow()

    private val _textHeightPercent = MutableStateFlow(40) // 30% to 60% of top screen
    val textHeightPercent: StateFlow<Int> = _textHeightPercent.asStateFlow()

    private val _overlayTransparency = MutableStateFlow(0.6f) // Dark glass overlay
    val overlayTransparency: StateFlow<Float> = _overlayTransparency.asStateFlow()

    private var recordTimerJob: Job? = null

    fun loadScript(scriptId: Long) {
        viewModelScope.launch {
            val loaded = if (scriptId > 0) scriptRepository.getScriptById(scriptId) else null
            if (loaded != null) {
                _script.value = loaded
                _fontSize.value = loaded.fontSize.coerceAtMost(48f) // Camera view uses slightly more compact text
                _scrollSpeed.value = loaded.scrollSpeed
            } else {
                val fallback = ScriptEntity(
                    title = "Quick Camera Session",
                    content = """
                        Look directly into the camera lens!

                        Position this text window near your camera lens to maintain direct, authentic eye contact with your viewers.

                        Press REC when ready. The prompter will start scrolling automatically.
                    """.trimIndent()
                )
                _script.value = fallback
            }
        }
    }

    fun flipCamera() {
        _lensFacing.value = if (_lensFacing.value == CameraSelector.LENS_FACING_FRONT) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }
    }

    fun startCountdownBeforeRecord() {
        _countdownActive.value = true
    }

    fun onCountdownFinished() {
        _countdownActive.value = false
        startRecording()
    }

    fun startRecording() {
        _isRecording.value = true
        _isPrompterScrolling.value = true
        _recordSeconds.value = 0L

        recordTimerJob?.cancel()
        recordTimerJob = viewModelScope.launch {
            while (_isRecording.value) {
                delay(1000L)
                _recordSeconds.value += 1
            }
        }
    }

    fun stopRecording(outputFile: File? = null) {
        _isRecording.value = false
        _isPrompterScrolling.value = false
        recordTimerJob?.cancel()

        if (outputFile != null && outputFile.exists()) {
            _savedVideoUri.value = Uri.fromFile(outputFile)
        }
    }

    fun togglePrompterScroll() {
        _isPrompterScrolling.value = !_isPrompterScrolling.value
    }

    fun togglePrompterScrolling() {
        togglePrompterScroll()
    }

    fun setScrollSpeed(speed: Float) {
        _scrollSpeed.value = speed.coerceIn(0.2f, 4.0f)
    }

    fun setFontSize(size: Float) {
        _fontSize.value = size.coerceIn(20f, 60f)
    }

    fun dismissSavedDialog() {
        _savedVideoUri.value = null
    }

    override fun onCleared() {
        super.onCleared()
        recordTimerJob?.cancel()
    }
}
