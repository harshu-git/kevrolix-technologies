package com.antiphonesnatcher.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antiphonesnatcher.app.data.PrivacyMode
import com.antiphonesnatcher.app.ui.theme.BorderSubtle
import com.antiphonesnatcher.app.ui.theme.SurfaceCard
import com.antiphonesnatcher.app.ui.theme.TextPrimary
import com.antiphonesnatcher.app.ui.theme.TextSecondary

@Composable
fun ModeSelector(
    selectedMode: PrivacyMode,
    onModeSelected: (PrivacyMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = PrivacyMode.entries

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            modes.forEach { mode ->
                val isSelected = mode == selectedMode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0xFF2C2C34) else Color.Transparent)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onModeSelected(mode)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.displayName,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
