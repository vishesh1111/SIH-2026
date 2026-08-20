package com.sih2026.touristsafety.presentation.screens.efir

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EFirScreen(
    onNavigateBack: () -> Unit,
    viewModel: EFirViewModel = hiltViewModel()
) {
    val currentStep by viewModel.currentStep.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val rawDescription by viewModel.rawDescription.collectAsState()
    val structuredFir by viewModel.structuredFir.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI E-FIR Maker") },
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
                    onDownloadPdf = viewModel::downloadPdf,
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
    Text(text = "Step 1: Describe Incident", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(16.dp))
    OutlinedTextField(
        value = rawDescription,
        onValueChange = onDescriptionChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        label = { Text("Tell us what happened in your own words...") },
        trailingIcon = {
            IconButton(onClick = { android.widget.Toast.makeText(context, "Voice input coming soon", android.widget.Toast.LENGTH_SHORT).show() }) {
                Icon(Icons.Default.Mic, contentDescription = "Voice Input")
            }
        }
    )
    Spacer(modifier = Modifier.height(16.dp))
    if (isGenerating) {
        CircularProgressIndicator(modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally))
        Text("Gemini AI is analyzing and structuring your FIR...", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    } else {
        Button(
            onClick = onGenerate,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Generate FIR")
        }
    }
}

@Composable
fun Step2ReviewFir(
    structuredFir: com.sih2026.touristsafety.data.remote.StructuredFir?,
    onUpdateField: (String, Any) -> Unit,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    if (structuredFir == null) return
    
    Text(text = "Step 2: Review Structured FIR", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(16.dp))
    
    OutlinedTextField(
        value = structuredFir.incidentType,
        onValueChange = { onUpdateField("incidentType", it) },
        label = { Text("Incident Type") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    
    Text("Suggested Sections:", style = MaterialTheme.typography.labelLarge)
    Row {
        structuredFir.bnsSections.forEach { section ->
            AssistChip(onClick = { android.widget.Toast.makeText(context, "Section details coming soon", android.widget.Toast.LENGTH_SHORT).show() }, label = { Text(section) }, modifier = Modifier.padding(end = 8.dp))
        }
    }
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = structuredFir.dateTime,
        onValueChange = { onUpdateField("dateTime", it) },
        label = { Text("Date/Time") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    
    OutlinedTextField(
        value = structuredFir.place,
        onValueChange = { onUpdateField("place", it) },
        label = { Text("Place of Occurrence") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = structuredFir.accusedDescription,
        onValueChange = { onUpdateField("accusedDescription", it) },
        label = { Text("Description of Accused") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = structuredFir.propertyLost,
        onValueChange = { onUpdateField("propertyLost", it) },
        label = { Text("Property Lost/Damaged") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    
    OutlinedTextField(
        value = structuredFir.witnesses,
        onValueChange = { onUpdateField("witnesses", it) },
        label = { Text("Witnesses") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = structuredFir.narrative,
        onValueChange = { onUpdateField("narrative", it) },
        label = { Text("Narrative") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3
    )
    Spacer(modifier = Modifier.height(16.dp))
    
    Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
        Text("Continue to Evidence")
    }
}

@Composable
fun Step3AttachEvidence(
    onNext: () -> Unit
) {
    val context = LocalContext.current
    Text(text = "Step 3: Attach Evidence", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(16.dp))
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Upload Photos/Videos")
            Button(onClick = { android.widget.Toast.makeText(context, "File upload coming soon", android.widget.Toast.LENGTH_SHORT).show() }) { Text("Select Files") }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Current GPS Coordinates")
            Text("Lat: 28.6139, Lng: 77.2090 (Connaught Place)")
            Button(onClick = { android.widget.Toast.makeText(context, "GPS refreshed", android.widget.Toast.LENGTH_SHORT).show() }) { Text("Refresh Location") }
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
        Text("Review & Submit")
    }
}

@Composable
fun Step4Submit(
    structuredFir: com.sih2026.touristsafety.data.remote.StructuredFir?,
    onDownloadPdf: (android.content.Context) -> Unit,
    getPolicePortalUrl: (String) -> String
) {
    val context = LocalContext.current
    if (structuredFir == null) return
    
    Text(text = "Step 4: Submit", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(16.dp))
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("FIR Preview", style = MaterialTheme.typography.titleMedium)
            Text("Type: ${structuredFir.incidentType}")
            Text("Sections: ${structuredFir.bnsSections.joinToString()}")
            Text("Place: ${structuredFir.place}")
            Text("Complainant: ${structuredFir.complainantDetails}")
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = { onDownloadPdf(context) }, modifier = Modifier.fillMaxWidth()) {
        Text("Download as PDF")
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    Button(
        onClick = {
            val url = getPolicePortalUrl("Delhi")
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
    ) {
        Text("Submit to Police Portal")
    }
}
