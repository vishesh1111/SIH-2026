package com.sih2026.touristsafety.presentation.screens.eprofile

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sih2026.touristsafety.data.local.entities.DocumentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentUploadScreen(
    initialCategory: String?,
    onNavigateBack: () -> Unit,
    onSave: () -> Unit,
    viewModel: EProfileViewModel = hiltViewModel()
) {
    val isFixedCategory = initialCategory != null
    var selectedType by remember { mutableStateOf(initialCategory ?: "Passport") }
    var docNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            capturedImage = bitmap
            Toast.makeText(context, "Image captured successfully", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isFixedCategory) "Upload $initialCategory" else "Upload Document") },
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
            val categories = listOf("Passport", "Visa", "Flight Tickets", "Hotel Bookings", "Travel Insurance", "ID Cards")
            var showDialog by remember { mutableStateOf(false) }

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { if (!isFixedCategory) showDialog = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Document Type: $selectedType",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (!isFixedCategory) {
                            Icon(
                                imageVector = if (showDialog) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Dropdown Arrow",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (showDialog && !isFixedCategory) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text("Select Document Type") },
                        text = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                categories.forEach { category ->
                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedType = category
                                                showDialog = false
                                            }
                                            .padding(vertical = 12.dp)
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                onClick = { takePictureLauncher.launch(null) }
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (capturedImage != null) {
                        Image(
                            bitmap = capturedImage!!.asImageBitmap(),
                            contentDescription = "Captured Document",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Camera", modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tap to capture using camera")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = docNumber,
                onValueChange = { docNumber = it },
                label = { Text("Document Number (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = expiryDate,
                onValueChange = { expiryDate = it },
                label = { Text("Expiry Date (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (selectedType.isBlank()) {
                        Toast.makeText(context, "Please provide document type", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    coroutineScope.launch(Dispatchers.IO) {
                        var savedPath = ""
                        capturedImage?.let { bitmap ->
                            try {
                                val file = java.io.File(context.filesDir, "doc_${UUID.randomUUID()}.jpg")
                                val out = java.io.FileOutputStream(file)
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                                out.flush()
                                out.close()
                                savedPath = file.absolutePath
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        val entity = DocumentEntity(
                            id = UUID.randomUUID().toString(),
                            userId = "u1", // Hardcoded default user id
                            docType = selectedType,
                            docNumber = docNumber,
                            fileUrl = savedPath,
                            expiryDate = null,
                            verificationStatus = "Pending",
                            metadata = expiryDate, // Storing expiry date in metadata for simplicity
                            createdAt = System.currentTimeMillis()
                        )
                        viewModel.addDocument(entity)
                        
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Saved as $selectedType", Toast.LENGTH_SHORT).show()
                            onSave()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Document")
            }
        }
    }
}
