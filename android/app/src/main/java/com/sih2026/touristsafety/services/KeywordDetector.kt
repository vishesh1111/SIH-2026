package com.sih2026.touristsafety.services

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.LinkedList

class KeywordDetector(
    private val keywordThreshold: Int = 5,
    private val windowDurationMs: Long = 30_000L
) {
    private var model: Model? = null
    private var recognizer: Recognizer? = null
    
    private val keywordTimestamps = LinkedList<Long>()
    private val committedTranscripts = LinkedList<Pair<Long, String>>()
    private var currentPartialText: String = ""
    private var lastMatchedPartialWordCount = 0
    
    private val keywords = listOf(
        "help", "bachao", "madad", "bacha lo", "save me", "emergency", "danger", "police", "stop"
    )

    var onKeywordDetected: ((word: String, currentCount: Int) -> Unit)? = null
    var onThresholdBreached: ((fullTranscript: String) -> Unit)? = null
    var onTranscriptUpdated: ((liveTranscript: String) -> Unit)? = null

    suspend fun initialize(context: Context) = withContext(Dispatchers.IO) {
        try {
            val modelPath = copyModelFromAssets(context)
            model = Model(modelPath)
            recognizer = Recognizer(model, 16000f)
            Log.d(TAG, "Vosk model initialized successfully at 16kHz")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Vosk model: ${e.message}", e)
        }
    }

    fun feedAudio(audioData: ShortArray) {
        val rec = recognizer ?: return

        val byteArray = shortArrayToByteArray(audioData)
        val isFinal = rec.acceptWaveForm(byteArray, byteArray.size)

        val jsonResult = if (isFinal) {
            rec.result
        } else {
            rec.partialResult
        }

        if (!jsonResult.isNullOrBlank()) {
            processResult(jsonResult, isFinal)
        }
    }

    private fun processResult(jsonResult: String, isFinal: Boolean) {
        try {
            val jsonObject = JSONObject(jsonResult)
            val currentTime = System.currentTimeMillis()

            if (isFinal) {
                val finalText = jsonObject.optString("text", "").trim().lowercase()
                if (finalText.isNotBlank()) {
                    committedTranscripts.add(Pair(currentTime, finalText))
                    cleanUpOldData(currentTime)
                }
                currentPartialText = ""
                lastMatchedPartialWordCount = 0
                
                val fullLive = getRecentTranscriptTail(8)
                if (fullLive.isNotBlank()) {
                    onTranscriptUpdated?.invoke(fullLive)
                }
            } else {
                val partialText = jsonObject.optString("partial", "").trim().lowercase()
                if (partialText.isNotBlank() && partialText != currentPartialText) {
                    currentPartialText = partialText
                    
                    // Instant keyword detection on partial stream
                    checkForKeywordsInPartial(partialText, currentTime)
                    
                    val fullLive = getRecentTranscriptTail(8)
                    if (fullLive.isNotBlank()) {
                        onTranscriptUpdated?.invoke(fullLive)
                    }
                }
            }
        } catch (e: JSONException) {
            Log.e(TAG, "Error parsing JSON result: ${e.message}")
        }
    }

    private fun checkForKeywordsInPartial(partialText: String, currentTime: Long) {
        val words = partialText.split("\\s+".toRegex()).filter { it.isNotBlank() }
        
        var keywordOccurrencesInPartial = 0
        var lastDetectedKeyword = "help"

        for (word in words) {
            for (keyword in keywords) {
                if (word == keyword || word.contains(keyword) || partialText.contains(keyword)) {
                    keywordOccurrencesInPartial++
                    lastDetectedKeyword = keyword
                    break
                }
            }
        }

        if (keywordOccurrencesInPartial > lastMatchedPartialWordCount) {
            val newlySpokenCount = keywordOccurrencesInPartial - lastMatchedPartialWordCount
            lastMatchedPartialWordCount = keywordOccurrencesInPartial

            for (i in 0 until newlySpokenCount) {
                keywordTimestamps.add(currentTime)
            }
            cleanUpOldData(currentTime)

            val totalCount = keywordTimestamps.size
            onKeywordDetected?.invoke(lastDetectedKeyword, totalCount)

            if (totalCount >= keywordThreshold) {
                onThresholdBreached?.invoke(getRecentTranscript())
                keywordTimestamps.clear()
                lastMatchedPartialWordCount = 0
            }
        }
    }

    private fun cleanUpOldData(currentTime: Long) {
        val cutoffTime = currentTime - windowDurationMs
        
        while (keywordTimestamps.isNotEmpty() && keywordTimestamps.peek()!! < cutoffTime) {
            keywordTimestamps.poll()
        }
        
        while (committedTranscripts.isNotEmpty() && committedTranscripts.peek()!!.first < cutoffTime) {
            committedTranscripts.poll()
        }
    }

    fun getRecentTranscript(): String {
        val committed = committedTranscripts.joinToString(" ") { it.second }.trim()
        val combined = if (committed.isNotBlank() && currentPartialText.isNotBlank()) {
            "$committed $currentPartialText"
        } else if (committed.isNotBlank()) {
            committed
        } else {
            currentPartialText
        }
        return combined.trim()
    }

    fun getRecentTranscriptTail(maxWords: Int = 8): String {
        val full = getRecentTranscript()
        if (full.isBlank()) return ""
        val words = full.split("\\s+".toRegex()).filter { it.isNotBlank() }
        return if (words.size > maxWords) {
            "... " + words.takeLast(maxWords).joinToString(" ")
        } else {
            full
        }
    }

    private suspend fun copyModelFromAssets(context: Context): String = withContext(Dispatchers.IO) {
        val modelDirName = "vosk-model-small-en-in-0.4"
        val prefs = context.getSharedPreferences("VoskPrefs", Context.MODE_PRIVATE)
        val isCopied = prefs.getBoolean("is_model_copied", false)
        
        val destDir = File(context.filesDir, modelDirName)
        
        if (!isCopied || !destDir.exists()) {
            destDir.mkdirs()
            copyAssetFolder(context, "models/$modelDirName", destDir.absolutePath)
            prefs.edit().putBoolean("is_model_copied", true).apply()
        }
        
        destDir.absolutePath
    }

    @Throws(IOException::class)
    private fun copyAssetFolder(context: Context, srcName: String, destName: String) {
        val assetManager = context.assets
        val files = assetManager.list(srcName)
        
        if (files.isNullOrEmpty()) {
            val destFile = File(destName)
            destFile.parentFile?.mkdirs()
            
            assetManager.open(srcName).use { inStream ->
                FileOutputStream(destFile).use { outStream ->
                    inStream.copyTo(outStream)
                }
            }
        } else {
            File(destName).mkdirs()
            for (filename in files) {
                copyAssetFolder(context, "$srcName/$filename", "$destName/$filename")
            }
        }
    }

    private fun shortArrayToByteArray(shortArray: ShortArray): ByteArray {
        val byteBuffer = ByteBuffer.allocate(shortArray.size * 2)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        for (s in shortArray) {
            byteBuffer.putShort(s)
        }
        return byteBuffer.array()
    }

    fun release() {
        recognizer?.close()
        recognizer = null
        model?.close()
        model = null
        keywordTimestamps.clear()
        committedTranscripts.clear()
        currentPartialText = ""
        lastMatchedPartialWordCount = 0
    }

    companion object {
        private const val TAG = "KeywordDetector"
    }
}
