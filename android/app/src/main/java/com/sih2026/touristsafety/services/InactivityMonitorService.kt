package com.sih2026.touristsafety.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.sih2026.touristsafety.presentation.screens.checkin.CheckInPromptActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class InactivityMonitorService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    
    private var lastLocation: Location? = null
    private var stationaryStartTime: Long = 0
    private var directionChanges = 0
    
    companion object {
        const val CHANNEL_ID = "InactivityMonitorChannel"
        const val NOTIFICATION_ID = 101
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        startMonitoring()
        return START_STICKY
    }

    private fun startMonitoring() {
        serviceScope.launch {
            while (true) {
                // Mocking GPS check every 30 seconds
                delay(30_000)
                analyzeLocationData()
            }
        }
    }

    private fun analyzeLocationData() {
        var score = 0
        
        // Mock checks
        val isStationary = isStationary()
        val isInactive = isInactive()
        val hasUnusualMovement = hasUnusualMovement()
        val hasSpeedAnomaly = hasSpeedAnomaly()
        val isNightTime = isNightTime()
        val inDangerZone = inDangerZone()
        
        if (isStationary && isInactive) score += 40
        if (hasUnusualMovement) score += 30
        if (hasSpeedAnomaly) score += 50
        if (isNightTime) score += 20
        if (inDangerZone) score += 20

        if (score >= 80) {
            triggerSOS()
        } else if (score >= 60) {
            showCheckInPrompt()
        }
    }

    private fun isStationary(): Boolean {
        // Mock logic
        return Random.nextBoolean() && Random.nextBoolean() // 25% chance true
    }

    private fun isInactive(): Boolean {
        // Mock logic - screen is off / no interaction
        return Random.nextBoolean()
    }

    private fun hasUnusualMovement(): Boolean {
        // Mock zigzag
        return false
    }

    private fun hasSpeedAnomaly(): Boolean {
        // Mock > 80km/h
        return false
    }

    private fun isNightTime(): Boolean {
        // Mock 10pm to 6am
        return false
    }

    private fun inDangerZone(): Boolean {
        // Mock checking against GeofenceZoneEntities
        return false
    }

    private fun showCheckInPrompt() {
        val intent = Intent(this, CheckInPromptActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    private fun triggerSOS() {
        // Alert emergency contacts directly
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tourist Safety Active")
            .setContentText("Monitoring your safety \uD83D\uDEE1\uFE0F")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
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
        serviceJob.cancel()
    }
}
