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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

data class DetectionEvent(
    val timestamp: Long,
    val type: String,
    val result: String,
    val threatScore: Int
)

data class SOSTriggerEvent(
    val reason: String,
    val transcript: String?,
    val threatScore: Int
)

class ScreamDetectionService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_CANCEL_COUNTDOWN = "ACTION_CANCEL_COUNTDOWN"

        private val _events = MutableSharedFlow<DetectionEvent>(replay = 1)
        val events = _events.asSharedFlow()

        private val _threatLevel = MutableSharedFlow<ThreatLevel>(replay = 1)
        val threatLevel = _threatLevel.asSharedFlow()

        private val _activeSpeakerGender = MutableSharedFlow<String?>(replay = 1)
        val activeSpeakerGender = _activeSpeakerGender.asSharedFlow()

        private val _audioAmplitude = MutableSharedFlow<Float>(replay = 1)
        val audioAmplitude = _audioAmplitude.asSharedFlow()

        // NEW flows for keyword detection
        private val _keywordCount = MutableSharedFlow<Int>(replay = 1)
        val keywordCount = _keywordCount.asSharedFlow()

        private val _latestTranscript = MutableSharedFlow<String?>(replay = 1)
        val latestTranscript = _latestTranscript.asSharedFlow()

        private val _sosCountdown = MutableSharedFlow<Int>(replay = 1)
        val sosCountdown = _sosCountdown.asSharedFlow()

        private val _sosTrigger = MutableSharedFlow<SOSTriggerEvent>(replay = 1)
        val sosTrigger = _sosTrigger.asSharedFlow()

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        private var journeyStartTime = 0L
        fun getJourneyDurationSeconds(): Int = if (journeyStartTime > 0L) ((System.currentTimeMillis() - journeyStartTime) / 1000).toInt() else 0
    }

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private var isRecording = false
    private var audioRecord: AudioRecord? = null

    // Keyword detection
    private var keywordDetector: KeywordDetector? = null

    // Countdown state
    private var countdownJob: Job? = null
    private var isCountdownActive = false

    private val sampleRate = 16000
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(sampleRate * 2)

    override fun onCreate() {
        super.onCreate()
        MLModelManager.initialize(this)
        initializeKeywordDetector()
    }

    private fun initializeKeywordDetector() {
        keywordDetector = KeywordDetector().apply {
            onKeywordDetected = { word, count ->
                scope.launch {
                    _keywordCount.emit(count)
                    _events.emit(
                        DetectionEvent(
                            System.currentTimeMillis(),
                            "Keyword Detected",
                            "\"$word\" — $count/5 in last 30s",
                            count * 20
                        )
                    )
                }
            }
            onThresholdBreached = { fullTranscript ->
                scope.launch {
                    _latestTranscript.emit(fullTranscript)
                    startSOSCountdown("Keyword Alert: 5+ distress words in 30s", fullTranscript)
                }
            }
            onTranscriptUpdated = { liveTranscript ->
                scope.launch {
                    _latestTranscript.emit(liveTranscript)
                }
            }
        }
        scope.launch {
            keywordDetector?.initialize(this@ScreamDetectionService)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startMonitoring()
            ACTION_STOP -> stopMonitoring()
            ACTION_CANCEL_COUNTDOWN -> cancelCountdown()
        }
        return START_STICKY
    }

    private fun startMonitoring() {
        if (isRecording) return

        startForegroundService()
        isRecording = true
        _isRunning.value = true
        if (journeyStartTime == 0L) journeyStartTime = System.currentTimeMillis()

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
                        val normAmp = (maxAmp / 32768f).coerceIn(0f, 1f)
                        _audioAmplitude.emit(normAmp)

                        // Feed audio to Vosk keyword detector
                        val audioChunk = chunk.copyOf(read)
                        keywordDetector?.feedAudio(audioChunk)

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
        var maxAmp = 0
        for (sample in audioData) {
            val absVal = Math.abs(sample.toInt())
            if (absVal > maxAmp) maxAmp = absVal
        }
        val amplitude = maxAmp / 32768f
        val isLoudAudio = amplitude > 0.40f

        // Step 1: Voice Activity Detection (or loud audio)
        val hasVoice = AudioProcessor.isVoiceDetected(audioData, sampleRate) || isLoudAudio
        if (!hasVoice) {
            _activeSpeakerGender.emit(null)
            _threatLevel.emit(ThreatLevel.LOW)
            return
        }

        // Step 2: SVM Phase 1 — Noise vs Human
        val mfccs = AudioProcessor.extractMFCCs(audioData, sampleRate)
        val phase1Result = MLModelManager.runScreamPhase1Inference(null, mfccs)
        if (phase1Result == 1 && !isLoudAudio) {
            // Classified as noise, not human — skip only if not loud
            _threatLevel.emit(ThreatLevel.LOW)
            return
        }

        // Step 3: SVM Phase 2 — Scream vs Speech
        val phase2Result = MLModelManager.runScreamPhase2Inference(null, mfccs)
        val isScream = phase2Result == 1 || isLoudAudio

        // Step 4: Gender detection via pitch + ML fallback
        val melSpecs = AudioProcessor.extractMelSpectrogram(audioData, sampleRate)
        val pitch = AudioProcessor.estimatePitch(audioData, sampleRate)
        val maleProb = MLModelManager.runGenderInference(null, melSpecs)

        val gender = if (pitch > 60f && pitch < 165f) {
            "male"
        } else if (pitch >= 165f && pitch < 300f) {
            "female"
        } else {
            if (maleProb > 0.5f) "male" else "female"
        }
        _activeSpeakerGender.emit(gender)

        // Step 5: Distress inference via TFLite
        val distressInput = FloatArray(13248) { i ->
            if (i < audioData.size) audioData[i] / 32768f else 0f
        }
        val distressProb = if (isLoudAudio) {
            (MLModelManager.runDistressInference(null, distressInput) + 0.4f).coerceIn(0.5f, 0.95f)
        } else {
            MLModelManager.runDistressInference(null, distressInput)
        }

        // Step 6: Context signals
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isNight = currentHour < 6 || currentHour >= 21

        // Step 7: Use ThreatAssessor for scoring
        val assessment = ThreatAssessor.assessThreat(
            screamDetected = isScream,
            gender = gender,
            distressLevel = distressProb,
            isNight = isNight,
            isDangerZone = false,
            keywordAlertTriggered = false
        )

        _threatLevel.emit(assessment.level)

        // Emit detection events
        if (isScream) {
            _events.emit(
                DetectionEvent(
                    System.currentTimeMillis(),
                    "Scream / Loud Noise Detected",
                    "Gender: $gender, Level: ${(amplitude * 100).toInt()}%, Score: ${assessment.score}",
                    assessment.score
                )
            )
        } else if (distressProb > 0.5f) {
            _events.emit(
                DetectionEvent(
                    System.currentTimeMillis(),
                    "Distress Audio Detected",
                    "Gender: $gender, Distress: ${(distressProb * 100).toInt()}%, Score: ${assessment.score}",
                    assessment.score
                )
            )
        }

        // Step 8: Auto-trigger SOS with 10s countdown if CRITICAL
        if (assessment.shouldTriggerSOS && !isCountdownActive) {
            val transcript = keywordDetector?.getRecentTranscript()
            startSOSCountdown(assessment.triggerReason, transcript)
        }

        // Update transcript for UI
        val transcript = keywordDetector?.getRecentTranscript()
        if (!transcript.isNullOrBlank()) {
            _latestTranscript.emit(transcript)
        }
    }

    /**
     * Starts a 10-second countdown before triggering SOS.
     * During these 10 seconds, the user can cancel via the notification or in-app button.
     */
    private fun startSOSCountdown(reason: String, transcript: String?) {
        if (isCountdownActive) return
        isCountdownActive = true

        countdownJob = scope.launch {
            for (remaining in 10 downTo 1) {
                if (!isCountdownActive) return@launch
                _sosCountdown.emit(remaining)
                delay(1000)
            }

            // Countdown finished without cancellation -> Trigger SOS
            _sosCountdown.emit(0)
            _sosTrigger.emit(
                SOSTriggerEvent(
                    reason = reason,
                    transcript = transcript,
                    threatScore = 80
                )
            )
            _events.emit(
                DetectionEvent(
                    System.currentTimeMillis(),
                    "🚨 SOS ACTIVATED",
                    "Reason: $reason",
                    100
                )
            )
            isCountdownActive = false
        }
    }

    private fun cancelCountdown() {
        countdownJob?.cancel()
        countdownJob = null
        isCountdownActive = false
        scope.launch {
            _sosCountdown.emit(-1) // -1 = cancelled
            _events.emit(
                DetectionEvent(
                    System.currentTimeMillis(),
                    "SOS Cancelled",
                    "Countdown was cancelled by user",
                    0
                )
            )
        }
    }

    private fun stopMonitoring() {
        isRecording = false
        _isRunning.value = false
        journeyStartTime = 0L
        cancelCountdown()
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        keywordDetector?.release()
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
            .setContentText("Monitoring for screams, distress & keywords...")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .build()

        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        _isRunning.value = false
        journeyStartTime = 0L
        job.cancel()
        audioRecord?.release()
        keywordDetector?.release()
    }
}
