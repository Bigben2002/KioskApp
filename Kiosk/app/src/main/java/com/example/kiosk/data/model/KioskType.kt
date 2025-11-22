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
        themeColor = Color(0xFFDC2626),
        icon = "🍔",
        categories = listOf("버거", "사이드", "음료")
    ),
    CAFE(
        title = "카페",
        themeColor = Color(0xFF795548),
        icon = "☕️",
        categories = listOf("커피", "음료", "디저트")
    ),
    // 👇 새로 추가
    CINEMA(
        title = "영화관",
        themeColor = Color(0xFF334155), // slate-700
        icon = "🎬",
        categories = emptyList() // 메뉴형이 아니라 단계형 UI
    ),
    RESTAURANT(
        title = "식당",
        themeColor = Color(0xFF8B4513),
        icon = "🍱",
        categories = listOf("국밥류", "사이드", "음료")
    )
}
