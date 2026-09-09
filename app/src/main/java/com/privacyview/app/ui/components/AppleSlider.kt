package com.privacyview.app.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.privacyview.app.ui.theme.SliderActive
import com.privacyview.app.ui.theme.SliderTrack
import com.privacyview.app.ui.theme.TextSecondary
import com.privacyview.app.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppleSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0.15f..0.95f,
            enabled = enabled,
            interactionSource = interactionSource,
            colors = SliderDefaults.colors(
                thumbColor = SliderActive,
                activeTrackColor = SliderActive,
                inactiveTrackColor = SliderTrack,
                disabledThumbColor = Color(0xFF55555A),
                disabledActiveTrackColor = Color(0xFF444448),
                disabledInactiveTrackColor = SliderTrack
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Low",
                style = Typography.labelSmall,
                color = TextSecondary
            )
            Text(
                text = "Medium",
                style = Typography.labelSmall,
                color = TextSecondary
            )
            Text(
                text = "High",
                style = Typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}
