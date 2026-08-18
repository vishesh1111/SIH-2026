package com.sih2026.touristsafety.presentation.screens.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sih2026.touristsafety.data.local.dao.EmergencyContactDao
import com.sih2026.touristsafety.data.local.entities.EmergencyContactEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class EmergencyContactsViewModel @Inject constructor(
    private val contactDao: EmergencyContactDao
) : ViewModel() {

    // Using a dummy user ID for now
    private val userId = "default_user"

    val contacts: StateFlow<List<EmergencyContactEntity>> = contactDao.getContactsForUser(userId)
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
            val contact = EmergencyContactEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = name,
                phone = phone,
                relationship = relationship,
                countryCode = countryCode,
                residentAddress = residentAddress,
                isPrimary = isPrimary
            )
            contactDao.insertContact(contact)
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
