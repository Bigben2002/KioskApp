// app/src/main/java/com/example/kiosk/ui/screens/cinema/CinemaFoodScreens.kt
package com.example.kiosk.ui.screens.cinema

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kiosk.data.model.CartItem
import com.example.kiosk.data.model.MenuItem
import com.example.kiosk.ui.components.ButtonVariant
import com.example.kiosk.ui.components.KioskButton
import com.example.kiosk.ui.components.KioskCard
import java.text.NumberFormat
import java.util.Locale

// ------------------------------------------------------------
// 음식 메뉴 선택 화면
// ------------------------------------------------------------
@Composable
fun FoodMenuScreen(
    categories: List<String>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    items: List<MenuItem>,
    onAdd: (MenuItem) -> Unit,
    totalCount: Int,
    totalPrice: Int,
    onShowCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (totalCount > 0) {
                BottomAppBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    KioskButton(
                        onClick = onShowCart, // 팝업 열기
                        variant = ButtonVariant.DESTRUCTIVE,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .shadow(2.dp, RoundedCornerShape(8.dp))
                    ) {
                        Text("장바구니 보기 (${totalCount}개 · ${totalPrice}원)")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 상단 카테고리 칩
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (i in 0 until categories.size) {
                    val cat = categories[i]
                    val selected = cat == selectedCategory
                    Surface(
                        onClick = { onSelectCategory(cat) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (selected) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                        contentColor = if (selected) Color.White else Color(0xFF0F172A)
                    ) {
                        Text(
                            text = cat,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            fontSize = 14.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // 메뉴 그리드
            KioskCard(modifier = Modifier.fillMaxSize()) {
                Column(Modifier.padding(12.dp)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(items) { m ->
                            MenuCard(m = m, onAdd = { onAdd(m) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuCard(m: MenuItem, onAdd: () -> Unit) {
    KioskCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) { Text("🍿", fontSize = 28.sp) } // 텍스트 이모지

            Spacer(Modifier.height(8.dp))
            Text(m.name, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${m.price}원", fontSize = 12.sp, color = Color(0xFF6B7280))
            Spacer(Modifier.height(8.dp))
            KioskButton(onClick = onAdd, modifier = Modifier.fillMaxWidth()) { Text("담기") }
        }
    }
}

// ------------------------------------------------------------
// 음식 결제 완료 영수증 화면
// ------------------------------------------------------------
@Composable
fun FoodPaymentSuccessScreen(
    cart: List<CartItem>,
    totalPrice: Int,
    onDone: () -> Unit,
    onAgain: () -> Unit
) {
    val themeColor = Color(0xFF16A34A) // Green

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Surface(
            shape = CircleShape,
            color = themeColor,
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "결제 완료!",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "주문이 완료되었습니다.\n준비가 완료되면 알려드리겠습니다.",
            fontSize = 18.sp,
            color = Color(0xFF4B5563), // gray-600
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        // 영수증 카드
        KioskCard(
            backgroundColor = Color(0xFFF9FAFB), // gray-50
            borderColor = Color(0xFFE5E7EB),     // gray-200
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "주문 내역",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // --- 항목 ---
                cart.forEach { item ->
                    val itemPrice = (item.menuItem.price + (item.selectedOption?.price ?: 0)) * item.quantity
                    ReceiptRow( // CinemaPaymentScreens.kt 에 있는 ReceiptRow 재사용
                        label = "${item.menuItem.name} x${item.quantity}",
                        value = "${NumberFormat.getNumberInstance(Locale.KOREA).format(itemPrice)}원"
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // --- 총 금액 ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("총 금액", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${NumberFormat.getNumberInstance(Locale.KOREA).format(totalPrice)}원",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 하단 버튼
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onAgain, modifier = Modifier.height(52.dp)) { Text("다시 홈으로") }
            KioskButton(onClick = onDone, modifier = Modifier.height(52.dp)) { Text("완료 (종료)") }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}