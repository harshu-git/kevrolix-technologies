package com.antiphonesnatcher.app.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import com.antiphonesnatcher.app.PrivacyApp
import com.antiphonesnatcher.app.service.PrivacyOverlayService
import com.antiphonesnatcher.app.service.PrivacyOverlayView
import com.antiphonesnatcher.app.service.StatusBarLockHelper

/**
 * Dedicated fullscreen modal lock Activity for Panic Blackout.
 *
 * Runs as the primary system foreground surface when panic/snatch is triggered.
 * Blocks the notification shade, suppresses system edge gestures, and requires
 * the owner's Secret Recovery PIN or 4-zone Tap Sequence to exit.
 */
class PanicLockActivity : ComponentActivity() {

    private var overlayView: PrivacyOverlayView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activeInstance = this

        // Configure system lockscreen visibility & display cutout
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        // Intercept and absorb hardware Back press
        onBackPressedDispatcher.addCallback(this) {
            // Cannot exit Panic Blackout via Back button
        }

        // Attach PrivacyOverlayView as full-screen content
        val prefs = PrivacyApp.instance.preferences
        val view = PrivacyOverlayView(this, onDismissRequest = {
            PrivacyOverlayService.dismissPanic(this@PanicLockActivity)
            finish()
        })

        val currentMode = prefs.getPrivacyModeSync()
        val currentStrength = prefs.getStrengthSync()
        val recoveryMethod = prefs.getRecoveryMethodSync()
        val pin = prefs.getRecoveryPinSync()
        val tapSequence = prefs.getRecoveryTapSequenceListSync()

        view.updateConfiguration(currentMode, currentStrength, recoveryMethod, pin, tapSequence)
        overlayView = view

        setContentView(
            view,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        StatusBarLockHelper.enforceImmersiveSticky(window.decorView)
    }

    override fun onResume() {
        super.onResume()
        StatusBarLockHelper.enforceImmersiveSticky(window.decorView)
        StatusBarLockHelper.collapsePanels(this)
        StatusBarLockHelper.applySystemGestureExclusion(window.decorView)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) {
            // Notification shade or system dialog attempted to take focus: force snap shut!
            StatusBarLockHelper.collapsePanels(this)
            StatusBarLockHelper.enforceImmersiveSticky(window.decorView)
            window.decorView.postDelayed({
                StatusBarLockHelper.collapsePanels(this)
            }, 60L)
            window.decorView.postDelayed({
                StatusBarLockHelper.collapsePanels(this)
            }, 180L)
        } else {
            StatusBarLockHelper.enforceImmersiveSticky(window.decorView)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Swallowing volume and media keys to preserve lockdown integrity
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (activeInstance == this) {
            activeInstance = null
        }
        overlayView = null
    }

    companion object {
        private var activeInstance: PanicLockActivity? = null

        fun finishIfActive() {
            activeInstance?.finish()
            activeInstance = null
        }

        fun isRunning(): Boolean = activeInstance != null

        fun launch(context: Context) {
            try {
                val intent = Intent(context, PanicLockActivity::class.java).apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                                Intent.FLAG_ACTIVITY_NO_ANIMATION
                    )
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
