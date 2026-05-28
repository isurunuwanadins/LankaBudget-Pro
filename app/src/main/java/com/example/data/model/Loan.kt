package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loans")
data class Loan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val lenderName: String,
    val borrowedAmount: Double,
    val remainingAmount: Double,
    val isCleared: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
