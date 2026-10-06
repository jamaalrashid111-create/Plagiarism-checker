package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.PlagiarismCheckEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlagiarismDao {
    @Query("SELECT * FROM plagiarism_checks ORDER BY timestamp DESC")
    fun getAllChecks(): Flow<List<PlagiarismCheckEntity>>

    @Query("SELECT * FROM plagiarism_checks WHERE id = :id LIMIT 1")
    suspend fun getCheckById(id: String): PlagiarismCheckEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheck(check: PlagiarismCheckEntity)

    @Query("DELETE FROM plagiarism_checks WHERE id = :id")
    suspend fun deleteCheck(id: String)

    @Query("DELETE FROM plagiarism_checks")
    suspend fun clearAllChecks()
}
