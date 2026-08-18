package com.sih2026.touristsafety.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sih2026.touristsafety.data.local.entities.EmergencyContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EmergencyContactEntity)

    @Update
    suspend fun updateContact(contact: EmergencyContactEntity)

    @Delete
    suspend fun deleteContact(contact: EmergencyContactEntity)

    @Query("SELECT * FROM emergency_contacts WHERE userId = :userId")
    fun getContactsForUser(userId: String): Flow<List<EmergencyContactEntity>>

    @Query("SELECT * FROM emergency_contacts WHERE id = :id")
    suspend fun getContactById(id: String): EmergencyContactEntity?

    @Query("SELECT * FROM emergency_contacts")
    fun getAllContacts(): Flow<List<EmergencyContactEntity>>
}
