package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisPurple
import com.example.ui.theme.JarvisPurpleNeon
import com.example.voice.JarvisVoiceState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun JarvisOrb(
    voiceState: JarvisVoiceState,
    rmsVolume: Float = 0f,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "jarvis_orb_transition")

    // Slow continuous outer rotation
    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_rotation"
    )

    // Reverse middle ring rotation (faster when processing)
    val middleDuration = when (voiceState) {
        JarvisVoiceState.PROCESSING -> 3000
        JarvisVoiceState.SPEAKING -> 6000
        JarvisVoiceState.LISTENING -> 5000
        JarvisVoiceState.IDLE -> 9000
    }
    val middleRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = middleDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "middle_rotation"
    )

    // Breathing pulse
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_pulse"
    )

    // Voice-reactive volume animatable
    val animatedVolume = remember { Animatable(0f) }
    LaunchedEffect(rmsVolume) {
        animatedVolume.animateTo(
            targetValue = rmsVolume.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 80)
        )
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 2f * 0.9f

            // Dynamic scale factor based on state and voice volume
            val scaleFactor = when (voiceState) {
                JarvisVoiceState.LISTENING -> 0.95f + (animatedVolume.value * 0.25f)
                JarvisVoiceState.SPEAKING -> breathingPulse * (0.95f + (animatedVolume.value * 0.15f))
                JarvisVoiceState.PROCESSING -> breathingPulse * 1.02f
                JarvisVoiceState.IDLE -> breathingPulse * 0.95f
            }

            val primaryColor = when (voiceState) {
                JarvisVoiceState.LISTENING -> JarvisCyan
                JarvisVoiceState.SPEAKING -> JarvisPurpleNeon
                JarvisVoiceState.PROCESSING -> JarvisGreen
                JarvisVoiceState.IDLE -> JarvisCyan
            }

            val secondaryColor = when (voiceState) {
                JarvisVoiceState.LISTENING -> JarvisCyanLight
                JarvisVoiceState.SPEAKING -> JarvisCyan
                JarvisVoiceState.PROCESSING -> JarvisPurple
                JarvisVoiceState.IDLE -> JarvisPurple
            }

            // 1. Outer Glow halo
            val glowRadius = baseRadius * scaleFactor
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.35f),
                        secondaryColor.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = glowRadius * 1.15f
                ),
                radius = glowRadius * 1.15f,
                center = center
            )

            // 2. Outer Ring with holographic arcs & ticks
            rotate(degrees = outerRotation, pivot = center) {
                val outerRadius = baseRadius * 0.92f * scaleFactor
                // Segmented arcs
                drawArc(
                    color = primaryColor.copy(alpha = 0.7f),
                    startAngle = 0f,
                    sweepAngle = 70f,
                    useCenter = false,
                    topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                    size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2),
                    style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                )
                drawArc(
                    color = primaryColor.copy(alpha = 0.7f),
                    startAngle = 120f,
                    sweepAngle = 70f,
                    useCenter = false,
                    topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                    size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2),
                    style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                )
                drawArc(
                    color = primaryColor.copy(alpha = 0.7f),
                    startAngle = 240f,
                    sweepAngle = 70f,
                    useCenter = false,
                    topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                    size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2),
                    style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                )

                // Outer ticks
                val tickCount = 24
                for (i in 0 until tickCount) {
                    val angle = (i * (360f / tickCount)) * (Math.PI / 180f).toFloat()
                    val tickStartR = outerRadius + 4f
                    val tickLength = if (i % 4 == 0) 10f else 5f
                    val tickEndR = tickStartR + tickLength
                    val startX = center.x + tickStartR * cos(angle)
                    val startY = center.y + tickStartR * sin(angle)
                    val endX = center.x + tickEndR * cos(angle)
                    val endY = center.y + tickEndR * sin(angle)
                    drawLine(
                        color = secondaryColor.copy(alpha = if (i % 4 == 0) 0.8f else 0.4f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (i % 4 == 0) 2f else 1f
                    )
                }
            }

            // 3. Middle Counter-Rotating Ring
            rotate(degrees = middleRotation, pivot = center) {
                val midRadius = baseRadius * 0.72f * scaleFactor
                drawArc(
                    color = secondaryColor.copy(alpha = 0.85f),
                    startAngle = 30f,
                    sweepAngle = 100f,
                    useCenter = false,
                    topLeft = Offset(center.x - midRadius, center.y - midRadius),
                    size = androidx.compose.ui.geometry.Size(midRadius * 2, midRadius * 2),
                    style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                )
                drawArc(
                    color = secondaryColor.copy(alpha = 0.85f),
                    startAngle = 180f,
                    sweepAngle = 100f,
                    useCenter = false,
                    topLeft = Offset(center.x - midRadius, center.y - midRadius),
                    size = androidx.compose.ui.geometry.Size(midRadius * 2, midRadius * 2),
                    style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                )

                // Small dots along circle
                for (d in 0 until 8) {
                    val angle = (d * 45f) * (Math.PI / 180f).toFloat()
                    val dotX = center.x + midRadius * cos(angle)
                    val dotY = center.y + midRadius * sin(angle)
                    drawCircle(
                        color = primaryColor,
                        radius = 2.5f,
                        center = Offset(dotX, dotY)
                    )
                }
            }

            // 4. Inner Ring with high tech hex/circle
            val innerRingRadius = baseRadius * 0.48f * scaleFactor
            drawCircle(
                color = primaryColor.copy(alpha = 0.4f),
                radius = innerRingRadius,
                center = center,
                style = Stroke(width = 1.5f)
            )

            // 5. Glowing Central Core (Arc Reactor Core)
            val coreRadius = baseRadius * 0.32f * scaleFactor
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        primaryColor,
                        secondaryColor,
                        Color.Transparent
                    ),
                    center = center,
                    radius = coreRadius * 1.2f
                ),
                radius = coreRadius,
                center = center
            )

            // Central holographic iris icon / triangle design
            val path = Path()
            val triRadius = coreRadius * 0.55f
            for (p in 0 until 3) {
                val angle = (p * 120f - 90f + outerRotation * 0.5f) * (Math.PI / 180f).toFloat()
                val px = center.x + triRadius * cos(angle)
                val py = center.y + triRadius * sin(angle)
                if (p == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.9f),
                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
            )
        }
    }
}
