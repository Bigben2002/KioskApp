// app/src/main/java/com/example/kiosk/ui/screens/cinema/CinemaDialogs.kt
package com.example.kiosk.ui.screens.cinema

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.kiosk.ui.components.KioskButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableDialog(
    movies: List<MovieItem>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("전체 상영 시간표", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "닫기")
                    }
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(movies) { movie ->
                        Column {
                            Text(
                                text = movie.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(movie.showTimes.size) { i ->
                                    val t = movie.showTimes[i]
                                    AssistChip(
                                        onClick = { /* 팝업에서는 선택 안 함 */ },
                                        label = { Text(t) },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 좌석 선택 안내 팝업
@Composable
fun SeatInstructionDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("좌석 선택 안내", fontWeight = FontWeight.Bold)
        },
        text = {
            Text("인원 수에 맞게 좌석을 선택해야 합니다.\n이미 선택된 좌석은 짙은 회색으로 되어 있고 선택이 불가능합니다.")
        },
        confirmButton = {
            KioskButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("닫기")
            }
        }
    )
}

// ------------------------------------------------------------
// 예매 번호 입력 팝업
// ------------------------------------------------------------
@Composable
fun BookingNumberInputDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("예매 번호 입력") },
        text = {
            Column {
                Text("12자리 예매 번호를 입력하세요.")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        // 숫자만, 12자리까지만 입력 가능
                        if (it.length <= 12 && it.all { c -> c.isDigit() }) {
                            text = it
                        }
                    },
                    label = { Text("예매 번호 (숫자 12자리)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            KioskButton(
                onClick = { onConfirm(text) },
                enabled = text.length == 12 // 12자리가 입력되었을 때만 활성화
            ) { Text("조회") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("취소") }
        }
    )
}

// ------------------------------------------------------------
// ✅ 예매 티켓 결과 (영수증) 팝업 (수정됨)
// ------------------------------------------------------------
@Composable
fun BookingResultDialog(
    ticket: BookedTicket,
    movie: MovieItem?,     // 찾은 영화 정보
    theater: TheaterOption?, // 찾은 상영관 정보
    onDismiss: () -> Unit,
    onPrintTicket: () -> Unit  // ✅ "실물 티켓 출력" 콜백 추가
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // 헤더
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("예매 티켓 확인", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "닫기")
                    }
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))

                // --- 본문 (영수증) ---
                ReceiptRow(label = "예매 번호", value = ticket.bookingNumber)
                ReceiptRow(label = "영화", value = movie?.title ?: "-")
                ReceiptRow(label = "상영관", value = theater?.name ?: "-")
                ReceiptRow(label = "시간", value = ticket.time)

                val peopleDetail = mutableListOf<String>()
                if (ticket.adultCount > 0) peopleDetail.add("성인 ${ticket.adultCount}명")
                if (ticket.childCount > 0) peopleDetail.add("아이 ${ticket.childCount}명")
                if (ticket.seniorCount > 0) peopleDetail.add("우대 ${ticket.seniorCount}명")
                ReceiptRow(label = "인원", value = peopleDetail.joinToString(", ").ifEmpty { "-" })

                ReceiptRow(label = "좌석", value = ticket.seats.joinToString(", "))

                Spacer(Modifier.height(24.dp))

                // --- QR 코드 표시 ---
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "QR 코드",
                        modifier = Modifier.size(100.dp),
                        tint = Color(0xFF374151)
                    )
                    Text(
                        "티켓 출력을 위해 QR코드를 스캔하세요",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                // --- ---

                Spacer(Modifier.height(24.dp))

                // --- ✅ [요청] 하단 버튼 (2개로 수정) ---
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDismiss, // "닫기"
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("닫기")
                    }
                    KioskButton(
                        onClick = onPrintTicket, // "실물 티켓 출력"
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("실물 티켓 출력")
                    }
                }
            }
        }
    }
}