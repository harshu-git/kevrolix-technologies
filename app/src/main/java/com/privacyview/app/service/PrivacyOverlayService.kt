package com.privacyview.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.privacyview.app.MainActivity
import com.privacyview.app.PrivacyApp
import com.privacyview.app.R
import com.privacyview.app.data.PrivacyMode
import com.privacyview.app.widget.PrivacyWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class PrivacyOverlayService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var windowManager: WindowManager? = null
    private var overlayView: PrivacyOverlayView? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var isOverlayAttached = false

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        startForegroundServiceNotification()
        observePreferences()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_PRIVACY -> {
                PrivacyApp.instance.preferences.setPrivacyEnabled(false)
                stopOverlay()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_TOGGLE_PRIVACY -> {
                val current = PrivacyApp.instance.preferences.isPrivacyEnabledSync()
                val next = !current
                PrivacyApp.instance.preferences.setPrivacyEnabled(next)
                if (!next) {
                    stopOverlay()
                    stopSelf()
                    return START_NOT_STICKY
                }
            }
        }

        ensureOverlayAttached()
        PrivacyWidgetProvider.updateAllWidgets(this)
        return START_STICKY
    }

    private fun observePreferences() {
        val prefs = PrivacyApp.instance.preferences
        serviceScope.launch {
            combine(prefs.privacyMode, prefs.strength) { mode, strength ->
                Pair(mode, strength)
            }.collect { (mode, strength) ->
                updateOverlayAppearance(mode, strength)
            }
        }

        serviceScope.launch {
            prefs.isPrivacyEnabled.collect { isEnabled ->
                if (isEnabled) {
                    ensureOverlayAttached()
                } else {
                    stopOverlay()
                    stopSelf()
                }
                PrivacyWidgetProvider.updateAllWidgets(this@PrivacyOverlayService)
            }
        }
    }

    private fun ensureOverlayAttached() {
        if (isOverlayAttached) return

        if (!Settings.canDrawOverlays(this)) {
            PrivacyApp.instance.preferences.setPrivacyEnabled(false)
            stopSelf()
            return
        }

        val wm = windowManager ?: return
        val view = PrivacyOverlayView(this)
        overlayView = view

        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        var baseFlags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS

        // On Android 12+ (API 31+), enable true cross-window blur behind the overlay if supported
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (wm.isCrossWindowBlurEnabled) {
                baseFlags = baseFlags or WindowManager.LayoutParams.FLAG_BLUR_BEHIND
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            windowType,
            baseFlags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        layoutParams = params

        val prefs = PrivacyApp.instance.preferences
        val currentMode = prefs.getPrivacyModeSync()
        val currentStrength = prefs.getStrengthSync()

        applyBlurAndTint(params, currentMode, currentStrength)
        view.updateConfiguration(currentMode, currentStrength)

        try {
            wm.addView(view, params)
            isOverlayAttached = true
        } catch (e: Exception) {
            e.printStackTrace()
            isOverlayAttached = false
        }
    }

    private fun updateOverlayAppearance(mode: PrivacyMode, strength: Float) {
        val wm = windowManager ?: return
        val view = overlayView ?: return
        val params = layoutParams ?: return

        applyBlurAndTint(params, mode, strength)
        view.updateConfiguration(mode, strength)

        if (isOverlayAttached) {
            try {
                wm.updateViewLayout(view, params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun applyBlurAndTint(params: WindowManager.LayoutParams, mode: PrivacyMode, strength: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val wm = windowManager
            if (wm != null && wm.isCrossWindowBlurEnabled && mode == PrivacyMode.BLUR) {
                params.flags = params.flags or WindowManager.LayoutParams.FLAG_BLUR_BEHIND
                val blurRadius = (strength * 40f).roundToInt().coerceIn(4, 80)
                params.setBlurRadius(blurRadius)
            } else {
                params.flags = params.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv()
                params.setBlurRadius(0)
            }
        }
    }

    private fun stopOverlay() {
        if (isOverlayAttached && overlayView != null) {
            try {
                windowManager?.removeView(overlayView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isOverlayAttached = false
            overlayView = null
            layoutParams = null
        }
    }

    private fun startForegroundServiceNotification() {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val turnOffIntent = Intent(this, PrivacyOverlayService::class.java).apply {
            action = ACTION_STOP_PRIVACY
        }
        val turnOffPendingIntent = PendingIntent.getService(
            this,
            1,
            turnOffIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, PrivacyApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_privacy_tile)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                R.drawable.ic_privacy_tile,
                getString(R.string.turn_off_privacy),
                turnOffPendingIntent
            )
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        stopOverlay()
        PrivacyWidgetProvider.updateAllWidgets(this)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val NOTIFICATION_ID = 4201
        const val ACTION_START_PRIVACY = "com.privacyview.app.ACTION_START_PRIVACY"
        const val ACTION_STOP_PRIVACY = "com.privacyview.app.ACTION_STOP_PRIVACY"
        const val ACTION_TOGGLE_PRIVACY = "com.privacyview.app.ACTION_TOGGLE_PRIVACY"

        fun start(context: Context) {
            val intent = Intent(context, PrivacyOverlayService::class.java).apply {
                action = ACTION_START_PRIVACY
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, PrivacyOverlayService::class.java).apply {
                action = ACTION_STOP_PRIVACY
            }
            context.startService(intent)
        }

        fun toggle(context: Context) {
            val intent = Intent(context, PrivacyOverlayService::class.java).apply {
                action = ACTION_TOGGLE_PRIVACY
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
