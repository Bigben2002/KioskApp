package com.example.kiosk.data.model

data class MenuItem(
    val id: String,
    val name: String,
    val price: Int,
    val category: String,
    val options: List<ItemOption> = emptyList(),
    val imageResId: Int? = null
)
