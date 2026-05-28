package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "repayment_logs")
data class RepaymentLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val loanId: Int,
    val amountPaid: Double,
    val timestamp: Long = System.currentTimeMillis()
)
