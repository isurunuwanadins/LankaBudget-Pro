package com.example.data.db

import androidx.room.*
import com.example.data.model.Investment
import kotlinx.coroutines.flow.Flow

@Dao
interface InvestmentDao {
    @Query("SELECT * FROM investments ORDER BY timestamp DESC")
    fun getAllInvestments(): Flow<List<Investment>>

    @Query("SELECT * FROM investments WHERE id = :id LIMIT 1")
    suspend fun getInvestmentById(id: Int): Investment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvestment(investment: Investment)

    @Update
    suspend fun updateInvestment(investment: Investment)

    @Delete
    suspend fun deleteInvestment(investment: Investment)

    @Query("DELETE FROM investments")
    suspend fun clearAllInvestments()
}
