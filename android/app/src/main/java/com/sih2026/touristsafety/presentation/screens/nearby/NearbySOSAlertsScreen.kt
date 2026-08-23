package com.sih2026.touristsafety.presentation.screens.nearby

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sih2026.touristsafety.data.local.entities.ReceivedSOSAlertEntity
import com.sih2026.touristsafety.presentation.theme.SosRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbySOSAlertsScreen(
    viewModel: NearbySOSAlertsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val alerts by viewModel.alerts.collectAsState()
    val context = LocalContext.current

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Restart scanning if permissions are granted
        if (permissions.values.any { it }) {
            viewModel.restartScanning()
        }
    }

    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val hasScan = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
            val hasConnect = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            if (!hasScan || !hasConnect) {
                permissionsLauncher.launch(
                    arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
                )
            }
        } else {
            val hasLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (!hasLocation) {
                permissionsLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nearby SOS Alerts") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (alerts.isEmpty()) {
            EmptyAlertsView(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(alerts, key = { it.id }) { alert ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.EndToStart) {
                                viewModel.deleteAlert(alert.id)
                                true
                            } else false
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            val color by animateColorAsState(
                                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) Color.Red else Color.Transparent,
                                label = "dismissColor"
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                                }
                            }
                        }
                    ) {
                        SOSAlertCard(
                            alert = alert,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyAlertsView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Bluetooth,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No SOS alerts detected nearby",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Your phone is silently scanning for emergency beacons from nearby tourists",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SOSAlertCard(
    alert: ReceivedSOSAlertEntity,
    viewModel: NearbySOSAlertsViewModel
) {
    val context = LocalContext.current
    val isRecent = (System.currentTimeMillis() - alert.receivedAt) < 5 * 60 * 1000

    var showDetailsDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    if (showDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showDetailsDialog = false },
            title = { Text("SOS Alert Details") },
            text = {
                Column {
                    val displayName = alert.victimName ?: "User ${alert.victimUserIdHash.take(6)}"
                    Text("Victim: $displayName")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Location: ${alert.latitude}, ${alert.longitude}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Distance: ${viewModel.getDistanceEstimate(alert.rssi)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Audio Recording: Sent to Cloud / Encrypted")
                    Spacer(modifier = Modifier.height(8.dp))
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
                    Text("Time: ${dateFormat.format(Date(alert.receivedAt))}")
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetailsDialog = false }) {
                    Text("Close")
                }
            },
            dismissButton = {
                Button(onClick = {
                    val uri = Uri.parse("geo:${alert.latitude},${alert.longitude}?q=${alert.latitude},${alert.longitude}(SOS+Alert)")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(intent)
                }) {
                    Text("Open in Maps")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { showDetailsDialog = true },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRecent) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isRecent) {
                    PulsingIndicator()
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(SosRed, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = "⚠️ SOS ALERT",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SosRed
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Type: ${viewModel.getSOSTypeLabel(alert.sosType)}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Distance: ${viewModel.getDistanceEstimate(alert.rssi)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Location: ${alert.latitude}, ${alert.longitude}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
            val dateString = dateFormat.format(Date(alert.receivedAt))
            Text(
                text = "Received: $dateString",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            var isAcknowledging by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
            var ackResult by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<Boolean?>(null) }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = {
                        val uri = Uri.parse("geo:${alert.latitude},${alert.longitude}?q=${alert.latitude},${alert.longitude}(SOS+Alert)")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        context.startActivity(intent)
                    }
                ) {
                    Text("Maps")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = {
                        isAcknowledging = true
                        val helperInfo = "Helper Device (${android.os.Build.MODEL})"
                        viewModel.acknowledgeDirectly(alert.victimUserIdHash, helperInfo) { success ->
                            isAcknowledging = false
                            ackResult = success
                        }
                    },
                    enabled = !isAcknowledging && ackResult != true,
                    colors = ButtonDefaults.buttonColors(containerColor = if (ackResult == true) Color.Green else SosRed)
                ) {
                    if (isAcknowledging) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Connecting...")
                    } else if (ackResult == true) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Acknowledged")
                    } else if (ackResult == false) {
                        Text("Retry Ack")
                    } else {
                        Text("Acknowledge")
                    }
                }
            }
        }
    }
}

@Composable
fun PulsingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier
            .size(12.dp)
            .scale(scale)
            .background(SosRed, CircleShape)
    )
}
