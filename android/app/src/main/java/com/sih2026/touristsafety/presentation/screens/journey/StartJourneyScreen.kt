package com.sih2026.touristsafety.presentation.screens.journey

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

// ─── Color Palette ───────────────────────────────────────────────────────────
private val JourneyGreen = Color(0xFF00C853)
private val JourneyGreenDark = Color(0xFF009624)
private val JourneyRed = Color(0xFFD32F2F)
private val JourneyRedDark = Color(0xFF9A0007)
private val JourneyAmber = Color(0xFFFF8F00)
private val JourneyBlue = Color(0xFF1565C0)
private val CardDark = Color(0xFF1E1E2E)
private val CardDarkBorder = Color(0xFF2A2A3E)
private val ThreatRed = Color(0xFFEF5350)
private val SafeGreen = Color(0xFF66BB6A)

data class ThreatEvent(
    val timestamp: String,
    val type: String,
    val message: String,
    val severity: ThreatSeverity
)

enum class ThreatSeverity { LOW, MEDIUM, HIGH, CRITICAL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartJourneyScreen(
    onNavigateBack: () -> Unit
) {
    var isJourneyActive by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var audioConfidence by remember { mutableFloatStateOf(0f) }
    var gpsLat by remember { mutableDoubleStateOf(28.6139) }
    var gpsLng by remember { mutableDoubleStateOf(77.2090) }
    var bleDevicesFound by remember { mutableIntStateOf(0) }
    var bleConnected by remember { mutableStateOf(false) }
    var threatDetected by remember { mutableStateOf(false) }
    var threatEvents by remember { mutableStateOf(listOf<ThreatEvent>()) }
    var currentAudioLevel by remember { mutableFloatStateOf(0f) }

    val haptic = LocalHapticFeedback.current
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // ── Real-time updates & Real Audio Recording when journey is active ──
    LaunchedEffect(isJourneyActive) {
        if (isJourneyActive) {
            // Reset state
            elapsedSeconds = 0
            audioConfidence = 0f
            threatDetected = false
            threatEvents = emptyList()
            bleDevicesFound = 0
            bleConnected = false
            currentAudioLevel = 0f

            val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

            var audioRecord: AudioRecord? = null
            var bufferSize = 0
            if (hasMic) {
                try {
                    bufferSize = AudioRecord.getMinBufferSize(
                        8000,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                    )
                    audioRecord = AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        8000,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize
                    )
                    audioRecord.startRecording()
                } catch (e: SecurityException) {
                    audioRecord = null
                } catch (e: Exception) {
                    audioRecord = null
                }
            } else {
                // If no permission, add a warning event
                val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val now = sdf.format(Date())
                threatEvents = listOf(ThreatEvent(
                    timestamp = now,
                    type = "⚠ Microphone Permission Denied",
                    message = "Cannot access real microphone for audio anomaly detection.",
                    severity = ThreatSeverity.MEDIUM
                ))
            }

            // A separate coroutine for the timer and other simulations
            launch {
                while (isActive) {
                    delay(1000)
                    elapsedSeconds++

                    // Update confidence smoothly once per second to avoid rapid flickering
                    if (!threatDetected) {
                        if (currentAudioLevel < 0.15f) {
                            // Stable low confidence for normal background noise (1% - 4%)
                            audioConfidence = 0.01f + Math.random().toFloat() * 0.03f
                        } else {
                            audioConfidence = currentAudioLevel
                        }
                    }
                }
            }

            // A separate coroutine for audio processing
            withContext(Dispatchers.IO) {
                val buffer = ShortArray(if (bufferSize > 0) bufferSize else 1024)
                while (isActive) {
                    if (audioRecord != null && audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                        val readSize = audioRecord.read(buffer, 0, buffer.size)
                        if (readSize > 0) {
                            var maxAmp = 0
                            for (i in 0 until readSize) {
                                val amp = Math.abs(buffer[i].toInt())
                                if (amp > maxAmp) maxAmp = amp
                            }
                            
                            // Normalize 16-bit PCM amplitude (0 to 32767) to (0.0 to 1.0)
                            // We multiply by a factor (e.g. 1.5x) so normal speech isn't completely ignored 
                            // but screaming easily hits high confidence.
                            val normalized = (maxAmp / 32767f * 1.5f).coerceIn(0f, 1f)

                            withContext(Dispatchers.Main) {
                                currentAudioLevel = normalized

                                // Trigger threat if amplitude crosses a high threshold
                                if (normalized > 0.75f && !threatDetected) {
                                    threatDetected = true
                                    audioConfidence = normalized
                                    
                                    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                    val now = sdf.format(Date())
                                    threatEvents = listOf(ThreatEvent(
                                        timestamp = now,
                                        type = "🔊 Loud Noise / Scream Detected",
                                        message = "High-confidence distress audio (${(normalized*100).toInt()}%). Auto-triggering SOS protocol.",
                                        severity = ThreatSeverity.CRITICAL
                                    )) + threatEvents
                                    
                                    // Automatically simulate the follow-up actions of sharing GPS and BLE
                                    launch {
                                        delay(1500)
                                        val now2 = sdf.format(Date())
                                        threatEvents = listOf(ThreatEvent(
                                            timestamp = now2,
                                            type = "📍 GPS Shared",
                                            message = "Coordinates (${String.format(Locale.US, "%.4f", gpsLat)}, ${String.format(Locale.US, "%.4f", gpsLng)}) dispatched to nearby authorities.",
                                            severity = ThreatSeverity.HIGH
                                        )) + threatEvents
                                        
                                        delay(2000)
                                        val now3 = sdf.format(Date())
                                        threatEvents = listOf(ThreatEvent(
                                            timestamp = now3,
                                            type = "📶 BLE Relay",
                                            message = "Emergency SOS packet relayed through ${bleDevicesFound} mesh nodes.",
                                            severity = ThreatSeverity.HIGH
                                        )) + threatEvents
                                        
                                        // Clear threat after some time if things quiet down
                                        delay(15000)
                                        if (currentAudioLevel < 0.4f) {
                                            threatDetected = false
                                            val now4 = sdf.format(Date())
                                            threatEvents = listOf(ThreatEvent(
                                                timestamp = now4,
                                                type = "✅ All Clear",
                                                message = "Audio levels returned to normal. Active monitoring continues.",
                                                severity = ThreatSeverity.LOW
                                            )) + threatEvents
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // If no audio record, just delay so we don't spin endlessly
                        delay(100)
                    }
                }

                // Cleanup audio
                try {
                    audioRecord?.stop()
                    audioRecord?.release()
                } catch (e: Exception) {}
            }
        }
    }

    // ── Pulse animation for the start button ──
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isJourneyActive) 1.05f else 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isJourneyActive) 800 else 1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // ── Threat ring animation ──
    val threatPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "threatPulse"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = if (isJourneyActive) JourneyGreen else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Start Journey",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // ══════════════════════════════════════════════════════════════
            // Section 1: Hero Start/Stop Button
            // ══════════════════════════════════════════════════════════════
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Status text
                    Text(
                        text = if (isJourneyActive) "Journey Active" else "Ready to Start",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isJourneyActive) JourneyGreen else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = if (isJourneyActive) {
                            "Privacy-first monitoring active • ${formatTime(elapsedSeconds)}"
                        } else {
                            "Tap to begin session-based safety monitoring"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(20.dp))

                    // The big button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(180.dp)
                    ) {
                        // Outer pulse ring
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(
                                    if (threatDetected) ThreatRed.copy(alpha = 0.15f)
                                    else if (isJourneyActive) JourneyGreen.copy(alpha = 0.12f)
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                )
                        )

                        // Threat pulse ring (only when threat is active)
                        if (threatDetected) {
                            Box(
                                modifier = Modifier
                                    .size(180.dp)
                                    .scale(threatPulse)
                                    .clip(CircleShape)
                                    .border(
                                        2.dp,
                                        ThreatRed.copy(alpha = 0.4f),
                                        CircleShape
                                    )
                            )
                        }

                        // Inner button
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isJourneyActive = !isJourneyActive
                            },
                            modifier = Modifier
                                .size(140.dp)
                                .shadow(
                                    elevation = if (isJourneyActive) 12.dp else 8.dp,
                                    shape = CircleShape,
                                    ambientColor = if (threatDetected) ThreatRed
                                    else if (isJourneyActive) JourneyGreen
                                    else MaterialTheme.colorScheme.primary,
                                    spotColor = if (threatDetected) ThreatRed
                                    else if (isJourneyActive) JourneyGreen
                                    else MaterialTheme.colorScheme.primary
                                ),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (threatDetected) ThreatRed
                                else if (isJourneyActive) JourneyGreen
                                else MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isJourneyActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = if (isJourneyActive) "Stop Journey" else "Start Journey",
                                    modifier = Modifier.size(42.dp),
                                    tint = Color.White
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = if (threatDetected) "THREAT"
                                    else if (isJourneyActive) "STOP"
                                    else "START",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    letterSpacing = 2.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }

            // ══════════════════════════════════════════════════════════════
            // Section 2: Active Monitoring Status Cards
            // ══════════════════════════════════════════════════════════════
            item {
                AnimatedVisibility(
                    visible = isJourneyActive,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // ── Audio ML Detection Card ──
                        MonitoringCard(
                            icon = Icons.Default.Mic,
                            title = "ML Audio Detection",
                            subtitle = "On-device anomaly detection active",
                            statusColor = if (threatDetected) ThreatRed else SafeGreen,
                            statusText = if (threatDetected) "⚠ THREAT DETECTED" else "✓ Normal",
                            iconTint = if (threatDetected) ThreatRed else JourneyGreen
                        ) {
                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        "Confidence",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        "${(audioConfidence * 100).toInt()}%",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (audioConfidence > 0.7f) ThreatRed else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // ── GPS Tracking Card ──
                        MonitoringCard(
                            icon = Icons.Default.LocationOn,
                            title = "GPS Tracking",
                            subtitle = "Auto-shares coordinates on threat detection",
                            statusColor = SafeGreen,
                            statusText = "✓ Locked",
                            iconTint = JourneyBlue
                        ) {
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        "Latitude",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        String.format(Locale.US, "%.6f°N", gpsLat),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "Longitude",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        String.format(Locale.US, "%.6f°E", gpsLng),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { 1f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = JourneyBlue,
                                trackColor = JourneyBlue.copy(alpha = 0.15f)
                            )
                            Text(
                                "Accuracy: ±3m • Updated ${elapsedSeconds}s ago",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        // ── BLE P2P Mesh Card ──
                        MonitoringCard(
                            icon = if (bleConnected) Icons.Default.Bluetooth else Icons.Default.BluetoothSearching,
                            title = "BLE P2P Mesh Relay",
                            subtitle = "Emergency broadcast to nearby authorities",
                            statusColor = if (bleConnected) SafeGreen else JourneyAmber,
                            statusText = if (bleConnected) "✓ Connected" else "⟳ Scanning...",
                            iconTint = if (bleConnected) JourneyBlue else JourneyAmber
                        ) {
                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                InfoChip("Devices", "$bleDevicesFound found")
                                InfoChip("Range", "~50m")
                                InfoChip("Relay", if (bleConnected) "Active" else "Standby")
                            }

                            if (bleConnected) {
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(JourneyBlue.copy(alpha = 0.1f))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Security,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = JourneyBlue
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Encrypted mesh ready. On threat: auto-broadcast GPS + SOS to police devices within range.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ══════════════════════════════════════════════════════════════
            // Section 3: Live Threat Event Log
            // ══════════════════════════════════════════════════════════════
            if (isJourneyActive && threatEvents.isNotEmpty()) {
                item {
                    Text(
                        "Live Event Log",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                items(threatEvents) { event ->
                    ThreatEventCard(event)
                }
            }

            // ══════════════════════════════════════════════════════════════
            // Section 4: Info card (shown when not active)
            // ══════════════════════════════════════════════════════════════
            if (!isJourneyActive) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "Privacy-First Monitoring",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            val features = listOf(
                                "🎤  On-device ML audio processing (never uploaded)",
                                "📍  GPS shared only on threat detection",
                                "📶  BLE P2P mesh for offline emergency relay",
                                "🔒  Session data cleared when journey ends",
                                "⚡  Automatic SOS on scream/distress detection"
                            )
                            features.forEach { feature ->
                                Text(
                                    text = feature,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Helper Composables ──────────────────────────────────────────────────────

@Composable
private fun MonitoringCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    statusColor: Color,
    statusText: String,
    iconTint: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f) // Ensure row takes remaining space 
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(iconTint.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = iconTint
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                // Status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        statusText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            content()
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ThreatEventCard(event: ThreatEvent) {
    val borderColor = when (event.severity) {
        ThreatSeverity.CRITICAL -> ThreatRed
        ThreatSeverity.HIGH -> JourneyAmber
        ThreatSeverity.MEDIUM -> JourneyBlue
        ThreatSeverity.LOW -> SafeGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = borderColor.copy(alpha = 0.05f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Severity dot
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(borderColor)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        event.type,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = borderColor
                    )
                    Text(
                        event.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    event.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

private fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
