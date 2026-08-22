package com.sih2026.touristsafety.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.sih2026.touristsafety.MainActivity
import com.sih2026.touristsafety.R
import com.sih2026.touristsafety.data.local.entities.ReceivedSOSAlertEntity
import com.sih2026.touristsafety.data.model.SOSPayload
import com.sih2026.touristsafety.services.ble.BleSOSAdvertiser
import com.sih2026.touristsafety.services.ble.BleSOSScanner
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NearbySOSService : Service() {

    @Inject lateinit var advertiser: BleSOSAdvertiser
    @Inject lateinit var scanner: BleSOSScanner

    companion object {
        const val ACTION_START_ADVERTISING = "ACTION_START_ADVERTISING"
        const val ACTION_STOP_ADVERTISING = "ACTION_STOP_ADVERTISING"
        const val ACTION_START_SCANNING = "ACTION_START_SCANNING"
        const val ACTION_STOP_SCANNING = "ACTION_STOP_SCANNING"
        const val EXTRA_LATITUDE = "extra_latitude"
        const val EXTRA_LONGITUDE = "extra_longitude"
        const val EXTRA_USER_ID = "extra_user_id"
        const val EXTRA_SOS_TYPE = "extra_sos_type"
        const val CHANNEL_ID = "nearby_sos_channel"
        const val NOTIFICATION_ID = 201
        const val ALERT_CHANNEL_ID = "sos_alert_channel"
        const val ALERT_NOTIFICATION_ID = 202
    }

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID, 
                    createForegroundNotification("Safety Mesh Active", "Protecting nearby tourists 🛡️"),
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                )
            } else {
                startForeground(NOTIFICATION_ID, createForegroundNotification("Safety Mesh Active", "Protecting nearby tourists 🛡️"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        startScanning()
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            when (action) {
                ACTION_START_ADVERTISING -> {
                    val lat = intent.getDoubleExtra(EXTRA_LATITUDE, 0.0)
                    val lon = intent.getDoubleExtra(EXTRA_LONGITUDE, 0.0)
                    val userId = intent.getStringExtra(EXTRA_USER_ID) ?: ""
                    val sosType = intent.getIntExtra(EXTRA_SOS_TYPE, 0).toByte()
                    val payload = SOSPayload(userId = userId, latitude = lat, longitude = lon, timestamp = System.currentTimeMillis(), sosType = sosType)
                    advertiser.startAdvertising(payload)
                    
                    try {
                        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.notify(NOTIFICATION_ID, createForegroundNotification("SOS Beacon Broadcasting...", "Sending help requests to nearby devices."))
                    } catch (e: Exception) {}
                }
                ACTION_STOP_ADVERTISING -> {
                    advertiser.stopAdvertising()
                    try {
                        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.notify(NOTIFICATION_ID, createForegroundNotification("Safety Mesh Active", "Protecting nearby tourists 🛡️"))
                    } catch (e: Exception) {}
                }
                ACTION_START_SCANNING -> {
                    startScanning()
                }
                ACTION_STOP_SCANNING -> {
                    scanner.stopScanning()
                }
            }
        }
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startScanning() {
        scanner.startScanning()
        serviceScope.launch {
            scanner.scannedAlerts.collect { alert ->
                showSOSAlertNotification(alert)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        advertiser.stopAdvertising()
        scanner.stopScanning()
        serviceScope.cancel()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Nearby SOS Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val alertChannel = NotificationChannel(
                ALERT_CHANNEL_ID,
                "SOS Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
            manager.createNotificationChannel(alertChannel)
        }
    }

    private fun createForegroundNotification(title: String, text: String): Notification {
        // MainActivity or default activity intent
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Adjust to your actual app icon if different
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun showSOSAlertNotification(alert: ReceivedSOSAlertEntity) {
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, alert.id, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setContentTitle("⚠️ SOS Alert Nearby! Someone needs help.")
            .setContentText("Tap to view location.")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Adjust to your actual app icon if different
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(ALERT_NOTIFICATION_ID + alert.id, notification)
    }
}
