package com.sih2026.touristsafety.presentation.screens.eprofile

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToUpload: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: EProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val documents by viewModel.documents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var showQrDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("E-Profile Vault") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToUpload) {
                Icon(Icons.Default.Add, contentDescription = "Add Document")
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // Profile Header
                profile?.let { p ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .clickable { /* Update Avatar */ }
                            ) {
                                // Placeholder for avatar
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        Icons.Default.CameraAlt,
                                        contentDescription = "Avatar",
                                        modifier = Modifier.padding(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = p.fullName,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Verified",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text("${p.nationality} • ${p.gender}")
                                TextButton(onClick = { /* Edit Profile */ }, contentPadding = PaddingValues(0.dp)) {
                                    Text("Edit Profile")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Document Categories", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                val categories = listOf(
                    "Passport" to "📘",
                    "Visa" to "📝",
                    "Flight Tickets" to "✈️",
                    "Hotel Bookings" to "🏨",
                    "Travel Insurance" to "🛡️",
                    "ID Cards" to "🆔"
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(categories) { (catName, icon) ->
                        val count = documents[catName]?.size ?: 0
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clickable { /* Navigate to category list */ }
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = icon, style = MaterialTheme.typography.displaySmall)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = catName, fontWeight = FontWeight.SemiBold)
                                Text(text = "$count documents", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showQrDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Show QR Code")
                }
            }
        }
    }

    if (showQrDialog && profile != null) {
        Dialog(onDismissRequest = { showQrDialog = false }) {
            Card(modifier = Modifier.padding(16.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Profile Verification QR", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    val qrBitmap = viewModel.generateQRCode("profile:${profile?.id}:verified")
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier.size(200.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text("Failed to generate QR Code")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { showQrDialog = false }) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
