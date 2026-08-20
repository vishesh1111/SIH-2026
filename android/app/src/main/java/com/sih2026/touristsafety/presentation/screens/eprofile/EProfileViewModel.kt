package com.sih2026.touristsafety.presentation.screens.eprofile

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sih2026.touristsafety.data.local.dao.DocumentDao
import com.sih2026.touristsafety.data.local.entities.DocumentEntity
import com.sih2026.touristsafety.utils.QRCodeGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.sih2026.touristsafety.data.local.entities.ProfileEntity

import com.sih2026.touristsafety.data.local.dao.ProfileDao

@HiltViewModel
class EProfileViewModel @Inject constructor(
    private val documentDao: DocumentDao,
    private val profileDao: ProfileDao
) : ViewModel() {

    private val _profile = MutableStateFlow<ProfileEntity?>(null)
    val profile: StateFlow<ProfileEntity?> = _profile.asStateFlow()

    private val _documents = MutableStateFlow<Map<String, List<DocumentEntity>>>(emptyMap())
    val documents: StateFlow<Map<String, List<DocumentEntity>>> = _documents.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadProfile()
        loadDocuments()
    }

    fun loadProfile() {
        viewModelScope.launch {
            var dbProfile = profileDao.getProfile("u1")
            if (dbProfile == null) {
                dbProfile = ProfileEntity(
                    id = "u1",
                    fullName = "Vishesh Verma",
                    nationality = "Indian",
                    gender = "Male",
                    phone = "+91 9876543210",
                    email = "test@example.com",
                    passportNumber = null,
                    visaNumber = null,
                    profilePhotoUrl = null,
                    createdAt = System.currentTimeMillis()
                )
                profileDao.insertProfile(dbProfile)
            }
            _profile.value = dbProfile
        }
    }

    fun updateProfile(fullName: String, nationality: String, gender: String) {
        viewModelScope.launch {
            val currentProfile = _profile.value ?: return@launch
            val updatedProfile = currentProfile.copy(
                fullName = fullName,
                nationality = nationality,
                gender = gender
            )
            profileDao.insertProfile(updatedProfile)
            _profile.value = updatedProfile
        }
    }

    fun updateProfilePhoto(photoUrl: String) {
        viewModelScope.launch {
            val currentProfile = _profile.value ?: return@launch
            val updatedProfile = currentProfile.copy(
                profilePhotoUrl = photoUrl
            )
            profileDao.insertProfile(updatedProfile)
            _profile.value = updatedProfile
        }
    }

    fun loadDocuments() {
        viewModelScope.launch {
            _isLoading.value = true
            documentDao.getDocumentsForUser("u1").collect { docs ->
                _documents.value = docs.groupBy { it.docType }
                _isLoading.value = false
            }
        }
    }

    fun addDocument(document: DocumentEntity) {
        viewModelScope.launch {
            documentDao.insertDocument(document)
        }
    }

    fun deleteDocument(id: String) {
        viewModelScope.launch {
            val doc = documentDao.getDocumentById(id)
            if (doc != null) {
                documentDao.deleteDocument(doc)
            }
        }
    }

    fun generateQRCode(profileData: String): Bitmap? {
        return QRCodeGenerator.generateQR(profileData, 512)
    }
}
