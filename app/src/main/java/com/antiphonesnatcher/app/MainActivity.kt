package com.antiphonesnatcher.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.antiphonesnatcher.app.licensing.LicenseValidator
import com.antiphonesnatcher.app.service.PrivacyOverlayService
import com.antiphonesnatcher.app.ui.screens.MainScreen
import com.antiphonesnatcher.app.ui.screens.OnboardingScreen
import com.antiphonesnatcher.app.ui.screens.SettingsScreen
import com.antiphonesnatcher.app.ui.screens.UnauthorizedScreen
import com.antiphonesnatcher.app.ui.theme.BorderSubtle
import com.antiphonesnatcher.app.ui.theme.AntiPhoneSnatcherTheme
import com.antiphonesnatcher.app.ui.theme.TextPrimary
import com.antiphonesnatcher.app.ui.theme.TextSecondary
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {

    private enum class AppScreen {
        MAIN,
        SETTINGS
    }

    private val isTamperAlertActive = MutableStateFlow(false)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra("EXTRA_TAMPER_ALERT", false)) {
            isTamperAlertActive.value = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (intent?.getBooleanExtra("EXTRA_TAMPER_ALERT", false) == true) {
            isTamperAlertActive.value = true
        }

        setContent {
            AntiPhoneSnatcherTheme {
                val prefs = PrivacyApp.instance.preferences
                val isProtectionArmed by prefs.isProtectionArmed.collectAsState()
                val isPrivacyEnabled by prefs.isPrivacyEnabled.collectAsState()
                val currentMode by prefs.privacyMode.collectAsState()
                val recoveryMethod by prefs.recoveryMethod.collectAsState()
                val recoveryPin by prefs.recoveryPin.collectAsState()
                val recoveryTapSequence by prefs.recoveryTapSequence.collectAsState()
                val muteAudioOnPanic by prefs.muteAudioOnPanic.collectAsState()
                val volumeDownAction by prefs.volumeDownAction.collectAsState()
                val volumeUpAction by prefs.volumeUpAction.collectAsState()
                val isHardwareShortcutEnabled by prefs.isHardwareShortcutEnabled.collectAsState()
                val isAntiSnatchEnabled by prefs.isAntiSnatchEnabled.collectAsState()
                val restoreOnBoot by prefs.restoreOnBoot.collectAsState()
                val hasCompletedOnboarding by prefs.hasCompletedOnboarding.collectAsState()
                val licenseStatus by LicenseValidator.licenseStatus.collectAsState()

                val showTamperAlert by isTamperAlertActive.collectAsState()
                var tamperPinInput by remember { mutableStateOf("") }
                var tamperError by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    LicenseValidator.verifyLicense(this@MainActivity)
                }

                var currentScreen by remember { mutableStateOf(AppScreen.MAIN) }

                val lifecycleOwner = LocalLifecycleOwner.current
                var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(this)) }

                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            hasOverlayPermission = Settings.canDrawOverlays(this@MainActivity)
                            LicenseValidator.verifyLicense(this@MainActivity)
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                // Hardware / System Navigation Back Button Handler
                // When in SETTINGS, system back press returns cleanly to MAIN instead of closing app!
                BackHandler(enabled = currentScreen != AppScreen.MAIN) {
                    currentScreen = AppScreen.MAIN
                }

                // If protection is armed and user has permission, ensure sentinel is running passively in background
                LaunchedEffect(hasCompletedOnboarding, hasOverlayPermission, isProtectionArmed, isAntiSnatchEnabled, licenseStatus) {
                    if (licenseStatus == LicenseValidator.LicenseStatus.LICENSED &&
                        hasCompletedOnboarding && hasOverlayPermission && isProtectionArmed && isAntiSnatchEnabled) {
                        PrivacyOverlayService.armSentinel(this@MainActivity)
                    }
                }

                if (licenseStatus == LicenseValidator.LicenseStatus.NOT_LICENSED) {
                    UnauthorizedScreen(
                        onRetryCheck = { LicenseValidator.verifyLicense(this@MainActivity) },
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (!hasCompletedOnboarding) {
                    OnboardingScreen(
                        hasOverlayPermission = hasOverlayPermission,
                        onRequestPermission = { requestOverlayPermission() },
                        currentMode = currentMode,
                        onModeChanged = { prefs.setPrivacyMode(it) },
                        recoveryMethod = recoveryMethod,
                        onRecoveryMethodChanged = { prefs.setRecoveryMethod(it) },
                        recoveryPin = recoveryPin,
                        onRecoveryPinChanged = { prefs.setRecoveryPin(it) },
                        recoveryTapSequence = recoveryTapSequence,
                        onRecoveryTapSequenceChanged = { prefs.setRecoveryTapSequence(it) },
                        onFinishOnboarding = {
                            prefs.setOnboardingCompleted(true)
                            prefs.setProtectionArmed(true)
                            if (Settings.canDrawOverlays(this@MainActivity)) {
                                PrivacyOverlayService.armSentinel(this@MainActivity)
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
                                    isProtectionArmed = isProtectionArmed,
                                    isPrivacyEnabled = isPrivacyEnabled,
                                    currentMode = currentMode,
                                    recoveryMethod = recoveryMethod,
                                    volumeDownAction = volumeDownAction,
                                    isHardwareShortcutEnabled = isHardwareShortcutEnabled,
                                    isMuteAudioEnabled = muteAudioOnPanic,
                                    isAntiSnatchEnabled = isAntiSnatchEnabled,
                                    onToggleArmed = {
                                        if (!Settings.canDrawOverlays(this@MainActivity)) {
                                            requestOverlayPermission()
                                            return@MainScreen
                                        }
                                        val nextArmed = !isProtectionArmed
                                        prefs.setProtectionArmed(nextArmed)
                                        if (nextArmed) {
                                            PrivacyOverlayService.armSentinel(this@MainActivity)
                                        } else {
                                            PrivacyOverlayService.disarmAll(this@MainActivity)
                                        }
                                    },
                                    onTriggerPanic = {
                                        if (!Settings.canDrawOverlays(this@MainActivity)) {
                                            requestOverlayPermission()
                                            return@MainScreen
                                        }
                                        PrivacyOverlayService.triggerPanic(this@MainActivity)
                                    },
                                    onDismissPanic = {
                                        PrivacyOverlayService.dismissPanic(this@MainActivity)
                                    },
                                    onModeChanged = { prefs.setPrivacyMode(it) },
                                    onNavigateToSettings = { currentScreen = AppScreen.SETTINGS },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            AppScreen.SETTINGS -> {
                                SettingsScreen(
                                    isHardwareShortcutEnabled = isHardwareShortcutEnabled,
                                    onToggleHardwareShortcut = { prefs.setHardwareShortcutEnabled(it) },
                                    volumeDownAction = volumeDownAction,
                                    onVolumeDownActionChanged = { prefs.setVolumeDownAction(it) },
                                    volumeUpAction = volumeUpAction,
                                    onVolumeUpActionChanged = { prefs.setVolumeUpAction(it) },
                                    isAntiSnatchEnabled = isAntiSnatchEnabled,
                                    onToggleAntiSnatch = {
                                        prefs.setAntiSnatchEnabled(it)
                                        if (it && isProtectionArmed && Settings.canDrawOverlays(this@MainActivity)) {
                                            PrivacyOverlayService.armSentinel(this@MainActivity)
                                        }
                                    },
                                    muteAudioOnPanic = muteAudioOnPanic,
                                    onToggleMuteAudio = { prefs.setMuteAudioOnPanic(it) },
                                    recoveryMethod = recoveryMethod,
                                    onRecoveryMethodChanged = { prefs.setRecoveryMethod(it) },
                                    recoveryPin = recoveryPin,
                                    onRecoveryPinChanged = { prefs.setRecoveryPin(it) },
                                    recoveryTapSequence = recoveryTapSequence,
                                    onRecoveryTapSequenceChanged = { prefs.setRecoveryTapSequence(it) },
                                    restoreOnBoot = restoreOnBoot,
                                    onToggleRestoreOnBoot = { prefs.setRestoreOnBoot(it) },
                                    onNavigateBack = { currentScreen = AppScreen.MAIN },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                // Emergency Tamper Alert Lockdown Dialog
                if (showTamperAlert) {
                    AlertDialog(
                        onDismissRequest = { /* Modal: Cannot dismiss without entering recovery PIN */ },
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFFF3B30),
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "TAMPER ALARM ACTIVE",
                                    color = Color(0xFFFF3B30),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                            }
                        },
                        text = {
                            Column {
                                Text(
                                    text = "Overlay permission was revoked while anti-theft defense was active. Tamper alarm is sounding.\n\nEnter your Secret Recovery PIN to silence the siren and disarm.",
                                    fontSize = 13.5.sp,
                                    color = TextPrimary,
                                    lineHeight = 19.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = tamperPinInput,
                                    onValueChange = {
                                        if (it.length <= 8) {
                                            tamperPinInput = it
                                            tamperError = false
                                        }
                                    },
                                    singleLine = true,
                                    isError = tamperError,
                                    label = { Text("Recovery PIN") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedBorderColor = Color(0xFFFF3B30),
                                        unfocusedBorderColor = BorderSubtle
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (tamperError) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Incorrect PIN. Alarm remains active.",
                                        color = Color(0xFFFF3B30),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val targetPin = prefs.getRecoveryPinSync()
                                    if (tamperPinInput == targetPin || (targetPin.isEmpty() && tamperPinInput == "1234")) {
                                        PrivacyOverlayService.stopTamperAlarm()
                                        prefs.setProtectionArmed(false)
                                        prefs.setPrivacyEnabled(false)
                                        isTamperAlertActive.value = false
                                        tamperPinInput = ""
                                    } else {
                                        tamperError = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF3B30),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Silence Alarm & Disarm", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        },
                        containerColor = Color(0xFF1C1C22),
                        shape = RoundedCornerShape(18.dp)
                    )
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