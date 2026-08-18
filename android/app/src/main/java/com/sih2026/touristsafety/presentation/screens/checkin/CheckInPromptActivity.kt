package com.sih2026.touristsafety.presentation.screens.checkin

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sih2026.touristsafety.presentation.theme.TouristSafetyTheme
import kotlinx.coroutines.delay

class CheckInPromptActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Show over lock screen & turn on screen
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        // Max brightness
        val params = window.attributes
        params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        window.attributes = params

        setContent {
            TouristSafetyTheme {
                CheckInScreen(
                    onSafe = { finish() },
                    onNeedHelp = { 
                        // Trigger SOS
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun CheckInScreen(onSafe: () -> Unit, onNeedHelp: () -> Unit) {
    var timeLeft by remember { mutableIntStateOf(300) } // 5 minutes in seconds

    LaunchedEffect(key1 = timeLeft) {
        if (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        } else {
            // Auto trigger SOS when time is up
            onNeedHelp()
        }
    }

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Are you okay?",
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Unusual activity detected.",
                color = Color.LightGray,
                fontSize = 18.sp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = timeString,
                color = Color.Red,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Auto-alerting emergency contacts in...",
                color = Color.LightGray,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            Button(
                onClick = onSafe,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
            ) {
                Text("I'M SAFE", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onNeedHelp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("NEED HELP", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
