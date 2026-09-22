package com.antiphonesnatcher.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.antiphonesnatcher.app.ui.theme.ActiveGreen
import com.antiphonesnatcher.app.ui.theme.InactiveGray
import com.antiphonesnatcher.app.ui.theme.TextPrimary
import com.antiphonesnatcher.app.ui.theme.Typography

@Composable
fun StatusIndicator(
    isActive: Boolean,
    label: String = if (isActive) "Privacy is ON" else "Privacy is OFF",
    activeColor: androidx.compose.ui.graphics.Color = ActiveGreen,
    modifier: Modifier = Modifier
) {
    val dotColor by animateColorAsState(
        targetValue = if (isActive) activeColor else InactiveGray,
        animationSpec = tween(300),
        label = "status_dot_color"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = Typography.titleLarge,
            color = TextPrimary
        )
    }
}
