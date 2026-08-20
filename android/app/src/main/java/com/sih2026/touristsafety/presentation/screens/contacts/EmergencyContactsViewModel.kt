package com.sih2026.touristsafety.presentation.screens.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sih2026.touristsafety.data.local.dao.EmergencyContactDao
import com.sih2026.touristsafety.data.local.dao.ProfileDao
import com.sih2026.touristsafety.data.local.entities.EmergencyContactEntity
import com.sih2026.touristsafety.data.remote.SupabaseManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class EmergencyContactsViewModel @Inject constructor(
    private val contactDao: EmergencyContactDao,
    private val profileDao: ProfileDao,
    private val supabaseManager: SupabaseManager
) : ViewModel() {

    val contacts: StateFlow<List<EmergencyContactEntity>> = contactDao.getAllContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addContact(
        name: String,
        phone: String,
        relationship: String,
        countryCode: String,
        residentAddress: String?,
        isPrimary: Boolean
    ) {
        viewModelScope.launch {
            val currentProfile = profileDao.getCurrentProfile()
            val userId = currentProfile?.id ?: UUID.randomUUID().toString()
            val contactId = UUID.randomUUID().toString()

            val contact = EmergencyContactEntity(
                id = contactId,
                userId = userId,
                name = name,
                phone = phone,
                relationship = relationship,
                countryCode = countryCode,
                residentAddress = residentAddress,
                isPrimary = isPrimary
            )
            // Save locally
            contactDao.insertContact(contact)

            // Sync to Supabase
            supabaseManager.insertEmergencyContact(
                id = contactId,
                userId = userId,
                name = name,
                phone = phone,
                relationship = relationship,
                countryCode = countryCode,
                residentAddress = residentAddress,
                isPrimary = isPrimary
            )
        }
    }

    fun deleteContact(id: String) {
        viewModelScope.launch {
            val contact = contactDao.getContactById(id)
            if (contact != null) {
                contactDao.deleteContact(contact)
            }
        }
    }

    fun updateContact(contact: EmergencyContactEntity) {
        viewModelScope.launch {
            contactDao.insertContact(contact)
        }
    }

    fun setPrimary(id: String) {
        viewModelScope.launch {
            contacts.value.forEach { 
                if (it.id == id && !it.isPrimary) {
                    contactDao.updateContact(it.copy(isPrimary = true))
                } else if (it.id != id && it.isPrimary) {
                    contactDao.updateContact(it.copy(isPrimary = false))
                }
            }
        }
    }
}
