package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.ui.MainViewModel
import com.example.ui.ScreenMode
import com.example.ui.chat.ChatScreen
import com.example.ui.history.HistoryBottomSheet
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.voice.VoiceAssistantScreen

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val screenMode by viewModel.screenMode.collectAsState()
    var showHistorySheet by remember { mutableStateOf(false) }

    // Audio permission launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // Permission denied info
        }
    }

    LaunchedEffect(Unit) {
        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    // Hardware and gesture Back button handling
    BackHandler(enabled = screenMode != ScreenMode.CHAT) {
        viewModel.setScreenMode(ScreenMode.CHAT)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .safeDrawingPadding()
    ) {
        when (screenMode) {
            ScreenMode.CHAT -> {
                ChatScreen(
                    viewModel = viewModel,
                    onOpenHistory = { showHistorySheet = true },
                    onOpenSettings = { viewModel.setScreenMode(ScreenMode.SETTINGS) }
                )
            }
            ScreenMode.VOICE_HUD -> {
                VoiceAssistantScreen(
                    viewModel = viewModel,
                    onClose = { viewModel.setScreenMode(ScreenMode.CHAT) }
                )
            }
            ScreenMode.SETTINGS -> {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.setScreenMode(ScreenMode.CHAT) }
                )
            }
        }

        if (showHistorySheet) {
            HistoryBottomSheet(
                viewModel = viewModel,
                onDismiss = { showHistorySheet = false }
            )
        }
    }
}
