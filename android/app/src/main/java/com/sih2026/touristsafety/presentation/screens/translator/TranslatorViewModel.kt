package com.sih2026.touristsafety.presentation.screens.translator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val isDownloaded: Boolean
)

@HiltViewModel
class TranslatorViewModel @Inject constructor(
    private val translationService: TranslationService
) : ViewModel() {

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

    private var translationJob: Job? = null

    fun setInputText(text: String) {
        _inputText.value = text
        debounceTranslate()
    }

    private fun debounceTranslate() {
        translationJob?.cancel()
        translationJob = viewModelScope.launch {
            delay(500)
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

    fun swapLanguages() {
        val temp = _sourceLanguage.value
        _sourceLanguage.value = _targetLanguage.value
        _targetLanguage.value = temp
        val tempText = _translatedText.value
        _translatedText.value = _inputText.value
        _inputText.value = tempText
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
            translationService.downloadModel(languageCode)
            // Update downloaded status
        }
    }

    fun speakText(text: String, languageCode: String) {
        // Implement TTS
    }
}
