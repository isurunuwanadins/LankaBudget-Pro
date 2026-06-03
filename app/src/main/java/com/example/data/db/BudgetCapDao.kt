package com.example.data.db

import androidx.room.*
import com.example.data.model.BudgetCapEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetCapDao {
    @Query("SELECT * FROM budget_caps ORDER BY year DESC, month DESC")
    fun getAllBudgetCaps(): Flow<List<BudgetCapEntity>>

    @Query("SELECT * FROM budget_caps WHERE month = :month AND year = :year")
    fun getBudgetCapsForPeriod(month: Int, year: Int): Flow<List<BudgetCapEntity>>

    @Query("SELECT * FROM budget_caps WHERE categoryId = :categoryId AND month = :month AND year = :year LIMIT 1")
    suspend fun getBudgetCap(categoryId: String, month: Int, year: Int): BudgetCapEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetCap(budgetCap: BudgetCapEntity)

    @Query("DELETE FROM budget_caps WHERE id = :id")
    suspend fun deleteBudgetCap(id: Int)

    @Query("DELETE FROM budget_caps WHERE categoryId = :categoryId AND month = :month AND year = :year")
    suspend fun deleteBudgetCapByKeys(categoryId: String, month: Int, year: Int)
}
