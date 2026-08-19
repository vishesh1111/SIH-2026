package com.sih2026.touristsafety.presentation.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.sih2026.touristsafety.data.local.entities.GeofenceZoneEntity
import com.sih2026.touristsafety.data.remote.PlacesApiService
import com.sih2026.touristsafety.domain.model.NearbyPlace
import com.sih2026.touristsafety.domain.model.TouristLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TouristMapViewModel @Inject constructor(
    private val placesApi: PlacesApiService
) : ViewModel() {

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
        viewModelScope.launch {
            try {
                val loc = _userLocation.value ?: LatLng(28.6139, 77.2090)
                val places = placesApi.getNearbyPlaces(loc.latitude, loc.longitude)
                _nearbyPlaces.value = places
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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

    fun updateUserLocation(latLng: LatLng) {
        _userLocation.value = latLng
        loadNearbyPlaces()
    }

    fun connectWithTourist(touristId: String) {
        // Logic to connect
    }
}
