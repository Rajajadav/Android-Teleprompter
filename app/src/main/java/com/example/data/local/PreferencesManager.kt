package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "promptdesk_preferences")

class PreferencesManager(private val context: Context) {

    companion object {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val KEY_PROFILE_COMPLETED = booleanPreferencesKey("profile_completed")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        val KEY_USER_PHOTO_URL = stringPreferencesKey("user_photo_url")
        val KEY_USER_OCCUPATION = stringPreferencesKey("user_occupation")
        val KEY_USER_OTHER_OCCUPATION = stringPreferencesKey("user_other_occupation")
        val KEY_USER_USE_CASES = stringPreferencesKey("user_use_cases")
        val KEY_USER_REFERRAL = stringPreferencesKey("user_referral")
        val KEY_USER_OTHER_REFERRAL = stringPreferencesKey("user_other_referral")

        val KEY_DEFAULT_SPEED = floatPreferencesKey("default_speed")
        val KEY_DEFAULT_FONT_SIZE = floatPreferencesKey("default_font_size")
        val KEY_DEFAULT_ALIGNMENT = stringPreferencesKey("default_alignment")
        val KEY_DEFAULT_COUNTDOWN = intPreferencesKey("default_countdown")
        val KEY_DEFAULT_MIRROR = booleanPreferencesKey("default_mirror")
        val KEY_TEXT_WIDTH_PERCENT = intPreferencesKey("text_width_percent")
        val KEY_PARAGRAPH_SPACING = intPreferencesKey("paragraph_spacing")

        val KEY_VOICE_FOLLOW_ENABLED = booleanPreferencesKey("voice_follow_enabled")
        val KEY_VOICE_FOLLOW_LANG = stringPreferencesKey("voice_follow_lang")
        val KEY_VOICE_FOLLOW_SENSITIVITY = floatPreferencesKey("voice_follow_sensitivity")

        val KEY_CAMERA_DEFAULT_FRONT = booleanPreferencesKey("camera_default_front")
        val KEY_CAMERA_RESOLUTION = stringPreferencesKey("camera_resolution")
        val KEY_CAMERA_FPS = intPreferencesKey("camera_fps")
        val KEY_CAMERA_AUDIO_ENABLED = booleanPreferencesKey("camera_audio_enabled")

        val KEY_THEME_MODE = stringPreferencesKey("theme_mode") // DARK, LIGHT, SYSTEM
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_IS_LOGGED_IN] ?: false
    }

    val isProfileCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_PROFILE_COMPLETED] ?: false
    }

    val userName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_NAME] ?: "Raja Jadav"
    }

    val userEmail: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_EMAIL] ?: "rajajadavstudio@gmail.com"
    }

    val userPhotoUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_PHOTO_URL] ?: ""
    }

    val userOccupation: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_OCCUPATION] ?: "Content Creator"
    }

    val userOtherOccupation: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_OTHER_OCCUPATION] ?: ""
    }

    val userUseCases: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_USER_USE_CASES] ?: "YouTube Videos,Short Videos"
        if (raw.isBlank()) emptyList() else raw.split("|||")
    }

    val userReferral: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_REFERRAL] ?: ""
    }

    val userOtherReferral: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_OTHER_REFERRAL] ?: ""
    }

    suspend fun saveGoogleUser(name: String, email: String, photoUrl: String?) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = true
            prefs[KEY_USER_NAME] = name
            prefs[KEY_USER_EMAIL] = email
            if (!photoUrl.isNullOrBlank()) {
                prefs[KEY_USER_PHOTO_URL] = photoUrl
            }
        }
    }

    suspend fun saveProfileSetup(
        name: String,
        occupation: String,
        otherOccupation: String,
        useCases: List<String>,
        referral: String,
        otherReferral: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_NAME] = name
            prefs[KEY_USER_OCCUPATION] = occupation
            prefs[KEY_USER_OTHER_OCCUPATION] = otherOccupation
            prefs[KEY_USER_USE_CASES] = useCases.joinToString("|||")
            prefs[KEY_USER_REFERRAL] = referral
            prefs[KEY_USER_OTHER_REFERRAL] = otherReferral
            prefs[KEY_PROFILE_COMPLETED] = true
            prefs[KEY_ONBOARDING_COMPLETED] = true
            prefs[KEY_IS_LOGGED_IN] = true
        }
    }

    suspend fun signOut() {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = false
            prefs[KEY_PROFILE_COMPLETED] = false
            prefs[KEY_ONBOARDING_COMPLETED] = false
        }
    }

    val defaultSpeed: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_SPEED] ?: 1.3f
    }

    suspend fun setDefaultSpeed(speed: Float) {
        context.dataStore.edit { prefs -> prefs[KEY_DEFAULT_SPEED] = speed }
    }

    val defaultFontSize: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_FONT_SIZE] ?: 42f
    }

    suspend fun setDefaultFontSize(size: Float) {
        context.dataStore.edit { prefs -> prefs[KEY_DEFAULT_FONT_SIZE] = size }
    }

    val defaultAlignment: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_ALIGNMENT] ?: "CENTER"
    }

    suspend fun setDefaultAlignment(alignment: String) {
        context.dataStore.edit { prefs -> prefs[KEY_DEFAULT_ALIGNMENT] = alignment }
    }

    val defaultCountdown: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_COUNTDOWN] ?: 3
    }

    suspend fun setDefaultCountdown(seconds: Int) {
        context.dataStore.edit { prefs -> prefs[KEY_DEFAULT_COUNTDOWN] = seconds }
    }

    val defaultMirror: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_MIRROR] ?: false
    }

    suspend fun setDefaultMirror(mirror: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_DEFAULT_MIRROR] = mirror }
    }

    val textWidthPercent: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_TEXT_WIDTH_PERCENT] ?: 85
    }

    suspend fun setTextWidthPercent(percent: Int) {
        context.dataStore.edit { prefs -> prefs[KEY_TEXT_WIDTH_PERCENT] = percent }
    }

    val voiceFollowLang: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_VOICE_FOLLOW_LANG] ?: "en-US"
    }

    suspend fun setVoiceFollowLang(lang: String) {
        context.dataStore.edit { prefs -> prefs[KEY_VOICE_FOLLOW_LANG] = lang }
    }

    val voiceFollowSensitivity: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[KEY_VOICE_FOLLOW_SENSITIVITY] ?: 1.0f
    }

    suspend fun setVoiceFollowSensitivity(sensitivity: Float) {
        context.dataStore.edit { prefs -> prefs[KEY_VOICE_FOLLOW_SENSITIVITY] = sensitivity }
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_THEME_MODE] ?: "DARK"
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs -> prefs[KEY_THEME_MODE] = mode }
    }
}
