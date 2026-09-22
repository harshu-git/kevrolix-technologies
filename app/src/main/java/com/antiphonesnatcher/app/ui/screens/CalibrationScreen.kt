package com.antiphonesnatcher.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MedicalInformation
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.ScreenRotation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antiphonesnatcher.app.data.PrivacyMode
import com.antiphonesnatcher.app.ui.components.AppleSlider
import com.antiphonesnatcher.app.ui.components.ModeSelector
import com.antiphonesnatcher.app.ui.theme.BackgroundDark
import com.antiphonesnatcher.app.ui.theme.BorderSubtle
import com.antiphonesnatcher.app.ui.theme.SurfaceCard
import com.antiphonesnatcher.app.ui.theme.TextPrimary
import com.antiphonesnatcher.app.ui.theme.TextSecondary
import com.antiphonesnatcher.app.ui.theme.Typography
import kotlin.math.abs

@Composable
fun CalibrationScreen(
    currentMode: PrivacyMode,
    currentStrength: Float,
    onModeChanged: (PrivacyMode) -> Unit,
    onStrengthChanged: (Float) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBlurSupported = remember { PrivacyMode.isBlurSupported(context) }
    val scrollState = rememberScrollState()

    var simulatedAngle by remember { mutableFloatStateOf(0f) } // 0 deg to 75 deg

    // Real-world physical optics simulation for calibration preview:
    // Direct front (0°-15°): Front user retains high direct contrast sensitivity.
    // Oblique angle (45°-75°): Panel luminance naturally drops 50%-70%; software tint
    // and ambient reflection push contrast below readability.
    val absAngle = abs(simulatedAngle)
    val angleFactor = (absAngle / 70f).coerceIn(0f, 1f)
    val directReadability = if (absAngle < 20f) "100% Readable (Front)" else if (absAngle < 45f) "Partial Glare Drop (45°)" else "Obscured (Side Angle)"

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
            // Top Bar
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
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Test Privacy",
                    style = Typography.headlineMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Calibration & Device Angle Guide Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Outlined.ScreenRotation,
                        contentDescription = null,
                        tint = Color(0xFF30D158),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Real Device Calibration",
                            style = Typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tilt your phone 45° to 60° to the side under ambient light. Adjust the slider until side readability collapses while front text remains clear and comfortable.",
                            style = Typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Simulated Viewing Angle Bench
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Simulated Angle",
                            style = Typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "${simulatedAngle.toInt()}° • $directReadability",
                            style = Typography.labelSmall,
                            color = if (absAngle < 20f) Color(0xFF30D158) else Color(0xFFFF9F0A)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    AppleSlider(
                        value = simulatedAngle / 75f,
                        onValueChange = { simulatedAngle = it * 75f }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Realistic Confidential Sample Content
            Text(
                text = "Confidential Sample Content",
                style = Typography.titleMedium,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF141418))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // Card 1: Banking Balance
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1E1E26))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.CreditCard,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Apex Premier Checking",
                                        style = Typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$ 48,291.50",
                                style = Typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Acct •••• 9842 • Wire pending: -$12,500.00",
                                style = Typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Card 2: Confidential Business Message
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1E1E26))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Message,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Private Acquisition Term Sheet",
                                    style = Typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "The board agreed to the $4.2M valuation terms. Keep this strictly confidential until press release.",
                                style = Typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Card 3: 2FA Emergency Recovery Token
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1E1E26))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "2FA Emergency Token",
                                    style = Typography.labelSmall,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "XK92-8410-PV49-1104",
                                    style = Typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Clean Optical Contrast-Cutoff Simulation Layer
                val simOcclusion = (0.35f + angleFactor * 0.58f) * currentStrength
                val overlayAlpha = simOcclusion.coerceIn(0.15f, 0.96f)
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = overlayAlpha))
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Calibration Live Controls
            Text(
                text = "Adjust Calibration",
                style = Typography.titleMedium,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            ModeSelector(
                selectedMode = currentMode,
                onModeSelected = onModeChanged
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Privacy Strength",
                    style = Typography.titleMedium,
                    color = TextSecondary
                )
                Text(
                    text = "${(currentStrength * 100).toInt()}%",
                    style = Typography.labelLarge,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            AppleSlider(
                value = currentStrength,
                onValueChange = onStrengthChanged
            )
        }
    }
}
