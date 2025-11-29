package com.example.kiosk.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kiosk.data.model.KioskType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeKioskSelectScreen(
    onSelect: (KioskType) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("연습할 매장을 선택하세요", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E40AF))
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))
                    )
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SelectCard(
                    type = KioskType.BURGER,
                    onClick = onSelect,
                    modifier = Modifier.weight(1f)
                )
                SelectCard(
                    type = KioskType.CAFE,
                    onClick = onSelect,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SelectCard(
                    type = KioskType.CINEMA,
                    onClick = onSelect,
                    modifier = Modifier.weight(1f)
                )
                SelectCard(
                    type = KioskType.RESTAURANT,
                    onClick = onSelect,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text("아이콘을 눌러 연습을 시작하세요", color = Color(0xFFDBEAFE))
        }
    }
}

@Composable
private fun SelectCard(
    type: KioskType,
    onClick: (KioskType) -> Unit,
    modifier: Modifier = Modifier,
    disabled: Boolean = false
) {
    val bg = Color.White.copy(alpha = 0.15f)
    val border = if (disabled) Color.White.copy(alpha = 0.2f) else Color.White
    val label = if (disabled) "${type.title}\n(준비중)" else type.title

    Surface(
        modifier = modifier
            .height(160.dp)
            .clickable(enabled = !disabled) { onClick(type) },
        color = bg,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 2.dp,
            brush = Brush.horizontalGradient(listOf(border, border))
        )
    ) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(type.icon, fontSize = 42.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                label,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 22.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
