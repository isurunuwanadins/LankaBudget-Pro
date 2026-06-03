package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goal_contributions")
data class GoalContribution(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val goalId: Int,
    val transactionId: Int,
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis()
)
