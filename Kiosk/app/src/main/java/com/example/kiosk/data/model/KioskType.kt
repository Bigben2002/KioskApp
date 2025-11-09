package com.example.kiosk.data.model

import androidx.compose.ui.graphics.Color

enum class KioskType(
    val title: String,
    val themeColor: Color,
    val icon: String,
    val categories: List<String>
) {
    BURGER(
        title = "햄버거 가게",
        themeColor = Color(0xFFDC2626), // 빨간색
        icon = "🍔",
        categories = listOf("버거", "사이드", "음료")
    ),
    CAFE(
        title = "카페",
        themeColor = Color(0xFF795548), // 갈색
        icon = "☕️",
        categories = listOf("커피", "음료", "디저트")
    )
}