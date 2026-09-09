package com.privacyview.app.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PrivacyPreferences(context: Context) {

    private val appContext = context.applicationContext
    private val prefs: SharedPreferences = appContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _isPrivacyEnabled = MutableStateFlow(prefs.getBoolean(KEY_IS_ENABLED, false))
    val isPrivacyEnabled: StateFlow<Boolean> = _isPrivacyEnabled.asStateFlow()

    private val initialMode = resolveSafeMode(
        PrivacyMode.fromName(prefs.getString(KEY_MODE, PrivacyMode.DARK.name))
    )
    private val _privacyMode = MutableStateFlow(initialMode)
    val privacyMode: StateFlow<PrivacyMode> = _privacyMode.asStateFlow()

    private val _strength = MutableStateFlow(prefs.getFloat(KEY_STRENGTH, 0.60f))
    val strength: StateFlow<Float> = _strength.asStateFlow()

    private val _isHardwareShortcutEnabled = MutableStateFlow(
        prefs.getBoolean(KEY_HARDWARE_SHORTCUT, false)
    )
    val isHardwareShortcutEnabled: StateFlow<Boolean> = _isHardwareShortcutEnabled.asStateFlow()

    private val _restoreOnBoot = MutableStateFlow(prefs.getBoolean(KEY_RESTORE_BOOT, false))
    val restoreOnBoot: StateFlow<Boolean> = _restoreOnBoot.asStateFlow()

    private val _hasCompletedOnboarding = MutableStateFlow(
        prefs.getBoolean(KEY_ONBOARDING_DONE, false)
    )
    val hasCompletedOnboarding: StateFlow<Boolean> = _hasCompletedOnboarding.asStateFlow()

    /**
     * Resolves the requested mode against hardware capabilities.
     * If BLUR is chosen on an unsupported device, gracefully falls back to DARK.
     */
    private fun resolveSafeMode(mode: PrivacyMode): PrivacyMode {
        return if (mode == PrivacyMode.BLUR && !PrivacyMode.isBlurSupported(appContext)) {
            PrivacyMode.DARK
        } else {
            mode
        }
    }

    fun setPrivacyEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_IS_ENABLED, enabled).apply()
        _isPrivacyEnabled.value = enabled
    }

    fun isPrivacyEnabledSync(): Boolean {
        return prefs.getBoolean(KEY_IS_ENABLED, false)
    }

    fun setPrivacyMode(mode: PrivacyMode) {
        val safeMode = resolveSafeMode(mode)
        prefs.edit().putString(KEY_MODE, safeMode.name).apply()
        _privacyMode.value = safeMode
    }

    fun getPrivacyModeSync(): PrivacyMode {
        val stored = PrivacyMode.fromName(prefs.getString(KEY_MODE, PrivacyMode.DARK.name))
        return resolveSafeMode(stored)
    }

    fun setStrength(value: Float) {
        val clamped = value.coerceIn(0.15f, 0.95f)
        prefs.edit().putFloat(KEY_STRENGTH, clamped).apply()
        _strength.value = clamped
    }

    fun getStrengthSync(): Float {
        return prefs.getFloat(KEY_STRENGTH, 0.60f)
    }

    fun setHardwareShortcutEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HARDWARE_SHORTCUT, enabled).apply()
        _isHardwareShortcutEnabled.value = enabled
    }

    fun isHardwareShortcutEnabledSync(): Boolean {
        return prefs.getBoolean(KEY_HARDWARE_SHORTCUT, false)
    }

    fun setRestoreOnBoot(restore: Boolean) {
        prefs.edit().putBoolean(KEY_RESTORE_BOOT, restore).apply()
        _restoreOnBoot.value = restore
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, completed).apply()
        _hasCompletedOnboarding.value = completed
    }

    companion object {
        private const val PREFS_NAME = "privacy_view_prefs"
        private const val KEY_IS_ENABLED = "is_privacy_enabled"
        private const val KEY_MODE = "privacy_mode"
        private const val KEY_STRENGTH = "privacy_strength"
        private const val KEY_HARDWARE_SHORTCUT = "hardware_shortcut_enabled"
        private const val KEY_RESTORE_BOOT = "restore_on_boot"
        private const val KEY_ONBOARDING_DONE = "onboarding_completed"
    }
}
