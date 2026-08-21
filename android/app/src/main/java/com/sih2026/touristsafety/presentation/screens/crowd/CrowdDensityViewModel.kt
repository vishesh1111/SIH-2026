package com.sih2026.touristsafety.presentation.screens.crowd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for Crowd Density Screen
 */
data class CrowdDensityUiState(
    val isLoading: Boolean = false,
    val centerLocation: LatLng = LatLng(18.9220, 72.8347), // Gateway of India
    val crowdPoints: List<LatLng> = emptyList(),
    val densityInfo: DensityInfo? = null,
    val hotspots: List<Hotspot> = emptyList(),
    val isRealTimeEnabled: Boolean = false,
    val lastUpdateTime: Long = System.currentTimeMillis()
)

/**
 * ViewModel for Crowd Density Map Screen
 * 
 * Manages mock data generation and simulated real-time updates
 */
@HiltViewModel
class CrowdDensityViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CrowdDensityUiState())
    val uiState: StateFlow<CrowdDensityUiState> = _uiState.asStateFlow()

    private val mockDensityStream = MockDensityStream()

    init {
        loadInitialData()
    }

    /**
     * Load initial mock crowd data
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Simulate network delay
            delay(500)
            
            val centerLocation = _uiState.value.centerLocation
            val crowdPoints = generateMockCrowdData(
                center = centerLocation,
                radiusMeters = 200.0,
                count = 85
            )
            val densityInfo = analyzeMockDensity(crowdPoints.size)
            val hotspots = generateMockHotspots(centerLocation)
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                crowdPoints = crowdPoints,
                densityInfo = densityInfo,
                hotspots = hotspots,
                lastUpdateTime = System.currentTimeMillis()
            )
        }
    }

    /**
     * Refresh crowd data (simulates new GPS readings)
     */
    fun refreshData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            delay(300)
            
            val centerLocation = _uiState.value.centerLocation
            val newCrowdPoints = generateMockCrowdData(
                center = centerLocation,
                radiusMeters = 200.0,
                count = (70..100).random() // Vary the count for realism
            )
            val newDensityInfo = analyzeMockDensity(newCrowdPoints.size)
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                crowdPoints = newCrowdPoints,
                densityInfo = newDensityInfo,
                lastUpdateTime = System.currentTimeMillis()
            )
        }
    }

    /**
     * Toggle real-time updates (simulated)
     */
    fun toggleRealTimeUpdates() {
        val newState = !_uiState.value.isRealTimeEnabled
        _uiState.value = _uiState.value.copy(isRealTimeEnabled = newState)
        
        if (newState) {
            startRealTimeSimulation()
        }
    }

    /**
     * Simulate real-time density updates
     */
    private fun startRealTimeSimulation() {
        viewModelScope.launch {
            while (_uiState.value.isRealTimeEnabled) {
                delay(5000) // Update every 5 seconds
                
                if (!_uiState.value.isRealTimeEnabled) break
                
                val centerLocation = _uiState.value.centerLocation
                val newCrowdPoints = generateMockCrowdData(
                    center = centerLocation,
                    radiusMeters = 200.0,
                    count = (75..95).random()
                )
                val newDensityInfo = mockDensityStream.getNextDensityUpdate()
                
                _uiState.value = _uiState.value.copy(
                    crowdPoints = newCrowdPoints,
                    densityInfo = newDensityInfo,
                    lastUpdateTime = System.currentTimeMillis()
                )
            }
        }
    }

    /**
     * Update center location (e.g., when user moves to different area)
     */
    fun updateCenterLocation(newLocation: LatLng) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                centerLocation = newLocation,
                isLoading = true
            )
            
            delay(300)
            
            val crowdPoints = generateMockCrowdData(
                center = newLocation,
                radiusMeters = 200.0,
                count = 85
            )
            val densityInfo = analyzeMockDensity(crowdPoints.size)
            val hotspots = generateMockHotspots(newLocation)
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                crowdPoints = crowdPoints,
                densityInfo = densityInfo,
                hotspots = hotspots,
                lastUpdateTime = System.currentTimeMillis()
            )
        }
    }

    /**
     * Simulate different crowd scenarios for demo
     */
    fun loadScenario(scenario: CrowdScenario) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            delay(300)
            
            val centerLocation = _uiState.value.centerLocation
            val (pointCount, radiusMeters) = when (scenario) {
                CrowdScenario.LOW_CROWD -> Pair(30, 250.0)
                CrowdScenario.MODERATE_CROWD -> Pair(65, 200.0)
                CrowdScenario.HIGH_CROWD -> Pair(95, 180.0)
                CrowdScenario.CRITICAL_CROWD -> Pair(115, 150.0)
            }
            
            val crowdPoints = generateMockCrowdData(
                center = centerLocation,
                radiusMeters = radiusMeters,
                count = pointCount
            )
            val densityInfo = analyzeMockDensity(crowdPoints.size)
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                crowdPoints = crowdPoints,
                densityInfo = densityInfo,
                lastUpdateTime = System.currentTimeMillis()
            )
        }
    }
}

/**
 * Pre-defined crowd scenarios for demo purposes
 */
enum class CrowdScenario {
    LOW_CROWD,
    MODERATE_CROWD,
    HIGH_CROWD,
    CRITICAL_CROWD
}
