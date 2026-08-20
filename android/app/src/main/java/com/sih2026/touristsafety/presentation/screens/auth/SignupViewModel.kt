package com.sih2026.touristsafety.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sih2026.touristsafety.data.local.dao.ProfileDao
import com.sih2026.touristsafety.data.local.entities.ProfileEntity
import com.sih2026.touristsafety.data.remote.SupabaseManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val profileDao: ProfileDao,
    private val supabaseManager: SupabaseManager
) : ViewModel() {

    fun saveProfile(
        fullName: String,
        email: String,
        phone: String,
        nationality: String,
        gender: String,
        password: String = "test123",
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            // 1. Register with Supabase Auth & insert into profiles table
            val remoteId = supabaseManager.signUpUser(
                fullName = fullName,
                email = email,
                phone = phone,
                nationality = nationality,
                gender = gender,
                password = password
            )
            val userId = remoteId ?: UUID.randomUUID().toString()

            val entity = ProfileEntity(
                id = userId,
                fullName = fullName,
                email = email,
                phone = phone,
                nationality = nationality,
                gender = gender,
                passportNumber = null,
                visaNumber = null,
                profilePhotoUrl = null,
                createdAt = System.currentTimeMillis()
            )
            // 2. Save to local database (Offline-first)
            profileDao.insertProfile(entity)

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                onSuccess()
            }
        }
    }
}
