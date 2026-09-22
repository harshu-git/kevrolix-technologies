package com.antiphonesnatcher.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MusicOff
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.VolumeDown
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.antiphonesnatcher.app.ui.components.AccessibilityDisclosureDialog
import com.antiphonesnatcher.app.service.PrivacyAccessibilityService
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antiphonesnatcher.app.data.PanicAction
import com.antiphonesnatcher.app.data.PanicRecoveryMethod
import com.antiphonesnatcher.app.data.TapSequenceHelper
import com.antiphonesnatcher.app.ui.components.SequenceRecorderView
import com.antiphonesnatcher.app.ui.theme.ActiveGreen
import com.antiphonesnatcher.app.ui.theme.BackgroundDark
import com.antiphonesnatcher.app.ui.theme.BorderSubtle
import com.antiphonesnatcher.app.ui.theme.SurfaceCard
import com.antiphonesnatcher.app.ui.theme.TextPrimary
import com.antiphonesnatcher.app.ui.theme.TextSecondary
import com.antiphonesnatcher.app.ui.theme.Typography

@Composable
fun SettingsScreen(
    isHardwareShortcutEnabled: Boolean,
    onToggleHardwareShortcut: (Boolean) -> Unit,
    volumeDownAction: PanicAction,
    onVolumeDownActionChanged: (PanicAction) -> Unit,
    volumeUpAction: PanicAction,
    onVolumeUpActionChanged: (PanicAction) -> Unit,
    isAntiSnatchEnabled: Boolean,
    onToggleAntiSnatch: (Boolean) -> Unit,
    muteAudioOnPanic: Boolean,
    onToggleMuteAudio: (Boolean) -> Unit,
    recoveryMethod: PanicRecoveryMethod,
    onRecoveryMethodChanged: (PanicRecoveryMethod) -> Unit,
    recoveryPin: String,
    onRecoveryPinChanged: (String) -> Unit,
    recoveryTapSequence: String = TapSequenceHelper.DEFAULT_SEQUENCE,
    onRecoveryTapSequenceChanged: (String) -> Unit = {},
    restoreOnBoot: Boolean,
    onToggleRestoreOnBoot: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showPinDialog by remember { mutableStateOf(false) }
    var pinInputValue by remember { mutableStateOf(recoveryPin) }
    var showAccessibilityDisclosure by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 40.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SurfaceCard)
                        .border(1.dp, BorderSubtle, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Panic Configuration",
                        style = Typography.headlineMedium
                    )
                    Text(
                        text = "Instant emergency triggers and secure recovery",
                        style = Typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // SECTION 1: Automatic Anti-Snatch Theft Detection
            Text(
                text = "Autonomous Sensor Protection",
                style = Typography.titleMedium,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Anti-Snatch Protection",
                                style = Typography.titleMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Instantly blackout and lock phone if snatched from your hand",
                                style = Typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = isAntiSnatchEnabled,
                            onCheckedChange = onToggleAntiSnatch,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF000000),
                                checkedTrackColor = Color(0xFFFFFFFF),
                                uncheckedThumbColor = Color(0xFF8E8E93),
                                uncheckedTrackColor = Color(0xFF2C2C30)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Uses on-device high-speed accelerometer and gyroscope to detect sudden violent acceleration spikes and rapid hand pulls. Requires no user interaction.",
                        style = Typography.labelSmall,
                        color = Color(0xFF7E7E86)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 2: Hardware Volume Shortcuts
            Text(
                text = "Hardware Button Triggers",
                style = Typography.titleMedium,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable Volume Double-Press",
                                style = Typography.titleMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Trigger emergency action without waking or unlocking",
                                style = Typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = isHardwareShortcutEnabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    val isServiceEnabled = PrivacyAccessibilityService.isAccessibilityServiceEnabled(context)
                                    if (!isServiceEnabled) {
                                        showAccessibilityDisclosure = true
                                    } else {
                                        onToggleHardwareShortcut(true)
                                    }
                                } else {
                                    onToggleHardwareShortcut(false)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF000000),
                                checkedTrackColor = Color(0xFFFFFFFF),
                                uncheckedThumbColor = Color(0xFF8E8E93),
                                uncheckedTrackColor = Color(0xFF2C2C30)
                            )
                        )
                    }

                    if (isHardwareShortcutEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(BorderSubtle)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Double Press Volume Down Action Selector
                        Text(
                            text = "Double-Press Volume Down",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ActionPillGroup(
                            selectedAction = volumeDownAction,
                            options = listOf(PanicAction.PANIC_BLACKOUT, PanicAction.CLOSE_CURRENT_APP, PanicAction.BOTH),
                            onSelect = onVolumeDownActionChanged
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Double Press Volume Up Action Selector
                        Text(
                            text = "Double-Press Volume Up",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ActionPillGroup(
                            selectedAction = volumeUpAction,
                            options = listOf(PanicAction.PANIC_BLACKOUT, PanicAction.CLOSE_CURRENT_APP, PanicAction.NONE),
                            onSelect = onVolumeUpActionChanged
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 3: Audio Mute & Media Protection
            Text(
                text = "Audio and Media Muting",
                style = Typography.titleMedium,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Instant Audio Mute",
                            style = Typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Immediately silence calls, music, and videos when Panic Mode starts",
                            style = Typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = muteAudioOnPanic,
                        onCheckedChange = onToggleMuteAudio,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF000000),
                            checkedTrackColor = Color(0xFFFFFFFF),
                            uncheckedThumbColor = Color(0xFF8E8E93),
                            uncheckedTrackColor = Color(0xFF2C2C30)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 4: Secure Recovery Mechanism
            Text(
                text = "Secure Recovery Method",
                style = Typography.titleMedium,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Text(
                        text = "Choose how to exit Panic Blackout:",
                        style = Typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        RecoveryMethodPill(
                            title = "Secret Sequence",
                            subtitle = "Custom 4-zone pattern",
                            isSelected = recoveryMethod == PanicRecoveryMethod.SECRET_TAPS,
                            modifier = Modifier.weight(1f),
                            onClick = { onRecoveryMethodChanged(PanicRecoveryMethod.SECRET_TAPS) }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        RecoveryMethodPill(
                            title = "Secret PIN",
                            subtitle = "Discreet keypad",
                            isSelected = recoveryMethod == PanicRecoveryMethod.PIN,
                            modifier = Modifier.weight(1f),
                            onClick = { onRecoveryMethodChanged(PanicRecoveryMethod.PIN) }
                        )
                    }

                    if (recoveryMethod == PanicRecoveryMethod.SECRET_TAPS) {
                        Spacer(modifier = Modifier.height(14.dp))
                        SequenceRecorderView(
                            currentSequence = recoveryTapSequence,
                            onSequenceSaved = onRecoveryTapSequenceChanged
                        )
                    } else if (recoveryMethod == PanicRecoveryMethod.PIN) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF202026))
                                .clickable {
                                    pinInputValue = recoveryPin
                                    showPinDialog = true
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Current PIN",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "• • • •",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E599)
                                )
                            }
                            Text(
                                text = "Change PIN",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF00E599)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Secret Wake Trigger: Tap or hold with 2 fingers on screen, or press Volume Up + Down together to reveal the PIN pad. Single taps remain black to protect privacy.",
                            fontSize = 12.sp,
                            color = Color(0xFF00E599),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 5: System Persistence
            Text(
                text = "System Behavior",
                style = Typography.titleMedium,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Restore on Reboot",
                            style = Typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Automatically re-engage Panic Shield if device was restarted while active",
                            style = Typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = restoreOnBoot,
                        onCheckedChange = onToggleRestoreOnBoot,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF000000),
                            checkedTrackColor = Color(0xFFFFFFFF),
                            uncheckedThumbColor = Color(0xFF8E8E93),
                            uncheckedTrackColor = Color(0xFF2C2C30)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Privacy & Legal Section
            Text(
                text = "Legal & Security Disclosures",
                style = Typography.titleMedium,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                    .clickable { showPrivacyPolicyDialog = true }
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF14241C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Policy,
                            contentDescription = null,
                            tint = Color(0xFF00E599),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Privacy Policy & Disclaimers",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Zero data collection, sensor permissions & liability terms",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                    Text(
                        text = "View",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF00E599),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Contact & Developer Support Section
            Text(
                text = "Support & Developer",
                style = Typography.titleMedium,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                    .clickable {
                        try {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:kevrolix@gmail.com?subject=Phone%20Anti%20Snatcher%20Inquiry")
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1A1A22)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Mail,
                            contentDescription = null,
                            tint = Color(0xFFFFFFFF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Contact Security Lab",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "kevrolix@gmail.com • Bug reports & feedback",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = "Email",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFFFFFFF),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Spacious About Kevrolix Technologies Branding
            Text(
                text = "About Anti Phone Snatcher",
                style = Typography.titleMedium,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(22.dp))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Application", fontSize = 14.sp, color = TextSecondary)
                    Text(text = "Anti Phone Snatcher", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Engineering", fontSize = 14.sp, color = TextSecondary)
                    Text(text = "Kevrolix Technologies", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Architecture", fontSize = 14.sp, color = TextSecondary)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF14241C))
                            .border(1.dp, Color(0xFF00E599).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "100% On-Device • 0 KB Sent",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF00E599)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Philosophy Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF131317))
                        .border(1.dp, Color(0xFF26262F), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "PHILOSOPHY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF888894),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "\"Zero delay. Total blackout. Thieves get nothing.\"",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFFFFFFF),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }

    // PIN Change Dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            containerColor = Color(0xFF1C1C22),
            title = {
                Text(text = "Set Secret Recovery PIN", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "Enter a 4-digit code to securely recover your screen when Panic Mode is active.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = pinInputValue,
                        onValueChange = {
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                pinInputValue = it
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = Color(0xFF00E599),
                            unfocusedBorderColor = BorderSubtle
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (pinInputValue.length == 4) {
                            onRecoveryPinChanged(pinInputValue)
                            showPinDialog = false
                        }
                    },
                    enabled = pinInputValue.length == 4
                ) {
                    Text(text = "Save PIN", color = if (pinInputValue.length == 4) Color(0xFF00E599) else TextSecondary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text(text = "Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Comprehensive Legal & Privacy Policy Modal
    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            containerColor = Color(0xFF18181F),
            title = {
                Text(
                    text = "Privacy Policy & Disclaimers",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Last updated: September 2026",
                        fontSize = 12.sp,
                        color = Color(0xFF888894)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "1. Pure Zero-Data Collection Guarantee",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E599)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Anti Phone Snatcher is engineered on a strict zero-knowledge, zero-telemetry architecture. The application does not collect, record, track, log, or transmit any personal data, usage analytics, location data, or device identifiers. 100% of operations execute locally on your physical hardware. Network activity is 0 KB.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "2. Device Permissions & Functional Scope",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E599)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Display Over Other Apps (Overlay): Required strictly to project the instant pitch-black panic overlay across the display when a snatch or emergency is triggered.\n• Motion & Accelerometer Sensors: Analyzed in volatile memory in real-time exclusively to detect rapid, sudden displacement characteristic of device snatching. Sensor readings are never stored or transmitted.\n• Status Bar Collapse & Key Suppression: Applied during blackout mode solely to prevent unauthorized shade pulldown or notification access.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "3. Limitation of Liability & 'AS-IS' Disclaimer",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF453A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Anti Phone Snatcher is provided strictly on an \"AS IS\" and \"AS AVAILABLE\" basis without warranties of any kind, whether express, implied, or statutory.\n\nWhile this software employs defensive measures including high-frequency motion heuristics and instant screen occlusion to thwart snatchers, Kevrolix Technologies and its developers DO NOT warrant or guarantee that all theft attempts, device seizures, physical loss, or property damages will be prevented under every real-world circumstance.\n\nTo the maximum extent permitted by applicable law, Kevrolix Technologies shall not be held liable for any direct, indirect, incidental, punitive, or consequential damages, including loss of device, property theft, hardware damage, or personal injury resulting from the use or inability to use this software.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "4. User Safety & Responsibility",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your personal safety is paramount. In any physical confrontation or hazardous encounter, never endanger yourself to protect a mobile device. Users remain solely responsible for memorizing their configured unlock credentials (PIN or custom tap pattern).",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "5. Contact & Support",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "For legal inquiries, technical feedback, or security reports, please contact: kevrolix@gmail.com",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyPolicyDialog = false }) {
                    Text(text = "I Understand", color = Color(0xFF00E599), fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    // Google Play Prominent Disclosure Dialog for Accessibility Service
    if (showAccessibilityDisclosure) {
        AccessibilityDisclosureDialog(
            onDismiss = { showAccessibilityDisclosure = false },
            onConfirm = {
                showAccessibilityDisclosure = false
                onToggleHardwareShortcut(true)
                try {
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                } catch (_: Exception) {}
            }
        )
    }
}

@Composable
private fun ActionPillGroup(
    selectedAction: PanicAction,
    options: List<PanicAction>,
    onSelect: (PanicAction) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF202026))
            .padding(3.dp)
    ) {
        options.forEach { action ->
            val isSelected = action == selectedAction
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) Color(0xFF32323C) else Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelect(action) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (action) {
                        PanicAction.PANIC_BLACKOUT -> "Blackout"
                        PanicAction.CLOSE_CURRENT_APP -> "Close App"
                        PanicAction.BOTH -> "Both"
                        PanicAction.NONE -> "None"
                    },
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected) TextPrimary else TextSecondary
                )
            }
        }
    }
}

@Composable
private fun RecoveryMethodPill(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) Color(0xFF1E2620) else Color(0xFF202026))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) Color(0xFF00E599) else BorderSubtle,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) Color(0xFF00E599) else TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}
