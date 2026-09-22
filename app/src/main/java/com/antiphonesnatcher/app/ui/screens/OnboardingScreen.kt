package com.antiphonesnatcher.app.ui.screens

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.VolumeDown
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import com.antiphonesnatcher.app.ui.components.AccessibilityDisclosureDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antiphonesnatcher.app.data.PanicRecoveryMethod
import com.antiphonesnatcher.app.data.PrivacyMode
import com.antiphonesnatcher.app.data.TapSequenceHelper
import com.antiphonesnatcher.app.ui.components.ModeSelector
import com.antiphonesnatcher.app.ui.components.SequenceRecorderView
import com.antiphonesnatcher.app.ui.theme.BackgroundDark
import com.antiphonesnatcher.app.ui.theme.BorderSubtle
import com.antiphonesnatcher.app.ui.theme.SurfaceCard
import com.antiphonesnatcher.app.ui.theme.TextPrimary
import com.antiphonesnatcher.app.ui.theme.TextSecondary
import com.antiphonesnatcher.app.ui.theme.Typography

@Composable
fun OnboardingScreen(
    hasOverlayPermission: Boolean,
    onRequestPermission: () -> Unit,
    currentMode: PrivacyMode,
    onModeChanged: (PrivacyMode) -> Unit,
    recoveryMethod: PanicRecoveryMethod,
    onRecoveryMethodChanged: (PanicRecoveryMethod) -> Unit,
    recoveryPin: String,
    onRecoveryPinChanged: (String) -> Unit,
    recoveryTapSequence: String = TapSequenceHelper.DEFAULT_SEQUENCE,
    onRecoveryTapSequenceChanged: (String) -> Unit = {},
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableIntStateOf(0) }

    // When the user returns having granted permission, automatically advance to next step!
    androidx.compose.runtime.LaunchedEffect(hasOverlayPermission) {
        if (currentStep == 1 && hasOverlayPermission) {
            currentStep = 2
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Step Indicators (4 steps)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                (0..3).forEach { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(4.dp)
                            .width(if (index == currentStep) 28.dp else 12.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (index == currentStep) Color(0xFFFFFFFF)
                                else if (index < currentStep) Color(0xFF55555A)
                                else Color(0xFF222226)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Step Content (Scrollable so Continue button is always reachable)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.TopCenter
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "onboarding_step"
                ) { step ->
                    when (step) {
                        0 -> StepWelcome()
                        1 -> StepPermission(
                            hasPermission = hasOverlayPermission,
                            onRequestPermission = onRequestPermission
                        )
                        2 -> StepRecoveryMethod(
                            method = recoveryMethod,
                            onMethodChanged = onRecoveryMethodChanged,
                            pin = recoveryPin,
                            onPinChanged = onRecoveryPinChanged,
                            tapSequence = recoveryTapSequence,
                            onTapSequenceChanged = onRecoveryTapSequenceChanged
                        )
                        3 -> StepSummary(
                            mode = currentMode,
                            recoveryMethod = recoveryMethod
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val canProceed = when (currentStep) {
                1 -> true // Must remain clickable so user can tap 'Grant Permission' to open settings!
                2 -> if (recoveryMethod == PanicRecoveryMethod.SECRET_TAPS) {
                         TapSequenceHelper.parseSequence(recoveryTapSequence).size == 4
                     } else {
                         recoveryPin.length == 4
                     }
                else -> true
            }

            // Bottom Navigation Buttons (Fixed at bottom)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 0) {
                    Text(
                        text = "Back",
                        style = Typography.bodyLarge,
                        color = TextSecondary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { currentStep-- }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Box(
                    modifier = Modifier
                        .height(52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(if (canProceed) Color(0xFFFFFFFF) else Color(0xFF232328))
                        .clickable(enabled = canProceed) {
                            if (currentStep == 1 && !hasOverlayPermission) {
                                onRequestPermission()
                            } else if (currentStep < 3) {
                                currentStep++
                            } else {
                                onFinishOnboarding()
                            }
                        }
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            currentStep == 1 && !hasOverlayPermission -> "Grant Permission"
                            currentStep == 2 && recoveryMethod == PanicRecoveryMethod.SECRET_TAPS && TapSequenceHelper.parseSequence(recoveryTapSequence).size < 4 -> "Record 4 Taps to Continue"
                            currentStep == 2 && recoveryMethod == PanicRecoveryMethod.PIN && recoveryPin.length < 4 -> "Enter 4-Digit PIN"
                            currentStep < 3 -> "Continue"
                            else -> "Finish Setup"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (canProceed) Color(0xFF000000) else Color(0xFF72727D)
                    )
                }
            }
        }
    }
}

@Composable
private fun StepWelcome() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(SurfaceCard)
                .border(1.dp, BorderSubtle, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = null,
                tint = Color(0xFFFFFFFF),
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Emergency Privacy Shield",
            style = Typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "One action immediately blacks out your screen, mutes all playing audio, and locks sensitive applications from onlookers.",
            style = Typography.bodyLarge,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StepPermission(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "System Overlay Access",
            style = Typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Android requires 'Display over other apps' so Anti Phone Snatcher can instantly cover your screen when emergency privacy is triggered.",
            style = Typography.bodyLarge,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceCard)
                .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Security,
                    contentDescription = null,
                    tint = Color(0xFF30D158),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = if (hasPermission) "Overlay Permission Granted ✓"
                    else "Anti Phone Snatcher operates 100% on-device. Zero data is recorded or transmitted.",
                    style = Typography.bodyMedium,
                    color = if (hasPermission) Color(0xFF30D158) else TextSecondary
                )
            }
        }

        if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1B1B22))
                    .padding(14.dp)
            ) {
                Text(
                    text = "Tip for modern Android (13 to 16): If permission appears grayed out when sideloading, open App Info → tap the 3 dots in top-right → tap 'Allow restricted settings'.",
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = Color(0xFF8E8E98)
                )
            }
        }

        val context = LocalContext.current
        val isAccessibilityEnabled = com.antiphonesnatcher.app.service.PrivacyAccessibilityService.isAccessibilityServiceEnabled(context)
        var showAccessibilityDisclosure by remember { mutableStateOf(false) }

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceCard)
                .border(1.dp, if (isAccessibilityEnabled) Color(0xFF30D158).copy(alpha = 0.5f) else BorderSubtle, RoundedCornerShape(18.dp))
                .clickable {
                    if (!isAccessibilityEnabled) {
                        showAccessibilityDisclosure = true
                    } else {
                        try {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                }
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = if (isAccessibilityEnabled) Color(0xFF30D158) else Color(0xFFFFB300),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isAccessibilityEnabled) "Anti-Tamper Shield: Active ✓" else "Anti-Tamper Shield (Tap to Enable)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isAccessibilityEnabled) Color(0xFF30D158) else TextPrimary
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = if (isAccessibilityEnabled) "Full protection active: notification shade pull-down is blocked and Settings tampering instantly locks phone."
                        else "Recommended: Blocks notification shade pull-down and locks phone instantly if a thief tries to open Settings during blackout.",
                        fontSize = 11.5.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        if (showAccessibilityDisclosure) {
            AccessibilityDisclosureDialog(
                onDismiss = { showAccessibilityDisclosure = false },
                onConfirm = {
                    showAccessibilityDisclosure = false
                    try {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                }
            )
        }
    }
}

@Composable
private fun StepRecoveryMethod(
    method: PanicRecoveryMethod,
    onMethodChanged: (PanicRecoveryMethod) -> Unit,
    pin: String,
    onPinChanged: (String) -> Unit,
    tapSequence: String,
    onTapSequenceChanged: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Secure Recovery Method",
            style = Typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Choose how to unlock your phone once Panic Mode has locked the screen.",
            style = Typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            RecoveryChoiceCard(
                title = "Secret Tap Sequence",
                subtitle = "Custom 4-zone pattern",
                isSelected = method == PanicRecoveryMethod.SECRET_TAPS,
                modifier = Modifier.weight(1f),
                onClick = { onMethodChanged(PanicRecoveryMethod.SECRET_TAPS) }
            )
            Spacer(modifier = Modifier.width(12.dp))
            RecoveryChoiceCard(
                title = "Secret PIN",
                subtitle = "4-digit keypad",
                isSelected = method == PanicRecoveryMethod.PIN,
                modifier = Modifier.weight(1f),
                onClick = { onMethodChanged(PanicRecoveryMethod.PIN) }
            )
        }

        if (method == PanicRecoveryMethod.SECRET_TAPS) {
            Spacer(modifier = Modifier.height(20.dp))
            SequenceRecorderView(
                currentSequence = tapSequence,
                onSequenceSaved = onTapSequenceChanged
            )
        } else if (method == PanicRecoveryMethod.PIN) {
            Spacer(modifier = Modifier.height(20.dp))

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
                        text = "Set 4-Digit Recovery PIN",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = pin,
                        onValueChange = {
                            if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                onPinChanged(it)
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
                        placeholder = { Text("1234", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "To reveal the PIN during blackout: Tap or hold with 2 fingers on screen, or press Volume Up + Down together. Single taps stay completely black.",
                        fontSize = 12.sp,
                        color = Color(0xFF00E599),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RecoveryChoiceCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (isSelected) Color(0xFF19241C) else SurfaceCard)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) Color(0xFF00E599) else BorderSubtle,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color(0xFF00E599) else TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun StepSummary(
    mode: PrivacyMode,
    recoveryMethod: PanicRecoveryMethod
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFF1B2E1E))
                .border(1.dp, Color(0xFF30D158), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = Color(0xFF30D158),
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Emergency Shield Ready",
            style = Typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Anti Phone Snatcher is primed for instant protection. Use the Volume shortcut, Home widget, or Quick Settings tile whenever you need immediate privacy.",
            style = Typography.bodyLarge,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceCard)
                .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Panic Mask",
                        style = Typography.labelSmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = mode.displayName,
                        style = Typography.titleMedium,
                        color = TextPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Recovery Method",
                        style = Typography.labelSmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = recoveryMethod.displayName,
                        style = Typography.titleMedium,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}
