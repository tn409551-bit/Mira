package com.example.ui.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatMessageEntity
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    viewModel: MainViewModel,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()
    val rmsVolume by viewModel.rmsVolume.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val partialSpeech by viewModel.partialSpeech.collectAsState()
    val language by viewModel.language.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current

    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
    ) {
        // Futuristic Top Bar
        JarvisTopBar(
            onNewChat = { viewModel.createNewChat() },
            onOpenVoiceHud = { viewModel.setScreenMode(ScreenMode.VOICE_HUD) },
            onOpenHistory = onOpenHistory,
            onOpenSettings = onOpenSettings
        )

        // Active Speaking / Listening Banner
        AnimatedVisibility(
            visible = voiceState != JarvisVoiceState.IDLE,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                color = JarvisSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        WaveformVisualizer(
                            voiceState = voiceState,
                            rmsVolume = rmsVolume,
                            barCount = 10,
                            maxHeight = 22.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = when (voiceState) {
                                JarvisVoiceState.LISTENING -> if (partialSpeech.isNotBlank()) partialSpeech else "ଶୁଣୁଛି (Listening)..."
                                JarvisVoiceState.SPEAKING -> "ଜାର୍ଭିସ୍ କହୁଛି (Speaking)..."
                                JarvisVoiceState.PROCESSING -> "ଚିନ୍ତା କରୁଛି (Processing)..."
                                else -> ""
                            },
                            color = JarvisCyanLight,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }

                    if (voiceState == JarvisVoiceState.SPEAKING) {
                        IconButton(
                            onClick = { viewModel.stopSpeaking() },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("stop_speaking_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop speaking",
                                tint = JarvisRed
                            )
                        }
                    } else if (voiceState == JarvisVoiceState.LISTENING) {
                        IconButton(
                            onClick = { viewModel.stopVoiceInput() },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("stop_listening_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MicOff,
                                contentDescription = "Stop listening",
                                tint = JarvisCyan
                            )
                        }
                    }
                }
            }
        }

        // Status / Error message banner
        statusMessage?.let { err ->
            Surface(
                color = JarvisRed.copy(alpha = 0.15f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisRed.copy(alpha = 0.4f))
            ) {
                Text(
                    text = err,
                    color = JarvisRed,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                ChatMessageItem(
                    message = message,
                    onSpeak = { viewModel.speakText(message.text) },
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Jarvis Message", message.text)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "କପି ହୋଇଗଲା (Copied)", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Generating indicator
            if (isGenerating) {
                item {
                    GeneratingIndicator()
                }
            }
        }

        // Quick Suggestions Horizontal Row
        val quickPrompts = viewModel.getQuickPrompts()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickPrompts.forEach { prompt ->
                Surface(
                    color = JarvisSurfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder),
                    modifier = Modifier
                        .clickable { viewModel.sendMessage(prompt.prompt) }
                        .testTag("quick_prompt_${prompt.category}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(JarvisCyan)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = prompt.title,
                            color = JarvisTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Bottom Input Row
        ChatInputBar(
            text = inputText,
            onTextChange = { inputText = it },
            onSend = {
                if (inputText.isNotBlank()) {
                    viewModel.sendMessage(inputText)
                    inputText = ""
                }
            },
            isGenerating = isGenerating,
            voiceState = voiceState,
            onMicClick = {
                if (voiceState == JarvisVoiceState.LISTENING) {
                    viewModel.stopVoiceInput()
                } else {
                    viewModel.startVoiceInput()
                }
            },
            language = language
        )
    }
}

@Composable
fun JarvisTopBar(
    onNewChat: () -> Unit,
    onOpenVoiceHud: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Surface(
        color = JarvisSurface,
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, JarvisBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mini futuristic reactor badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(JarvisCyan.copy(alpha = 0.2f), JarvisPurple.copy(alpha = 0.2f))
                            )
                        )
                        .border(1.dp, JarvisCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(JarvisCyan)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "ODIA JARVIS AI",
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = JarvisCyanLight,
                        letterSpacing = 0.8.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(JarvisGreen)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "ONLINE • ଓଡ଼ିଆ AI ସହାୟକ",
                            color = JarvisTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Top action buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Futuristic Voice HUD Mode Switch
                IconButton(
                    onClick = onOpenVoiceHud,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(JarvisSurfaceVariant)
                        .border(1.dp, JarvisCyan.copy(alpha = 0.4f), CircleShape)
                        .testTag("open_voice_hud_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Open Voice HUD",
                        tint = JarvisCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // New Chat Button
                IconButton(
                    onClick = onNewChat,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(JarvisSurfaceVariant)
                        .testTag("new_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Chat",
                        tint = JarvisTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // History Button
                IconButton(
                    onClick = onOpenHistory,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Chat History",
                        tint = JarvisTextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Settings Button
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = JarvisTextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessageEntity,
    onSpeak: () -> Unit,
    onCopy: () -> Unit
) {
    val isUser = message.sender == "user"
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            // Assistant avatar orb
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(JarvisSurfaceVariant)
                    .border(1.dp, JarvisCyan.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "J",
                    fontFamily = OrbitronFontFamily,
                    color = JarvisCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 310.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                color = if (isUser) {
                    JarvisSurfaceElevated
                } else if (message.isError) {
                    JarvisRed.copy(alpha = 0.12f)
                } else {
                    JarvisSurfaceVariant
                },
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isUser) JarvisPurple.copy(alpha = 0.5f) else JarvisBorder
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = message.text,
                        color = JarvisTextPrimary,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formattedTime,
                            color = JarvisTextMuted,
                            fontSize = 10.sp
                        )

                        if (!isUser) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Speak button
                                IconButton(
                                    onClick = onSpeak,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Speak message",
                                        tint = JarvisCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                // Copy button
                                IconButton(
                                    onClick = onCopy,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy message",
                                        tint = JarvisTextMuted,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GeneratingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(JarvisSurfaceVariant)
                .border(1.dp, JarvisCyan.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = JarvisCyan,
                strokeWidth = 2.dp
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Surface(
            color = JarvisSurfaceVariant,
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ଜାର୍ଭିସ୍ ଚିନ୍ତା କରୁଛି (Jarvis is thinking)...",
                    color = JarvisCyanLight,
                    fontSize = 12.sp,
                    fontFamily = OrbitronFontFamily
                )
            }
        }
    }
}

@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    isGenerating: Boolean,
    voiceState: JarvisVoiceState,
    onMicClick: () -> Unit,
    language: String
) {
    val hint = when (language) {
        "hi" -> "यहाँ पूछें या माइक दबाएँ..."
        "en" -> "Ask Jarvis anything or tap mic..."
        else -> "ଓଡ଼ିଆରେ ପଚାରନ୍ତୁ କିମ୍ବା ମାଇକ୍ ବ୍ୟବହାର କରନ୍ତୁ..."
    }

    Surface(
        color = JarvisSurface,
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, JarvisBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Voice Input Mic Button
            val isListening = voiceState == JarvisVoiceState.LISTENING
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (isListening) JarvisRed.copy(alpha = 0.2f) else JarvisSurfaceVariant
                    )
                    .border(
                        1.5.dp,
                        if (isListening) JarvisRed else JarvisCyan.copy(alpha = 0.6f),
                        CircleShape
                    )
                    .clickable { onMicClick() }
                    .testTag("microphone_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Voice input",
                    tint = if (isListening) JarvisRed else JarvisCyan,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Text Input Field
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp, max = 120.dp)
                    .testTag("chat_input_field"),
                placeholder = {
                    Text(
                        text = hint,
                        color = JarvisTextMuted,
                        fontSize = 13.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = JarvisCyan,
                    unfocusedBorderColor = JarvisBorder,
                    focusedTextColor = JarvisTextPrimary,
                    unfocusedTextColor = JarvisTextPrimary,
                    focusedContainerColor = JarvisSurfaceVariant,
                    unfocusedContainerColor = JarvisSurfaceVariant
                ),
                shape = RoundedCornerShape(24.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Send Button
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (text.isNotBlank() && !isGenerating) JarvisCyan else JarvisSurfaceElevated
                    )
                    .clickable(enabled = text.isNotBlank() && !isGenerating) { onSend() }
                    .testTag("send_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send message",
                    tint = if (text.isNotBlank() && !isGenerating) JarvisBackground else JarvisTextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
