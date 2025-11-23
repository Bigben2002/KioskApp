package com.example.kiosk.data.model

data class HistoryRecord(
    val id: String = "",
    val date: String = "",
    val mission: String = "",
    val success: Boolean = false,
    val userOrder: List<RequiredItem> = emptyList(),
    val timestamp: Long = 0L
)
