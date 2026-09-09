package com.privacyview.app.data

enum class PrivacyMode(
    val displayName: String,
    val description: String,
    val defaultStrength: Float
) {
    BLUR(
        displayName = "Blur",
        description = "High-frequency micro-louver dispersion for off-axis distortion",
        defaultStrength = 0.65f
    ),
    DARK(
        displayName = "Dark",
        description = "Polarized luminance clamping to suppress off-axis panel viewing cones",
        defaultStrength = 0.55f
    );

    companion object {
        fun fromName(name: String?): PrivacyMode {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: BLUR
        }
    }
}
