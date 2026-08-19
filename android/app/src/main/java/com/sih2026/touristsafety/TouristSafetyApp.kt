package com.sih2026.touristsafety

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration

@HiltAndroidApp
class TouristSafetyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize OSMDroid configuration early so map tiles load properly
        Configuration.getInstance().apply {
            userAgentValue = packageName
            load(this@TouristSafetyApp, getSharedPreferences("osmdroid", MODE_PRIVATE))
        }
    }
}
