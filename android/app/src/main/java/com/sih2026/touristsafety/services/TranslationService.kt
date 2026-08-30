package com.sih2026.touristsafety.services

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranslationService @Inject constructor() {

    private val translatorCache = ConcurrentHashMap<String, Translator>()
    private val modelManager = RemoteModelManager.getInstance()

    suspend fun translateText(text: String, sourceLangCode: String, targetLangCode: String): String {
        if (text.isBlank()) return ""

        val sourceLang = TranslateLanguage.fromLanguageTag(sourceLangCode) ?: sourceLangCode
        val targetLang = TranslateLanguage.fromLanguageTag(targetLangCode) ?: targetLangCode

        val cacheKey = "${sourceLang}_$targetLang"
        
        try {
            val translator = translatorCache.getOrPut(cacheKey) {
                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(sourceLang)
                    .setTargetLanguage(targetLang)
                    .build()
                Translation.getClient(options)
            }

            val conditions = DownloadConditions.Builder().build()
            
            // Try downloading if network available, or use cached on-device model
            try {
                translator.downloadModelIfNeeded(conditions).await()
            } catch (netEx: Exception) {
                // If offline and model not downloaded, fall through to offline dictionary
            }

            return translator.translate(text).await()
        } catch (e: Exception) {
            // Fallback to embedded offline dictionary when offline or if ML kit model not downloaded
            val offlineResult = OfflineTouristDictionary.translateOffline(text, sourceLangCode, targetLangCode)
            if (!offlineResult.isNullOrBlank()) {
                return offlineResult
            }
            return "Offline translation: $text"
        }
    }

    suspend fun downloadModel(languageCode: String): Boolean {
        return try {
            val lang = TranslateLanguage.fromLanguageTag(languageCode) ?: return false
            val model = TranslateRemoteModel.Builder(lang).build()
            val conditions = DownloadConditions.Builder().build()
            modelManager.download(model, conditions).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun isModelDownloaded(languageCode: String): Boolean {
        return try {
            val lang = TranslateLanguage.fromLanguageTag(languageCode) ?: return false
            val model = TranslateRemoteModel.Builder(lang).build()
            modelManager.isModelDownloaded(model).await()
        } catch (e: Exception) {
            false
        }
    }
}
