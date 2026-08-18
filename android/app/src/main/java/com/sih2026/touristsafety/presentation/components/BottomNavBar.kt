package com.sih2026.touristsafety.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
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
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Screen.Home.route,
            onClick = { onNavigate(Screen.Home.route) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Map.route,
            onClick = { onNavigate(Screen.Map.route) },
            icon = { Icon(Icons.Default.Map, contentDescription = "Map") },
            label = { Text("Map") }
        )
        NavigationBarItem(
            selected = currentRoute == Screen.SOS.route,
            onClick = { onNavigate(Screen.SOS.route) },
            icon = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color(0xFFD32F2F))
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "SOS",
                        tint = Color.White
                    )
                }
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentRoute == Screen.DisasterAlerts.route,
            onClick = { onNavigate(Screen.DisasterAlerts.route) },
            icon = { Icon(Icons.Default.Notifications, contentDescription = "Alerts") },
            label = { Text("Alerts") }
        )
        NavigationBarItem(
            selected = currentRoute == Screen.EProfile.route,
            onClick = { onNavigate(Screen.EProfile.route) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}
