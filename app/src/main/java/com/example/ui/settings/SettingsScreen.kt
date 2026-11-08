package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
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

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val customApiKey by viewModel.customApiKey.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val autoSpeak by viewModel.autoSpeakEnabled.collectAsState()
    val speechRate by viewModel.speechRate.collectAsState()
    val speechPitch by viewModel.speechPitch.collectAsState()
    val isTestingApi by viewModel.isTestingApi.collectAsState()
    val apiTestResult by viewModel.apiTestResult.collectAsState()

    var apiKeyInput by remember(customApiKey) { mutableStateOf(customApiKey) }
    var showClearDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
    ) {
        // Top Bar
        Surface(
            color = JarvisSurface,
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, JarvisBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = JarvisCyan
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "ସେଟିଙ୍ଗସ୍ (SETTINGS)",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 16.sp,
                    color = JarvisCyanLight,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Settings Content Scrollable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Language Preference Section
            SettingsCard(
                title = "ପ୍ରାଥମିକ ଭାଷା (LANGUAGE)",
                icon = Icons.Default.Language
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LanguageOptionRow(
                        title = "ଓଡ଼ିଆ (Odia) - Default",
                        subtitle = "ସମସ୍ତ ଉତ୍ତର ଏବଂ ସ୍ୱର ଓଡ଼ିଆରେ",
                        isSelected = language == "or",
                        onClick = { viewModel.setLanguage("or") }
                    )
                    LanguageOptionRow(
                        title = "हिन्दी (Hindi)",
                        subtitle = "हिन्दी भाषा में बातचीत और आवाज",
                        isSelected = language == "hi",
                        onClick = { viewModel.setLanguage("hi") }
                    )
                    LanguageOptionRow(
                        title = "English",
                        subtitle = "Conversations & responses in English",
                        isSelected = language == "en",
                        onClick = { viewModel.setLanguage("en") }
                    )
                }
            }

            // 2. Voice & Speech Settings
            SettingsCard(
                title = "ଭଏସ୍ ସେଟିଙ୍ଗସ୍ (VOICE SETTINGS)",
                icon = Icons.Default.RecordVoiceOver
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Auto-speak toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ସ୍ୱୟଂଚାଳିତ ଭଏସ୍ ଉତ୍ତର (Auto-Speak)",
                                color = JarvisTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "ଉତ୍ତର ଆସିବା ମାତ୍ରେ ଜାର୍ଭିସ୍ ପଢ଼ି ଶୁଣାଇବ",
                                color = JarvisTextMuted,
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = autoSpeak,
                            onCheckedChange = { viewModel.setAutoSpeak(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = JarvisBackground,
                                checkedTrackColor = JarvisCyan,
                                uncheckedThumbColor = JarvisTextMuted,
                                uncheckedTrackColor = JarvisSurfaceVariant
                            ),
                            modifier = Modifier.testTag("auto_speak_switch")
                        )
                    }

                    // Speech Rate Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "ସ୍ୱର ଗତି (Speech Speed)",
                                color = JarvisTextPrimary,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "%.1fx".format(speechRate),
                                color = JarvisCyan,
                                fontFamily = OrbitronFontFamily,
                                fontSize = 12.sp
                            )
                        }
                        Slider(
                            value = speechRate,
                            onValueChange = { viewModel.setSpeechRate(it) },
                            valueRange = 0.6f..1.6f,
                            colors = SliderDefaults.colors(
                                thumbColor = JarvisCyan,
                                activeTrackColor = JarvisCyan,
                                inactiveTrackColor = JarvisSurfaceElevated
                            )
                        )
                    }

                    // Speech Pitch Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "ସ୍ୱର ପିଚ୍ (Pitch)",
                                color = JarvisTextPrimary,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "%.1fx".format(speechPitch),
                                color = JarvisPurpleNeon,
                                fontFamily = OrbitronFontFamily,
                                fontSize = 12.sp
                            )
                        }
                        Slider(
                            value = speechPitch,
                            onValueChange = { viewModel.setSpeechPitch(it) },
                            valueRange = 0.7f..1.5f,
                            colors = SliderDefaults.colors(
                                thumbColor = JarvisPurpleNeon,
                                activeTrackColor = JarvisPurpleNeon,
                                inactiveTrackColor = JarvisSurfaceElevated
                            )
                        )
                    }

                    // Test Voice Button
                    OutlinedButton(
                        onClick = {
                            val testMessage = when (language) {
                                "hi" -> "नमस्ते, मैं ओडिया जार्विस एआई सहायक हूँ।"
                                "en" -> "Hello, I am Odia Jarvis AI assistant."
                                else -> "ନମସ୍କାର, ମୁଁ ଆପଣଙ୍କ ଓଡ଼ିଆ ଜାର୍ଭିସ୍ ଏଆଇ ସହାୟକ।"
                            }
                            viewModel.speakText(testMessage)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_voice_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Test voice",
                            tint = JarvisCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ସ୍ୱର ପରୀକ୍ଷା କରନ୍ତୁ (Test Voice)",
                            color = JarvisCyanLight,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // 3. AI Model & API Configuration
            SettingsCard(
                title = "AI ମଡେଲ୍ ଏବଂ API (AI ENGINE)",
                icon = Icons.Default.SmartToy
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "AI Model ଚୟନ କରନ୍ତୁ:",
                        color = JarvisTextSecondary,
                        fontSize = 13.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ModelChip(
                            title = "Gemini 2.5 Flash",
                            subtitle = "ଦ୍ରୁତ ଏବଂ ସଠିକ୍",
                            isSelected = selectedModel == "gemini-2.5-flash",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setModel("gemini-2.5-flash") }
                        )
                        ModelChip(
                            title = "Gemini 2.5 Pro",
                            subtitle = "ଗଭୀର ଚିନ୍ତନ",
                            isSelected = selectedModel == "gemini-2.5-pro",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setModel("gemini-2.5-pro") }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // API Key Field (allows override or custom input)
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = {
                            apiKeyInput = it
                            viewModel.setCustomApiKey(it)
                        },
                        label = {
                            Text(
                                "Custom Gemini API Key (Optional)",
                                color = JarvisTextSecondary,
                                fontSize = 12.sp
                            )
                        },
                        placeholder = {
                            Text(
                                "Using default Secrets panel key",
                                color = JarvisTextMuted,
                                fontSize = 12.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary,
                            focusedContainerColor = JarvisSurfaceElevated,
                            unfocusedContainerColor = JarvisSurfaceElevated
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_api_key_field")
                    )

                    // Test API Connection Button
                    Button(
                        onClick = { viewModel.testApiConnection() },
                        enabled = !isTestingApi,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JarvisSurfaceElevated,
                            contentColor = JarvisCyan
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_api_button")
                    ) {
                        if (isTestingApi) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = JarvisCyan,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ସଂଯୋଗ ଯାଞ୍ଚ ହେଉଛି...",
                                color = JarvisCyan,
                                fontSize = 13.sp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = "Test connection",
                                tint = JarvisCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "API ସଂଯୋଗ ପରୀକ୍ଷା କରନ୍ତୁ (Test Connection)",
                                color = JarvisCyanLight,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Test Result text
                    apiTestResult?.let { res ->
                        Text(
                            text = res,
                            color = if (res.startsWith("ସଫଳ")) JarvisGreen else JarvisRed,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // 4. Data & Privacy
            SettingsCard(
                title = "ଡାଟା ପରିଚାଳନା (DATA)",
                icon = Icons.Default.Delete
            ) {
                OutlinedButton(
                    onClick = { showClearDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisRed.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_clear_all_button")
                ) {
                    Text(
                        text = "ସମସ୍ତ ଚାଟ୍ ଇତିହାସ ବିଲୋପ କରନ୍ତୁ (Clear History)",
                        color = JarvisRed,
                        fontSize = 13.sp
                    )
                }
            }

            // 5. Developer & About Information
            Surface(
                color = JarvisSurfaceVariant,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(JarvisCyan.copy(alpha = 0.15f))
                            .border(1.5.dp, JarvisCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "OJ",
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = JarvisCyan,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "ODIA JARVIS AI",
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = JarvisCyanLight,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Advanced Futuristic Odia AI Assistant",
                        color = JarvisTextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Developer",
                            tint = JarvisPurpleNeon,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Developer: Tikeswar",
                            fontWeight = FontWeight.SemiBold,
                            color = JarvisPurpleNeon,
                            fontSize = 13.sp
                        )
                    }

                    Text(
                        text = "Powered by Google Gemini 2.5 • Version 1.0",
                        color = JarvisTextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = JarvisSurfaceElevated,
            title = {
                Text(
                    text = "ସମସ୍ତ ଚାଟ୍ ଡିଲିଟ୍ କରିବେ?",
                    color = JarvisTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "ଆପଣଙ୍କର ସମସ୍ତ ସଂରକ୍ଷିତ ବାର୍ତ୍ତାଳାପ ଡିଲିଟ୍ ହୋଇଯିବ।",
                    color = JarvisTextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllChats()
                        showClearDialog = false
                    }
                ) {
                    Text("ଡିଲିଟ୍ କରନ୍ତୁ", color = JarvisRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("ବାତିଲ", color = JarvisTextSecondary)
                }
            }
        )
    }
}

@Composable
fun SettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Surface(
        color = JarvisSurfaceVariant,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = JarvisCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontFamily = OrbitronFontFamily,
                    fontSize = 12.sp,
                    color = JarvisCyanLight,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
            content()
        }
    }
}

@Composable
fun LanguageOptionRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) JarvisSurfaceElevated else JarvisSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) JarvisCyan else JarvisBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    color = if (isSelected) JarvisCyanLight else JarvisTextPrimary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp
                )
                Text(
                    text = subtitle,
                    color = JarvisTextMuted,
                    fontSize = 11.sp
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = JarvisCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ModelChip(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) JarvisSurfaceElevated else JarvisSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) JarvisPurpleNeon else JarvisBorder
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontFamily = OrbitronFontFamily,
                color = if (isSelected) JarvisPurpleNeon else JarvisTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = JarvisTextMuted,
                fontSize = 10.sp
            )
        }
    }
}
