package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "split_bills")
data class SplitBillEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val totalAmount: Double,
    val myShare: Double,
    val payerName: String,
    val membersJson: String, // format: "Alice|1000.0|unpaid,Bob|1000.0|paid"
    val timestamp: Long = System.currentTimeMillis()
)
