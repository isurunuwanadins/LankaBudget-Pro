package com.example.data.model

data class DashboardWidget(
    val id: String, // "BALANCE", "NET_WORTH", "ALLOCATION", "CHART", "AUDIT", "TRANSACTIONS"
    val title: String,
    val isEnabled: Boolean = true,
    val isHalfWidth: Boolean = false,
    val heightScale: Float = 1.0f,
    val glassColorTint: String? = "White",
    val order: Int,
    val gridWidth: Int = if (id == "CHART") 5 else 5,
    val gridHeight: Int = when (id) {
        "BALANCE" -> 2
        "NET_WORTH" -> 3
        "ALLOCATION" -> 2
        "CHART" -> 3
        "AUDIT" -> 2
        "TRANSACTIONS" -> 3
        else -> 2
    }
)
