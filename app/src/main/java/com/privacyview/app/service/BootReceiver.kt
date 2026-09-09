package com.privacyview.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.privacyview.app.PrivacyApp

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent?.action != Intent.ACTION_BOOT_COMPLETED) return

        val prefs = PrivacyApp.instance.preferences
        val restore = prefs.restoreOnBoot.value
        val isEnabled = prefs.isPrivacyEnabledSync()

        if (restore && isEnabled && Settings.canDrawOverlays(context)) {
            PrivacyOverlayService.start(context)
        }
    }
}
