package com.example.kiosk.ui.screens.cafe

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
    menuItem: MenuItem,          // 선택한 메뉴 정보
    themeColor: Color,           // 앱 테마 색상 (갈색)
    onDismiss: () -> Unit,       // 닫기 버튼 동작
    onAddToCart: (List<ItemOption>, Int) -> Unit // 장바구니 담기 완료 시 동작
) {
    // [해석] 1. 초기 옵션 선택 상태
    // 옵션이 있다면 첫 번째(보통 HOT 또는 기본값)를 기본으로 선택해둡니다.
    // 사용자가 아무것도 선택 안 하고 담는 실수를 방지합니다.
    var selectedOptions by remember {
        mutableStateOf(if (menuItem.options.isNotEmpty()) listOf(menuItem.options.first()) else emptyList())
    }

    var quantity by remember { mutableStateOf(1) } // 수량 (기본 1개)

    // [해석] 2. "HOT"이 선택되었는지 감시하는 변수
    // 이 값이 true면 '얼음' 관련 옵션들을 비활성화할 것입니다.
    val isHotSelected = selectedOptions.any { it.name.contains("HOT", ignoreCase = true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 700.dp), // 내용이 많아도 화면을 꽉 채우지 않도록 제한
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // === 상단 헤더 (메뉴 이름 + 닫기 버튼) ===
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

                // === 옵션 리스트 영역 ===
                // 옵션이 많을 경우를 대비해 스크롤 가능하게(verticalScroll) 설정
                Column(
                    modifier = Modifier
                        .weight(1f) // 남은 공간을 차지
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    menuItem.options.forEach { option ->
                        val isSelected = selectedOptions.contains(option)

                        // "얼음"이 들어간 옵션인지 확인
                        val isIceOption = option.name.contains("얼음")

                        // [해석] 비활성화 로직: HOT이 선택된 상태에서 이 옵션이 얼음 관련이면 -> 선택 불가(false)
                        val isEnabled = !(isHotSelected && isIceOption)

                        // [해석] 색상 로직: HOT은 빨강, ICE는 파랑, 나머지는 테마색
                        val optionColor = when {
                            option.name.contains("HOT", ignoreCase = true) -> Color(0xFFDC2626) // Red
                            option.name.contains("ICE", ignoreCase = true) -> Color(0xFF2563EB) // Blue
                            else -> themeColor
                        }

                        // 선택되었을 때만 테두리와 배경색을 칠함
                        val borderColor = if (isSelected) optionColor else Color(0xFFE5E7EB)
                        val backgroundColor = if (isSelected) optionColor.copy(alpha = 0.1f) else Color.White
                        // 비활성화되면 글자색을 연하게
                        val textColor = if (!isEnabled) Color.LightGray else if (isSelected) optionColor else Color.Black

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(if (isEnabled) 1f else 0.3f) // 비활성화 시 흐릿하게 처리
                                .clickable(enabled = isEnabled) {
                                    // === [핵심] 스마트 선택 로직 ===
                                    selectedOptions = if (isSelected) {
                                        // 이미 선택된 거라면 -> 해제 (리스트에서 제거)
                                        selectedOptions - option
                                    } else {
                                        // 새로 선택하는 경우 -> 다른 옵션과의 충돌 검사
                                        if (option.name.contains("HOT")) {
                                            // 1. HOT 선택 시: 기존 ICE나 얼음 옵션은 다 뺌 + HOT 추가
                                            selectedOptions.filter { !it.name.contains("ICE") && !it.name.contains("얼음") } + option
                                        } else if (option.name.contains("ICE")) {
                                            // 2. ICE 선택 시: HOT은 뺌 + ICE 추가
                                            selectedOptions.filter { !it.name.contains("HOT") } + option
                                        } else if (option.name.contains("얼음 추가")) {
                                            // 3. 얼음 조절: '추가', '빼기', '적게' 셋 중 하나만 선택되도록 기존 얼음옵션 제거
                                            selectedOptions.filter { !it.name.contains("얼음 빼기") && !it.name.contains("얼음 적게") } + option
                                        } else if (option.name.contains("얼음 빼기")) {
                                            selectedOptions.filter { !it.name.contains("얼음 추가") && !it.name.contains("얼음 적게") } + option
                                        } else if (option.name.contains("얼음 적게")) {
                                            selectedOptions.filter { !it.name.contains("얼음 추가") && !it.name.contains("얼음 빼기") } + option
                                        } else {
                                            // 4. 그 외(샷 추가 등): 그냥 추가 (중복 가능)
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
                            // 옵션 이름
                            Text(
                                text = option.name,
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = textColor
                            )
                            // 옵션 가격 (0원이면 표시 안 함)
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

                // === 3. 수량 조절 영역 ===
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
                        // (-) 버튼
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

                        // 숫자 표시
                        Text(
                            text = "$quantity",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.widthIn(min = 32.dp),
                            textAlign = TextAlign.Center
                        )

                        // (+) 버튼
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

                // === 4. 담기 버튼 (최종 가격 계산) ===
                // 옵션 가격 총합
                val optionsTotal = selectedOptions.sumOf { it.price }
                // 최종 가격 = (메뉴 기본가 + 옵션가) * 수량
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