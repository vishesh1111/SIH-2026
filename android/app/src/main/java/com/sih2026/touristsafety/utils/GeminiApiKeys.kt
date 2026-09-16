package com.sih2026.touristsafety.utils

/**
 * Gemini API key provider.
 * Keys are loaded from BuildConfig (injected from local.properties at build time).
 * NEVER hardcode API keys in source code — they get leaked to version control.
 */
object GeminiApiKeys {
    private val keys: List<String> by lazy {
        listOf(
            com.sih2026.touristsafety.BuildConfig.GEMINI_API_KEY
            // Add more keys via additional BuildConfig fields if needed
        ).filter { it.isNotBlank() }
    }
    private var currentIndex = 0

    fun getNextKey(): String {
        if (keys.isEmpty()) {
            throw IllegalStateException(
                "No Gemini API key found. Set GEMINI_API_KEY in android/local.properties"
            )
        }
        val key = keys[currentIndex]
        currentIndex = (currentIndex + 1) % keys.size
        return key
    }
}
