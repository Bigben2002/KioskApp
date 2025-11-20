package com.example.kiosk.ui.screens.cafe

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
import com.example.kiosk.data.model.CartItem
import com.example.kiosk.ui.components.KioskCard
import java.text.NumberFormat
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date

// 카페 테마 색상
private val CafeThemeColor = Color(0xFF6F4E37)

// ------------------------------------------------------------
// 1. 결제 방식 선택 화면 (카페 버전)
// ------------------------------------------------------------
@Composable
fun CafePaymentMethodSelectScreen(
    onPaid: (String) -> Unit,
    onBack: () -> Unit
) {
    var method by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(24.dp)) {

        Spacer(Modifier.height(80.dp))

        Text("결제 수단을 선택해주세요", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(Modifier.weight(1f)) {
                PaymentMethodCard("신용카드", Icons.Default.CreditCard, method == "CARD") { method = "CARD" }
            }
            Box(Modifier.weight(1f)) {
                PaymentMethodCard("QR/바코드", Icons.Default.QrCodeScanner, method == "QR") { method = "QR" }
            }
        }

        Spacer(Modifier.weight(1f))

        Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5E7EB), contentColor = Color.Black)
            ) {
                Text("이전", fontSize = 18.sp)
            }

            Button(
                onClick = { onPaid(method!!) },
                enabled = method != null,
                modifier = Modifier.weight(1f).height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CafeThemeColor,
                    disabledContainerColor = Color(0xFFD1D5DB)
                )
            ) {
                Text("결제하기", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
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
    val borderColor = if (selected) CafeThemeColor else Color(0xFFE5E7EB)
    val backgroundColor = if (selected) CafeThemeColor.copy(alpha = 0.1f) else Color.White
    val contentColor = if (selected) CafeThemeColor else Color(0xFF374151)

    KioskCard(borderColor = borderColor, backgroundColor = backgroundColor, onClick = onClick) {
        Column(
            Modifier.padding(24.dp).height(120.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(16.dp))
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = contentColor)
        }
    }
}

// ------------------------------------------------------------
// 2. 안내 화면들 (카드 삽입 / QR 스캔 / 로딩)
// ------------------------------------------------------------
@Composable
fun CafePaymentCardInsertScreen() {
    GuideScreen(
        icon = Icons.Default.SimCard,
        text = "IC칩이 위로 향하게\n카드를 끝까지 넣어주세요"
    )
}

@Composable
fun CafePaymentQrScanScreen() {
    GuideScreen(
        icon = Icons.Default.QrCodeScanner,
        text = "바코드 또는 QR코드를\n스캐너에 대주세요"
    )
}

@Composable
fun CafePaymentProcessingScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(64.dp), color = CafeThemeColor)
        Spacer(Modifier.height(32.dp))
        Text("결제 진행 중입니다...", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("카드를 빼지 말고 잠시만 기다려주세요", fontSize = 16.sp, color = Color.Gray, modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun GuideScreen(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(120.dp), tint = CafeThemeColor)
        Spacer(Modifier.height(32.dp))
        Text(text, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = 30.sp)
    }
}

// ------------------------------------------------------------
// 3. 결제 완료 (영수증) 화면 - 카페 전용 수정
// ------------------------------------------------------------
@Composable
fun CafePaymentSuccessScreen(
    cart: List<CartItem>,
    totalPrice: Int,
    diningMethod: String, // "매장" or "포장"
    isPracticeMode: Boolean,
    onDone: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA)
    val now = Date()
    // 주문 번호 생성 (랜덤)
    val orderNumber = remember { (100..999).random() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(100.dp))

        // 체크 아이콘
        Surface(shape = CircleShape, color = CafeThemeColor, modifier = Modifier.size(80.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("결제가 완료되었습니다!", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("주문번호를 확인하고 기다려주세요", fontSize = 16.sp, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))

        Spacer(modifier = Modifier.height(32.dp))

        // 영수증 카드
        KioskCard(
            backgroundColor = Color(0xFFF9FAFB),
            borderColor = Color(0xFFE5E7EB),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // 주문 번호 강조
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("주문번호", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    Text("$orderNumber", fontSize = 32.sp, fontWeight = FontWeight.Black, color = CafeThemeColor)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // 주문 정보
                ReceiptRow("주문형태", diningMethod)
                ReceiptRow("주문일시", dateFormat.format(now))

                Spacer(modifier = Modifier.height(16.dp))
                Text("주문 내역", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))

                // 장바구니 목록 출력
                cart.forEach { item ->
                    CafeReceiptItemRow(item)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // 총 금액
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("총 결제금액", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${NumberFormat.getNumberInstance(Locale.KOREA).format(totalPrice)}원",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = CafeThemeColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        val buttonText = if (isPracticeMode) "처음으로 돌아가기" else "결과 확인하기"

        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CafeThemeColor)
        ) {
            Text(buttonText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 16.sp)
        Text(value, color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun CafeReceiptItemRow(item: CartItem) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. 메뉴 이름 및 옵션 표시
        Column(modifier = Modifier.weight(1f)) {
            Text(item.menuItem.name, fontSize = 16.sp, fontWeight = FontWeight.Medium)

            // ✅ [수정] 리스트(selectedOptions)가 있으면 내용을 보여줌
            if (item.selectedOptions.isNotEmpty()) {
                // 예: "HOT, 샷 추가"
                val optionsString = item.selectedOptions.joinToString(", ") { it.name }
                Text("└ $optionsString", fontSize = 14.sp, color = Color.Gray)
            }
            // (기존 버거 호환용: 단일 옵션이 있으면 보여줌)
            else if (item.selectedOption != null) {
                Text("└ ${item.selectedOption.name}", fontSize = 14.sp, color = Color.Gray)
            }
        }

        // 2. 수량 및 가격 표시
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${item.quantity}개", fontSize = 16.sp, color = Color.Black)
            Spacer(modifier = Modifier.width(16.dp))

            // ✅ [수정] 가격 계산도 리스트 옵션을 포함하도록 수정
            val optionsPrice = item.selectedOptions.sumOf { it.price } + (item.selectedOption?.price ?: 0)
            val price = (item.menuItem.price + optionsPrice) * item.quantity

            Text(
                "${NumberFormat.getNumberInstance(Locale.KOREA).format(price)}원",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}