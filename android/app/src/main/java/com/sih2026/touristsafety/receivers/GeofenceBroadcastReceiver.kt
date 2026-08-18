package com.sih2026.touristsafety.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.sih2026.touristsafety.R

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null || geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent?.errorCode ?: -1)
            Log.e("GeofenceReceiver", "Error: $errorMessage")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        val triggeringGeofences = geofencingEvent.triggeringGeofences

        if (triggeringGeofences.isNullOrEmpty()) return

        val zoneId = triggeringGeofences[0].requestId

        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                showNotification(context, "Entered Zone", "You have entered a monitored zone: $zoneId")
                // Handle danger/restricted zone logic and auto-trigger SOS if needed
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                showNotification(context, "Exited Zone", "You have exited zone: $zoneId")
            }
            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                showNotification(context, "Dwelling in Zone", "You are spending time in zone: $zoneId")
            }
        }
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "geofence_alerts"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Geofence Alerts", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
