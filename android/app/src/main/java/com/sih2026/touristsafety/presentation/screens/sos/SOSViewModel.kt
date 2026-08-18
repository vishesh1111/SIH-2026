package com.sih2026.touristsafety.presentation.screens.sos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sih2026.touristsafety.services.SOSManager
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
        val latitude: Double? = null,
        val longitude: Double? = null
    ) : SOSState()
    object Resolved : SOSState()
}

@HiltViewModel
class SOSViewModel @Inject constructor(
    private val sosManager: SOSManager
) : ViewModel() {

    private val _sosState = MutableStateFlow<SOSState>(SOSState.Idle)
    val sosState: StateFlow<SOSState> = _sosState.asStateFlow()

    fun activateSOS() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val timestamp = dateFormat.format(Date())
        
        _sosState.value = SOSState.Active(
            timestamp = timestamp,
            smsSent = false,
            locationShared = false,
            audioRecording = false
        )

        viewModelScope.launch {
            sosManager.activateSOS { smsSent, locationShared, audioRecording, lat, lon ->
                _sosState.value = SOSState.Active(
                    timestamp = timestamp,
                    smsSent = smsSent,
                    locationShared = locationShared,
                    audioRecording = audioRecording,
                    latitude = lat,
                    longitude = lon
                )
            }
        }
    }

    fun cancelSOS() {
        sosManager.cancelSOS()
        _sosState.value = SOSState.Idle
    }
}
