package com.sih2026.touristsafety.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sih2026.touristsafety.data.local.dao.ProfileDao
import com.sih2026.touristsafety.data.local.entities.ProfileEntity
import com.sih2026.touristsafety.data.remote.SupabaseManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val profileDao: ProfileDao,
    private val supabaseManager: SupabaseManager
) : ViewModel() {

    fun validateLogin(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            // 1. Check local database
            var profile = profileDao.getProfileByEmail(email)

            // 2. If not found locally, check Supabase cloud
            if (profile == null) {
                val remoteProfile = supabaseManager.fetchProfileByEmail(email)
                if (remoteProfile != null) {
                    val entity = ProfileEntity(
                        id = remoteProfile.optString("id"),
                        fullName = remoteProfile.optString("full_name"),
                        email = remoteProfile.optString("email"),
                        phone = remoteProfile.optString("phone"),
                        nationality = remoteProfile.optString("nationality"),
                        gender = remoteProfile.optString("gender"),
                        passportNumber = if (remoteProfile.has("passport_number") && !remoteProfile.isNull("passport_number")) remoteProfile.getString("passport_number") else null,
                        visaNumber = if (remoteProfile.has("visa_number") && !remoteProfile.isNull("visa_number")) remoteProfile.getString("visa_number") else null,
                        profilePhotoUrl = if (remoteProfile.has("profile_photo_url") && !remoteProfile.isNull("profile_photo_url")) remoteProfile.getString("profile_photo_url") else null,
                        createdAt = System.currentTimeMillis()
                    )
                    profileDao.insertProfile(entity)
                    profile = entity
                }
            }

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                if (profile != null) {
                    onSuccess()
                } else {
                    onError("Account does not exist. Please sign up.")
                }
            }
        }
    }
}
