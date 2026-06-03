package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_caps")
data class BudgetCapEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoryId: String, // E.g., category name / id
    val month: Int,        // 1-12
    val year: Int,         // e.g., 2026
    val allocatedAmount: Double
)
