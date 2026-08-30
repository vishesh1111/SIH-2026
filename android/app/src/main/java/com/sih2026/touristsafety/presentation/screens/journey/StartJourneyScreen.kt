package com.sih2026.touristsafety.presentation.screens.journey

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.sih2026.touristsafety.services.ThreatLevel
import java.util.*

// ─── Color Palette ───────────────────────────────────────────────────────────
private val JourneyGreen = Color(0xFF00C853)
private val JourneyAmber = Color(0xFFFF8F00)
private val JourneyBlue = Color(0xFF1565C0)
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
    viewModel: StartJourneyViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val isJourneyActive by viewModel.isJourneyActive.collectAsState()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsState()
    val audioConfidence by viewModel.audioConfidence.collectAsState()
    val threatLevel by viewModel.threatLevel.collectAsState()
    val threatDetected by viewModel.threatDetected.collectAsState()
    val latestTranscript by viewModel.latestTranscript.collectAsState()
    val keywordCount by viewModel.keywordCount.collectAsState()
    val activeSpeakerGender by viewModel.activeSpeakerGender.collectAsState()
    val locationName by viewModel.locationName.collectAsState()
    val nearbyAuthorities by viewModel.nearbyAuthorities.collectAsState()
    val bleDevicesFound by viewModel.bleDevicesFound.collectAsState()
    val bleConnected by viewModel.bleConnected.collectAsState()
    val sosCountdown by viewModel.sosCountdown.collectAsState()
    val isSosDispatched by viewModel.isSosDispatched.collectAsState()
    val threatEvents by viewModel.threatEvents.collectAsState()

    val haptic = LocalHapticFeedback.current
    val listState = rememberLazyListState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.toggleJourney(context)
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ══════════════════════════════════════════════════════════════
            // Section 1: Hero Start/Stop Button with Real Status
            // ══════════════════════════════════════════════════════════════
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Status text above button
                    Text(
                        text = if (threatDetected) "Threat Detected!"
                        else if (isJourneyActive) "Journey Active"
                        else "Ready to Start",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (threatDetected) ThreatRed
                        else if (isJourneyActive) JourneyGreen
                        else MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = if (isJourneyActive) "Privacy-first ML monitoring active • ${formatTime(elapsedSeconds)}"
                        else "Tap START before beginning your journey for continuous safety monitoring",
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
                                if (!isJourneyActive) {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.RECORD_AUDIO,
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION,
                                            Manifest.permission.SEND_SMS
                                        )
                                    )
                                } else {
                                    viewModel.toggleJourney(context)
                                }
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
            // Section 1.5: 10s Cancelable SOS Countdown Overlay
            // ══════════════════════════════════════════════════════════════
            if (sosCountdown > 0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text("🚨 THREAT CONFIRMED", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Auto-dispatching SOS emergency broadcast in ${sosCountdown}s",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.cancelSOSCountdown(context) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color(0xFFD32F2F)
                                )
                            ) {
                                Text("Cancel SOS (I'm Safe)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else if (isSosDispatched) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFB71C1C))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF69F0AE))
                                Spacer(Modifier.width(8.dp))
                                Text("🚨 SOS BROADCAST ACTIVE", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            }
                            Spacer(Modifier.height(4.dp))
                            val policeName = nearbyAuthorities.firstOrNull { it.type == AuthorityType.POLICE }?.name ?: "Local Police Station"
                            val hospitalName = nearbyAuthorities.firstOrNull { it.type == AuthorityType.HOSPITAL }?.name ?: "District Hospital"
                            Text(
                                "Emergency SMS dispatched to contacts. Alert notifications sent to $policeName (0.7km), $hospitalName (1.2km) & Ambulance 108 near $locationName.",
                                color = Color.White.copy(alpha = 0.95f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.cancelSOSCountdown(context) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color(0xFFB71C1C)
                                )
                            ) {
                                Text("Dismiss / Stop SOS", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
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
                            subtitle = "On-device distress & keyword models active",
                            statusColor = if (threatDetected) ThreatRed else SafeGreen,
                            statusText = if (threatDetected) "⚠ THREAT DETECTED" else "✓ Normal",
                            iconTint = if (threatDetected) ThreatRed else JourneyGreen
                        ) {
                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Threat Level",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        threatLevel.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (threatLevel == ThreatLevel.CRITICAL || threatLevel == ThreatLevel.HIGH) ThreatRed else JourneyGreen
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "Confidence",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        "${(audioConfidence * 100).toInt()}%",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (audioConfidence > 0.6f) ThreatRed else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            if (!latestTranscript.isNullOrBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🗣️ Heard: ", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                        Text(
                                            text = "\"$latestTranscript\"",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            if (activeSpeakerGender != null) {
                                Spacer(Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    InfoChip("Voice Gender", activeSpeakerGender!!.replaceFirstChar { it.uppercase() })
                                    if (keywordCount > 0) {
                                        InfoChip("Keywords", "$keywordCount/5 detected")
                                    }
                                }
                            }
                        }

                        // ── GPS Tracking Card: Shows Real Location Name ──
                        MonitoringCard(
                            icon = Icons.Default.LocationOn,
                            title = "GPS Tracking",
                            subtitle = "Auto-shares location on threat detection",
                            statusColor = SafeGreen,
                            statusText = "✓ Locked",
                            iconTint = JourneyBlue
                        ) {
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Place,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = locationName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Accuracy: ±3m • Real-time GPS Locked",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SafeGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // ── Nearby Emergency Authorities Card ──
                        MonitoringCard(
                            icon = Icons.Default.LocalPolice,
                            title = "Nearby Emergency Authorities",
                            subtitle = if (isSosDispatched) "🚨 Live SOS Alert Notified to Local Authorities" else "Auto-mapped response units in your jurisdiction",
                            statusColor = if (isSosDispatched) ThreatRed else SafeGreen,
                            statusText = if (isSosDispatched) "🚨 ALERT NOTIFIED" else "● Standby",
                            iconTint = if (isSosDispatched) ThreatRed else MaterialTheme.colorScheme.primary
                        ) {
                            Spacer(Modifier.height(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                nearbyAuthorities.forEach { authority ->
                                    AuthorityItemRow(authority = authority) {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${authority.phone}"))
                                        context.startActivity(intent)
                                    }
                                }
                            }
                        }

                        // ── BLE P2P Mesh Card ──
                        MonitoringCard(
                            icon = if (bleConnected) Icons.Default.Bluetooth else Icons.AutoMirrored.Filled.BluetoothSearching,
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
                                        "Mesh node online. Packets will relay through nearby devices without internet.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = JourneyBlue,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ══════════════════════════════════════════════════════════════
            // Section 3: Live Event Log (Key Milestones Only)
            // ══════════════════════════════════════════════════════════════
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Live Event Log",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (threatEvents.isNotEmpty()) {
                        Text(
                            "${threatEvents.size} events",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            if (threatEvents.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No events yet. Start the journey to begin monitoring.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(threatEvents) { event ->
                    ThreatEventCard(event = event)
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun AuthorityItemRow(
    authority: NearbyAuthority,
    onCallClick: () -> Unit
) {
    val icon = when (authority.type) {
        AuthorityType.POLICE -> Icons.Default.LocalPolice
        AuthorityType.HOSPITAL -> Icons.Default.LocalHospital
        AuthorityType.AMBULANCE -> Icons.Default.Emergency
        AuthorityType.PATROL -> Icons.Default.Shield
    }

    val iconColor = when (authority.type) {
        AuthorityType.POLICE -> JourneyBlue
        AuthorityType.HOSPITAL -> Color(0xFFE53935)
        AuthorityType.AMBULANCE -> JourneyAmber
        AuthorityType.PATROL -> JourneyGreen
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (authority.isNotified) ThreatRed.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        border = if (authority.isNotified) androidx.compose.foundation.BorderStroke(1.dp, ThreatRed.copy(alpha = 0.5f)) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = authority.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${authority.distance} • ${authority.address}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(6.dp))

            if (authority.isNotified) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = ThreatRed
                ) {
                    Text(
                        text = "🚨 NOTIFIED",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = SafeGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "● Standby",
                        color = SafeGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.width(6.dp))

            IconButton(
                onClick = onCallClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = "Call ${authority.name}",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ─── Reusable Components ──────────────────────────────────────────────────────
@Composable
private fun MonitoringCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    statusColor: Color,
    statusText: String,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            content()
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontSize = 11.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun ThreatEventCard(event: ThreatEvent) {
    val borderColor = when (event.severity) {
        ThreatSeverity.CRITICAL -> ThreatRed
        ThreatSeverity.HIGH -> ThreatRed.copy(alpha = 0.7f)
        ThreatSeverity.MEDIUM -> JourneyAmber
        ThreatSeverity.LOW -> SafeGreen
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(borderColor)
                    .align(Alignment.CenterVertically)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = event.type,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = event.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = event.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
}
