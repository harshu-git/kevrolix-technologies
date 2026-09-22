package com.antiphonesnatcher.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.antiphonesnatcher.app.PrivacyApp

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent?.action != Intent.ACTION_BOOT_COMPLETED) return

        val prefs = PrivacyApp.instance.preferences
        val restore = prefs.restoreOnBoot.value
        val isArmed = prefs.isProtectionArmedSync()
        val isBlackout = prefs.isPrivacyEnabledSync()

        if (restore && Settings.canDrawOverlays(context)) {
            if (isBlackout) {
                PrivacyOverlayService.triggerPanic(context)
            } else if (isArmed && prefs.isAntiSnatchEnabledSync()) {
                PrivacyOverlayService.armSentinel(context)
            }
        }
    }
}
