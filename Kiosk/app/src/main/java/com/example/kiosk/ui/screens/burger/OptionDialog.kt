package com.example.kiosk.ui.screens.burger

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

@Composable
fun BurgerOptionDialog(
    menuItem: MenuItem,
    themeColor: Color,
    onDismiss: () -> Unit,
    onAddToCart: (MenuItem, ItemOption) -> Unit
) {
    var selectedOption by remember { mutableStateOf(menuItem.options.first()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("${menuItem.name} 옵션 선택", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                menuItem.options.forEach { option ->
                    OptionRow(
                        option = option,
                        isSelected = option == selectedOption,
                        themeColor = themeColor,
                        onClick = { selectedOption = option }
                    )
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
                        onClick = { onAddToCart(menuItem, selectedOption) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        val finalPrice = menuItem.price + selectedOption.priceDelta
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
    val backgroundColor = if (isSelected) themeColor.copy(alpha = 0.05f) else Color.Transparent

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
        if (option.priceDelta > 0) {
            Text(
                "+${NumberFormat.getNumberInstance(Locale.KOREA).format(option.priceDelta)}원",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}