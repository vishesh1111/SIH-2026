package com.sih2026.touristsafety.presentation.screens.incidents

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.sih2026.touristsafety.data.remote.AIAnalysisResult
import com.sih2026.touristsafety.data.remote.StructuredFir
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentReportScreen(
    viewModel: IncidentReportViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToEFir: () -> Unit = {}
) {
    val currentStep by viewModel.currentStep.collectAsState()
    val capturedPhotos by viewModel.capturedPhotos.collectAsState()
    val incidentType by viewModel.incidentType.collectAsState()
    val description by viewModel.description.collectAsState()
    val location by viewModel.location.collectAsState()
    val aiAnalysis by viewModel.aiAnalysis.collectAsState()
    val structuredFir by viewModel.structuredFir.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val isPdfExporting by viewModel.isPdfExporting.collectAsState()
    val downloadedPdfUri by viewModel.downloadedPdfUri.collectAsState()

    var showSuccessDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

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
                    photos = capturedPhotos,
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
                    structuredFir = structuredFir,
                    isPdfExporting = isPdfExporting,
                    downloadedPdfUri = downloadedPdfUri,
                    onSubmit = { 
                        viewModel.submitReport()
                        showSuccessDialog = true
                    },
                    onSaveDraft = { 
                        viewModel.saveDraft()
                        Toast.makeText(context, "Draft saved successfully", Toast.LENGTH_SHORT).show()
                        onNavigateBack()
                    },
                    onGeneratePdf = { viewModel.downloadOfficialFirPdf(context) },
                    onOpenPdf = { viewModel.openDownloadedPdf(context) }
                )
            }
        }

        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { showSuccessDialog = false },
                title = { Text("Report Submitted") },
                text = { Text("Your incident report has been submitted. Would you like to generate the official CCTNS e-FIR PDF document?") },
                confirmButton = {
                    Button(onClick = {
                        showSuccessDialog = false
                        viewModel.downloadOfficialFirPdf(context)
                    }) {
                        Text("Download Official e-FIR (PDF)")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showSuccessDialog = false
                        onNavigateBack()
                    }) {
                        Text("Done")
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
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { onPhotoAdded(it) }
    }

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Capture / Upload Evidence",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Take up to 5 photos of the scene or missing items",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black)
        ) {
            if (hasCameraPermission) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            imageCapture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .build()

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageCapture
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                IconButton(
                    onClick = {
                        val imgCap = imageCapture ?: return@IconButton
                        val photoFile = File(context.cacheDir, "evidence_${System.currentTimeMillis()}.jpg")
                        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                        imgCap.takePicture(
                            outputOptions,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                    val savedUri = Uri.fromFile(photoFile)
                                    onPhotoAdded(savedUri)
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    exception.printStackTrace()
                                }
                            }
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .size(72.dp)
                        .background(Color.White, CircleShape)
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Take Photo",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }

                IconButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp)
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = "Gallery",
                        tint = Color.Black
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Camera permission required", color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text("Grant Permission")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { galleryLauncher.launch("image/*") }) {
                        Text("Pick from Gallery")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (photos.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(photos) { index, uri ->
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        coil.compose.AsyncImage(
                            model = uri,
                            contentDescription = "Photo $index",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { onPhotoRemoved(index) },
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.TopEnd)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Delete",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (photos.isEmpty()) "Skip Photo & Continue" else "Continue (${photos.size} Photos)")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step2IncidentDetails(
    photos: List<Uri>,
    incidentType: String,
    description: String,
    location: LatLng?,
    aiAnalysis: AIAnalysisResult?,
    isAnalyzing: Boolean,
    onTypeChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAnalyze: () -> Unit,
    onNext: () -> Unit
) {
    val incidentTypes = listOf("Theft", "Harassment", "Scam / Fraud", "Physical Assault", "Lost Item", "Medical Emergency", "Other")
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Incident Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        if (photos.isNotEmpty()) {
            Text("Evidence Photos (${photos.size})", style = MaterialTheme.typography.labelMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(photos) { _, uri ->
                    coil.compose.AsyncImage(
                        model = uri,
                        contentDescription = "Evidence",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = incidentType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Incident Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                incidentTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            onTypeChange(type)
                            expanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Description") },
            placeholder = { Text("Describe what happened, lost items, suspects...") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        val currentDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        OutlinedTextField(
            value = "Date & Time: $currentDate",
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
            IconButton(onClick = { Toast.makeText(context, "Location pinned: Connaught Place, New Delhi", Toast.LENGTH_SHORT).show() }) {
                Icon(Icons.Default.Map, contentDescription = "Edit Location")
            }
        }

        Button(
            onClick = onAnalyze,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isAnalyzing && aiAnalysis == null && description.isNotBlank()
        ) {
            if (isAnalyzing) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analyzing Evidence & Legal Rules...")
            } else {
                Text("Analyze Evidence with AI")
            }
        }

        aiAnalysis?.let { analysis ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("AI Legal Analysis Result", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Scene: ${analysis.sceneDescription}", style = MaterialTheme.typography.bodyMedium)
                    if (analysis.detectedObjects.isNotEmpty()) {
                        Text("Detected: ${analysis.detectedObjects.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                    }
                    Text("Severity: ${analysis.severityAssessment}", fontWeight = FontWeight.Bold, color = if (analysis.severityAssessment.contains("High", true) || analysis.severityAssessment.contains("Critical", true)) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                    
                    if (analysis.suggestedBnsSections.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Applicable BNS (2023) Sections:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        Row(modifier = Modifier.fillMaxWidth()) {
                            analysis.suggestedBnsSections.forEach { section ->
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(section, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            }
                        }
                    }

                    if (analysis.jurisdictionalPoliceStation.isNotBlank()) {
                        Text("Jurisdiction: ${analysis.jurisdictionalPoliceStation}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
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
    aiAnalysis: AIAnalysisResult?,
    structuredFir: StructuredFir?,
    isPdfExporting: Boolean,
    downloadedPdfUri: Uri?,
    onSubmit: () -> Unit,
    onSaveDraft: () -> Unit,
    onGeneratePdf: () -> Unit,
    onOpenPdf: () -> Unit
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
                Text("Type: $incidentType", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Description: $description")
                Text("Location: ${location?.latitude ?: 28.6139}, ${location?.longitude ?: 77.2090}")
                Text("Photos attached: ${photos.size}")
                if (photos.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().height(80.dp)
                    ) {
                        itemsIndexed(photos) { index, uri ->
                            coil.compose.AsyncImage(
                                model = uri,
                                contentDescription = "Photo ${index + 1}",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("AI Legal Assessment (BNSS 173)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF003366)
                    ) {
                        Text(
                            "BNS 2023",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                val sections = structuredFir?.bnsSections ?: aiAnalysis?.suggestedBnsSections ?: listOf("Sec 303(2) BNS (Theft)")
                Text("Classified Sections: ${sections.joinToString(", ")}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                
                val ps = structuredFir?.policeStation ?: aiAnalysis?.jurisdictionalPoliceStation ?: "Connaught Place Police Station"
                Text("Jurisdiction: $ps, New Delhi District", style = MaterialTheme.typography.bodySmall)

                val severity = aiAnalysis?.severityAssessment ?: "Moderate"
                Text("Assessed Severity: $severity", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Submit Incident Report")
        }

        if (isPdfExporting) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Compiling CCTNS e-FIR PDF with QR code...", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        } else {
            FilledTonalButton(
                onClick = onGeneratePdf,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Official e-FIR (BNS Form-II PDF)")
            }
        }

        if (downloadedPdfUri != null) {
            FilledTonalButton(
                onClick = onOpenPdf,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Downloaded e-FIR PDF", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
            }
        }

        OutlinedButton(
            onClick = onSaveDraft,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save as Draft")
        }
    }
}
