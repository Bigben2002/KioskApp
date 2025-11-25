package com.example.kiosk.ui.screens.burger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.QrCodeScanner
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
import java.text.NumberFormat
import java.util.Locale

// ------------------------------------------------------------
// 1. 결제 방식 선택 화면
// ------------------------------------------------------------
@Composable
fun PaymentMethodSelectScreen(
    onPaid: (String) -> Unit,
    onBack: () -> Unit
) {
    var method by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text("결제 방식 선택", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f)) {
                PaymentMethodCard("카드 결제", Icons.Default.CreditCard, method == "CARD") { method = "CARD" }
            }
            Box(Modifier.weight(1f)) {
                PaymentMethodCard("QR 결제", Icons.Default.QrCodeScanner, method == "QR") { method = "QR" }
            }
        }
        Spacer(Modifier.weight(1f))
        Row(
            Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(56.dp)
            ) {
                Text("이전")
            }
            Button(
                onClick = { onPaid(method!!) },
                enabled = method != null,
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
            ) {
                Text("결제 완료")
            }
        }
    }
}

@Composable
private fun PaymentMethodCard(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) Color(0xFF2563EB) else Color(0xFFE5E7EB)
    val backgroundColor = if (selected) Color(0xFFF0F9FF) else Color.White

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor)
    ) {
        Column(
            Modifier.padding(16.dp).height(100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF111827), modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, fontSize = 16.sp)
        }
    }
}

// ------------------------------------------------------------
// 2. '카드 삽입' 안내 화면
// ------------------------------------------------------------
@Composable
fun PaymentCardInsertScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CreditCard,
            contentDescription = null,
            modifier = Modifier.size(128.dp),
            tint = Color(0xFF374151)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "카드를 단말기에 삽입해주세요",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ------------------------------------------------------------
// 3. 'QR 스캔' 안내 화면
// ------------------------------------------------------------
@Composable
fun PaymentQrScanScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = null,
            modifier = Modifier.size(128.dp),
            tint = Color(0xFF374151)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "QR코드를 QR코드 리더기에 맞춰주세요",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

// ------------------------------------------------------------
// 4. '결제 중' 로딩 화면
// ------------------------------------------------------------
@Composable
fun PaymentProcessingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = Color(0xFFDC2626)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "결제 중입니다...",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "카드를 빼지 마세요",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}