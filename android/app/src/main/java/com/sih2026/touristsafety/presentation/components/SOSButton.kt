package com.sih2026.touristsafety.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sih2026.touristsafety.presentation.theme.SosRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SOSButton(
    onSOSTriggered: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf(3) }
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(200.dp)
            .scale(if (!isPressed) pulseScale else 1f)
            .clip(CircleShape)
            .background(SosRed.copy(alpha = 0.2f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (!isPressed) {
                    isPressed = true
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    coroutineScope.launch {
                        while (countdown > 0) {
                            delay(1000)
                            countdown--
                        }
                        onSOSTriggered()
                        isPressed = false
                        countdown = 3
                    }
                }
            }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(SosRed)
        ) {
            Text(
                text = if (isPressed) "$countdown" else "SOS",
                color = Color.White,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
