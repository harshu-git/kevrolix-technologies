package com.antiphonesnatcher.app.ui.components

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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antiphonesnatcher.app.data.TapSequenceHelper
import com.antiphonesnatcher.app.ui.theme.BorderSubtle
import com.antiphonesnatcher.app.ui.theme.SurfaceCard
import com.antiphonesnatcher.app.ui.theme.TextPrimary
import com.antiphonesnatcher.app.ui.theme.TextSecondary
import com.antiphonesnatcher.app.ui.theme.Typography

/**
 * Interactive 4-Zone Secret Tap Sequence Recorder.
 * Allows users to tap their custom sequence on an interactive screen mockup.
 */
@Composable
fun SequenceRecorderView(
    currentSequence: String,
    onSequenceSaved: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialList = remember(currentSequence) { TapSequenceHelper.parseSequence(currentSequence) }
    val recordingSteps = remember { mutableStateListOf<Int>().apply { addAll(initialList) } }
    var lastTappedZone by remember { mutableStateOf<Int?>(null) }

    fun addStep(zone: Int) {
        if (recordingSteps.size >= 4) {
            recordingSteps.clear()
        }
        recordingSteps.add(zone)
        lastTappedZone = zone
        if (recordingSteps.size == 4) {
            onSequenceSaved(TapSequenceHelper.serializeSequence(recordingSteps.toList()))
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Create Your Secret Tap Pattern",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (recordingSteps.size < 4) "Tap any 4 zones on the screen pad below in your custom secret order."
                       else "✓ Sequence recorded! Tapping during blackout will restore your device.",
                fontSize = 12.sp,
                color = if (recordingSteps.size < 4) TextSecondary else Color(0xFF00E599),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive 4-Zone Screen Pad (Aspect Ratio of Phone)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.68f)
                    .aspectRatio(1.05f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF141418))
                    .border(1.5.dp, Color(0xFF2C2C35), RoundedCornerShape(18.dp))
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        ZoneButton(
                            zone = 1,
                            title = "Top-Left",
                            shortName = "TL",
                            stepIndex = recordingSteps.indexOf(1).takeIf { it >= 0 }?.plus(1),
                            isLastTapped = lastTappedZone == 1,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                            onClick = { addStep(1) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        ZoneButton(
                            zone = 2,
                            title = "Top-Right",
                            shortName = "TR",
                            stepIndex = recordingSteps.indexOf(2).takeIf { it >= 0 }?.plus(1),
                            isLastTapped = lastTappedZone == 2,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                            onClick = { addStep(2) }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        ZoneButton(
                            zone = 3,
                            title = "Bottom-Left",
                            shortName = "BL",
                            stepIndex = recordingSteps.indexOf(3).takeIf { it >= 0 }?.plus(1),
                            isLastTapped = lastTappedZone == 3,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                            onClick = { addStep(3) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        ZoneButton(
                            zone = 4,
                            title = "Bottom-Right",
                            shortName = "BR",
                            stepIndex = recordingSteps.indexOf(4).takeIf { it >= 0 }?.plus(1),
                            isLastTapped = lastTappedZone == 4,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                            onClick = { addStep(4) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sequence Slots
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                for (i in 0 until 4) {
                    val stepZone = recordingSteps.getOrNull(i)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (stepZone != null) Color(0xFF00E599).copy(alpha = 0.15f) else Color(0xFF1B1B22))
                            .border(
                                width = 1.dp,
                                color = if (stepZone != null) Color(0xFF00E599) else Color(0xFF2C2C35),
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (stepZone != null) {
                            Text(
                                text = TapSequenceHelper.getZoneShortName(stepZone),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E599)
                            )
                        } else {
                            Text(
                                text = "${i + 1}",
                                fontSize = 12.sp,
                                color = Color(0xFF555562)
                            )
                        }
                    }
                    if (i < 3) {
                        Text(
                            text = "→",
                            fontSize = 12.sp,
                            color = Color(0xFF555562),
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sequence text & Reset button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (recordingSteps.size == 4) "Saved: ${TapSequenceHelper.formatSequence(TapSequenceHelper.serializeSequence(recordingSteps))}"
                           else "Tap ${4 - recordingSteps.size} more zone(s)...",
                    fontSize = 12.sp,
                    color = if (recordingSteps.size == 4) Color(0xFF00E599) else TextSecondary,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "Clear",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8E8E93),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            recordingSteps.clear()
                            lastTappedZone = null
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun ZoneButton(
    zone: Int,
    title: String,
    shortName: String,
    stepIndex: Int?,
    isLastTapped: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isLastTapped) Color(0xFF00E599).copy(alpha = 0.25f)
                      else if (stepIndex != null) Color(0xFF1E2622)
                      else Color(0xFF1B1B22),
        animationSpec = tween(180),
        label = "zone_bg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isLastTapped) Color(0xFF00E599)
                      else if (stepIndex != null) Color(0xFF00E599).copy(alpha = 0.5f)
                      else Color(0xFF2C2C35),
        animationSpec = tween(180),
        label = "zone_border"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = shortName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (stepIndex != null) Color(0xFF00E599) else Color(0xFF8E8E93)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                color = if (stepIndex != null) Color(0xFF00E599).copy(alpha = 0.8f) else Color(0xFF555562)
            )
        }

        if (stepIndex != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00E599)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$stepIndex",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}
