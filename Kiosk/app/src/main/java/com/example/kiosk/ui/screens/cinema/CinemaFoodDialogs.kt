// app/src/main/java/com/example/kiosk/ui/screens/cinema/CinemaFoodDialogs.kt
package com.example.kiosk.ui.screens.cinema

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.kiosk.data.model.CartItem
import com.example.kiosk.ui.components.ButtonVariant
import com.example.kiosk.ui.components.KioskButton
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CinemaCartDialog(
    cart: List<CartItem>,
    totalPrice: Int,
    onDismiss: () -> Unit,
    onInc: (Int) -> Unit,
    onDec: (Int) -> Unit,
    onClear: () -> Unit,
    onCheckout: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // 헤더
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("장바구니", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "닫기", tint = Color.Gray)
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

                // 본문 (스크롤)
                if (cart.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .padding(vertical = 32.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("장바구니가 비었습니다", fontSize = 18.sp, color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(cart.size) { index ->
                            val c = cart[index]
                            CartDialogRow(
                                name = c.menuItem.name,
                                price = c.menuItem.price,
                                qty = c.quantity,
                                onInc = { onInc(index) },
                                onDec = { onDec(index) }
                            )
                            if (index < cart.size - 1) Divider()
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // 합계
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("합계", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${NumberFormat.getNumberInstance(Locale.KOREA).format(totalPrice)}원",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(16.dp))

                // 하단 버튼
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KioskButton(
                        onClick = onClear,
                        variant = ButtonVariant.OUTLINE,
                        modifier = Modifier.weight(1f)
                    ) { Text("비우기") }
                    KioskButton(
                        onClick = onCheckout,
                        variant = ButtonVariant.DESTRUCTIVE,
                        enabled = cart.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) { Text("결제하기") }
                }
            }
        }
    }
}

@Composable
private fun CartDialogRow(
    name: String,
    price: Int,
    qty: Int,
    onInc: () -> Unit,
    onDec: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(name, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${price}원", fontSize = 12.sp, color = Color(0xFF6B7280))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.OutlinedButton(
                onClick = onDec,
                contentPadding = PaddingValues(0.dp)
            ) { Text("-") }
            Text(qty.toString(), modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
            androidx.compose.material3.OutlinedButton(
                onClick = onInc,
                contentPadding = PaddingValues(0.dp)
            ) { Text("+") }
        }
    }
}