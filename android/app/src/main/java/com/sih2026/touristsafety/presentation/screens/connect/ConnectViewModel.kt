package com.sih2026.touristsafety.presentation.screens.connect

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.sih2026.touristsafety.domain.model.TouristLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ConnectViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _isSharing = MutableStateFlow(false)
    val isSharing: StateFlow<Boolean> = _isSharing.asStateFlow()

    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation.asStateFlow()
    
    private val _userAddress = MutableStateFlow<String?>(null)
    val userAddress: StateFlow<String?> = _userAddress.asStateFlow()

    private val _otherTourists = MutableStateFlow<List<TouristLocation>>(emptyList())
    val otherTourists: StateFlow<List<TouristLocation>> = _otherTourists.asStateFlow()

    private var sharingJob: Job? = null
    
    private suspend fun getAddress(lat: Double, lng: Double): String = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                // Combine locality parts, ignoring Plus Codes (featureName often gives plus codes)
                val parts = listOfNotNull(address.subLocality, address.locality, address.adminArea)
                if (parts.isNotEmpty()) {
                    parts.joinToString(", ")
                } else {
                    address.featureName ?: "Unknown Location"
                }
            } else {
                "Unknown Location"
            }
        } catch (e: Exception) {
            "Unknown Location"
        }
    }

    fun updateUserLocation(latLng: LatLng) {
        _userLocation.value = latLng
        
        viewModelScope.launch {
            _userAddress.value = getAddress(latLng.latitude, latLng.longitude)
            // Show tourists even before hitting start
            if (_otherTourists.value.isEmpty()) {
                fetchOtherTourists(latLng)
            }
        }
        
        if (_isSharing.value) {
            // Instantly send update if we are already sharing
            viewModelScope.launch {
                sendLocationToNetwork(latLng)
            }
        }
    }

    fun toggleSharing() {
        val newState = !_isSharing.value
        _isSharing.value = newState

        if (newState) {
            startSharingRoutine()
        } else {
            stopSharingRoutine()
        }
    }

    private fun startSharingRoutine() {
        sharingJob?.cancel()
        sharingJob = viewModelScope.launch {
            while (_isSharing.value) {
                _userLocation.value?.let { location ->
                    sendLocationToNetwork(location)
                    fetchOtherTourists(location)
                }
                delay(60_000) // 1 minute
            }
        }
    }

    private fun stopSharingRoutine() {
        sharingJob?.cancel()
        sharingJob = null
        // Don't clear tourists, so we can still see them even when stopped
    }

    private suspend fun sendLocationToNetwork(location: LatLng) {
        // In a real app, this would use SupabaseManager to upload coords.
        // For now, we simulate a successful upload.
        delay(500)
    }

    private suspend fun fetchOtherTourists(center: LatLng) {
        // Simulate fetching tourists nearby from network
        delay(500)
        
        val randomTourists = buildList {
            // Place 20 tourists within a ~50km radius (approx 0.45 degrees)
            repeat(20) { i ->
                val offsetLat = (Math.random() - 0.5) * 0.9
                val offsetLng = (Math.random() - 0.5) * 0.9
                val lat = center.latitude + offsetLat
                val lng = center.longitude + offsetLng
                val address = getAddress(lat, lng)
                
                add(
                    TouristLocation(
                        id = "tourist_$i",
                        name = "Tourist ${i + 1}",
                        nationality = address, // Show exact location name
                        latitude = lat,
                        longitude = lng
                    )
                )
            }
        }
        _otherTourists.value = randomTourists
    }
}
