package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_transactions")
data class RecurringTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val isIncome: Boolean,
    val bucket: String, // "NEEDS", "WANTS", "SAVINGS" for expenses; "INCOME" for income
    val category: String,
    val recurrencePeriod: String, // "DAILY", "WEEKLY", "MONTHLY", "YEARLY"
    val startDate: Long = System.currentTimeMillis(),
    val lastGeneratedDate: Long = 0L
)
