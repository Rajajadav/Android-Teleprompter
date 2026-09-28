package com.example.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed class VoiceFollowStatus {
    object Idle : VoiceFollowStatus()
    object Listening : VoiceFollowStatus()
    data class WordMatched(val targetScrollFraction: Float, val recognizedPhrase: String) : VoiceFollowStatus()
    data class Error(val message: String) : VoiceFollowStatus()
}

class VoiceFollowManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var scriptWords: List<String> = emptyList()
    private var scriptLines: List<String> = emptyList()
    private var currentWordIndex = 0

    private val _status = MutableStateFlow<VoiceFollowStatus>(VoiceFollowStatus.Idle)
    val status: StateFlow<VoiceFollowStatus> = _status.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    var sensitivity: Float = 1.0f // 0.5 to 2.0
    var languageCode: String = "en-US" // "en-US", "hi-IN", "en-IN"

    fun setScript(content: String) {
        scriptLines = content.lines().filter { it.isNotBlank() }
        scriptWords = tokenize(content)
        currentWordIndex = 0
    }

    fun startListening(initialProgress: Float = 0f) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _status.value = VoiceFollowStatus.Error("Speech recognition is not available on this device.")
            return
        }

        stopListening()

        if (scriptWords.isNotEmpty()) {
            currentWordIndex = (initialProgress * scriptWords.size).toInt().coerceIn(0, scriptWords.size - 1)
        }

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createRecognitionListener())
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                putExtra("android.speech.extra.DICTATION_MODE", true)
            }

            speechRecognizer?.startListening(intent)
            _isListening.value = true
            _status.value = VoiceFollowStatus.Listening
        } catch (e: Exception) {
            Log.e("VoiceFollow", "Failed to start listening: ${e.message}")
            _status.value = VoiceFollowStatus.Error("Microphone initialization error: ${e.localizedMessage}")
            _isListening.value = false
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.w("VoiceFollow", "Error stopping recognizer: ${e.message}")
        } finally {
            speechRecognizer = null
            _isListening.value = false
            _status.value = VoiceFollowStatus.Idle
        }
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _status.value = VoiceFollowStatus.Listening
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                // Speech ended for current utterance; if user is still active in voice follow, restart seamlessly
                if (_isListening.value) {
                    restartListeningIfNeeded()
                }
            }

            override fun onError(error: Int) {
                // Error 7 (ERROR_NO_MATCH) or Error 6 (ERROR_SPEECH_TIMEOUT) are normal when speaker pauses
                if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                    if (_isListening.value) {
                        restartListeningIfNeeded()
                    }
                } else {
                    Log.w("VoiceFollow", "Recognition error code: $error")
                    if (_isListening.value) {
                        restartListeningIfNeeded()
                    }
                }
            }

            override fun onResults(results: Bundle?) {
                processSpeechBundle(results)
                if (_isListening.value) {
                    restartListeningIfNeeded()
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                processSpeechBundle(partialResults)
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    private fun restartListeningIfNeeded() {
        if (!_isListening.value) return
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.w("VoiceFollow", "Restart failed: ${e.message}")
        }
    }

    private fun processSpeechBundle(bundle: Bundle?) {
        val matches = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: return
        if (matches.isEmpty() || scriptWords.isEmpty()) return

        for (phrase in matches) {
            val spokenTokens = tokenize(phrase)
            if (spokenTokens.isEmpty()) continue

            val matchIndex = findFuzzyMatchInScript(spokenTokens)
            if (matchIndex != null && matchIndex >= currentWordIndex) {
                currentWordIndex = matchIndex
                val fraction = (currentWordIndex.toFloat() / scriptWords.size).coerceIn(0f, 1f)
                _status.value = VoiceFollowStatus.WordMatched(fraction, phrase)
                break
            }
        }
    }

    /**
     * Fuzzy scan forward in script within a 60-word sliding window from current position
     * Supports Hindi, English and Hinglish variations.
     */
    private fun findFuzzyMatchInScript(spokenWords: List<String>): Int? {
        val lookaheadWindow = (60 * sensitivity).toInt().coerceAtLeast(20)
        val startIdx = (currentWordIndex - 5).coerceAtLeast(0)
        val endIdx = (currentWordIndex + lookaheadWindow).coerceAtMost(scriptWords.size)

        for (i in startIdx until endIdx) {
            var matchCount = 0
            for (j in spokenWords.indices) {
                if (i + j < scriptWords.size) {
                    val scriptWord = scriptWords[i + j]
                    val spokenWord = spokenWords[j]
                    if (isWordSimilar(scriptWord, spokenWord)) {
                        matchCount++
                    }
                }
            }

            // If at least 2 consecutive words match or 1 long word matches well
            val threshold = if (spokenWords.size == 1) 1 else (spokenWords.size * 0.5f).toInt().coerceAtLeast(2)
            if (matchCount >= threshold) {
                return (i + spokenWords.size).coerceAtMost(scriptWords.size - 1)
            }
        }
        return null
    }

    private fun isWordSimilar(a: String, b: String): Boolean {
        if (a == b) return true
        if (a.contains(b) || b.contains(a)) return true
        // Levenshtein distance check for minor phonetics
        val distance = levenshtein(a, b)
        val maxLen = maxOf(a.length, b.length)
        return if (maxLen <= 3) distance == 0 else distance <= 2
    }

    private fun levenshtein(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[s1.length][s2.length]
    }

    private fun tokenize(text: String): List<String> {
        return text.lowercase(Locale.ROOT)
            .replace("[^\\p{L}\\p{Nd}]+".toRegex(), " ")
            .trim()
            .split("\\s+".toRegex())
            .filter { it.isNotEmpty() }
    }
}
