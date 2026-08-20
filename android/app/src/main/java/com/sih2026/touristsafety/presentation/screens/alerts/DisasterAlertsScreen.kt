package com.sih2026.touristsafety.presentation.screens.alerts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sih2026.touristsafety.data.local.entities.DisasterAlertEntity
import java.util.concurrent.TimeUnit

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Call
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisasterAlertsScreen(
    viewModel: DisasterAlertsViewModel = hiltViewModel()
) {
    val alerts by viewModel.alerts.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isOffline by viewModel.isOffline.collectAsState()
    val weather by viewModel.weather.collectAsState()

    val context = LocalContext.current
    var currentLocationText by remember { mutableStateOf("Locating...") }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(Unit) {
        val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        
        if (hasPermission) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        try {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                                    if (addresses.isNotEmpty()) {
                                        val address = addresses[0]
                                        val city = address.locality ?: address.subAdminArea ?: "Unknown City"
                                        val state = address.adminArea ?: "Unknown State"
                                        currentLocationText = "$city, $state"
                                    } else {
                                        currentLocationText = "Location unknown"
                                    }
                                    viewModel.fetchWeatherForLocation(location.latitude, location.longitude)
                                }
                            } else {
                                @Suppress("DEPRECATION")
                                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                if (!addresses.isNullOrEmpty()) {
                                    val address = addresses[0]
                                    val city = address.locality ?: address.subAdminArea ?: "Unknown City"
                                    val state = address.adminArea ?: "Unknown State"
                                    currentLocationText = "$city, $state"
                                } else {
                                    currentLocationText = "Location unknown"
                                }
                                viewModel.fetchWeatherForLocation(location.latitude, location.longitude)
                            }
                        } catch (e: Exception) {
                            currentLocationText = "Location unavailable"
                        }
                    } else {
                        currentLocationText = "Location not found"
                    }
                }
            } catch (e: SecurityException) {
                currentLocationText = "Permission denied"
            }
        } else {
            currentLocationText = "Location permission required"
        }
    }

    val filters = listOf("All", "Cyclone", "Flood", "Earthquake", "Heat Wave", "Other")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alerts") },
                actions = {
                    IconButton(onClick = { viewModel.refreshAlerts() }) {
                        Icon(Icons.Default.Warning, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Offline banner
            if (isOffline) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No Internet Connection - Showing cached alerts",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            // Location
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Current Location: $currentLocationText", style = MaterialTheme.typography.titleMedium)
            }

            // Weather Card
            weather?.let { w ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Current Weather", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${String.format(java.util.Locale.US, "%.1f", w.temperature)}°C", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text("Feels like ${String.format(java.util.Locale.US, "%.1f", w.feelsLike)}°C", style = MaterialTheme.typography.bodyMedium)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            val isHot = w.feelsLike >= 35.0
                            if (isHot) {
                                Text("High Heat Risk", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            } else {
                                Text("Safe Temp", color = Color(0xFF4CAF50), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Rainfall: ${w.precipitation} mm", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // Filters
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { viewModel.filterAlerts(filter) },
                        label = { Text(filter) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Alerts list
            if (alerts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No active alerts in your area",
                            color = Color(0xFF4CAF50),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            } else {
                val sortedAlerts = alerts.sortedBy { alert ->
                    when (alert.severity.lowercase()) {
                        "extreme" -> 0
                        "severe" -> 1
                        "moderate" -> 2
                        else -> 3
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(sortedAlerts) { alert ->
                        AlertCard(alert)
                    }
                }
            }
        }
    }
}

@Composable
fun AlertCard(alert: DisasterAlertEntity) {
    var expanded by remember { mutableStateOf(false) }

    val severityColor = when (alert.severity.lowercase()) {
        "extreme" -> Color.Red
        "severe" -> Color(0xFFFFA500) // Orange
        "moderate" -> Color.Yellow
        else -> Color.Blue
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = severityColor.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = severityColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = alert.headline,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = severityColor,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = alert.severity.uppercase(),
                        color = if (severityColor == Color.Yellow) Color.Black else Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                
                Text(
                    text = "Source: ${alert.source}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = alert.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    alert.instructions?.let {
                        Text(
                            text = "Do's and Don'ts",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    Text(
                        text = "Affected Areas: ${alert.affectedStates}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}


