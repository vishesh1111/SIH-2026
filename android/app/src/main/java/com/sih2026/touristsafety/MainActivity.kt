package com.sih2026.touristsafety

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sih2026.touristsafety.presentation.navigation.TouristSafetyNavHost
import com.sih2026.touristsafety.presentation.theme.TouristSafetyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        setContent {
            TouristSafetyTheme {
                TouristSafetyNavHost()
            }
        }
    }
}
