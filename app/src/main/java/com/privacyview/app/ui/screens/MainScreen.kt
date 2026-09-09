package com.privacyview.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.privacyview.app.data.PrivacyMode
import com.privacyview.app.ui.components.AppleSlider
import com.privacyview.app.ui.components.ModeSelector
import com.privacyview.app.ui.components.StatusIndicator
import com.privacyview.app.ui.theme.ActiveGreen
import com.privacyview.app.ui.theme.BackgroundDark
import com.privacyview.app.ui.theme.BorderSubtle
import com.privacyview.app.ui.theme.SurfaceCard
import com.privacyview.app.ui.theme.TextPrimary
import com.privacyview.app.ui.theme.TextSecondary
import com.privacyview.app.ui.theme.Typography

@Composable
fun MainScreen(
    isPrivacyEnabled: Boolean,
    currentMode: PrivacyMode,
    currentStrength: Float,
    onTogglePrivacy: () -> Unit,
    onModeChanged: (PrivacyMode) -> Unit,
    onStrengthChanged: (Float) -> Unit,
    onNavigateToCalibration: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val heroButtonBg by animateColorAsState(
        targetValue = if (isPrivacyEnabled) Color(0xFF1E1E22) else Color(0xFFFFFFFF),
        animationSpec = tween(250),
        label = "hero_bg"
    )

    val heroButtonText by animateColorAsState(
        targetValue = if (isPrivacyEnabled) Color(0xFFFFFFFF) else Color(0xFF000000),
        animationSpec = tween(250),
        label = "hero_text"
    )

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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PrivacyView",
                        style = Typography.displayLarge
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Your screen. Your privacy.",
                        style = Typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SurfaceCard)
                        .border(1.dp, BorderSubtle, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Status Card & Hero Toggle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(28.dp))
                    .padding(24.dp)
            ) {
                Column {
                    StatusIndicator(isActive = isPrivacyEnabled)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isPrivacyEnabled) {
                            "Screen protection is active across your device."
                        } else {
                            "Tap to obscure screen viewing from side angles."
                        },
                        style = Typography.bodyMedium,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Primary Hero Action Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(heroButtonBg)
                            .border(
                                width = if (isPrivacyEnabled) 1.dp else 0.dp,
                                color = if (isPrivacyEnabled) BorderSubtle else Color.Transparent,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onTogglePrivacy()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isPrivacyEnabled) "Turn Off Privacy" else "Turn On Privacy",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = heroButtonText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Privacy Effect Section
            Text(
                text = "Privacy Effect",
                style = Typography.titleMedium,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            ModeSelector(
                selectedMode = currentMode,
                onModeSelected = onModeChanged
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = currentMode.description,
                style = Typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.padding(start = 8.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Privacy Strength Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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

            Spacer(modifier = Modifier.height(36.dp))

            // Quick Access Section
            Text(
                text = "Quick Access",
                style = Typography.titleMedium,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
            ) {
                SettingRow(
                    icon = Icons.Outlined.Widgets,
                    title = "Home Screen Widget",
                    subtitle = "Toggle privacy directly from your home screen"
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(BorderSubtle)
                )
                SettingRow(
                    icon = Icons.Outlined.PhoneAndroid,
                    title = "Quick Settings Tile",
                    subtitle = "Swipe down shade for instant one-tap control"
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Advanced Section
            Text(
                text = "Advanced",
                style = Typography.titleMedium,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
            ) {
                SettingRowClickable(
                    icon = Icons.Outlined.Visibility,
                    title = "Test Privacy",
                    subtitle = "Calibrate balance between front & side angles",
                    onClick = onNavigateToCalibration
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(BorderSubtle)
                )
                SettingRowClickable(
                    icon = Icons.Outlined.Tune,
                    title = "Hardware Shortcut",
                    subtitle = "Double press volume key (Disabled by default)",
                    onClick = onNavigateToSettings
                )
            }
        }
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF222226)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = Typography.titleMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = Typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun SettingRowClickable(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF222226)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = Typography.titleMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = Typography.bodyMedium,
                color = TextSecondary
            )
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}
