package com.antiphonesnatcher.app.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import java.lang.reflect.Method

/**
 * High-security utility to lock down the status bar and notification shade during Panic Blackout.
 *
 * Employs meta-reflection to bypass Android ART hidden API enforcement, allowing
 * StatusBarManager.collapsePanels() to execute cleanly without being blocked by the system.
 */
object StatusBarLockHelper {

    private var collapsePanelsMethod: Method? = null
    private var isMethodResolved = false

    @Synchronized
    private fun getCollapseMethod(): Method? {
        if (isMethodResolved) return collapsePanelsMethod
        isMethodResolved = true

        // 1. Direct reflection attempt
        try {
            val sbmClass = Class.forName("android.app.StatusBarManager")
            collapsePanelsMethod = try {
                sbmClass.getMethod("collapsePanels").apply { isAccessible = true }
            } catch (_: Exception) {
                sbmClass.getMethod("collapse").apply { isAccessible = true }
            }
        } catch (_: Exception) {
            collapsePanelsMethod = null
        }

        // 2. Meta-reflection bypass if direct reflection was blocked by ART hidden API policy
        if (collapsePanelsMethod == null) {
            try {
                val forName = Class::class.java.getDeclaredMethod("forName", String::class.java)
                val getDeclaredMethod = Class::class.java.getDeclaredMethod(
                    "getDeclaredMethod",
                    String::class.java,
                    arrayOf<Class<*>>()::class.java
                )
                val sbmClass = forName.invoke(null, "android.app.StatusBarManager") as Class<*>
                collapsePanelsMethod = try {
                    val m = getDeclaredMethod.invoke(sbmClass, "collapsePanels", emptyArray<Class<*>>()) as Method
                    m.isAccessible = true
                    m
                } catch (_: Exception) {
                    val m = getDeclaredMethod.invoke(sbmClass, "collapse", emptyArray<Class<*>>()) as Method
                    m.isAccessible = true
                    m
                }
            } catch (_: Exception) {
                collapsePanelsMethod = null
            }
        }

        return collapsePanelsMethod
    }

    /**
     * Instantly forces the notification shade and quick settings panels to snap shut.
     */
    fun collapsePanels(context: Context) {
        // 1. Accessibility service fast-dismiss
        PrivacyAccessibilityService.dismissIfActive()

        // 2. StatusBarManager reflection attempt
        try {
            @SuppressLint("WrongConstant")
            val statusBarService = context.getSystemService("statusbar") ?: return
            val method = getCollapseMethod()
            method?.invoke(statusBarService)
        } catch (_: Exception) {
            // Handled gracefully
        }

        // 3. Broadcast fallback for older Android versions & OEM customizations
        try {
            @Suppress("DEPRECATION")
            val closeDialogs = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            context.sendBroadcast(closeDialogs)
        } catch (_: Exception) {
            // Handled gracefully on Android 12+
        }
    }

    /**
     * Enforces complete fullscreen immersive lockdown mode, hiding the status bar,
     * navigation bar, and preventing status bar pull-down gestures.
     */
    fun enforceImmersiveSticky(view: View) {
        @Suppress("DEPRECATION")
        view.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.windowInsetsController?.let { controller ->
                controller.hide(
                    WindowInsets.Type.statusBars()
                    or WindowInsets.Type.navigationBars()
                    or WindowInsets.Type.systemGestures()
                    or WindowInsets.Type.captionBar()
                    or WindowInsets.Type.displayCutout()
                )
                // BEHAVIOR_DEFAULT prevents edge-swipes from revealing transient bars
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_DEFAULT
            }
        }

        applySystemGestureExclusion(view)
    }

    /**
     * Excludes the top edge and system gesture areas (Android 10+),
     * suppressing Android's edge gesture detector from opening the notification bar.
     */
    fun applySystemGestureExclusion(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val w = if (view.width > 0) view.width else view.resources.displayMetrics.widthPixels
            val h = if (view.height > 0) view.height else view.resources.displayMetrics.heightPixels
            if (w > 0 && h > 0) {
                try {
                    val density = view.resources.displayMetrics.density
                    val topEdgePx = (60 * density).toInt().coerceAtMost(h / 6)
                    val bottomEdgePx = (48 * density).toInt().coerceAtMost(h / 6)

                    val rects = listOf(
                        android.graphics.Rect(0, 0, w, topEdgePx),
                        android.graphics.Rect(0, h - bottomEdgePx, w, h)
                    )
                    view.systemGestureExclusionRects = rects
                } catch (_: Exception) {
                    // Handled gracefully on OEM ROMs with strict rect count limits
                }
            }
        }
    }
}
