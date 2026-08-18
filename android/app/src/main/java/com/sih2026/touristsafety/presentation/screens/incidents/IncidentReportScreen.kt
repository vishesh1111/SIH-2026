package com.sih2026.touristsafety.presentation.screens.incidents

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentReportScreen(
    viewModel: IncidentReportViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val currentStep by viewModel.currentStep.collectAsState()
    val capturedPhotos by viewModel.capturedPhotos.collectAsState()
    val incidentType by viewModel.incidentType.collectAsState()
    val description by viewModel.description.collectAsState()
    val location by viewModel.location.collectAsState()
    val aiAnalysis by viewModel.aiAnalysis.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()

    var showSuccessDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Incident - Step $currentStep of 3") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep > 1) viewModel.previousStep() else onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (currentStep) {
                1 -> Step1CaptureEvidence(
                    photos = capturedPhotos,
                    onPhotoAdded = { viewModel.capturePhoto(it) },
                    onPhotoRemoved = { viewModel.removePhoto(it) },
                    onNext = { viewModel.nextStep() }
                )
                2 -> Step2IncidentDetails(
                    incidentType = incidentType,
                    description = description,
                    location = location,
                    aiAnalysis = aiAnalysis,
                    isAnalyzing = isAnalyzing,
                    onTypeChange = { viewModel.updateIncidentType(it) },
                    onDescriptionChange = { viewModel.updateDescription(it) },
                    onAnalyze = { viewModel.analyzeWithAI() },
                    onNext = { viewModel.nextStep() }
                )
                3 -> Step3ReviewSubmit(
                    photos = capturedPhotos,
                    incidentType = incidentType,
                    description = description,
                    location = location,
                    aiAnalysis = aiAnalysis,
                    onSubmit = { 
                        viewModel.submitReport()
                        showSuccessDialog = true
                    },
                    onSaveDraft = { viewModel.saveDraft() }
                )
            }
        }

        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { showSuccessDialog = false },
                title = { Text("Report Submitted") },
                text = { Text("Your incident report (ID: INC-${System.currentTimeMillis().toString().takeLast(6)}) has been submitted successfully.") },
                confirmButton = {
                    Button(onClick = {
                        showSuccessDialog = false
                        onNavigateBack()
                    }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun Step1CaptureEvidence(
    photos: List<Uri>,
    onPhotoAdded: (Uri) -> Unit,
    onPhotoRemoved: (Int) -> Unit,
    onNext: () -> Unit
) {
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> uri?.let { onPhotoAdded(it) } }
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mock Camera Preview
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
            Text("Camera Preview", color = Color.White, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { galleryLauncher.launch("image/*") }) {
                Icon(Icons.Default.Image, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gallery")
            }
            Button(
                onClick = { onPhotoAdded(Uri.parse("mock_uri_${System.currentTimeMillis()}")) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Camera, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Capture")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (photos.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(photos) { index, uri ->
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        // In a real app, use Coil AsyncImage
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.align(Alignment.Center))
                        IconButton(
                            onClick = { onPhotoRemoved(index) },
                            modifier = Modifier.align(Alignment.TopEnd).size(24.dp).padding(4.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        } else {
            Text("No photos captured yet (Max 5)", style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            enabled = photos.isNotEmpty()
        ) {
            Text("Next: Incident Details")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step2IncidentDetails(
    incidentType: String,
    description: String,
    location: LatLng?,
    aiAnalysis: com.sih2026.touristsafety.data.remote.AIAnalysisResult?,
    isAnalyzing: Boolean,
    onTypeChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAnalyze: () -> Unit,
    onNext: () -> Unit
) {
    val scrollState = rememberScrollState()
    var expandedType by remember { mutableStateOf(false) }
    val types = listOf("Theft", "Snatching", "Harassment", "Accident", "Lost Item", "Fraud", "Assault", "Other")

    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val currentTime = sdf.format(Date())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expandedType,
            onExpandedChange = { expandedType = !expandedType }
        ) {
            OutlinedTextField(
                value = incidentType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Incident Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedType,
                onDismissRequest = { expandedType = false }
            ) {
                types.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            onTypeChange(type)
                            expandedType = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            maxLines = 5
        )

        OutlinedTextField(
            value = "Date & Time: $currentTime",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = location?.let { "Lat: ${it.latitude}, Lng: ${it.longitude}" } ?: "Fetching location...",
                onValueChange = {},
                readOnly = true,
                label = { Text("Location") },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { /* Edit on Map */ }) {
                Icon(Icons.Default.Map, contentDescription = "Edit Location")
            }
        }

        Button(
            onClick = onAnalyze,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isAnalyzing && aiAnalysis == null
        ) {
            if (isAnalyzing) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analyzing Evidence...")
            } else {
                Text("Analyze Evidence with AI")
            }
        }

        aiAnalysis?.let { analysis ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("AI Analysis Result", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Scene: ${analysis.sceneDescription}")
                    Text("Detected: ${analysis.detectedObjects.joinToString(", ")}")
                    Text("Severity: ${analysis.severityAssessment}")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f, false))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            enabled = description.isNotBlank()
        ) {
            Text("Review & Submit")
        }
    }
}

@Composable
fun Step3ReviewSubmit(
    photos: List<Uri>,
    incidentType: String,
    description: String,
    location: LatLng?,
    aiAnalysis: com.sih2026.touristsafety.data.remote.AIAnalysisResult?,
    onSubmit: () -> Unit,
    onSaveDraft: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Incident Summary", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Type: $incidentType", fontWeight = FontWeight.Bold)
                Text("Description: $description")
                Text("Location: ${location?.latitude}, ${location?.longitude}")
                Text("Photos attached: ${photos.size}")
            }
        }

        aiAnalysis?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("AI Assessment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Severity: ${it.severityAssessment}")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Submit Report")
        }

        OutlinedButton(
            onClick = onSaveDraft,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save as Draft")
        }
    }
}
