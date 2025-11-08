package com.example.kiosk.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.kiosk.data.model.HistoryRecord
import com.example.kiosk.data.repository.HistoryRepository
import java.text.NumberFormat
import java.util.Locale

@Composable
fun LearningHistoryDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    // Repository를 통해 저장된 기록 불러오기
    val historyRepository = remember { HistoryRepository(context) }
    val history = remember { historyRepository.getAllHistory() }

    // 통계 계산
    val totalCount = history.size
    val successCount = history.count { it.success }
    val successRate = if (totalCount > 0) (successCount.toFloat() / totalCount * 100).toInt() else 0

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 700.dp), // 화면 높이의 약 80~90% 제한
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                // =================================================================
                // 1. 헤더 영역 (보라색 배경)
                // =================================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF9333EA)) // purple-600
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "학습 기록",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기",
                            tint = Color.White
                        )
                    }
                }

                // =================================================================
                // 2. 통계 영역 (기록이 있을 때만 표시)
                // =================================================================
                if (totalCount > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFAF5FF)) // purple-50
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.EmojiEvents, // Trophy 대체
                            iconColor = Color(0xFF9333EA),    // purple-600
                            value = "$successCount",
                            label = "성공"
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.TrendingUp,
                            iconColor = Color(0xFF9333EA),    // purple-600
                            value = "$totalCount",
                            label = "총 시도"
                        )
                        // 성공률 카드는 아이콘 대신 텍스트 이모지 사용 예시
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(2.dp, Color(0xFFE9D5FF)) // purple-200
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("📊", fontSize = 24.sp, modifier = Modifier.padding(bottom = 4.dp))
                                Text("$successRate%", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text("성공률", fontSize = 12.sp, color = Color(0xFF4B5563)) // gray-600
                            }
                        }
                    }
                }

                // =================================================================
                // 3. 기록 리스트 영역 (LazyColumn)
                // =================================================================
                if (history.isEmpty()) {
                    // 기록 없음 상태
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false) // 남은 공간 차지하되 너무 늘어나지 않게
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFF3F4F6), // gray-100
                                modifier = Modifier.size(80.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("📝", fontSize = 40.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "아직 학습 기록이 없습니다",
                                fontSize = 18.sp,
                                color = Color(0xFF4B5563), // gray-600
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "실전 모드로 연습하면\n기록이 저장됩니다",
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280), // gray-500
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // 기록 리스트
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f, fill = false) // 다이얼로그 최대 높이 내에서 스크롤
                            .padding(horizontal = 16.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(history) { record ->
                            HistoryItemCard(record)
                        }
                    }
                }
            }
        }
    }
}

// 통계 카드 컴포넌트
@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color,
    value: String,
    label: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, Color(0xFFE9D5FF)) // purple-200
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier
                    .size(24.dp)
                    .padding(bottom = 4.dp)
            )
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF4B5563) // gray-600
            )
        }
    }
}

// 개별 기록 아이템 카드 컴포넌트
@Composable
private fun HistoryItemCard(record: HistoryRecord) {
    // 성공 여부에 따른 색상 설정
    val backgroundColor = if (record.success) Color(0xFFF0FDF4) else Color(0xFFFEF2F2) // green-50 / red-50
    val borderColor = if (record.success) Color(0xFFBBF7D0) else Color(0xFFFECACA)     // green-200 / red-200
    val iconColor = if (record.success) Color(0xFF22C55E) else Color(0xFFEF4444)       // green-500 / red-500
    val iconVector = if (record.success) Icons.Default.Check else Icons.Default.Close
    val badgeText = if (record.success) "성공" else "실패"
    val badgeColor = if (record.success) Color(0xFF0F172A) else Color(0xFFEF4444)      // slate-900 (primary) / red-500 (destructive)

    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(2.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 상단: 아이콘, 배지, 날짜
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 성공/실패 원형 아이콘
                    Surface(
                        shape = CircleShape,
                        color = iconColor,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    // 성공/실패 배지
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = badgeColor,
                    ) {
                        Text(
                            text = badgeText,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                // 날짜
                Text(
                    text = record.date,
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280) // gray-500
                )
            }

            // 미션 내용
            Text(
                text = "미션",
                fontSize = 14.sp,
                color = Color(0xFF4B5563), // gray-600
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = record.mission,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 주문 내역 (있을 경우만 표시)
            if (record.userOrder.isNotEmpty()) {
                Text(
                    text = "주문 내역",
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563), // gray-600
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                // 주문 목록을 쉼표로 연결해서 표시
                val orderText = record.userOrder.joinToString(", ") { item ->
                    "${item.name} ${item.quantity}개"
                }
                Text(
                    text = orderText,
                    fontSize = 14.sp,
                    color = Color(0xFF1F2937) // gray-800
                )
            }
        }
    }
}