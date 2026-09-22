package com.antiphonesnatcher.app.data

import android.content.Context

/**
 * Stealth Shield Modes for Anti Phone Snatcher.
 *
 * BLACKOUT: True 0-power OLED pure black screen. Looks turned off. Double tap wakes instantly.
 * DECOY_UPDATE: Realistic minimalist Android system update ("Installing system security update... 47%").
 * DECOY_CLOCK: Ultra-clean minimal standby lock clock. Completely masks your sensitive apps underneath.
 */
enum class PrivacyMode(
    val displayName: String,
    val subtitle: String,
    val description: String,
    val defaultStrength: Float
) {
    BLACKOUT(
        displayName = "OLED Blackout",
        subtitle = "Zero-Power Stealth",
        description = "Screen appears completely turned off. Double-tap wakes your screen instantly.",
        defaultStrength = 1.0f
    ),
    DECOY_UPDATE(
        displayName = "System Update",
        subtitle = "Authentic Camouflage",
        description = "Displays a hyper-realistic Android update screen to completely fool onlookers.",
        defaultStrength = 1.0f
    ),
    DECOY_CLOCK(
        displayName = "Minimal Clock",
        subtitle = "Discreet Standby",
        description = "Shows an elegant, minimal night clock while keeping your active apps hidden.",
        defaultStrength = 0.95f
    );

    companion object {
        fun fromName(name: String?): PrivacyMode {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: BLACKOUT
        }

        fun isBlurSupported(context: Context): Boolean = false
    }
}
