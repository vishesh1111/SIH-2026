package com.sih2026.touristsafety.presentation.screens.eprofile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentUploadScreen(
    onNavigateBack: () -> Unit,
    onSave: () -> Unit
) {
    var selectedType by remember { mutableStateOf("Passport") }
    var docNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var isScanning by remember { mutableStateOf(false) }
    
    val documentTypes = listOf("Passport", "Visa", "Flight Tickets", "Hotel Bookings", "Travel Insurance", "ID Cards")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload Document") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Dropdown mock (using a simple text field for demo)
            OutlinedTextField(
                value = selectedType,
                onValueChange = { selectedType = it },
                label = { Text("Document Type") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                onClick = { isScanning = true }
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (isScanning) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Scanning document using OCR...")
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Camera", modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tap to capture or select from gallery")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = docNumber,
                onValueChange = { docNumber = it },
                label = { Text("Document Number") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = expiryDate,
                onValueChange = { expiryDate = it },
                label = { Text("Expiry Date") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Document")
            }
        }
    }
}
