package com.privacyview.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.privacyview.app.data.PrivacyPreferences

class PrivacyApp : Application() {

    lateinit var preferences: PrivacyPreferences
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        preferences = PrivacyPreferences(this)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "privacy_view_status_channel"
        lateinit var instance: PrivacyApp
            private set
    }
}
