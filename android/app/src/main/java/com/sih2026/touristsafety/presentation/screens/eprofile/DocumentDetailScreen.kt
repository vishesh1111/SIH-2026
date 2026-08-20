package com.sih2026.touristsafety.presentation.screens.eprofile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.sih2026.touristsafety.utils.QRCodeGenerator
import com.sih2026.touristsafety.data.local.entities.DocumentEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailScreen(
    documentId: String,
    onNavigateBack: () -> Unit,
    viewModel: EProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val documents by viewModel.documents.collectAsState()
    
    // Find the specific document by ID
    var currentDoc by remember { mutableStateOf<DocumentEntity?>(null) }
    
    LaunchedEffect(documents) {
        currentDoc = documents.values.flatten().find { it.id == documentId }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentDoc?.docType ?: "Document Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { android.widget.Toast.makeText(context, "Share coming soon", android.widget.Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { 
                        viewModel.deleteDocument(documentId)
                        android.widget.Toast.makeText(context, "Document deleted", android.widget.Toast.LENGTH_SHORT).show()
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { padding ->
        if (currentDoc == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (currentDoc!!.fileUrl.isNotBlank()) {
                    coil.compose.AsyncImage(
                        model = java.io.File(currentDoc!!.fileUrl),
                        contentDescription = "Document Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Document Image placeholder
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("Document Image securely stored offline")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Type: ${currentDoc!!.docType}", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Number: ${currentDoc!!.docNumber}", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        val expiryText = currentDoc!!.metadata ?: "N/A"
                        Text("Expiry: $expiryText", style = MaterialTheme.typography.bodyLarge)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Verification QR Code", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                val qrBitmap = QRCodeGenerator.generateQR("doc:$documentId:hash", 300)
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Document QR Code",
                        modifier = Modifier.size(150.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}
