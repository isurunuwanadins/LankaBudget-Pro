package com.example.data.db

import androidx.room.*
import com.example.data.model.Goal
import com.example.data.model.GoalContribution
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM financial_goals ORDER BY targetDate ASC")
    fun getAllGoals(): Flow<List<Goal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal): Long

    @Delete
    suspend fun deleteGoal(goal: Goal)

    @Query("DELETE FROM financial_goals")
    suspend fun clearAllGoals()

    // Contributions
    @Query("SELECT * FROM goal_contributions WHERE goalId = :goalId")
    fun getContributionsForGoal(goalId: Int): Flow<List<GoalContribution>>

    @Query("SELECT SUM(amount) FROM goal_contributions WHERE goalId = :goalId")
    fun getContributionsSumForGoal(goalId: Int): Flow<Double?>

    @Query("SELECT * FROM goal_contributions")
    fun getAllContributions(): Flow<List<GoalContribution>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContribution(contribution: GoalContribution): Long

    @Query("DELETE FROM goal_contributions WHERE goalId = :goalId")
    suspend fun deleteContributionsForGoal(goalId: Int)
}
