package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "investments")
data class Investment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // "Money Market", "Fixed Deposit", "Stocks", "Crypto", "Other"
    val amountInvested: Double,
    val currentValue: Double,
    val expectedYield: Double, // Rate in % (e.g., 10.5)
    val remarks: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
