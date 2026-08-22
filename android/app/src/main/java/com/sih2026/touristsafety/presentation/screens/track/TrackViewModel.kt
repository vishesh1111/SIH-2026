package com.sih2026.touristsafety.presentation.screens.track

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

data class VisitedLocation(
    val name: String,
    val coordinates: LatLng,
    val arrivalTime: String,
    val departureTime: String,
    val duration: String
)

data class TrackUiState(
    val isLoading: Boolean = false,
    val visitedLocations: List<VisitedLocation> = emptyList(),
    val mapCenter: LatLng = LatLng(18.9220, 72.8347)
)

@HiltViewModel
class TrackViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackUiState())
    val uiState: StateFlow<TrackUiState> = _uiState.asStateFlow()

    fun updateUserLocation(userLocation: LatLng) {
        if (_uiState.value.visitedLocations.isNotEmpty() || _uiState.value.isLoading) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, mapCenter = userLocation)
            
            val mockCoords = listOf(
                LatLng(userLocation.latitude - 0.005, userLocation.longitude - 0.005),
                LatLng(userLocation.latitude - 0.002, userLocation.longitude + 0.001),
                LatLng(userLocation.latitude + 0.003, userLocation.longitude + 0.004),
                LatLng(userLocation.latitude + 0.007, userLocation.longitude - 0.002),
                userLocation
            )

            val mockTimes = listOf(
                Triple("09:00 AM", "09:45 AM", "45 mins"),
                Triple("10:30 AM", "11:30 AM", "1 hr"),
                Triple("12:15 PM", "01:30 PM", "1 hr 15 mins"),
                Triple("02:00 PM", "03:45 PM", "1 hr 45 mins"),
                Triple("04:30 PM", "Present", "Currently here")
            )

            val resolvedLocations = withContext(Dispatchers.IO) {
                val geocoder = Geocoder(context, Locale.getDefault())
                mockCoords.mapIndexed { index, latLng ->
                    var placeName = "Unknown Location"
                    try {
                        val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            val parts = listOfNotNull(addr.subLocality, addr.locality, addr.adminArea)
                            if (parts.isNotEmpty()) {
                                placeName = parts.joinToString(", ")
                            } else if (addr.featureName != null && !addr.featureName.contains("+")) {
                                placeName = addr.featureName
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    
                    VisitedLocation(
                        name = placeName,
                        coordinates = latLng,
                        arrivalTime = mockTimes[index].first,
                        departureTime = mockTimes[index].second,
                        duration = mockTimes[index].third
                    )
                }
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                visitedLocations = resolvedLocations,
                mapCenter = userLocation
            )
        }
    }
}
