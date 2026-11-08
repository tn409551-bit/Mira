package com.example.ui

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.repository.JarvisRepository
import com.example.voice.JarvisVoiceState
import com.example.voice.VoiceManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenMode {
    CHAT,
    VOICE_HUD,
    SETTINGS
}

data class QuickPrompt(
    val title: String,
    val prompt: String,
    val category: String,
    val iconName: String
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs: SharedPreferences =
        application.getSharedPreferences("odia_jarvis_prefs", Context.MODE_PRIVATE)

    private val database = AppDatabase.getDatabase(application)
    private val repository = JarvisRepository(database.chatDao())
    val voiceManager = VoiceManager(application)

    val sessions: StateFlow<List<ChatSessionEntity>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentSessionId = MutableStateFlow<Long?>(null)
    val currentSessionId: StateFlow<Long?> = _currentSessionId.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val messages: StateFlow<List<ChatMessageEntity>> = _messages.asStateFlow()

    private val _screenMode = MutableStateFlow(ScreenMode.CHAT)
    val screenMode: StateFlow<ScreenMode> = _screenMode.asStateFlow()

    // Preferences state
    private val _language = MutableStateFlow(prefs.getString("pref_language", "or") ?: "or")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _customApiKey = MutableStateFlow(prefs.getString("pref_custom_api_key", "") ?: "")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _selectedModel = MutableStateFlow(prefs.getString("pref_model", "gemini-2.5-flash") ?: "gemini-2.5-flash")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _autoSpeakEnabled = MutableStateFlow(prefs.getBoolean("pref_auto_speak", true))
    val autoSpeakEnabled: StateFlow<Boolean> = _autoSpeakEnabled.asStateFlow()

    private val _speechRate = MutableStateFlow(prefs.getFloat("pref_speech_rate", 1.0f))
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _speechPitch = MutableStateFlow(prefs.getFloat("pref_speech_pitch", 1.0f))
    val speechPitch: StateFlow<Float> = _speechPitch.asStateFlow()

    // AI & System status
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _apiTestResult = MutableStateFlow<String?>(null)
    val apiTestResult: StateFlow<String?> = _apiTestResult.asStateFlow()

    private val _isTestingApi = MutableStateFlow(false)
    val isTestingApi: StateFlow<Boolean> = _isTestingApi.asStateFlow()

    private val _lastVoiceTranscript = MutableStateFlow("")
    val lastVoiceTranscript: StateFlow<String> = _lastVoiceTranscript.asStateFlow()

    private val _lastJarvisResponse = MutableStateFlow("")
    val lastJarvisResponse: StateFlow<String> = _lastJarvisResponse.asStateFlow()

    val voiceState: StateFlow<JarvisVoiceState> = voiceManager.voiceState
    val rmsVolume: StateFlow<Float> = voiceManager.rmsVolume
    val partialSpeech: StateFlow<String> = voiceManager.partialText

    private var messagesJob: Job? = null

    init {
        voiceManager.speechRate = _speechRate.value
        voiceManager.speechPitch = _speechPitch.value
        initDefaultSession()
    }

    private fun getActiveApiKey(): String {
        val custom = _customApiKey.value.trim()
        if (custom.isNotBlank()) return custom
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            if (key.isBlank() || key == "PLACEHOLDER_API_KEY" || key.startsWith("MY_GEMINI")) "" else key
        } catch (e: Exception) {
            ""
        }
    }

    private fun initDefaultSession() {
        viewModelScope.launch {
            repository.allSessions.collect { sessionList ->
                if (_currentSessionId.value == null) {
                    if (sessionList.isNotEmpty()) {
                        selectSession(sessionList.first().id)
                    } else {
                        createNewChat()
                    }
                }
            }
        }
    }

    fun setScreenMode(mode: ScreenMode) {
        _screenMode.value = mode
    }

    fun selectSession(sessionId: Long) {
        _currentSessionId.value = sessionId
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            repository.getMessagesForSession(sessionId).collect { list ->
                _messages.value = list
            }
        }
    }

    fun createNewChat() {
        viewModelScope.launch {
            voiceManager.stopSpeaking()
            val initialTitle = when (_language.value) {
                "hi" -> "नई बातचीत"
                "en" -> "New Conversation"
                else -> "ନୂତନ ବାର୍ତ୍ତାଳାପ"
            }
            val newId = repository.createNewSession(title = initialTitle, language = _language.value)
            selectSession(newId)

            // Add Jarvis initial greeting
            val greeting = when (_language.value) {
                "hi" -> "नमस्ते! मैं आपका ओडिया जार्विस एआई सहायक हूँ। आज मैं आपकी क्या मदद कर सकता हूँ?"
                "en" -> "Hello! I am your Odia Jarvis AI assistant. How may I assist you today?"
                else -> "ନମସ୍କାର! ମୁଁ ଆପଣଙ୍କ ଓଡ଼ିଆ ଜାର୍ଭିସ୍ ଏଆଇ ସହାୟକ। ଆଜି ମୁଁ ଆପଣଙ୍କୁ କିପରି ସାହାଯ୍ୟ କରିପାରିବି?"
            }
            repository.saveMessage(newId, sender = "assistant", text = greeting, language = _language.value)
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_currentSessionId.value == sessionId) {
                _currentSessionId.value = null
                _messages.value = emptyList()
            }
        }
    }

    fun clearAllChats() {
        viewModelScope.launch {
            voiceManager.stopSpeaking()
            repository.clearAllHistory()
            _currentSessionId.value = null
            _messages.value = emptyList()
            createNewChat()
        }
    }

    fun sendMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isBlank() || _isGenerating.value) return

        val sessionId = _currentSessionId.value ?: return

        _lastVoiceTranscript.value = trimmed
        _statusMessage.value = null

        viewModelScope.launch {
            // Save user message
            repository.saveMessage(sessionId, sender = "user", text = trimmed, language = _language.value)

            _isGenerating.value = true
            val effectiveKey = getActiveApiKey()

            val result = repository.generateAiResponse(
                apiKey = effectiveKey,
                model = _selectedModel.value,
                sessionId = sessionId,
                userPrompt = trimmed,
                primaryLanguage = _language.value
            )

            _isGenerating.value = false

            result.onSuccess { replyText ->
                _lastJarvisResponse.value = replyText
                repository.saveMessage(sessionId, sender = "assistant", text = replyText, language = _language.value)
                if (_autoSpeakEnabled.value) {
                    voiceManager.speak(replyText, _language.value)
                }
            }.onFailure { error ->
                val errorMsg = error.message ?: "କିଛି ତ୍ରୁଟି ଘଟିଛି (Unknown error occurred)."
                _statusMessage.value = errorMsg
                repository.saveMessage(
                    sessionId,
                    sender = "assistant",
                    text = "⚠️ $errorMsg\n\nଦୟାକରି ସେଟିଙ୍ଗସରେ Gemini API Key କିମ୍ବା ଇଣ୍ଟରନେଟ୍ ସଂଯୋଗ ଯାଞ୍ଚ କରନ୍ତୁ।",
                    language = _language.value,
                    isError = true
                )
            }
        }
    }

    fun startVoiceInput() {
        voiceManager.startListening(
            languageCode = _language.value,
            onResult = { recognizedText ->
                sendMessage(recognizedText)
            },
            onError = { err ->
                _statusMessage.value = err
            }
        )
    }

    fun stopVoiceInput() {
        voiceManager.stopListening()
    }

    fun speakText(text: String) {
        voiceManager.speak(text, _language.value)
    }

    fun stopSpeaking() {
        voiceManager.stopSpeaking()
    }

    fun setLanguage(lang: String) {
        _language.value = lang
        prefs.edit().putString("pref_language", lang).apply()
    }

    fun setCustomApiKey(key: String) {
        _customApiKey.value = key
        prefs.edit().putString("pref_custom_api_key", key).apply()
    }

    fun setModel(model: String) {
        _selectedModel.value = model
        prefs.edit().putString("pref_model", model).apply()
    }

    fun setAutoSpeak(enabled: Boolean) {
        _autoSpeakEnabled.value = enabled
        prefs.edit().putBoolean("pref_auto_speak", enabled).apply()
    }

    fun setSpeechRate(rate: Float) {
        _speechRate.value = rate
        voiceManager.speechRate = rate
        prefs.edit().putFloat("pref_speech_rate", rate).apply()
    }

    fun setSpeechPitch(pitch: Float) {
        _speechPitch.value = pitch
        voiceManager.speechPitch = pitch
        prefs.edit().putFloat("pref_speech_pitch", pitch).apply()
    }

    fun testApiConnection() {
        viewModelScope.launch {
            _isTestingApi.value = true
            _apiTestResult.value = null
            val key = getActiveApiKey()
            val result = repository.testApiConnection(key, _selectedModel.value)
            _isTestingApi.value = false
            result.onSuccess {
                _apiTestResult.value = "ସଫଳ! Gemini AI ସଂଯୋଗ ସକ୍ରିୟ ଅଛି (Connected successfully): $it"
            }.onFailure {
                _apiTestResult.value = "ସଂଯୋଗ ବିଫଳ (Connection failed): ${it.localizedMessage}"
            }
        }
    }

    fun getQuickPrompts(): List<QuickPrompt> {
        return when (_language.value) {
            "hi" -> listOf(
                QuickPrompt("ओडिशा का इतिहास", "ओडिशा के समृद्ध इतिहास और कलिंग युद्ध के बारे में संक्षेप में बताइए।", "GK", "history"),
                QuickPrompt("ओडिया कहानी", "एक प्रेरणादायक और सुंदर ओडिया लोककथा सुनाइए।", "Story", "book"),
                QuickPrompt("गणित सहायता", "कक्षा 10वीं के पाइथागोरस प्रमेय को सरल उदाहरण के साथ समझाइए।", "Math", "calculate"),
                QuickPrompt("अनुवाद (Odia to English)", "'ମୁଁ ଆପଣଙ୍କୁ ବହୁତ ଧନ୍ୟବାଦ ଜଣାଉଛି' का अंग्रेजी में अनुवाद क्या होगा?", "Translate", "translate"),
                QuickPrompt("दैनिक सुविचार", "आज के लिए एक सकारात्मक और प्रेरणादायक सुविचार दीजिए।", "Daily", "lightbulb")
            )
            "en" -> listOf(
                QuickPrompt("Odisha History", "Explain the rich history of Odisha, Kalinga, and Jagannath culture.", "GK", "history"),
                QuickPrompt("Odia Story", "Tell a captivating traditional Odia folk tale with moral values.", "Story", "book"),
                QuickPrompt("Math Help", "Explain the Pythagorean theorem step-by-step with an example.", "Math", "calculate"),
                QuickPrompt("Translate (Odia)", "Translate 'Good morning, how can I help you today?' into Odia.", "Translate", "translate"),
                QuickPrompt("Daily Inspiration", "Share an inspiring quote and productive thought for today.", "Daily", "lightbulb")
            )
            else -> listOf(
                QuickPrompt("ଓଡ଼ିଶାର ଇତିହାସ", "ଓଡ଼ିଶାର ଗୌରବମୟ ଇତିହାସ, କଳିଙ୍ଗ ଯୁଦ୍ଧ ଏବଂ ଜଗନ୍ନାଥ ସଂସ୍କୃତି ବିଷୟରେ ସଂକ୍ଷେପରେ ବର୍ଣ୍ଣନା କରନ୍ତୁ।", "ଇତିହାସ", "history"),
                QuickPrompt("ସୁନ୍ଦର ଓଡ଼ିଆ ଗଳ୍ପ", "ଏକ ଶିକ୍ଷଣୀୟ ଏବଂ ମନଛୁଆଁ ପାରମ୍ପରିକ ଓଡ଼ିଆ କାହାଣୀ କୁହନ୍ତୁ।", "ଗଳ୍ପ", "book"),
                QuickPrompt("ଓଡ଼ିଆ କବିତା", "ପ୍ରକୃତି ଓ ଉତ୍କଳ ମାଟିକୁ ନେଇ ଏକ ଚମତ୍କାର ଓଡ଼ିଆ କବିତା ଲେଖନ୍ତୁ।", "କବିତା", "edit"),
                QuickPrompt("ଗଣିତ ସମାଧାନ", "ପାଇଥାଗୋରାସ୍ ସୂତ୍ର (Pythagoras theorem) କୁ ଏକ ସହଜ ଉଦାହରଣ ସହ ବୁଝାନ୍ତୁ।", "ଗଣିତ", "calculate"),
                QuickPrompt("ଭାଷା ଅନୁବାଦ", "'I am learning artificial intelligence' କୁ ଓଡ଼ିଆ ଏବଂ ହିନ୍ଦୀରେ ଅନୁବାଦ କରନ୍ତୁ।", "ଅନୁବାଦ", "translate"),
                QuickPrompt("ଆଜିର ସୁଚିନ୍ତା", "ଆଜିର ଦିନ ପାଇଁ ଏକ ପ୍ରେରଣାଦାୟୀ ଚିନ୍ତାଧାରା ଏବଂ ସକାରାତ୍ମକ ବାର୍ତ୍ତା ଦିଅନ୍ତୁ।", "ପ୍ରେରଣା", "lightbulb")
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }
}
