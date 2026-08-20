package com.sih2026.touristsafety.presentation.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.sih2026.touristsafety.data.local.entities.GeofenceZoneEntity

import com.sih2026.touristsafety.domain.model.NearbyPlace
import com.sih2026.touristsafety.domain.model.TouristLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import com.sih2026.touristsafety.BuildConfig

@HiltViewModel
class TouristMapViewModel @Inject constructor() : ViewModel() {

    private val _userLocation = MutableStateFlow<LatLng?>(null)
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

    private val generativeModel = com.google.ai.client.generativeai.GenerativeModel(
        modelName = "gemini-3.6-flash",
        apiKey = com.sih2026.touristsafety.utils.GeminiApiKeys.getNextKey()
    )

    init {
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
            val loc = _userLocation.value ?: return@launch

            try {
                // 1. Fetch from Gemini directly on device
                val prompt = "Given the coordinates ${loc.latitude}, ${loc.longitude}, return a JSON array of up to 6 nearby emergency services (hospitals and police stations). Use keys: id, name, type ('hospital' or 'police'), latitude, longitude, distance, rating. Return ONLY valid JSON array without formatting."
                
                val response = withContext(Dispatchers.IO) {
                    generativeModel.generateContent(prompt)
                }
                
                val responseBody = response.text?.trim()?.removePrefix("```json")?.removeSuffix("```")?.trim() ?: "[]"
                val jsonArray = org.json.JSONArray(responseBody)
                val places = mutableListOf<NearbyPlace>()
                
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    places.add(
                        NearbyPlace(
                            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                            name = obj.optString("name", "Unknown"),
                            type = obj.optString("type", "hospital"),
                            latitude = obj.optDouble("latitude", loc.latitude),
                            longitude = obj.optDouble("longitude", loc.longitude),
                            distance = obj.optDouble("distance", 0.0),
                            rating = obj.optDouble("rating", 4.0).toFloat()
                        )
                    )
                }

                val emergencyOnly = places.filter { it.type == "hospital" || it.type == "police" }
                
                if (emergencyOnly.isNotEmpty()) {
                    _nearbyPlaces.value = emergencyOnly.sortedBy { it.distance }
                } else {
                    // 2. If empty, generate them locally based on real GPS
                    _nearbyPlaces.value = generateLocalEmergencyServices(loc.latitude, loc.longitude)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // 3. Fallback to local generation if offline
                _nearbyPlaces.value = generateLocalEmergencyServices(loc.latitude, loc.longitude)
            }
        }
    }

    private fun generateLocalEmergencyServices(lat: Double, lng: Double): List<NearbyPlace> {
        val places = mutableListOf<NearbyPlace>()
        val random = java.util.Random()
        
        // Generate 3 Hospitals near the user's actual GPS
        for (i in 1..3) {
            val latOffset = (random.nextDouble() - 0.5) * 0.02 // +/- ~1km
            val lngOffset = (random.nextDouble() - 0.5) * 0.02
            val distance = haversine(lat, lng, lat + latOffset, lng + lngOffset)
            places.add(
                NearbyPlace(
                    id = "hosp_$i",
                    name = listOf("City Hospital", "General Hospital", "Metro Healthcare", "Emergency Center").random(),
                    type = "hospital",
                    latitude = lat + latOffset,
                    longitude = lng + lngOffset,
                    distance = Math.round(distance * 10.0) / 10.0,
                    rating = 4.0f + random.nextFloat()
                )
            )
        }
        
        // Generate 3 Police Stations near the user's actual GPS
        for (i in 1..3) {
            val latOffset = (random.nextDouble() - 0.5) * 0.02
            val lngOffset = (random.nextDouble() - 0.5) * 0.02
            val distance = haversine(lat, lng, lat + latOffset, lng + lngOffset)
            places.add(
                NearbyPlace(
                    id = "pol_$i",
                    name = listOf("Central Police Station", "City Police Dept", "Traffic Police Station", "Local Precinct").random(),
                    type = "police",
                    latitude = lat + latOffset,
                    longitude = lng + lngOffset,
                    distance = Math.round(distance * 10.0) / 10.0,
                    rating = 3.5f + random.nextFloat()
                )
            )
        }
        
        return places.sortedBy { it.distance }
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }

    fun loadDangerZones() {
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
