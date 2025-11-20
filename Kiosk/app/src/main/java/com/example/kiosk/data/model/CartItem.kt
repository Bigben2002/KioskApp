package com.example.kiosk.data.model

data class CartItem(
    val menuItem: MenuItem,
    var quantity: Int,
    val selectedOption: ItemOption? = null,
    val selectedOptions: List<ItemOption> = emptyList()
)
