package com.sih2026.touristsafety.presentation.screens.settings

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sih2026.touristsafety.presentation.screens.checkin.CheckInPromptActivity
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InactivitySettingsScreen(
    viewModel: InactivitySettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val isEnabled by viewModel.isEnabled.collectAsState()
    val thresholdMinutes by viewModel.thresholdMinutes.collectAsState()
    val stationaryEnabled by viewModel.stationaryEnabled.collectAsState()
    val unusualMovementEnabled by viewModel.unusualMovementEnabled.collectAsState()
    val speedAnomalyEnabled by viewModel.speedAnomalyEnabled.collectAsState()
    val contacts by viewModel.contacts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inactivity & Safety Monitor") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Monitoring Status",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = if (isEnabled) "Active" else "Disabled",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { viewModel.setEnabled(it) }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Configuration",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text("Inactivity Threshold: $thresholdMinutes mins")
                Slider(
                    value = thresholdMinutes.toFloat(),
                    onValueChange = { viewModel.setThresholdMinutes(it.roundToInt()) },
                    valueRange = 15f..120f,
                    steps = 6,
                    enabled = isEnabled
                )

                ListItem(
                    headlineContent = { Text("Stationary Detection") },
                    supportingContent = { Text("Alert if at same location for too long") },
                    trailingContent = {
                        Switch(
                            checked = stationaryEnabled,
                            onCheckedChange = { viewModel.setStationaryDetection(it) },
                            enabled = isEnabled
                        )
                    }
                )

                ListItem(
                    headlineContent = { Text("Unusual Movement") },
                    supportingContent = { Text("Detect erratic or zigzag patterns") },
                    trailingContent = {
                        Switch(
                            checked = unusualMovementEnabled,
                            onCheckedChange = { viewModel.setUnusualMovement(it) },
                            enabled = isEnabled
                        )
                    }
                )

                ListItem(
                    headlineContent = { Text("Speed Anomaly") },
                    supportingContent = { Text("Alert on sudden high speed (>80km/h)") },
                    trailingContent = {
                        Switch(
                            checked = speedAnomalyEnabled,
                            onCheckedChange = { viewModel.setSpeedAnomaly(it) },
                            enabled = isEnabled
                        )
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Emergency Contacts to Alert",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (contacts.isEmpty()) {
                    Text(
                        text = "No emergency contacts configured yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    contacts.forEach { contact ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = contact.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (contact.isPrimary) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Badge { Text("Primary") }
                                        }
                                    }
                                    Text(
                                        text = "${contact.countryCode} ${contact.phone} • ${contact.relationship}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        val intent = Intent(context, CheckInPromptActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Simulate Check-in Prompt")
                }
            }
        }
    }
}
