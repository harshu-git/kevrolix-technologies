package com.privacyview.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.privacyview.app.data.PrivacyMode
import com.privacyview.app.ui.components.AppleSlider
import com.privacyview.app.ui.components.ModeSelector
import com.privacyview.app.ui.theme.BackgroundDark
import com.privacyview.app.ui.theme.BorderSubtle
import com.privacyview.app.ui.theme.SurfaceCard
import com.privacyview.app.ui.theme.TextPrimary
import com.privacyview.app.ui.theme.TextSecondary
import com.privacyview.app.ui.theme.Typography

@Composable
fun OnboardingScreen(
    hasOverlayPermission: Boolean,
    onRequestPermission: () -> Unit,
    currentMode: PrivacyMode,
    currentStrength: Float,
    onModeChanged: (PrivacyMode) -> Unit,
    onStrengthChanged: (Float) -> Unit,
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 24.dp, vertical = 48.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Step Indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                (0..4).forEach { index ->
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

            // Step Content
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
                    2 -> StepModeSelection(
                        currentMode = currentMode,
                        onModeChanged = onModeChanged
                    )
                    3 -> StepStrength(
                        currentStrength = currentStrength,
                        onStrengthChanged = onStrengthChanged
                    )
                    4 -> StepSummary(
                        mode = currentMode,
                        strength = currentStrength
                    )
                }
            }

            // Bottom Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 0) {
                    Box(
                        modifier = Modifier
                            .height(54.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(SurfaceCard)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                currentStep--
                            }
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Back",
                            style = Typography.labelLarge,
                            color = TextSecondary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Box(
                    modifier = Modifier
                        .height(54.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFFFFFFF))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (currentStep == 1 && !hasOverlayPermission) {
                                onRequestPermission()
                            } else if (currentStep < 4) {
                                currentStep++
                            } else {
                                onFinishOnboarding()
                            }
                        }
                        .padding(horizontal = 28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (currentStep == 1 && !hasOverlayPermission) "Grant Permission"
                        else if (currentStep < 4) "Continue"
                        else "Finish Setup",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF000000)
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
            text = "Welcome to PrivacyView",
            style = Typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Keep your screen private from side angles while enjoying crystal-clear viewing directly in front.",
            style = Typography.bodyLarge,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceCard)
                .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            BenefitItem("No cameras or facial tracking")
            BenefitItem("Zero data or screen content collected")
            BenefitItem("Instant one-tap toggle from Quick Settings")
        }
    }
}

@Composable
private fun BenefitItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Check,
            contentDescription = null,
            tint = Color(0xFF30D158),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = Typography.bodyMedium,
            color = TextPrimary
        )
    }
}

@Composable
private fun StepPermission(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
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
                imageVector = Icons.Outlined.Layers,
                contentDescription = null,
                tint = if (hasPermission) Color(0xFF30D158) else Color(0xFFFFFFFF),
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "System Overlay Permission",
            style = Typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Android requires 'Display over other apps' so PrivacyView can apply the optical privacy layer across your entire phone.",
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
                    text = if (hasPermission) "Permission Granted ✓"
                    else "PrivacyView never records, captures, or transmits screen content.",
                    style = Typography.bodyMedium,
                    color = if (hasPermission) Color(0xFF30D158) else TextSecondary
                )
            }
        }
    }
}

@Composable
private fun StepModeSelection(
    currentMode: PrivacyMode,
    onModeChanged: (PrivacyMode) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Choose Privacy Mode",
            style = Typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Select how PrivacyView alters optical transmission from side angles.",
            style = Typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(28.dp))

        val context = androidx.compose.ui.platform.LocalContext.current
        val isBlurSupported = androidx.compose.runtime.remember { com.privacyview.app.data.PrivacyMode.isBlurSupported(context) }

        ModeSelector(
            selectedMode = currentMode,
            onModeSelected = onModeChanged,
            isBlurAvailable = isBlurSupported
        )

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceCard)
                .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = currentMode.displayName + " Mode",
                    style = Typography.titleLarge,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = currentMode.description,
                    style = Typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun StepStrength(
    currentStrength: Float,
    onStrengthChanged: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Set Privacy Strength",
            style = Typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "You can fine-tune this setting at any time to match your device display.",
            style = Typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(36.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Intensity Level",
                style = Typography.titleMedium,
                color = TextSecondary
            )
            Text(
                text = "${(currentStrength * 100).toInt()}%",
                style = Typography.titleLarge,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        AppleSlider(
            value = currentStrength,
            onValueChange = onStrengthChanged
        )
    }
}

@Composable
private fun StepSummary(
    mode: PrivacyMode,
    strength: Float
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
            text = "You're All Set",
            style = Typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "PrivacyView is ready. Use the home widget or Quick Settings tile for instant one-tap protection whenever you need it.",
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
                        text = "Configured Mode",
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
                        text = "Strength",
                        style = Typography.labelSmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(strength * 100).toInt()}%",
                        style = Typography.titleMedium,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}
