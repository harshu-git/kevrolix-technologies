package com.antiphonesnatcher.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.provider.Settings
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.antiphonesnatcher.app.PrivacyApp
import com.antiphonesnatcher.app.data.PanicAction

/**
 * Optional accessibility service to detect hardware button shortcuts (Volume Up / Down double-press).
 * OFF by default, strictly opt-in, with zero data extraction or screen reading capabilities.
 * Capable of instant Panic Blackout and closing active apps via system global action.
 */
class PrivacyAccessibilityService : AccessibilityService() {

    private var lastVolumeDownTime = 0L
    private var lastVolumeUpTime = 0L

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event == null) return false

        val prefs = PrivacyApp.instance.preferences

        // During active blackout, check for Volume Up + Down combo to reveal unlock screen!
        if (prefs.isPrivacyEnabledSync()) {
            if (event.action == KeyEvent.ACTION_DOWN) {
                val now = System.currentTimeMillis()
                if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    lastVolumeUpTime = now
                    if (now - lastVolumeDownTime < 450L) {
                        PrivacyOverlayService.revealPinPad()
                    }
                    return true // Consume volume key
                } else if (event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    lastVolumeDownTime = now
                    if (now - lastVolumeUpTime < 450L) {
                        PrivacyOverlayService.revealPinPad()
                    }
                    return true // Consume volume key
                }
            }
            return true // Consume other keys during blackout
        }

        if (!prefs.isHardwareShortcutEnabledSync()) {
            return super.onKeyEvent(event)
        }

        if (event.action == KeyEvent.ACTION_DOWN) {
            val now = System.currentTimeMillis()

            if (event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                if (now - lastVolumeDownTime < DOUBLE_PRESS_WINDOW_MS) {
                    lastVolumeDownTime = 0L
                    executePanicAction(prefs.getVolumeDownActionSync())
                    return true // Consume key event to prevent unwanted volume changes
                } else {
                    lastVolumeDownTime = now
                }
            } else if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                if (now - lastVolumeUpTime < DOUBLE_PRESS_WINDOW_MS) {
                    lastVolumeUpTime = 0L
                    executePanicAction(prefs.getVolumeUpActionSync())
                    return true // Consume key event
                } else {
                    lastVolumeUpTime = now
                }
            }
        }

        return super.onKeyEvent(event)
    }

    private fun executePanicAction(action: PanicAction) {
        when (action) {
            PanicAction.PANIC_BLACKOUT -> {
                engagePanicBlackout()
            }
            PanicAction.CLOSE_CURRENT_APP -> {
                closeCurrentApp()
            }
            PanicAction.BOTH -> {
                closeCurrentApp()
                engagePanicBlackout()
            }
            PanicAction.NONE -> {
                // Disabled
            }
        }
    }

    /**
     * Instantly closes the current foreground application by executing HOME global action.
     */
    private fun closeCurrentApp() {
        try {
            performGlobalAction(GLOBAL_ACTION_HOME)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Engages full pitch-black panic mode and audio silencing.
     */
    private fun engagePanicBlackout() {
        if (!Settings.canDrawOverlays(this)) return

        PrivacyOverlayService.triggerPanic(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val prefs = PrivacyApp.instance.preferences
        // When Panic Blackout is active, protect against SystemUI pulldowns and Settings tampering!
        if (prefs.isPrivacyEnabledSync()) {
            val pkg = event.packageName?.toString() ?: ""

            // 1. If notification shade or status bar opens, snap it shut immediately
            if (pkg.contains("systemui", ignoreCase = true)) {
                dismissNotificationShade()
            }

            // 2. CRITICAL TAMPER DEFENSE: If Settings or Permission Controller is opened,
            // immediately lock device with hardware lockscreen!
            if (pkg.contains("settings", ignoreCase = true) ||
                pkg.contains("permissioncontroller", ignoreCase = true) ||
                pkg.contains("packageinstaller", ignoreCase = true)) {

                lockScreenInternal()
                dismissNotificationShade()
            }
        }
    }

    private fun lockScreenInternal() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
            } else {
                performGlobalAction(GLOBAL_ACTION_HOME)
            }
        } catch (_: Exception) {}
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        activeInstance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        if (activeInstance == this) {
            activeInstance = null
        }
    }

    private fun dismissNotificationShade() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            try {
                performGlobalAction(GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE)
            } catch (_: Exception) {}
        } else {
            try {
                performGlobalAction(GLOBAL_ACTION_BACK)
            } catch (_: Exception) {}
        }
    }

    override fun onInterrupt() {
        // Service interrupted
    }

    companion object {
        private const val DOUBLE_PRESS_WINDOW_MS = 450L
        private var activeInstance: PrivacyAccessibilityService? = null

        fun dismissIfActive() {
            activeInstance?.dismissNotificationShade()
        }

        /**
         * Instantly locks device using hardware lockscreen (Android 9+)
         */
        fun lockScreenNow(): Boolean {
            val service = activeInstance ?: return false
            service.lockScreenInternal()
            return true
        }

        /**
         * Checks whether the user has enabled the Accessibility Sentinel in Android Settings.
         */
        fun isAccessibilityServiceEnabled(context: android.content.Context): Boolean {
            if (activeInstance != null) return true
            return try {
                val expectedServiceName = "${context.packageName}/${PrivacyAccessibilityService::class.java.canonicalName}"
                val enabledServices = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                ) ?: return false
                enabledServices.contains(expectedServiceName) || enabledServices.contains(context.packageName)
            } catch (_: Exception) {
                false
            }
        }
    }
}
