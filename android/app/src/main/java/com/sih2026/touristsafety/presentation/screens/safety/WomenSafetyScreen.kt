package com.sih2026.touristsafety.presentation.screens.safety

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sih2026.touristsafety.services.ThreatLevel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WomenSafetyScreen(
    onNavigateBack: () -> Unit,
    viewModel: WomenSafetyViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isEnabled by viewModel.isEnabled.collectAsState()
    val threatLevel by viewModel.currentThreatLevel.collectAsState()
    val detectionLog by viewModel.detectionLog.collectAsState()
    val sensitivity by viewModel.sensitivity.collectAsState()
    val activeSpeakerGender by viewModel.activeSpeakerGender.collectAsState()

    val threatColor = when (threatLevel) {
        ThreatLevel.LOW -> Color(0xFF388E3C)
        ThreatLevel.MEDIUM -> Color(0xFFFBC02D)
        ThreatLevel.HIGH -> Color(0xFFF57C00)
        ThreatLevel.CRITICAL -> Color(0xFFD32F2F)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Safety Monitor") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        var showSavedPopup by remember { mutableStateOf(false) }
        var previousIsEnabled by remember { mutableStateOf(isEnabled) }

        LaunchedEffect(isEnabled) {
            if (previousIsEnabled && !isEnabled) {
                showSavedPopup = true
            }
            previousIsEnabled = isEnabled
        }

        if (showSavedPopup) {
            AlertDialog(
                onDismissRequest = { showSavedPopup = false },
                title = { Text("Recording Saved") },
                text = { Text("The audio recording has been safely stored locally on your device.") },
                confirmButton = {
                    TextButton(onClick = { showSavedPopup = false }) {
                        Text("OK")
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enable Safety Mode", style = MaterialTheme.typography.titleLarge)
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { viewModel.toggleMonitoring(context) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = threatColor.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Status", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Audio Monitoring: ${if (isEnabled) "Active" else "Inactive"}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Threat Level: ${threatLevel.name}", color = threatColor, fontWeight = FontWeight.Bold)
                    
                    if (isEnabled && activeSpeakerGender != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Active Speaker: ${activeSpeakerGender!!.replaceFirstChar { it.uppercase() }}",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    if (isEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))
                        WaveformAnimation(color = threatColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Settings", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Sensitivity")
            Slider(
                value = sensitivity,
                onValueChange = viewModel::updateSensitivity
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = viewModel::testDetection,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Test Detection System")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Detection Log", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            if (detectionLog.isEmpty()) {
                Text("No detections yet.")
            } else {
                detectionLog.take(5).forEach { log ->
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.padding(12.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                val date = Date(log.timestamp)
                                val format = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
                                Text(log.type, fontWeight = FontWeight.Bold)
                                Text("${format.format(date)} - ${log.result}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Emergency Instructions", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("If the system detects a scream or distress signal and the threat level reaches CRITICAL, it will automatically send an SOS to your emergency contacts with your location.")
                }
            }
        }
    }
}

@Composable
fun WaveformAnimation(color: Color) {
    val infiniteTransition = rememberInfiniteTransition()
    val scales = List(5) { i ->
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(500, delayMillis = i * 100),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        scales.forEach { scale ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight(scale.value)
                    .background(color)
            )
        }
    }
}
