package com.sih2026.touristsafety.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sih2026.touristsafety.presentation.navigation.Screen

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onHazardClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Layer 1: The standard Navigation Bar (5 items)
        NavigationBar(
            modifier = Modifier.fillMaxWidth().shadow(elevation = 8.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            NavigationBarItem(
                selected = currentRoute == Screen.Home.route,
                onClick = { onNavigate(Screen.Home.route) },
                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                label = { Text("Home") },
                colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.secondaryContainer)
            )
            NavigationBarItem(
                selected = currentRoute == Screen.Map.route,
                onClick = { onNavigate(Screen.Map.route) },
                icon = { Icon(Icons.Default.Map, contentDescription = "Map") },
                label = { Text("Map") },
                colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.secondaryContainer)
            )
            // Empty placeholder for SOS to maintain perfect 5-item spacing
            NavigationBarItem(
                selected = false,
                onClick = { },
                icon = { androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(24.dp)) },
                label = { Text("") },
                enabled = false,
                colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
            )
            NavigationBarItem(
                selected = currentRoute == Screen.DisasterAlerts.route,
                onClick = { onNavigate(Screen.DisasterAlerts.route) },
                icon = { Icon(Icons.Default.Notifications, contentDescription = "Alerts") },
                label = { Text("Alerts") },
                colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.secondaryContainer)
            )
            NavigationBarItem(
                selected = currentRoute == Screen.WomenSafety.route,
                onClick = { onNavigate(Screen.WomenSafety.route) },
                icon = { Icon(Icons.Default.Shield, contentDescription = "Safety") },
                label = { Text("Safety") },
                colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.secondaryContainer)
            )
        }

        // Layer 2: The floating SOS button overlay (5 slots)
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .offset(y = (-24).dp), // Float it above the bar
                contentAlignment = Alignment.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(elevation = 6.dp, shape = CircleShape)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD32F2F))
                        .clickable { onHazardClick() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Hazard Alarm",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
        }
    }
}
