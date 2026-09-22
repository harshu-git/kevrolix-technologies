package com.antiphonesnatcher.app.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.antiphonesnatcher.app.MainActivity
import com.antiphonesnatcher.app.PrivacyApp
import com.antiphonesnatcher.app.R
import com.antiphonesnatcher.app.data.PanicRecoveryMethod
import com.antiphonesnatcher.app.data.PrivacyMode
import com.antiphonesnatcher.app.licensing.LicenseValidator
import com.antiphonesnatcher.app.ui.PanicLockActivity
import com.antiphonesnatcher.app.widget.PrivacyWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Core privacy foreground service.
 *
 * Runs continuously in the background when Anti-Snatch Sentinel Protection is Armed.
 * The phone screen remains 100% normal and fully usable while armed.
 *
 * When a snatch is detected by AntiSnatchSensorHelper (or triggered by volume shortcuts / widgets),
 * this service engages Panic Blackout Mode: instantly placing a modal pitch-black system overlay
 * and muting all media streams.
 *
 * During Blackout, the status bar and notification shade are continuously locked via
 * StatusBarLockHelper, and key events are consumed so no navigation buttons work.
 *
 * The unlock screen is only revealed when pressing Volume Up + Down together or performing
 * a secret gesture.
 */
class PrivacyOverlayService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var windowManager: WindowManager? = null
    private var overlayView: PrivacyOverlayView? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var isOverlayAttached = false
    private var audioHelper: AudioManagerHelper? = null
    private var antiSnatchHelper: AntiSnatchSensorHelper? = null
    private var collapseLoopJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        activeInstance = this
        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        audioHelper = AudioManagerHelper(this)

        antiSnatchHelper = AntiSnatchSensorHelper(this) {
            // Snatch detected! Instantly engage Panic Mode
            val prefs = PrivacyApp.instance.preferences
            if (prefs.isAntiSnatchEnabledSync() && !isOverlayAttached) {
                prefs.setPrivacyEnabled(true)
                ensureOverlayAttached()
                updateNotification(isBlackout = true)
                PrivacyWidgetProvider.updateAllWidgets(this@PrivacyOverlayService)
            }
        }

        val prefs = PrivacyApp.instance.preferences
        val isBlackout = prefs.isPrivacyEnabledSync()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    buildNotification(isBlackout),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    buildNotification(isBlackout),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE
                )
            } else {
                startForeground(NOTIFICATION_ID, buildNotification(isBlackout))
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            try {
                startForeground(NOTIFICATION_ID, buildNotification(isBlackout))
            } catch (_: Throwable) {}
        }

        observePreferences()
        startTamperWatchdog()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!LicenseValidator.isLicensed(this)) {
            stopOverlay()
            stopSelf()
            return START_NOT_STICKY
        }

        val prefs = PrivacyApp.instance.preferences

        when (intent?.action) {
            ACTION_TAMPER_ALERT -> {
                handlePermissionTamperDetected()
                return START_STICKY
            }

            ACTION_DISARM_ALL, ACTION_STOP_PRIVACY -> {
                isTamperAlarmActive = false
                audioHelper?.stopTamperAlarm()
                prefs.setPrivacyEnabled(false)
                prefs.setProtectionArmed(false)
                stopOverlay()
                stopSelf()
                PrivacyWidgetProvider.updateAllWidgets(this)
                return START_NOT_STICKY
            }

            ACTION_DISMISS_PANIC -> {
                isTamperAlarmActive = false
                audioHelper?.stopTamperAlarm()
                prefs.setPrivacyEnabled(false)
                stopOverlay()
                if (prefs.isAntiSnatchEnabledSync() && prefs.isProtectionArmedSync()) {
                    updateNotification(isBlackout = false)
                    antiSnatchHelper?.resetWarmup()
                    antiSnatchHelper?.startListening()
                } else {
                    stopSelf()
                }
                PrivacyWidgetProvider.updateAllWidgets(this)
                return START_STICKY
            }

            ACTION_TRIGGER_PANIC -> {
                prefs.setPrivacyEnabled(true)
                ensureOverlayAttached()
                updateNotification(isBlackout = true)
                PrivacyWidgetProvider.updateAllWidgets(this)
                return START_STICKY
            }

            ACTION_ARM_SENTINEL -> {
                prefs.setProtectionArmed(true)
                if (prefs.isAntiSnatchEnabledSync()) {
                    antiSnatchHelper?.startListening()
                }
                val isBlackout = prefs.isPrivacyEnabledSync()
                updateNotification(isBlackout)
                if (isBlackout) {
                    ensureOverlayAttached()
                }
                PrivacyWidgetProvider.updateAllWidgets(this)
                return START_STICKY
            }

            ACTION_TOGGLE_PRIVACY -> {
                val next = !prefs.isPrivacyEnabledSync()
                prefs.setPrivacyEnabled(next)
                if (next) {
                    ensureOverlayAttached()
                    updateNotification(isBlackout = true)
                } else {
                    stopOverlay()
                    updateNotification(isBlackout = false)
                    antiSnatchHelper?.resetWarmup()
                }
                PrivacyWidgetProvider.updateAllWidgets(this)
                return START_STICKY
            }

            else -> {
                // Default start: check if privacy was previously engaged (e.g. boot restore)
                if (prefs.isPrivacyEnabledSync()) {
                    ensureOverlayAttached()
                    updateNotification(isBlackout = true)
                } else {
                    updateNotification(isBlackout = false)
                }
                if (prefs.isAntiSnatchEnabledSync() && prefs.isProtectionArmedSync()) {
                    antiSnatchHelper?.startListening()
                }
                PrivacyWidgetProvider.updateAllWidgets(this)
                return START_STICKY
            }
        }
    }

    private fun observePreferences() {
        val prefs = PrivacyApp.instance.preferences

        serviceScope.launch {
            combine(
                prefs.privacyMode,
                prefs.strength,
                prefs.recoveryMethod,
                prefs.recoveryPin,
                prefs.recoveryTapSequence
            ) { mode, strength, recoveryMethod, pin, tapSeq ->
                Tuple5(mode, strength, recoveryMethod, pin, tapSeq)
            }.collect { (mode, strength, recoveryMethod, pin, tapSeq) ->
                updateOverlayAppearance(mode, strength, recoveryMethod, pin, tapSeq)
            }
        }

        serviceScope.launch {
            prefs.isAntiSnatchEnabled.collect { isEnabled ->
                if (isEnabled && prefs.isProtectionArmedSync()) {
                    antiSnatchHelper?.startListening()
                } else {
                    antiSnatchHelper?.stopListening()
                }
            }
        }

        serviceScope.launch {
            prefs.isProtectionArmed.collect { isArmed ->
                if (isArmed && prefs.isAntiSnatchEnabledSync()) {
                    antiSnatchHelper?.startListening()
                } else {
                    antiSnatchHelper?.stopListening()
                }
                PrivacyTileService.requestTileUpdate(this@PrivacyOverlayService)
                PrivacyWidgetProvider.updateAllWidgets(this@PrivacyOverlayService)
            }
        }

        serviceScope.launch {
            prefs.isPrivacyEnabled.collect { isBlackout ->
                if (isBlackout) {
                    ensureOverlayAttached()
                    updateNotification(isBlackout = true)
                } else {
                    stopOverlay()
                    updateNotification(isBlackout = false)
                }
                PrivacyTileService.requestTileUpdate(this@PrivacyOverlayService)
                PrivacyWidgetProvider.updateAllWidgets(this@PrivacyOverlayService)
            }
        }
    }

    private fun ensureOverlayAttached() {
        if (isOverlayAttached) return

        if (!Settings.canDrawOverlays(this)) {
            handlePermissionTamperDetected()
            return
        }

        val wm = windowManager ?: return
        val prefs = PrivacyApp.instance.preferences

        // 1. Instantly mute audio and pause media if enabled
        if (prefs.isMuteAudioOnPanicSync()) {
            audioHelper?.engageInstantMute()
        }

        val view = PrivacyOverlayView(this, onDismissRequest = {
            prefs.setPrivacyEnabled(false)
            stopOverlay()
            if (prefs.isAntiSnatchEnabledSync() && prefs.isProtectionArmedSync()) {
                updateNotification(isBlackout = false)
                antiSnatchHelper?.resetWarmup()
                antiSnatchHelper?.startListening()
            } else {
                stopSelf()
            }
            PrivacyWidgetProvider.updateAllWidgets(this@PrivacyOverlayService)
        })
        overlayView = view

        // Edge-to-edge system coverage & complete status bar lockdown
        StatusBarLockHelper.enforceImmersiveSticky(view)

        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val baseFlags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS or
                WindowManager.LayoutParams.FLAG_FULLSCREEN or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED

        val (screenWidth, screenHeight) = getPhysicalScreenDimensions(wm)

        val params = WindowManager.LayoutParams(
            screenWidth,
            screenHeight,
            windowType,
            baseFlags,
            PixelFormat.OPAQUE
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        layoutParams = params

        val currentMode = prefs.getPrivacyModeSync()
        val currentStrength = prefs.getStrengthSync()
        val recoveryMethod = prefs.getRecoveryMethodSync()
        val pin = prefs.getRecoveryPinSync()
        val tapSequence = prefs.getRecoveryTapSequenceListSync()

        view.updateConfiguration(currentMode, currentStrength, recoveryMethod, pin, tapSequence)

        try {
            wm.addView(view, params)
            isOverlayAttached = true
            StatusBarLockHelper.enforceImmersiveSticky(view)
            StatusBarLockHelper.collapsePanels(this)
            startCollapseSentinel()
        } catch (e: Exception) {
            e.printStackTrace()
            isOverlayAttached = false
        }

        // Fort Knox Defense: Launch full-screen PanicLockActivity to block notification shade
        PanicLockActivity.launch(this)
    }

    private fun startCollapseSentinel() {
        collapseLoopJob?.cancel()
        collapseLoopJob = serviceScope.launch(Dispatchers.Default) {
            while (isOverlayAttached) {
                StatusBarLockHelper.collapsePanels(this@PrivacyOverlayService)
                delay(450)
            }
        }
    }

    private fun stopCollapseSentinel() {
        collapseLoopJob?.cancel()
        collapseLoopJob = null
    }

    private var tamperWatchdogJob: Job? = null
    private var isTamperAlarmActive = false

    private fun startTamperWatchdog() {
        tamperWatchdogJob?.cancel()
        tamperWatchdogJob = serviceScope.launch(Dispatchers.Default) {
            val prefs = PrivacyApp.instance.preferences
            while (isActive) {
                delay(350)
                val isArmed = prefs.isProtectionArmedSync()
                val isBlackout = prefs.isPrivacyEnabledSync()

                if ((isArmed || isBlackout) && !Settings.canDrawOverlays(this@PrivacyOverlayService)) {
                    handlePermissionTamperDetected()
                    break
                }
            }
        }
    }

    private fun stopTamperWatchdog() {
        tamperWatchdogJob?.cancel()
        tamperWatchdogJob = null
    }

    private fun handlePermissionTamperDetected() {
        if (isTamperAlarmActive) return
        isTamperAlarmActive = true

        android.util.Log.w("PrivacyOverlayService", "TAMPER DETECTED: Overlay permission revoked while armed/in-blackout!")

        // 1. Immediately lock device screen using Accessibility Service if available
        PrivacyAccessibilityService.lockScreenNow()

        // 2. Play screaming tamper alarm siren at maximum volume
        audioHelper?.playTamperAlarm()

        // 3. Launch MainActivity with emergency lockdown challenge
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                action = ACTION_TAMPER_ALERT
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                putExtra("EXTRA_TAMPER_ALERT", true)
            }
            startActivity(intent)
        } catch (_: Exception) {}
    }

    fun revealPinPad() {
        overlayView?.revealUnlockPad()
    }

    private fun getPhysicalScreenDimensions(wm: WindowManager): Pair<Int, Int> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val bounds = wm.maximumWindowMetrics.bounds
                Pair(bounds.width(), bounds.height())
            } else {
                @Suppress("DEPRECATION")
                val display = wm.defaultDisplay
                val point = Point()
                @Suppress("DEPRECATION")
                display.getRealSize(point)
                Pair(point.x, point.y)
            }
        } catch (_: Exception) {
            Pair(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        }
    }

    private fun updateOverlayAppearance(
        mode: PrivacyMode,
        strength: Float,
        recoveryMethod: PanicRecoveryMethod,
        pin: String,
        tapSeq: String
    ) {
        val wm = windowManager ?: return
        val view = overlayView ?: return
        val params = layoutParams ?: return

        view.updateConfiguration(
            mode,
            strength,
            recoveryMethod,
            pin,
            com.antiphonesnatcher.app.data.TapSequenceHelper.parseSequence(tapSeq)
        )

        if (isOverlayAttached) {
            try {
                wm.updateViewLayout(view, params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun stopOverlay() {
        stopCollapseSentinel()
        PanicLockActivity.finishIfActive()

        // Restore muted audio streams when exiting Panic Mode
        audioHelper?.restoreAudio()

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

    private fun buildNotification(isBlackout: Boolean): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (isBlackout) {
            // Stealth notification: lowest priority, secret visibility, no actions, no alerts
            return NotificationCompat.Builder(this, PrivacyApp.CHANNEL_STEALTH_ID)
                .setSmallIcon(R.drawable.ic_privacy_tile)
                .setContentIntent(openAppPendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setShowWhen(false)
                .setContentTitle("Security Service")
                .setContentText("Protected mode active")
                .build()
        }

        val builder = NotificationCompat.Builder(this, PrivacyApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_privacy_tile)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        val triggerIntent = Intent(this, PrivacyOverlayService::class.java).apply {
            action = ACTION_TRIGGER_PANIC
        }
        val triggerPendingIntent = PendingIntent.getService(
            this,
            2,
            triggerIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        builder.setContentTitle("Anti-Snatch Shield Armed")
            .setContentText("Monitoring in background • Screen normal.")
            .addAction(
                R.drawable.ic_privacy_tile,
                "Trigger Panic",
                triggerPendingIntent
            )

        return builder.build()
    }

    private fun updateNotification(isBlackout: Boolean) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(NOTIFICATION_ID, buildNotification(isBlackout))
    }

    override fun onDestroy() {
        super.onDestroy()
        activeInstance = null
        stopTamperWatchdog()
        audioHelper?.stopTamperAlarm()
        stopCollapseSentinel()
        antiSnatchHelper?.stopListening()
        stopOverlay()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)

    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_ARM_SENTINEL = "com.antiphonesnatcher.app.ACTION_ARM_SENTINEL"
        const val ACTION_TRIGGER_PANIC = "com.antiphonesnatcher.app.ACTION_TRIGGER_PANIC"
        const val ACTION_DISMISS_PANIC = "com.antiphonesnatcher.app.ACTION_DISMISS_PANIC"
        const val ACTION_DISARM_ALL = "com.antiphonesnatcher.app.ACTION_DISARM_ALL"
        const val ACTION_STOP_PRIVACY = "com.antiphonesnatcher.app.ACTION_STOP_PRIVACY"
        const val ACTION_TOGGLE_PRIVACY = "com.antiphonesnatcher.app.ACTION_TOGGLE_PRIVACY"
        const val ACTION_TAMPER_ALERT = "com.antiphonesnatcher.app.ACTION_TAMPER_ALERT"

        private var activeInstance: PrivacyOverlayService? = null

        fun stopTamperAlarm() {
            activeInstance?.isTamperAlarmActive = false
            activeInstance?.audioHelper?.stopTamperAlarm()
        }

        fun revealPinPad() {
            activeInstance?.revealPinPad()
        }

        fun armSentinel(context: Context) {
            try {
                val intent = Intent(context, PrivacyOverlayService::class.java).apply {
                    action = ACTION_ARM_SENTINEL
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        fun triggerPanic(context: Context) {
            try {
                val intent = Intent(context, PrivacyOverlayService::class.java).apply {
                    action = ACTION_TRIGGER_PANIC
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        fun dismissPanic(context: Context) {
            val intent = Intent(context, PrivacyOverlayService::class.java).apply {
                action = ACTION_DISMISS_PANIC
            }
            context.startService(intent)
        }

        fun disarmAll(context: Context) {
            val intent = Intent(context, PrivacyOverlayService::class.java).apply {
                action = ACTION_DISARM_ALL
            }
            context.startService(intent)
        }

        // Backward-compatible aliases
        fun start(context: Context) = armSentinel(context)
        fun stop(context: Context) = disarmAll(context)
    }
}
