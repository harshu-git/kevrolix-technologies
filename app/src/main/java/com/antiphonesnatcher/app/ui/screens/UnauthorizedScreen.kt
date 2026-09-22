package com.antiphonesnatcher.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antiphonesnatcher.app.BuildConfig
import com.antiphonesnatcher.app.licensing.LicenseValidator
import com.antiphonesnatcher.app.ui.theme.BackgroundDark
import com.antiphonesnatcher.app.ui.theme.BorderSubtle
import com.antiphonesnatcher.app.ui.theme.SurfaceCard
import com.antiphonesnatcher.app.ui.theme.TextPrimary
import com.antiphonesnatcher.app.ui.theme.TextSecondary

private val WarningAmber = Color(0xFFFFB300)
private val WarningAmberDark = Color(0x33FFB300)
private val GooglePlayGreen = Color(0xFF00C853)
private val LockRed = Color(0xFFFF3B30)

/**
 * Security defense screen shown when the app detects an unauthorized installation
 * (e.g., sideloaded via Bluetooth, Quick Share, or downloaded from a third-party pirate site).
 */
@Composable
fun UnauthorizedScreen(
    onRetryCheck: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val installer = LicenseValidator.getInstallerPackage(context) ?: "Unknown / Direct Sideload"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Shield & Lock Icon
            Box(
                modifier = Modifier
                    .size(108.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(WarningAmberDark, Color.Transparent)
                        )
                    )
                    .border(2.dp, WarningAmber.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = WarningAmber,
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Title
            Text(
                text = "Official License Required",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle
            Text(
                text = "Anti Phone Snatcher is an active anti-theft defense application. To guarantee tamper protection and security integrity, it must be installed directly from Google Play.",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 21.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Security Explanation Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = LockRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Why is this copy blocked?",
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                    }

                    Text(
                        text = "• Copies shared via Bluetooth, Quick Share, or downloaded from third-party APK websites have unverified signatures.\n• Anti-theft sensors and privacy blackouts require an authentic Google Play installation.\n• Detected installation source: $installer",
                        color = TextSecondary,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Primary Action: Google Play Store Button
            Button(
                onClick = { openGooglePlay(context) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GooglePlayGreen,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Get Official App on Google Play",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Secondary Action: Re-verify
            OutlinedButton(
                onClick = onRetryCheck,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Retry Verification", fontSize = 14.sp)
            }
        }
    }
}

private fun openGooglePlay(context: Context) {
    val packageName = context.packageName.removeSuffix(".debug")
    val marketUri = Uri.parse("market://details?id=$packageName")
    val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")

    val marketIntent = Intent(Intent.ACTION_VIEW, marketUri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        context.startActivity(marketIntent)
    } catch (e: Exception) {
        val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(webIntent)
        } catch (_: Exception) {
            Toast.makeText(context, "Could not open Google Play Store", Toast.LENGTH_SHORT).show()
        }
    }
}