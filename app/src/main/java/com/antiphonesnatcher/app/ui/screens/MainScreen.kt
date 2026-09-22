package com.antiphonesnatcher.app.ui.screens

import android.app.StatusBarManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.widget.Toast
import com.antiphonesnatcher.app.R
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
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MotionPhotosOn
import androidx.compose.material.icons.outlined.MusicOff
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.VolumeDown
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antiphonesnatcher.app.data.PanicAction
import com.antiphonesnatcher.app.data.PanicRecoveryMethod
import com.antiphonesnatcher.app.data.PrivacyMode
import com.antiphonesnatcher.app.service.PrivacyTileService
import com.antiphonesnatcher.app.ui.components.ModeSelector
import com.antiphonesnatcher.app.ui.components.StatusIndicator
import com.antiphonesnatcher.app.ui.theme.ActiveGreen
import com.antiphonesnatcher.app.ui.theme.BackgroundDark
import com.antiphonesnatcher.app.ui.theme.BorderSubtle
import com.antiphonesnatcher.app.ui.theme.SurfaceCard
import com.antiphonesnatcher.app.ui.theme.TextPrimary
import com.antiphonesnatcher.app.ui.theme.TextSecondary
import com.antiphonesnatcher.app.ui.theme.Typography
import com.antiphonesnatcher.app.widget.PrivacyWidgetProvider
import java.util.concurrent.Executor

@Composable
fun MainScreen(
    isProtectionArmed: Boolean,
    isPrivacyEnabled: Boolean,
    currentMode: PrivacyMode,
    recoveryMethod: PanicRecoveryMethod,
    volumeDownAction: PanicAction,
    isHardwareShortcutEnabled: Boolean,
    isMuteAudioEnabled: Boolean,
    isAntiSnatchEnabled: Boolean = true,
    onToggleArmed: () -> Unit,
    onTriggerPanic: () -> Unit,
    onDismissPanic: () -> Unit,
    onModeChanged: (PrivacyMode) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val heroButtonBg by animateColorAsState(
        targetValue = when {
            isPrivacyEnabled -> Color(0xFFFF3B30)
            isProtectionArmed -> Color(0xFF1E1E22)
            else -> Color(0xFFFFFFFF)
        },
        animationSpec = tween(250),
        label = "hero_bg"
    )

    val heroButtonText by animateColorAsState(
        targetValue = when {
            isPrivacyEnabled -> Color(0xFFFFFFFF)
            isProtectionArmed -> Color(0xFFFFFFFF)
            else -> Color(0xFF000000)
        },
        animationSpec = tween(250),
        label = "hero_text"
    )

    val appWidgetManager = remember { AppWidgetManager.getInstance(context) }
    val widgetProvider = remember { ComponentName(context, PrivacyWidgetProvider::class.java) }
    val hasActiveWidget = remember(isPrivacyEnabled, isProtectionArmed) {
        try {
            appWidgetManager.getAppWidgetIds(widgetProvider).isNotEmpty()
        } catch (_: Exception) {
            false
        }
    }

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
            // Header with Brand Logo Art Styling
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append("Phone ")
                            withStyle(style = SpanStyle(color = Color(0xFFFF3B30), fontWeight = FontWeight.ExtraBold)) {
                                append("Anti")
                            }
                            append(" Snatcher")
                        },
                        fontSize = 23.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Stay in Control • Instant Theft Defense",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFF8E8E98)
                    )
                }

                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(SurfaceCard)
                        .border(1.dp, BorderSubtle, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Status Card & Hero Controls
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(28.dp))
                    .padding(24.dp)
            ) {
                Column {
                    when {
                        isPrivacyEnabled -> {
                            StatusIndicator(
                                isActive = true,
                                label = "Panic Blackout Active",
                                activeColor = Color(0xFFFF453A)
                            )
                        }
                        isProtectionArmed -> {
                            StatusIndicator(
                                isActive = true,
                                label = "Anti-Snatch Shield Armed",
                                activeColor = ActiveGreen
                            )
                        }
                        else -> {
                            StatusIndicator(
                                isActive = false,
                                label = "Protection Disarmed",
                                activeColor = Color(0xFF6E6E73)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = when {
                            isPrivacyEnabled -> "Screen is blacked out and audio muted. Use your ${recoveryMethod.displayName} to unlock."
                            isProtectionArmed -> "Active in background • Screen is normal. If someone snatches your phone, it instantly blacks out and silences audio."
                            else -> "Anti-snatch sentinel is paused. Arm the shield to enable real-time theft protection while using your device."
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
                                width = if (isProtectionArmed && !isPrivacyEnabled) 1.dp else 0.dp,
                                color = if (isProtectionArmed && !isPrivacyEnabled) BorderSubtle else Color.Transparent,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (isPrivacyEnabled) {
                                    onDismissPanic()
                                } else {
                                    onToggleArmed()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when {
                                isPrivacyEnabled -> "Dismiss Panic Blackout"
                                isProtectionArmed -> "Disarm Anti-Snatch Shield"
                                else -> "Arm Anti-Snatch Shield"
                            },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = heroButtonText
                        )
                    }

                    // Secondary Trigger Button (Test / Manual Instant Blackout)
                    if (!isPrivacyEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFF19191D))
                                .border(1.dp, Color(0xFF2A2A30), RoundedCornerShape(18.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    onTriggerPanic()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color(0xFFFF453A),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Trigger Panic Blackout Now",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFFFFFFF)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Zero-Server Privacy Guarantee Trust Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF131714))
                    .border(1.dp, Color(0xFF1E3824), RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF183B20)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.VerifiedUser,
                            contentDescription = null,
                            tint = Color(0xFF00E599),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Zero-Server Guarantee",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF00E599)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF1E3824))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "100% On-Device",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E599)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Anti Phone Snatcher has NO servers and ZERO cloud accounts. All sensor triggers, blackouts, and mute routines run entirely inside your device hardware.",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = Color(0xFF8BA592)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Privacy Mode
            Text(
                text = "Privacy Mode",
                style = Typography.titleMedium,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            ModeSelector(
                selectedMode = currentMode,
                onModeSelected = onModeChanged
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = currentMode.description,
                style = Typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.padding(start = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Protection Configuration Card
            Text(
                text = "Protection Configuration",
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
                    icon = Icons.Outlined.MotionPhotosOn,
                    title = "Anti-Snatch Sensor",
                    subtitle = if (isAntiSnatchEnabled && isProtectionArmed) {
                        "Monitoring in background • Blackout on sudden violent snatch"
                    } else {
                        "Anti-snatch sensor disabled or disarmed"
                    },
                    trailingBadge = if (isAntiSnatchEnabled && isProtectionArmed) "Armed" else "Off",
                    onClick = onNavigateToSettings
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(BorderSubtle)
                )
                SettingRowClickable(
                    icon = Icons.Outlined.VolumeDown,
                    title = "Volume Button Shortcut",
                    subtitle = if (isHardwareShortcutEnabled) {
                        "Double-press Vol Down: ${volumeDownAction.displayName}"
                    } else {
                        "Disabled • Tap to configure hardware trigger"
                    },
                    trailingBadge = if (isHardwareShortcutEnabled) "Active" else "Off",
                    onClick = onNavigateToSettings
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(BorderSubtle)
                )
                SettingRowClickable(
                    icon = Icons.Outlined.MusicOff,
                    title = "Instant Audio Mute",
                    subtitle = if (isMuteAudioEnabled) "All media and calls silenced on panic" else "Audio muting disabled",
                    trailingBadge = if (isMuteAudioEnabled) "On" else "Off",
                    onClick = onNavigateToSettings
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(BorderSubtle)
                )
                SettingRowClickable(
                    icon = Icons.Outlined.Lock,
                    title = "Secure Recovery",
                    subtitle = "${recoveryMethod.displayName} (${if (recoveryMethod == PanicRecoveryMethod.SECRET_TAPS) "4 Taps" else "PIN"})",
                    trailingBadge = "Protected",
                    onClick = onNavigateToSettings
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Fast Activation Triggers
            Text(
                text = "Fast Activation Triggers",
                style = Typography.titleMedium,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Quick Settings",
                    subtitle = if (isProtectionArmed) "Tile: Armed" else "Tile: Disarmed",
                    icon = Icons.Outlined.Tune,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            try {
                                val statusBarManager = context.getSystemService(StatusBarManager::class.java)
                                statusBarManager?.requestAddTileService(
                                    ComponentName(context, PrivacyTileService::class.java),
                                    "Anti Snatcher",
                                    android.graphics.drawable.Icon.createWithResource(context, R.drawable.ic_privacy_tile),
                                    Executor { it.run() }
                                ) { _ -> }
                                Toast.makeText(context, "Quick Settings tile request sent. You can also swipe down and tap the edit pencil to place it.", Toast.LENGTH_LONG).show()
                            } catch (_: Exception) {
                                Toast.makeText(context, "Swipe down Quick Settings and tap the edit pencil to add Anti Snatcher tile", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, "Swipe down Quick Settings and tap the edit pencil to add Anti Snatcher tile", Toast.LENGTH_LONG).show()
                        }
                    }
                )

                QuickActionCard(
                    title = "Home Widget",
                    subtitle = if (hasActiveWidget) "Added to Home" else "Tap to add",
                    icon = Icons.Outlined.Widgets,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                                appWidgetManager.requestPinAppWidget(widgetProvider, null, null)
                            } else {
                                Toast.makeText(context, "Long-press your home screen to add Anti Phone Snatcher widget", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Long-press your home screen to add Anti Phone Snatcher widget", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(18.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1E24)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
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
    trailingBadge: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1E24)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (trailingBadge == "Armed" || trailingBadge == "Active" || trailingBadge == "On" || trailingBadge == "Protected") Color(0xFF183B20) else Color(0xFF222228))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = trailingBadge,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (trailingBadge == "Armed" || trailingBadge == "Active" || trailingBadge == "On" || trailingBadge == "Protected") Color(0xFF00E599) else TextSecondary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF55555A),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
