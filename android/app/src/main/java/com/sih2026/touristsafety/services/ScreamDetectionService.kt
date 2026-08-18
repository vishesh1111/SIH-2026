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
                val buffer = ShortArray(sampleRate * 5) // 5 seconds
                while (isRecording) {
                    var readSize = 0
                    while (readSize < buffer.size && isRecording) {
                        val read = audioRecord?.read(buffer, readSize, buffer.size - readSize) ?: 0
                        if (read > 0) readSize += read
                    }
                    
                    if (readSize == buffer.size) {
                        processAudio(buffer)
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
            return
        }
        
        // Extract Features
        val mfccs = AudioProcessor.extractMFCCs(audioData, sampleRate)
        
        // Phase 1: Noise vs Human
        val phase1Result = MLModelManager.runScreamPhase1Inference(null, mfccs)
        if (phase1Result != 2) return // Noise
        
        // Phase 2: Scream vs Speech
        val phase2Result = MLModelManager.runScreamPhase2Inference(null, mfccs)
        val isScream = phase2Result == 1
        
        // Gender & Distress
        val melSpecs = AudioProcessor.extractMelSpectrogram(audioData, sampleRate)
        val maleProb = MLModelManager.runGenderInference(null, melSpecs)
        val gender = if (maleProb > 0.5f) "male" else "female"
        
        val distressProb = MLModelManager.runDistressInference(null, FloatArray(0)) // mock input
        
        // Threat Assessment
        val assessment = ThreatAssessor.assessThreat(
            screamDetected = isScream,
            gender = gender,
            distressLevel = distressProb,
            isNight = false, // mock
            isDangerZone = false // mock
        )
        
        // Emit events
        if (isScream) {
            _events.emit(DetectionEvent(
                System.currentTimeMillis(),
                "Scream Detected",
                "Gender: $gender, Distress: ${(distressProb * 100).toInt()}%",
                assessment.score
            ))
        }
        _threatLevel.emit(assessment.level)
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
            .setContentTitle("Women Safety Active")
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
