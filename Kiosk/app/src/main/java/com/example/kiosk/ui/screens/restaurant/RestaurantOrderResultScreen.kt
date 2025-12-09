package com.example.kiosk.ui.screens.restaurant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kiosk.data.model.CartItem
import com.example.kiosk.ui.components.KioskCard
import java.text.NumberFormat
import java.util.Locale
import com.example.kiosk.data.model.Mission

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantOrderResultScreen(
    result: String,
    cart: List<CartItem>,
    totalPrice: Int,
    mission: Mission?,
    viewModel: RestaurantKioskViewModel,
    onExit: () -> Unit
) {
    val themeColor = when (result) {
        "fail" -> Color(0xFFDC2626)
        else -> Color(0xFF16A34A)
    }

    val resultIcon = if (result == "fail") Icons.Default.Close else Icons.Default.Check
    val resultTitle = when (result) {
        "success" -> "미션 성공!"
        "fail" -> "미션 실패"
        else -> "주문 완료"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        resultTitle,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onExit) {
                        Icon(Icons.Default.Home, contentDescription = "홈으로", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = themeColor)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
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
                        resultIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (result == "success") "미션 성공! 🎉" else if (result == "fail") "미션 실패" else "주문 완료!",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = when (result) {
                    "success" -> "정확하게 주문하셨습니다!\n정말 잘하셨어요!"
                    "fail" -> "주문이 미션과 다릅니다"
                    else -> "주문이 접수되었습니다\n번호표를 받아 기다려주세요"
                },
                fontSize = 18.sp,
                color = Color(0xFF4B5563),
                textAlign = TextAlign.Center,
                lineHeight = 26.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (result == "fail" && mission != null) {
                KioskCard(
                    backgroundColor = Color(0xFFFEFCE8),
                    borderColor = Color(0xFFFEF08A),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "미션",
                            fontSize = 16.sp,
                            color = Color(0xFF854D0E),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(mission.text, fontSize = 18.sp, color = Color(0xFF713F12))
                    }
                }
            }

            // 영수증 카드
            KioskCard(
                backgroundColor = Color(0xFFF9FAFB),
                borderColor = Color(0xFFE5E7EB),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "주문 내역",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    cart.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${item.menuItem.name} × ${item.quantity}",
                                    fontSize = 18.sp,
                                    color = Color(0xFF374151),
                                    fontWeight = FontWeight.Medium
                                )

                                // 옵션 표시
                                if (item.selectedOption != null && item.selectedOption.price > 0) {
                                    val options = item.selectedOption.name.split(", ")
                                    options.forEach { opt ->
                                        if (!opt.contains("보통") && !opt.contains("수육 없음")) {
                                            Text(
                                                text = "  • $opt",
                                                fontSize = 14.sp,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Text(
                                "${NumberFormat.getNumberInstance(Locale.KOREA).format(
                                    (item.menuItem.price + (item.selectedOption?.price ?: 0)) * item.quantity
                                )}원",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF374151)
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

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

            Button(
                onClick = {
                    viewModel.reset()
                    onExit()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColor)
            ) {
                Text("처음으로", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}