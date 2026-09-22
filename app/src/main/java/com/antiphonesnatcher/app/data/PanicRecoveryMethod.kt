package com.antiphonesnatcher.app.data

/**
 * Supported recovery methods to exit Panic Mode.
 */
enum class PanicRecoveryMethod(
    val displayName: String,
    val description: String
) {
    SECRET_TAPS(
        displayName = "Secret Tap Sequence",
        description = "Tap your custom 4-zone sequence on the dark screen to restore."
    ),
    PIN(
        displayName = "Custom PIN Code",
        description = "Requires entering your secret 4-digit PIN on a discreet pad."
    );

    companion object {
        fun fromName(name: String?): PanicRecoveryMethod {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: SECRET_TAPS
        }
    }
}

/**
 * Action executed when a hardware shortcut (e.g. Volume key double-press) is triggered.
 */
enum class PanicAction(
    val displayName: String,
    val description: String
) {
    PANIC_BLACKOUT(
        displayName = "Panic Blackout",
        description = "Immediately black out screen and mute all audio."
    ),
    CLOSE_CURRENT_APP(
        displayName = "Close Current App",
        description = "Instantly exit active application and return to Home screen."
    ),
    BOTH(
        displayName = "Blackout and Close App",
        description = "Exit active application and engage Panic Blackout simultaneously."
    ),
    NONE(
        displayName = "Disabled",
        description = "No action performed."
    );

    companion object {
        fun fromName(name: String?, default: PanicAction = PANIC_BLACKOUT): PanicAction {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: default
        }
    }
}
