package com.sih2026.touristsafety.services

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.media.MediaRecorder
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
    private val contactDao: EmergencyContactDao
) {
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private var mediaRecorder: MediaRecorder? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private var isActive = false

    @SuppressLint("MissingPermission")
    fun activateSOS(onStatusUpdate: (Boolean, Boolean, Boolean, Double?, Double?) -> Unit) {
        if (isActive) return
        isActive = true

        var smsSent = false
        var locationShared = false
        var audioRecording = false
        var latitude: Double? = null
        var longitude: Double? = null

        // 1. Start Audio Recording
        audioRecording = startAudioRecording()
        onStatusUpdate(smsSent, locationShared, audioRecording, latitude, longitude)

        // 2. Get Location & Send SMS
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
                        val contacts = contactDao.getContactsForUser("default_user").firstOrNull() ?: emptyList()
                        smsSent = sendEmergencySMS(contacts, location)
                        launch(Dispatchers.Main) {
                            onStatusUpdate(smsSent, locationShared, audioRecording, latitude, longitude)
                        }
                    }
                }
                fusedLocationClient.removeLocationUpdates(this)
            }
        }, Looper.getMainLooper())
        
        createSOSAlert()
    }

    private fun sendEmergencySMS(contacts: List<EmergencyContactEntity>, location: Location): Boolean {
        if (contacts.isEmpty()) return false
        return try {
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            
            val mapsLink = "https://maps.google.com/?q=${location.latitude},${location.longitude}"
            val message = "SOS! I need help. My current location is: $mapsLink"
            
            contacts.forEach { contact ->
                val phone = "${contact.countryCode}${contact.phone}"
                smsManager.sendTextMessage(phone, null, message, null, null)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
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
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
