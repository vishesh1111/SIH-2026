package com.sih2026.touristsafety.presentation.screens.incidents

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.sih2026.touristsafety.data.remote.AIAnalysisResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LatLng(val latitude: Double, val longitude: Double)

@HiltViewModel
class IncidentReportViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _capturedPhotos = MutableStateFlow<List<Uri>>(emptyList())
    val capturedPhotos: StateFlow<List<Uri>> = _capturedPhotos.asStateFlow()

    private val _incidentType = MutableStateFlow("Theft")
    val incidentType: StateFlow<String> = _incidentType.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _location = MutableStateFlow<LatLng?>(LatLng(28.6139, 77.2090))
    val location: StateFlow<LatLng?> = _location.asStateFlow()

    private val _aiAnalysis = MutableStateFlow<AIAnalysisResult?>(null)
    val aiAnalysis: StateFlow<AIAnalysisResult?> = _aiAnalysis.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-3.6-flash",
        apiKey = com.sih2026.touristsafety.utils.GeminiApiKeys.getNextKey()
    )

    fun nextStep() {
        if (_currentStep.value < 3) {
            _currentStep.value += 1
        }
    }

    fun previousStep() {
        if (_currentStep.value > 1) {
            _currentStep.value -= 1
        }
    }

    fun capturePhoto(uri: Uri) {
        val currentPhotos = _capturedPhotos.value.toMutableList()
        if (currentPhotos.size < 5) {
            currentPhotos.add(uri)
            _capturedPhotos.value = currentPhotos
        }
    }

    fun removePhoto(index: Int) {
        val currentPhotos = _capturedPhotos.value.toMutableList()
        if (index in currentPhotos.indices) {
            currentPhotos.removeAt(index)
            _capturedPhotos.value = currentPhotos
        }
    }
    
    fun updateIncidentType(type: String) {
        _incidentType.value = type
    }
    
    fun updateDescription(desc: String) {
        _description.value = desc
    }

    fun analyzeWithAI() {
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                val context = getApplication<Application>()
                val bitmaps = _capturedPhotos.value.mapNotNull { uri ->
                    try {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            android.graphics.BitmapFactory.decodeStream(stream)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("IncidentVM", "Failed to load image: ${e.message}")
                        null
                    }
                }

                val prompt = buildString {
                    append("You are an AI incident analysis assistant for a tourist safety app. ")
                    append("Analyze the provided evidence image(s) and the user's description to generate a structured incident report. ")
                    append("\n\nUser's description: ${_description.value.ifBlank { "No description provided" }}")
                    append("\nReported incident type: ${_incidentType.value}")
                    append("\n\nPlease provide your analysis in EXACTLY this format (use these exact labels):")
                    append("\nScene: <describe what you see in the image(s) and the incident scene>")
                    append("\nDetected: <comma-separated list of key objects, people, or evidence visible>")
                    append("\nType: <your assessment of the incident type based on the evidence>")
                    append("\nSeverity: <Low, Moderate, High, or Critical>")
                }

                // Execute raw HTTP request to bypass SDK key format issues
                val responseText = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val partsArray = org.json.JSONArray()
                    
                    // Add images
                    bitmaps.forEach { bmp ->
                        val baos = java.io.ByteArrayOutputStream()
                        bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, baos)
                        val b64 = android.util.Base64.encodeToString(baos.toByteArray(), android.util.Base64.NO_WRAP)
                        
                        val inlineData = org.json.JSONObject()
                        inlineData.put("mimeType", "image/jpeg")
                        inlineData.put("data", b64)
                        
                        val part = org.json.JSONObject()
                        part.put("inlineData", inlineData)
                        partsArray.put(part)
                    }
                    
                    // Add text prompt
                    val textPart = org.json.JSONObject()
                    textPart.put("text", prompt)
                    partsArray.put(textPart)

                    val contentsArray = org.json.JSONArray()
                    val contentObj = org.json.JSONObject()
                    contentObj.put("parts", partsArray)
                    contentsArray.put(contentObj)

                    val requestBodyJson = org.json.JSONObject()
                    requestBodyJson.put("contents", contentsArray)

                    val apiKey = com.sih2026.touristsafety.utils.GeminiApiKeys.getNextKey()
                    val urlStr = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent"
                    
                    val url = java.net.URL(urlStr)
                    val connection = url.openConnection() as java.net.HttpURLConnection
                    connection.requestMethod = "POST"
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.setRequestProperty("x-goog-api-key", apiKey)
                    connection.setRequestProperty("x-goog-api-client", "genai-android/0.9.0")
                    connection.doOutput = true
                    
                    connection.outputStream.use { os ->
                        val input = requestBodyJson.toString().toByteArray(Charsets.UTF_8)
                        os.write(input, 0, input.size)
                    }
                    
                    val responseCode = connection.responseCode
                    val responseString = if (responseCode in 200..299) {
                        connection.inputStream.bufferedReader().use { it.readText() }
                    } else {
                        connection.errorStream.bufferedReader().use { it.readText() }
                    }
                    
                    if (responseCode !in 200..299) {
                        throw Exception("API Error: $responseCode - $responseString")
                    }
                    
                    val responseJson = org.json.JSONObject(responseString)
                    responseJson.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                }

                android.util.Log.d("IncidentVM", "Gemini response: $responseText")

                // Parse the structured response
                val scene = extractField(responseText, "Scene") ?: "Unable to analyze scene"
                val detected = extractField(responseText, "Detected") ?: "No objects detected"
                val type = extractField(responseText, "Type") ?: _incidentType.value
                val severity = extractField(responseText, "Severity") ?: "Moderate"

                _aiAnalysis.value = AIAnalysisResult(
                    sceneDescription = scene,
                    detectedObjects = detected.split(",").map { it.trim() }.filter { it.isNotBlank() },
                    potentialIncidentType = type,
                    severityAssessment = severity
                )
            } catch (e: Exception) {
                android.util.Log.e("IncidentVM", "AI analysis failed: ${e.message}", e)
                _aiAnalysis.value = AIAnalysisResult(
                    sceneDescription = "Analysis failed: ${e.message}",
                    detectedObjects = emptyList(),
                    potentialIncidentType = _incidentType.value,
                    severityAssessment = "Unknown"
                )
            }
            _isAnalyzing.value = false
        }
    }

    private fun extractField(text: String, field: String): String? {
        val regex = Regex("$field:\\s*(.+)", RegexOption.IGNORE_CASE)
        return regex.find(text)?.groupValues?.get(1)?.trim()
    }

    fun submitReport() {
        viewModelScope.launch {
            // Save to DB and API
        }
    }

    fun saveDraft() {
        viewModelScope.launch {
            // Save draft locally
        }
    }
}
