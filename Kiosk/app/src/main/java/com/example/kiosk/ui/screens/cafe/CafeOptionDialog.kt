package com.example.kiosk.ui.screens.cafe

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.kiosk.data.model.ItemOption
import com.example.kiosk.data.model.MenuItem
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CafeOptionDialog(
    menuItem: MenuItem,
    themeColor: Color,
    onDismiss: () -> Unit,
    onAddToCart: (List<ItemOption>, Int) -> Unit
) {
    // 기본 선택: 옵션이 있으면 첫번째거, 없으면 빈 리스트
    var selectedOptions by remember {
        mutableStateOf(if (menuItem.options.isNotEmpty()) listOf(menuItem.options.first()) else emptyList())
    }

    var quantity by remember { mutableStateOf(1) }

    // HOT 선택 여부 실시간 감지
    val isHotSelected = selectedOptions.any { it.name.contains("HOT", ignoreCase = true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 700.dp), // 높이 넉넉하게
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // 1. 헤더 (제목 + 닫기)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = menuItem.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "닫기", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                Text("옵션 선택", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B5563))
                Spacer(modifier = Modifier.height(12.dp))

                // 2. 옵션 리스트 (스크롤 가능하게 설정)
                Column(
                    modifier = Modifier
                        .weight(1f) // 남은 공간을 차지
                        .verticalScroll(rememberScrollState()), // 스크롤 가능
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    menuItem.options.forEach { option ->
                        val isSelected = selectedOptions.contains(option)
                        val isIceOption = option.name.contains("얼음")
                        // HOT이면 얼음 옵션 비활성화
                        val isEnabled = !(isHotSelected && isIceOption)

                        // 색상 로직
                        val optionColor = when {
                            option.name.contains("HOT", ignoreCase = true) -> Color(0xFFDC2626)
                            option.name.contains("ICE", ignoreCase = true) -> Color(0xFF2563EB)
                            else -> themeColor
                        }

                        val borderColor = if (isSelected) optionColor else Color(0xFFE5E7EB)
                        val backgroundColor = if (isSelected) optionColor.copy(alpha = 0.1f) else Color.White
                        val textColor = if (!isEnabled) Color.LightGray else if (isSelected) optionColor else Color.Black

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(if (isEnabled) 1f else 0.3f)
                                .clickable(enabled = isEnabled) {
                                    selectedOptions = if (isSelected) {
                                        selectedOptions - option
                                    } else {
                                        // [스마트 선택 로직]
                                        if (option.name.contains("HOT")) {
                                            // HOT 누르면 ICE랑 얼음 관련 다 뺌
                                            selectedOptions.filter { !it.name.contains("ICE") && !it.name.contains("얼음") } + option
                                        } else if (option.name.contains("ICE")) {
                                            // ICE 누르면 HOT 뺌
                                            selectedOptions.filter { !it.name.contains("HOT") } + option
                                        } else if (option.name.contains("얼음 추가")) {
                                            // 얼음 3형제 중 하나만 선택
                                            selectedOptions.filter { !it.name.contains("얼음 빼기") && !it.name.contains("얼음 적게") } + option
                                        } else if (option.name.contains("얼음 빼기")) {
                                            selectedOptions.filter { !it.name.contains("얼음 추가") && !it.name.contains("얼음 적게") } + option
                                        } else if (option.name.contains("얼음 적게")) {
                                            selectedOptions.filter { !it.name.contains("얼음 추가") && !it.name.contains("얼음 빼기") } + option
                                        } else {
                                            // 그 외 (샷 추가 등)
                                            selectedOptions + option
                                        }
                                    }
                                }
                                .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
                                .background(backgroundColor, RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option.name,
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = textColor
                            )
                            if (option.price > 0) {
                                Text(
                                    "+${NumberFormat.getNumberInstance(Locale.KOREA).format(option.price)}원",
                                    color = if (!isEnabled) Color.LightGray else Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // 3. 수량 조절 영역 (여기가 안 보였던 부분입니다!)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("수량", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 마이너스 버튼
                        FilledIconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            modifier = Modifier.size(44.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color(0xFFF3F4F6),
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(Icons.Default.Remove, "감소")
                        }

                        Text(
                            text = "$quantity",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.widthIn(min = 32.dp),
                            textAlign = TextAlign.Center
                        )

                        // 플러스 버튼
                        FilledIconButton(
                            onClick = { quantity++ },
                            modifier = Modifier.size(44.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color(0xFFF3F4F6),
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(Icons.Default.Add, "증가")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 4. 담기 버튼
                val optionsTotal = selectedOptions.sumOf { it.price }
                val finalPrice = (menuItem.price + optionsTotal) * quantity

                Button(
                    onClick = { onAddToCart(selectedOptions, quantity) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = themeColor)
                ) {
                    Text(
                        "${NumberFormat.getNumberInstance(Locale.KOREA).format(finalPrice)}원 담기",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}