package com.sih2026.touristsafety.presentation.screens.sos

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.sih2026.touristsafety.presentation.components.SOSButton
import com.sih2026.touristsafety.presentation.theme.SosRed

@Composable
fun SOSScreen(
    viewModel: SOSViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.sosState.collectAsState()
    val context = LocalContext.current

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            viewModel.activateSOS()
        }
    }

    val onActivate = {
        val requiredPermissions = arrayOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.RECORD_AUDIO
        )
        val allGranted = requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) {
            viewModel.activateSOS()
        } else {
            permissionsLauncher.launch(requiredPermissions)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (state is SOSState.Active) SosRed else MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            if (state is SOSState.Idle || state is SOSState.Countdown) {
                Text(
                    text = "Emergency Assistance",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(32.dp))
                SOSButton(
                    onSOSTriggered = onActivate
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Long press for 3 seconds to activate",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            } else if (state is SOSState.Active) {
                val activeState = state as SOSState.Active
                
                Text(
                    text = "SOS ACTIVATED",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Activated at: ${activeState.timestamp}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Sending alerts to emergency contacts...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (activeState.latitude != null && activeState.longitude != null) {
                    Text(
                        text = "Lat: ${activeState.latitude}, Lon: ${activeState.longitude}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))

                StatusIndicator(label = "SMS Sent", isSuccess = activeState.smsSent)
                StatusIndicator(label = "Location Shared", isSuccess = activeState.locationShared)
                StatusIndicator(label = "Audio Recording", isSuccess = activeState.audioRecording)

                Spacer(modifier = Modifier.height(48.dp))
                
                Button(
                    onClick = { viewModel.cancelSOS() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = SosRed),
                    modifier = Modifier.fillMaxWidth(0.8f).height(56.dp)
                ) {
                    Text("Cancel SOS", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StatusIndicator(label: String, isSuccess: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.White, style = MaterialTheme.typography.bodyLarge)
        Icon(
            imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
            contentDescription = null,
            tint = Color.White
        )
    }
}
