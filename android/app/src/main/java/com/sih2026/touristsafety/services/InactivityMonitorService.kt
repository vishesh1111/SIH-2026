package com.sih2026.touristsafety.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.sih2026.touristsafety.R
import com.sih2026.touristsafety.data.local.InactivityPreferences
import com.sih2026.touristsafety.data.local.dao.GeofenceZoneDao
import com.sih2026.touristsafety.presentation.screens.checkin.CheckInPromptActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.sqrt

@AndroidEntryPoint
class InactivityMonitorService : Service() {

    @Inject lateinit var sosManager: SOSManager
    @Inject lateinit var geofenceZoneDao: GeofenceZoneDao
    @Inject lateinit var inactivityPreferences: InactivityPreferences

    companion object {
        const val CHANNEL_ID = "InactivityMonitorChannel"
        const val NOTIFICATION_ID = 101
        private const val TAG = "InactivityMonitor"
    }

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationHistory = ArrayDeque<Location>(20)

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private val accelerometerReadings = mutableListOf<Float>()

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                val magnitude = sqrt(it.values[0].pow(2) + it.values[1].pow(2) + it.values[2].pow(2))
                synchronized(accelerometerReadings) {
                    accelerometerReadings.add(magnitude)
                    if (accelerometerReadings.size > 300) accelerometerReadings.removeAt(0)
                }
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                synchronized(locationHistory) {
                    locationHistory.addLast(location)
                    if (locationHistory.size > 20) locationHistory.removeFirst()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.let {
            sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        startLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 30_000L)
            .setMinUpdateIntervalMillis(15_000L)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request location updates", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        startMonitoring()
        return START_STICKY
    }

    private fun startMonitoring() {
        serviceScope.launch {
            while (true) {
                delay(30_000) // analyze every 30 seconds

                // Read preferences
                val enabled = inactivityPreferences.isEnabled.first()
                if (!enabled) continue

                analyzeLocationData()
            }
        }
    }

    private suspend fun analyzeLocationData() {
        var score = 0

        val stationaryEnabled = inactivityPreferences.stationaryDetection.first()
        val unusualEnabled = inactivityPreferences.unusualMovement.first()
        val speedEnabled = inactivityPreferences.speedAnomaly.first()

        val stationary = isStationary()
        val inactive = isInactive()
        val unusual = if (unusualEnabled) hasUnusualMovement() else false
        val speedAnomaly = if (speedEnabled) hasSpeedAnomaly() else false
        val nightTime = isNightTime()
        val dangerZone = inDangerZone()

        if (stationaryEnabled && stationary && inactive) score += 40
        if (unusual) score += 30
        if (speedAnomaly) score += 50
        if (nightTime) score += 20
        if (dangerZone) score += 20

        Log.d(TAG, "Score=$score stationary=$stationary inactive=$inactive unusual=$unusual speed=$speedAnomaly night=$nightTime danger=$dangerZone")

        if (score >= 80) {
            triggerSOS()
        } else if (score >= 60) {
            showCheckInPrompt()
        }
    }

    private fun isStationary(): Boolean {
        val history = synchronized(locationHistory) { locationHistory.toList() }
        if (history.size < 5) return false
        val recent = history.takeLast(5)
        val anchor = recent.first()
        return recent.all { anchor.distanceTo(it) < 15f } // all within 15 meters
    }

    private fun isInactive(): Boolean {
        val readings = synchronized(accelerometerReadings) { accelerometerReadings.toList() }
        if (readings.size < 60) return false // need at least 1 minute of data
        val recent = readings.takeLast(60)
        val mean = recent.average()
        val variance = recent.map { (it - mean) * (it - mean) }.average()
        return variance < 0.05 // very low variance = phone not moving
    }

    private fun hasUnusualMovement(): Boolean {
        val history = synchronized(locationHistory) { locationHistory.toList() }
        if (history.size < 10) return false
        val recent = history.takeLast(10)
        var sharpTurns = 0
        for (i in 1 until recent.size - 1) {
            val bearing1 = recent[i - 1].bearingTo(recent[i])
            val bearing2 = recent[i].bearingTo(recent[i + 1])
            val diff = Math.abs(bearing2 - bearing1)
            val normalizedDiff = if (diff > 180) 360 - diff else diff
            if (normalizedDiff > 90) sharpTurns++
        }
        return sharpTurns >= 4 // 4+ sharp turns in 10 readings = zigzag
    }

    private fun hasSpeedAnomaly(): Boolean {
        val history = synchronized(locationHistory) { locationHistory.toList() }
        if (history.size < 2) return false
        val recent = history.takeLast(5)
        return recent.any { it.hasSpeed() && it.speed > 22.2f } // > 80 km/h
    }

    private fun isNightTime(): Boolean {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return hour >= 22 || hour < 6
    }

    private suspend fun inDangerZone(): Boolean {
        val currentLocation = synchronized(locationHistory) { locationHistory.lastOrNull() } ?: return false
        val zones = geofenceZoneDao.getActiveZones().first()
        return zones.any { zone ->
            val results = FloatArray(1)
            Location.distanceBetween(
                currentLocation.latitude, currentLocation.longitude,
                zone.latitude, zone.longitude, results
            )
            results[0] < zone.radius
        }
    }

    private fun triggerSOS() {
        sosManager.activateSOS { _, _, _, _, _ -> }
    }

    private fun showCheckInPrompt() {
        val intent = Intent(this, CheckInPromptActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tourist Safety Active")
            .setContentText("Monitoring your safety 🛡️")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Inactivity Monitor",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        try {
            sensorManager.unregisterListener(sensorListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering sensor listener", e)
        }
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Error removing location updates", e)
        }
        serviceScope.cancel()
    }
}
