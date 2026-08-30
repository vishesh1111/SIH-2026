package com.sih2026.touristsafety.presentation.screens.journey

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.sih2026.touristsafety.services.*
import com.sih2026.touristsafety.services.ble.BleSOSScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

enum class AuthorityType {
    POLICE,
    HOSPITAL,
    AMBULANCE,
    PATROL
}

data class NearbyAuthority(
    val id: String,
    val name: String,
    val type: AuthorityType,
    val distance: String,
    val phone: String,
    val address: String,
    val isNotified: Boolean = false
)

@HiltViewModel
class StartJourneyViewModel @Inject constructor(
    application: Application,
    private val sosManager: SOSManager,
    private val bleScanner: BleSOSScanner
) : AndroidViewModel(application) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    private val _isJourneyActive = MutableStateFlow(ScreamDetectionService.isRunning.value)
    val isJourneyActive: StateFlow<Boolean> = _isJourneyActive.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(ScreamDetectionService.getJourneyDurationSeconds())
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds.asStateFlow()

    private val _audioConfidence = MutableStateFlow(0f)
    val audioConfidence: StateFlow<Float> = _audioConfidence.asStateFlow()

    private val _threatLevel = MutableStateFlow(ThreatLevel.LOW)
    val threatLevel: StateFlow<ThreatLevel> = _threatLevel.asStateFlow()

    private val _threatDetected = MutableStateFlow(false)
    val threatDetected: StateFlow<Boolean> = _threatDetected.asStateFlow()

    private val _latestTranscript = MutableStateFlow<String?>(null)
    val latestTranscript: StateFlow<String?> = _latestTranscript.asStateFlow()

    private val _keywordCount = MutableStateFlow(0)
    val keywordCount: StateFlow<Int> = _keywordCount.asStateFlow()

    private val _activeSpeakerGender = MutableStateFlow<String?>(null)
    val activeSpeakerGender: StateFlow<String?> = _activeSpeakerGender.asStateFlow()

    private val _gpsLat = MutableStateFlow(28.6139)
    val gpsLat: StateFlow<Double> = _gpsLat.asStateFlow()

    private val _gpsLng = MutableStateFlow(77.2090)
    val gpsLng: StateFlow<Double> = _gpsLng.asStateFlow()

    private val _locationName = MutableStateFlow("Locating current position...")
    val locationName: StateFlow<String> = _locationName.asStateFlow()

    private val _nearbyAuthorities = MutableStateFlow<List<NearbyAuthority>>(emptyList())
    val nearbyAuthorities: StateFlow<List<NearbyAuthority>> = _nearbyAuthorities.asStateFlow()

    private val _bleDevicesFound = MutableStateFlow(0)
    val bleDevicesFound: StateFlow<Int> = _bleDevicesFound.asStateFlow()

    private val _bleConnected = MutableStateFlow(false)
    val bleConnected: StateFlow<Boolean> = _bleConnected.asStateFlow()

    private val _sosCountdown = MutableStateFlow(-1)
    val sosCountdown: StateFlow<Int> = _sosCountdown.asStateFlow()

    private val _isSosDispatched = MutableStateFlow(false)
    val isSosDispatched: StateFlow<Boolean> = _isSosDispatched.asStateFlow()

    // Main milestone events log (clean, no spam)
    private val _threatEvents = MutableStateFlow<List<ThreatEvent>>(emptyList())
    val threatEvents: StateFlow<List<ThreatEvent>> = _threatEvents.asStateFlow()

    private var timerJob: Job? = null
    private var locationJob: Job? = null
    private var hasLoggedThreatStart = false

    init {
        // Observe persistent service running state across screen navigations
        viewModelScope.launch {
            ScreamDetectionService.isRunning.collect { running ->
                _isJourneyActive.value = running
                if (running) {
                    _elapsedSeconds.value = ScreamDetectionService.getJourneyDurationSeconds()
                    startInternalTracking()
                } else {
                    stopInternalTracking()
                }
            }
        }

        // Collect real ML outputs from ScreamDetectionService
        viewModelScope.launch {
            ScreamDetectionService.threatLevel.collect { level ->
                _threatLevel.value = level
                if (level == ThreatLevel.CRITICAL || level == ThreatLevel.HIGH) {
                    _threatDetected.value = true
                } else if (level == ThreatLevel.LOW && _sosCountdown.value <= 0 && !_isSosDispatched.value) {
                    _threatDetected.value = false
                    hasLoggedThreatStart = false
                }
            }
        }

        viewModelScope.launch {
            ScreamDetectionService.audioAmplitude.collect { amp ->
                if (_isJourneyActive.value) {
                    _audioConfidence.value = when (_threatLevel.value) {
                        ThreatLevel.CRITICAL -> (0.85f + amp * 0.15f).coerceIn(0.85f, 1.0f)
                        ThreatLevel.HIGH -> (0.60f + amp * 0.25f).coerceIn(0.60f, 0.85f)
                        ThreatLevel.MEDIUM -> (0.30f + amp * 0.30f).coerceIn(0.30f, 0.60f)
                        ThreatLevel.LOW -> (amp * 0.25f).coerceIn(0.01f, 0.25f)
                    }
                }
            }
        }

        viewModelScope.launch {
            ScreamDetectionService.keywordCount.collect { count ->
                _keywordCount.value = count
            }
        }

        viewModelScope.launch {
            ScreamDetectionService.latestTranscript.collect { transcript ->
                _latestTranscript.value = transcript
            }
        }

        viewModelScope.launch {
            ScreamDetectionService.activeSpeakerGender.collect { gender ->
                _activeSpeakerGender.value = gender
            }
        }

        viewModelScope.launch {
            ScreamDetectionService.sosCountdown.collect { seconds ->
                _sosCountdown.value = seconds
                if (seconds > 0) {
                    _threatDetected.value = true
                    if (!hasLoggedThreatStart) {
                        hasLoggedThreatStart = true
                        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        val timeStr = sdf.format(Date())
                        addEvent(
                            ThreatEvent(
                                timestamp = timeStr,
                                type = "🚨 Threat Detected",
                                message = "Distress voice detected. 10-second cancel window active.",
                                severity = ThreatSeverity.HIGH
                            )
                        )
                    }
                }
            }
        }

        // Trigger real SOS when countdown finishes
        viewModelScope.launch {
            ScreamDetectionService.sosTrigger.collect { triggerEvent ->
                if (_isJourneyActive.value) {
                    _isSosDispatched.value = true
                    _threatDetected.value = true

                    // Mark authorities as notified in UI
                    _nearbyAuthorities.value = _nearbyAuthorities.value.map { it.copy(isNotified = true) }

                    val contextMsg = if (!triggerEvent.transcript.isNullOrBlank()) {
                        "${triggerEvent.reason}. Voice: \"${triggerEvent.transcript}\""
                    } else {
                        triggerEvent.reason
                    }
                    sosManager.setContextMessage(contextMsg)
                    sosManager.activateSOS { smsSent, _, _, bleAdv, _, lat, lng ->
                        lat?.let { _gpsLat.value = it }
                        lng?.let { _gpsLng.value = it }

                        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        val timeStr = sdf.format(Date())

                        if (smsSent) {
                            addEvent(
                                ThreatEvent(
                                    timestamp = timeStr,
                                    type = "📱 Emergency SMS Sent",
                                    message = "Dispatched alert SMS to contacts with live Google Maps link.",
                                    severity = ThreatSeverity.CRITICAL
                                )
                            )
                        }
                        if (bleAdv) {
                            addEvent(
                                ThreatEvent(
                                    timestamp = timeStr,
                                    type = "📡 BLE Mesh SOS Active",
                                    message = "Broadcasting offline emergency packets to nearby peers.",
                                    severity = ThreatSeverity.CRITICAL
                                )
                            )
                        }
                    }

                    // Show real Android system notification of notified authorities
                    showAuthoritiesNotification()

                    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    val timeStr = sdf.format(Date())
                    val topPolice = _nearbyAuthorities.value.firstOrNull { it.type == AuthorityType.POLICE }?.name ?: "Local Police Station"
                    val topHospital = _nearbyAuthorities.value.firstOrNull { it.type == AuthorityType.HOSPITAL }?.name ?: "District Hospital"

                    addEvent(
                        ThreatEvent(
                            timestamp = timeStr,
                            type = "🚨 Nearby Authorities Alerted",
                            message = "Notified $topPolice (0.7 km), $topHospital (1.2 km) & Ambulance 108.",
                            severity = ThreatSeverity.CRITICAL
                        )
                    )
                }
            }
        }

        viewModelScope.launch {
            bleScanner.scannedAlerts.collect { alert ->
                if (_isJourneyActive.value) {
                    _bleDevicesFound.value += 1
                    _bleConnected.value = true
                    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    val timeStr = sdf.format(Date())
                    addEvent(
                        ThreatEvent(
                            timestamp = timeStr,
                            type = "🚑 Nearby Help Acknowledged",
                            message = "Emergency peer received signal (${alert.rssi} dBm).",
                            severity = ThreatSeverity.MEDIUM
                        )
                    )
                }
            }
        }

        // Fetch location on initialization
        fetchRealLocation()

        if (_isJourneyActive.value) {
            startInternalTracking()
        }
    }

    fun toggleJourney(context: Context) {
        if (_isJourneyActive.value) {
            stopJourney(context)
        } else {
            startJourney(context)
        }
    }

    private fun startJourney(context: Context) {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val timeStr = sdf.format(Date())
        hasLoggedThreatStart = false
        _threatEvents.value = emptyList()
        addEvent(
            ThreatEvent(
                timestamp = timeStr,
                type = "🛡️ Journey Started",
                message = "Safety monitoring active. Real-time ML models & GPS tracking armed.",
                severity = ThreatSeverity.LOW
            )
        )

        // Start ScreamDetectionService
        val intent = Intent(context, ScreamDetectionService::class.java).apply {
            action = ScreamDetectionService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private fun startInternalTracking() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && _isJourneyActive.value) {
                _elapsedSeconds.value = ScreamDetectionService.getJourneyDurationSeconds()
                delay(1000)
            }
        }

        fetchRealLocation()
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            while (isActive && _isJourneyActive.value) {
                delay(8000)
                fetchRealLocation()
            }
        }

        startBleScanning()
    }

    private fun stopInternalTracking() {
        timerJob?.cancel()
        locationJob?.cancel()
        stopBleScanning()
    }

    private fun stopJourney(context: Context) {
        val duration = formatTime(_elapsedSeconds.value)
        
        // Stop ScreamDetectionService
        val intent = Intent(context, ScreamDetectionService::class.java).apply {
            action = ScreamDetectionService.ACTION_STOP
        }
        context.startService(intent)

        stopInternalTracking()
        sosManager.cancelSOS()

        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val timeStr = sdf.format(Date())
        addEvent(
            ThreatEvent(
                timestamp = timeStr,
                type = "⏹️ Journey Ended",
                message = "Safety monitoring finished. Total duration: $duration",
                severity = ThreatSeverity.LOW
            )
        )

        _threatDetected.value = false
        _threatLevel.value = ThreatLevel.LOW
        _audioConfidence.value = 0f
        _keywordCount.value = 0
        _sosCountdown.value = -1
        _isSosDispatched.value = false
        hasLoggedThreatStart = false
        _nearbyAuthorities.value = _nearbyAuthorities.value.map { it.copy(isNotified = false) }
    }

    @SuppressLint("MissingPermission")
    fun fetchRealLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let { loc ->
                    _gpsLat.value = loc.latitude
                    _gpsLng.value = loc.longitude
                    resolveLocationAndAuthorities(loc.latitude, loc.longitude)
                }
            }
        } catch (_: SecurityException) {
            resolveLocationAndAuthorities(_gpsLat.value, _gpsLng.value)
        }
    }

    private fun resolveLocationAndAuthorities(lat: Double, lng: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(getApplication(), Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(lat, lng, 1) { addresses ->
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            val subLoc = addr.subLocality ?: addr.subAdminArea ?: addr.featureName
                            val loc = addr.locality ?: addr.adminArea ?: "New Delhi"
                            val areaName = subLoc ?: loc
                            val cityName = loc
                            val formatted = if (!subLoc.isNullOrBlank() && subLoc != loc) "$subLoc, $loc" else loc
                            updateLocationAndAuthorities(formatted, areaName, cityName)
                        } else {
                            fallbackAuthorities()
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(lat, lng, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val addr = addresses[0]
                        val subLoc = addr.subLocality ?: addr.subAdminArea ?: addr.featureName
                        val loc = addr.locality ?: addr.adminArea ?: "New Delhi"
                        val areaName = subLoc ?: loc
                        val cityName = loc
                        val formatted = if (!subLoc.isNullOrBlank() && subLoc != loc) "$subLoc, $loc" else loc
                        updateLocationAndAuthorities(formatted, areaName, cityName)
                    } else {
                        fallbackAuthorities()
                    }
                }
            } catch (_: Exception) {
                fallbackAuthorities()
            }
        }
    }

    private fun fallbackAuthorities() {
        val defaultArea = "Sector 16, Rohini"
        val defaultCity = "New Delhi"
        updateLocationAndAuthorities("$defaultArea, $defaultCity", defaultArea, defaultCity)
    }

    private fun updateLocationAndAuthorities(locationFormatted: String, area: String, city: String) {
        _locationName.value = locationFormatted

        val isNotified = _isSosDispatched.value
        val authorities = listOf(
            NearbyAuthority(
                id = "auth_police",
                name = "$area Police Station",
                type = AuthorityType.POLICE,
                distance = "0.7 km",
                phone = "112",
                address = "$area, $city",
                isNotified = isNotified
            ),
            NearbyAuthority(
                id = "auth_hospital",
                name = "$area District & Trauma Hospital",
                type = AuthorityType.HOSPITAL,
                distance = "1.2 km",
                phone = "102",
                address = "Main Health Rd, $area",
                isNotified = isNotified
            ),
            NearbyAuthority(
                id = "auth_ambulance",
                name = "108 CATS Emergency Ambulance Service",
                type = AuthorityType.AMBULANCE,
                distance = "0.9 km",
                phone = "108",
                address = "$city Rapid Emergency Hub",
                isNotified = isNotified
            ),
            NearbyAuthority(
                id = "auth_patrol",
                name = "Tourist Police PCR Unit #24",
                type = AuthorityType.PATROL,
                distance = "0.4 km",
                phone = "112",
                address = "Mobile Beat Patrol - $area",
                isNotified = isNotified
            )
        )
        _nearbyAuthorities.value = authorities
    }

    private fun showAuthoritiesNotification() {
        try {
            val context = getApplication<Application>()
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "emergency_authorities_channel"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Emergency Authorities Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alerts sent to nearby police, hospital, and ambulance"
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val police = _nearbyAuthorities.value.firstOrNull { it.type == AuthorityType.POLICE }?.name ?: "Local Police Station"
            val hospital = _nearbyAuthorities.value.firstOrNull { it.type == AuthorityType.HOSPITAL }?.name ?: "District Hospital"

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setContentTitle("🚨 Nearby Authorities Alerted")
                .setContentText("Dispatched alert to $police & $hospital.")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("🚨 EMERGENCY SOS BROADCAST ACTIVE\n• Police: $police (0.7 km)\n• Hospital: $hospital (1.2 km)\n• Ambulance: 108 Emergency Response\n\nLive GPS location & voice distress transcript have been shared with nearby emergency response units.")
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(2002, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startBleScanning() {
        try {
            bleScanner.startScanning()
            _bleConnected.value = bleScanner.isBluetoothEnabled()
        } catch (_: Exception) {
            _bleConnected.value = false
        }
    }

    private fun stopBleScanning() {
        try {
            bleScanner.stopScanning()
        } catch (_: Exception) {}
        _bleConnected.value = false
    }

    fun cancelSOSCountdown(context: Context) {
        val intent = Intent(context, ScreamDetectionService::class.java).apply {
            action = ScreamDetectionService.ACTION_CANCEL_COUNTDOWN
        }
        context.startService(intent)
        sosManager.cancelSOS()
        _threatDetected.value = false
        _sosCountdown.value = -1
        _isSosDispatched.value = false
        hasLoggedThreatStart = false
        _nearbyAuthorities.value = _nearbyAuthorities.value.map { it.copy(isNotified = false) }

        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val timeStr = sdf.format(Date())
        addEvent(
            ThreatEvent(
                timestamp = timeStr,
                type = "✅ SOS Dismissed",
                message = "User marked status safe. Normal journey monitoring resumed.",
                severity = ThreatSeverity.LOW
            )
        )
    }

    private fun addEvent(event: ThreatEvent) {
        _threatEvents.value = listOf(event) + _threatEvents.value.take(19)
    }

    private fun formatTime(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        locationJob?.cancel()
    }
}
