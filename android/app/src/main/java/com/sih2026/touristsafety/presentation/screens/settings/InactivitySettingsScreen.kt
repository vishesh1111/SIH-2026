package com.sih2026.touristsafety.presentation.screens.settings

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sih2026.touristsafety.presentation.screens.checkin.CheckInPromptActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InactivitySettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(true) }
    var threshold by remember { mutableFloatStateOf(30f) }
    var stationaryEnabled by remember { mutableStateOf(true) }
    var unusualMovementEnabled by remember { mutableStateOf(true) }
    var speedAnomalyEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inactivity & Safety Monitor") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Monitoring Status", style = MaterialTheme.typography.titleMedium)
                                Text(if (isEnabled) "Active" else "Disabled", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        Switch(checked = isEnabled, onCheckedChange = { isEnabled = it })
                    }
                }
            }

            item {
                Text("Configuration", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
                
                Text("Inactivity Threshold: ${threshold.toInt()} mins")
                Slider(
                    value = threshold,
                    onValueChange = { threshold = it },
                    valueRange = 15f..120f,
                    steps = 6,
                    enabled = isEnabled
                )
                
                ListItem(
                    headlineContent = { Text("Stationary Detection") },
                    supportingContent = { Text("Alert if at same location for too long") },
                    trailingContent = { Switch(checked = stationaryEnabled, onCheckedChange = { stationaryEnabled = it }, enabled = isEnabled) }
                )
                
                ListItem(
                    headlineContent = { Text("Unusual Movement") },
                    supportingContent = { Text("Detect erratic or zigzag patterns") },
                    trailingContent = { Switch(checked = unusualMovementEnabled, onCheckedChange = { unusualMovementEnabled = it }, enabled = isEnabled) }
                )
                
                ListItem(
                    headlineContent = { Text("Speed Anomaly") },
                    supportingContent = { Text("Alert on sudden high speed (>80km/h)") },
                    trailingContent = { Switch(checked = speedAnomalyEnabled, onCheckedChange = { speedAnomalyEnabled = it }, enabled = isEnabled) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Emergency Contacts to Alert", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
                
                var contact1 by remember { mutableStateOf(true) }
                var contact2 by remember { mutableStateOf(true) }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = contact1, onCheckedChange = { contact1 = it })
                    Text("Dad (+91 9876543210)")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = contact2, onCheckedChange = { contact2 = it })
                    Text("Mom (+91 8765432109)")
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
