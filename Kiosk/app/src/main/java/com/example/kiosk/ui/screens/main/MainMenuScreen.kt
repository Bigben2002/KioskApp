package com.example.kiosk.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kiosk.ui.components.KioskCard
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    onNavigateToPractice: () -> Unit,
    onNavigateToReal: () -> Unit,
    onOpenHelp: () -> Unit,
    onOpenHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF2563EB), Color(0xFF1D4ED8)))) // blue-600 to blue-700
    ) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Menu, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("키오스크 연습", color = Color.White, fontSize = 20.sp)
                }
            },
            actions = {
                IconButton(onClick = onOpenHistory) {
                    Icon(Icons.Default.BarChart, contentDescription = "기록", tint = Color.White)
                }
                IconButton(onClick = onOpenHelp) {
                    Icon(Icons.AutoMirrored.Filled.Help, contentDescription = "도움말", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E40AF)) // blue-800
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(80.dp), shadowElevation = 4.dp) {
                Box(contentAlignment = Alignment.Center) { Text("📱", fontSize = 40.sp) }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("키오스크 연습하기", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Text("천천히 배우고 익숙해지세요", fontSize = 18.sp, color = Color(0xFFDBEAFE)) // blue-100
            Spacer(modifier = Modifier.height(32.dp))

            MenuCard(
                title = "연습 모드",
                desc = "단계별 안내 제공",
                longDesc = "화면에 나오는 안내를 따라하며 천천히 배워보세요",
                icon = Icons.Default.MenuBook,
                gradientColors = listOf(Color(0xFF3B82F6), Color(0xFF2563EB)), // blue-500 to blue-600
                textColor = Color(0xFFEFF6FF), // blue-50
                onClick = onNavigateToPractice
            )
            Spacer(modifier = Modifier.height(16.dp))
            MenuCard(
                title = "실전 모드",
                desc = "미션 완수하기",
                longDesc = "주어진 미션을 완수하며 실력을 키워보세요",
                icon = Icons.Default.Bolt,
                gradientColors = listOf(Color(0xFF22C55E), Color(0xFF16A34A)), // green-500 to green-600
                textColor = Color(0xFFF0FDF4), // green-50
                onClick = onNavigateToReal
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = onOpenHistory,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Color(0xFF1D4ED8)),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFBFDBFE)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.BarChart, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("학습 기록 확인", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onOpenHelp,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Color(0xFF1D4ED8)),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFBFDBFE)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Help, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("사용 방법 보기", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("실제 결제가 진행되지 않는\n안전한 연습 환경입니다", textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color(0xFFDBEAFE), fontSize = 14.sp)
        }
    }
}

@Composable
fun MenuCard(title: String, desc: String, longDesc: String, icon: ImageVector, gradientColors: List<Color>, textColor: Color, onClick: () -> Unit) {
    KioskCard(onClick = onClick, modifier = Modifier.fillMaxWidth(), backgroundColor = Color.Transparent, borderColor = Color.Transparent) {
        Column(modifier = Modifier.background(Brush.linearGradient(gradientColors)).padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(56.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp)) }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(desc, fontSize = 14.sp, color = textColor)
                }
            }
            Text(longDesc, color = textColor, modifier = Modifier.padding(bottom = 16.dp))
            Text("시작하기 →", color = Color.White, fontSize = 14.sp, modifier = Modifier.align(Alignment.End))
        }
    }
}

// ✅ 여기에 미리보기 전용 함수를 새로 만듭니다.
@Preview(showBackground = true)
@Composable
fun MainMenuScreenPreview() {
    // 실제 화면 함수를 호출하면서, 빈 이벤트 핸들러({})를 넣어줍니다.
    MainMenuScreen(
        onNavigateToPractice = {},
        onNavigateToReal = {},
        onOpenHelp = {},
        onOpenHistory = {}
    )
}