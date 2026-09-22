package com.antiphonesnatcher.app.data

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

    private val _isProtectionArmed = MutableStateFlow(prefs.getBoolean(KEY_PROTECTION_ARMED, true))
    val isProtectionArmed: StateFlow<Boolean> = _isProtectionArmed.asStateFlow()

    private val initialMode = PrivacyMode.fromName(
        prefs.getString(KEY_MODE, PrivacyMode.BLACKOUT.name)
    )
    private val _privacyMode = MutableStateFlow(initialMode)
    val privacyMode: StateFlow<PrivacyMode> = _privacyMode.asStateFlow()

    private val _strength = MutableStateFlow(prefs.getFloat(KEY_STRENGTH, 1.0f))
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

    private val _isPalmWaveEnabled = MutableStateFlow(prefs.getBoolean(KEY_PALM_WAVE, true))
    val isPalmWaveEnabled: StateFlow<Boolean> = _isPalmWaveEnabled.asStateFlow()

    private val _isAntiSnatchEnabled = MutableStateFlow(prefs.getBoolean(KEY_ANTI_SNATCH, true))
    val isAntiSnatchEnabled: StateFlow<Boolean> = _isAntiSnatchEnabled.asStateFlow()

    private val _isSentryAlertEnabled = MutableStateFlow(prefs.getBoolean(KEY_SENTRY_ALERT, false))
    val isSentryAlertEnabled: StateFlow<Boolean> = _isSentryAlertEnabled.asStateFlow()

    private val _recoveryMethod = MutableStateFlow(
        PanicRecoveryMethod.fromName(prefs.getString(KEY_RECOVERY_METHOD, PanicRecoveryMethod.SECRET_TAPS.name))
    )
    val recoveryMethod: StateFlow<PanicRecoveryMethod> = _recoveryMethod.asStateFlow()

    private val _recoveryPin = MutableStateFlow(prefs.getString(KEY_RECOVERY_PIN, "1234") ?: "1234")
    val recoveryPin: StateFlow<String> = _recoveryPin.asStateFlow()

    private val _recoveryTapSequence = MutableStateFlow(
        prefs.getString(KEY_RECOVERY_TAP_SEQUENCE, "") ?: ""
    )
    val recoveryTapSequence: StateFlow<String> = _recoveryTapSequence.asStateFlow()

    private val _muteAudioOnPanic = MutableStateFlow(prefs.getBoolean(KEY_MUTE_AUDIO, true))
    val muteAudioOnPanic: StateFlow<Boolean> = _muteAudioOnPanic.asStateFlow()

    private val _volumeDownAction = MutableStateFlow(
        PanicAction.fromName(prefs.getString(KEY_VOL_DOWN_ACTION, PanicAction.PANIC_BLACKOUT.name), PanicAction.PANIC_BLACKOUT)
    )
    val volumeDownAction: StateFlow<PanicAction> = _volumeDownAction.asStateFlow()

    private val _volumeUpAction = MutableStateFlow(
        PanicAction.fromName(prefs.getString(KEY_VOL_UP_ACTION, PanicAction.PANIC_BLACKOUT.name), PanicAction.PANIC_BLACKOUT)
    )
    val volumeUpAction: StateFlow<PanicAction> = _volumeUpAction.asStateFlow()

    private fun resolveSafeMode(mode: PrivacyMode): PrivacyMode = mode

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
        val stored = PrivacyMode.fromName(prefs.getString(KEY_MODE, PrivacyMode.BLACKOUT.name))
        return resolveSafeMode(stored)
    }

    fun setStrength(value: Float) {
        val clamped = value.coerceIn(0.15f, 1.0f)
        prefs.edit().putFloat(KEY_STRENGTH, clamped).apply()
        _strength.value = clamped
    }

    fun getStrengthSync(): Float {
        return prefs.getFloat(KEY_STRENGTH, 1.0f)
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

    fun setPalmWaveEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PALM_WAVE, enabled).apply()
        _isPalmWaveEnabled.value = enabled
    }

    fun isPalmWaveEnabledSync(): Boolean {
        return prefs.getBoolean(KEY_PALM_WAVE, true)
    }

    fun setAntiSnatchEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ANTI_SNATCH, enabled).apply()
        _isAntiSnatchEnabled.value = enabled
    }

    fun isAntiSnatchEnabledSync(): Boolean {
        return prefs.getBoolean(KEY_ANTI_SNATCH, true)
    }

    fun setSentryAlertEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SENTRY_ALERT, enabled).apply()
        _isSentryAlertEnabled.value = enabled
    }

    fun isSentryAlertEnabledSync(): Boolean {
        return prefs.getBoolean(KEY_SENTRY_ALERT, false)
    }

    fun setRecoveryMethod(method: PanicRecoveryMethod) {
        prefs.edit().putString(KEY_RECOVERY_METHOD, method.name).apply()
        _recoveryMethod.value = method
    }

    fun getRecoveryMethodSync(): PanicRecoveryMethod {
        return PanicRecoveryMethod.fromName(prefs.getString(KEY_RECOVERY_METHOD, PanicRecoveryMethod.SECRET_TAPS.name))
    }

    fun setRecoveryPin(pin: String) {
        prefs.edit().putString(KEY_RECOVERY_PIN, pin).apply()
        _recoveryPin.value = pin
    }

    fun getRecoveryPinSync(): String {
        return prefs.getString(KEY_RECOVERY_PIN, "1234") ?: "1234"
    }

    fun setRecoveryTapSequence(sequence: String) {
        prefs.edit().putString(KEY_RECOVERY_TAP_SEQUENCE, sequence).apply()
        _recoveryTapSequence.value = sequence
    }

    fun getRecoveryTapSequenceSync(): String {
        return prefs.getString(KEY_RECOVERY_TAP_SEQUENCE, "") ?: ""
    }

    fun getRecoveryTapSequenceListSync(): List<Int> {
        return TapSequenceHelper.parseSequence(getRecoveryTapSequenceSync())
    }

    fun setMuteAudioOnPanic(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MUTE_AUDIO, enabled).apply()
        _muteAudioOnPanic.value = enabled
    }

    fun isMuteAudioOnPanicSync(): Boolean {
        return prefs.getBoolean(KEY_MUTE_AUDIO, true)
    }

    fun setVolumeDownAction(action: PanicAction) {
        prefs.edit().putString(KEY_VOL_DOWN_ACTION, action.name).apply()
        _volumeDownAction.value = action
    }

    fun getVolumeDownActionSync(): PanicAction {
        return PanicAction.fromName(prefs.getString(KEY_VOL_DOWN_ACTION, PanicAction.PANIC_BLACKOUT.name), PanicAction.PANIC_BLACKOUT)
    }

    fun setVolumeUpAction(action: PanicAction) {
        prefs.edit().putString(KEY_VOL_UP_ACTION, action.name).apply()
        _volumeUpAction.value = action
    }

    fun getVolumeUpActionSync(): PanicAction {
        return PanicAction.fromName(prefs.getString(KEY_VOL_UP_ACTION, PanicAction.PANIC_BLACKOUT.name), PanicAction.PANIC_BLACKOUT)
    }

    fun setProtectionArmed(armed: Boolean) {
        prefs.edit().putBoolean(KEY_PROTECTION_ARMED, armed).apply()
        _isProtectionArmed.value = armed
    }

    fun isProtectionArmedSync(): Boolean {
        return prefs.getBoolean(KEY_PROTECTION_ARMED, true)
    }

    companion object {
        private const val PREFS_NAME = "privacy_view_prefs"
        private const val KEY_IS_ENABLED = "is_privacy_enabled"
        private const val KEY_PROTECTION_ARMED = "protection_armed"
        private const val KEY_MODE = "privacy_mode"
        private const val KEY_STRENGTH = "privacy_strength"
        private const val KEY_HARDWARE_SHORTCUT = "hardware_shortcut_enabled"
        private const val KEY_RESTORE_BOOT = "restore_on_boot"
        private const val KEY_ONBOARDING_DONE = "onboarding_completed"
        private const val KEY_PALM_WAVE = "palm_wave_enabled"
        private const val KEY_ANTI_SNATCH = "anti_snatch_enabled"
        private const val KEY_SENTRY_ALERT = "sentry_alert_enabled"
        private const val KEY_RECOVERY_METHOD = "recovery_method"
        private const val KEY_RECOVERY_PIN = "recovery_pin"
        private const val KEY_RECOVERY_TAP_SEQUENCE = "recovery_tap_sequence"
        private const val KEY_MUTE_AUDIO = "mute_audio_on_panic"
        private const val KEY_VOL_DOWN_ACTION = "vol_down_action"
        private const val KEY_VOL_UP_ACTION = "vol_up_action"
    }
}
