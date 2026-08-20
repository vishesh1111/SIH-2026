package com.sih2026.touristsafety.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sih2026.touristsafety.data.local.InactivityPreferences
import com.sih2026.touristsafety.data.local.dao.EmergencyContactDao
import com.sih2026.touristsafety.data.local.entities.EmergencyContactEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InactivitySettingsViewModel @Inject constructor(
    private val preferences: InactivityPreferences,
    private val contactDao: EmergencyContactDao
) : ViewModel() {

    val isEnabled: StateFlow<Boolean> = preferences.isEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val thresholdMinutes: StateFlow<Int> = preferences.thresholdMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 30)

    val stationaryEnabled: StateFlow<Boolean> = preferences.stationaryDetection
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val unusualMovementEnabled: StateFlow<Boolean> = preferences.unusualMovement
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val speedAnomalyEnabled: StateFlow<Boolean> = preferences.speedAnomaly
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val contacts: StateFlow<List<EmergencyContactEntity>> = contactDao.getContactsForUser("default_user")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setEnabled(value: Boolean) {
        viewModelScope.launch {
            preferences.setEnabled(value)
        }
    }

    fun setThresholdMinutes(value: Int) {
        viewModelScope.launch {
            preferences.setThresholdMinutes(value)
        }
    }

    fun setStationaryDetection(value: Boolean) {
        viewModelScope.launch {
            preferences.setStationaryDetection(value)
        }
    }

    fun setUnusualMovement(value: Boolean) {
        viewModelScope.launch {
            preferences.setUnusualMovement(value)
        }
    }

    fun setSpeedAnomaly(value: Boolean) {
        viewModelScope.launch {
            preferences.setSpeedAnomaly(value)
        }
    }
}
