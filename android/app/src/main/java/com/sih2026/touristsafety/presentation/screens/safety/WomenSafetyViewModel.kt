package com.sih2026.touristsafety.presentation.screens.safety

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sih2026.touristsafety.services.DetectionEvent
import com.sih2026.touristsafety.services.SOSTriggerEvent
import com.sih2026.touristsafety.services.ScreamDetectionService
import com.sih2026.touristsafety.services.SOSManager
import com.sih2026.touristsafety.services.ThreatLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WomenSafetyViewModel @Inject constructor(
    private val sosManager: SOSManager
) : ViewModel() {

    private val _isEnabled = MutableStateFlow(false)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private val _currentThreatLevel = MutableStateFlow(ThreatLevel.LOW)
    val currentThreatLevel: StateFlow<ThreatLevel> = _currentThreatLevel.asStateFlow()

    private val _latestEvent = MutableStateFlow<DetectionEvent?>(null)
    val latestEvent: StateFlow<DetectionEvent?> = _latestEvent.asStateFlow()

    private val _sensitivity = MutableStateFlow(0.5f)
    val sensitivity: StateFlow<Float> = _sensitivity.asStateFlow()
    
    private val _activeSpeakerGender = MutableStateFlow<String?>(null)
    val activeSpeakerGender: StateFlow<String?> = _activeSpeakerGender.asStateFlow()

    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    // NEW: Keyword detection states
    private val _keywordCount = MutableStateFlow(0)
    val keywordCount: StateFlow<Int> = _keywordCount.asStateFlow()

    private val _latestTranscript = MutableStateFlow<String?>(null)
    val latestTranscript: StateFlow<String?> = _latestTranscript.asStateFlow()

    private val _sosCountdown = MutableStateFlow(-1) // -1 = inactive
    val sosCountdown: StateFlow<Int> = _sosCountdown.asStateFlow()

    init {
        // Collect events from the service - keep only the single latest event
        viewModelScope.launch {
            ScreamDetectionService.events.collect { event ->
                _latestEvent.value = event
            }
        }
        
        viewModelScope.launch {
            ScreamDetectionService.threatLevel.collect { level ->
                _currentThreatLevel.value = level
            }
        }
        
        viewModelScope.launch {
            ScreamDetectionService.activeSpeakerGender.collect { gender ->
                _activeSpeakerGender.value = gender
            }
        }

        viewModelScope.launch {
            ScreamDetectionService.audioAmplitude.collect { amp ->
                _audioAmplitude.value = amp
            }
        }

        // NEW: Collect keyword detection flows
        viewModelScope.launch {
            ScreamDetectionService.keywordCount.collect { count ->
                _keywordCount.value = count
            }
        }

        viewModelScope.launch {
            ScreamDetectionService.latestTranscript.collect { transcript ->
                _latestTranscript.value = transcript
            }
        }

        viewModelScope.launch {
            ScreamDetectionService.sosCountdown.collect { seconds ->
                _sosCountdown.value = seconds
            }
        }

        // NEW: Handle SOS trigger — set context message and activate SOS
        viewModelScope.launch {
            ScreamDetectionService.sosTrigger.collect { triggerEvent ->
                handleSOSTrigger(triggerEvent)
            }
        }
    }

    private fun handleSOSTrigger(event: SOSTriggerEvent) {
        // Set the transcribed context as the SMS message
        val contextMsg = if (!event.transcript.isNullOrBlank()) {
            "${event.reason}. User said: \"${event.transcript}\""
        } else {
            event.reason
        }
        sosManager.setContextMessage(contextMsg)
        sosManager.activateSOS { smsSent, locationShared, audioRecording, latitude, longitude ->
            // SOS activated with context-aware message
        }
    }

    fun toggleMonitoring(context: Context) {
        val nextState = !_isEnabled.value
        _isEnabled.value = nextState
        _isMonitoring.value = nextState
        
        val intent = Intent(context, ScreamDetectionService::class.java).apply {
            action = if (nextState) ScreamDetectionService.ACTION_START else ScreamDetectionService.ACTION_STOP
        }
        
        if (nextState) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } else {
            context.startService(intent)
            _currentThreatLevel.value = ThreatLevel.LOW
            _keywordCount.value = 0
            _latestTranscript.value = null
            _sosCountdown.value = -1
            _latestEvent.value = null
        }
    }

    fun cancelSOSCountdown(context: Context) {
        val intent = Intent(context, ScreamDetectionService::class.java).apply {
            action = ScreamDetectionService.ACTION_CANCEL_COUNTDOWN
        }
        context.startService(intent)
    }

    fun updateSensitivity(level: Float) {
        _sensitivity.value = level
    }

    fun testDetection() {
        // Mock a test detection
        viewModelScope.launch {
            val event = DetectionEvent(
                System.currentTimeMillis(),
                "Test Detection",
                "Gender: female, Distress: 85%",
                75
            )
            _latestEvent.value = event
            _currentThreatLevel.value = ThreatLevel.CRITICAL
        }
    }
}
