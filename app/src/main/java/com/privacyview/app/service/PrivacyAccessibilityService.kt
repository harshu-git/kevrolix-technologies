package com.privacyview.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.provider.Settings
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.privacyview.app.PrivacyApp

/**
 * Optional accessibility service to detect hardware button shortcuts (e.g. double-press of volume button).
 * OFF by default, strictly opt-in, with zero data extraction or screen reading capabilities.
 */
class PrivacyAccessibilityService : AccessibilityService() {

    private var lastVolumeDownTime = 0L

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event == null) return false

        val prefs = PrivacyApp.instance.preferences
        if (!prefs.isHardwareShortcutEnabledSync()) {
            return super.onKeyEvent(event)
        }

        if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            val now = System.currentTimeMillis()
            if (now - lastVolumeDownTime < DOUBLE_PRESS_WINDOW_MS) {
                // Double press detected!
                lastVolumeDownTime = 0L
                togglePrivacy()
                return true // Consume key event
            } else {
                lastVolumeDownTime = now
            }
        }

        return super.onKeyEvent(event)
    }

    private fun togglePrivacy() {
        if (!Settings.canDrawOverlays(this)) return

        val prefs = PrivacyApp.instance.preferences
        val newState = !prefs.isPrivacyEnabledSync()
        prefs.setPrivacyEnabled(newState)

        if (newState) {
            PrivacyOverlayService.start(this)
        } else {
            PrivacyOverlayService.stop(this)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No-op: We strictly do not observe screen content
    }

    override fun onInterrupt() {
        // Service interrupted
    }

    companion object {
        private const val DOUBLE_PRESS_WINDOW_MS = 450L
    }
}
