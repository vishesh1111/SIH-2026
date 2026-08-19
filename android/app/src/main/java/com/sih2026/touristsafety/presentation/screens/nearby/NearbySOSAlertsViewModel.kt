package com.sih2026.touristsafety.presentation.screens.nearby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sih2026.touristsafety.data.local.dao.ReceivedSOSAlertDao
import com.sih2026.touristsafety.data.local.entities.ReceivedSOSAlertEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NearbySOSAlertsViewModel @Inject constructor(
    private val alertDao: ReceivedSOSAlertDao
) : ViewModel() {

    val alerts: StateFlow<List<ReceivedSOSAlertEntity>> = alertDao.getAllAlerts().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun relayAlert(alertId: Int) {
        viewModelScope.launch {
            alertDao.markAsRelayed(alertId)
            // In a real app this would also POST to the backend, but for now just mark it.
        }
    }

    fun getDistanceEstimate(rssi: Int): String {
        return when {
            rssi > -50 -> "Very close (< 5m)"
            rssi > -70 -> "Nearby (5-20m)"
            rssi > -85 -> "In range (20-50m)"
            else -> "Far (50m+)"
        }
    }

    fun getSOSTypeLabel(sosType: Int): String {
        return when (sosType) {
            0 -> "General Emergency"
            1 -> "Medical Emergency"
            2 -> "Assault / Threat"
            3 -> "Natural Disaster"
            else -> "Unknown"
        }
    }
}
