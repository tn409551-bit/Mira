package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class JarvisVoiceState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING
}

class VoiceManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    private val _voiceState = MutableStateFlow(JarvisVoiceState.IDLE)
    val voiceState: StateFlow<JarvisVoiceState> = _voiceState.asStateFlow()

    private val _rmsVolume = MutableStateFlow(0f)
    val rmsVolume: StateFlow<Float> = _rmsVolume.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    var speechRate: Float = 1.0f
        set(value) {
            field = value
            textToSpeech?.setSpeechRate(value)
        }

    var speechPitch: Float = 1.0f
        set(value) {
            field = value
            textToSpeech?.setPitch(value)
        }

    init {
        try {
            textToSpeech = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            textToSpeech?.let { tts ->
                tts.setSpeechRate(speechRate)
                tts.setPitch(speechPitch)
                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _voiceState.value = JarvisVoiceState.SPEAKING
                    }

                    override fun onDone(utteranceId: String?) {
                        if (_voiceState.value == JarvisVoiceState.SPEAKING) {
                            _voiceState.value = JarvisVoiceState.IDLE
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        if (_voiceState.value == JarvisVoiceState.SPEAKING) {
                            _voiceState.value = JarvisVoiceState.IDLE
                        }
                    }
                })
            }
        }
    }

    fun isSpeechRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening(
        languageCode: String = "or",
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        stopSpeaking()

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("ଆପଣଙ୍କ ଡିଭାଇସରେ ସ୍ପିଚ୍ ରିକଗ୍ନିସନ୍ ସେବା ଉପଲବ୍ଧ ନାହିଁ (Speech recognition unavailable).")
            return
        }

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _voiceState.value = JarvisVoiceState.LISTENING
                        _partialText.value = ""
                    }

                    override fun onBeginningOfSpeech() {
                        _voiceState.value = JarvisVoiceState.LISTENING
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize roughly 0 to 1
                        val normalized = (rmsdB + 2f).coerceIn(0f, 10f) / 10f
                        _rmsVolume.value = normalized
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _voiceState.value = JarvisVoiceState.PROCESSING
                        _rmsVolume.value = 0f
                    }

                    override fun onError(error: Int) {
                        _voiceState.value = JarvisVoiceState.IDLE
                        _rmsVolume.value = 0f
                        val msg = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "କିଛି ଶୁଣାଗଲା ନାହିଁ (No voice recognized). ଦୟାକରି ପୁଣି କୁହନ୍ତୁ।"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "ସମୟ ସମାପ୍ତ ହେଲା (Voice timeout)."
                            SpeechRecognizer.ERROR_AUDIO -> "ଅଡିଓ ରେକର୍ଡିଂ ତ୍ରୁଟି (Audio error)."
                            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "ନେଟୱର୍କ ତ୍ରୁଟି (Network error)."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "ମାଇକ୍ରୋଫୋନ୍ ଅନୁମତି ଆବଶ୍ୟକ (Microphone permission needed)."
                            else -> "ଭଏସ୍ ଇନପୁଟ୍ ତ୍ରୁଟି (Code $error)"
                        }
                        onError(msg)
                    }

                    override fun onResults(results: Bundle?) {
                        _voiceState.value = JarvisVoiceState.IDLE
                        _rmsVolume.value = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        if (text.isNotBlank()) {
                            onResult(text)
                        } else {
                            onError("କୌଣସି ଶବ୍ଦ ଚିହ୍ନଟ ହୋଇପାରିଲା ନାହିଁ।")
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()
                        if (!text.isNullOrBlank()) {
                            _partialText.value = text
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)

                val tag = when (languageCode) {
                    "hi" -> "hi-IN"
                    "en" -> "en-US"
                    else -> "or-IN"
                }
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, tag)
                putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("or-IN", "hi-IN", "en-US", "en-IN"))
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _voiceState.value = JarvisVoiceState.IDLE
            onError("ମାଇକ୍ରୋଫୋନ୍ ଆରମ୍ଭ କରିବାରେ ଅସୁବିଧା: ${e.localizedMessage}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _voiceState.value = JarvisVoiceState.IDLE
        _rmsVolume.value = 0f
    }

    fun speak(text: String, languageCode: String = "or") {
        if (!isTtsInitialized || textToSpeech == null) return

        stopSpeaking()

        // Clean markdown symbols for natural TTS speech
        val cleanedText = text
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
            .replace(Regex("\\*(.*?)\\*"), "$1")
            .replace(Regex("`{1,3}(.*?)`{1,3}"), "$1")
            .replace(Regex("#+\\s*"), "")
            .replace(Regex("!\\[.*?\\]\\(.*?\\)"), "")
            .replace(Regex("\\[(.*?)\\]\\(.*?\\)"), "$1")
            .trim()

        val locale = when (languageCode) {
            "hi" -> Locale("hi", "IN")
            "en" -> Locale.US
            else -> {
                // Try Odia locale
                val odia = Locale("or", "IN")
                val res = textToSpeech?.isLanguageAvailable(odia)
                if (res != null && res >= TextToSpeech.LANG_AVAILABLE) {
                    odia
                } else {
                    // Fallback to Hindi or Indian English if Odia is not installed in system TTS engine
                    Locale("hi", "IN")
                }
            }
        }

        try {
            textToSpeech?.language = locale
            textToSpeech?.setSpeechRate(speechRate)
            textToSpeech?.setPitch(speechPitch)

            val utteranceId = "JARVIS_TTS_${System.currentTimeMillis()}"
            textToSpeech?.speak(cleanedText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            _voiceState.value = JarvisVoiceState.SPEAKING
        } catch (e: Exception) {
            e.printStackTrace()
            _voiceState.value = JarvisVoiceState.IDLE
        }
    }

    fun stopSpeaking() {
        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (_voiceState.value == JarvisVoiceState.SPEAKING) {
            _voiceState.value = JarvisVoiceState.IDLE
        }
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
