package com.sih2026.touristsafety.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.sih2026.touristsafety.data.local.dao.GeofenceZoneDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class GeofencingService : Service() {

    @Inject lateinit var geofenceManager: GeofenceManager
    @Inject lateinit var geofenceZoneDao: GeofenceZoneDao

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        
        serviceScope.launch {
            geofenceZoneDao.getActiveZones().collectLatest { zones ->
                geofenceManager.registerGeofences(zones)
            }
        }
        
        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "geofencing_service_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Geofencing Service", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Tourist Safety")
            .setContentText("Monitoring your area")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        geofenceManager.unregisterAll()
    }
}
