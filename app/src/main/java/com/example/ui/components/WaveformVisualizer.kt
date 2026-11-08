package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisPurpleNeon
import com.example.voice.JarvisVoiceState

@Composable
fun WaveformVisualizer(
    voiceState: JarvisVoiceState,
    rmsVolume: Float = 0f,
    modifier: Modifier = Modifier,
    barCount: Int = 18,
    maxHeight: Dp = 48.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")

    val isActive = voiceState != JarvisVoiceState.IDLE

    Row(
        modifier = modifier.height(maxHeight),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val animDuration = 400 + (i % 5) * 120
            val animDelay = (i * 60) % 300

            val animatedHeight by infiniteTransition.animateFloat(
                initialValue = 0.15f,
                targetValue = if (isActive) 0.95f else 0.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = animDuration, delayMillis = animDelay, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$i"
            )

            // Dynamic height factor based on state and RMS
            val heightFraction = if (voiceState == JarvisVoiceState.LISTENING) {
                (0.2f + (rmsVolume * 0.8f) * animatedHeight).coerceIn(0.1f, 1f)
            } else if (isActive) {
                animatedHeight
            } else {
                0.15f
            }

            val brush = when (voiceState) {
                JarvisVoiceState.LISTENING -> Brush.verticalGradient(
                    listOf(JarvisCyan, Color(0xFF00B4D8))
                )
                JarvisVoiceState.SPEAKING -> Brush.verticalGradient(
                    listOf(JarvisPurpleNeon, JarvisCyan)
                )
                JarvisVoiceState.PROCESSING -> Brush.verticalGradient(
                    listOf(Color(0xFF00FF9D), JarvisCyan)
                )
                JarvisVoiceState.IDLE -> Brush.verticalGradient(
                    listOf(Color(0x5500F0FF), Color(0x2200F0FF))
                )
            }

            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .height(maxHeight * heightFraction)
                    .clip(RoundedCornerShape(2.dp))
                    .background(brush)
            )
        }
    }
}
