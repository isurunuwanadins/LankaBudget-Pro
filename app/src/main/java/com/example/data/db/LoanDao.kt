package com.example.data.db

import androidx.room.*
import com.example.data.model.Loan
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    @Query("SELECT * FROM loans ORDER BY timestamp DESC")
    fun getAllLoans(): Flow<List<Loan>>

    @Query("SELECT * FROM loans WHERE id = :id LIMIT 1")
    suspend fun getLoanById(id: Int): Loan?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: Loan)

    @Update
    suspend fun updateLoan(loan: Loan)

    @Delete
    suspend fun deleteLoan(loan: Loan)

    @Query("DELETE FROM loans")
    suspend fun clearAllLoans()
}
