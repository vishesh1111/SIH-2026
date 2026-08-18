package com.sih2026.touristsafety.services

import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranslationService @Inject constructor() {
    
    // In a real app we'd keep track of translators to avoid recreating them
    
    suspend fun translateText(text: String, sourceLangCode: String, targetLangCode: String): String {
        return try {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLangCode)
                .setTargetLanguage(targetLangCode)
                .build()
            val translator = Translation.getClient(options)
            
            // Ensure model is downloaded (in prod, handle this separately)
            translator.downloadModelIfNeeded().await()
            
            val result = translator.translate(text).await()
            translator.close()
            result
        } catch (e: Exception) {
            "Translation failed: ${e.message}"
        }
    }
    
    suspend fun downloadModel(languageCode: String) {
        // ML Kit translation model downloading logic
    }
    
    fun isModelDownloaded(languageCode: String): Boolean {
        // Check ML kit downloaded models
        return true // mocked
    }
}
