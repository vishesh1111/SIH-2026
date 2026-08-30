package com.sih2026.touristsafety.services

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.*
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class OfflineSpeechRecognizer(private val context: Context) {

    private var model: Model? = null
    private var recognizer: Recognizer? = null
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val sampleRate = 16000
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(sampleRate * 2)

    suspend fun initialize() = withContext(Dispatchers.IO) {
        try {
            val modelPath = copyModelFromAssets(context)
            model = Model(modelPath)
            recognizer = Recognizer(model, sampleRate.toFloat())
            Log.d("OfflineSpeechRec", "Vosk Offline Model Initialized")
        } catch (e: Exception) {
            Log.e("OfflineSpeechRec", "Failed to init offline recognizer: ${e.message}", e)
        }
    }

    @SuppressLint("MissingPermission")
    fun startListening(
        onPartialResult: (String) -> Unit,
        onFinalResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (isRecording) return
        if (recognizer == null) {
            scope.launch {
                initialize()
                withContext(Dispatchers.Main) {
                    startListeningInternal(onPartialResult, onFinalResult, onError)
                }
            }
            return
        }

        startListeningInternal(onPartialResult, onFinalResult, onError)
    }

    @SuppressLint("MissingPermission")
    private fun startListeningInternal(
        onPartialResult: (String) -> Unit,
        onFinalResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                onError("AudioRecord failed to initialize")
                return
            }

            audioRecord?.startRecording()
            isRecording = true

            recordingJob = scope.launch {
                val buffer = ShortArray(sampleRate / 10) // 100ms
                var fullSpokenText = ""

                while (isRecording && isActive) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) {
                        val byteArray = shortArrayToByteArray(buffer.copyOf(read))
                        val isFinal = recognizer?.acceptWaveForm(byteArray, byteArray.size) ?: false

                        if (isFinal) {
                            val jsonResult = recognizer?.result ?: ""
                            val text = extractText(jsonResult, "text")
                            if (text.isNotBlank()) {
                                fullSpokenText = if (fullSpokenText.isBlank()) text else "$fullSpokenText $text"
                                withContext(Dispatchers.Main) {
                                    onPartialResult(fullSpokenText)
                                }
                            }
                        } else {
                            val jsonResult = recognizer?.partialResult ?: ""
                            val partial = extractText(jsonResult, "partial")
                            if (partial.isNotBlank()) {
                                val combined = if (fullSpokenText.isBlank()) partial else "$fullSpokenText $partial"
                                withContext(Dispatchers.Main) {
                                    onPartialResult(combined)
                                }
                            }
                        }
                    }
                }

                // Final flush
                val finalJson = recognizer?.finalResult ?: ""
                val remainingText = extractText(finalJson, "text")
                if (remainingText.isNotBlank()) {
                    fullSpokenText = if (fullSpokenText.isBlank()) remainingText else "$fullSpokenText $remainingText"
                }

                withContext(Dispatchers.Main) {
                    onFinalResult(fullSpokenText.trim())
                }
            }
        } catch (e: Exception) {
            isRecording = false
            onError(e.message ?: "Recording error")
        }
    }

    fun stopListening() {
        isRecording = false
        recordingJob?.cancel()
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
    }

    private fun extractText(jsonStr: String, key: String): String {
        return try {
            if (jsonStr.isBlank()) "" else JSONObject(jsonStr).optString(key, "")
        } catch (_: Exception) {
            ""
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

    fun release() {
        stopListening()
        recognizer?.close()
        recognizer = null
        model?.close()
        model = null
        scope.cancel()
    }
}
