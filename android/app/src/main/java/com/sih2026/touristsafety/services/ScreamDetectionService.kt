package com.sih2026.touristsafety.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class DetectionEvent(
    val timestamp: Long,
    val type: String,
    val result: String,
    val threatScore: Int
)

class ScreamDetectionService : Service() {
    
    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        
        private val _events = MutableSharedFlow<DetectionEvent>(replay = 10)
        val events = _events.asSharedFlow()
        
        private val _threatLevel = MutableSharedFlow<ThreatLevel>(replay = 1)
        val threatLevel = _threatLevel.asSharedFlow()
        
        private val _activeSpeakerGender = MutableSharedFlow<String?>(replay = 1)
        val activeSpeakerGender = _activeSpeakerGender.asSharedFlow()
        
        private val _audioAmplitude = MutableSharedFlow<Float>(replay = 1)
        val audioAmplitude = _audioAmplitude.asSharedFlow()
    }

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private var isRecording = false
    private var audioRecord: AudioRecord? = null
    
    private val sampleRate = 16000
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    override fun onCreate() {
        super.onCreate()
        MLModelManager.initialize(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startMonitoring()
            ACTION_STOP -> stopMonitoring()
        }
        return START_STICKY
    }

    private fun startMonitoring() {
        if (isRecording) return
        
        startForegroundService()
        isRecording = true
        
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )
            
            audioRecord?.startRecording()
            
            scope.launch {
                val chunk = ShortArray(sampleRate / 10) // 100ms chunk
                val rollingBuffer = ShortArray(sampleRate * 3) // 3 seconds context
                var bufferIndex = 0
                var chunksRead = 0

                while (isRecording) {
                    val read = audioRecord?.read(chunk, 0, chunk.size) ?: 0
                    if (read > 0) {
                        // Calculate amplitude for UI
                        var maxAmp = 0
                        for (i in 0 until read) {
                            val absVal = Math.abs(chunk[i].toInt())
                            if (absVal > maxAmp) maxAmp = absVal
                        }
                        _audioAmplitude.emit(maxAmp / 32768f)

                        // Append to rolling buffer
                        if (bufferIndex + read <= rollingBuffer.size) {
                            System.arraycopy(chunk, 0, rollingBuffer, bufferIndex, read)
                            bufferIndex += read
                        } else {
                            System.arraycopy(rollingBuffer, read, rollingBuffer, 0, rollingBuffer.size - read)
                            System.arraycopy(chunk, 0, rollingBuffer, rollingBuffer.size - read, read)
                            bufferIndex = rollingBuffer.size
                        }

                        chunksRead++
                        // Process ML every ~1 second (10 chunks of 100ms)
                        if (chunksRead >= 10 && bufferIndex >= rollingBuffer.size) {
                            processAudio(rollingBuffer)
                            chunksRead = 0
                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            stopMonitoring()
        }
    }
    
    private suspend fun processAudio(audioData: ShortArray) {
        // VAD
        if (!AudioProcessor.isVoiceDetected(audioData, sampleRate)) {
            _activeSpeakerGender.emit(null)
            _threatLevel.emit(ThreatLevel.LOW)
            return
        }
        
        // Extract Features
        val mfccs = AudioProcessor.extractMFCCs(audioData, sampleRate)
        val melSpecs = AudioProcessor.extractMelSpectrogram(audioData, sampleRate)
        
        // Gender Inference using Pitch (more reliable than broken .h5 model)
        val pitch = AudioProcessor.estimatePitch(audioData, sampleRate)
        val maleProb = MLModelManager.runGenderInference(null, melSpecs)
        
        // If pitch is extremely high or low (not human speech), rely on ML fallback. Otherwise pitch is king.
        val gender = if (pitch > 60f && pitch < 165f) {
            "male"
        } else if (pitch >= 165f && pitch < 300f) {
            "female"
        } else {
            if (maleProb > 0.5f) "male" else "female"
        }
        _activeSpeakerGender.emit(gender)
        
        // Distress Inference (Requires exactly 13248 float samples)
        val distressInput = FloatArray(13248) { i ->
            if (i < audioData.size) audioData[i] / 32768f else 0f
        }
        val distressProb = MLModelManager.runDistressInference(null, distressInput)
        
        // Threat Assessment strictly based on input voice volume as requested
        var maxAmp = 0f
        for (sample in audioData) {
            val abs = Math.abs(sample.toInt())
            if (abs > maxAmp) maxAmp = abs.toFloat()
        }
        val volumeLevel = maxAmp / 32768f
        
        val newThreatLevel = when {
            volumeLevel > 0.7f -> ThreatLevel.CRITICAL
            volumeLevel > 0.4f -> ThreatLevel.HIGH
            volumeLevel > 0.15f -> ThreatLevel.MEDIUM
            else -> ThreatLevel.LOW
        }
        
        val isScream = volumeLevel > 0.7f
        
        // Emit events
        if (isScream) {
            _events.emit(DetectionEvent(
                System.currentTimeMillis(),
                "Scream Detected",
                "Gender: $gender, Volume: ${(volumeLevel * 100).toInt()}%",
                100
            ))
        }
        _threatLevel.emit(newThreatLevel)
    }

    private fun stopMonitoring() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startForegroundService() {
        val channelId = "scream_detection_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Safety Monitoring",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
        
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Safety Monitor Active")
            .setContentText("Monitoring for screams and distress signals...")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .build()
            
        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        audioRecord?.release()
    }
}
