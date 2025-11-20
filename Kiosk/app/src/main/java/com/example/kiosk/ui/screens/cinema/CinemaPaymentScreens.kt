package com.example.kiosk.ui.screens.cinema

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
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
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kiosk.ui.components.KioskButton
import com.example.kiosk.ui.components.KioskCard
import java.text.NumberFormat
import java.util.Locale

// ------------------------------------------------------------
// 1. 결제 방식 선택 화면 (기존 유지)
// ------------------------------------------------------------
@Composable
fun PaymentMethodSelectScreen(
    onPaid: (String) -> Unit,
    onBack: () -> Unit
) {
    var method by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("결제 방식 선택", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f)) {
                PaymentMethodCard("카드 결제", Icons.Default.SimCard, method == "CARD") { method = "CARD" }
            }
            Box(Modifier.weight(1f)) {
                PaymentMethodCard("QR 결제", Icons.Default.QrCodeScanner, method == "QR") { method = "QR" }
            }
        }
        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).height(56.dp)) { Text("이전") }
            KioskButton(onClick = { onPaid(method!!) }, enabled = method != null, modifier = Modifier.weight(1f).height(56.dp)) { Text("결제 완료") }
        }
    }
}

@Composable
private fun PaymentMethodCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val border = if (selected) Color(0xFF2563EB) else Color(0xFFE5E7EB)
    KioskCard(borderColor = border, onClick = onClick) {
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
// 2. '카드 삽입' 안내 화면 (기존 유지)
// ------------------------------------------------------------
@Composable
fun PaymentCardInsertScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
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
// 'QR 스캔' 안내 화면 (기존 유지)
// ------------------------------------------------------------
@Composable
fun PaymentQrScanScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
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
// 3. '결제 중' 로딩 화면 (기존 유지)
// ------------------------------------------------------------
@Composable
fun PaymentProcessingScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(64.dp))
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


// ------------------------------------------------------------
// 4. '티켓 결제 완료' 영수증 화면 (연습 모드용 - 기존 유지)
// ------------------------------------------------------------
@SuppressLint("NewApi")
@Composable
fun PaymentSuccessScreen_Ticket(
    movie: MovieItem?,
    time: String?,
    theater: TheaterOption?,
    seats: List<String>,
    dateMillis: Long,
    adultCount: Int,
    childCount: Int,
    seniorCount: Int,
    totalPrice: Int,
    onDone: () -> Unit,
    onAgain: () -> Unit
) {
    val dateText = remember(dateMillis) {
        SimpleDateFormat("yyyy.MM.dd (E)", Locale.KOREA).format(dateMillis)
    }
    val themeColor = Color(0xFF16A34A) // Green

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // 결과 아이콘
        Surface(
            shape = CircleShape,
            color = themeColor,
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // 결과 메시지
        Text(
            text = "결제 완료!",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "티켓 예매가 완료되었습니다\n영화관에서 티켓을 출력해주세요",
            fontSize = 18.sp,
            color = Color(0xFF4B5563), // gray-600
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        // 영수증 카드
        KioskCard(
            backgroundColor = Color(0xFFF9FAFB), // gray-50
            borderColor = Color(0xFFE5E7EB),     // gray-200
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("주문 내역", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

                // --- 항목 ---
                ReceiptRow(label = "영화", value = movie?.title ?: "-")
                ReceiptRow(label = "일시", value = "$dateText ${time ?: "-"}")
                ReceiptRow(label = "상영관", value = theater?.name ?: "-")

                val peopleDetail = mutableListOf<String>()
                if (adultCount > 0) peopleDetail.add("성인 ${adultCount}명")
                if (childCount > 0) peopleDetail.add("아이 ${childCount}명")
                if (seniorCount > 0) peopleDetail.add("우대 ${seniorCount}명")
                ReceiptRow(label = "인원", value = peopleDetail.joinToString(", ").ifEmpty { "-" })

                ReceiptRow(label = "좌석", value = if (seats.isEmpty()) "-" else seats.joinToString(", "))

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // --- 총 금액 ---
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

        Spacer(modifier = Modifier.height(32.dp))

        // 하단 버튼
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onAgain, modifier = Modifier.height(52.dp)) { Text("다시 홈으로") }
            KioskButton(onClick = onDone, modifier = Modifier.height(52.dp)) { Text("완료 (종료)") }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ------------------------------------------------------------
// 미션 결과 영수증 화면 (실전 모드용)
// ------------------------------------------------------------
@SuppressLint("NewApi")
@Composable
fun MissionResultScreen_Ticket(
    movie: MovieItem?,
    time: String?,
    theater: TheaterOption?,
    seats: List<String>,
    dateMillis: Long,
    adultCount: Int,
    childCount: Int,
    seniorCount: Int,
    totalPrice: Int,
    missionResultText: String,
    onDone: () -> Unit,
    onAgain: () -> Unit
) {
    val dateText = remember(dateMillis) {
        SimpleDateFormat("yyyy.MM.dd (E)", Locale.KOREA).format(dateMillis)
    }
    // 결과 텍스트를 기준으로 색상 결정
    val themeColor = when {
        missionResultText.contains("100%") -> Color(0xFF16A34A) // Green
        missionResultText.contains("50%") -> Color(0xFFCA8A04) // Yellow (Amber)
        else -> Color(0xFFDC2626) // Red
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // 결과 아이콘
        Surface(
            shape = CircleShape,
            color = themeColor,
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // 결과 메시지
        Text(
            text = "미션 결과: $missionResultText",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = themeColor
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = when {
                missionResultText.contains("100%") -> "모든 미션을 완벽하게 수행했습니다! 🎉"
                missionResultText.contains("50%") -> "부분적으로 미션을 수행했습니다. 다시 도전해보세요!"
                else -> "미션 수행에 실패했습니다. 다음 기회에 다시 시도해보세요."
            },
            fontSize = 18.sp,
            color = Color(0xFF4B5563),
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        // 영수증 카드
        KioskCard(
            backgroundColor = Color(0xFFF9FAFB),
            borderColor = Color(0xFFE5E7EB),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("주문 내역", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

                ReceiptRow(label = "영화", value = movie?.title ?: "-")
                ReceiptRow(label = "일시", value = "$dateText ${time ?: "-"}")
                ReceiptRow(label = "상영관", value = theater?.name ?: "-")

                val peopleDetail = mutableListOf<String>()
                if (adultCount > 0) peopleDetail.add("성인 ${adultCount}명")
                if (childCount > 0) peopleDetail.add("아이 ${childCount}명")
                if (seniorCount > 0) peopleDetail.add("우대 ${seniorCount}명")
                ReceiptRow(label = "인원", value = peopleDetail.joinToString(", ").ifEmpty { "-" })

                ReceiptRow(label = "좌석", value = if (seats.isEmpty()) "-" else seats.joinToString(", "))

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

        Spacer(modifier = Modifier.height(32.dp))

        // 하단 버튼
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onAgain, modifier = Modifier.height(52.dp)) { Text("다시 도전") }

            // ✅ 오류 수정: KioskButton 대신 Material3 Button을 사용하여 colors 인자 전달
            Button(
                onClick = onDone,
                modifier = Modifier.height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColor), // 동적 색상 적용
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("완료 (종료)")
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// 영수증 행 (공통)
@Composable
fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color(0xFF6B7280),
            modifier = Modifier.widthIn(min = 60.dp)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            color = Color(0xFF1F2937),
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}