package com.sih2026.touristsafety.services

import android.app.Service
import android.content.Intent
import android.os.IBinder

class LocationTrackingService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}
