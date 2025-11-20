package com.example.kiosk.data.model

data class Mission(
    val text: String,
    val required: List<RequiredItem>
)

data class RequiredItem(
    val name: String,
    val quantity: Int,
    val option: String? = null
)

