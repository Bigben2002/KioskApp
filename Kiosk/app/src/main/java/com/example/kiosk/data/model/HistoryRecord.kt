package com.example.kiosk.data.model

data class HistoryRecord(
    val id: String,
    val date: String,
    val mission: String,
    val success: Boolean,
    val userOrder: List<RequiredItem>,
    val timestamp: Long
)
