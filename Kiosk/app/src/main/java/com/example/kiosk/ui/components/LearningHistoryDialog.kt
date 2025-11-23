package com.example.kiosk.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kiosk.data.model.HistoryRecord
import com.example.kiosk.ui.viewmodel.HistoryViewModel

@Composable
fun LearningHistoryDialog(
    onDismiss: () -> Unit,
    // 1. ViewModel 주입 (자동 생성)
    viewModel: HistoryViewModel = viewModel()
) {
    // 2. ViewModel의 데이터를 구독 (데이터가 바뀌면 화면도 바뀜)
    val history by viewModel.history.collectAsState()

    // 3. 화면이 켜질 때마다 데이터 새로고침 요청
    LaunchedEffect(Unit) {
        viewModel.fetchHistory()
    }

    // 4. 통계 계산 (history 변수 사용)
    val totalCount = history.size
    val successCount = history.count { it.success }
    val successRate = if (totalCount > 0) (successCount.toFloat() / totalCount * 100).toInt() else 0

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 700.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                // [헤더]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF9333EA))
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("학습 기록", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "닫기", tint = Color.White)
                    }
                }

                // [통계]
                if (totalCount > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFAF5FF))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(Modifier.weight(1f), Icons.Default.EmojiEvents, Color(0xFF9333EA), "$successCount", "성공")
                        StatCard(Modifier.weight(1f), Icons.Default.TrendingUp, Color(0xFF9333EA), "$totalCount", "총 시도")
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(2.dp, Color(0xFFE9D5FF))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("📊", fontSize = 24.sp, modifier = Modifier.padding(bottom = 4.dp))
                                Text("$successRate%", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text("성공률", fontSize = 12.sp, color = Color(0xFF4B5563))
                            }
                        }
                    }
                }

                // [기록 리스트]
                if (history.isEmpty()) {
                    // 기록 없음 (로딩 중이거나 진짜 없을 때)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFF3F4F6),
                                modifier = Modifier.size(80.dp)
                            ) { Box(contentAlignment = Alignment.Center) { Text("📝", fontSize = 40.sp) } }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("기록을 불러오고 있거나 없습니다", fontSize = 18.sp, color = Color(0xFF4B5563), fontWeight = FontWeight.Medium)
                        }
                    }
                } else {
                    // 리스트 표시
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
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

// --- 하위 컴포넌트 (StatCard, HistoryItemCard)는 기존 코드 그대로 유지 ---
@Composable
private fun StatCard(modifier: Modifier = Modifier, icon: ImageVector, iconColor: Color, value: String, label: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, Color(0xFFE9D5FF))
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp).padding(bottom = 4.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = label, fontSize = 12.sp, color = Color(0xFF4B5563))
        }
    }
}

@Composable
private fun HistoryItemCard(record: HistoryRecord) {
    val backgroundColor = if (record.success) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
    val borderColor = if (record.success) Color(0xFFBBF7D0) else Color(0xFFFECACA)
    val iconColor = if (record.success) Color(0xFF22C55E) else Color(0xFFEF4444)
    val iconVector = if (record.success) Icons.Default.Check else Icons.Default.Close
    val badgeText = if (record.success) "성공" else "실패"
    val badgeColor = if (record.success) Color(0xFF0F172A) else Color(0xFFEF4444)

    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(2.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = iconColor, modifier = Modifier.size(32.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = iconVector, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                    Surface(shape = RoundedCornerShape(4.dp), color = badgeColor) {
                        Text(text = badgeText, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }
                Text(text = record.date, fontSize = 12.sp, color = Color(0xFF6B7280))
            }

            Text(text = "미션", fontSize = 14.sp, color = Color(0xFF4B5563), modifier = Modifier.padding(bottom = 4.dp))
            Text(text = record.mission, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))

            if (record.userOrder.isNotEmpty()) {
                Text(text = "주문 내역", fontSize = 14.sp, color = Color(0xFF4B5563), modifier = Modifier.padding(bottom = 4.dp))
                val orderText = record.userOrder.joinToString(", ") { item -> "${item.name} ${item.quantity}개" }
                Text(text = orderText, fontSize = 14.sp, color = Color(0xFF1F2937))
            }
        }
    }
}