package com.example.data.db

import androidx.room.*
import com.example.data.model.SplitBillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SplitBillDao {
    @Query("SELECT * FROM split_bills ORDER BY timestamp DESC")
    fun getAllSplitBills(): Flow<List<SplitBillEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplitBill(splitBill: SplitBillEntity): Long

    @Update
    suspend fun updateSplitBill(splitBill: SplitBillEntity)

    @Delete
    suspend fun deleteSplitBill(splitBill: SplitBillEntity)

    @Query("DELETE FROM split_bills")
    suspend fun clearAllSplitBills()
}
