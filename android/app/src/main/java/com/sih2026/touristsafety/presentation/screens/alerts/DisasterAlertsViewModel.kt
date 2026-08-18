package com.sih2026.touristsafety.presentation.screens.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sih2026.touristsafety.data.local.entities.DisasterAlertEntity
import com.sih2026.touristsafety.data.repository.DisasterAlertRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DisasterAlertsViewModel @Inject constructor(
    private val repository: DisasterAlertRepository
) : ViewModel() {

    private val _alerts = MutableStateFlow<List<DisasterAlertEntity>>(emptyList())
    val alerts: StateFlow<List<DisasterAlertEntity>> = _alerts.asStateFlow()

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    init {
        loadAlerts("All")
        refreshAlerts()
    }

    fun filterAlerts(type: String) {
        _selectedFilter.value = type
        loadAlerts(type)
    }

    private fun loadAlerts(type: String) {
        viewModelScope.launch {
            if (type == "All") {
                repository.getAlerts().collectLatest {
                    _alerts.value = it
                }
            } else {
                repository.getAlertsByType(type).collectLatest {
                    _alerts.value = it
                }
            }
        }
    }

    fun refreshAlerts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.refreshAlertsFromServer()
                _isOffline.value = false
            } catch (e: Exception) {
                _isOffline.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }
}
