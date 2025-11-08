package com.example.kiosk.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun HelpDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp), // 화면 높이의 약 80~90% 정도로 제한
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                // =================================================================
                // 1. 헤더 영역 (파란색 배경, 제목, 닫기 버튼)
                // =================================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2563EB)) // blue-600
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "사용 방법",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.background(Color.Transparent) // 닫기 버튼 배경 투명
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기",
                            tint = Color.White
                        )
                    }
                }

                // =================================================================
                // 2. 본문 영역 (스크롤 가능)
                // =================================================================
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()) // 세로 스크롤 활성화
                ) {
                    // 연습 모드 설명 섹션
                    HelpSection(
                        title = "📚 연습 모드",
                        items = listOf(
                            "• 화면에 나오는 안내를 따라 하세요",
                            "• 누를 버튼을 표시로 알려드립니다",
                            "• 천천히 따라하며 익숙해지세요",
                            "• 언제든 처음부터 다시 시작 가능"
                        ),
                        backgroundColor = Color(0xFFEFF6FF), // blue-50
                        borderColor = Color(0xFFBFDBFE)      // blue-200
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 실전 모드 설명 섹션
                    HelpSection(
                        title = "⚡ 실전 모드",
                        items = listOf(
                            "• 안내 없이 직접 주문해보세요",
                            "• 실제 키오스크처럼 작동합니다",
                            "• 연습 모드로 충분히 배운 후 도전",
                            "• 막히면 뒤로 가기를 눌러주세요"
                        ),
                        backgroundColor = Color(0xFFF0FDF4), // green-50
                        borderColor = Color(0xFFBBF7D0)      // green-200
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 팁 섹션
                    HelpSection(
                        title = "💡 키오스크 사용 팁",
                        items = listOf(
                            "• 화면을 손가락으로 가볍게 터치",
                            "• 주문할 메뉴를 차례대로 선택",
                            "• 수량을 조절할 수 있습니다",
                            "• 장바구니에서 주문 확인",
                            "• 결제 버튼으로 주문 완료"
                        ),
                        backgroundColor = Color(0xFFFEFCE8), // yellow-50
                        borderColor = Color(0xFFFEF08A)      // yellow-200
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 자주 묻는 질문 섹션
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)), // gray-50
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(2.dp, Color(0xFFE5E7EB)), // gray-200
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "❓ 자주 묻는 질문",
                                fontSize = 20.sp, // text-xl
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // QnA 아이템들
                            QnaItem(question = "잘못 눌렀어요", answer = "뒤로 가기나 취소 버튼을 누르세요")
                            Spacer(modifier = Modifier.height(16.dp))
                            QnaItem(question = "처음부터 다시 하고 싶어요", answer = "처음으로 버튼을 눌러주세요")
                            Spacer(modifier = Modifier.height(16.dp))
                            QnaItem(question = "진짜 결제가 되나요?", answer = "아니요, 연습용이라 실제 결제는 안됩니다")
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // 하단 격려 메시지
                    Text(
                        text = "천천히 연습하시면\n금방 익숙해지실 거예요! 💪",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = Color(0xFF6B7280), // gray-500
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

// =================================================================
// 3. 재사용을 위한 작은 컴포넌트들 (섹션, QnA 아이템)
// =================================================================

@Composable
private fun HelpSection(
    title: String,
    items: List<String>,
    backgroundColor: Color,
    borderColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(2.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 20.sp, // text-xl
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            items.forEach { item ->
                Text(
                    text = item,
                    color = Color(0xFF374151), // gray-700
                    fontSize = 18.sp,          // text-lg
                    modifier = Modifier.padding(vertical = 2.dp, horizontal = 16.dp) // ml-4 효과
                )
            }
        }
    }
}

@Composable
private fun QnaItem(question: String, answer: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // 왼쪽 파란색 바 (border-l-4 border-blue-400 효과)
        Box(
            modifier = Modifier
                .padding(end = 12.dp)
                .height(48.dp) // 대략적인 높이 설정
                .fillMaxWidth(0.01f) // 아주 얇게
                .background(Color(0xFF60A5FA)) // blue-400
        )
        Column {
            Text(
                text = "Q. $question",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151), // gray-700
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "A. $answer",
                fontSize = 16.sp,
                color = Color(0xFF4B5563)  // gray-600
            )
        }
    }
}