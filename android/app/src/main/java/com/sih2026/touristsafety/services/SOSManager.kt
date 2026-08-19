package com.sih2026.touristsafety.services

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.media.MediaRecorder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Looper
import android.telephony.SmsManager
import com.google.android.gms.location.*
import com.sih2026.touristsafety.data.local.dao.EmergencyContactDao
import com.sih2026.touristsafety.data.local.entities.EmergencyContactEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SOSManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contactDao: EmergencyContactDao,
    private val physicalSignalService: PhysicalSignalService
) {
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private var mediaRecorder: MediaRecorder? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private var isActive = false

    /**
     * Callback signature:
     * (smsSent, locationShared, audioRecording, bleAdvertising, physicalSignaling, latitude, longitude)
     */
    @SuppressLint("MissingPermission")
    fun activateSOS(onStatusUpdate: (Boolean, Boolean, Boolean, Boolean, Boolean, Double?, Double?) -> Unit) {
        if (isActive) return
        isActive = true

        var smsSent = false
        var locationShared = false
        var audioRecording = false
        var bleAdvertising = false
        var physicalSignaling = false
        var latitude: Double? = null
        var longitude: Double? = null

        // 0. Activate Physical Signals immediately (siren + flashlight SOS + max brightness)
        physicalSignalService.activate()
        physicalSignaling = true

        // 1. Start Audio Recording
        audioRecording = startAudioRecording()
        onStatusUpdate(smsSent, locationShared, audioRecording, bleAdvertising, physicalSignaling, latitude, longitude)

        // 2. Get Location & Send SMS (or BLE if offline)
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMaxUpdates(1)
            .build()
            
        fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                    locationShared = true
                    
                    scope.launch {
                        // Try with user ID first, then fall back to all contacts
                        var contacts = contactDao.getContactsForUser("default_user").firstOrNull() ?: emptyList()
                        if (contacts.isEmpty()) {
                            contacts = contactDao.getAllContacts().firstOrNull() ?: emptyList()
                        }
                        android.util.Log.d("SOSManager", "Found ${contacts.size} emergency contacts")
                        smsSent = sendEmergencySMS(contacts, location)
                        android.util.Log.d("SOSManager", "SMS sent result: $smsSent")
                        
                        // If SMS failed (no cellular signal), activate BLE broadcasting
                        if (!smsSent || !isNetworkAvailable()) {
                            startBLEAdvertising(location)
                            bleAdvertising = true
                        }

                        launch(Dispatchers.Main) {
                            onStatusUpdate(smsSent, locationShared, audioRecording, bleAdvertising, physicalSignaling, latitude, longitude)
                        }
                    }
                }
                fusedLocationClient.removeLocationUpdates(this)
            }
        }, Looper.getMainLooper())
        
        createSOSAlert()
    }

    /**
     * Legacy callback for backward compatibility (without BLE/physical signal parameters).
     */
    @SuppressLint("MissingPermission")
    fun activateSOS(onStatusUpdate: (Boolean, Boolean, Boolean, Double?, Double?) -> Unit) {
        activateSOS { smsSent, locationShared, audioRecording, _, _, lat, lon ->
            onStatusUpdate(smsSent, locationShared, audioRecording, lat, lon)
        }
    }

    /**
     * Starts the NearbySOSService to broadcast an SOS beacon via BLE.
     * Works even in Airplane mode (BLE can be enabled independently).
     */
    private fun startBLEAdvertising(location: Location) {
        try {
            val intent = Intent(context, NearbySOSService::class.java).apply {
                action = NearbySOSService.ACTION_START_ADVERTISING
                putExtra(NearbySOSService.EXTRA_LATITUDE, location.latitude)
                putExtra(NearbySOSService.EXTRA_LONGITUDE, location.longitude)
                putExtra(NearbySOSService.EXTRA_USER_ID, "default_user")
                putExtra(NearbySOSService.EXTRA_SOS_TYPE, 0.toByte())
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Stops BLE advertising when SOS is cancelled.
     */
    private fun stopBLEAdvertising() {
        try {
            val intent = Intent(context, NearbySOSService::class.java).apply {
                action = NearbySOSService.ACTION_STOP_ADVERTISING
            }
            context.startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Check if the device has any network connectivity (cellular or WiFi).
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun sendEmergencySMS(contacts: List<EmergencyContactEntity>, location: Location): Boolean {
        if (contacts.isEmpty()) {
            android.util.Log.w("SOSManager", "No emergency contacts to send SMS to")
            return false
        }
        return try {
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            
            val mapsLink = "https://maps.google.com/?q=${location.latitude},${location.longitude}"
            val message = "SOS! I need help. My current location is: $mapsLink"
            
            var sentCount = 0
            contacts.forEach { contact ->
                try {
                    // Clean phone number: remove spaces, dashes, parentheses
                    val cleanPhone = "${contact.countryCode}${contact.phone}"
                        .replace(" ", "")
                        .replace("-", "")
                        .replace("(", "")
                        .replace(")", "")
                    android.util.Log.d("SOSManager", "Sending SMS to: $cleanPhone (${contact.name})")
                    smsManager.sendTextMessage(cleanPhone, null, message, null, null)
                    sentCount++
                } catch (e: Exception) {
                    android.util.Log.e("SOSManager", "Failed to send SMS to ${contact.name}: ${e.message}")
                }
            }
            android.util.Log.d("SOSManager", "SMS sent to $sentCount/${contacts.size} contacts")
            sentCount > 0
        } catch (e: Exception) {
            android.util.Log.e("SOSManager", "SMS sending failed: ${e.message}", e)
            false
        }
    }

    private fun startAudioRecording(): Boolean {
        return try {
            val audioFile = File(context.cacheDir, "sos_audio_${System.currentTimeMillis()}.3gp")
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFile.absolutePath)
                prepare()
                start()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun createSOSAlert() {
        // Save alert to local database
    }

    fun cancelSOS() {
        isActive = false
        stopBLEAdvertising()
        physicalSignalService.deactivate()
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
