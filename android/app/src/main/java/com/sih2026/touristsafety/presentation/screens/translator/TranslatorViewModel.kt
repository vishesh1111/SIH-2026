package com.sih2026.touristsafety.presentation.screens.translator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sih2026.touristsafety.services.OfflineSpeechRecognizer
import com.sih2026.touristsafety.services.TranslationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LanguageOption(
    val code: String,
    val name: String,
    val nativeName: String,
    val isDownloaded: Boolean = false
)

@HiltViewModel
class TranslatorViewModel @Inject constructor(
    application: Application,
    private val translationService: TranslationService
) : AndroidViewModel(application) {

    private val offlineSpeechRecognizer = OfflineSpeechRecognizer(application)

    private val indianLanguages = listOf(
        LanguageOption("hi", "Hindi", "हिन्दी", true),
        LanguageOption("bn", "Bengali", "বাংলা", false),
        LanguageOption("te", "Telugu", "తెలుగు", false),
        LanguageOption("mr", "Marathi", "मराठी", false),
        LanguageOption("ta", "Tamil", "தமிழ்", false),
        LanguageOption("gu", "Gujarati", "ગુજરાતી", false),
        LanguageOption("ur", "Urdu", "اردو", false),
        LanguageOption("kn", "Kannada", "ಕನ್ನಡ", false),
        LanguageOption("ml", "Malayalam", "മലയാളം", false),
        LanguageOption("pa", "Punjabi", "ਪੰਜਾਬੀ", false)
    )

    private val otherLanguages = listOf(
        LanguageOption("en", "English", "English", true),
        LanguageOption("fr", "French", "Français", false),
        LanguageOption("de", "German", "Deutsch", false),
        LanguageOption("es", "Spanish", "Español", false),
        LanguageOption("ja", "Japanese", "日本語", false),
        LanguageOption("ko", "Korean", "한국어", false),
        LanguageOption("zh", "Mandarin", "中文", false),
        LanguageOption("ar", "Arabic", "العربية", false),
        LanguageOption("ru", "Russian", "Русский", false)
    )

    private val _availableLanguages = MutableStateFlow(indianLanguages + otherLanguages)
    val availableLanguages: StateFlow<List<LanguageOption>> = _availableLanguages.asStateFlow()

    private val _sourceLanguage = MutableStateFlow(_availableLanguages.value.find { it.code == "en" }!!)
    val sourceLanguage: StateFlow<LanguageOption> = _sourceLanguage.asStateFlow()

    private val _targetLanguage = MutableStateFlow(_availableLanguages.value.find { it.code == "hi" }!!)
    val targetLanguage: StateFlow<LanguageOption> = _targetLanguage.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _translatedText = MutableStateFlow("")
    val translatedText: StateFlow<String> = _translatedText.asStateFlow()

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isDownloadingModel = MutableStateFlow(false)
    val isDownloadingModel: StateFlow<Boolean> = _isDownloadingModel.asStateFlow()

    private var translationJob: Job? = null

    init {
        checkDownloadedModels()
        viewModelScope.launch {
            offlineSpeechRecognizer.initialize()
        }
    }

    private fun checkDownloadedModels() {
        viewModelScope.launch {
            val updated = _availableLanguages.value.map { lang ->
                val downloaded = translationService.isModelDownloaded(lang.code) || lang.code == "en"
                lang.copy(isDownloaded = downloaded)
            }
            _availableLanguages.value = updated
        }
    }

    fun setInputText(text: String) {
        _inputText.value = text
        debounceTranslate()
    }

    private fun debounceTranslate() {
        translationJob?.cancel()
        translationJob = viewModelScope.launch {
            delay(350)
            translate()
        }
    }

    fun translate() {
        if (_inputText.value.isBlank()) {
            _translatedText.value = ""
            return
        }
        _isTranslating.value = true
        viewModelScope.launch {
            val result = translationService.translateText(
                _inputText.value,
                _sourceLanguage.value.code,
                _targetLanguage.value.code
            )
            _translatedText.value = result
            _isTranslating.value = false
        }
    }

    fun toggleOfflineVoiceInput() {
        if (_isListening.value) {
            stopListening()
        } else {
            startListening()
        }
    }

    private fun startListening() {
        _isListening.value = true
        offlineSpeechRecognizer.startListening(
            onPartialResult = { liveSpokenText ->
                _inputText.value = liveSpokenText
                debounceTranslate()
            },
            onFinalResult = { finalSpokenText ->
                _inputText.value = finalSpokenText
                _isListening.value = false
                translate()
            },
            onError = {
                _isListening.value = false
            }
        )
    }

    private fun stopListening() {
        offlineSpeechRecognizer.stopListening()
        _isListening.value = false
        translate()
    }

    fun swapLanguages() {
        val temp = _sourceLanguage.value
        _sourceLanguage.value = _targetLanguage.value
        _targetLanguage.value = temp
        val tempText = _translatedText.value
        _translatedText.value = _inputText.value
        _inputText.value = tempText
        translate()
    }
    
    fun setSourceLanguage(lang: LanguageOption) {
        _sourceLanguage.value = lang
        translate()
    }

    fun setTargetLanguage(lang: LanguageOption) {
        _targetLanguage.value = lang
        translate()
    }

    fun downloadModel(languageCode: String) {
        viewModelScope.launch {
            _isDownloadingModel.value = true
            val success = translationService.downloadModel(languageCode)
            if (success) {
                checkDownloadedModels()
            }
            _isDownloadingModel.value = false
        }
    }

    fun speakText(text: String, languageCode: String) {
        // TTS implementation
    }

    override fun onCleared() {
        super.onCleared()
        offlineSpeechRecognizer.release()
    }
}
