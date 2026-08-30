package com.sih2026.touristsafety.services

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaRecorder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
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
    private var contextMessage: String? = null

    fun setContextMessage(message: String?) {
        contextMessage = message
    }

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

        // 1. Start Audio Recording
        audioRecording = startAudioRecording()
        onStatusUpdate(smsSent, locationShared, audioRecording, bleAdvertising, physicalSignaling, latitude, longitude)

        // 2. Fetch Location and Dispatch SMS immediately (no waiting for satellite lock)
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                val currentLoc = lastLoc ?: Location("default").apply {
                    this.latitude = 28.6139
                    this.longitude = 77.2090
                }
                latitude = currentLoc.latitude
                longitude = currentLoc.longitude
                locationShared = true

                // Start BLE Advertising
                startBLEAdvertising(currentLoc)
                bleAdvertising = true

                // Dispatch SMS immediately to all emergency contacts
                scope.launch {
                    var contacts = contactDao.getContactsForUser("default_user").firstOrNull() ?: emptyList()
                    if (contacts.isEmpty()) {
                        contacts = contactDao.getAllContacts().firstOrNull() ?: emptyList()
                    }
                    if (contacts.isEmpty()) {
                        contacts = listOf(
                            EmergencyContactEntity(
                                id = "default_police",
                                userId = "default_user",
                                name = "Tourist Helpline / Police",
                                phone = "112",
                                countryCode = "+91",
                                relationship = "Helpline",
                                residentAddress = null,
                                isPrimary = true
                            )
                        )
                    }
                    android.util.Log.d("SOSManager", "Found ${contacts.size} emergency contacts")
                    smsSent = sendEmergencySMS(contacts, currentLoc)
                    android.util.Log.d("SOSManager", "SMS sent result: $smsSent")

                    launch(Dispatchers.Main) {
                        onStatusUpdate(smsSent, locationShared, audioRecording, bleAdvertising, physicalSignaling, latitude, longitude)
                    }
                }
            }.addOnFailureListener {
                val fallbackLoc = Location("fallback").apply {
                    this.latitude = 28.6139
                    this.longitude = 77.2090
                }
                latitude = fallbackLoc.latitude
                longitude = fallbackLoc.longitude
                startBLEAdvertising(fallbackLoc)
                bleAdvertising = true

                scope.launch {
                    var contacts = contactDao.getAllContacts().firstOrNull() ?: emptyList()
                    if (contacts.isEmpty()) {
                        contacts = listOf(
                            EmergencyContactEntity(
                                id = "default_police",
                                userId = "default_user",
                                name = "Tourist Helpline / Police",
                                phone = "112",
                                countryCode = "+91",
                                relationship = "Helpline",
                                residentAddress = null,
                                isPrimary = true
                            )
                        )
                    }
                    smsSent = sendEmergencySMS(contacts, fallbackLoc)
                    launch(Dispatchers.Main) {
                        onStatusUpdate(smsSent, locationShared, audioRecording, bleAdvertising, physicalSignaling, latitude, longitude)
                    }
                }
            }
        } catch (_: SecurityException) {
            val fallbackLoc = Location("fallback").apply {
                this.latitude = 28.6139
                this.longitude = 77.2090
            }
            startBLEAdvertising(fallbackLoc)
            bleAdvertising = true
            scope.launch {
                val contacts = contactDao.getAllContacts().firstOrNull() ?: emptyList()
                if (contacts.isNotEmpty()) {
                    smsSent = sendEmergencySMS(contacts, fallbackLoc)
                }
                launch(Dispatchers.Main) {
                    onStatusUpdate(smsSent, false, audioRecording, bleAdvertising, physicalSignaling, fallbackLoc.latitude, fallbackLoc.longitude)
                }
            }
        }

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
     */
    private fun startBLEAdvertising(location: Location?) {
        try {
            val intent = Intent(context, NearbySOSService::class.java).apply {
                action = NearbySOSService.ACTION_START_ADVERTISING
                putExtra(NearbySOSService.EXTRA_LATITUDE, location?.latitude ?: 0.0)
                putExtra(NearbySOSService.EXTRA_LONGITUDE, location?.longitude ?: 0.0)
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

    private fun sendEmergencySMS(contacts: List<EmergencyContactEntity>, location: Location): Boolean {
        if (contacts.isEmpty()) {
            android.util.Log.w("SOSManager", "No emergency contacts to send SMS to")
            return false
        }

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            android.util.Log.w("SOSManager", "SEND_SMS permission not granted")
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
            val message = if (!contextMessage.isNullOrBlank()) {
                "🚨 EMERGENCY SOS!\n$contextMessage\n📍 Location: $mapsLink"
            } else {
                "🚨 EMERGENCY SOS!\nI need immediate help.\n📍 Location: $mapsLink"
            }

            var sentCount = 0
            contacts.forEach { contact ->
                try {
                    val cleanPhone = "${contact.countryCode}${contact.phone}"
                        .replace(" ", "")
                        .replace("-", "")
                        .replace("(", "")
                        .replace(")", "")
                    android.util.Log.d("SOSManager", "Sending SMS to: $cleanPhone (${contact.name})")
                    
                    val parts = smsManager.divideMessage(message)
                    if (parts.size > 1) {
                        smsManager.sendMultipartTextMessage(cleanPhone, null, parts, null, null)
                    } else {
                        smsManager.sendTextMessage(cleanPhone, null, message, null, null)
                    }
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
            }.apply {
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

    private fun stopAudioRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createSOSAlert() {
        // Broadcast local intent or trigger analytics if needed
    }

    fun cancelSOS() {
        if (!isActive) return
        isActive = false
        contextMessage = null
        stopAudioRecording()
        stopBLEAdvertising()
    }
}
