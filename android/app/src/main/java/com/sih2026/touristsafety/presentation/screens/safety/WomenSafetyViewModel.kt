package com.sih2026.touristsafety.presentation.screens.safety

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sih2026.touristsafety.services.DetectionEvent
import com.sih2026.touristsafety.services.ScreamDetectionService
import com.sih2026.touristsafety.services.ThreatLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WomenSafetyViewModel @Inject constructor(
    // private val context: Context // Removed context injection to keep it clean, handled in UI
) : ViewModel() {

    private val _isEnabled = MutableStateFlow(false)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private val _currentThreatLevel = MutableStateFlow(ThreatLevel.LOW)
    val currentThreatLevel: StateFlow<ThreatLevel> = _currentThreatLevel.asStateFlow()

    private val _detectionLog = MutableStateFlow<List<DetectionEvent>>(emptyList())
    val detectionLog: StateFlow<List<DetectionEvent>> = _detectionLog.asStateFlow()

    private val _sensitivity = MutableStateFlow(0.5f)
    val sensitivity: StateFlow<Float> = _sensitivity.asStateFlow()

    init {
        // Collect events from the service
        viewModelScope.launch {
            ScreamDetectionService.events.collect { event ->
                val logs = _detectionLog.value.toMutableList()
                logs.add(0, event) // Add to top
                _detectionLog.value = logs
            }
        }
        
        viewModelScope.launch {
            ScreamDetectionService.threatLevel.collect { level ->
                _currentThreatLevel.value = level
            }
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
        }
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
            val logs = _detectionLog.value.toMutableList()
            logs.add(0, event)
            _detectionLog.value = logs
            _currentThreatLevel.value = ThreatLevel.CRITICAL
        }
    }
}
