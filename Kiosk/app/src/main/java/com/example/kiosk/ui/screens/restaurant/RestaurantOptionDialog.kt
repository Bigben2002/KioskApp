package com.example.kiosk.ui.screens.restaurant

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.NumberFormat
import java.util.Locale
import com.example.kiosk.data.model.MenuItem
import com.example.kiosk.data.model.ItemOption

@Composable
fun RestaurantOptionDialog(
    menuItem: MenuItem,
    themeColor: Color,
    onDismiss: () -> Unit,
    onAddToCart: (MenuItem, ItemOption?, ItemOption?) -> Unit
) {
    var selectedSpecialOption by remember {
        mutableStateOf(
            if (menuItem.options.isNotEmpty()) menuItem.options.first() else null
        )
    }

    var selectedPorkOption by remember { mutableStateOf<ItemOption?>(null) }

    val isGukbap = menuItem.category == "국밥류"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "${menuItem.name} 옵션 선택",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 특 옵션 (돼지국밥, 순대국밥만)
                if (menuItem.options.isNotEmpty()) {
                    Text("양 선택", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))

                    menuItem.options.forEach { option ->
                        OptionRow(
                            option = option,
                            isSelected = option == selectedSpecialOption,
                            themeColor = themeColor,
                            onClick = { selectedSpecialOption = option }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 수육 추가 옵션 (모든 국밥류)
                if (isGukbap) {
                    Text("수육 추가", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))

                    val porkOptions = listOf(
                        ItemOption("수육 없음", 0),
                        ItemOption("수육 추가 (+5,000원)", 5000)
                    )

                    porkOptions.forEach { option ->
                        OptionRow(
                            option = option,
                            isSelected = option == selectedPorkOption,
                            themeColor = themeColor,
                            onClick = {
                                selectedPorkOption = if (selectedPorkOption == option) null else option
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE5E7EB),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("취소")
                    }
                    Button(
                        onClick = {
                            onAddToCart(menuItem, selectedSpecialOption, selectedPorkOption)
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        val specialPrice = selectedSpecialOption?.price ?: 0
                        val porkPrice = selectedPorkOption?.price ?: 0
                        val finalPrice = menuItem.price + specialPrice + porkPrice
                        Text("${NumberFormat.getNumberInstance(Locale.KOREA).format(finalPrice)}원 담기")
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionRow(
    option: ItemOption,
    isSelected: Boolean,
    themeColor: Color,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) themeColor else Color(0xFFE5E7EB)
    val backgroundColor = if (isSelected) themeColor.copy(alpha = 0.1f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, RoundedCornerShape(8.dp))
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            option.name,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) themeColor else Color.Black
        )
        if (option.price > 0) {
            Text(
                "+${NumberFormat.getNumberInstance(Locale.KOREA).format(option.price)}원",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}