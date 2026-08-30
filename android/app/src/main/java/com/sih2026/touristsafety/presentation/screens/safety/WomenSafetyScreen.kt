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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val latestEvent by viewModel.latestEvent.collectAsState()
    val sensitivity by viewModel.sensitivity.collectAsState()
    val activeSpeakerGender by viewModel.activeSpeakerGender.collectAsState()
    val keywordCount by viewModel.keywordCount.collectAsState()
    val latestTranscript by viewModel.latestTranscript.collectAsState()
    val sosCountdown by viewModel.sosCountdown.collectAsState()

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

            // SOS Countdown Overlay
            if (sosCountdown in 1..10) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F).copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "⚠️ SOS ACTIVATING IN",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "$sosCountdown",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "seconds",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.cancelSOSCountdown(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("CANCEL SOS", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Status Card
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
                        val currentAmplitude by viewModel.audioAmplitude.collectAsState()
                        WaveformAnimation(color = threatColor, amplitude = currentAmplitude)
                    }
                }
            }

            // Keyword Detection Card
            if (isEnabled) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (keywordCount >= 3) Color(0xFFF57C00).copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🎙️ Voice Keyword Detection", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Keyword counter with progress bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "\"help\" detected:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "$keywordCount / 5",
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    keywordCount >= 4 -> Color(0xFFD32F2F)
                                    keywordCount >= 3 -> Color(0xFFF57C00)
                                    keywordCount >= 1 -> Color(0xFFFBC02D)
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { (keywordCount / 5f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth(),
                            color = when {
                                keywordCount >= 4 -> Color(0xFFD32F2F)
                                keywordCount >= 3 -> Color(0xFFF57C00)
                                else -> MaterialTheme.colorScheme.primary
                            },
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Say \"help\", \"bachao\", or \"madad\" 5 times in 30 seconds to trigger SOS",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "🗣️ Heard: ",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                val displayText = if (!latestTranscript.isNullOrBlank()) {
                                    val words = latestTranscript!!.split("\\s+".toRegex()).filter { it.isNotBlank() }
                                    if (words.size > 7) "... " + words.takeLast(7).joinToString(" ") else latestTranscript!!
                                } else {
                                    "Listening for speech..."
                                }
                                Text(
                                    text = if (!latestTranscript.isNullOrBlank()) "\"$displayText\"" else displayText,
                                    fontSize = 12.sp,
                                    fontWeight = if (!latestTranscript.isNullOrBlank()) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (!latestTranscript.isNullOrBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
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
            Text("Latest Detection Log", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            val latestLog = latestEvent
            if (latestLog == null) {
                Text("No detections yet.")
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = when {
                                latestLog.type.contains("SOS ACTIVATED") -> Color(0xFFD32F2F)
                                latestLog.type.contains("SOS") -> Color(0xFFF57C00)
                                latestLog.type.contains("Keyword") -> Color(0xFF1976D2)
                                latestLog.type.contains("Scream") -> Color(0xFFD32F2F)
                                latestLog.type.contains("Distress") -> Color(0xFFF57C00)
                                latestLog.type.contains("Cancelled") -> Color(0xFF388E3C)
                                else -> Color.Gray
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            val date = Date(latestLog.timestamp)
                            val format = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
                            Text(latestLog.type, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("${format.format(date)} - ${latestLog.result}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            

        }
    }
}

@Composable
fun WaveformAnimation(color: Color, amplitude: Float) {
    // Scale the amplitude up by 5x so the pulses are highly visible and vertical
    val scaledAmplitude = (amplitude * 5f).coerceIn(0.1f, 1f)
    
    // Smooth out the incoming amplitude
    val animatedAmplitude by animateFloatAsState(
        targetValue = scaledAmplitude,
        animationSpec = tween(150),
        label = "AmplitudeAnimation"
    )

    // Apply pseudo-random heights based on the single amplitude
    val multipliers = listOf(0.6f, 1.0f, 0.8f, 0.5f, 0.9f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp), // Increased height for more vertical pulses
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        multipliers.forEach { mult ->
            Box(
                modifier = Modifier
                    .width(6.dp) // Slightly thicker
                    .fillMaxHeight(animatedAmplitude * mult)
                    .background(color)
            )
        }
    }
}
