package com.example.kiosk.data.model

data class HistoryRecord(
    val id: String,
    val date: String,
    val mission: String,
    val success: Boolean,
    val userOrder: List<RequiredItem>,
    val timestamp: Long,
    // 👇 영화관 미션 결과를 위한 필드 추가
    val cinemaSuccessStatus: String? = null // "1/1 (100%)" 형태로 저장
)
