package com.sih2026.touristsafety.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sih2026.touristsafety.data.local.entities.DocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity)

    @Delete
    suspend fun deleteDocument(document: DocumentEntity)

    @Query("SELECT * FROM documents WHERE userId = :userId")
    fun getDocumentsForUser(userId: String): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentById(id: String): DocumentEntity?

    @Query("SELECT * FROM documents")
    fun getAllDocuments(): Flow<List<DocumentEntity>>
}
