package com.privacyview.app.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.privacyview.app.ui.theme.ActiveGreen
import com.privacyview.app.ui.theme.BackgroundDark
import com.privacyview.app.ui.theme.BorderSubtle
import com.privacyview.app.ui.theme.SurfaceCard
import com.privacyview.app.ui.theme.TextPrimary
import com.privacyview.app.ui.theme.TextSecondary
import com.privacyview.app.ui.theme.Typography

@Composable
fun SettingsScreen(
    isHardwareShortcutEnabled: Boolean,
    onToggleHardwareShortcut: (Boolean) -> Unit,
    restoreOnBoot: Boolean,
    onToggleRestoreOnBoot: (Boolean) -> Unit,
    onNavigateToCalibration: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

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
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Settings",
                    style = Typography.headlineMedium
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Android Security & Tapjacking Handling
            Text(
                text = "Android Security & Overlays",
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
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Outlined.Security,
                        contentDescription = null,
                        tint = Color(0xFF30D158),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Tapjacking Protection Handling",
                            style = Typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Android blocks taps on sensitive permission dialogs and secure banking PIN screens while any overlay is active. If Android displays 'App obscuring a permission request', simply tap 'Turn Off' on the PrivacyView notification or Quick Settings tile, confirm the prompt, and tap once to re-enable.",
                            style = Typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Optional Hardware Shortcut Section
            Text(
                text = "Optional Hardware Shortcut",
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
                                text = "Double-press Volume Down",
                                style = Typography.titleMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Secondary physical key toggle (Disabled by default)",
                                style = Typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = isHardwareShortcutEnabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    try {
                                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                                    } catch (_: Exception) {}
                                }
                                onToggleHardwareShortcut(checked)
                            },
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
                        text = "Hardware shortcuts require the optional PrivacyView accessibility service. Core app operation does not require accessibility. OEM battery managers (Samsung One UI, Xiaomi HyperOS) may limit volume intercepts when media is playing.",
                        style = Typography.labelSmall,
                        color = Color(0xFF6E6E73)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // System Behavior Section
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
                            text = "Automatically reapply privacy if it was active prior to restart",
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

            Spacer(modifier = Modifier.height(24.dp))

            // Calibration Shortcut
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                    .clickable(onClick = onNavigateToCalibration)
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Visibility,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Privacy Calibration Sandbox",
                            style = Typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Calibrate side-angle contrast with test cards",
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

            Spacer(modifier = Modifier.height(28.dp))

            // Standalone Product About
            Text(
                text = "About PrivacyView",
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
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Application", style = Typography.bodyMedium, color = TextSecondary)
                    Text(text = "PrivacyView", style = Typography.titleMedium, color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Version", style = Typography.bodyMedium, color = TextSecondary)
                    Text(text = "1.0.0 (Production Architecture)", style = Typography.titleMedium, color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Advertisements", style = Typography.bodyMedium, color = TextSecondary)
                    Text(text = "Zero (Paid Premium Utility)", style = Typography.titleMedium, color = ActiveGreen)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Data Collection", style = Typography.bodyMedium, color = TextSecondary)
                    Text(text = "0 bytes (100% On-Device)", style = Typography.titleMedium, color = ActiveGreen)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "PrivacyView is an independent utility. Works without cameras, microphones, tracking, or network transmission.",
                style = Typography.labelSmall,
                color = Color(0xFF6E6E73),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}
