package com.privacyview.app.ui.screens

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
import androidx.compose.material.icons.outlined.Message
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
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
    val scrollState = rememberScrollState()
    var simulatedAngle by remember { mutableFloatStateOf(0f) } // 0 deg to 75 deg

    // Calculate optical occlusion for simulated angle
    val angleFactor = (abs(simulatedAngle) / 75f).coerceIn(0f, 1f)
    val combinedOcclusion = (angleFactor * currentStrength * 1.5f).coerceIn(0f, 0.96f)

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

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Tilt your device or use the angle simulator below to check how confidential content looks from the front versus side angles.",
                style = Typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Simulated Viewing Angle Control
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
                            text = "Simulated Viewing Angle",
                            style = Typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "${simulatedAngle.toInt()}° (${if (simulatedAngle < 15f) "Front" else "Side Angle"})",
                            style = Typography.labelLarge,
                            color = if (simulatedAngle < 15f) Color(0xFF30D158) else Color(0xFFFF9F0A)
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

            // Interactive Sample Confidential Content Sandbox
            Text(
                text = "Sample Confidential Content",
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
                        .padding(20.dp)
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
                                        modifier = Modifier.size(18.dp)
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
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Acct ending in •••• 9842",
                                style = Typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Card 2: Private Message
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
                                    text = "Confidential Client Review",
                                    style = Typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "The buyout valuation was approved at $4.2M. Please keep documents strictly private until signing.",
                                style = Typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Card 3: Security Code Note
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
                                    text = "2FA Emergency Recovery Token",
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

                // Optical Filter Simulation Layer
                if (combinedOcclusion > 0.05f) {
                    when (currentMode) {
                        PrivacyMode.BLUR -> {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .blur((combinedOcclusion * 18f).dp)
                                    .background(Color.Black.copy(alpha = combinedOcclusion * 0.75f))
                            )
                        }
                        PrivacyMode.DARK -> {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color.Black.copy(alpha = combinedOcclusion * 0.95f))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Calibration Adjustment Controls
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
