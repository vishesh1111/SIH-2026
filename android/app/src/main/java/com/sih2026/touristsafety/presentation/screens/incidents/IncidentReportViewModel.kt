package com.sih2026.touristsafety.presentation.screens.incidents

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sih2026.touristsafety.data.remote.AIAnalysisResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LatLng(val latitude: Double, val longitude: Double)

@HiltViewModel
class IncidentReportViewModel @Inject constructor(
    // private val incidentApiService: IncidentApiService,
    // private val incidentDao: IncidentDao
) : ViewModel() {

    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _capturedPhotos = MutableStateFlow<List<Uri>>(emptyList())
    val capturedPhotos: StateFlow<List<Uri>> = _capturedPhotos.asStateFlow()

    private val _incidentType = MutableStateFlow("Theft")
    val incidentType: StateFlow<String> = _incidentType.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _location = MutableStateFlow<LatLng?>(LatLng(28.6139, 77.2090)) // Default to Delhi
    val location: StateFlow<LatLng?> = _location.asStateFlow()

    private val _aiAnalysis = MutableStateFlow<AIAnalysisResult?>(null)
    val aiAnalysis: StateFlow<AIAnalysisResult?> = _aiAnalysis.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

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
            // Simulate AI analysis delay
            delay(2000)
            _aiAnalysis.value = AIAnalysisResult(
                sceneDescription = "A crowded street with signs of commotion.",
                detectedObjects = listOf("Wallet", "Backpack", "Scattered Items"),
                potentialIncidentType = "Theft/Snatching",
                severityAssessment = "Moderate"
            )
            _isAnalyzing.value = false
        }
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
