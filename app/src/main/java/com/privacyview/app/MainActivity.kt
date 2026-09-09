package com.privacyview.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.privacyview.app.data.PrivacyMode
import com.privacyview.app.service.PrivacyOverlayService
import com.privacyview.app.ui.screens.CalibrationScreen
import com.privacyview.app.ui.screens.MainScreen
import com.privacyview.app.ui.screens.OnboardingScreen
import com.privacyview.app.ui.screens.SettingsScreen
import com.privacyview.app.ui.theme.PrivacyViewTheme

class MainActivity : ComponentActivity() {

    private enum class AppScreen {
        MAIN,
        CALIBRATION,
        SETTINGS
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            PrivacyViewTheme {
                val prefs = PrivacyApp.instance.preferences
                val isEnabled by prefs.isPrivacyEnabled.collectAsState()
                val currentMode by prefs.privacyMode.collectAsState()
                val currentStrength by prefs.strength.collectAsState()
                val isHardwareShortcutEnabled by prefs.isHardwareShortcutEnabled.collectAsState()
                val restoreOnBoot by prefs.restoreOnBoot.collectAsState()
                val hasCompletedOnboarding by prefs.hasCompletedOnboarding.collectAsState()

                var currentScreen by remember { mutableStateOf(AppScreen.MAIN) }
                val hasOverlayPermission = Settings.canDrawOverlays(this)

                if (!hasCompletedOnboarding) {
                    OnboardingScreen(
                        hasOverlayPermission = hasOverlayPermission,
                        onRequestPermission = { requestOverlayPermission() },
                        currentMode = currentMode,
                        currentStrength = currentStrength,
                        onModeChanged = { prefs.setPrivacyMode(it) },
                        onStrengthChanged = { prefs.setStrength(it) },
                        onFinishOnboarding = {
                            prefs.setOnboardingCompleted(true)
                            if (hasOverlayPermission) {
                                prefs.setPrivacyEnabled(true)
                                PrivacyOverlayService.start(this)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "screen_transition"
                    ) { screen ->
                        when (screen) {
                            AppScreen.MAIN -> {
                                MainScreen(
                                    isPrivacyEnabled = isEnabled,
                                    currentMode = currentMode,
                                    currentStrength = currentStrength,
                                    onTogglePrivacy = {
                                        if (!Settings.canDrawOverlays(this@MainActivity)) {
                                            requestOverlayPermission()
                                            return@MainScreen
                                        }
                                        val nextState = !isEnabled
                                        prefs.setPrivacyEnabled(nextState)
                                        if (nextState) {
                                            PrivacyOverlayService.start(this@MainActivity)
                                        } else {
                                            PrivacyOverlayService.stop(this@MainActivity)
                                        }
                                    },
                                    onModeChanged = { prefs.setPrivacyMode(it) },
                                    onStrengthChanged = { prefs.setStrength(it) },
                                    onNavigateToCalibration = { currentScreen = AppScreen.CALIBRATION },
                                    onNavigateToSettings = { currentScreen = AppScreen.SETTINGS },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            AppScreen.CALIBRATION -> {
                                CalibrationScreen(
                                    currentMode = currentMode,
                                    currentStrength = currentStrength,
                                    onModeChanged = { prefs.setPrivacyMode(it) },
                                    onStrengthChanged = { prefs.setStrength(it) },
                                    onNavigateBack = { currentScreen = AppScreen.MAIN },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            AppScreen.SETTINGS -> {
                                SettingsScreen(
                                    isHardwareShortcutEnabled = isHardwareShortcutEnabled,
                                    onToggleHardwareShortcut = { prefs.setHardwareShortcutEnabled(it) },
                                    restoreOnBoot = restoreOnBoot,
                                    onToggleRestoreOnBoot = { prefs.setRestoreOnBoot(it) },
                                    onNavigateToCalibration = { currentScreen = AppScreen.CALIBRATION },
                                    onNavigateBack = { currentScreen = AppScreen.MAIN },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun requestOverlayPermission() {
        try {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        } catch (_: Exception) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            startActivity(intent)
        }
    }
}
