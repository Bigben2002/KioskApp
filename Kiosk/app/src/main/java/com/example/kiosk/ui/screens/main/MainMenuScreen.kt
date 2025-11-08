package com.example.kiosk.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kiosk.ui.components.KioskCard
import com.example.kiosk.ui.theme.KioskTheme

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
            .background(Brush.verticalGradient(listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))))
    ) {
        // 상단바
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
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E40AF))
        )

        // 메인 컨텐츠
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp) // 전체 화면에 16dp 패딩 적용
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            // 스마트폰 아이콘
            Surface(
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier.size(80.dp),
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("📱", fontSize = 40.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("키오스크 연습하기", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Text("천천히 배우고 익숙해지세요", fontSize = 18.sp, color = Color(0xFFDBEAFE))

            Spacer(modifier = Modifier.height(32.dp))

            // === [수정됨] 연습 모드 카드 ===
            // modifier를 통해 높이(180dp)와 가로 채우기(fillMaxWidth)를 명시적으로 지정
            MenuCard(
                title = "연습 모드",
                desc = "단계별 안내 제공",
                longDesc = "화면에 나오는 안내를 따라하며 천천히 배워보세요",
                icon = Icons.Default.MenuBook,
                gradientColors = listOf(Color(0xFF3B82F6), Color(0xFF2563EB)),
                textColor = Color(0xFFEFF6FF),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp), // 높이 고정
                onClick = onNavigateToPractice
            )

            Spacer(modifier = Modifier.height(16.dp))

            // === [수정됨] 실전 모드 카드 ===
            // 연습 모드와 완전히 동일한 modifier 적용
            MenuCard(
                title = "실전 모드",
                desc = "미션 완수하기",
                longDesc = "주어진 미션을 완수하며 실력을 키워보세요",
                icon = Icons.Default.Bolt,
                gradientColors = listOf(Color(0xFF22C55E), Color(0xFF16A34A)),
                textColor = Color(0xFFF0FDF4),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp), // 높이 고정
                onClick = onNavigateToReal
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 하단 버튼들
            OutlinedButton(
                onClick = onOpenHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1D4ED8)
                ),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1D4ED8)
                ),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFBFDBFE)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Help, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("사용 방법 보기", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// === [수정됨] MenuCard 컴포넌트 정의 ===
// 전달받은 modifier를 KioskCard에 그대로 적용하도록 수정되었습니다.
@Composable
fun MenuCard(
    title: String,
    desc: String,
    longDesc: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    textColor: Color,
    modifier: Modifier = Modifier, // 기본값
    onClick: () -> Unit
) {
    KioskCard(
        onClick = onClick,
        modifier = modifier, // 👈 전달받은 modifier를 그대로 사용 (fillMaxWidth 강제 제거)
        backgroundColor = Color.Transparent,
        borderColor = Color.Transparent
    ) {
        // 내부 컨텐츠 영역 (높이가 늘어나면 이 부분도 같이 늘어나도록 weight 사용 가능하지만, 현재는 패딩으로 처리)
        Column(
            modifier = Modifier
                .fillMaxSize() // 카드가 커지면 내부 배경도 가득 채움
                .background(Brush.linearGradient(gradientColors))
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween // 상하 내용 분산 배치
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(desc, fontSize = 14.sp, color = textColor)
                }
            }

            // 설명 텍스트와 시작하기 버튼 사이의 간격 확보
            Spacer(modifier = Modifier.height(12.dp))

            Column {
                Text(longDesc, color = textColor, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "시작하기 →",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainMenuScreenPreview() {
    KioskTheme {
        MainMenuScreen(
            onNavigateToPractice = {},
            onNavigateToReal = {},
            onOpenHelp = {},
            onOpenHistory = {}
        )
    }
}