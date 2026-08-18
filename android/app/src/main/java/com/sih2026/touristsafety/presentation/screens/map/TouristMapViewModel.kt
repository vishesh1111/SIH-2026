package com.sih2026.touristsafety.presentation.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.sih2026.touristsafety.data.local.entities.GeofenceZoneEntity
import com.sih2026.touristsafety.domain.model.NearbyPlace
import com.sih2026.touristsafety.domain.model.TouristLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TouristMapViewModel @Inject constructor() : ViewModel() {

    private val _userLocation = MutableStateFlow<LatLng?>(LatLng(28.6139, 77.2090)) // Default to Delhi
    val userLocation: StateFlow<LatLng?> = _userLocation.asStateFlow()

    private val _nearbyTourists = MutableStateFlow<List<TouristLocation>>(emptyList())
    val nearbyTourists: StateFlow<List<TouristLocation>> = _nearbyTourists.asStateFlow()

    private val _nearbyPlaces = MutableStateFlow<List<NearbyPlace>>(emptyList())
    val nearbyPlaces: StateFlow<List<NearbyPlace>> = _nearbyPlaces.asStateFlow()

    private val _dangerZones = MutableStateFlow<List<GeofenceZoneEntity>>(emptyList())
    val dangerZones: StateFlow<List<GeofenceZoneEntity>> = _dangerZones.asStateFlow()

    private val _isShowingTourists = MutableStateFlow(false)
    val isShowingTourists: StateFlow<Boolean> = _isShowingTourists.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    init {
        loadNearbyPlaces()
        loadDangerZones()
        loadTourists()
    }

    fun toggleTouristVisibility() {
        _isShowingTourists.value = !_isShowingTourists.value
    }
    
    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun loadNearbyPlaces() {
        // Mock data
        _nearbyPlaces.value = listOf(
            NearbyPlace("1", "India Gate", "monument", 28.6129, 77.2295, 1.2, 4.8f),
            NearbyPlace("2", "City Hospital", "hospital", 28.6145, 77.2085, 0.5, 4.2f),
            NearbyPlace("3", "Central Police Station", "police", 28.6200, 77.2100, 0.8, 4.0f)
        )
    }

    fun loadDangerZones() {
        // Mock data
        _dangerZones.value = listOf(
            GeofenceZoneEntity(
                id = "1",
                name = "Unsafe Area (Night)",
                zoneType = "DANGER",
                latitude = 28.6250,
                longitude = 77.2200,
                radius = 500.0f,
                description = "High crime area at night",
                alertMessage = "Avoid this area",
                severity = 3,
                state = "Delhi",
                isActive = true
            )
        )
    }

    private fun loadTourists() {
        // Mock data
        _nearbyTourists.value = listOf(
            TouristLocation("1", "John Doe", 28.6140, 77.2100, "USA"),
            TouristLocation("2", "Jane Smith", 28.6130, 77.2080, "UK")
        )
    }

    fun connectWithTourist(touristId: String) {
        // Logic to connect
    }
}
