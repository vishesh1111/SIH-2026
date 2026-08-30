package com.sih2026.touristsafety.presentation.screens.efir

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sih2026.touristsafety.data.remote.StructuredFir

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EFirScreen(
    onNavigateBack: () -> Unit,
    viewModel: EFirViewModel = hiltViewModel()
) {
    val currentStep by viewModel.currentStep.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val isPdfExporting by viewModel.isPdfExporting.collectAsState()
    val downloadedPdfUri by viewModel.downloadedPdfUri.collectAsState()
    val rawDescription by viewModel.rawDescription.collectAsState()
    val structuredFir by viewModel.structuredFir.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Official e-FIR Generator (BNS)") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            when (currentStep) {
                1 -> Step1DescribeIncident(
                    rawDescription = rawDescription,
                    onDescriptionChange = viewModel::setRawDescription,
                    isGenerating = isGenerating,
                    onGenerate = viewModel::generateFir
                )
                2 -> Step2ReviewFir(
                    structuredFir = structuredFir,
                    onUpdateField = viewModel::updateField,
                    onNext = viewModel::nextStep
                )
                3 -> Step3AttachEvidence(
                    onNext = viewModel::nextStep
                )
                4 -> Step4Submit(
                    structuredFir = structuredFir,
                    isPdfExporting = isPdfExporting,
                    downloadedPdfUri = downloadedPdfUri,
                    onDownloadPdf = viewModel::downloadPdf,
                    onOpenPdf = viewModel::openDownloadedPdf,
                    getPolicePortalUrl = viewModel::getPolicePortalUrl
                )
            }
        }
    }
}

@Composable
fun Step1DescribeIncident(
    rawDescription: String,
    onDescriptionChange: (String) -> Unit,
    isGenerating: Boolean,
    onGenerate: () -> Unit
) {
    val context = LocalContext.current
    Text(text = "Step 1: Incident Description", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text(
        text = "Describe what happened. Our Legal AI will classify applicable BNS 2023 sections and format an authentic CCTNS Form-II e-FIR.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(16.dp))
    OutlinedTextField(
        value = rawDescription,
        onValueChange = onDescriptionChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        placeholder = { Text("e.g. My black backpack containing iPhone 15 and passport was stolen near Red Fort around 2 PM by two unidentified bike riders...") },
        trailingIcon = {
            IconButton(onClick = { android.widget.Toast.makeText(context, "Listening for voice input...", android.widget.Toast.LENGTH_SHORT).show() }) {
                Icon(Icons.Default.Mic, contentDescription = "Voice Input")
            }
        }
    )
    Spacer(modifier = Modifier.height(16.dp))
    if (isGenerating) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Legal AI Drafter is structuring your e-FIR...",
                    fontWeight = FontWeight.SemiBold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    "Extracting BNS 2023 sections, police station jurisdiction & complaint petition",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    } else {
        Button(
            onClick = onGenerate,
            modifier = Modifier.fillMaxWidth(),
            enabled = rawDescription.isNotBlank()
        ) {
            Icon(Icons.Default.Description, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Draft Official e-FIR")
        }
    }
}

@Composable
fun Step2ReviewFir(
    structuredFir: StructuredFir?,
    onUpdateField: (String, Any) -> Unit,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    if (structuredFir == null) return
    
    Text(text = "Step 2: Review Legal Classifications", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text(
        text = "Review the extracted Bharatiya Nyaya Sanhita (BNS) sections and details below before final submission.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(16.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Applicable Statutory Sections (BNS 2023):", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                structuredFir.bnsSections.forEach { section ->
                    SuggestionChip(
                        onClick = { Toast.makeText(context, section, Toast.LENGTH_SHORT).show() },
                        label = { Text(section, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = structuredFir.incidentType,
        onValueChange = { onUpdateField("incidentType", it) },
        label = { Text("Incident Category") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = structuredFir.policeStation,
        onValueChange = { onUpdateField("policeStation", it) },
        label = { Text("Jurisdictional Police Station") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = structuredFir.dateTime,
        onValueChange = { onUpdateField("dateTime", it) },
        label = { Text("Date & Time of Occurrence") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    
    OutlinedTextField(
        value = structuredFir.place,
        onValueChange = { onUpdateField("place", it) },
        label = { Text("Place of Occurrence & Landmark") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = structuredFir.propertyLost,
        onValueChange = { onUpdateField("propertyLost", it) },
        label = { Text("Stolen / Involved Property Particulars") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = structuredFir.accusedDescription,
        onValueChange = { onUpdateField("accusedDescription", it) },
        label = { Text("Suspect / Accused Physical Description") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = structuredFir.complainantDetails,
        onValueChange = { onUpdateField("complainantDetails", it) },
        label = { Text("Complainant / Tourist Particulars") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = structuredFir.narrative,
        onValueChange = { onUpdateField("narrative", it) },
        label = { Text("Formal Narrative Summary") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3
    )
    Spacer(modifier = Modifier.height(16.dp))
    
    Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
        Text("Continue to Evidence & Location")
    }
}

@Composable
fun Step3AttachEvidence(
    onNext: () -> Unit
) {
    val context = LocalContext.current
    Text(text = "Step 3: Attach Evidence & Coordinates", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(16.dp))
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Photos / Evidence Files", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Attach photos of the scene, purchase bills of stolen items, or suspect sketches.", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(onClick = { Toast.makeText(context, "Evidence attached to e-FIR docket", Toast.LENGTH_SHORT).show() }) {
                Text("Select Evidence Files")
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Verified Incident GPS Coordinates", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Lat: 28.6139, Lng: 77.2090 (Connaught Place, New Delhi)", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = { Toast.makeText(context, "GPS Verified", Toast.LENGTH_SHORT).show() }) {
                Text("Verify Location")
            }
        }
    }
    
    Spacer(modifier = Modifier.height(24.dp))
    Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
        Text("Proceed to Generation & Download")
    }
}

@Composable
fun Step4Submit(
    structuredFir: StructuredFir?,
    isPdfExporting: Boolean,
    downloadedPdfUri: Uri?,
    onDownloadPdf: (android.content.Context) -> Unit,
    onOpenPdf: (android.content.Context) -> Unit,
    getPolicePortalUrl: (String) -> String
) {
    val context = LocalContext.current
    if (structuredFir == null) return
    
    Text(text = "Step 4: Official e-FIR Document", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(16.dp))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("CCTNS Form - II", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF003366)
                ) {
                    Text(
                        "BNS 2023 COMPLIANT",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("e-FIR Ref: ${structuredFir.firNumber}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Jurisdiction: ${structuredFir.policeStation}, ${structuredFir.district}")
            Text("Offence: ${structuredFir.incidentType}")
            Text("Sections: ${structuredFir.bnsSections.joinToString()}")
            Text("Complainant: ${structuredFir.complainantDetails}")
        }
    }
    
    Spacer(modifier = Modifier.height(20.dp))

    if (isPdfExporting) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Compiling official CCTNS PDF with QR code...", fontWeight = FontWeight.SemiBold)
            }
        }
    } else {
        Button(
            onClick = { onDownloadPdf(context) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Download, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generate & Download Official e-FIR (PDF)")
        }
    }

    if (downloadedPdfUri != null) {
        Spacer(modifier = Modifier.height(10.dp))
        FilledTonalButton(
            onClick = { onOpenPdf(context) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF388E3C))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Open Downloaded e-FIR PDF")
        }
    }
    
    Spacer(modifier = Modifier.height(12.dp))
    OutlinedButton(
        onClick = {
            val url = getPolicePortalUrl(structuredFir.state)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.OpenInNew, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Submit Directly to ${structuredFir.state} Police Portal")
    }
}
