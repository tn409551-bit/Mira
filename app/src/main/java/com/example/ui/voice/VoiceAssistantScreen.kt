package com.example.ui.voice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.ScreenMode
import com.example.ui.components.JarvisOrb
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisPurple
import com.example.ui.theme.JarvisPurpleNeon
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.OrbitronFontFamily
import com.example.voice.JarvisVoiceState

@Composable
fun VoiceAssistantScreen(
    viewModel: MainViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val voiceState by viewModel.voiceState.collectAsState()
    val rmsVolume by viewModel.rmsVolume.collectAsState()
    val partialSpeech by viewModel.partialSpeech.collectAsState()
    val lastTranscript by viewModel.lastVoiceTranscript.collectAsState()
    val lastResponse by viewModel.lastJarvisResponse.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val language by viewModel.language.collectAsState()

    val isListening = voiceState == JarvisVoiceState.LISTENING
    val isSpeaking = voiceState == JarvisVoiceState.SPEAKING
    val isProcessing = voiceState == JarvisVoiceState.PROCESSING || isGenerating

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        JarvisBackground,
                        Color(0xFF090D1A),
                        JarvisBackground
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top HUD Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "JARVIS VOICE INTERFACE",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 13.sp,
                    color = JarvisCyanLight,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isListening -> JarvisCyan
                                    isSpeaking -> JarvisPurpleNeon
                                    isProcessing -> JarvisGreen
                                    else -> JarvisGreen
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "VOICE ENGINE: " + when (language) {
                            "hi" -> "HINDI (हिन्दी)"
                            "en" -> "ENGLISH"
                            else -> "ODIA (ଓଡ଼ିଆ)"
                        },
                        fontSize = 10.sp,
                        color = JarvisTextSecondary,
                        fontFamily = OrbitronFontFamily
                    )
                }
            }

            Row {
                IconButton(
                    onClick = { viewModel.setScreenMode(ScreenMode.CHAT) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(JarvisSurfaceVariant)
                        .testTag("switch_to_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Forum,
                        contentDescription = "Switch to Chat",
                        tint = JarvisCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(JarvisSurfaceVariant)
                        .testTag("close_voice_hud_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close HUD",
                        tint = JarvisTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Center Content Scrollable
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Jarvis Orb
            JarvisOrb(
                voiceState = voiceState,
                rmsVolume = rmsVolume,
                size = 230.dp,
                modifier = Modifier.testTag("animated_jarvis_orb")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // State Badge Text
            val statusLabel = when {
                isListening -> "ଶୁଣୁଛି... (LISTENING)"
                isProcessing -> "ଚିନ୍ତା କରୁଛି... (PROCESSING)"
                isSpeaking -> "ଜାର୍ଭିସ୍ କହୁଛି... (SPEAKING)"
                else -> "ପ୍ରସ୍ତୁତ (READY • TAP MIC TO TALK)"
            }

            Surface(
                color = JarvisSurfaceVariant,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder)
            ) {
                Text(
                    text = statusLabel,
                    fontFamily = OrbitronFontFamily,
                    fontSize = 13.sp,
                    color = when {
                        isListening -> JarvisCyanLight
                        isSpeaking -> JarvisPurpleNeon
                        isProcessing -> JarvisGreen
                        else -> JarvisCyan
                    },
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Waveform Equalizer
            WaveformVisualizer(
                voiceState = voiceState,
                rmsVolume = rmsVolume,
                barCount = 24,
                maxHeight = 36.dp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Holographic Subtitles Box
            Surface(
                color = JarvisSurface.copy(alpha = 0.85f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // User query preview
                    val userQuery = if (isListening && partialSpeech.isNotBlank()) {
                        partialSpeech
                    } else if (lastTranscript.isNotBlank()) {
                        lastTranscript
                    } else {
                        "ମାଇକ୍ରୋଫୋନ୍ ଦବାଇ ଓଡ଼ିଆରେ କୁହନ୍ତୁ (Tap mic and speak)..."
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "USER INPUT:",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 10.sp,
                            color = JarvisCyanLight,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = userQuery,
                        color = JarvisTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Jarvis response preview
                    if (lastResponse.isNotBlank() || isProcessing) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "JARVIS OUTPUT:",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 10.sp,
                                color = JarvisPurpleNeon,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = if (isProcessing) "ଉତ୍ତର ପ୍ରସ୍ତୁତ ହେଉଛି..." else lastResponse,
                            color = JarvisTextSecondary,
                            fontSize = 14.sp,
                            maxLines = 6,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Quick Suggestion Chips
        val quickPrompts = viewModel.getQuickPrompts().take(4)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickPrompts.forEach { qp ->
                Surface(
                    color = JarvisSurfaceElevated,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder),
                    modifier = Modifier.clickable {
                        viewModel.sendMessage(qp.prompt)
                    }
                ) {
                    Text(
                        text = qp.title,
                        color = JarvisTextPrimary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Bottom Voice Control Deck
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stop speaking button if active
            AnimatedVisibility(visible = isSpeaking, enter = fadeIn(), exit = fadeOut()) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(JarvisRed.copy(alpha = 0.2f))
                        .border(1.5.dp, JarvisRed, CircleShape)
                        .clickable { viewModel.stopSpeaking() }
                        .testTag("stop_voice_speaking_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop Speaking",
                        tint = JarvisRed,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            if (isSpeaking) {
                Spacer(modifier = Modifier.width(20.dp))
            }

            // Giant Futuristic Mic Button
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                if (isListening) JarvisRed.copy(alpha = 0.35f) else JarvisCyan.copy(alpha = 0.35f),
                                JarvisSurfaceVariant
                            )
                        )
                    )
                    .border(
                        2.5.dp,
                        if (isListening) JarvisRed else JarvisCyan,
                        CircleShape
                    )
                    .clickable {
                        if (isListening) {
                            viewModel.stopVoiceInput()
                        } else {
                            viewModel.startVoiceInput()
                        }
                    }
                    .testTag("main_voice_mic_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Microphone",
                    tint = if (isListening) JarvisRed else JarvisCyanLight,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}
