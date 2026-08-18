package com.sih2026.touristsafety.presentation.screens.efir

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sih2026.touristsafety.data.remote.EFirApiService
import com.sih2026.touristsafety.data.remote.FirRequest
import com.sih2026.touristsafety.data.remote.StructuredFir
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EFirViewModel @Inject constructor(
    // private val apiService: EFirApiService
) : ViewModel() {

    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _rawDescription = MutableStateFlow("")
    val rawDescription: StateFlow<String> = _rawDescription.asStateFlow()

    private val _structuredFir = MutableStateFlow<StructuredFir?>(null)
    val structuredFir: StateFlow<StructuredFir?> = _structuredFir.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _attachedEvidence = MutableStateFlow<List<Uri>>(emptyList())
    val attachedEvidence: StateFlow<List<Uri>> = _attachedEvidence.asStateFlow()
    
    fun setRawDescription(desc: String) {
        _rawDescription.value = desc
    }

    fun nextStep() {
        if (_currentStep.value < 4) {
            _currentStep.value += 1
        }
    }
    
    fun previousStep() {
        if (_currentStep.value > 1) {
            _currentStep.value -= 1
        }
    }

    fun generateFir() {
        viewModelScope.launch {
            _isGenerating.value = true
            // Mock API call
            delay(2000)
            _structuredFir.value = StructuredFir(
                incidentType = "Theft",
                bnsSections = listOf("BNS 303", "BNS 305"),
                dateTime = "2026-08-18 10:00 AM",
                place = "Connaught Place, New Delhi",
                accusedDescription = "Unknown, approx 5'8\", wearing black hoodie",
                propertyLost = "Wallet containing Rs 2000, ID cards (Est. Value: Rs 2500)",
                witnesses = "None",
                narrative = _rawDescription.value,
                complainantDetails = "Vishesh Verma, Indian Tourist"
            )
            _isGenerating.value = false
            _currentStep.value = 2
        }
    }

    fun updateField(field: String, value: Any) {
        val currentFir = _structuredFir.value ?: return
        _structuredFir.value = when(field) {
            "incidentType" -> currentFir.copy(incidentType = value as String)
            "dateTime" -> currentFir.copy(dateTime = value as String)
            "place" -> currentFir.copy(place = value as String)
            "accusedDescription" -> currentFir.copy(accusedDescription = value as String)
            "propertyLost" -> currentFir.copy(propertyLost = value as String)
            "witnesses" -> currentFir.copy(witnesses = value as String)
            "narrative" -> currentFir.copy(narrative = value as String)
            "complainantDetails" -> currentFir.copy(complainantDetails = value as String)
            else -> currentFir
        }
    }

    fun downloadPdf() {
        // Mock PDF download
    }

    fun getPolicePortalUrl(state: String): String {
        return when(state.lowercase()) {
            "delhi" -> "https://digitalpolice.gov.in/"
            "maharashtra" -> "https://citizen.mahapolice.gov.in/"
            "karnataka" -> "https://ksp.karnataka.gov.in/"
            "tamil nadu" -> "https://eservices.tnpolice.gov.in/"
            "kerala" -> "https://keralapolice.gov.in/"
            "goa" -> "https://goapolice.gov.in/"
            "rajasthan" -> "https://police.rajasthan.gov.in/"
            "uttar pradesh" -> "https://uppolice.gov.in/"
            "gujarat" -> "https://gujhome.gujarat.gov.in/"
            "west bengal" -> "https://wbpolice.gov.in/"
            else -> "https://digitalpolice.gov.in/"
        }
    }
}
