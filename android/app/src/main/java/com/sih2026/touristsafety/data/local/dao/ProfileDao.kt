package com.sih2026.touristsafety.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sih2026.touristsafety.data.local.entities.ProfileEntity

@Dao
interface ProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun getProfile(id: String): ProfileEntity?

    @Query("SELECT * FROM profiles WHERE email = :email LIMIT 1")
    suspend fun getProfileByEmail(email: String): ProfileEntity?

    @Query("SELECT * FROM profiles ORDER BY createdAt DESC LIMIT 1")
    suspend fun getCurrentProfile(): ProfileEntity?

    @Query("SELECT * FROM profiles ORDER BY createdAt DESC LIMIT 1")
    fun getCurrentProfileFlow(): kotlinx.coroutines.flow.Flow<ProfileEntity?>
}
