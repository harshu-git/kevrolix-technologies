package com.privacyview.app.data

import android.content.Context
import android.os.Build
import android.view.WindowManager

/**
 * PrivacyView operating modes.
 *
 * DARK: The universal baseline filter. Works across all supported Android versions (Android 8.0 to 15/16).
 *       Applies calibrated obsidian luminance clamping to exploit the natural 50%-70% off-axis
 *       luminance falloff of mobile OLED and LCD displays.
 *
 * BLUR: Enhanced privacy filter for Android 12+ (API 31+) devices supporting WindowManager.FLAG_BLUR_BEHIND.
 *       Directly engages system GPU compositor to blur background applications. Gracefully falls back
 *       to DARK mode if cross-window blur is unsupported or disabled (e.g. Battery Saver mode).
 */
enum class PrivacyMode(
    val displayName: String,
    val subtitle: String,
    val description: String,
    val defaultStrength: Float
) {
    DARK(
        displayName = "Dark",
        subtitle = "Universal Baseline",
        description = "Calibrated luminance clamping that exploits natural OLED/LCD off-axis contrast falloff.",
        defaultStrength = 0.55f
    ),
    BLUR(
        displayName = "Blur",
        subtitle = "Enhanced (Android 12+)",
        description = "Hardware cross-window blur powered by the Android GPU compositor.",
        defaultStrength = 0.65f
    );

    companion object {
        fun fromName(name: String?): PrivacyMode {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: DARK
        }

        /**
         * Checks whether the device hardware and Android OS version support true cross-window blur.
         */
        fun isBlurSupported(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                return false
            }
            val wm = context.getSystemService(WindowManager::class.java) ?: return false
            return try {
                wm.isCrossWindowBlurEnabled
            } catch (_: Exception) {
                false
            }
        }
    }
}
