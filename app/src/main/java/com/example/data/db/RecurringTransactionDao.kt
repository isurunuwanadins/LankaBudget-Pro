package com.example.data.db

import androidx.room.*
import com.example.data.model.RecurringTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringTransactionDao {
    @Query("SELECT * FROM recurring_transactions ORDER BY id DESC")
    fun getAllRecurringTransactions(): Flow<List<RecurringTransaction>>

    @Query("SELECT * FROM recurring_transactions")
    suspend fun getRecurringTransactionsList(): List<RecurringTransaction>

    @Query("SELECT * FROM recurring_transactions WHERE id = :id")
    suspend fun getRecurringTransactionById(id: Int): RecurringTransaction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringTransaction(recurring: RecurringTransaction): Long

    @Update
    suspend fun updateRecurringTransaction(recurring: RecurringTransaction)

    @Delete
    suspend fun deleteRecurringTransaction(recurring: RecurringTransaction)

    @Query("DELETE FROM recurring_transactions")
    suspend fun clearAllRecurringTransactions()
}
