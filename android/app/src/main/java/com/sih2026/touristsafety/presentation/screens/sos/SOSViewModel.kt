package com.sih2026.touristsafety.presentation.screens.sos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sih2026.touristsafety.services.SOSManager
import com.sih2026.touristsafety.services.ble.BleSOSAdvertiser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

sealed class SOSState {
    object Idle : SOSState()
    data class Countdown(val secondsLeft: Int) : SOSState()
    data class Active(
        val timestamp: String,
        val smsSent: Boolean,
        val locationShared: Boolean,
        val audioRecording: Boolean,
        val bleAdvertising: Boolean = false,
        val physicalSignaling: Boolean = false,
        val latitude: Double? = null,
        val longitude: Double? = null,
        val acknowledgments: List<String> = emptyList()
    ) : SOSState()
    object Resolved : SOSState()
}

@HiltViewModel
class SOSViewModel @Inject constructor(
    private val sosManager: SOSManager,
    private val bleSOSAdvertiser: BleSOSAdvertiser
) : ViewModel() {

    private val _sosState = MutableStateFlow<SOSState>(SOSState.Idle)
    val sosState: StateFlow<SOSState> = _sosState.asStateFlow()
    
    private val receivedAcks = mutableListOf<String>()

    init {
        viewModelScope.launch {
            bleSOSAdvertiser.acknowledgments.collect { ack ->
                if (!receivedAcks.contains(ack)) {
                    receivedAcks.add(ack)
                    val currentState = _sosState.value
                    if (currentState is SOSState.Active) {
                        _sosState.value = currentState.copy(acknowledgments = receivedAcks.toList())
                    }
                }
            }
        }
    }

    fun activateSOS() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val timestamp = dateFormat.format(Date())
        
        receivedAcks.clear()
        
        _sosState.value = SOSState.Active(
            timestamp = timestamp,
            smsSent = false,
            locationShared = false,
            audioRecording = false,
            bleAdvertising = false,
            physicalSignaling = false,
            acknowledgments = emptyList()
        )

        viewModelScope.launch {
            sosManager.activateSOS { smsSent, locationShared, audioRecording, bleAdvertising, physicalSignaling, lat, lon ->
                val currentState = _sosState.value
                val acks = if (currentState is SOSState.Active) currentState.acknowledgments else emptyList()
                
                _sosState.value = SOSState.Active(
                    timestamp = timestamp,
                    smsSent = smsSent,
                    locationShared = locationShared,
                    audioRecording = audioRecording,
                    bleAdvertising = bleAdvertising,
                    physicalSignaling = physicalSignaling,
                    latitude = lat,
                    longitude = lon,
                    acknowledgments = acks
                )
            }
        }
    }

    fun cancelSOS() {
        sosManager.cancelSOS()
        _sosState.value = SOSState.Idle
    }
}
