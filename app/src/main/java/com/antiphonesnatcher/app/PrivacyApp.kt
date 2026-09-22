package com.antiphonesnatcher.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.antiphonesnatcher.app.data.PrivacyPreferences

class PrivacyApp : Application() {

    lateinit var preferences: PrivacyPreferences
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val crashFile = java.io.File(getExternalFilesDir(null) ?: filesDir, "crash.txt")
                crashFile.writeText("CRASH on thread ${thread.name}: ${throwable.stackTraceToString()}\n")
            } catch (_: Throwable) {}
            defaultHandler?.uncaughtException(thread, throwable)
        }

        preferences = PrivacyPreferences(this)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java) ?: return

            val normalChannel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }
            manager.createNotificationChannel(normalChannel)

            val stealthChannel = NotificationChannel(
                CHANNEL_STEALTH_ID,
                "System Protection",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Silent system protection process"
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
                lockscreenVisibility = android.app.Notification.VISIBILITY_SECRET
            }
            manager.createNotificationChannel(stealthChannel)
        }
    }

    companion object {
        const val CHANNEL_ID = "privacy_view_status_channel"
        const val CHANNEL_STEALTH_ID = "privacy_view_stealth_channel"
        lateinit var instance: PrivacyApp
            private set
    }
}
